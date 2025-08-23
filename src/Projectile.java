import java.awt.*;
import java.awt.geom.Ellipse2D;

public class Projectile extends Ellipse2D.Double {

    private static final int BULLET_WIDTH = 12;
    private static final int BULLET_HEIGHT = 12;
    private static final int BULLET_DAMAGE = 12;
    public static final int DEFAULT_SPEED = 5;
    double speedX;
    double speedY;
    private double realX, realY;
    private int damage;
    private Object shooter;

    public Projectile(int x, int y, int targetX, int targetY, int speed,Object shooter) {
        super(x,y,BULLET_WIDTH,BULLET_HEIGHT);
        double angle = Math.atan((double)(targetY - y)/(targetX - x)) + (targetX < x ? Math.PI : 0); //Calc angle that needs to be fired at
        speedX = speed*Math.cos(angle); //Calc x speed
        speedY = speed*Math.sin(angle); //Calc y speed
        realX = x;
        realY = y;
        damage = BULLET_DAMAGE;
        this.shooter = shooter; //Used so that the bullet doesn't hit itself
    }

    public void update() {
        this.realX += speedX;
        this.realY += speedY;
        this.x = (int) realX;
        this.y = (int) realY;
    }


    public int getDamage() {
        return damage;
    }

    public double[] getSpeed() {
        return new double[] {speedX,speedY};
    }

    public void drawSelf(Graphics g) {
        Graphics2D new_g = (Graphics2D) g;
        new_g.setColor(Color.RED);
        new_g.fill(this);
    }

    public Object getShooter() {
        return shooter;
    }
}
