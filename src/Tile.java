import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;

public class Tile extends Rectangle {

    public static final int DEFAULT_WIDTH = 96;
    public static final int DEFAULT_HEIGHT = 96;
    private String tag;
    private Color color = Color.BLACK;
    public double realX, realY;
    private int image_width, image_height;
    private String tileImageString = "";
    private int[] sides;
    private String[][] customImages;
    public static HashMap<String,Image> images = new HashMap<>();
    private boolean visible = true;

    static {
        for(File f : new File(Panel.getResourceFilePath("TileAssets/TileSprites")).listFiles()) {
            if(!f.getName().equals(".DS_Store")) {
                try {
                    images.put(f.getName(), ImageIO.read(f));
                } catch(IOException e) {
                    System.out.println(e);
                }
            }
        }

    }

    public int getImage_width() {
        return image_width;
    }

    public int getImage_height() {
        return image_height;
    }

    public void setImage_width(int image_width) {
        this.image_width = image_width;
    }

    public void setImage_height(int image_height) {
        this.image_height = image_height;
    }

    public Tile(int initX, int initY, int width, int height) {
        super(initX, initY, width, height);
        this.tag = "Platform";
        realX = initX;
        realY = initY;
        image_width = 48;
        image_height = 48;
    }

    public Tile(int initX, int initY, int width, int height, String[][] images) {
        this(initX,initY,width,height);
        customImages = images;
    }

    public Tile(int initX, int initY) {
        this(initX, initY, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public void drawSelf(Graphics g) {
        if(visible) {
            Graphics2D new_g = (Graphics2D) g;
            new_g.setColor(color);
            drawImage(g);
            //((Graphics2D) g).draw(this);
        }
    }

    public String getTag() {
        return tag;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setTag(String t) {
        tag = t;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void changeX(double x) {
        this.realX += x;
        this.x = (int) realX;
    }

    public void changeY(double y) {
        this.realY += y;
        this.y = (int) realY;
    }


    public void drawImage(Graphics g) {
        for (int i = 0; i < width / image_width; i++) {
            for (int j = 0; j < height / image_height; j++) {
                if(customImages == null) {
                    //Checks if each side should be covered of the grass
                    sides = new int[4];
                    if (j == 0)
                        sides[0] = 1;

                    if (i == (width / image_width) - 1)
                        sides[1] = 1;

                    if (j == (height / image_height) - 1)
                        sides[2] = 1;

                    if (i == 0)
                        sides[3] = 1;
                    g.drawImage(images.get("" + sides[0] + sides[1] + sides[2] + sides[3] + ".png"), x + i * image_width, y + j * image_height, image_width, image_height, null); //Draws the image based on a pre-generated hashmap
                } else {
                    g.drawImage(images.get(customImages[j][i] + ".png"), x + i * image_width, y + j * image_height, image_width, image_height, null);
                }
            }
        }
    }
}
