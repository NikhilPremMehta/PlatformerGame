import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.*;

//Make damage be taken when level is moving
//Add enemy damage animation for fireball
//Make checkpoint spell only being able to used on normal platforms
//Make coins only be gained after touching a platform
//Add another crumble platform level
//Add another dash orb level
//Add another enemy level (Boss level?)
//Add a secret level/s with the coin
//Fix shooter enemy
//Platforms you cant wall jump on
//Fix phase platforms on a level change
//Make last coin possible to get
//Die on the corner of blocks when on move platform
//If checkpoint on the wall, you die endlessly

public class PhysicsUpdater {
    private static final double GRAVITY = 0.55;
    private static final double FRICTION = 0.93;
    private static final int JUMP_HEIGHT = 10;
    private static final double WALL_JUMP_HEIGHT = 8;
    private static final double MAX_SPEED = 3.5;
    private static final double X_ACCEL = 0.55; //Making X_ACCEL a fraction will limit how often the speed accelerates
    private static final double INCREASE_FRICTION_THRESHOLD = 3; //The speed at which below to increase friction
    private static final double COYOTE_JUMP_TIME = 150; //Time after leaving the ground that you can still jump
    private static final double WALL_JUMP_HORIZONTAL_HEIGHT = 10;
    private static final double WALL_JUMP_NO_CONTROL_TIME = 200; //Time of no control after a wall jump
    private static final double HALT_TIME = 50; //Time at a wall jump before actually doing the wall jump
    private static final double WALL_SLIDE_VELOCITY = 1; //Velocity when clinging to a wall
    private static final double WALL_JUMP_COYOTE_TIME = 200;
    private static final int TELEPORT_DEATH_DISTANCE = 28; //Amount of position change needed to consider being crushed (therefore activating a player death)
    private static final double PEAK_JUMP_SPEED_DECREASE = 0.3; //Used to make the jump a bit more floaty
    private static final double PEAK_JUMP_SPEED_VALUE = 6; //Used to make the jump a bit more floaty
    private static final double DASH_SPEED = 11;
    private static final int DASH_NO_CONTROL_TIME = 90;
    private static final double DASH_DAMAGE_MULTIPLIER = 3;
    private static final int DASH_COOLDOWN = 250;
    private static final double Y_MAX_SPEED = 7; //Max Y vel
    private static final int RESPAWN_SCENE_CHANGE_TIMER = 700;

    private static final String checkpointRespawn = Panel.getResourceFilePath("SoundAssets/revive.wav");
    private static final String landSound = Panel.getResourceFilePath("SoundAssets/land.wav");
    private static final String deathSound = Panel.getResourceFilePath("SoundAssets/death.wav");


    private Player player;
    private Level level;
    private Panel panel;
    private ArrayList<Projectile> projectiles;
    private double[] cur_speed; //Index 0 is horizontal velocity, index 1 is vertical
    private boolean touch_ground; //Is used to determine if can jump
    private double coyote_jump_timer;
    private boolean wall_jumped;
    private double no_control_timer;
    private double wall_jump_coyote_timer;
    private int wall_jump_direction;
    private boolean firstWallJump = true;
    public boolean left, right, up, down, bounceUp, dash; //Changes based on if the keys are pressed, bounceUp is specifically for bounce pads
    private boolean intersected_wall; //Is used to determine if the character horizontally intersects with anything
    private boolean intersected_floor; //Is used to determine if the character vertically intersects with anything
    private String tag; //Stores the tag of what is intersected
    private int damage;
    public int changeSceneTimer;
    private Tile intersectedTile;
    private boolean respawn; //Used so that physics is disabled on death for the animation
    private int[] lastPos; //Used to check if player is crushed
    private int dashNoControlTimer;
    private int dashCooldownTimer;
    private boolean canDash = true;
    private boolean dashed;
    private ArrayList<DashTrail> trails;
    private ArrayList<Coin> pickedUp; //Used so that if a player dies with a coin before hitting a platform, they lose the coin
    private int respawnTimer;



