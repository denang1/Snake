import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.geom.*;
import javax.sound.sampled.*;
import java.net.URL;
import java.awt.image.BufferedImage;
//Dennis Angelov - Enjoy!
public class SnakeScreen
{
	private int width, height;
	
	private ArrayList<Snake> snakes = new ArrayList<>();
	private Snake player1;
	private Snake player2;
	private ArrayList<Food> food = new ArrayList<>();
	private final int BLOCK_SIZE = 16;
	private Graphics graphics;
	private int key1;
	private int key2;
	private int gameState = 0;
	private boolean hasInvincible1;
	private boolean hasInvincible2;
	private boolean hasGrow1;
	private boolean hasGrow2;
	private PowerUp powerUp = null;
	private int powerUp1DurationGrow = 200;
	private int powerUp1DurationInvincible = 200;
	private int powerUp2DurationGrow = 200;
	private int powerUp2DurationInvincible = 200;
	private boolean transforming1 = false;
	private boolean transforming2 = false;
	public int transformingCount1 = -1;
	public int transformingCount2 = -1;
	private Clip clip;
	private BufferedImage grow = ImageLoader.loadCompatibleImage("Mushroom.png");
	private BufferedImage superStar = ImageLoader.loadCompatibleImage("superstar.png");
	private BufferedImage fahrenbacher = ImageLoader.loadCompatibleImage("fahrenbacher.png");

	public SnakeScreen(int w, int h) {
		width = w;
		height = h;
		
		initLevel();
		playMusic("fivearmies.wav");
	}
	
	public void initLevel() {
		player1 = new Snake(width-16,height-16,180,16,Color.RED);
		player2 = new Snake(16,16,0,16,Color.BLUE);

		snakes.clear();

		snakes.add(player1);
		snakes.add(player2);
		
		key1 = KeyEvent.VK_RIGHT;
		key2 = KeyEvent.VK_A;

		randomFood();
		randomPowerUp();
	}
	
	public void render(Graphics g) {
		if(gameState == 1) {
			graphics = g;
			if(player1.isInvincible()) {
				player1.randomColors(g);
			}
			else {
				player1.render(g);
			}
			if(player2.isInvincible()) {
				player2.randomColors(g);
			}
			else {
				player2.render(g);
			}
			food.get(0).render(g);
			powerUp.render(g);
		}
		else if(gameState == 0) {
			g.setColor(Color.GRAY);
			g.setFont(new Font("Helvetica",25,60));
			g.drawString("SNAKE --- Press Enter to Play", 275, 200);
			g.setColor(Color.RED);
			g.drawString("Player 1 is Red",200,400);
			g.setColor(Color.BLUE);
			g.drawString("Player 2 is Blue",700,400);
			g.setFont(new Font("Monospaced",25,30));
			g.drawString("WASD",850,450);
			g.setColor(Color.RED);
			g.drawString("Arrow Keys",300,450);
			g.drawImage(grow,400,500,64,64,null);
			g.setColor(Color.WHITE);
			g.setFont(new Font("Monospaced",25,40));
			g.drawString("Beef Up & Speed Up",550,550);
			g.drawImage(superStar,400,700,64,64,null);
			g.drawString("Invincibility",550,750);
			g.drawImage(fahrenbacher,400,900,64,64,null);
			g.drawString("Food",550,950);

		}
		else if(gameState == 2) {
			g.setColor(Color.WHITE);
			g.setFont(new Font("Helvetica",25,60));
			g.drawString("Player 2 wins! Press Enter to restart",200,500);
		}
		else if(gameState == 3) {
			g.setColor(Color.WHITE);
			g.setFont(new Font("Helvetica",25,60));
			g.drawString("Player 1 wins! Press Enter to restart",200,500);
		}
		else if(gameState == 4) {
			g.setColor(Color.WHITE);
			g.setFont(new Font("Helvetica",25,60));
			g.drawString("It's a draw! Press Enter to restart", 200, 500);
		}

	}
	
