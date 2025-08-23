import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Level {

    public static final double CHANGE_TIME = 0.864;

    private static final String changeSound = Panel.getResourceFilePath("SoundAssets/world_move.wav");
    private static final double OFFSET_Y = 4.67;
    private static final double OFFSET_X = -4;

    private Scene[][] scenes;
    private int[] currentScene;
    //    private int[] checkPointScene;
//    private int[] checkPointCoords;
    private Checkpoint cur_checkpoint;
    private Player player;
    private double[] speed;
    private double timer = 0;
    public boolean changing;
    private int[] globalCoords;

    public Level(Scene[][] scenes, int[] start, Player p) {
        this.scenes = scenes;
        player = p;
        speed = new double[2];
        currentScene = start;
        cur_checkpoint = new Checkpoint(100, Panel.PANEL_HEIGHT - Tile.DEFAULT_HEIGHT-Checkpoint.DEFAULT_CHECKPOINT_HEIGHT, start);
        addTile(cur_checkpoint);
        for (int r = 0; r < scenes.length; r++) { //Offset the positions of tiles so that each scene gets its own space
            for (int c = 0; c < scenes[0].length; c++) {
                if (scenes[r][c] != null) {
                    for (Tile t : scenes[r][c].getObjects()) {
                        t.changeX((c - start[1]) * Panel.PANEL_WIDTH);
                        t.changeY((r - start[0]) * Panel.PANEL_HEIGHT);
                    }
                }
            }
        }
    }

//    public void changeScene(int[] direction) {
//        if(!changing) {
//            changing = true;
//            if (direction[0] == 0 && direction[1] == 1) speed = new int[]{0, Panel.PANEL_HEIGHT};
//            else if (direction[0] == 0 && direction[1] == -1) speed = new int[]{0, -Panel.PANEL_HEIGHT};
//            else if (direction[0] == 1 && direction[1] == 0) speed = new int[]{Panel.PANEL_WIDTH, 0};
//            else speed = new int[]{-Panel.PANEL_WIDTH, 0};
//            currentScene[0] += direction[0];
//            currentScene[1] += direction[1];
//            speed[0] /= (100 * CHANGE_TIME);
//            speed[1] /= (100 * CHANGE_TIME);
//        }
//    }

//    public int[] getCheckPointScene() {
//        return checkPointScene;
//    }

    public void changeScene(int[] scene) {
        if (!changing) {
            changing = true;
            //Sets speed based on position needed to move and change time
            speed[0] = (scene[1] - currentScene[1]) * Panel.PANEL_WIDTH;
            speed[1] = (scene[0] - currentScene[0]) * Panel.PANEL_HEIGHT;
            speed[0] /= (100 * CHANGE_TIME);
            speed[1] /= (100 * CHANGE_TIME);
            StdAudio.playInBackground(changeSound);
            player.realX = player.x;
            player.realY = player.y;
            for(Tile t : getObjects()) {
                t.changeX(OFFSET_X * (scene[1] - currentScene[1])); //Due to rounding errors there needs to be a tiny offset
                t.changeY(OFFSET_Y * (scene[0] - currentScene[0]));
            }

            player.changeX(OFFSET_X * (scene[1] - currentScene[1]));
            player.changeY(OFFSET_Y * (scene[0] - currentScene[0]));
            currentScene = scene;
        }
    }

    public int[] getCurrentScene() {
        return currentScene;
    }

    public void setCheckpoint(Checkpoint checkpoint) {
        cur_checkpoint = checkpoint;
    }


    public double[] getSpeed() {
        return speed;
    }

    public ArrayList<Tile> getObjects() { //Combines objects from every scene into one arraylist of tiles
        ArrayList<Tile> objects = new ArrayList<>();
        for (Scene[] scene_arrays : scenes) {
            for (Scene s : scene_arrays) {
                if (s != null) {
                    for (Tile t : s.getObjects()) {
                        objects.add(t);
                    }
                }
            }
        }
        return objects;
    }

    public void drawSelf(Graphics g) {
        for (Tile t : getObjects()) { //draws every object in its Scenes
            t.drawSelf(g);
        }
    }

    public Checkpoint getCheckpoint() {
        return cur_checkpoint;
    }

    public void addTile(Tile t) {
        scenes[currentScene[0]][currentScene[1]].getObjects().add(0, t); //Adds a tile
    }

}
