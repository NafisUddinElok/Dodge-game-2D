import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

// ===================== CONFIG (Constants) =====================
class Config {
    static final int PLAYER_SPEED   = 4;
    static final int BLOCK_SIZE     = 40;
    static final int INITIAL_SPEED  = 4;
    static final int PLAYER_WIDTH   = 50;
    static final int PLAYER_HEIGHT  = 95;
    static final int MAX_HEALTH     = 3;
}

// ===================== INTERFACE =====================
interface Collidable {
    Rectangle getBounds();
}

// ===================== ABSTRACT BASE CLASS =====================
abstract class GameObject {
    protected int x, y;
    protected int width, height;

    public GameObject(int x, int y, int width, int height) {
        this.x = x; this.y = y;
        this.width = width; this.height = height;
    }

    abstract void update();
    abstract void draw(Graphics2D g2);

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}

// ===================== INPUT HANDLER =====================
class InputHandler extends KeyAdapter {
    boolean up, down, left, right;
    @SuppressWarnings("unused")
            boolean restartPressed;
    boolean pausePressed;

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = true;
        if (e.getKeyCode() == KeyEvent.VK_LEFT)  left  = true;
        if (e.getKeyCode() == KeyEvent.VK_UP)    up    = true;
        if (e.getKeyCode() == KeyEvent.VK_DOWN)  down  = true;
        if (e.getKeyCode() == KeyEvent.VK_R)     restartPressed = true;
        if (e.getKeyCode() == KeyEvent.VK_P)     pausePressed   = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = false;
        if (e.getKeyCode() == KeyEvent.VK_LEFT)  left  = false;
        if (e.getKeyCode() == KeyEvent.VK_UP)    up    = false;
        if (e.getKeyCode() == KeyEvent.VK_DOWN)  down  = false;
        if (e.getKeyCode() == KeyEvent.VK_R)     restartPressed = false;
        if (e.getKeyCode() == KeyEvent.VK_P)     pausePressed   = false;
    }
}

// ===================== PLAYER =====================
class Player extends GameObject implements Collidable {
    private int dx, dy;
    private int health = Config.MAX_HEALTH;
    private boolean shielded = false;
    private int shieldTimer = 0;
    private int invincibleTimer = 0; // brief invincibility after hit

    public Player(int x, int y) {
        super(x, y, Config.PLAYER_WIDTH, Config.PLAYER_HEIGHT);
    }

    public void update(int panelW, int panelH, InputHandler input) {
        dx = input.right ? Config.PLAYER_SPEED : input.left ? -Config.PLAYER_SPEED : 0;
        dy = input.down  ? Config.PLAYER_SPEED : input.up   ? -Config.PLAYER_SPEED : 0;

        x += dx; y += dy;

        // Boundary check
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > panelW - width)  x = panelW - width;
        if (y > panelH - height) y = panelH - height;

        if (shieldTimer > 0)     shieldTimer--;
        if (invincibleTimer > 0) invincibleTimer--;
        if (shieldTimer == 0)    shielded = false;
    }

    @Override public void update() {}

    public boolean isInvincible() { return invincibleTimer > 0; }
    public void activateShield()  { shielded = true; shieldTimer = 300; }
    public boolean isShielded()   { return shielded; }
    public int getHealth()        { return health; }
    public int getShieldTimer()   { return shieldTimer; }

    public boolean hit() {
        if (invincibleTimer > 0) return false;
        if (shielded) { shielded = false; shieldTimer = 0; invincibleTimer = 60; return false; }
        health--;
        invincibleTimer = 60;
        return health <= 0;
    }

    @Override
    public Rectangle getBounds() { return new Rectangle(x + 10, y, 30, height); }

    @Override
    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Flicker when invincible
        if (invincibleTimer > 0 && (invincibleTimer / 5) % 2 == 0) return;

        // Shield aura
        if (shielded) {
            g2.setColor(new Color(0, 180, 255, 80));
            g2.fillOval(x - 10, y - 35, 70, 130);
            g2.setColor(new Color(0, 180, 255, 180));
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(x - 10, y - 35, 70, 130);
            g2.setStroke(new BasicStroke(1));
        }

        // Hair (back)
        g2.setColor(new Color(80, 40, 0));
        g2.fillOval(x + 5, y - 30, 40, 40);
        g2.fillRect(x + 5, y, 8, 30);
        g2.fillRect(x + 37, y, 8, 30);

        // Head
        g2.setColor(new Color(255, 220, 185));
        g2.fillOval(x + 10, y - 28, 30, 30);

        // Eyes
        g2.setColor(Color.BLACK);
        g2.fillOval(x + 15, y - 18, 5, 5);
        g2.fillOval(x + 28, y - 18, 5, 5);

        // Smile
        g2.drawArc(x + 16, y - 12, 18, 8, 180, 180);

        // Neck
        g2.setColor(new Color(255, 220, 185));
        g2.fillRect(x + 20, y + 2, 10, 8);

        // Dress top
        g2.setColor(new Color(220, 80, 120));
        g2.fillRoundRect(x + 10, y + 10, 30, 30, 10, 10);

        // Skirt
        int[] skirtX = {x, x + 12, x + 38, x + 50};
        int[] skirtY = {y + 65, y + 40, y + 40, y + 65};
        g2.fillPolygon(skirtX, skirtY, 4);

        // Arms
        g2.setColor(new Color(255, 220, 185));
        g2.fillRoundRect(x - 5,  y + 12, 14, 8, 6, 6);
        g2.fillRoundRect(x + 41, y + 12, 14, 8, 6, 6);

        // Legs
        g2.fillRoundRect(x + 15, y + 65, 8, 25, 5, 5);
        g2.fillRoundRect(x + 27, y + 65, 8, 25, 5, 5);

        // Shoes
        g2.setColor(new Color(80, 40, 0));
        g2.fillOval(x + 12, y + 87, 13, 7);
        g2.fillOval(x + 25, y + 87, 13, 7);
    }
}

