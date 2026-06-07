package entities;
import config.Config;
import engine.EventBus;
import input.InputHandler;
import manager.ScoreManager;
import java.awt.*;

enum PlayerState { NORMAL, HIT, SHIELDED, DEAD }

public class Player extends GameObject {
    private int health = Config.MAX_HEALTH;
    private int invTimer = 0, shieldTimer = 0;
    PlayerState state = PlayerState.NORMAL;
    private String name;

    public Player(int x, int y, String name) {
        super(x, y, Config.PLAYER_WIDTH, Config.PLAYER_HEIGHT);
        this.name = name;
    }

    @Override public void update() {}

    public void update(int panelW, int panelH, InputHandler input) {
        if (state == PlayerState.DEAD) return;
        int dx = input.right ? Config.PLAYER_SPEED : input.left ? -Config.PLAYER_SPEED : 0;
        int dy = input.down  ? Config.PLAYER_SPEED : input.up   ? -Config.PLAYER_SPEED : 0;
        x += dx; y += dy;
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > panelW - width)  x = panelW - width;
        if (y > panelH - height) y = panelH - height;
        if (invTimer > 0) invTimer--;
        if (shieldTimer > 0) { shieldTimer--; state = PlayerState.SHIELDED; }
        else if (invTimer == 0 && state != PlayerState.DEAD) state = PlayerState.NORMAL;
        ScoreManager.getInstance().update();
    }

    public void hit() {
        if (invTimer > 0) return;
        if (state == PlayerState.SHIELDED) { shieldTimer=0; state=PlayerState.NORMAL; invTimer=60; EventBus.emit("SHIELD_BREAK"); return; }
        health--; invTimer=60; state=PlayerState.HIT;
        EventBus.emit("PLAYER_HIT", health);
        if (health <= 0) { state=PlayerState.DEAD; EventBus.emit("GAME_OVER"); }
    }

    public void activateShield() { shieldTimer=300; state=PlayerState.SHIELDED; }
    public void heal()           { if (health<Config.MAX_HEALTH) health++; }
    public void activateSlow()   { EventBus.emit("SLOW_ON"); }
    public int  getHealth()      { return health; }
    public int  getShieldTimer() { return shieldTimer; }
    public boolean isInvincible(){ return invTimer>0; }
    public String getName()      { return name; }

    @Override
    public Rectangle getBounds() { return new Rectangle(x+10, y, 30, height); }

    @Override
    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (isInvincible() && (invTimer/5)%2==0) return;

        if (state == PlayerState.SHIELDED) {
            g2.setColor(new Color(0,180,255,80)); g2.fillOval(x-10,y-35,70,130);
            g2.setColor(new Color(0,180,255,200)); g2.setStroke(new BasicStroke(2));
            g2.drawOval(x-10,y-35,70,130); g2.setStroke(new BasicStroke(1));
        }
        g2.setColor(new Color(60,35,10)); g2.fillOval(x+8,y-28,34,22); g2.fillRect(x+8,y-14,34,8);
        g2.setColor(new Color(255,220,185)); g2.fillOval(x+8,y-22,34,32);
        g2.setColor(new Color(60,35,10)); g2.setStroke(new BasicStroke(2.5f));
        g2.drawLine(x+13,y-10,x+21,y-12); g2.drawLine(x+29,y-12,x+37,y-10); g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.BLACK); g2.fillOval(x+14,y-8,6,5); g2.fillOval(x+30,y-8,6,5);
        g2.setColor(new Color(220,180,150)); g2.fillOval(x+22,y-2,6,5);
        g2.setColor(new Color(180,100,80)); g2.drawLine(x+18,y+5,x+32,y+5);
        g2.setColor(new Color(255,220,185)); g2.fillRect(x+19,y+10,12,10);
        g2.setColor(new Color(50,100,200)); g2.fillRoundRect(x+5,y+20,40,35,8,8);
        g2.setColor(Color.WHITE);
        g2.fillPolygon(new int[]{x+20,x+25,x+25},new int[]{y+20,y+28,y+20},3);
        g2.fillPolygon(new int[]{x+30,x+25,x+25},new int[]{y+20,y+28,y+20},3);
        g2.setColor(new Color(40,40,80)); g2.fillRect(x+8,y+55,16,30); g2.fillRect(x+26,y+55,16,30);
        g2.setColor(new Color(80,50,20)); g2.fillRect(x+5,y+53,40,5);
        g2.setColor(new Color(200,180,0)); g2.fillRect(x+21,y+53,8,5);
        g2.setColor(new Color(50,100,200)); g2.fillRoundRect(x-8,y+20,16,10,6,6); g2.fillRoundRect(x+42,y+20,16,10,6,6);
        g2.setColor(new Color(255,220,185)); g2.fillOval(x-10,y+28,12,10); g2.fillOval(x+48,y+28,12,10);
        g2.setColor(new Color(30,20,10)); g2.fillRoundRect(x+5,y+82,18,8,5,5); g2.fillRoundRect(x+27,y+82,18,8,5,5);
        g2.setColor(new Color(0,0,0,160)); g2.setFont(new Font("Arial",Font.BOLD,11));
        FontMetrics fm=g2.getFontMetrics(); int nameW=fm.stringWidth(name);
        g2.fillRoundRect(x+25-nameW/2-3,y-48,nameW+6,16,6,6);
        g2.setColor(Color.WHITE); g2.drawString(name,x+25-nameW/2,y-36);
    }
}
