import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Bar {
    public static final int DEFAULT_BAR_WIDTH = 45;
    public static final int DEFAULT_BAR_HEIGHT = 5;
    public static final int DEFAULT_BORDER_WIDTH = 1;
    public static final int DEFAULT_HEIGHT_ABOVE = 15;
    private static final double BAR_ACCELERATION_CONSTANT = 0.015;
    private static final double TIME_BEFORE_REGEN = 3000;
    private static final int DEFAULT_REGEN_RATE = 1;
    private Rectangle border;
    private Rectangle health;
    private Rectangle healthRemoved;
    private int curPoints;
    private double pointsRemoved;
    private int height_above;
    private int border_width;
    private int maxHP;
    private Color bar_color;
    private double barAcceleration;
    private int x,y;
    private int timer;
    private int regen_rate;
    private int objWidth;


    public Bar(int width,int height, int border_width, int height_above, int objWidth, Color bar_color,int regen_rate) {
        curPoints = width;
        border = new Rectangle(0,0,width+2*border_width,height+2*border_width);
        health = new Rectangle(0,0,width,height);
        healthRemoved = new Rectangle(0,0,0,height);
        this.border_width = border_width;
        this.height_above = height_above;
        this.objWidth = objWidth;
        maxHP = width;
        this.bar_color = bar_color;
        this.regen_rate = regen_rate;
        Timer t = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timer += 500;
                if(timer >= TIME_BEFORE_REGEN && curPoints < maxHP) //Regen if not at max and if not taken damage in TIME_BEFORE_REGEN time
                    curPoints += regen_rate;
                if(curPoints>maxHP) curPoints = maxHP; //Caps the HP
            }
        });
        t.start();
    }

    public Bar(int width,int height, int border_width, int height_above, int objWidth, Color bar_color) {
        this(width,height,border_width,height_above,objWidth,bar_color,DEFAULT_REGEN_RATE);
    }

    public Bar(int objWidth) {
        this(DEFAULT_BAR_WIDTH, DEFAULT_BAR_HEIGHT, DEFAULT_BORDER_WIDTH, DEFAULT_HEIGHT_ABOVE,objWidth,Color.RED);
    }

    public Bar(int width, int objWidth) {
        this(width, DEFAULT_BAR_HEIGHT, DEFAULT_BORDER_WIDTH, DEFAULT_HEIGHT_ABOVE,objWidth,Color.RED);
    }

    public Bar(int objWidth,Color bar_color) {
        this(DEFAULT_BAR_WIDTH, DEFAULT_BAR_HEIGHT, DEFAULT_BORDER_WIDTH, DEFAULT_HEIGHT_ABOVE,objWidth,bar_color);
    }
    public Bar(int height_above, int objWidth,Color bar_color) {
        this(DEFAULT_BAR_WIDTH, DEFAULT_BAR_HEIGHT, DEFAULT_BORDER_WIDTH, objWidth,height_above,bar_color);
    }

    public Bar(int height_above, int objWidth,Color bar_color, int regen_rate) {
        this(DEFAULT_BAR_WIDTH, DEFAULT_BAR_HEIGHT, DEFAULT_BORDER_WIDTH, height_above,objWidth,bar_color,regen_rate);
    }



    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void drawSelf(Graphics g) {

        if(pointsRemoved > 0) {
            barAcceleration += BAR_ACCELERATION_CONSTANT; //Accelerates the white bar that represents damage dealt
            pointsRemoved -= barAcceleration;
        } else {
            barAcceleration = 0;
            pointsRemoved = 0;
        }

        Graphics2D new_g = (Graphics2D) g;

        //Creates the border
        new_g.setColor(Color.BLACK);
        border.x = x + objWidth/2 - border.width/2;
        border.y = y - height_above;
        new_g.fill(border);


        new_g.setColor(bar_color);

        //Creates the actual health bar
        health.x = x + objWidth/2 - maxHP/2;
        health.y = y - height_above + border_width;
        health.width = curPoints;
        new_g.fill(health);

        //Draws the damage done bar
        new_g.setColor(Color.white);
        healthRemoved.x = x + objWidth/2 - maxHP/2 + curPoints;
        healthRemoved.y = y - height_above + border_width;
        healthRemoved.width = (int) pointsRemoved;
        new_g.fill(healthRemoved);
    }

    public void subtractPoints(int h) {
        pointsRemoved += h;
        if(pointsRemoved > curPoints) //If points removed is greater than remaining points, set it to remaining points
            pointsRemoved = curPoints;
        curPoints -= h;
        if(curPoints < 0)
            curPoints = 0;
        timer = 0;
    }

    public int getPoints() {
        return curPoints;
    }

    public void reset() { //Reset HP bar
        curPoints = maxHP;
        pointsRemoved = 0;
    }

    public int getMaxHP() {
        return maxHP;
    }
}