	public void update() {
		if(gameState == 1) {
			if(player1.getSize() == 16 && transforming1) {
				if(transformingCount1 == -1) {
					transformingCount1 = player1.getSize();
				}
				else {
					transformingCount1--;
				}
				if(transformingCount1 == 0) {
					transforming1 = false;
					transformingCount1 = -1;
				}
			}
			if(player2.getSize() == 16 && transforming2) {
				if(transformingCount2 == -1) {
					transformingCount2 = player2.getSize();
				}
				else {
					transformingCount2--;
				}
				if(transformingCount2 == 0) {
					transforming2 = false;
					transformingCount2 = -1;
				}
			}

			if(hasGrow1) {
				powerUp1DurationGrow--;
				if(powerUp1DurationGrow <= 0) {
					hasGrow1 = false;
					powerUp1DurationGrow = 200;
					transforming1 = true;
					player1.setSize(16);
				}
			}
			if(hasInvincible1) {
				powerUp1DurationInvincible--;
				if(powerUp1DurationInvincible <= 0) {
					hasInvincible1 = false;
					powerUp1DurationInvincible = 200;
					player1.setInvincible(false);
				}
			}

			if(hasGrow2) {
				powerUp2DurationGrow--;
				if(powerUp2DurationGrow <= 0) {
					hasGrow2 = false;
					powerUp2DurationGrow = 200;
					transforming2 = true;
					player2.setSize(16);
				}
			}
			if(hasInvincible2) {
				powerUp2DurationInvincible--;
				if(powerUp2DurationInvincible <= 0) {
					hasInvincible2 = false;
					powerUp2DurationInvincible = 200;
					player2.setInvincible(false);
				}
			}
			if(!player1.isDead() && !player2.isDead()) {
				if(key1 == KeyEvent.VK_RIGHT && player1.getBody().get(0).y > 0 && player1.getBody().get(0).y < height) {
					player1.changeDirection(0);
				}
				if(key2 == KeyEvent.VK_D && player2.getBody().get(0).y > 0 && player2.getBody().get(0).y < height) {
					player2.changeDirection(0);
				}
				if(key1 == KeyEvent.VK_LEFT && player1.getBody().get(0).y > 0 && player1.getBody().get(0).y < height) {
					player1.changeDirection(180);
				}
				if(key2 == KeyEvent.VK_A && player2.getBody().get(0).y > 0 && player2.getBody().get(0).y < height) {
					player2.changeDirection(180);
				}
				if(key1 == KeyEvent.VK_UP && player1.getBody().get(0).x > 0 && player1.getBody().get(0).x < width) {
					player1.changeDirection(90);
				}
				if(key2 == KeyEvent.VK_W && player2.getBody().get(0).x > 0 && player2.getBody().get(0).x < width) {
					player2.changeDirection(90);
				}
				if(key1 == KeyEvent.VK_DOWN && player1.getBody().get(0).x > 0 && player1.getBody().get(0).x < width) {
					player1.changeDirection(270);
				}
				if(key2 == KeyEvent.VK_S && player2.getBody().get(0).x > 0 && player2.getBody().get(0).x < width) {
					player2.changeDirection(270);
				}
				if(player1.willIntersect(food.get(0))) {
					player1.move(true);
					randomFood();
					food.remove(food.get(0));
				}
				else {
					player1.move(false);
					if(powerUp.getType().equals("Grow")) {
						if(player1.intersectsGrow((Grow)powerUp)) {
							snakes.get(0).setSize(48);
			
							hasGrow1 = true;
							powerUp1DurationGrow = 200;
							transforming1 = true;
							powerUp = null;
							randomPowerUp();	
						}
						else if(player2.intersectsGrow((Grow)powerUp)) {
							snakes.get(1).setSize(48);
							
							hasGrow2 = true;
							powerUp2DurationGrow = 200;
							transforming2 = true;
							powerUp = null;
							randomPowerUp();
						}

					}

					else if(powerUp.getType().equals("SuperStar")) {
						if(player1.intersectsSuperStar((SuperStar)powerUp)) {
							snakes.get(0).setInvincible(true);
			
							hasInvincible1 = true;
							powerUp1DurationInvincible = 200;
							powerUp = null;
							randomPowerUp();	
						}
						else if(player2.intersectsSuperStar((SuperStar)powerUp)) {
							snakes.get(1).setInvincible(true);
							
							hasInvincible2 = true;
							powerUp2DurationInvincible = 200;
							powerUp = null;
							randomPowerUp();
						}
					}
					if(!transforming1 && player1.intersectsSelf()) {
						player1.setDead(true);
					}
					if(player1.getBody().get(0).intersects(player2.getBody().get(0)) || player2.getBody().get(0).intersects(player1.getBody().get(0))) {
						player1.setDead(true);
						player2.setDead(true);
					}
					else if(player1.intersects(player2)) {
						player1.setDead(true);
					}
				}
				if(player2.willIntersect(food.get(0))) {
					player2.move(true);
					randomFood();
					food.remove(food.get(0));
				}
				else {
					player2.move(false);
					if(!transforming2 && player2.intersectsSelf()) {
						player2.setDead(true);
					}
					else if(player2.intersects(player1)) {
						player2.setDead(true);
					}
				}
	
				if(player1.getBody().get(0).x > width) {
					player1.getBody().get(0).x = 0;
				}
				if(player1.getBody().get(0).x < 0) {
					player1.getBody().get(0).x = width;
				}
				if(player1.getBody().get(0).y > height) {
					player1.getBody().get(0).y = 0;
				}
				if(player1.getBody().get(0).y < 0) {
					player1.getBody().get(0).y = height;
				}

				if(player2.getBody().get(0).x > width) {
					player2.getBody().get(0).x = 0;
				}
				if(player2.getBody().get(0).x < 0) {
					player2.getBody().get(0).x = width;
				}
				if(player2.getBody().get(0).y > height) {
					player2.getBody().get(0).y = 0;
				}
				if(player2.getBody().get(0).y < 0) {
					player2.getBody().get(0).y = height;
				}
			}
			if(player1.isDead() && player2.isDead()) {
				gameState = 4;
			}
			else if(player1.isDead()) {
				gameState = 2;
				}
			else if(player2.isDead()) {
				gameState = 3;
			}
		}
	}
	
