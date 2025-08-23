import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class Checkpoint extends Tile{

    public static final int DEFAULT_CHECKPOINT_WIDTH = 36;
    public static final int DEFAULT_CHECKPOINT_HEIGHT = 60;
    private static final int ANIMATION_FRAME_RATE = 150;
    private static final String checkpointActivated = Panel.getResourceFilePath("SoundAssets/checkpointActivated.wav");

    private int[] checkPointScene;
    private int[] checkPointCoords;
    private Image[] deactivated;
    private Image[] activated;
    private Image[] cur_cycle;
    private int curFrame;

    public Checkpoint(int initX, int initY, int width, int height,int[] checkPointScene) {
        super(initX,initY,width,height);
        setTag("Checkpoint"); //Sets the tag
        setColor(Color.MAGENTA);
        //Set the checkpoint coords and scene
        this.checkPointScene = checkPointScene;
        this.checkPointCoords = new int[] {initX+DEFAULT_CHECKPOINT_WIDTH/2-Player.HITBOX_WIDTH/2,initY+DEFAULT_CHECKPOINT_HEIGHT-Player.PLAYER_HEIGHT};
        activated = Panel.loadAnimation(Panel.getResourceFilePath("TileAssets/CheckpointSprites/CheckpointActivated"));
        deactivated = Panel.loadAnimation(Panel.getResourceFilePath("TileAssets/CheckpointSprites/CheckpointDeactivated"));
        cur_cycle = deactivated;

        Timer t = new Timer(ANIMATION_FRAME_RATE, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                curFrame = (curFrame + 1) % cur_cycle.length; //Go to next frame
            }
        });
        t.start();
    }

    public Checkpoint(int initX, int initY, int[] checkPointScene) {
        this(initX,initY,DEFAULT_CHECKPOINT_WIDTH,DEFAULT_CHECKPOINT_HEIGHT,checkPointScene);
    }

    @Override
    public void drawImage(Graphics g) {
        g.drawImage(cur_cycle[curFrame],x,y,width,height,null);
    }

    public int[] getCheckPointScene() {
        return checkPointScene;
    }

    public int[] getCheckPointCoords() {
        return checkPointCoords;
    }

    public void activate() { //Play the activation sound and change the cycle to activated
        if(cur_cycle != activated) {
            cur_cycle = activated;
            curFrame = 0;
            StdAudio.playInBackground(checkpointActivated);
        }
    }

    public void deactivate() { //Change cycle to idle/deactivated
        if(cur_cycle != deactivated) {
            cur_cycle = deactivated;
            curFrame = 0;
        }
    }
}
