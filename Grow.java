import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.geom.*;

public class Grow extends PowerUp {

	public Grow(Rectangle r) {
		super(r);
		img = ImageLoader.loadCompatibleImage("Mushroom.png");
		type = "Grow";
	}

	public Rectangle getRectangle() {
		return powerUp;
	}
}