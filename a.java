import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;

// config 
class Config {
    static final int PLAYER_SPEED  = 4;
    static final int BLOCK_SIZE    = 40;
    static final int INITIAL_SPEED = 4;
    static final int PLAYER_WIDTH  = 50;
    static final int PLAYER_HEIGHT = 95;
    static final int MAX_HEALTH    = 3;
    static final int PANEL_W       = 1000;
    static final int PANEL_H       = 500;
}

// event bus
class EventBus {
    private static final Map<String, List<Consumer<Object>>> listeners = new HashMap<>();
    public static void on(String event, Consumer<Object> listener) {
        listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(listener);
    }
    public static void emit(String event) { emit(event, null); }
    public static void emit(String event, Object data) {
        List<Consumer<Object>> list = listeners.get(event);
        if (list != null) for (Consumer<Object> l : list) l.accept(data);
    }
    public static void clear() { listeners.clear(); }
}

// score manager
class ScoreManager {
    private static ScoreManager instance;
    private int score = 0, highScore = 0, combo = 1, comboTimer = 0;
    private ScoreManager() {}
    public static ScoreManager getInstance() {
        if (instance == null) instance = new ScoreManager();
        return instance;
    }
    public void update()       { if (comboTimer > 0) comboTimer--; else combo = 1; }
    public void addTime()      { score += combo; if (score > highScore) highScore = score; }
    public void nearMissBonus(){ combo = Math.min(combo+1,5); comboTimer=120; score+=10*combo; EventBus.emit("NEAR_MISS"); }
    public void reset()        { score=0; combo=1; comboTimer=0; }
    public int getScore()      { return score; }
    public int getHighScore()  { return highScore; }
    public int getLevel()      { return score/300+1; }
    public int getCombo()      { return combo; }
    public boolean hasCombo()  { return combo>1; }
}

// movement behaviour
interface MovementBehavior { void move(int[] pos, int speed); }

class StraightDown implements MovementBehavior {
    @Override
    public void move(int[] pos, int speed) { pos[1] += speed; }
}
@SuppressWarnings("unused")
class ZigZagMovement implements MovementBehavior {
    int timer=0, dir=1;
    @Override
    public void move(int[] pos, int speed) { pos[1]+=speed; timer++; if(timer%30==0) dir*=-1; pos[0]+=dir*4; }
}
class FollowMovement implements MovementBehavior {
    int playerX;
    public FollowMovement(int px) { playerX=px; }
    @Override
    public void move(int[] pos, int speed) { pos[1]+=speed; if(pos[0]<playerX) pos[0]+=2; else if(pos[0]>playerX) pos[0]-=2; }
    public void setPlayerX(int px) { playerX=px; }
}

// player state
enum PlayerState { NORMAL, HIT, SHIELDED, DEAD }

// abstruct game object
abstract class GameObject {
    protected int x, y, width, height;
    protected boolean alive = true;
    public GameObject(int x,int y,int w,int h){ this.x=x;this.y=y;this.width=w;this.height=h; }
    abstract void update();
    abstract void draw(Graphics2D g2);
    void onCollision(Player player) {}
    boolean isAlive() { return alive; }
    Rectangle getBounds() { return new Rectangle(x,y,width,height); }
}

// player male character
class Player extends GameObject {
    private int health   = Config.MAX_HEALTH;
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

        // Shield aura
        if (state == PlayerState.SHIELDED) {
            g2.setColor(new Color(0,180,255,80));
            g2.fillOval(x-10, y-35, 70, 130);
            g2.setColor(new Color(0,180,255,200));
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(x-10, y-35, 70, 130);
            g2.setStroke(new BasicStroke(1));
        }

        // --- SHORT HAIR (male) ---
        g2.setColor(new Color(60, 35, 10));
        g2.fillOval(x+8, y-28, 34, 22); // hair top
        g2.fillRect(x+8, y-14, 34, 8);  // hair sides

        // --- HEAD ---
        g2.setColor(new Color(255, 220, 185));
        g2.fillOval(x+8, y-22, 34, 32);