    public PhysicsUpdater(Player p, Level l,Panel panel,ArrayList<Projectile> projectiles,ArrayList<DashTrail> d) {
        this.projectiles = projectiles;
        trails = d;
        tag = "";
        player = p;
        level = l;
        this.panel = panel;
        cur_speed = new double[] {0,0}; //Speed is a double despite coordinates being an int so that I can make it longer between acceleration updates without changing the timer tick speed
        lastPos = new int[] {player.x,player.y};
        respawnTimer = -100000;


        Timer timer = new Timer(10, new ActionListener() { //Timer which repeatedly does the physics of the game
            @Override
            public void actionPerformed(ActionEvent e) {
                tag = "";

                if(cur_speed[1] > 15) cur_speed[1] = 15;

                for(int i = 0;i<trails.size();i++) { //Lowers the lifetime for each trail
                    trails.get(i).decrementTimer();
                    if(trails.get(i).getTime() <= 0)
                        trails.remove(trails.get(i));
                }

                //Decrement timers
                coyote_jump_timer -= 10;
                no_control_timer -= 10;
                wall_jump_coyote_timer -= 10;
                dashNoControlTimer -= 10;
                dashCooldownTimer -= 10;
                respawnTimer -= 10;

                if (respawnTimer == 0) {
                    level.changeScene(level.getCheckpoint().getCheckPointScene()); //Go to checkpoint scene
                    respawn = true;
                } else if (respawnTimer == -200) StdAudio.playInBackground(checkpointRespawn);

                if(dashNoControlTimer <= 0 && dashed) {
                    cur_speed[1] *= 0.5;
                    if(cur_speed[1] > 0) cur_speed[1] *= 0.5;
                    dashed = false;
                    if(Math.abs(cur_speed[0]) > MAX_SPEED) cur_speed[0] = MAX_SPEED * (cur_speed[0] > 0 ? 1 : -1);
                    dashCooldownTimer = DASH_COOLDOWN;
                    trails.add(new DashTrail(player.x,player.y,player.getDirection()));
                }


                if(level.changing) { //If level is changing, move the y
                    changeSceneTimer += 10;
                    for(Tile s : level.getObjects()) {
                        s.changeY(-level.getSpeed()[1]);
                    }
                    for(DashTrail d : trails) {
                        d.changeY(-level.getSpeed()[1]);
                    }
                    player.changeY(-level.getSpeed()[1]);
                    lastPos[1] += -level.getSpeed()[1];
                    if(cur_speed[0] == 0) {
                        cur_speed[0] = 0.01 * (-level.getSpeed()[0] < 0 ? 1 : -1);
                    }
                }

                if(changeSceneTimer >= level.CHANGE_TIME*1000) { //If change time has passed, stop changing the scene
                    level.changing = false;
                    changeSceneTimer = 0;
                }

                if(respawn && !level.changing && respawnTimer < 0) { //If respawned and the level isn't changing, then revive
                    respawn = false;
                    player.getHp().reset();
                    cur_speed = new double[] {0,0};
                    player.revive();
                    //Sets x & y to checkpoint values
                    player.setX(level.getCheckpoint().getCheckPointCoords()[0]);
                    player.setY(level.getCheckpoint().getCheckPointCoords()[1]);
                    lastPos[0] = player.x;
                    lastPos[1] = player.y;

                }


                for(int i = projectiles.size()-1;i>=0;i--) { //Moves all projectiles and checks collisions
                    Projectile p = projectiles.get(i);
                    p.update(); //update projectile pos
                    if(p.intersects(player) && p.getShooter() != player) { //If touching player, damage player
                        player.getHp().subtractPoints(p.getDamage());
                        player.damage();
                        //Sets the player speed to the projectile speed to show momentum
                        cur_speed[0] = p.getSpeed()[0];
                        cur_speed[1] = p.getSpeed()[1] * 2; //The *2 is just to make a bigger change in the y direction as otherwise it is very minimal
                        no_control_timer = 200;
                        projectiles.remove(p);
                        //break;
                    }
                    for(Tile t: level.getObjects()) {
                        if(p.intersects(t) && t != p.getShooter() && !t.getTag().equals("Invisible")) { //If touching a gameObject, remove the projectile
                            projectiles.remove(p);
                            if(t.getTag().equals("Enemy")) { //If its an enemy, deal damage to them
                                ((Enemy) t).getHp().subtractPoints(p.getDamage());
                                ((Enemy) t).damage();
                            }
                            break;
                        }
                    }
                }

                //Make physics in the y direction work
                if(!respawn) {
                    if(dashNoControlTimer <= 0) {
                        if (intersected_wall && cur_speed[1] > 0 && (right || left)) //If clinging to wall, set y velocity to the constant
                            cur_speed[1] = WALL_SLIDE_VELOCITY;
                        else
                            cur_speed[1] += GRAVITY - (Math.abs(cur_speed[1]) < PEAK_JUMP_SPEED_VALUE ? PEAK_JUMP_SPEED_DECREASE : 0); //Put the character downwards by gravity
                    }
                } else {cur_speed[1] = 0;}
                intersected_wall = false;
                if(up) up(); //Jump if possible
                if(wall_jumped) cur_speed[1] = 0;
                if(!respawn)
                    player.changeY(cur_speed[1]); //Change the actual y pos by the speed
                for(Tile t : level.getObjects()) {
                    if(player.intersects(t) && !t.getTag().equals("Move Platform") && !(t.getTag().equals("Invisible") || t.getTag().equals("Checkpoint") || t.getTag().equals("Key") || t.getTag().equals("Coin") || t.getTag().equals("Spell Orb"))) { //If touching an object that is solid, then the moving platforms no longer affect the players movement
                        for (Tile tile : level.getObjects()) {
                            if (tile.getTag().equals("Move Platform"))
                                ((MovePlatform) tile).touchingPlayer = false; // Moving platform no longer influences player position
                        }
                    }
                }
                for(Tile t: level.getObjects()) { //Check if the player intersects with any objects

                    if(t.getTag().equals("Enemy")) { //For every enemy check if dead, and shoot if possible
                        if (((Enemy) t).getHp().getPoints() <= 0)
                            ((Enemy) t).die();
                        if (((Enemy) t).getEnemyType().equals("Shooter")) {
                            ((ShooterEnemy) t).shoot(level.getObjects(), player);
                            ((ShooterEnemy) t).decrementTimer(); //Timer is used as a cooldown to the shooting
                        }
                    }

                        if (t.getTag().equals("Move Platform")) { //Move moving platform in the y position
                            double platformSpeed = ((MovePlatform) t).getSpeed()[1];
                            ((MovePlatform) t).updateY();
                            if (player.intersects(t) && !respawn) //If touching player
                                ((MovePlatform) t).touchingPlayer = true;
                            else if (Math.abs((int) cur_speed[1]) >= 2 || respawn || dashed) { //If not touching player, and the speed is enough to have actually moved the player
                                ((MovePlatform) t).touchingPlayer = false;
                            }
                            if (((MovePlatform) t).touchingPlayer) { //Move player if touching platform
                                if (((MovePlatform) t).getSpeed()[1] > 0 && !respawn) {
                                    player.changeY(platformSpeed);
                                    if (cur_speed[1] == 0) //If y speed of player is 0, set it to slightly downwards so that physics updater knows to push the player on top of the platform
                                        cur_speed[1] = 0.01 * (player.y < t.y ? 1 : -1);
                                }
                            }
                            if (t.y < player.y) { //If you are touching the bottom of the moving platform. your x isnt affected
                                ((MovePlatform) t).touchingPlayer = false;
                            }
                        }

                    if(t.getTag().equals("Door")) ((Door) t).open();

                    while(player.intersects(t) && !respawn) { //Move the player up/down to just out of the intersection if player is alive
                        if(t.getTag().equals("Phase Platform") && (player.y>t.y || (down && touch_ground))) { //If the player is jumping up or pressing down and touching the top
                            ((PhasePlatform) t).allowPhase(player);
                            break;
                        }
                        if(t.getTag().equals("Key") || t.getTag().equals("Coin") || t.getTag().equals("Spell Orb") || t.getTag().equals("Dash Orb")) { //Activates anything that can be activated on intersection
                            if(t.getTag().equals("Dash Orb")) {
                                canDash = true;
                                dashCooldownTimer = 0;
                            }
                            if(t.getTag().equals("Coin"))
                                pickedUp.add((Coin) t);
                            ((Activatable) t).activate();
                            break;
                        }
                        if(t.getTag().equals("Fake Platform")) {
                            ((FakePlatform) t).fade();
                            break;
                        }
                        if(t.getTag().equals("Invisible")) break;

                        if(t.getTag().equals("Checkpoint")) { //sets checkpoint if intersects
                            level.setCheckpoint((Checkpoint) t);
                            ((Checkpoint) t).activate();
                            for(Tile tiles : level.getObjects()) {
                                if(tiles.getTag().equals("Checkpoint") && tiles != t) {
                                    ((Checkpoint) tiles).deactivate();
                                }
                            }
                            break;
                        }

                        tag = t.getTag(); //Sets the stored value to the intersection tag
                        intersectedTile = t;
                        if(tag.equals("Break Platform") && cur_speed[1] > 0)
                            ((BreakPlatform) t).break_platform();
                        if(tag.equals("Hazard")) {
                            damage = ((Hazard) t).getDamage();
                        }
                        player.changeY(cur_speed[1] >= 0 ? -1 : 1); //Pushes up if cur speed is downwards and vice versa
                        intersected_floor = true; //Says that the ground/ceiling was touched so that speed and other variables can be modified later
                    }
                }
                if(intersected_floor) { //If ground/ceiling touched

                    wall_jump_coyote_timer = 0;
                    if(tag.equals("Hazard")) { //Damages player if the player hits a hazard
                        if(cur_speed[1] > 0 && !level.changing) {
                            cur_speed[1] = -(7+damage/5);
                        }
                    }
                    else if(intersectedTile.getTag().equals("Enemy")) { //Damages enemies if player stomped on them
                        if(!((Enemy) intersectedTile).isDamageByCrit() || dashed) { //Damages the enemy and increases the damage if the player was dashing and only damages the enemy without a dash if thats not an atrribute
                            ((Enemy) intersectedTile).getHp().subtractPoints((int)(Player.STOMP_DAMAGE * (dashed ? DASH_DAMAGE_MULTIPLIER : 1)));
                        }
                        ((Enemy) intersectedTile).squash();
                        canDash = true;
                        cur_speed[1] = -Enemy.HEIGHT_ON_JUMP; //Pushes the player up
                    }

                    else if(cur_speed[1] > 0) { //If the speed was downwards, then the player is now touching the ground
                        //if(touch_ground == false && cur_speed[1] > 3) StdAudio.playInBackground(landSound);
                        touch_ground = true;
                        pickedUp = new ArrayList<>();
                        canDash = true;
                        coyote_jump_timer = COYOTE_JUMP_TIME;
                        firstWallJump = true;
                    }

                    dashNoControlTimer = 0;
                    dashed = false;


                    if(tag.equals("Bounce Pad") && touch_ground) { //Checks if you are on a bounce pad's top
                        cur_speed[1] = -BouncePad.BOUNCE_HEIGHT - (bounceUp ? BouncePad.EXTRA_JUMP_HEIGHT : 0); //Boosts the player upwards, and boosts them even further if they are holding down jump
                        ((BouncePad) intersectedTile).bounce();
                        coyote_jump_timer = 0;
                        touch_ground = false;
                        up = false;
                    }
                    else if(!tag.equals("Hazard") && !tag.equals("Enemy"))
                        cur_speed[1] = 0; //If you aren't on a bounce pad, if you touch the ground/ceiling the y speed should become 0
                    intersected_floor = false; //Sets this to false so that it can check for floor/ceiling intersection next loop
                } else if(Math.abs(cur_speed[1]) >= 1) {
                    touch_ground = false; //If you didn't intersect with the floor, you can't jump
                }


                //Make physics in the x direction work
                if(dash) dash();
                if(left) left();
                if(right) right();

                if(!respawn) {
                    player.changeX(cur_speed[0]); //Updates x position by x speed
                }

                if(level.changing) { //Changes everything x if the scene is changing
                    for(Tile s : level.getObjects()) {
                        s.changeX(-level.getSpeed()[0]);
                    }
                    for(DashTrail d : trails) {
                        d.changeX(-level.getSpeed()[0]);
                    }
                    player.changeX(-level.getSpeed()[0]);
                    lastPos[0] += -level.getSpeed()[0];
                }

                //Changes scene if on the borders
                if(player.x >= Panel.PANEL_WIDTH+1) {
                    level.changeScene(new int[] {level.getCurrentScene()[0],level.getCurrentScene()[1] + 1});
                }
                if(player.x <= -1) {
                    level.changeScene(new int[] {level.getCurrentScene()[0],level.getCurrentScene()[1] - 1});
                }
                if(player.y >= Panel.PANEL_HEIGHT+1) {
                    level.changeScene(new int[] {level.getCurrentScene()[0] + 1,level.getCurrentScene()[1]});
                }
                if(player.y <= -1 && cur_speed[1] <= -5.5) { //The speed requirement is so that you dont barely touch the top of the screen and fall back down
                    level.changeScene(new int[] {level.getCurrentScene()[0] - 1,level.getCurrentScene()[1]});
                }

                for(Tile t : level.getObjects()) {
                    if(player.intersects(t) && !t.getTag().equals("Move Platform") && !(t.getTag().equals("Invisible") || t.getTag().equals("Checkpoint") || t.getTag().equals("Key") || t.getTag().equals("Coin") || t.getTag().equals("Spell Orb"))) { //If touching an object that is solid, then the moving platforms no longer affect the players movement
                        for (Tile tile : level.getObjects()) {
                            if (tile.getTag().equals("Move Platform"))
                                ((MovePlatform) tile).touchingPlayer = false; // Moving platform no longer influences player position
                        }
                    }
                }

                for(Tile t: level.getObjects()) { //Checks if player intersects with anything

                    if(t.getTag().equals("Move Platform") || t.getTag().equals("Enemy")) { //Moves player in the x if touching moving platform
                        double platformSpeed = ((MovePlatform) t).getSpeed()[0];
                        ((MovePlatform) t).updateX();
                        if(t.getTag().equals("Move Platform") && ((MovePlatform) t).touchingPlayer) {
                            player.changeX(platformSpeed);
                        }
                        if(player.intersects(t) && cur_speed[0] == 0)
                            cur_speed[0] = -0.01 * (platformSpeed > 0 ? 1 : -1); //If speed is 0 and touching the moving platform, make the speed so that in the real collision check, the player gets pushed the right way

                    }

                    while(player.intersects(t) & !respawn) {
                        if(t.getTag().equals("Key") || t.getTag().equals("Coin") || t.getTag().equals("Spell Orb") || t.getTag().equals("Dash Orb")) {
                            if(t.getTag().equals("Dash Orb")) {
                                canDash = true;
                                dashCooldownTimer = 0;
                            }
                            if(t.getTag().equals("Coin"))
                                pickedUp.add((Coin) t);
                            ((Activatable) t).activate();
                            break;
                        }
                        if (t.getTag().equals("Fake Platform")) {
                            ((FakePlatform) t).fade();
                            break;
                        }
                        if(t.getTag().equals("Invisible")) break;

                        if(t.getTag().equals("Checkpoint")) {
                            level.setCheckpoint((Checkpoint)t);
                            break;
                        }

                        tag = t.getTag(); //Gets the stored value to the intersection tag
                        if(tag.equals("Hazard")) {
                            damage = ((Hazard) t).getDamage();
                        }
                        if(tag.equals("Enemy")) {
                            damage = Enemy.DAMAGE;
                        }
                        if(tag.equals("Break Platform"))
                            ((BreakPlatform) t).break_platform();
                        player.changeX(cur_speed[0] >= 0 ? -1 : 1); //Moves the player right/left depending on which way they are moving
                        intersected_wall = true;
                    }
                }
                if(!tag.equals("Hazard") && !tag.equals("Enemy") && no_control_timer <= WALL_JUMP_NO_CONTROL_TIME && wall_jumped) wallJump(); //If possible to wall jump, then do so
                if(intersected_wall) {
                    if(dashNoControlTimer >= DASH_NO_CONTROL_TIME-10) {
                        canDash = true;
                        cur_speed[1] = WALL_SLIDE_VELOCITY;
                        player.setY(lastPos[1]);
                    } else if (dashNoControlTimer > 0) {
                        cur_speed[1] /= 1.5;
                    }
                    dashNoControlTimer = 0;
                    dashed = false;
                    if((tag.equals("Hazard") || tag.equals("Enemy"))) { //Pushes player if the player hits a spike/lava
                        cur_speed[0] = (cur_speed[0] > 0 ? -1 : 1) * (5 + damage/5);
                        no_control_timer = 300;
                    } else if(cur_speed[1] >= -3) { //If not damaged and running into a wall, set the wall jump coyote timer and store the direction to jump to
                        wall_jump_coyote_timer = WALL_JUMP_COYOTE_TIME;
                        wall_jump_direction = (cur_speed[0] > 0 ? -1 : 1);
                    }
                    if(!tag.equals("Hazard") && !tag.equals("Enemy")) {
                        cur_speed[0] = 0; //Makes the x speed halt if you hit a wall
                    }
                }

                if(wall_jump_coyote_timer > 0 && up && !touch_ground && !wall_jumped && !tag.equals("Hazard") && !dashed/*&& Math.abs(cur_speed[0])>WALL_JUMP_SPEED_THRESHOLD*/) { //If wall jumping, then do so
                    no_control_timer = WALL_JUMP_NO_CONTROL_TIME;
                    if(!firstWallJump) no_control_timer += HALT_TIME; //Add a time for the player to halt before wall jumping if this isn't the first wall jump
                    wall_jumped = true;
                }

                if(cur_speed[0] != 0 && dashNoControlTimer <= 0) //If there is movement, apply friction
                    cur_speed[0] *= FRICTION * (Math.abs(cur_speed[0]) < INCREASE_FRICTION_THRESHOLD ? FRICTION : 1) ; //Applies friction, and increases it if the speed is low to make it feel a little less slippery
                if(Math.abs(cur_speed[0]) < 0.4) //Makes speed 0 if it's very small
                    cur_speed[0] = 0;
                else if(no_control_timer <= 0 && dashNoControlTimer <= 0 && (left || right)) { //Sets direction based on speed if it is possible to control the character so that the character faces the right way
                    player.setDirection((cur_speed[0] > 0 ? 1 : -1));
                }
                //Damage
                if(damage != 0) {
                    player.getHp().subtractPoints(damage);
                    player.damage();
                    damage = 0;
                }

                //Animations
                if(cur_speed[1] >= 1) //If falling, play fall animation
                    player.fall();
                else if(cur_speed[1] <= -1) //If jumping, play jump animation
                    player.jump();
                else if(Math.abs(cur_speed[0]) >= 1 && (left || right)) //If moving and pressing left or right, play run animation
                    player.run();
                else
                    player.idle(); //Default to idle animation

                if(!respawn && Math.sqrt(Math.pow(lastPos[0] - player.x,2) + Math.pow(lastPos[1] - player.y,2)) >= TELEPORT_DEATH_DISTANCE && Math.sqrt(Math.pow(cur_speed[0],2) + Math.pow(cur_speed[1],2)) < TELEPORT_DEATH_DISTANCE) { //If the player has moved more distance than the constant, kill them as they have been crushed (Theres no other way for them to move that fast)
                    player.getHp().subtractPoints(player.getHp().getPoints()); //Kill the player
                    //Puts the player back before they teleported to make the death animation go in the correct spot
                    player.setX(lastPos[0]);
                    player.setY(lastPos[1]);
                }
                lastPos = new int[] {player.x,player.y};

                if(player.getHp().getPoints() <= 0 && !respawn && respawnTimer < 0) { //If dead and not already respawning, respawn
                    if(!level.changing || damage >= player.getHp().getPoints()) {
                        player.death(); //Play death animation
                        respawnTimer = RESPAWN_SCENE_CHANGE_TIMER;
                        for(Coin c : pickedUp) {
                            c.reset();
                        }
                        StdAudio.playInBackground(deathSound);
                        respawn = true;
                    }
                }

                if(dashNoControlTimer == DASH_NO_CONTROL_TIME-10) {
                    player.dash();
                }

                if(dashed) {
                    if(dashNoControlTimer % 20 == 0 || dashNoControlTimer == DASH_NO_CONTROL_TIME - 10)
                        trails.add(new DashTrail(player.x,player.y,player.getDirection()));
                }
                if(cur_speed[1] > Y_MAX_SPEED && dashNoControlTimer <= 0) cur_speed[1] = Y_MAX_SPEED;


                panel.repaint(); //Update frame
            }
        });
        timer.start();
    }

