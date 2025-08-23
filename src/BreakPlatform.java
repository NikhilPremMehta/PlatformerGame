import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class BreakPlatform extends Tile{

    public static final double DEFAULT_BREAK_TIME = 0.45;
    public static final double DEFAULT_REAPPEAR_TIME = 1.5;
    public static final int DEFAULT_BREAK_PLATFORM_WIDTH = 24;
    public static final int DEFAULT_BREAK_PLATFORM_HEIGHT = 24;
    public static final int FALL_TIME = 150;

    private static final String crumble = Panel.getResourceFilePath("SoundAssets/crumble.wav");
    private static final String reappear = Panel.getResourceFilePath("SoundAssets/reappear.wav");
    private static final String shake = Panel.getResourceFilePath("SoundAssets/shake.wav");
    private static final int SHAKE_AUDIO_LENGTH = 113;

    private double break_time;
    private double reappear_time;
    private Timer t;
    private Timer shakeTimer;
    private int timer;
    public String initTag = "Break Platform";
    private static Image tile;
    private boolean activated;
    private int fallTimer;
    private double[] y_fall_speed;
    private double[] y_displacement;
    private static final AlphaComposite full = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1);
    static {
        try {
            tile = ImageIO.read(new File(Panel.getResourceFilePath("TileAssets/BreakPlatformSprites/tile.png")));
        } catch (IOException e) {
            System.out.println(e);
        }
    }


    public BreakPlatform(int initX, int initY, int width, int height, double bt, double rt) {
        super(initX,initY,width,height);
        setTag("Break Platform"); //Sets the tag
        setColor(Color.GRAY);
        break_time = bt;
        reappear_time = rt;
        setImage_height(24);
        setImage_width(24);
        y_displacement = new double[width/getImage_width()]; //Sets a value for each individual tile
        y_fall_speed = new double[width/getImage_width()]; //Sets a value for each individual tile

    }

    public BreakPlatform(int initX, int initY) {
        this(initX,initY,DEFAULT_BREAK_PLATFORM_WIDTH,DEFAULT_BREAK_PLATFORM_HEIGHT);
    }

    public BreakPlatform(int initX, int initY, int width, int height) {
        this(initX,initY,width,height,DEFAULT_BREAK_TIME,DEFAULT_REAPPEAR_TIME);
    }

    public void break_platform() {
        if(break_time != 0) {
            setTag("Platform");
            shake();
        }
        timer = 0;
        activated = true;
        t = new Timer(10,new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color c = getColor();
                if(timer == break_time * 1000) { //Set platform invisible after time
                    setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 0)); //This is unused now that there are sprites, but I just kept it in incase I needed to test
                    setTag("Invisible");
                    fallTimer = FALL_TIME;
                    activated = false;
                    if(break_time != 0) {
                        shakeTimer.stop();
                        StdAudio.playInBackground(crumble);
                    }
                }
                timer += 10;
                if(timer == break_time * 1000 + reappear_time * 1000) { //Sets platform to visible
                    reappear();
                    t.stop();
                }
            }
        });
        t.start();
    }
    public void reappear() {
        Color c = getColor();
        setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),255));
        setTag(initTag);
        setVisible(true);
        StdAudio.playInBackground(reappear);
    }

    public void drawSelf(Graphics g) {
        super.drawSelf(g);
        if(!isVisible()) {
            //Draw a white outline so that the location of the break platform is known
            g.setColor(Color.WHITE);
            ((Graphics2D) g).setStroke(new java.awt.BasicStroke(3));
            ((Graphics2D) g).draw(this);
        }
    }

    private void shake() { //Start the shake audio
        shakeTimer = new Timer(SHAKE_AUDIO_LENGTH, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StdAudio.playInBackground(shake);
            }
        });
        shakeTimer.start();
    }

    @Override
    public void drawImage(Graphics g) {
        fallTimer -= 10;
        if(fallTimer == 0) {
            setVisible(false);
            y_displacement = new double[width/getImage_width()];
            y_fall_speed = new double[width/getImage_width()];
        } else if(fallTimer > 0) {
            for(int i = 0; i<y_displacement.length;i++) { //Makes the individual tiles fall at dif rates
                y_fall_speed[i] += Math.random();
                y_displacement[i] += y_fall_speed[i];
            }
        }
        for(int i = 0;i<width/getImage_width();i++) {
            if(fallTimer >= 0) {
                AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)fallTimer / FALL_TIME);
                ((Graphics2D) g).setComposite(ac);
            }
            g.drawImage(tile, x + i*getImage_width() + ((int)(Math.random()*5)-2)*(activated?1:0), y + ((int)(Math.random()*5)-2)*(activated?1:0) + (int)y_displacement[i], getImage_width(), getImage_height(), null);
            ((Graphics2D) g).setComposite(full);
        }
    }
}

