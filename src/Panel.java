import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Panel extends JPanel {

    public static final int PANEL_WIDTH = 864;
    public static final int PANEL_HEIGHT = 672;
    private static final int LEVEL_TILE_WIDTH = 30;
    private static final int DISPLAY_FONT_SIZE = 60;
    public static int coins;

    private Image background;

    private Player player;
    private PhysicsUpdater physics;
    private ArrayList<Projectile> projectiles;
    private Level level;
    private boolean up_held, dash_held;
    private int total_coins;
    private ArrayList<Spell> spells;
    private ArrayList<DashTrail> trails;
    private static Image COIN_IMAGE;

    static {
        try {
            COIN_IMAGE = ImageIO.read(Panel.class.getResource("TileAssets/CoinSprites/CoinIdle/tile003.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Add all spells
    private Spell checkPoint = new Spell(1000,30) {
        @Override
        public void activate() {
            if(cooldownTimer < 0 && physics.touching_ground() && player.getMp().getPoints() >= getManaCost()) { //If can place down a checkpoint
                Checkpoint c = new Checkpoint(player.x+player.HITBOX_WIDTH/2-Checkpoint.DEFAULT_CHECKPOINT_WIDTH/2,player.y+player.HITBOX_HEIGHT-Checkpoint.DEFAULT_CHECKPOINT_HEIGHT,level.getCurrentScene()); //Place checkpoint
                level.addTile(c);
                level.setCheckpoint(c);
                cooldownTimer = getCooldown();
                player.getMp().subtractPoints(getManaCost());
            }
        }
    };

    private Spell fireBall = new Spell(600,15) {
        @Override
        public void activate() {
            if(cooldownTimer < 0  && player.getMp().getPoints() >= getManaCost()) { //If can shoot fireball
                Projectile p = new Projectile(player.x+player.width/2, player.y+player.height/2, player.x+player.width/2 + (player.getDirection() > 0 ? 1 : -1), player.y+player.height/2, Projectile.DEFAULT_SPEED,player); //Create fireball
                projectiles.add(p);
                cooldownTimer = getCooldown();
                player.getMp().subtractPoints(getManaCost());
            }
        }
    };

    private Spell healSpell = new Spell(1000,40) {
        @Override
        public void activate() {
            if(cooldownTimer < 0  && player.getMp().getPoints() >= getManaCost()) { //If can heal
                player.getMp().subtractPoints(player.getHp().getMaxHP()-player.getHp().getPoints()); //heal
                player.getHp().reset();
            }
        }
    };

    public Panel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT)); //Sets the size of the tab
        StdAudio.loopInBackground(getResourceFilePath("SoundAssets/Lost_in_the_Dessert.wav"));
        try {
            background = ImageIO.read(getClass().getResource("background.png")); //Add Background
        } catch (IOException e) {
            System.out.println("Couldn't open background image file; field remains null.");
        }

        player = new Player(100,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT- Player.HITBOX_HEIGHT);
        projectiles = new ArrayList<>();
        trails = new ArrayList<>();
        spells = new ArrayList<>();
        level = createLevel(); //Makes the level
        physics = new PhysicsUpdater(player,level,this,projectiles,trails);
        for(Tile t : level.getObjects()) {
            if(t.getTag().equals("Coin")) {
                total_coins++;
            }
        }
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if((e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_UP) && !up_held) {
                    physics.up = true;
                    physics.bounceUp = true;
                    up_held = true;
                }

                if(e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_LEFT)
                    physics.left = true;

                if(e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_RIGHT)
                    physics.right = true;

                if(e.getKeyCode() == KeyEvent.VK_S || e.getKeyCode() == KeyEvent.VK_DOWN)
                    physics.down = true;

                if(e.getKeyCode() == KeyEvent.VK_SPACE && !dash_held) {
                    physics.dash = true;
                    dash_held = true;
                }

                for(int i = 0;i<spells.size();i++) { //Check if a spell should be used (Maps each spell to the number keys)
                    if (e.getKeyChar() == (char) (48 + i+1)) {
                        spells.get(i).activate();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) { //Checks when the key is released to set a key to false, so that you can press two keys at once (jump and right/left)
                super.keyReleased(e);
                if(e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_UP) {
                    physics.up = false;
                    physics.bounceUp = false;
                    up_held = false;
                }
                if(e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_LEFT)
                    physics.left = false;

                if(e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_RIGHT)
                    physics.right = false;

                if(e.getKeyCode() == KeyEvent.VK_S || e.getKeyCode() == KeyEvent.VK_DOWN)
                    physics.down = false;
                if(e.getKeyCode() == KeyEvent.VK_SPACE)  {
                    physics.dash = false;
                    dash_held = false;
                }
            }

        });

        this.addMouseListener(new MouseAdapter() { //To help build levels by getting the positions of specific places
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println(e.getX() + " " + e.getY());
            }
        });
        this.setFocusable(true);
    }


    @Override
    public void paint(Graphics g) {

        g.drawImage(background,0,0,Panel.PANEL_WIDTH,Panel.PANEL_HEIGHT,null);
        for(Projectile p : projectiles) {
            p.drawSelf(g);
        }
        level.drawSelf(g);
        for(DashTrail d : trails) {
            d.drawSelf(g);
        }
        player.drawSelf(g);

        Graphics2D new_g = (Graphics2D) g;
        g.drawImage(COIN_IMAGE,30,30,Coin.DEFAULT_COIN_WIDTH*2,Coin.DEFAULT_COIN_HEIGHT*2,null);
        new_g.setColor(Color.ORANGE);
        new_g.setFont(new Font("ThaleahFat",Font.BOLD,DISPLAY_FONT_SIZE));
        new_g.drawString(coins + "/" + total_coins,86,69);
    }

    private Level createLevel() { //Create the level
        ArrayList<Tile> t0 = new ArrayList<>();
        t0.add(new Tile(0,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT,Panel.PANEL_WIDTH,Tile.DEFAULT_HEIGHT)); //Ground
        t0.add(new Coin(PANEL_WIDTH/2-Coin.DEFAULT_COIN_WIDTH/2,PANEL_HEIGHT/2-Coin.DEFAULT_COIN_HEIGHT/2));
        Border b0 = new Border(true,false,true,true);
        b0.addTiles(t0);
        Scene sceneZero = new Scene(t0);

        ArrayList<Tile> t = new ArrayList<>();
        t.add(new Tile(0,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT,Panel.PANEL_WIDTH,Tile.DEFAULT_HEIGHT)); //Ground
        t.add(new Text(250,400,"Press A and D to move"));
        t.add(new Tile(300,PANEL_HEIGHT-2*Tile.DEFAULT_HEIGHT));
        t.add(new Text(300 + Tile.DEFAULT_WIDTH/2,Panel.PANEL_HEIGHT/2,"Press W to jump"));
        t.add(new Tile(500,Panel.PANEL_HEIGHT-5*Tile.DEFAULT_HEIGHT,Tile.DEFAULT_WIDTH,Tile.DEFAULT_HEIGHT*3));
        t.add(new Tile(700,Panel.PANEL_HEIGHT-4*Tile.DEFAULT_HEIGHT,Tile.DEFAULT_WIDTH,Tile.DEFAULT_HEIGHT*3));
        t.add(new Text(600,140,"Press W on a wall to wall jump"));
        t.add(new Coin(325,140));
        t.add(new Coin(68,100));
        t.add(new Tile(180,27,Tile.DEFAULT_WIDTH,Tile.DEFAULT_HEIGHT*2));
        Border b = new Border(true,false,false,true); //Borders
        b.addTiles(t);
        Scene sceneOne = new Scene(t);

        ArrayList<Tile> t2 = new ArrayList<>();
        t2.add(new Tile(0,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT,Panel.PANEL_WIDTH,Tile.DEFAULT_HEIGHT)); //Ground
        t2.add(new BouncePad(300,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT-BouncePad.DEFAULT_BOUNCE_PAD_HEIGHT));
        t2.add(new Tile(350,Panel.PANEL_HEIGHT-4*Tile.DEFAULT_HEIGHT,Tile.DEFAULT_WIDTH,Tile.DEFAULT_HEIGHT*3));
        t2.add(new Text(300,150,"Hold W to jump higher on a spring"));
        t2.add(new Hazard(350+Tile.DEFAULT_WIDTH,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT-Hazard.DEFAULT_HAZARD_HEIGHT,Hazard.DEFAULT_HAZARD_WIDTH*6,Hazard.DEFAULT_HAZARD_HEIGHT));
        t2.add(new Text(350+Tile.DEFAULT_WIDTH+Hazard.DEFAULT_HAZARD_WIDTH*4,350,"Avoid touching spikes"));
        t2.add(new Coin(470,490));
        Border b2 = new Border(true,false,false,true);
        b2.addTiles(t2);
        Scene sceneTwo = new Scene(t2);

        ArrayList<Tile> t3 = new ArrayList<>();
        t3.add(new Tile(0,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT,Panel.PANEL_WIDTH,Tile.DEFAULT_HEIGHT)); //Ground
        t3.add(new Tile(250,Panel.PANEL_HEIGHT-3*Tile.DEFAULT_HEIGHT,Tile.DEFAULT_WIDTH,Tile.DEFAULT_HEIGHT*2));
        t3.add(new Text(350,100,"Press SPACE to dash in the direction\n your arrow keys are directed towards\nYou can dash again once you hit the ground"));
        t3.add(new Tile(650,Panel.PANEL_HEIGHT-4*Tile.DEFAULT_HEIGHT,Tile.DEFAULT_WIDTH,Tile.DEFAULT_HEIGHT*3));
        t3.add(new Coin(510,392));
        t3.add(new Hazard(260+Tile.DEFAULT_WIDTH,Panel.PANEL_HEIGHT-Tile.DEFAULT_WIDTH-Hazard.DEFAULT_HAZARD_HEIGHT,300,Hazard.DEFAULT_HAZARD_HEIGHT));
        t3.add(new Checkpoint(650+Tile.DEFAULT_WIDTH/2-Checkpoint.DEFAULT_CHECKPOINT_WIDTH/2,Panel.PANEL_HEIGHT-4*Tile.DEFAULT_HEIGHT-Checkpoint.DEFAULT_CHECKPOINT_HEIGHT,new int[] {0,3}));
        Border b3 = new Border(true,false,false,true);
        b3.addTiles(t3);
        Scene sceneThree = new Scene(t3);

        ArrayList<Tile> t4 = new ArrayList<>();
        t4.add(new Tile(0,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT,Panel.PANEL_WIDTH-PhasePlatform.DEFAULT_PHASE_PLATFORM_WIDTH-Tile.DEFAULT_WIDTH,Tile.DEFAULT_HEIGHT)); //Ground
        t4.add(new FakePlatform(250,Panel.PANEL_HEIGHT-3*Tile.DEFAULT_HEIGHT,Tile.DEFAULT_WIDTH,Tile.DEFAULT_HEIGHT*2));
        t4.add(new PhasePlatform(Panel.PANEL_WIDTH-PhasePlatform.DEFAULT_PHASE_PLATFORM_WIDTH-Tile.DEFAULT_WIDTH,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT));
        t4.add(new Tile(Panel.PANEL_WIDTH-Tile.DEFAULT_WIDTH,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT));
        t4.add(new Text(300,200,"Sometimes things are not as they seem..."));
        t4.add(new Text(550,350,"Press S on wooden platforms to go down"));
        t4.add(new Coin(268,175));
        t4.add(new Coin(Panel.PANEL_WIDTH-PhasePlatform.DEFAULT_PHASE_PLATFORM_WIDTH-Tile.DEFAULT_WIDTH/2+Coin.DEFAULT_COIN_WIDTH/2,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT+30));
        Border b4 = new Border(true,true,false,false);
        b4.addTiles(t4);
        Scene sceneFour = new Scene(t4);

        ArrayList<Tile> t5 = new ArrayList<>();
        t5.add(new Tile(0,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT,Panel.PANEL_WIDTH,Tile.DEFAULT_HEIGHT)); //Ground
        t5.add(new Hazard(0,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT-Hazard.DEFAULT_HAZARD_HEIGHT,PANEL_WIDTH,Hazard.DEFAULT_HAZARD_HEIGHT,45));
        t5.add(new Text(Panel.PANEL_WIDTH/2,100,"Toxic spikes will instantly kill you"));
        t5.add(new Tile(Panel.PANEL_WIDTH-3*Tile.DEFAULT_WIDTH,200,Tile.DEFAULT_WIDTH*3,Tile.DEFAULT_HEIGHT));
        t5.add(new Checkpoint(Panel.PANEL_WIDTH-3*Tile.DEFAULT_WIDTH/2-Checkpoint.DEFAULT_CHECKPOINT_WIDTH/2,200-Checkpoint.DEFAULT_CHECKPOINT_HEIGHT,new int[] {1,4}));
        t5.add(new MovePlatform(Panel.PANEL_WIDTH-Tile.DEFAULT_WIDTH*4,340,MovePlatform.DEFAULT_MOVE_PLATFORM_WIDTH*2,MovePlatform.DEFAULT_MOVE_PLATFORM_HEIGHT,50,340,4));
        t5.add(new Coin(620,400));
        Border b5 = new Border(false,true,false,true);
        b5.addTiles(t5);
        Scene sceneFive = new Scene(t5);

        ArrayList<Tile> t6 = new ArrayList<>();
        t6.add((new Tile(0,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT,Panel.PANEL_WIDTH,Tile.DEFAULT_HEIGHT))); //Ground
        t6.add(new Hazard(0,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT-Hazard.DEFAULT_HAZARD_HEIGHT,PANEL_WIDTH-2*Tile.DEFAULT_WIDTH,Hazard.DEFAULT_HAZARD_HEIGHT,45));
        t6.add(new BreakPlatform(20,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT-Hazard.DEFAULT_HAZARD_HEIGHT-48,120,BreakPlatform.DEFAULT_BREAK_PLATFORM_HEIGHT));
        t6.add(new BreakPlatform(140,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT-Hazard.DEFAULT_HAZARD_HEIGHT-48,120,BreakPlatform.DEFAULT_BREAK_PLATFORM_HEIGHT));
        t6.add(new BreakPlatform(260,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT-Hazard.DEFAULT_HAZARD_HEIGHT-48,120,BreakPlatform.DEFAULT_BREAK_PLATFORM_HEIGHT));
        t6.add(new BreakPlatform(380,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT-Hazard.DEFAULT_HAZARD_HEIGHT-48,120,BreakPlatform.DEFAULT_BREAK_PLATFORM_HEIGHT));
        t6.add(new MovePlatform(20,100,480,MovePlatform.DEFAULT_MOVE_PLATFORM_HEIGHT*3,20,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT-Hazard.DEFAULT_HAZARD_HEIGHT-48-MovePlatform.DEFAULT_MOVE_PLATFORM_HEIGHT*3,3));
        Border b6 = new Border(true,false,false,true);
        b6.addTiles(t6);
        Scene sceneSix = new Scene(t6);

        ArrayList<Tile> t7 = new ArrayList<>();
        t7.add((new Tile(0,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT,Panel.PANEL_WIDTH,Tile.DEFAULT_HEIGHT)));
        t7.add(new Enemy(Panel.PANEL_WIDTH-200,Panel.PANEL_WIDTH-400,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT-Enemy.ENEMY_HEIGHT,4));
        t7.add(new Tile(Panel.PANEL_WIDTH-400-Tile.DEFAULT_WIDTH,Panel.PANEL_HEIGHT-2*Tile.DEFAULT_HEIGHT));
        t7.add(new Enemy(Panel.PANEL_WIDTH-700,Panel.PANEL_WIDTH-400-Tile.DEFAULT_WIDTH-Enemy.DEFAULT_WIDTH,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT-Enemy.ENEMY_HEIGHT,6,Enemy.DEFAULT_HEALTH*2,true));
        t7.add(new Coin(717,95));
        t7.add(new Text(630,300,"Jump on the Enemy to do damage"));
        t7.add(new Text(300,100,"Some enemies only take damage\n on a down dash (does 3x the damage)"));
        Border b7 = new Border(true,false,false,true);
        b7.addTiles(t7);
        Scene sceneSeven = new Scene(t7);

        ArrayList<Tile> t8 = new ArrayList<>();
        t8.add(new Tile(0,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT,Panel.PANEL_WIDTH,Tile.DEFAULT_HEIGHT));
        t8.add(new Hazard(Tile.DEFAULT_WIDTH,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT-Hazard.DEFAULT_HAZARD_HEIGHT,Hazard.DEFAULT_HAZARD_WIDTH*11,Hazard.DEFAULT_HAZARD_HEIGHT,45));
        t8.add(new Tile(600,Panel.PANEL_HEIGHT-4*Tile.DEFAULT_HEIGHT,Tile.DEFAULT_WIDTH,Tile.DEFAULT_HEIGHT*3));
        t8.add(new Tile(400,0,Tile.DEFAULT_WIDTH,Tile.DEFAULT_HEIGHT*4));
        t8.add(new Checkpoint(Panel.PANEL_WIDTH-Tile.DEFAULT_WIDTH*3/2,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT-Checkpoint.DEFAULT_CHECKPOINT_HEIGHT,new int[] {1,1}));
        t8.add(new DashOrb(286,409));
        t8.add(new DashOrb(170,360));
        t8.add(new Text(200,230,"Pick up the orb to\n replenish your dash"));
        t8.add(new Tile(0,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT*4,Tile.DEFAULT_WIDTH,Tile.DEFAULT_HEIGHT*3));
        t8.add(new Coin(320,140));
        Border b8 = new Border(true,false,false,true);
        b8.addTiles(t8);
        Scene sceneEight = new Scene(t8);

        ArrayList<Tile> t9 = new ArrayList<>();
        t9.add(new Tile(Tile.DEFAULT_WIDTH+PhasePlatform.DEFAULT_PHASE_PLATFORM_WIDTH,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT,Panel.PANEL_WIDTH-Tile.DEFAULT_WIDTH*2,Tile.DEFAULT_HEIGHT));
        t9.add(new Tile(0,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT));
        t9.add(new PhasePlatform(Tile.DEFAULT_WIDTH,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT));
        t9.add(new Tile(Panel.PANEL_WIDTH-3*Tile.DEFAULT_WIDTH,Panel.PANEL_HEIGHT - Tile.DEFAULT_HEIGHT*3,Tile.DEFAULT_WIDTH,Tile.DEFAULT_HEIGHT*2));
        t9.add(new SpellOrb(150,450,healSpell,Color.GREEN,spells));
        t9.add(new SpellOrb(250,450,fireBall,Color.RED,spells));
        t9.add(new SpellOrb(350,450,checkPoint,Color.CYAN,spells));
        t9.add(new Text(Panel.PANEL_WIDTH/2,100,"I didn't have the time to fully implement these\nso I just put them here\nWhen you collect one of these orbs\nyou get a spell, to use the spells\nuse the number keys corresponding to the order\n of unlocking them. The green one is a heal spell\nThe red is a fireball spell\nThe blue is a checkpoint spell"));
        Border b9 = new Border(true,false,true,false);
        b9.addTiles(t9);
        Scene sceneNine = new Scene(t9);

        ArrayList<Tile> t10 = new ArrayList<>();
        t10.add(new Tile(Tile.DEFAULT_WIDTH+PhasePlatform.DEFAULT_PHASE_PLATFORM_WIDTH,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT,Panel.PANEL_WIDTH-Tile.DEFAULT_WIDTH*2,Tile.DEFAULT_HEIGHT));
        t10.add(new Tile(0,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT));
        t10.add(new PhasePlatform(Tile.DEFAULT_WIDTH,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT));
        ArrayList<Key> keys = new ArrayList<>();
        Key k = new Key(435+BreakPlatform.DEFAULT_BREAK_PLATFORM_WIDTH/2-Key.DEFAULT_KEY_WIDTH/2,50);
        Key k2 = new Key(700,150);
        keys.add(k2);
        keys.add(k);
        t10.add(new Door(Tile.DEFAULT_WIDTH+PhasePlatform.DEFAULT_PHASE_PLATFORM_WIDTH/2-Door.HITBOX_WIDTH/2,Panel.PANEL_HEIGHT - Tile.DEFAULT_HEIGHT-Door.HITBOX_HEIGHT,keys));
        t10.add(new Door(Tile.DEFAULT_WIDTH+PhasePlatform.DEFAULT_PHASE_PLATFORM_WIDTH/2-Door.HITBOX_WIDTH/2-Door.HITBOX_WIDTH,Panel.PANEL_HEIGHT - Tile.DEFAULT_HEIGHT-Door.HITBOX_HEIGHT,keys));
        t10.add(new Door(Tile.DEFAULT_WIDTH+PhasePlatform.DEFAULT_PHASE_PLATFORM_WIDTH/2-Door.HITBOX_WIDTH/2+Door.HITBOX_WIDTH,Panel.PANEL_HEIGHT - Tile.DEFAULT_HEIGHT-Door.HITBOX_HEIGHT,keys));
        t10.add(new ShooterEnemy(400,650,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT-ShooterEnemy.ENEMY_HEIGHT,4,projectiles,Enemy.DEFAULT_HEALTH,false));
        t10.add(new BreakPlatform(435,300));
        t10.add(new PhasePlatform(435+BreakPlatform.DEFAULT_BREAK_PLATFORM_WIDTH/2-24,150,48,PhasePlatform.DEFAULT_PHASE_PLATFORM_HEIGHT));
        t10.add(new Text(250,100,"You can jump on \nwooden platforms from below"));
        t10.add(k);
        t10.add(k2);
        t10.add(new Tile(504,0,Tile.DEFAULT_WIDTH,Tile.DEFAULT_HEIGHT*2));
        t10.add(new Coin(775,15));
        Border b10 = new Border(false,true,true,false);
        b10.addTiles(t10);
        Scene sceneTen = new Scene(t10);

        ArrayList<Tile> t11 = new ArrayList<>();
        t11.add((new Tile(0,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT,Panel.PANEL_WIDTH,Tile.DEFAULT_HEIGHT))); //Ground
        t11.add(new Hazard(Tile.DEFAULT_WIDTH,PANEL_HEIGHT-Tile.DEFAULT_HEIGHT-Hazard.DEFAULT_HAZARD_HEIGHT,Panel.PANEL_WIDTH-2*Tile.DEFAULT_WIDTH,Hazard.DEFAULT_HAZARD_HEIGHT,45));
        t11.add(new Tile(0,PANEL_HEIGHT-2*Tile.DEFAULT_HEIGHT));
        t11.add(new BouncePad(BouncePad.DEFAULT_BOUNCE_PAD_WIDTH/2,Panel.PANEL_HEIGHT-2*Tile.DEFAULT_WIDTH-BouncePad.DEFAULT_BOUNCE_PAD_HEIGHT));
        t11.add(new DashOrb(450,340));
        t11.add(new DashOrb(650,240));
        t11.add(new Tile(Panel.PANEL_WIDTH-Tile.DEFAULT_WIDTH,Panel.PANEL_HEIGHT-5*Tile.DEFAULT_HEIGHT,Tile.DEFAULT_WIDTH,Tile.DEFAULT_HEIGHT*4));

        Border b11 = new Border(false,false,true,true);
        b11.addTiles(t11);
        Scene sceneEleven = new Scene(t11);

        ArrayList<Tile> t12 = new ArrayList<>();
        t12.add(new Tile(0,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT,Panel.PANEL_WIDTH-Tile.DEFAULT_HEIGHT,Tile.DEFAULT_HEIGHT,new String[][] {{"1001","1000","1000","1000","1000","1000","1000","1000","1000","1000","1000","1000","1000","1000","1000","1000"},{"0011","0010","0010","0010","0010","0010","0010","0010","0010","0010","0010","0010","0010","0010","0010","0010"}})); //Ground
        t12.add(new Coin(795,605));
        t12.add(new FakePlatform(Panel.PANEL_WIDTH-Tile.DEFAULT_HEIGHT,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT,Panel.getResourceFilePath("TileAssets/fakePlatform1.png")));
        t12.add(new Text(PANEL_WIDTH/2,PANEL_HEIGHT/2,"You Win!"));
        Border b12 = new Border(true,true,false,false);
        b12.addTiles(t12);
        Scene sceneTwelve = new Scene(t12);

        ArrayList<Tile> t13 = new ArrayList<>();
        t13.add(new Tile(0,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT,Panel.PANEL_WIDTH,Tile.DEFAULT_HEIGHT)); //Ground
        t13.add(new Coin(45,45));
        t13.add(new Tile(Panel.PANEL_WIDTH-2*Tile.DEFAULT_WIDTH,Tile.DEFAULT_HEIGHT*3,2*Tile.DEFAULT_WIDTH,Tile.DEFAULT_HEIGHT));
        t13.add(new ShooterEnemy(350,350,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT-Enemy.ENEMY_HEIGHT,5,projectiles,50,false));
        t13.add(new Hazard(0,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT-Hazard.DEFAULT_HAZARD_HEIGHT,350,Hazard.DEFAULT_HAZARD_HEIGHT,45));
        t13.add(new Hazard(350+ShooterEnemy.ENEMY_WIDTH,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT-Hazard.DEFAULT_HAZARD_HEIGHT,Panel.PANEL_WIDTH-350+ShooterEnemy.ENEMY_WIDTH,Hazard.DEFAULT_HAZARD_HEIGHT,45));
        t13.add(new Tile(Panel.PANEL_WIDTH-Tile.DEFAULT_WIDTH*5,0,Tile.DEFAULT_WIDTH,Tile.DEFAULT_HEIGHT*4));
        Border b13 = new Border(false,true,false,true);
        b13.addTiles(t13);
        Scene sceneThirteen = new Scene(t13);

        ArrayList<Tile> t14 = new ArrayList<>();
        t14.add(new Tile(0,Panel.PANEL_HEIGHT-Tile.DEFAULT_HEIGHT,Panel.PANEL_WIDTH,Tile.DEFAULT_HEIGHT)); //Ground
        t14.add(new Text(PANEL_WIDTH/2,PANEL_HEIGHT/2,"The actual You Win!"));
        t14.add(new Coin(Panel.PANEL_WIDTH - 721,97));
        Border b14 = new Border(true,false,true,true);
        b14.addTiles(t14);
        Scene sceneFourteen = new Scene(t14);

        Level level = new Level(new Scene[][] {{sceneZero,sceneOne,sceneTwo,sceneThree,sceneFour},{sceneNine,sceneEight,sceneSeven,sceneSix,sceneFive},{sceneTen,null,null,null,null},{sceneEleven,sceneTwelve,null,null,null},{sceneFourteen,sceneThirteen,null,null,null}},new int[] {0,1},player);
        return level;
    }

    public static Image[] loadAnimation(String pathName) {
        //Get all frames from the animation folder
        File[] frames = new File(pathName).listFiles(new FilenameFilter() { //This is so that the file with the metadata isnt included as a frame in the animation
            @Override
            public boolean accept(File dir, String name) {
                return !name.equals(".DS_Store");
            }
        });

        //An issue occurred where the animation wasn't in order even though it should have been lexicographically, so this is to sort the animation in order
        Arrays.sort(frames, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.compareTo(o2);
            }
        });

        //Actually set the images to each file
        Image[] images = new Image[frames.length];
        for (int i = 0; i < frames.length; i++) {
            try {
                images[i] = ImageIO.read(frames[i]);
            } catch(IOException e) {
                System.out.println(e);
            }
        }
        return images;
    }

    public static String getResourceFilePath(String resourceName) {
        // Try to get the resource from the classpath
        URL resourceUrl = Panel.class.getClassLoader().getResource(resourceName);

        if (resourceUrl != null) {
            // Convert the resource URL to a file path
            return Paths.get(resourceUrl.getPath()).toString();
        } else {
            System.out.println("Resource not found: " + resourceName);
            return null;
        }
    }

}

