package manager;
import engine.EventBus;
public class ScoreManager {
    private static ScoreManager instance;
    private int score=0,highScore=0,combo=1,comboTimer=0;
    private ScoreManager() {}
    public static ScoreManager getInstance(){ if(instance==null) instance=new ScoreManager(); return instance; }
    public void update()        { if(comboTimer>0) comboTimer--; else combo=1; }
    public void addTime()       { score+=combo; if(score>highScore) highScore=score; }
    public void nearMissBonus() { combo=Math.min(combo+1,5); comboTimer=120; score+=10*combo; EventBus.emit("NEAR_MISS"); }
    public void reset()         { score=0; combo=1; comboTimer=0; }
    public int getScore()       { return score; }
    public int getHighScore()   { return highScore; }
    public int getLevel()       { return score/300+1; }
    public int getCombo()       { return combo; }
    public boolean hasCombo()   { return combo>1; }
}
