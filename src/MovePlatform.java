import javax.swing.*;

public class MovePlatform extends Tile{

    private static final double ERROR_MARGIN = 0.1;
    public static final int DEFAULT_MOVE_PLATFORM_WIDTH = 48;
    public static final int DEFAULT_MOVE_PLATFORM_HEIGHT = 48;
    private double[] speed;
    private Timer t;
    private int timer;
    private MovePlatform thisPlatform = this;
    private double destX, destY;
    private double initX,initY;
    public boolean touchingPlayer;

    public MovePlatform(int initX, int initY, int width, int height, int destX, int destY, double time) {
        super(initX,initY,width,height);
        setTag("Move Platform");
        speed = new double[] {(destX-initX)/(time*100),(destY-initY)/(time*100)};
        this.initX = initX;
        this.initY = initY;
        this.destX = destX;
        this.destY = destY;
    }

    public double[] getSpeed() {
        return speed;
    }

    public MovePlatform(int initX, int initY, int destX, int destY, double time) {
        this(initX,initY,DEFAULT_WIDTH,DEFAULT_HEIGHT, destX, destY, time);
    }

    public void updateX() {
        realX += speed[0];
        x = (int) realX;
        if(Math.abs(realX - destX) < ERROR_MARGIN) { //If the x pos is close enough to the destX, reverse x
            realX = destX;
            speed[0] = -speed[0];
        }
        if(Math.abs(realX - initX) < ERROR_MARGIN) { //If the x pos is close enough to the initX, reverse x
            realX = initX;
            speed[0] = -speed[0];
        }
    }
    public void updateY() {
        realY += speed[1];
        y = (int) realY;
        if(Math.abs(realY - destY) < ERROR_MARGIN) { //If the y pos is close enough to the destY, reverse y
            realY = destY;
            speed[1] = -speed[1];
        }
        if(Math.abs(realY - initY) < ERROR_MARGIN) { //If the y pos is close enough to the initY, reverse y
            realY = initY;
            speed[1] = -speed[1];
        }
    }

    public void changeX(double x) {
        this.initX += x;
        this.destX += x;
        super.changeX(x);
    }

    public void changeY(double y) {
        this.initY += y;
        this.destY += y;
        super.changeY(y);
    }
}
