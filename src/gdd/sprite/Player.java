package gdd.sprite;

import static gdd.Global.*;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public class Player extends Sprite {

    private static final int START_X = 50; // Move player to left side of screen
    private static final int START_Y = 270; // Center vertically
    private int width;
    
    // NEW: Separated powerup system
    private int currentSpeed = INITIAL_PLAYER_SPEED; // Start with speed 3
    private int currentBulletCount = INITIAL_BULLET_COUNT; // Start with 1 bullet
    private int invincibilityFrames = 0;
    
    // RESTORED: MultiShot system for Scene 2
    private int multishotFramesRemaining = 0;
    private int extraShots = 0;
    private int autoFireCooldown = 0;
    

    private Rectangle bounds = new Rectangle(175,135,17,32);

    public Player() {
        // Set fixed collision bounds for player - smaller than visual size to prevent false collisions
        setCollisionBounds(25, 15); // Player collision box - smaller than visual
        initPlayer();
    }

    private void initPlayer() {
        var ii = new ImageIcon(IMG_PLAYER);

        // Scale the image to use the global scaling factor
        var scaledImage = ii.getImage().getScaledInstance(ii.getIconWidth() * SCALE_FACTOR,
                ii.getIconHeight() * SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH);
        
        // TODO: Re-enable rotation once image loading is fixed
        // var rotatedImage = rotateImage(scaledImage, 90);
        // setImage(rotatedImage);
        setImage(scaledImage);

        setX(START_X);
        setY(START_Y);
    }

    public int getSpeed() {
        return currentSpeed;
    }

    public int setSpeed(int speed) {
        if (speed < 1) {
            speed = 1; // Ensure speed is at least 1
        }
        this.currentSpeed = speed;
        return currentSpeed;
    }

    public void act() {
        y += dx; // Change from x += dx to y += dx for vertical movement

        if (y <= 2) {
            y = 2;
        }

        if (y >= BOARD_HEIGHT - 2 * width) {
            y = BOARD_HEIGHT - 2 * width;
        }
        
        // Update multishot timer
        if (multishotFramesRemaining > 0) {
            multishotFramesRemaining--;
            if (multishotFramesRemaining == 0) {
                extraShots = 0; // Reset extra shots when timer expires
            }
        }
        
        // Update auto-fire cooldown
        if (autoFireCooldown > 0) {
            autoFireCooldown--;
        }
        
        // Update invincibility frames
        if (invincibilityFrames > 0) {
            invincibilityFrames--;
        }
    }

    public void act(int direction) {
        // Player movement is controlled by keyboard input, not direction parameter
        act();
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_UP) {
            dx = -currentSpeed; // Move up
        }

        if (key == KeyEvent.VK_DOWN) {
            dx = currentSpeed; // Move down
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_UP) {
            dx = 0;
        }

        if (key == KeyEvent.VK_DOWN) {
            dx = 0;
        }
    }
    
    // NEW: Separated powerup system methods
    public void increaseSpeed() {
        if (currentSpeed < MAX_PLAYER_SPEED) {
            currentSpeed += SPEED_BOOST_AMOUNT;
        }
    }
    
    public void increaseBulletCount() {
        if (currentBulletCount < MAX_BULLET_COUNT) {
            currentBulletCount += BULLET_COUNT_INCREASE;
        }
    }
    
    public int getCurrentBulletCount() {
        return currentBulletCount;
    }
    
    public int getMaxBulletCount() {
        return MAX_BULLET_COUNT;
    }
    
    public int getCurrentSpeed() {
        return currentSpeed;
    }
    
    public int getMaxSpeed() {
        return MAX_PLAYER_SPEED;
    }
    
    // RESTORED: MultiShot system methods for Scene 2
    public void activateMultishot(int duration, int shots) {
        this.multishotFramesRemaining = duration;
        this.extraShots = shots;
    }
    
    public boolean hasMultishot() {
        return multishotFramesRemaining > 0 && extraShots > 0;
    }
    
    public int getExtraShots() {
        return extraShots;
    }
    
    public int getMultishotFramesRemaining() {
        return multishotFramesRemaining;
    }
    
    public boolean canAutoFire() {
        return hasMultishot() && autoFireCooldown <= 0;
    }
    
    public void triggerAutoFire() {
        autoFireCooldown = 15; // 15 frames between auto-shots
    }
    
    // UPDATED: Max shots based on current bullet count (for Scene 1)
    public int getMaxShots() {
        return currentBulletCount;
    }
    
    public boolean isInvincible() {
        return invincibilityFrames > 0;
    }
    
    public void takeDamage() {
        if (!isInvincible()) {
            invincibilityFrames = 120; // 2 seconds of invincibility at 60 FPS
        }
    }
}