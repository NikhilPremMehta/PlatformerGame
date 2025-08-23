import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Border {

    private ArrayList<Tile> tiles;
    private static final int size = 27;

    public Border(boolean top, boolean right, boolean left, boolean bottom) {
        tiles = new ArrayList<>();
        //Creates the Tiles for each border if needed
        if(top) tiles.add(new Tile(0,0,Panel.PANEL_WIDTH,size) {
            private Image image;
            {
                try {
                    image = ImageIO.read(new File(Panel.getResourceFilePath("TileAssets/Borders/borderTop.png")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void drawImage(Graphics g) {
                g.drawImage(image,x,y,width,height,null);
            }
        });

        if(bottom) tiles.add(new Tile(0,Panel.PANEL_HEIGHT-size,Panel.PANEL_WIDTH,size) {
            private Image image;
            {
                try {
                    image = ImageIO.read(new File(Panel.getResourceFilePath("TileAssets/Borders/borderBottom.png")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void drawImage(Graphics g) {
                g.drawImage(image,x,y,width,height,null);
            }
        });

        if(left) tiles.add(new Tile(0,0,size,Panel.PANEL_HEIGHT) {
            private Image image;
            {
                try {
                    image = ImageIO.read(new File(Panel.getResourceFilePath("TileAssets/Borders/borderLeft.png")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void drawImage(Graphics g) {
                g.drawImage(image,x,y,width,height,null);
            }
        });

        if(right) tiles.add(new Tile(Panel.PANEL_WIDTH-size,0,size,Panel.PANEL_HEIGHT) {
            private Image image;
            {
                try {
                    image = ImageIO.read(new File( Panel.getResourceFilePath("TileAssets/Borders/borderRight.png")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void drawImage(Graphics g) {
                g.drawImage(image,x,y,width,height,null);
            }
        });
    }

    public void addTiles(ArrayList<Tile> t) { //Adds the borders to the Tile arraylist
        for(Tile tile: tiles) {
            t.add(tile);
        }
    }
}
