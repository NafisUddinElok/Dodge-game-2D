package ui;
import config.Config;
import engine.EventBus;
import engine.GameEngine;
import entities.GameObject;
import entities.Player;
import manager.ScoreManager;
import java.awt.*;

public class Renderer {
    private int nearMissAlpha=0;
    private String nearMissText="";

    public Renderer(){
        EventBus.on("NEAR_MISS",d->{ nearMissAlpha=90; nearMissText="NEAR MISS! x"+ScoreManager.getInstance().getCombo(); });
    }

    public void render(Graphics2D g2,GameEngine engine,int w,int h){
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        if(engine.state==GameEngine.GameState.NAME_INPUT){ drawNameInput(g2,w,h); return; }
        drawBackground(g2,w,h);
        if(engine.state==GameEngine.GameState.PLAYING||engine.state==GameEngine.GameState.PAUSED){
            for(GameObject obj:engine.objects) obj.draw(g2);
            engine.player.draw(g2);
            drawHUD(g2,engine,w,h);
            drawNearMiss(g2,w);
            if(nearMissAlpha>0) nearMissAlpha-=3;
            if(engine.state==GameEngine.GameState.PAUSED) drawPause(g2,w,h);
        } else if(engine.state==GameEngine.GameState.GAME_OVER){
            drawBackground(g2,w,h); drawGameOver(g2,engine,w,h);
        }
    }

    private void drawNameInput(Graphics2D g2,int w,int h){
        GradientPaint bg=new GradientPaint(0,0,new Color(20,20,60),0,h,new Color(60,20,80));
        g2.setPaint(bg); g2.fillRect(0,0,w,h);
        g2.setColor(new Color(255,220,80)); g2.setFont(new Font("Arial",Font.BOLD,52));
        FontMetrics fm=g2.getFontMetrics(); String title="DODGE GAME";
        g2.drawString(title,w/2-fm.stringWidth(title)/2,120);
        g2.setColor(new Color(200,200,255)); g2.setFont(new Font("Arial",Font.PLAIN,20));
        fm=g2.getFontMetrics(); String sub="Enter your name to start";
        g2.drawString(sub,w/2-fm.stringWidth(sub)/2,170);
        g2.setColor(new Color(255,255,255,30)); g2.fillRoundRect(w/2-160,200,320,55,15,15);
        g2.setColor(new Color(255,220,80)); g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(w/2-160,200,320,55,15,15); g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial",Font.BOLD,28));
        fm=g2.getFontMetrics(); String buf=GamePanel.nameBuffer+"|";
        g2.drawString(buf,w/2-fm.stringWidth(buf)/2,238);
        g2.setColor(new Color(150,200,255)); g2.setFont(new Font("Arial",Font.PLAIN,16));
        fm=g2.getFontMetrics(); String hint="Press ENTER to play";
        g2.drawString(hint,w/2-fm.stringWidth(hint)/2,295);
        g2.setColor(new Color(120,120,180)); g2.setFont(new Font("Arial",Font.PLAIN,14));
        String ctrl="Controls: Arrow Keys to move  |  P = Pause  |  R = Restart";
        fm=g2.getFontMetrics(); g2.drawString(ctrl,w/2-fm.stringWidth(ctrl)/2,340);
        int lx=w/2-200,ly=380; g2.setFont(new Font("Arial",Font.PLAIN,13));
        g2.setColor(new Color(220,50,50));   g2.drawString("■ Normal",lx,ly);
        g2.setColor(new Color(255,140,0));   g2.drawString("■ Fast",lx+90,ly);
        g2.setColor(new Color(120,0,180));   g2.drawString("■ Big",lx+170,ly);
        g2.setColor(new Color(0,180,80));    g2.drawString("■ Chase",lx+240,ly);
        g2.setColor(new Color(0,180,255));   g2.drawString("◉ Shield",lx+330,ly);
        g2.setColor(new Color(100,100,255)); g2.drawString("◉ Slow",lx+420,ly);
    }

