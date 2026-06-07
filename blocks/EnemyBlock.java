package blocks;
import java.awt.*;
public class EnemyBlock extends NormalBlock {
    private FollowMovement fm;
    public EnemyBlock(int x,int y,int playerX){ super(x,y,4); width=30; height=30; fm=new FollowMovement(playerX); setBehavior(fm); }
    public void updatePlayerX(int px){ fm.setPlayerX(px); }
    @Override public void draw(Graphics2D g2){
        g2.setColor(new Color(0,180,80)); g2.fillRect(x,y,width,height);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial",Font.BOLD,8)); g2.drawString("CHASE",x+1,y+19);
    }
}
