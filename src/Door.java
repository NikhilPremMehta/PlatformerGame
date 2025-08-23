import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Door extends Tile {

    private static final int ANIMATION_FRAME_RATE = 160;
    public static final int HITBOX_WIDTH = 96;
    public static final int HITBOX_HEIGHT = 96;
    private static final int DEFAULT_DOOR_WIDTH = 288;
    private static final int DEFAULT_DOOR_HEIGHT = 192;
    private static final int OFFSETX = 96;
    private static final int OFFSETY = 48;

    private ArrayList<Key> keys;
    private static Image[] idle, open;
    private static final String unlockSound = Panel.getResourceFilePath("SoundAssets/doorUnlock.wav");

    static {
        idle = Panel.loadAnimation(Panel.getResourceFilePath("TileAssets/DoorSprites/DoorIdle"));
        open = Panel.loadAnimation(Panel.getResourceFilePath("TileAssets/DoorSprites/DoorOpen"));
    }
    private Image[] cur_cycle;
    private int curFrame;

    public Door(int initX, int initY, int width, int height, ArrayList<Key> k) {
        super(initX,initY,width,height);
        setTag("Door"); //Sets the tag
        setColor(Color.blue);
        keys = k;
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

    public Door(int initX, int initY,ArrayList<Key> k) {
        this(initX,initY,HITBOX_WIDTH,HITBOX_HEIGHT, k);
    }

    public void open() {
        for(Key k : keys) { //If not every key is activated, then dont open
            if(!k.isActivated())
                return;
        }
        setTag("Invisible");
        cur_cycle = open;
        curFrame = 0;
        StdAudio.playInBackground(unlockSound); //play sound
        setColor(new Color(getColor().getRed(),getColor().getGreen(),getColor().getBlue(),0));
    }

    public void drawImage(Graphics g) {
        g.drawImage(cur_cycle[curFrame],x-OFFSETX,y-OFFSETY,DEFAULT_DOOR_WIDTH,DEFAULT_DOOR_HEIGHT,null);
    }
}
