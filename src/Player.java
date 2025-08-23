import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class Player extends Rectangle {


    public static final int PLAYER_WIDTH = 48;
    public static final int PLAYER_HEIGHT = 48;
    public static final int HITBOX_WIDTH = 12;
    public static final int HITBOX_HEIGHT = 48;
    public static final int STOMP_DAMAGE = 10;
    private static final int ANIMATION_FRAME_RATE = 125;
    private static final Color PLAYER_COLOR = Color.BLUE;
    public double realX, realY;
    private Bar hp, mp;
    private int direction;
    private static final String dashSound = Panel.getResourceFilePath("SoundAssets/dash.wav");
    private static final String jumpSound = Panel.getResourceFilePath("SoundAssets/jump.wav");
    private static final String damageSound = Panel.getResourceFilePath("SoundAssets/damage.wav");

    private static Image[] idle, death, fall, jump, run, damage;
    static {
        setAnimations();
    }

    private Image[] cur_cycle; //Current animation
    private int curFrame; //Current frame in animation

    public Player(int xCoord, int yCoord)
    {
        super(xCoord,yCoord,HITBOX_WIDTH,HITBOX_HEIGHT);
        setAnimations(); //Set all animations
        realX = x;
        realY = y;
        hp = new Bar(PLAYER_WIDTH);
        mp = new Bar(24,PLAYER_WIDTH,Color.CYAN,3);
        cur_cycle = idle; //sets the current animation to idle

        Timer t = new Timer(ANIMATION_FRAME_RATE, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!(cur_cycle == death && curFrame == death.length-1)) { //If not on the last scene of the death animation
                    curFrame = (curFrame + 1) % cur_cycle.length; //Go to next frame
                    if(cur_cycle == damage && curFrame == damage.length-1) { //If on the last scene of damage, go to idle screen
                        cur_cycle = idle;
                        curFrame = 1;
                    }
                }
            }
        });
        t.start();
    }

    public void drawSelf(Graphics g) { //Draws itself on the panel
        //new_g.fill(this);
        if(hp.getPoints() > 0) { //Draw mana and hp if alive
            hp.drawSelf(g);
            mp.drawSelf(g);
        }
        g.drawImage(cur_cycle[curFrame],x-(PLAYER_WIDTH-HITBOX_WIDTH)/2 + (direction == -1 ? PLAYER_WIDTH : 0),y-(PLAYER_HEIGHT-HITBOX_HEIGHT),(width+(PLAYER_WIDTH-HITBOX_WIDTH))*(direction == -1 ? -1 : 1),PLAYER_HEIGHT,null); //Draw the actual player
        //((Graphics2D) g).fill(this);
    }

    public void changeX(double x) { //Change x of player including changing hp and mp pos
        realX += x;
        this.x = (int) realX;
        hp.setPos(this.x-(PLAYER_WIDTH-HITBOX_WIDTH)/2,this.y);
        mp.setPos(this.x-(PLAYER_WIDTH-HITBOX_WIDTH)/2,this.y);
    }

    public void setX(double x) { //Change x of player including changing hp and mp pos
        realX = x;
        this.x = (int) realX;
        hp.setPos(this.x-(PLAYER_WIDTH-HITBOX_WIDTH)/2,this.y);
        mp.setPos(this.x-(PLAYER_WIDTH-HITBOX_WIDTH)/2,this.y);
    }

    public void changeY(double y) { //Change y of player including changing hp and mp pos
        realY += y;
        this.y = (int) realY;
        hp.setPos(this.x,this.y-(PLAYER_HEIGHT-HITBOX_HEIGHT));
        mp.setPos(this.x,this.y-(PLAYER_HEIGHT-HITBOX_HEIGHT));
    }

    public void setY(double y) { //Change x of player including changing hp and mp pos
        realY = y;
        this.y = (int) realY;
        hp.setPos(this.x-(PLAYER_WIDTH-HITBOX_WIDTH)/2,this.y);
        mp.setPos(this.x-(PLAYER_WIDTH-HITBOX_WIDTH)/2,this.y);
    }

    public Bar getHp() {
        return hp;
    }

    public Bar getMp() {
        return mp;
    }

    public void revive() {
        cur_cycle = idle;
        curFrame = 0;
    }

    public static void setAnimations() { //loads in all animations
        idle = Panel.loadAnimation(Panel.getResourceFilePath("PlayerAssets/IdleAnimation"));
        death = Panel.loadAnimation(Panel.getResourceFilePath("PlayerAssets/DeathAnimation"));
        fall = Panel.loadAnimation(Panel.getResourceFilePath("PlayerAssets/FallAnimation"));
        run = Panel.loadAnimation(Panel.getResourceFilePath("PlayerAssets/RunAnimation"));
        damage = Panel.loadAnimation(Panel.getResourceFilePath("PlayerAssets/DamageAnimation"));
        jump = Panel.loadAnimation(Panel.getResourceFilePath("PlayerAssets/JumpAnimation"));
    }

    public void run() {
        if(cur_cycle == run || cur_cycle == death || cur_cycle == damage) return; //If current animation not already playing, and current animation is not death or damage then change animation
        curFrame = 0;
        cur_cycle = run;
    }

    public void idle() {
        if(cur_cycle == idle || cur_cycle == death || cur_cycle == damage) return; //If current animation not already playing, and current animation is not death or damage then change animation
        curFrame = 0;
        cur_cycle = idle;
    }

    public void death() {
        if(cur_cycle == death) return; //If not already in death animation
        curFrame = 0;
        cur_cycle = death;
    }

    public void jump() {
        if(cur_cycle == jump || cur_cycle == death || cur_cycle == damage) return; //If current animation not already playing, and current animation is not death or damage then change animation
        curFrame = 0;
        cur_cycle = jump;
        StdAudio.playInBackground(jumpSound);
    }

    public void fall() { //If current animation not already playing, and current animation is not death or damage then change animation
        if(cur_cycle == fall || cur_cycle == death || cur_cycle == damage) return;
        curFrame = 0;
        cur_cycle = fall;
    }

    public void damage() {
        if(cur_cycle == damage || cur_cycle == death) return; //If not already in damage or death animation
        curFrame = 0;
        cur_cycle = damage;
        StdAudio.playInBackground(damageSound);
    }

    public void dash() {
        StdAudio.playInBackground(dashSound);
    }


    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getDirection() {
        return direction;
    }
}