    public void up() {
        if(coyote_jump_timer >0) { //If touching ground jump
            up = false;
            cur_speed[1] = -JUMP_HEIGHT;
            coyote_jump_timer = 0;
            touch_ground = false;
        }
    }

    public void wallJump() {
        up = false;
        cur_speed[1] = -WALL_JUMP_HEIGHT; //Sets y speed from wall jump
        cur_speed[0] =  wall_jump_direction * WALL_JUMP_HORIZONTAL_HEIGHT; //Sets x speed for wall jump
        player.setDirection(wall_jump_direction); //Make the player look the right way
        intersected_wall = false;
        firstWallJump = false;
        wall_jumped = false;
        wall_jump_coyote_timer = 0;

    }

    public void left() {
        if(no_control_timer <= 0 && dashNoControlTimer <= 0 && cur_speed[0] > -MAX_SPEED)
            cur_speed[0] -= X_ACCEL;
        if(no_control_timer <= 0 && dashNoControlTimer <= 0 && cur_speed[0] < -MAX_SPEED) //Makes speed into max speed if it goes over
            cur_speed[0] = -MAX_SPEED;
    }

    public void right() {
        if(no_control_timer <= 0 && dashNoControlTimer <= 0 && cur_speed[0] < MAX_SPEED)
            cur_speed[0] += X_ACCEL;
        if(no_control_timer <= 0 && dashNoControlTimer <= 0 && cur_speed[0] > MAX_SPEED) //Makes speed into max speed if it goes over
            cur_speed[0] = MAX_SPEED;
    }

