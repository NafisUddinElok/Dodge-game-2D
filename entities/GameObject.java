package entities;
import java.awt.Graphics2D;
import java.awt.Rectangle;
public abstract class GameObject {
    protected int x, y, width, height;
    protected boolean alive = true;
    public GameObject(int x,int y,int w,int h){ this.x=x;this.y=y;this.width=w;this.height=h; }
    public abstract void update();
    public abstract void draw(Graphics2D g2);
    public void onCollision(Player player) {}
    public boolean isAlive() { return alive; }
    public Rectangle getBounds() { return new Rectangle(x,y,width,height); }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
