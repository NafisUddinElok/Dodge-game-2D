package blocks;
import entities.GameObject;
import powerups.PowerUp;
public class BlockFactory {
    public static GameObject createRandom(int panelW, int speed, int playerX) {
        int rx=(int)(Math.random()*(panelW-65));
        double r=Math.random();
        if      (r<0.35) return new NormalBlock(rx,0,speed);
        else if (r<0.55) return new FastBlock(rx,0);
        else if (r<0.70) return new BigBlock(rx,0,speed);
        else             return new EnemyBlock(rx,0,playerX);
    }
    public static GameObject createPowerUp(int panelW) {
        int rx=(int)(Math.random()*(panelW-30));
        double r=Math.random();
        if      (r<0.4) return new PowerUp(rx,0,PowerUp.Type.SHIELD);
        else if (r<0.7) return new PowerUp(rx,0,PowerUp.Type.SLOW);
        else            return new PowerUp(rx,0,PowerUp.Type.HEALTH);
    }
}
