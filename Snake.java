import java.awt.*;
import java.util.Random;
import java.util.*;
public class Snake {

	private int direction;
	private int size;
	private ArrayList<Rectangle> body = new ArrayList<Rectangle>();
	private boolean isDead;
	private Color color;
	private boolean invincible;
	
	
	public Snake(int x, int y, int dir, int siz, Color tempColor) {
		direction = dir;
		size = siz;
		body.add(new Rectangle(x,y,siz,siz));
		color = tempColor;
	}
	
	public void changeDirection(int newDir) {
		if(direction == newDir || Math.abs(direction-newDir) == 180) {
		
		}
		else {
			direction = newDir;
		}
	}
	
	public int getDirection() {
		return direction;
	}
	
	public boolean isDead() {
		return isDead;
	}

	public boolean isInvincible() {
		return invincible;
	}
	
	public void setDead(boolean d) {
		if(invincible) {
			isDead = false;
		}
		else {
			isDead = d;
		}
	}

	public void setInvincible(boolean d) {
		invincible = d;
	}

	public void setSize(int n) {
		size = n;
	}
	
	public int getSize() {
		return size;
	}
	
	public Rectangle currentHead() {
		return body.get(0);
	}
	
	public Rectangle nextHead() {
		Rectangle lastHead = currentHead();
		if(direction == 90) {
			return new Rectangle(lastHead.x, lastHead.y-size, size, size);
		}
		else if(direction == 0) {
			return new Rectangle(lastHead.x+size, lastHead.y, size, size);
		}
		else if(direction == 270) {
			return new Rectangle(lastHead.x, lastHead.y+size, size, size);
		}
		else {
			return new Rectangle(lastHead.x-size, lastHead.y, size, size);
		}
	}
	
	public ArrayList<Rectangle> getBody() {
		return body;
	}
	
	public void move(boolean grow) {
		body.add(0, nextHead());
		if(!grow) {
			body.remove(body.size()-1);
		}
	}
	
	public boolean intersectsSelf() {
		for(int i = 1; i < body.size(); i++) {
			if(body.get(0).intersects(body.get(i))) {
				return true;
			}
		}
		return false;
	}
	
	public boolean intersects(Snake other) {
		for(Rectangle bodyParts: body) {
			for(int x = 0; x < other.body.size(); x++) {
				if(bodyParts.intersects(other.body.get(x))) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean willIntersect(Food other) {
		Rectangle nextHead = nextHead();
		Rectangle food = other.getRectangle();
		if(nextHead.intersects(food)) {
			return true;
		}
		return false;
	}

	public boolean intersectsGrow(Grow other) {
		Rectangle nextHead = nextHead();
		Rectangle grow = other.getRectangle();
		if(nextHead.intersects(grow)) {
			return true;
		}
		return false;
	}

	public boolean intersectsSuperStar(SuperStar other) {
		Rectangle nextHead = nextHead();
		Rectangle superstar = other.getRectangle();
		if(nextHead.intersects(superstar)) {
			return true;
		}
		return false;
	}

	public void randomColors(Graphics h) {
		Random random = new Random();

		for(int i = 0; i < body.size(); i ++) {
			float r = random.nextFloat();
			float g = random.nextFloat();
			float b = random.nextFloat();

			Color randomColor = new Color(r,g,b);

			h.setColor(randomColor);
			h.fillRect(body.get(i).x, body.get(i).y, body.get(i).width, body.get(i).height);
		}
	}
	
	public void render(Graphics g) {
		g.setColor(color);
        g.fillRect(body.get(0).x, body.get(0).y, body.get(0).width, body.get(0).height);
        
        g.setColor(Color.GRAY);
        for(int i = 1; i < body.size(); i++) {
        	g.fillRect(body.get(i).x, body.get(i).y, body.get(i).width, body.get(i).height);
        }
	}
}