    private void drawBackground(Graphics2D g2,int w,int h){
        GradientPaint sky=new GradientPaint(0,0,new Color(100,180,255),0,h,new Color(200,230,255));
        g2.setPaint(sky); g2.fillRect(0,0,w,h);
        g2.setColor(new Color(255,255,255,200));
        drawCloud(g2,80,20); drawCloud(g2,380,35); drawCloud(g2,680,18); drawCloud(g2,870,40);
        g2.setColor(new Color(255,220,50,220)); g2.fillOval(w-90,10,65,65);
        g2.setColor(new Color(255,240,100,100)); g2.fillOval(w-100,0,85,85);
        g2.setColor(new Color(34,139,34)); g2.fillRect(0,h-25,w,25);
        g2.setColor(new Color(25,110,25)); g2.fillRect(0,h-25,w,5);
    }
    private void drawCloud(Graphics2D g2,int cx,int cy){
        g2.fillOval(cx,cy,80,35); g2.fillOval(cx+20,cy-15,55,35); g2.fillOval(cx-10,cy+5,50,28);
    }

    private void drawHUD(Graphics2D g2,GameEngine engine,int w,int h){
        ScoreManager sm=ScoreManager.getInstance(); Player p=engine.player;
        for(int i=0;i<Config.MAX_HEALTH;i++){
            g2.setColor(i<p.getHealth()?new Color(220,50,50):new Color(180,180,180));
            g2.setFont(new Font("Arial",Font.PLAIN,22)); g2.drawString("♥",15+i*28,30);
        }
        g2.setColor(Color.BLACK); g2.setFont(new Font("Arial",Font.BOLD,15));
        g2.drawString("Score: "+sm.getScore(),15,55);
        g2.drawString("Best:  "+sm.getHighScore(),15,75);
        g2.drawString("Level: "+sm.getLevel(),15,95);
        if(sm.hasCombo()){ g2.setColor(new Color(255,120,0)); g2.drawString("COMBO x"+sm.getCombo(),15,115); }
        if(p.getShieldTimer()>0){
            g2.setColor(new Color(0,180,255,180)); g2.fillRoundRect(15,125,p.getShieldTimer()/2,10,5,5);
            g2.setColor(Color.BLACK); g2.setFont(new Font("Arial",Font.PLAIN,12)); g2.drawString("SHIELD",15,148);
        }
        if(engine.slowActive){ g2.setColor(new Color(100,100,255)); g2.setFont(new Font("Arial",Font.BOLD,12)); g2.drawString("SLOW ACTIVE",15,165); }
        int lx=w-115; g2.setFont(new Font("Arial",Font.PLAIN,12));
        g2.setColor(new Color(220,50,50));   g2.drawString("■ Normal",lx,20);
        g2.setColor(new Color(255,140,0));   g2.drawString("■ Fast",lx,38);
        g2.setColor(new Color(120,0,180));   g2.drawString("■ Big",lx,56);
        g2.setColor(new Color(0,180,80));    g2.drawString("■ Chase",lx,74);
        g2.setColor(new Color(0,180,255));   g2.drawString("◉ Shield",lx,92);
        g2.setColor(new Color(100,100,255)); g2.drawString("◉ Slow",lx,110);
        g2.setColor(new Color(255,60,100));  g2.drawString("◉ Health",lx,128);
        g2.setColor(Color.DARK_GRAY); g2.setFont(new Font("Arial",Font.PLAIN,11));
        g2.drawString("P=Pause  R=Restart",w-130,h-10);
    }

    private void drawNearMiss(Graphics2D g2,int w){
        if(nearMissAlpha<=0) return;
        g2.setColor(new Color(255,200,0,nearMissAlpha)); g2.setFont(new Font("Arial",Font.BOLD,22));
        g2.drawString(nearMissText,w/2-80,60);
    }

    private void drawPause(Graphics2D g2,int w,int h){
        g2.setColor(new Color(0,0,0,130)); g2.fillRect(0,0,w,h);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial",Font.BOLD,45));
        g2.drawString("PAUSED",370,200);
        g2.setFont(new Font("Arial",Font.PLAIN,20)); g2.drawString("Press P to continue",360,240);
    }

    private void drawGameOver(Graphics2D g2,GameEngine engine,int w,int h){
        g2.setColor(new Color(0,0,0,160)); g2.fillRect(0,0,w,h);
        g2.setColor(new Color(255,60,60)); g2.setFont(new Font("Arial",Font.BOLD,50));
        String msg=engine.playerName+" \u2014 GAME OVER";
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString(msg,w/2-fm.stringWidth(msg)/2,160);
        ScoreManager sm=ScoreManager.getInstance();
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial",Font.BOLD,26));
        g2.drawString("Score:  "+sm.getScore(),390,215);
        g2.drawString("Best:   "+sm.getHighScore(),390,250);
        g2.setColor(new Color(100,255,100));
        g2.drawString("Press R to Restart",340,310);
    }
}
