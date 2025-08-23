import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Enemy extends MovePlatform {
    public static final int ENEMY_WIDTH = 48;
    public static final int ENEMY_HEIGHT = 48;
    public static final int SPEED = 4;
    public static final int DAMAGE = 10;
    public static final int DEFAULT_HEALTH = 30;
    public static final double HEIGHT_ON_JUMP = 12;
    private static final String damageSound = Panel.getResourceFilePath("SoundAssets/damage.wav");
    private static final String deathSound = Panel.getResourceFilePath("SoundAssets/death.wav");

    private static Image[] idle,squash, death;
    private Image[] run; //This isn't static because the run animation might be the idle animation for some enemies
    private static final int ANIMATION_FRAME_RATE = 80;

    static {
        idle = Panel.loadAnimation(Panel.getResourceFilePath("EnemyAssets/IdleAnimation"));
        squash = Panel.loadAnimation(Panel.getResourceFilePath("EnemyAssets/SquashAnimation"));
        death = Panel.loadAnimation(Panel.getResourceFilePath("EnemyAssets/DeathAnimation"));
    }
    private Image[] cur_cycle;
    private int curFrame;

    private Bar hp;
    private Timer t;
    private String enemyType;
    private int direction;

    private boolean damageByCrit;

    public Enemy(int initX,int destX,int y,int time, int health,boolean damageByCrit) {
        super(initX,y, ENEMY_WIDTH, ENEMY_HEIGHT,destX,y,time);
        run = Panel.loadAnimation(Panel.getResourceFilePath("EnemyAssets/RunAnimation"));
        enemyType = "Basic";
        setTag("Enemy");
        hp = new Bar(health,ENEMY_WIDTH);
        if(destX == initX) run = idle;
        cur_cycle = run;
        setImage_height(ENEMY_HEIGHT);
        setImage_width(ENEMY_WIDTH);
        direction = 1;
        this.damageByCrit = damageByCrit;

        Timer t = new Timer(ANIMATION_FRAME_RATE, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!(cur_cycle == death && curFrame == death.length-1)) { //If not on the last scene of the death animation
                    direction = (getSpeed()[0] > 0 ? 1 : -1);
                    curFrame = (curFrame + 1) % cur_cycle.length; //Go to next frame
                    if(cur_cycle == squash && curFrame == squash.length-1) { //If on the last scene of damage, go to idle screen
                        cur_cycle = run;
                        curFrame = 0;
                    }
                } else {
                    setVisible(false);
                }
            }
        });
        t.start();
    }

    public Enemy(int initX, int destX, int y, int time) {
        this(initX,destX,y,time,DEFAULT_HEALTH,false);
    }

    public void die() {
        if(cur_cycle != death) {
            setTag("Invisible");
            setColor(new Color(0, 0, 0, 0));
            cur_cycle = death;
            curFrame = 0;
            StdAudio.playInBackground(deathSound);
        }
    }

    public String getEnemyType() {
        return enemyType;
    }

    public void setEnemyType(String enemyType) {
        this.enemyType = enemyType;
    }

    public void drawImage(Graphics g) {

        g.drawImage(cur_cycle[curFrame],x+(direction == -1 ? width : 0),y,width * (direction == -1 ? -1 : 1),height,null);
        if(!getTag().equals("Invisible")) {
            hp.setPos(x, y);
            hp.drawSelf(g);
        }
    }

    public Bar getHp() {
        return hp;
    }

    public void squash() { //play animation and sound
        if(cur_cycle != squash && cur_cycle != death) {
            cur_cycle = squash;
            curFrame = 0;
            damage();
        }
    }

    public boolean isDamageByCrit() {
        return damageByCrit;
    }

    public void damage() {
        StdAudio.playInBackground(damageSound);
    }
}