        // --- EYEBROWS (thick, male) ---
        g2.setColor(new Color(60, 35, 10));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawLine(x+13, y-10, x+21, y-12);
        g2.drawLine(x+29, y-12, x+37, y-10);
        g2.setStroke(new BasicStroke(1));

        // --- EYES ---
        g2.setColor(Color.BLACK);
        g2.fillOval(x+14, y-8, 6, 5);
        g2.fillOval(x+30, y-8, 6, 5);

        // --- NOSE ---
        g2.setColor(new Color(220, 180, 150));
        g2.fillOval(x+22, y-2, 6, 5);

        // --- MOUTH (straight/slight smile) ---
        g2.setColor(new Color(180, 100, 80));
        g2.drawLine(x+18, y+5, x+32, y+5);

        // --- NECK ---
        g2.setColor(new Color(255, 220, 185));
        g2.fillRect(x+19, y+10, 12, 10);

        // --- BODY (shirt, broad shoulders) ---
        g2.setColor(new Color(50, 100, 200)); // blue shirt
        g2.fillRoundRect(x+5, y+20, 40, 35, 8, 8);

        // Collar
        g2.setColor(new Color(255,255,255));
        g2.fillPolygon(new int[]{x+20,x+25,x+25}, new int[]{y+20,y+28,y+20}, 3);
        g2.fillPolygon(new int[]{x+30,x+25,x+25}, new int[]{y+20,y+28,y+20}, 3);

        // --- PANTS ---
        g2.setColor(new Color(40, 40, 80)); // dark pants
        g2.fillRect(x+8,  y+55, 16, 30);
        g2.fillRect(x+26, y+55, 16, 30);

        // Belt
        g2.setColor(new Color(80, 50, 20));
        g2.fillRect(x+5, y+53, 40, 5);
        g2.setColor(new Color(200,180,0));
        g2.fillRect(x+21, y+53, 8, 5); // buckle

        // --- ARMS (broader) ---
        g2.setColor(new Color(50, 100, 200));
        g2.fillRoundRect(x-8,  y+20, 16, 10, 6, 6); // left
        g2.fillRoundRect(x+42, y+20, 16, 10, 6, 6); // right

        // Hands
        g2.setColor(new Color(255, 220, 185));
        g2.fillOval(x-10, y+28, 12, 10);
        g2.fillOval(x+48, y+28, 12, 10);

        // --- SHOES ---
        g2.setColor(new Color(30, 20, 10));
        g2.fillRoundRect(x+5,  y+82, 18, 8, 5, 5);
        g2.fillRoundRect(x+27, y+82, 18, 8, 5, 5);

        // --- NAME TAG ---
        g2.setColor(new Color(0,0,0,160));
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int nameW = fm.stringWidth(name);
        g2.fillRoundRect(x + 25 - nameW/2 - 3, y - 48, nameW + 6, 16, 6, 6);
        g2.setColor(Color.WHITE);
        g2.drawString(name, x + 25 - nameW/2, y - 36);
    }
}

// block factory
class BlockFactory {
    public static GameObject createRandom(int panelW, int speed, int playerX) {
        int rx = (int)(Math.random()*(panelW-65));
        double r = Math.random();
        if      (r < 0.35) return new NormalBlock(rx, 0, speed);
        else if (r < 0.55) return new FastBlock(rx, 0);
        else if (r < 0.70) return new BigBlock(rx, 0, speed);
        else               return new EnemyBlock(rx, 0, playerX);
    }
    public static GameObject createPowerUp(int panelW) {
        int rx = (int)(Math.random()*(panelW-30));
        double r = Math.random();
        if      (r < 0.4) return new PowerUp(rx, 0, PowerUp.Type.SHIELD);
        else if (r < 0.7) return new PowerUp(rx, 0, PowerUp.Type.SLOW);
        else               return new PowerUp(rx, 0, PowerUp.Type.HEALTH);
    }
}

