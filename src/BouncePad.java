import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class BouncePad extends Tile{
    public static final int BOUNCE_HEIGHT = 12;
    public static final int EXTRA_JUMP_HEIGHT = 4;
    private static final int ANIMATION_FRAME_RATE = 50;
    public static final int DEFAULT_BOUNCE_PAD_WIDTH = 42;
    public static final int DEFAULT_BOUNCE_PAD_HEIGHT = 36;
    private static Image idle;
    private static Image[] bounce;
    private int curFrame;
    private static final String spring = Panel.getResourceFilePath("SoundAssets/spring.wav");
    static {
        try {
            idle = ImageIO.read(new File(Panel.getResourceFilePath("TileAssets/BouncePadSprites/SpringIdle/Idle.png")));
        } catch (IOException e) {
            System.out.println(e);
        }
        bounce = Panel.loadAnimation(Panel.getResourceFilePath("TileAssets/BouncePadSprites/BounceAnimation"));
    }

    public BouncePad(int initX, int initY, int width, int height) {
        super(initX,initY,width,height);
        setTag("Bounce Pad"); //Sets the tag
        setColor(Color.YELLOW);
        curFrame = -1;
        setImage_height(33);
        setImage_width(36);
        Timer t = new Timer(ANIMATION_FRAME_RATE, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(curFrame != -1) {
                    curFrame = (curFrame + 1); //Go to next frame
                    if(curFrame > bounce.length-1) curFrame = -1; //If at the end of the bounce animation, go to idle
                }
            }
        });
        t.start();
    }

    public BouncePad(int initX, int initY,int angle) {
        this(initX,initY,DEFAULT_BOUNCE_PAD_WIDTH,DEFAULT_BOUNCE_PAD_HEIGHT);
    }
    public BouncePad(int initX, int initY) {
        this(initX,initY,DEFAULT_BOUNCE_PAD_WIDTH,DEFAULT_BOUNCE_PAD_HEIGHT);
    }

    @Override
    public void drawImage(Graphics g) {
        if(curFrame == -1) { //If idle show image, else show frame
            g.drawImage(idle,x,y,width,height,null);
        } else {
            g.drawImage(bounce[curFrame],x,y,width,height,null);
        }
    }

    public void bounce() { //Set the animation to bounce
        if(curFrame == -1) {
            curFrame = 0;
            StdAudio.playInBackground(spring);
        }
    }
}
