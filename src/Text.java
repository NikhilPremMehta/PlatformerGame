import java.awt.*;

public class Text extends Tile{

    private static final int DEFAULT_TEXT_SIZE = 32;
    private String text;
    private Font textFont;
    private int textSize;
    private Color c;

    public Text(int initX, int initY, String text, int textSize,Color c) {
        super(initX,initY,0,0);
        setTag("Invisible"); //Sets the tag
        this.text = text;
        this.textSize = textSize;
        this.c = c;
        textFont = new Font("ThaleahFat", Font.PLAIN, textSize);
    }

    public Text(int initX,int initY,String text) {
        this(initX,initY,text,DEFAULT_TEXT_SIZE,Color.BLACK);
    }

    public void drawSelf(Graphics g) {
        g.setColor(c);
        g.setFont(textFont);
        int count = 0; //Count is used so that I can determine the y offset
        for (String line : text.split("\n")) { //Used so that /n actually adds a new line
            g.drawString(line, x - g.getFontMetrics().stringWidth(line) / 2, y + g.getFontMetrics().getHeight() * count); //Centers the text
            count++;
        }
    }
}
