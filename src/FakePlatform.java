import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class FakePlatform extends Tile{

    private Timer t;
    private Image image;
    private int alpha = 255;
    private static final AlphaComposite full = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1);
    private static String activateSound = Panel.getResourceFilePath("SoundAssets/secret_revealed.wav");

    public FakePlatform(int initX, int initY, int width, int height) {
        super(initX,initY,width,height);
        setTag("Fake Platform"); //Sets the tag
    }


    public FakePlatform(int initX, int initY) {
        this(initX,initY,DEFAULT_WIDTH,DEFAULT_HEIGHT);
    }

    public FakePlatform(int initX, int initY,String path) {
        this(initX,initY,DEFAULT_WIDTH,DEFAULT_HEIGHT);
        try {
            this.image = ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FakePlatform(int initX, int initY, int width, int height, String path) {
        this(initX,initY,path);
        setImage_height(height);
        setImage_width(width);
        this.width = width;
        this.height = height;
    }

    public void fade() { //fade the platform
        setTag("Invisible");
        StdAudio.playInBackground(activateSound);
        t = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color c = getColor();
                if(alpha <= 5) {
                    setVisible(false);
                    t.stop();
                }
                alpha -= 5;
            }
        });
        t.start();
    }

    @Override
    public void drawImage(Graphics g) {
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)alpha/255); //Fades the platform
        ((Graphics2D) g).setComposite(ac);
        if(image == null) {
            super.drawImage(g);
        } else{
            g.drawImage(image,x,y,width,height,null);
        }
        ((Graphics2D) g).setComposite(full);
    }
}
