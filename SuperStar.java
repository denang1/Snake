import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.geom.*;

public class SuperStar extends PowerUp {

	public SuperStar(Rectangle r) {
		super(r);
		img = ImageLoader.loadCompatibleImage("superstar.png");
		type = "SuperStar";

	}

	public Rectangle getRectangle() {
		return powerUp;
	}
}