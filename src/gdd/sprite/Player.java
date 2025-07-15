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
    private int currentSpeed = 8; // Increased default speed (2+6)
    
    // MultiShot powerup variables
    private int multishotFramesRemaining = 0;
    private int extraShots = 0;
    private int autoFireCooldown = 0;
    private int invincibilityFrames = 0;
    
    // NEW: Track if player has collected SpeedUp powerup
    private boolean hasCollectedSpeedUp = false;
    

    private Rectangle bounds = new Rectangle(175,135,17,32);

    public Player() {
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

    private BufferedImage rotateImage(java.awt.Image image, double angle) {
        // Ensure the image is fully loaded
        if (image.getWidth(null) <= 0 || image.getHeight(null) <= 0) {
            // If dimensions are not available, return a simple BufferedImage conversion
            BufferedImage bufferedImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bufferedImage.createGraphics();
            g.drawImage(image, 0, 0, 32, 32, null);
            g.dispose();
            image = bufferedImage;
        }
        
        // Convert to BufferedImage if needed
        BufferedImage bufferedImage;
        if (image instanceof BufferedImage) {
            bufferedImage = (BufferedImage) image;
        } else {
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bufferedImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
        }

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        // Create new image with swapped dimensions for 90-degree rotation
        BufferedImage rotatedImage = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotatedImage.createGraphics();

        // Rotate around center
        AffineTransform transform = new AffineTransform();
        transform.translate(height / 2.0, width / 2.0);
        transform.rotate(Math.toRadians(angle));
        transform.translate(-width / 2.0, -height / 2.0);

        g2d.setTransform(transform);
        g2d.drawImage(bufferedImage, 0, 0, null);
        g2d.dispose();

        return rotatedImage;
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
    
    // UPDATED: Modified to respect single-shot limitation
    public int getMaxShots() {
        // Only allow 1 shot until SpeedUp is collected
        if (!hasCollectedSpeedUp) {
            return 1;
        }
        // After collecting SpeedUp, allow normal shot limits
        return hasMultishot() ? 15 : 6;
    }
    
    // UPDATED: Auto-fire only works if player has collected SpeedUp
    public boolean canAutoFire() {
        return hasCollectedSpeedUp && hasMultishot() && autoFireCooldown <= 0;
    }
    
    public void triggerAutoFire() {
        autoFireCooldown = 15; // 15 frames between auto-shots (more reasonable)
    }
    
    public boolean isInvincible() {
        return invincibilityFrames > 0;
    }
    
    public void takeDamage() {
        if (!isInvincible()) {
            invincibilityFrames = 120; // 2 seconds of invincibility at 60 FPS
        }
    }
    
    // Speed boost methods (now permanent)
    public void applySpeedBoost(int boostAmount) {
        int newSpeed = Math.min(currentSpeed + boostAmount, MAX_PLAYER_SPEED);
        currentSpeed = newSpeed;
    }
    
    public boolean canReceiveSpeedBoost() {
        return currentSpeed < MAX_PLAYER_SPEED; // Can get speed until hitting cap
    }
    
    // NEW: Methods to handle SpeedUp powerup collection
    public void enableMultipleShots() {
        this.hasCollectedSpeedUp = true;
    }
    
    public boolean canShootMultiple() {
        return hasCollectedSpeedUp;
    }
    
    public boolean hasCollectedSpeedUp() {
        return hasCollectedSpeedUp;
    }
    
}