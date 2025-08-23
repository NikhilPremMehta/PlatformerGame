import java.awt.*;
import javax.swing.*;

public class Game extends JFrame {



    public Game() {
        super("Platformer"); //Uses the constructor to set the title to "Platformer"
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Makes the project exit when closed
        this.add(new Panel()); //Creates the panel for the visuals to appear on
        this.pack(); // all components have been added -- shrink frame to fit
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); //Gets the screen size
        int x = screenSize.width / 2 - getWidth() / 2;
        int y = screenSize.height / 2 - getHeight() / 2;
        this.setLocation(x, y); // positions the game window in center of screen
    }
}

