import java.awt.*;
import java.util.ArrayList;

public class SpellOrb extends Tile implements Activatable {

    private static final int DEFAULT_SPELL_ORB_WIDTH = 30;
    private static final int DEFAULT_SPELL_ORB_HEIGHT = 30;
    private Spell s;
    private ArrayList<Spell> spells;
    private static final String activateSound = Panel.getResourceFilePath("SoundAssets/OrbPickup.wav");

    public SpellOrb(int initX, int initY, int width, int height, Spell s, Color color,ArrayList<Spell> spells) {
        super(initX,initY,width,height);
        this.s = s;
        this.spells = spells;
        setColor(color);
        setTag("Spell Orb");
    }

    public SpellOrb(int initX, int initY, Spell s, Color color,ArrayList<Spell> spells) {
        this(initX,initY,DEFAULT_SPELL_ORB_WIDTH,DEFAULT_SPELL_ORB_HEIGHT,s,color,spells);
    }

    public void activate() {
        setTag("Invisible");
        setColor(new Color(getColor().getRed(),getColor().getGreen(),getColor().getBlue(),0));
        spells.add(s); //Add the spell to the main list if the orb is activated
        StdAudio.playInBackground(activateSound);
    }

    @Override
    public void drawImage(Graphics g) {
        ((Graphics2D)g).fill(this);
    }
}
