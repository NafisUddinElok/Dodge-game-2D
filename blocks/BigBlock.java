package blocks;
import java.awt.*;
public class BigBlock extends NormalBlock {
    public BigBlock(int x,int y,int speed){ super(x,y,Math.max(2,speed-2)); width=65; height=65; }
    @Override public void draw(Graphics2D g2){
        g2.setColor(new Color(120,0,180)); g2.fillRect(x,y,width,height);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial",Font.BOLD,10)); g2.drawString("BIG",x+22,y+37);
    }
}
