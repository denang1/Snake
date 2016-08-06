import javax.swing.*;

import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.awt.geom.Point2D;

public class GameRunner extends JFrame implements WindowListener, KeyListener, MouseListener, MouseMotionListener, Runnable
{		
	private GraphicsDevice gd = null; //Needed for fullscreen mode.
	private BufferStrategy strategy = null; // Needed for double buffering.
	
	private Thread thread;
	private boolean shouldStop;
	
	private int fps = 20;
	private long period;
	private double effectiveFPS;
	private double effectiveUPS;
	
	//locking object used to synchronize event dispatch thread and game thread
	private Object lock = new Object();
		
	/*SCREEN SCALING PART 1*/
	private int desiredWidth = 1280;
	private int desiredHeight = 1024;
	private AffineTransform screenScale;
	private AffineTransform inverseScale = new AffineTransform();
	/*END PART 1*/
	
	private SnakeScreen screen;
	
	public static void main(String[] args) 
	{
		new GameRunner();
	}
	
	public GameRunner() {
		
		super(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
		setIgnoreRepaint(true);
		
		try {
			init();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			exit();
		}
	}
	
	/**
	 * Called by the constructors, this initializes fields and
	 * sets up the Graphics environment for display.
	 * @throws Exception
	 */
	private void init() throws Exception {
		//Set window properties
		setTitle("Game Window");
		setFocusTraversalKeysEnabled(false);
		
		setResizable(false);
		setIgnoreRepaint(true);
		setUndecorated(true);
		
		//Set the fullscreen
		gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		if (!gd.isFullScreenSupported())
			throw new Exception("Fullscreen is not supported");
		
		/*SCREEN SCALING PART 2*/
		gd.setFullScreenWindow(this);
		pickBestDisplayMode();
		
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(d);
		/*END PART 2*/
		
		setLayout(null);
		
		//Add action all of the listeners
		addKeyListener(this);
		addWindowListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		
		setFocusable(true);
		requestFocus();
		
		//Show the screen and make the buffers
		setVisible(true);
		createBufferStrategy(2);
		strategy = getBufferStrategy();
		
		dataInit();
		
		createThread();
		startThread();
	}
	
	/*SCREEN SCALING PART 3*/
	private void pickBestDisplayMode() {
		DisplayMode oldMode = gd.getDisplayMode();
		
		try
		{
			gd.setDisplayMode(new DisplayMode(desiredWidth, desiredHeight, 32, DisplayMode.REFRESH_RATE_UNKNOWN));
		}
		catch(Exception e)
		{
			gd.setDisplayMode(oldMode);
			
			int currentWidth = oldMode.getWidth();
			int currentHeight = oldMode.getHeight();
			
			double widthRatio = currentWidth / (double)desiredWidth;
			double heightRatio = currentHeight / (double)desiredHeight;
			
			double scaleFactor = 1.0;
			
			double xTrans = 0;
			double yTrans = 0;
			
			if(desiredWidth <= currentWidth && desiredHeight <= currentHeight)
			{
				scaleFactor = 1.0;
			}
			else if(desiredWidth >= currentWidth && desiredHeight >= currentHeight) {
				scaleFactor = Math.min(widthRatio, heightRatio);
			}
			else if(desiredWidth >= currentWidth) {
				scaleFactor = widthRatio;
			}
			else //desiredHeight > height
			{
				scaleFactor = heightRatio;
			}
			
			
			xTrans = (currentWidth - desiredWidth*scaleFactor)/(2 * scaleFactor);
			yTrans = (currentHeight - desiredHeight*scaleFactor)/(2 * scaleFactor);
			screenScale = AffineTransform.getScaleInstance(scaleFactor, scaleFactor);
			screenScale.translate(xTrans, yTrans);
			
			try {
				inverseScale = screenScale.createInverse();
			}
			catch (Exception exc) {
				exc.printStackTrace();
			}
		}		
	}
	/*END PART 3*/
	
	public void dataInit()
	{
		screen = new SnakeScreen(desiredWidth, desiredHeight);
	}
	
	public void setFPS(int f)
	{
		synchronized(lock)
		{
			fps = f;
			period = (long)((1000.0/fps) * 1000000L);
		}
	}
	
	public void windowClosing(WindowEvent e) {
		exit();
	}
	public void windowActivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	public void keyReleased(KeyEvent e){}
	public void keyTyped(KeyEvent e) {}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	
	public void mouseClicked(MouseEvent e)
	{
		Point2D p = inverseScale.transform(e.getPoint(), null);
	}
	
	public void mouseDragged(MouseEvent e)
	{
		Point2D p = inverseScale.transform(e.getPoint(), null);
	}
	
	public void mouseMoved(MouseEvent e)
	{
		Point2D p = inverseScale.transform(e.getPoint(), null);
	}
	
	public void mouseReleased(MouseEvent e)
	{
		Point2D p = inverseScale.transform(e.getPoint(), null);
	}
	
	public void mousePressed(MouseEvent e)
	{
		Point2D p = inverseScale.transform(e.getPoint(), null);
	}
	
	//quits game when escape is pressed
	public void keyPressed(KeyEvent e) 
	{	
		int code = e.getKeyCode();
		if(code == KeyEvent.VK_ESCAPE)
			exit();
		
		screen.keyPressed(e);
	}
	
	public void render(Graphics g)
	{
		screen.render(g);
	}
	
	public void update()
	{
		screen.update();
	}
	
	public void exit() {
		stopThread();
		if (gd != null)
			gd.setFullScreenWindow(null);
		dispose();
		System.exit(0);
	}
	
	public void run()
	{
		long beforeTime, afterTime, timeDiff, sleepTime;
		long overSleepTime = 0L;
		int noDelays = 0;
		long excess = 0L;
		
		synchronized(lock)
		{
			period = (long)((1000.0/fps) * 1000000L);
		}
		
		final int NO_DELAYS_PER_YIELD = 16;
		final int MAX_FRAME_SKIPS = 5;
		
		beforeTime = System.nanoTime();
		
		long time = System.nanoTime();
		long frameCounter = 0;
		long updateCounter = 0;
		
		while(!shouldStop)
		{
			synchronized (lock) {
				update();
				updateCounter++;
				
				paintScreen();
				
				afterTime = System.nanoTime();
				timeDiff = afterTime - beforeTime;
				sleepTime = (period - timeDiff) - overSleepTime;
			}
			
			if(sleepTime > 0)
			{
				try
				{	
					TimeUnit.NANOSECONDS.sleep(sleepTime);
				}
				catch(Exception e)
				{
				}
				overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
			}
			else
			{				
				excess -= sleepTime;
				overSleepTime = 0L;
				
				noDelays++;
				if(noDelays >= NO_DELAYS_PER_YIELD)
				{
					Thread.yield();
					noDelays = 0;
				}
			}
			
			synchronized (lock)
			{
				int skips = 0;
				while(excess > period && skips < MAX_FRAME_SKIPS)
				{
					excess -= period;
					update();
					updateCounter++;
					
					skips++;
				}
			}
			
			frameCounter++;
			beforeTime = System.nanoTime();
			if(beforeTime - time > 1000000000L) //1 second has passed
			{
				effectiveFPS = (double)frameCounter * 1000000000L / (beforeTime - time);
				effectiveUPS = (double)updateCounter * 1000000000L / (beforeTime - time);
				
				frameCounter = 0;
				updateCounter = 0;
				time = beforeTime;
			}		
		}
		
		thread = null;
	}
	
	private void paintScreen()
	{
		if(strategy == null)
			return;
		
		Graphics g = strategy.getDrawGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		Graphics2D g2 = ((Graphics2D)g);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
		g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
		g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
		
		if(screenScale != null) {
			((Graphics2D)g).transform(screenScale);
			g.setClip(0, 0, desiredWidth, desiredHeight);
		}
		
		render(g);
		
		
		strategy.show();
	}
	
	private synchronized void createThread()
	{
		if(thread == null)
		{
			thread = new Thread(this);
			shouldStop = true;
		}
	}
	
	private synchronized void startThread()
	{
		if(thread != null && shouldStop == true)
		{
			shouldStop = false;
			thread.start();
		}
	}
	
	private synchronized void stopThread()
	{
		shouldStop = true;
	}
	
	public synchronized boolean isRunning()
	{
		return thread != null && shouldStop == false;
	}
}
