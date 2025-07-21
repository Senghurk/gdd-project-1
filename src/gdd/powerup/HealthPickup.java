package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;
import javax.swing.ImageIcon;

public class HealthPickup extends PowerUp {

    public HealthPickup(int x, int y) {
        super(x, y);
        // Set image
        ImageIcon ii = new ImageIcon(IMG_POWERUP_HEALTH);
        var scaledImage = ii.getImage().getScaledInstance(ii.getIconWidth() ,
                ii.getIconHeight() ,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }

    @Override
    public void upgrade(Player player) {
        // Health pickup doesn't directly modify player - the scene handles lives
        // This method will be called by the scene to trigger health restoration
        this.die(); // Remove the power-up after use
    }
    
    // Special method to identify this as a health pickup
    public boolean isHealthPickup() {
        return true;
    }
}