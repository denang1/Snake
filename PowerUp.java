import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class PowerUp {

	protected Rectangle powerUp;
	protected BufferedImage img;
	protected String type;
	
	public PowerUp(Rectangle r) {
		powerUp = r;
		type = "PowerUp";
	}

	public void render(Graphics g) {
		g.drawImage(img,powerUp.x,powerUp.y,powerUp.width,powerUp.height,null);
	}

	public String getType() {
		return type;
	}
}