// ===================== BLOCKS =====================
class NormalBlock extends GameObject {
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
class FastBlock extends NormalBlock {
    public FastBlock(int x,int y){ super(x,y,10); width=20; height=20; }
    @Override public void draw(Graphics2D g2){
        g2.setColor(new Color(255,140,0)); g2.fillRect(x,y,width,height);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial",Font.BOLD,8)); g2.drawString("FAST",x+1,y+13);
    }
}
class BigBlock extends NormalBlock {
    public BigBlock(int x,int y,int speed){ super(x,y,Math.max(2,speed-2)); width=65; height=65; }
    @Override public void draw(Graphics2D g2){
        g2.setColor(new Color(120,0,180)); g2.fillRect(x,y,width,height);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial",Font.BOLD,10)); g2.drawString("BIG",x+22,y+37);
    }
}
class EnemyBlock extends NormalBlock {
    private FollowMovement fm;
    public EnemyBlock(int x,int y,int playerX){ super(x,y,4); width=30; height=30; fm=new FollowMovement(playerX); setBehavior(fm); }
    public void updatePlayerX(int px){ fm.setPlayerX(px); }
    @Override public void draw(Graphics2D g2){
        g2.setColor(new Color(0,180,80)); g2.fillRect(x,y,width,height);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial",Font.BOLD,8)); g2.drawString("CHASE",x+1,y+19);
    }
}

// powerup
class PowerUp extends GameObject {
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
        Color c = type==Type.SHIELD?new Color(0,180,255):type==Type.SLOW?new Color(100,100,255):new Color(255,60,100);
        g2.setColor(c); g2.fillOval(x,y,width,height);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial",Font.BOLD,12));
        g2.drawString(type==Type.SHIELD?"S":type==Type.SLOW?"~":"+", x+9, y+20);
    }
}

// input handler
class InputHandler extends KeyAdapter {
    boolean up,down,left,right;
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

// game engine
class GameEngine {
    Player player;
    List<GameObject> objects = new ArrayList<>();
    boolean slowActive=false; int slowTimer=0, nearMissTimer=0;
    String playerName;

    enum GameState { NAME_INPUT, PLAYING, PAUSED, GAME_OVER }
    GameState state = GameState.NAME_INPUT;

    public GameEngine(String name) {
        this.playerName = name;
        player = new Player(100, 150, name);
        registerEvents();
    }

    private void registerEvents(){
        EventBus.on("GAME_OVER", d -> state=GameState.GAME_OVER);
        EventBus.on("SLOW_ON",   d -> { slowActive=true; slowTimer=300; });
    }

    public void update(int panelW, int panelH, InputHandler input){
        if(state!=GameState.PLAYING) return;
        int speed = Config.INITIAL_SPEED + ScoreManager.getInstance().getScore()/200;
        int effectiveSpeed = slowActive ? Math.max(1,speed/2) : speed;
        if(slowTimer>0){ slowTimer--; if(slowTimer==0) slowActive=false; }
        player.update(panelW,panelH,input);
        ScoreManager.getInstance().addTime();
        for(GameObject obj:objects) if(obj instanceof EnemyBlock) ((EnemyBlock)obj).updatePlayerX(player.x);
        double spawnRate=0.02+ScoreManager.getInstance().getLevel()*0.004;
        if(Math.random()<spawnRate) objects.add(BlockFactory.createRandom(panelW,effectiveSpeed,player.x));
        if(Math.random()<0.004) objects.add(BlockFactory.createPowerUp(panelW));
        for(GameObject obj:objects) obj.update();
        objects.removeIf(obj->!obj.isAlive()||obj.y>panelH+100);
        for(GameObject obj:objects) if(player.getBounds().intersects(obj.getBounds())) obj.onCollision(player);
        checkNearMiss();
        if(nearMissTimer>0) nearMissTimer--;
    }

    private void checkNearMiss(){
        for(GameObject obj:objects){
            Rectangle exp=new Rectangle(player.getBounds().x-15,player.getBounds().y-15,player.getBounds().width+30,player.getBounds().height+30);
            if(exp.intersects(obj.getBounds())&&!player.getBounds().intersects(obj.getBounds())&&nearMissTimer==0){
                ScoreManager.getInstance().nearMissBonus(); nearMissTimer=60;
            }
        }
    }

    public void restart(){
        EventBus.clear();
        player=new Player(100,150,playerName);
        objects.clear(); slowActive=false; slowTimer=0;
        ScoreManager.getInstance().reset();
        state=GameState.PLAYING;
        registerEvents();
    }