	public void randomFood() {
		int randomX = 0;
		int randomY = 0;
		label1:
		randomX = (int)(Math.random()*width/16)*16;
		randomY = (int)(Math.random()*height/16)*16;
		label2:
		for(Snake player: snakes) {
			for(int i = 0; i < player.getBody().size(); i++) {
				if((randomX == player.getBody().get(i).x && randomY == player.getBody().get(i).y)) {
					break label2;
				}
			}
		}

		Rectangle preFood = new Rectangle(randomX, randomY, 32, 32);
		Food nextMeal = new Food(preFood);
		food.add(nextMeal);
	}

	public void randomPowerUp() {
		int randomNum = (int)(Math.random()*2);
		int randomX = 0;
		int randomY = 0;
		label1:
		randomX = (int)(Math.random()*width/16)*16;
		randomY = (int)(Math.random()*height/16)*16;
		label2:
		for(Snake player: snakes) {
			for(int i = 0; i < player.getBody().size(); i++) {
				if((randomX == player.getBody().get(i).x && randomY == player.getBody().get(i).y)) {
					break label2;
				}
			}
		}

		Rectangle prePowerUp = new Rectangle(randomX, randomY, 32, 32);
		if(randomNum == 0) {
			powerUp = new Grow(prePowerUp);
		}
		if(randomNum == 1) {
			powerUp = new SuperStar(prePowerUp);
		}
	}

	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_LEFT) {
			key1 = e.getKeyCode();
		}
		else if(e.getKeyCode() == KeyEvent.VK_ENTER) {
			gameState = 1;
			initLevel();
		}
		else {
			key2 = e.getKeyCode();
		}
	}

	public void playMusic(String s) {
		try {
			URL url = this.getClass().getClassLoader().getResource(s);
			AudioInputStream ais = AudioSystem.getAudioInputStream(url);
			clip = AudioSystem.getClip();
			clip.open(ais);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		try {
			if(clip != null) {
				new Thread() {
					public void run() {
						synchronized(clip) {
							clip.stop();
							clip.setFramePosition(0);
							clip.loop(Clip.LOOP_CONTINUOUSLY);
						}
					}
				}.start();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}