import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Coin extends Tile implements Activatable{

    public static final int DEFAULT_COIN_HEIGHT = 24;
    public static final int DEFAULT_COIN_WIDTH = 24;
    private static final int ANIMATION_FRAME_RATE = 100;
    private static Image[] idle, pickedUp;

    private static final String pickUpSound = Panel.getResourceFilePath("SoundAssets/coinPickup.wav");

    static {
        idle = Panel.loadAnimation(Panel.getResourceFilePath("TileAssets/CoinSprites/CoinIdle"));
        pickedUp = Panel.loadAnimation(Panel.getResourceFilePath("TileAssets/CoinSprites/CoinPickup"));
    }
    private Image[] cur_cycle;
    private int curFrame;

    public Coin(int initX, int initY, int width, int height) {
        super(initX,initY,width,height);
        setTag("Coin"); //Sets the tag
        setColor(Color.ORANGE);
        cur_cycle = idle;
        Timer t = new Timer(ANIMATION_FRAME_RATE, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!(cur_cycle == pickedUp && curFrame == pickedUp.length-1)) {
                    curFrame = (curFrame + 1) % cur_cycle.length; //Go to next frame
                } else {
                    setVisible(false);
                }
            }
        });
        t.start();
    }

    public Coin(int initX,int initY) {
        this(initX,initY,DEFAULT_COIN_WIDTH,DEFAULT_COIN_HEIGHT);
    }

    public void drawImage(Graphics g) {
        g.drawImage(cur_cycle[curFrame],x,y,width,height,null);
    }

    public void activate() { //Set animation and sound to activated and increases the coin count
        setTag("Invisible");
        //setColor(new Color(getColor().getRed(),getColor().getGreen(),getColor().getBlue(),0));
        Panel.coins++; //Increases coin count
        cur_cycle = pickedUp;
        curFrame = 0;
        //This change in y and doubling in height is because the pickedup animation actually has a different size than the idle
        y -= height;
        height *= 2;
        StdAudio.playInBackground(pickUpSound); //Play sound
    }

    public void reset() { //Used to reset the coin if the player dies before touching the ground
        height = DEFAULT_COIN_HEIGHT;
        y += height;
        cur_cycle = idle;
        curFrame = 0;
        setTag("Coin");
        setVisible(true);
        Panel.coins--;
    }
}
