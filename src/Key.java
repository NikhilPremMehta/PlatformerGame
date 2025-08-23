import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Key extends Tile implements Activatable {

    public static final int DEFAULT_KEY_HEIGHT = 48;
    public static final int DEFAULT_KEY_WIDTH = 48;
    private static final int ANIMATION_FRAME_RATE = 75;
    private static final String activatedSound = Panel.getResourceFilePath("SoundAssets/coinPickup.wav");
    private static final double[] quieterSound = StdAudio.read(activatedSound);

    private static Image[] idle, open;
    static {
        idle = Panel.loadAnimation(Panel.getResourceFilePath("TileAssets/KeyAssets/IdleAnimation"));
        open = Panel.loadAnimation(Panel.getResourceFilePath("TileAssets/KeyAssets/TouchedAnimation"));
    }

    private boolean activated;
    private Image[] cur_cycle;
    private int curFrame;

    public Key(int initX, int initY, int width, int height) {
        super(initX,initY,width,height);
        setTag("Key"); //Sets the tag
        setColor(Color.GREEN);
        cur_cycle = idle;
        setImage_height(height);
        setImage_width(width);

        Timer t = new Timer(ANIMATION_FRAME_RATE, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!(cur_cycle == open && curFrame == open.length-1)) {
                    curFrame = (curFrame + 1) % cur_cycle.length; //Go to next frame
                } else {
                    setVisible(false);
                }
            }
        });
        t.start();
    }

    public Key(int initX, int initY) {
        this(initX,initY,DEFAULT_KEY_WIDTH,DEFAULT_KEY_HEIGHT);
    }

    public void drawImage(Graphics g) {
        g.drawImage(cur_cycle[curFrame],x,y,width,height,null);
    }

    public boolean isActivated() {
        return activated;
    }

    public void activate() {
        activated = true;
        setTag("Invisible");
        setColor(new Color(getColor().getRed(),getColor().getGreen(),getColor().getBlue(),0));
        cur_cycle = open;
        curFrame = 0;
        StdAudio.playInBackground(activatedSound);
    }
}
