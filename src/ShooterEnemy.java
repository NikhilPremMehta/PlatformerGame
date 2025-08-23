import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;

public class ShooterEnemy extends Enemy {

    public static final int SHOOT_DELAY = 1000;
    private static final int BULLET_SPEED = 5;
    private static final int DELAY_AFTER_SIGHT = 200;
    private int shootTimer;
    private ArrayList<Projectile> projectiles;
    private Timer t;
    private int sightDelayTimer;
    private ShooterEnemy self = this;
    private boolean dead;


    public ShooterEnemy(int initX,int destX,int y,int time, ArrayList<Projectile> projectiles,int health,boolean damageOnCrit) {
        super(initX,destX,y,time,health,damageOnCrit);
        setEnemyType("Shooter");
        setTag("Enemy");
        this.projectiles = projectiles;
    }

    public void shoot(ArrayList<Tile> tiles, Player player) {
        if(shootTimer <= 0) {
            Line2D sightLine = new Line2D.Double(x+ENEMY_WIDTH/2,y+ENEMY_HEIGHT/2,player.x+Player.HITBOX_WIDTH/2, player.y + Player.HITBOX_WIDTH/2);
            for(Tile t : tiles) { //Check if the player is in line of sight
                if(sightLine.intersects(t) && t != this)
                    return; //Don't shoot if theres a Tile in the way
            }
            sightDelayTimer = 0; //Add a slight delay to the shot
            shootTimer = SHOOT_DELAY;
            t = new Timer(10, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sightDelayTimer += 10;
                    if(sightDelayTimer >= DELAY_AFTER_SIGHT && !dead) { //Shoot the bullet after short delay if not dead
                        Projectile p = new Projectile(x+ ENEMY_WIDTH /2,y+ ENEMY_HEIGHT /2,player.x+Player.HITBOX_WIDTH/2,player.y + Player.HITBOX_HEIGHT/2,BULLET_SPEED,self);
                        projectiles.add(p); //Add the projectile to this list so that it can be updated in physics updater
                        t.stop();
                    }
                }
            });
            t.start();
        }
    }

    public void die() {
        shootTimer = Integer.MAX_VALUE;
        super.die();
        dead = true;
    }

    public void decrementTimer() {
        shootTimer -= 10;
    }

}