    public void setPlayerName(String name){
        this.playerName=name;
        player=new Player(100,150,name);
        state=GameState.PLAYING;
    }
}

// renderer
class Renderer {
    private int nearMissAlpha=0; private String nearMissText="";
    public Renderer(){
        EventBus.on("NEAR_MISS", d->{ nearMissAlpha=90; nearMissText="NEAR MISS! x"+ScoreManager.getInstance().getCombo(); });
    }

    public void render(Graphics2D g2, GameEngine engine, int w, int h){
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        if(engine.state==GameEngine.GameState.NAME_INPUT){
            drawNameInput(g2, w, h);
            return;
        }

        drawBackground(g2,w,h);
        if(engine.state==GameEngine.GameState.PLAYING||engine.state==GameEngine.GameState.PAUSED){
            for(GameObject obj:engine.objects) obj.draw(g2);
            engine.player.draw(g2);
            drawHUD(g2,engine,w,h);
            drawNearMiss(g2,w);
            if(nearMissAlpha>0) nearMissAlpha-=3;
            if(engine.state==GameEngine.GameState.PAUSED) drawPause(g2,w,h);
        } else if(engine.state==GameEngine.GameState.GAME_OVER){
            drawBackground(g2,w,h);
            drawGameOver(g2,engine,w,h);
        }
    }

    private void drawNameInput(Graphics2D g2, int w, int h){
        // Background
        GradientPaint bg = new GradientPaint(0,0,new Color(20,20,60),0,h,new Color(60,20,80));
        g2.setPaint(bg); g2.fillRect(0,0,w,h);

        // Title
        g2.setColor(new Color(255,220,80));
        g2.setFont(new Font("Arial",Font.BOLD,52));
        FontMetrics fm=g2.getFontMetrics();
        String title="DODGE GAME";
        g2.drawString(title, w/2-fm.stringWidth(title)/2, 120);

        // Subtitle
        g2.setColor(new Color(200,200,255));
        g2.setFont(new Font("Arial",Font.PLAIN,20));
        fm=g2.getFontMetrics();
        String sub="Enter your name to start";
        g2.drawString(sub, w/2-fm.stringWidth(sub)/2, 170);

        // Name box background
        g2.setColor(new Color(255,255,255,30));
        g2.fillRoundRect(w/2-160, 200, 320, 55, 15, 15);
        g2.setColor(new Color(255,220,80));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(w/2-160, 200, 320, 55, 15, 15);
        g2.setStroke(new BasicStroke(1));

        // Name text
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial",Font.BOLD,28));
        fm=g2.getFontMetrics();
        g2.drawString(GamePanel.nameBuffer + "|", w/2 - fm.stringWidth(GamePanel.nameBuffer+"|")/2, 238);

        // Instructions
        g2.setColor(new Color(150,200,255));
        g2.setFont(new Font("Arial",Font.PLAIN,16));
        fm=g2.getFontMetrics();
        String hint="Press ENTER to play";
        g2.drawString(hint, w/2-fm.stringWidth(hint)/2, 295);

        // Controls
        g2.setColor(new Color(120,120,180));
        g2.setFont(new Font("Arial",Font.PLAIN,14));
        String ctrl="Controls: Arrow Keys to move  |  P = Pause  |  R = Restart";
        fm=g2.getFontMetrics();
        g2.drawString(ctrl, w/2-fm.stringWidth(ctrl)/2, 340);

        // Legend
        int lx=w/2-200; int ly=380;
        g2.setFont(new Font("Arial",Font.PLAIN,13));
        g2.setColor(new Color(220,50,50));   g2.drawString("■ Normal",  lx,     ly);
        g2.setColor(new Color(255,140,0));   g2.drawString("■ Fast",    lx+90,  ly);
        g2.setColor(new Color(120,0,180));   g2.drawString("■ Big",     lx+170, ly);
        g2.setColor(new Color(0,180,80));    g2.drawString("■ Chase",   lx+240, ly);
        g2.setColor(new Color(0,180,255));   g2.drawString("◉ Shield",  lx+330, ly);
        g2.setColor(new Color(100,100,255)); g2.drawString("◉ Slow",    lx+420, ly);
    }

