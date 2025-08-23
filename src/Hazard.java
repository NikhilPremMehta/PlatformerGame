import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Hazard extends Tile{
    private static final int DEFAULT_DAMAGE = 10;
    public static final int DEFAULT_HAZARD_WIDTH = 48;
    public static final int DEFAULT_HAZARD_HEIGHT = 48;

    private static Image hazard_image;
    private static Image toxic_hazard_image;


    private Image image;
    private int damage;

    static {
        try {
            hazard_image = ImageIO.read(new File(Panel.getResourceFilePath("TileAssets/HazardsSprites/spikes.png")));
            toxic_hazard_image = ImageIO.read(new File(Panel.getResourceFilePath("TileAssets/HazardsSprites/toxicSpikes.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Hazard(int initX, int initY, int width, int height,int damage) {
        super(initX,initY,width,height);
        setTag("Hazard"); //Sets the tag to hazard
        setColor(Color.RED);
        this.damage = damage;
        setImage_height(height);
        setImage_width(DEFAULT_HAZARD_WIDTH);
        if(damage == Bar.DEFAULT_BAR_WIDTH) image = toxic_hazard_image;
        else image = hazard_image;
    }

    public Hazard(int initX, int initY, int width, int height) {
        this(initX,initY,width,height,DEFAULT_DAMAGE);
    }

    public Hazard(int initX, int initY) {
        this(initX,initY,DEFAULT_HAZARD_WIDTH,DEFAULT_HAZARD_HEIGHT,DEFAULT_DAMAGE);
    }

    public int getDamage() {
        return damage;
    }

    @Override
    public void drawImage(Graphics g) {
        for(int i = 0;i<width/getImage_width();i++) {
            g.drawImage(image, x + i*getImage_width(), y, getImage_width(), getImage_height(), null);
        }
    }
}
