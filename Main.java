import config.Config;
import ui.GamePanel;
import javax.swing.*;
public class Main {
    public static void main(String[] args){
        JFrame frame=new JFrame("Dodge Game");
        GamePanel panel=new GamePanel();
        frame.add(panel);
        frame.setSize(Config.PANEL_W,Config.PANEL_H);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        panel.requestFocusInWindow();
    }
}
