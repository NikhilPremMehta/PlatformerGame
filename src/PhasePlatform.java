import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class PhasePlatform extends Tile{

    private Timer t;
    public static final int DEFAULT_PHASE_PLATFORM_WIDTH = 48*3;
    public static final int DEFAULT_PHASE_PLATFORM_HEIGHT = 24;
    private static Image left, middle, right;
    static {
        try {
            left = ImageIO.read(new File(Panel.getResourceFilePath("TileAssets/PhasePlatformSprites/wood_slab_left.png")));
            middle = ImageIO.read(new File(Panel.getResourceFilePath("TileAssets/PhasePlatformSprites/wood_slab_middle.png")));
            right = ImageIO.read(new File(Panel.getResourceFilePath("TileAssets/PhasePlatformSprites/wood_slab_right.png")));
        } catch(IOException e) {
            System.out.println(e);
        }
    }


    public PhasePlatform(int initX, int initY, int width, int height) {
        super(initX,initY,width,height);
        setTag("Phase Platform"); //Sets the tag
        setColor(new Color(153,76,0));
        setImage_height(24);
        setImage_width(48);
    }

    public PhasePlatform(int initX, int initY) {
        this(initX,initY,DEFAULT_PHASE_PLATFORM_WIDTH,DEFAULT_PHASE_PLATFORM_HEIGHT);
    }

    public void allowPhase(Player player) { //Wait until not intersecting this for it to become solid again
        PhasePlatform p = this;
        setTag("Invisible");
        t = new Timer(10,new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!p.intersects(player)) {
                    t.stop();
                    setTag("Phase Platform");
                }
            }
        });
        t.start();
    }

    @Override
    public void drawImage(Graphics g) {
        Image image;
        for(int i = 0;i<width/getImage_width();i++) {
            if((i > 0 && i < width/getImage_width()-1) || width/getImage_width() == 1) image = middle;
            else if(i == 0) image = left;
            else image = right;
            g.drawImage(image, x + i*getImage_width(), y, getImage_width(), getImage_height(), null);
        }
    }
}
