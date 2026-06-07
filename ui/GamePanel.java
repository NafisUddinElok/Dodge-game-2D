package ui;
import engine.GameEngine;
import input.InputHandler;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
public class GamePanel extends JPanel {
    public static String nameBuffer="";
    GameEngine   engine;
    InputHandler input=new InputHandler();
    Renderer     renderer=new Renderer();

    public GamePanel(){
        setFocusable(true);
        engine=new GameEngine("");
        addKeyListener(input);
        addKeyListener(new KeyAdapter(){
            @Override public void keyPressed(KeyEvent e){
                if(engine.state==GameEngine.GameState.NAME_INPUT){
                    if(e.getKeyCode()==KeyEvent.VK_ENTER){
                        String name=nameBuffer.trim().isEmpty()?"Player":nameBuffer.trim();
                        engine.setPlayerName(name);
                    } else if(e.getKeyCode()==KeyEvent.VK_BACK_SPACE){
                        if(nameBuffer.length()>0) nameBuffer=nameBuffer.substring(0,nameBuffer.length()-1);
                    } else {
                        char c=e.getKeyChar();
                        if((Character.isLetterOrDigit(c)||c==' ')&&nameBuffer.length()<12) nameBuffer+=c;
                    }
                    return;
                }
                if(e.getKeyCode()==KeyEvent.VK_P){
                    if(engine.state==GameEngine.GameState.PLAYING) engine.state=GameEngine.GameState.PAUSED;
                    else if(engine.state==GameEngine.GameState.PAUSED) engine.state=GameEngine.GameState.PLAYING;
                }
                if(e.getKeyCode()==KeyEvent.VK_R&&engine.state==GameEngine.GameState.GAME_OVER){
                    engine.restart(); renderer=new Renderer();
                }
            }
        });
        new Timer(20,e->{ engine.update(getWidth(),getHeight(),input); repaint(); }).start();
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        renderer.render((Graphics2D)g,engine,getWidth(),getHeight());
    }
}