    private void drawBackground(Graphics2D g2, int w, int h){
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

    private void drawHUD(Graphics2D g2, GameEngine engine, int w, int h){
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
        g2.setColor(new Color(255,140,0));   g2.drawString("■ Fast",  lx,38);
        g2.setColor(new Color(120,0,180));   g2.drawString("■ Big",   lx,56);
        g2.setColor(new Color(0,180,80));    g2.drawString("■ Chase", lx,74);
        g2.setColor(new Color(0,180,255));   g2.drawString("◉ Shield",lx,92);
        g2.setColor(new Color(100,100,255)); g2.drawString("◉ Slow",  lx,110);
        g2.setColor(new Color(255,60,100));  g2.drawString("◉ Health",lx,128);
        g2.setColor(Color.DARK_GRAY); g2.setFont(new Font("Arial",Font.PLAIN,11));
        g2.drawString("P=Pause  R=Restart",w-130,h-10);
    }

    private void drawNearMiss(Graphics2D g2,int w){
        if(nearMissAlpha<=0) return;
        g2.setColor(new Color(255,200,0,nearMissAlpha));
        g2.setFont(new Font("Arial",Font.BOLD,22));
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
        // Show player name in game over
        String msg = engine.playerName + " — GAME OVER";
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString(msg, w/2-fm.stringWidth(msg)/2, 160);
        ScoreManager sm=ScoreManager.getInstance();
        g2.setColor(Color.WHITE); g2.setFont(new Font("Arial",Font.BOLD,26));
        g2.drawString("Score:  "+sm.getScore(),    390,215);
        g2.drawString("Best:   "+sm.getHighScore(),390,250);
        g2.setColor(new Color(100,255,100));
        g2.drawString("Press R to Restart",        340,310);
    }
}

// game panel
class GamePanel extends JPanel {
    static String nameBuffer = "";
    GameEngine   engine;
    InputHandler input    = new InputHandler();
    Renderer     renderer = new Renderer();

    public GamePanel(){
        setFocusable(true);
        engine = new GameEngine("");  // starts at NAME_INPUT state

        addKeyListener(input);
        addKeyListener(new KeyAdapter(){
            @Override public void keyPressed(KeyEvent e){
                // Name input screen
                if(engine.state==GameEngine.GameState.NAME_INPUT){
                    if(e.getKeyCode()==KeyEvent.VK_ENTER){
                        String name=nameBuffer.trim().isEmpty()?"Player":nameBuffer.trim();
                        engine.setPlayerName(name);
                    } else if(e.getKeyCode()==KeyEvent.VK_BACK_SPACE){
                        if(nameBuffer.length()>0) nameBuffer=nameBuffer.substring(0,nameBuffer.length()-1);
                    } else {
                        char c=e.getKeyChar();
                        if(Character.isLetterOrDigit(c)||c==' '){
                            if(nameBuffer.length()<12) nameBuffer+=c;
                        }
                    }
                    return;
                }
                // Pause
                if(e.getKeyCode()==KeyEvent.VK_P){
                    if(engine.state==GameEngine.GameState.PLAYING) engine.state=GameEngine.GameState.PAUSED;
                    else if(engine.state==GameEngine.GameState.PAUSED) engine.state=GameEngine.GameState.PLAYING;
                }
                // Restart
                if(e.getKeyCode()==KeyEvent.VK_R&&engine.state==GameEngine.GameState.GAME_OVER){
                    engine.restart(); renderer=new Renderer();
                }
            }
        });

        new javax.swing.Timer(20, e->{ engine.update(getWidth(),getHeight(),input); repaint(); }).start();
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        renderer.render((Graphics2D)g, engine, getWidth(), getHeight());
    }
}

// main
public class a {
    public static void main(String[] args){
        JFrame frame=new JFrame("Dodge Game");
        GamePanel panel=new GamePanel();
        frame.add(panel);
        frame.setSize(Config.PANEL_W, Config.PANEL_H);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        panel.requestFocusInWindow();
    }
}