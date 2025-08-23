import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class Spell {
    private int cooldown;
    public int cooldownTimer;
    private int manaCost;

    public Spell(int cd,int manaCost) {
        cooldown = cd;
        this.manaCost = manaCost;
        Timer t = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cooldownTimer-=10; //Decrement the cooldown
            }
        });
        t.start();
    }

    public abstract void activate();

    public int getManaCost() {
        return manaCost;
    }

    public int getCooldown() {
        return cooldown;
    }

}
