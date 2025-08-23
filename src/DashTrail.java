import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class DashTrail {
    private static final int DASH_TRAIL_EXIST_TIME = 120;
    private Image image;
    private int x,y;
    private int timer;
    private int direction;

    public DashTrail(int x, int y,int direction) {
        try {
            File f = new File(Panel.getResourceFilePath("PlayerAssets/DashSprite/Dash.png"));
            image = ImageIO.read(f);
        } catch (IOException e) {
            System.out.println(e);
        }
        this.x = x;
        this.y = y;
        this.direction = direction;
        timer = DASH_TRAIL_EXIST_TIME;
    }

    public void drawSelf(Graphics g) {
        ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f + ((float) timer/2)/DASH_TRAIL_EXIST_TIME));
        g.drawImage(image,x-(Player.PLAYER_WIDTH- Player.HITBOX_WIDTH)/2 + (direction == -1 ? Player.PLAYER_WIDTH : 0),y-(Player.PLAYER_HEIGHT- Player.HITBOX_HEIGHT),(Player.PLAYER_WIDTH*(direction == -1 ? -1 : 1)),Player.PLAYER_HEIGHT,null); //Draw the image and flip it based on the player direction
        ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    public void decrementTimer() {
        timer -= 10;
    }

    public int getTime() {
        return timer;
    }

    public void changeX(double speed) {
        x += speed;
    }

    public void changeY(double speed) {
        y += speed;
    }
}
