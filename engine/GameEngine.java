package engine;
import blocks.BlockFactory;
import blocks.EnemyBlock;
import config.Config;
import entities.GameObject;
import entities.Player;
import input.InputHandler;
import manager.ScoreManager;
import java.awt.Rectangle;
import java.util.*;
public class GameEngine {
    public Player player;
    public List<GameObject> objects=new ArrayList<>();
    public boolean slowActive=false;
    public int slowTimer=0,nearMissTimer=0;
    public String playerName;
    public enum GameState { NAME_INPUT,PLAYING,PAUSED,GAME_OVER }
    public GameState state=GameState.NAME_INPUT;

    public GameEngine(String name){ this.playerName=name; player=new Player(100,150,name); registerEvents(); }

    private void registerEvents(){
        EventBus.on("GAME_OVER",d->state=GameState.GAME_OVER);
        EventBus.on("SLOW_ON",d->{ slowActive=true; slowTimer=300; });
    }

    public void update(int panelW,int panelH,InputHandler input){
        if(state!=GameState.PLAYING) return;
        int speed=Config.INITIAL_SPEED+ScoreManager.getInstance().getScore()/200;
        int effectiveSpeed=slowActive?Math.max(1,speed/2):speed;
        if(slowTimer>0){ slowTimer--; if(slowTimer==0) slowActive=false; }
        player.update(panelW,panelH,input);
        ScoreManager.getInstance().addTime();
        for(GameObject obj:objects) if(obj instanceof EnemyBlock) ((EnemyBlock)obj).updatePlayerX(player.getX());
        double spawnRate=0.02+ScoreManager.getInstance().getLevel()*0.004;
        if(Math.random()<spawnRate) objects.add(BlockFactory.createRandom(panelW,effectiveSpeed,player.getX()));
        if(Math.random()<0.004) objects.add(BlockFactory.createPowerUp(panelW));
        for(GameObject obj:objects) obj.update();
        objects.removeIf(obj->!obj.isAlive()||obj.getY()>panelH+100);
        for(GameObject obj:objects) if(player.getBounds().intersects(obj.getBounds())) obj.onCollision(player);
        checkNearMiss();
        if(nearMissTimer>0) nearMissTimer--;
    }

    private void checkNearMiss(){
        for(GameObject obj:objects){
            Rectangle pb=player.getBounds();
            Rectangle exp=new Rectangle(pb.x-15,pb.y-15,pb.width+30,pb.height+30);
            Rectangle ob=obj.getBounds();
            if(exp.intersects(ob)&&!pb.intersects(ob)&&nearMissTimer==0){ ScoreManager.getInstance().nearMissBonus(); nearMissTimer=60; }
        }
    }

    public void restart(){
        EventBus.clear(); player=new Player(100,150,playerName); objects.clear(); slowActive=false; slowTimer=0;
        ScoreManager.getInstance().reset(); state=GameState.PLAYING; registerEvents();
    }

    public void setPlayerName(String name){ this.playerName=name; player=new Player(100,150,name); state=GameState.PLAYING; }
}
