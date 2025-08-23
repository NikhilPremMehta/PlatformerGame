import java.util.ArrayList;

public class Scene { //Contains the objects for each Screen/mini-level, it mainly just adds more structure
    private ArrayList<Tile> objects;
    public Scene(ArrayList<Tile> t) {
        objects = t;
    }

    public ArrayList<Tile> getObjects() {
        return objects;
    }
}
