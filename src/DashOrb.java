import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DashOrb extends BreakPlatform implements Activatable{
    private static final int DASHORB_DEFAULT_HEIGHT = 24;
    private static final int DASHORB_DEFAULT_WIDTH = 24;
    private static final int ANIMATION_FRAME_RATE = 100;
    private static final String orbPickupSound = Panel.getResourceFilePath("SoundAssets/OrbPickup.wav");
    private static Image[] idle, pickedUp;
    static {
        idle = Panel.loadAnimation(Panel.getResourceFilePath("TileAssets/DashOrbSprites/DashOrbIdle"));
        pickedUp = Panel.loadAnimation(Panel.getResourceFilePath("TileAssets/DashOrbSprites/DashOrbPickup"));
    }

    private Image[] cur_cycle;
    private int curFrame;

    public DashOrb(int initX, int initY, int width, int height,double time) {
        super(initX,initY,width,height,0,time);
        setTag("Dash Orb"); //Sets the tag
        setColor(Color.BLUE);
        initTag = "Dash Orb";

        cur_cycle = idle;
        Timer t = new Timer(ANIMATION_FRAME_RATE, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!(cur_cycle == pickedUp && curFrame == pickedUp.length-1)) {
                    curFrame = (curFrame + 1) % cur_cycle.length; //Go to next frame
                } else {
                    setVisible(false); //If picked up, set to idle
                }
            }
        });
        t.start();
    }

    public DashOrb(int initX, int initY,double time) {
        this(initX,initY,DASHORB_DEFAULT_WIDTH,DASHORB_DEFAULT_HEIGHT,time);
    }
    public DashOrb(int initX, int initY) {
        this(initX,initY,DASHORB_DEFAULT_WIDTH,DASHORB_DEFAULT_HEIGHT,DEFAULT_REAPPEAR_TIME);
    }


    public void drawImage(Graphics g) {
        g.drawImage(cur_cycle[curFrame],x,y,width,height,null);
    }

    @Override
    public void activate() {
        setTag("Invisible");
        setColor(new Color(getColor().getRed(),getColor().getGreen(),getColor().getBlue(),0));
        break_platform();
        cur_cycle = pickedUp;
        curFrame = 0;
        StdAudio.playInBackground(orbPickupSound); //Plays sound
    }

    @Override
    public void reappear() {
        super.reappear();
        cur_cycle = idle; //Sets animation to idle
        curFrame = 0;
    }

    public void drawSelf(Graphics g) {
        if(isVisible()) {
            drawImage(g);
        }
    }
}