// ===================== NORMAL BLOCK =====================
class Block extends GameObject implements Collidable {
    protected int speed;

    public Block(int x, int y, int speed) {
        super(x, y, Config.BLOCK_SIZE, Config.BLOCK_SIZE);
        this.speed = speed;
    }

    @Override public void update() { y += speed; }

    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(new Color(220, 50, 50));
        g2.fillRect(x, y, width, height);
        g2.setColor(new Color(150, 20, 20));
        g2.drawRect(x, y, width, height);
    }
}

// ===================== FAST BLOCK (Inheritance + Polymorphism) =====================
class FastBlock extends Block {
    public FastBlock(int x, int y) {
        super(x, y, 10);
        width = 20; height = 20;
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(new Color(255, 140, 0));
        g2.fillRect(x, y, width, height);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 9));
        g2.drawString("FAST", x + 1, y + 13);
    }
}

// ===================== BIG BLOCK (Inheritance + Polymorphism) =====================
class BigBlock extends Block {
    public BigBlock(int x, int y, int speed) {
        super(x, y, Math.max(2, speed - 2));
        width = 65; height = 65;
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(new Color(120, 0, 180));
        g2.fillRect(x, y, width, height);
        g2.setColor(new Color(80, 0, 130));
        g2.drawRect(x, y, width, height);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.drawString("BIG", x + 22, y + 37);
    }
}

// ===================== ZIG-ZAG BLOCK (Inheritance + Polymorphism) =====================
class ZigZagBlock extends Block {
    private int zigTimer = 0;
    private int zigDir = 1;

    public ZigZagBlock(int x, int y) {
        super(x, y, 3);
        width = 30; height = 30;
    }

    @Override
    public void update() {
        y += speed;
        zigTimer++;
        if (zigTimer % 30 == 0) zigDir *= -1;
        x += zigDir * 4;
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(new Color(0, 200, 100));
        g2.fillRect(x, y, width, height);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 8));
        g2.drawString("ZIG", x + 6, y + 19);
    }
}

// ===================== POWERUP =====================
class PowerUp extends GameObject {
    public enum Type { SHIELD, SLOW, HEALTH }
    public Type type;
    private int speed = 3;
    public boolean collected = false;

    public PowerUp(int x, int y, Type type) {
        super(x, y, 28, 28);
        this.type = type;
    }

    @Override public void update() { y += speed; }

    @Override
    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        switch (type) {
            case SHIELD:
                g2.setColor(new Color(0, 180, 255));
                g2.fillOval(x, y, width, height);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 14));
                g2.drawString("S", x + 9, y + 20);
                break;
            case SLOW:
                g2.setColor(new Color(100, 100, 255));
                g2.fillOval(x, y, width, height);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 11));
                g2.drawString("SLO", x + 2, y + 19);
                break;
            case HEALTH:
                g2.setColor(new Color(255, 60, 100));
                g2.fillOval(x, y, width, height);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 16));
                g2.drawString("+", x + 7, y + 21);
                break;
        }
    }
}

// ===================== SCORE MANAGER (Static) =====================
class ScoreManager {
    private static int score = 0;
    private static int highScore = 0;

