package blocks;
import config.Config;
import entities.GameObject;
import entities.Player;
import java.awt.*;
public class NormalBlock extends GameObject {
    private MovementBehavior behavior;
    protected int speed;
    protected int[] pos;
    public NormalBlock(int x,int y,int speed){ super(x,y,Config.BLOCK_SIZE,Config.BLOCK_SIZE); this.speed=speed; pos=new int[]{x,y}; behavior=new StraightDown(); }
    public void setBehavior(MovementBehavior b){ behavior=b; }
    public MovementBehavior getBehavior(){ return behavior; }
    @Override public void update(){ behavior.move(pos,speed); x=pos[0]; y=pos[1]; }
    @Override public void onCollision(Player player){ player.hit(); }
    @Override public void draw(Graphics2D g2){
        g2.setColor(new Color(220,50,50)); g2.fillRect(x,y,width,height);
        g2.setColor(new Color(150,20,20)); g2.drawRect(x,y,width,height);
    }
}