    public void dash() {
        if(canDash && !intersected_wall && dashCooldownTimer <= 0 && respawnTimer < 0) {
            dash = false;
            //gets the direction of the dash
            int[] dir = new int[] {0,0};
            if(left) dir[0]--;
            if(right) dir[0]++;
            if(bounceUp) dir[1]--;
            if(down) dir[1]++;
            if(!Arrays.equals(dir,new int[] {0,0})) { //If there was atleast one directional button pressed
                double angle;
                if (dir[0] != 0)
                    angle = Math.atan(dir[1] / dir[0]) + (dir[0] == 1 ? 0 : Math.PI); //Find the angle of the direction
                else if (dir[1] == 1) //Since it would error out if it was 1 or -1 (1/0), set them manually
                    angle = Math.PI / 2;
                else
                    angle = -Math.PI / 2;
                if(Math.abs(Math.cos(angle) * DASH_SPEED) > 0.1) //Sets X speed if it needs to be set
                    cur_speed[0] = Math.cos(angle) * DASH_SPEED;
                else cur_speed[0] = 0;
                if(Math.abs(Math.sin(angle) * DASH_SPEED) > 0.1) //Sets Y speed if it needs to be set
                    cur_speed[1] = Math.sin(angle) * DASH_SPEED;
                else cur_speed[1] = 0;

                int bonus_dash_time = 0;

                if(Arrays.equals(dir,new int[] {0,-1})) {cur_speed[1] *= 0.9; bonus_dash_time += 10;} //So that vertical dash isn't too powerful
                if(Arrays.equals(dir,new int[] {1, 0}) || Arrays.equals(dir,new int[] {-1, 0})) {bonus_dash_time += 30; cur_speed[1] *= 1;} //To make horizontal dash  a bit better
                if(Arrays.equals(dir,new int[] {0, 1})) {bonus_dash_time += 40; cur_speed[1] *= 1;} //To make down dash a bit better
                dashNoControlTimer = DASH_NO_CONTROL_TIME + bonus_dash_time;
                if(Math.abs(cur_speed[0]) >= 0.4)
                    player.setDirection((cur_speed[0] > 0 ? 1 : -1));
                canDash = false;
                dashed = true;
            }
        }
    }

    public boolean touching_ground() {
        return touch_ground;
    }
}