    public static void add(int pts)  { score += pts; if (score > highScore) highScore = score; }
    public static void reset()       { score = 0; }
    public static int getScore()     { return score; }
    public static int getHighScore() { return highScore; }
    public static int getLevel()     { return score / 300 + 1; }
}

// ===================== GAME ENGINE (Separation of Concerns) =====================
class GameEngine {
    Player player;
    ArrayList<GameObject> objects = new ArrayList<>();
    int blockSpeed = Config.INITIAL_SPEED;
    boolean slowActive = false;
    int slowTimer = 0;

    enum GameState { PLAYING, PAUSED, GAME_OVER }
    GameState state = GameState.PLAYING;

    public GameEngine() {
        player = new Player(100, 150);
    }

    public void update(int panelW, int panelH, InputHandler input) {
        if (state != GameState.PLAYING) return;

        // Smooth difficulty
        blockSpeed = Config.INITIAL_SPEED + ScoreManager.getScore() / 200;

        // Slow powerup effect
        int effectiveSpeed = slowActive ? Math.max(1, blockSpeed / 2) : blockSpeed;
        if (slowTimer > 0) { slowTimer--; if (slowTimer == 0) slowActive = false; }

        player.update(panelW, panelH, input);

        // Spawn enemies
        double spawnRate = 0.02 + ScoreManager.getLevel() * 0.004;
        if (Math.random() < spawnRate) {
            int rx = (int)(Math.random() * (panelW - 60));
            double r = Math.random();
            if      (r < 0.35) objects.add(new Block(rx, 0, effectiveSpeed));
            else if (r < 0.55) objects.add(new FastBlock(rx, 0));
            else if (r < 0.70) objects.add(new BigBlock(rx, 0, effectiveSpeed));
            else               objects.add(new ZigZagBlock(rx, 0));
        }

        // Spawn powerups
        if (Math.random() < 0.004) {
            int rx = (int)(Math.random() * (panelW - 30));
            double r = Math.random();
            PowerUp.Type t = r < 0.4 ? PowerUp.Type.SHIELD :
                             r < 0.7 ? PowerUp.Type.SLOW : PowerUp.Type.HEALTH;
            objects.add(new PowerUp(rx, 0, t));
        }

        // Update all objects — Polymorphism
        for (GameObject obj : objects) obj.update();
        objects.removeIf(obj -> obj.y > panelH + 100);

        // Collision — PowerUps
        objects.removeIf(obj -> {
            if (obj instanceof PowerUp) {
                PowerUp pu = (PowerUp) obj;
                if (player.getBounds().intersects(pu.getBounds())) {
                    applyPowerUp(pu.type);
                    return true;
                }
            }
            return false;
        });

        // Collision — Blocks
        for (GameObject obj : objects) {
            if (obj instanceof Block && player.getBounds().intersects(obj.getBounds())) {
                if (player.hit()) state = GameState.GAME_OVER;
            }
        }

        ScoreManager.add(1);
    }

    void applyPowerUp(PowerUp.Type type) {
        switch (type) {
            case SHIELD: player.activateShield(); break;
            case SLOW:   slowActive = true; slowTimer = 300; break;
            case HEALTH:
                // max 3 health
                break;
        }
    }

    public void restart() {
        player = new Player(100, 150);
        objects.clear();
        blockSpeed = Config.INITIAL_SPEED;
        slowActive = false; slowTimer = 0;
        ScoreManager.reset();
        state = GameState.PLAYING;
    }
}

// ===================== GAME PANEL (Rendering only) =====================
class GamePanel extends JPanel {

    GameEngine engine = new GameEngine();
    InputHandler input = new InputHandler();

