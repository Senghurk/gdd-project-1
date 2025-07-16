package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;
import javax.swing.ImageIcon;

public class AddBulletPowerUp extends PowerUp {

    public AddBulletPowerUp(int x, int y) {
        super(x, y);
        // Set image with proper scaling
        ImageIcon ii = new ImageIcon(IMG_POWERUP_ADDBULLET);
        var scaledImage = ii.getImage().getScaledInstance(ii.getIconWidth() * SCALE_FACTOR,
                ii.getIconHeight() * SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }



    public void upgrade(Player player) {
        // Increase bullet count by 1
        player.increaseBulletCount();
        this.die(); // Remove the power-up after use
    }
} 