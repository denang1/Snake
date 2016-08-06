import java.awt.*;
import java.awt.image.BufferedImage;

public class Food {
	
	private Rectangle food;
	private BufferedImage img = ImageLoader.loadCompatibleImage("fahrenbacher.png");
		
	public Food(Rectangle r) {
		food = r;
	}
	
	public void render(Graphics g) {
		g.drawImage(img,food.x,food.y,food.width,food.height,null);
	}
	
	public Rectangle getRectangle() {
		return food;
	}
} 