    public GamePanel() {
        setFocusable(true);
        addKeyListener(input);

        // Pause toggle
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_P) {
                    if (engine.state == GameEngine.GameState.PLAYING)
                        engine.state = GameEngine.GameState.PAUSED;
                    else if (engine.state == GameEngine.GameState.PAUSED)
                        engine.state = GameEngine.GameState.PLAYING;
                }
                if (e.getKeyCode() == KeyEvent.VK_R &&
                    engine.state == GameEngine.GameState.GAME_OVER) {
                    engine.restart();
                }
            }
        });

        Timer timer = new Timer(20, e -> {
            engine.update(getWidth(), getHeight(), input);
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2);

        if (engine.state == GameEngine.GameState.PLAYING ||
            engine.state == GameEngine.GameState.PAUSED) {

            // Draw all objects — Polymorphism
            for (GameObject obj : engine.objects) obj.draw(g2);
            engine.player.draw(g2);
            drawHUD(g2);

            if (engine.state == GameEngine.GameState.PAUSED) drawPause(g2);

        } else if (engine.state == GameEngine.GameState.GAME_OVER) {
            drawGameOver(g2);
        }
    }

    void drawBackground(Graphics2D g2) {
        // Sky gradient
        GradientPaint sky = new GradientPaint(0, 0, new Color(100, 180, 255),
                                               0, getHeight(), new Color(200, 230, 255));
        g2.setPaint(sky);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Clouds
        g2.setColor(new Color(255, 255, 255, 200));
        drawCloud(g2, 80,  20);
        drawCloud(g2, 380, 35);
        drawCloud(g2, 680, 18);
        drawCloud(g2, 870, 40);

        // Sun
        g2.setColor(new Color(255, 220, 50, 220));
        g2.fillOval(getWidth() - 90, 10, 65, 65);
        g2.setColor(new Color(255, 240, 100, 100));
        g2.fillOval(getWidth() - 100, 0, 85, 85);

        // Ground
        g2.setColor(new Color(34, 139, 34));
        g2.fillRect(0, getHeight() - 25, getWidth(), 25);
        g2.setColor(new Color(25, 110, 25));
        g2.fillRect(0, getHeight() - 25, getWidth(), 5);
    }

    void drawCloud(Graphics2D g2, int cx, int cy) {
        g2.fillOval(cx,      cy,      80, 35);
        g2.fillOval(cx + 20, cy - 15, 55, 35);
        g2.fillOval(cx - 10, cy + 5,  50, 28);
    }

    void drawHUD(Graphics2D g2) {
        // Health hearts
        for (int i = 0; i < Config.MAX_HEALTH; i++) {
            g2.setColor(i < engine.player.getHealth() ?
                new Color(220, 50, 50) : new Color(180, 180, 180));
            g2.setFont(new Font("Arial", Font.PLAIN, 22));
            g2.drawString("♥", 15 + i * 28, 30);
        }

        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.drawString("Score: " + ScoreManager.getScore(),    15, 55);
        g2.drawString("Best:  " + ScoreManager.getHighScore(), 15, 75);
        g2.drawString("Level: " + ScoreManager.getLevel(),    15, 95);

        // Shield timer bar
        if (engine.player.isShielded()) {
            g2.setColor(new Color(0, 180, 255, 180));
            g2.fillRoundRect(15, 105, engine.player.getShieldTimer() / 2, 10, 5, 5);
            g2.setColor(Color.BLACK);
            g2.drawString("SHIELD", 15, 128);
        }

        // Slow indicator
        if (engine.slowActive) {
            g2.setColor(new Color(100, 100, 255));
            g2.setFont(new Font("Arial", Font.BOLD, 13));
            g2.drawString("SLOW ACTIVE", 15, 145);
        }

        // Legend top-right
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.setColor(new Color(220, 50, 50));  g2.drawString("■ Normal",  getWidth()-110, 20);
        g2.setColor(new Color(255,140,  0));  g2.drawString("■ Fast",    getWidth()-110, 38);
        g2.setColor(new Color(120,  0,180));  g2.drawString("■ Big",     getWidth()-110, 56);
        g2.setColor(new Color(  0,200,100));  g2.drawString("■ ZigZag",  getWidth()-110, 74);
        g2.setColor(new Color(  0,180,255));  g2.drawString("◉ Shield",  getWidth()-110, 92);
        g2.setColor(new Color(100,100,255));  g2.drawString("◉ Slow",    getWidth()-110,110);
        g2.setColor(new Color(255, 60,100));  g2.drawString("◉ Health",  getWidth()-110,128);

        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        g2.drawString("P = Pause", getWidth() - 85, getHeight() - 10);
    }

    void drawPause(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 130));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 45));
        g2.drawString("PAUSED", 370, 180);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        g2.drawString("Press P to continue", 370, 220);
    }

    void drawGameOver(Graphics2D g2) {
        drawBackground(g2);
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(new Color(255, 60, 60));
        g2.setFont(new Font("Arial", Font.BOLD, 55));
        g2.drawString("GAME OVER", 290, 160);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 26));
        g2.drawString("Score:  " + ScoreManager.getScore(),    390, 215);
        g2.drawString("Best:   " + ScoreManager.getHighScore(), 390, 250);
        g2.setColor(new Color(100, 255, 100));
        g2.drawString("Press R to Restart", 345, 310);
    }
}

// ===================== MAIN =====================
public class a {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Dodge Game");
        GamePanel panel = new GamePanel();
        frame.add(panel);
        frame.setSize(1000, 500);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        panel.requestFocusInWindow();
    }
}