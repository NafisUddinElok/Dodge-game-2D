package powerups;
import engine.EventBus;
import entities.GameObject;
import entities.Player;
import java.awt.*;
public class PowerUp extends GameObject {
    public enum Type { SHIELD, SLOW, HEALTH }
    public final Type type;
    public PowerUp(int x,int y,Type type){ super(x,y,28,28); this.type=type; }
    @Override public void update(){ y+=3; }
    @Override public void onCollision(Player player){
        switch(type){ case SHIELD: player.activateShield(); break; case SLOW: player.activateSlow(); break; case HEALTH: player.heal(); break; }
        alive=false; EventBus.emit("POWERUP_COLLECTED",type);
    }
    @Override public void draw(Graphics2D g2){
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        Color c=type==Type.SHIELD?new Color(0,180,255):type==Type.SLOW?new Color(100,100,255):new Color(255,60,100);
        g2.setColor(c); g2.fillOval(x,y,width,height);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial",Font.BOLD,12));
        g2.drawString(type==Type.SHIELD?"S":type==Type.SLOW?"~":"+",x+9,y+20);
    }
}
