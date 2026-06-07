package input;
import java.awt.event.*;
public class InputHandler extends KeyAdapter {
    public boolean up,down,left,right;
    @Override public void keyPressed(KeyEvent e){
        if(e.getKeyCode()==KeyEvent.VK_RIGHT) right=true;
        if(e.getKeyCode()==KeyEvent.VK_LEFT)  left=true;
        if(e.getKeyCode()==KeyEvent.VK_UP)    up=true;
        if(e.getKeyCode()==KeyEvent.VK_DOWN)  down=true;
    }
    @Override public void keyReleased(KeyEvent e){
        if(e.getKeyCode()==KeyEvent.VK_RIGHT) right=false;
        if(e.getKeyCode()==KeyEvent.VK_LEFT)  left=false;
        if(e.getKeyCode()==KeyEvent.VK_UP)    up=false;
        if(e.getKeyCode()==KeyEvent.VK_DOWN)  down=false;
    }
}
