package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;
import javax.swing.ImageIcon;

public class MultiShotPowerUp extends PowerUp {

    public MultiShotPowerUp(int x, int y) {
        super(x, y);
        // Set image with proper scaling
        ImageIcon ii = new ImageIcon(IMG_POWERUP_MULTISHOT);
        var scaledImage = ii.getImage().getScaledInstance(ii.getIconWidth() * SCALE_FACTOR,
                ii.getIconHeight() * SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }

    public void act() {
        // MultiShot specific behavior for sideways gameplay
        // Move from right to left like enemies
        this.x -= 2; // Move left by 2 pixels each frame
        
        // Remove power-up if it goes off the left side of the screen
        if (this.x < -50) {
            this.die();
        }
    }

    public void upgrade(Player player) {
        // Activate multishot for the player
        player.activateMultishot(MULTISHOT_DURATION_FRAMES, MULTISHOT_EXTRA_SHOTS);
        this.die(); // Remove the power-up after use
    }
}