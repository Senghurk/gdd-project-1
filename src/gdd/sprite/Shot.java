package gdd.sprite;

import static gdd.Global.*;
import javax.swing.ImageIcon;

public class Shot extends Sprite {

    private static final int H_SPACE = 1; // Horizontal offset from player 
    private static final int V_SPACE = 20; // Vertical offset from player center
    private int framesSinceFired = 0; // Track frames since shot was fired
    private static final int COLLISION_DELAY = 3; // Frames to wait before collision detection

    public Shot() {
        // Set fixed collision bounds for shots
        setCollisionBounds(8, 4); // Small collision box for shots
    }

    public Shot(int x, int y) {
        // Set fixed collision bounds for shots
        setCollisionBounds(8, 4); // Small collision box for shots
        initShot(x, y);
    }

    private void initShot(int x, int y) {

        var ii = new ImageIcon(IMG_SHOT);

        // Scale the image to use the global scaling factor
        var scaledImage = ii.getImage().getScaledInstance(ii.getIconWidth() * SCALE_FACTOR,
                ii.getIconHeight() * SCALE_FACTOR, 
                java.awt.Image.SCALE_SMOOTH);
        
        // TODO: Re-enable rotation once image loading is fixed
        // var rotatedImage = rotateImage(scaledImage, 90);
        // setImage(rotatedImage);
        setImage(scaledImage);

        setX(x + H_SPACE); // Spawn shot slightly to the right of player
        setY(y + V_SPACE); // Spawn shot at player's vertical center
        framesSinceFired = 0; // Reset frame counter
    }



    public void act() {
        this.x += 12; // Super fast shots for intense gameplay
        framesSinceFired++; // Increment frame counter
    }

    public void act(int direction) {
        // Shots move rightward for sideways gameplay
        act();
    }
    
    // Override collision detection to add delay
    @Override
    public boolean collidesWith(Sprite other) {
        // Don't detect collisions until enough frames have passed
        if (framesSinceFired < COLLISION_DELAY) {
            return false;
        }
        return super.collidesWith(other);
    }
}
