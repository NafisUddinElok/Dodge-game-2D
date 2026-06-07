package blocks;
import java.awt.*;
public class FastBlock extends NormalBlock {
    public FastBlock(int x,int y){ super(x,y,10); width=20; height=20; }
    @Override public void draw(Graphics2D g2){
        g2.setColor(new Color(255,140,0)); g2.fillRect(x,y,width,height);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial",Font.BOLD,8)); g2.drawString("FAST",x+1,y+13);
    }
}
