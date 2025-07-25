package gdd.sprite;

import gdd.Global;
import static gdd.Global.*;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;

public class Player extends Sprite {

    // Mode-aware positioning - use Global helper methods instead of fixed constants
    private int width;
    
    // NEW: Separated powerup system
    private int currentSpeed = INITIAL_PLAYER_SPEED; // Start with speed 3
    private int currentBulletCount = INITIAL_BULLET_COUNT; // Start with 1 bullet
    private int invincibilityFrames = 0;
    
    // RESTORED: MultiShot system for Scene 2
    private int multishotFramesRemaining = 0;
    private int extraShots = 0;
    private int autoFireCooldown = 0;
    
    private int frameCounter = 0; // For sprite animation
    private java.awt.Image scaledImgH1, scaledImgH2, scaledImgV1, scaledImgV2;


    public Player() {
        // Set fixed collision bounds for player - smaller than visual size to prevent false collisions
        setCollisionBounds(30, 25); // Player collision box - smaller than visual
        initPlayer();
    }

    private void initPlayer() {
        // Preload and scale both animation frames for both modes
        var iiH1 = new ImageIcon(IMG_PLAYER_H1);
        var iiH2 = new ImageIcon(IMG_PLAYER_H2);
        var iiV1 = new ImageIcon(IMG_PLAYER_V1);
        var iiV2 = new ImageIcon(IMG_PLAYER_V2);
        scaledImgH1 = iiH1.getImage().getScaledInstance(iiH1.getIconWidth() * SCALE_FACTOR,
                iiH1.getIconHeight() * SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH);
        scaledImgH2 = iiH2.getImage().getScaledInstance(iiH2.getIconWidth() * SCALE_FACTOR,
                iiH2.getIconHeight() * SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH);
        scaledImgV1 = iiV1.getImage().getScaledInstance(iiV1.getIconWidth() * SCALE_FACTOR,
                iiV1.getIconHeight() * SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH);
        scaledImgV2 = iiV2.getImage().getScaledInstance(iiV2.getIconWidth() * SCALE_FACTOR,
                iiV2.getIconHeight() * SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH);

        // Set initial image
        if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
            setImage(scaledImgV1);
        } else {
            setImage(scaledImgH1);
        }

        // Use mode-aware positioning
        setX(Global.getPlayerStartX());
        setY(Global.getPlayerStartY());
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
        frameCounter++;
        // Animate every 20 frames
        if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
            if ((frameCounter / 30) % 2 == 0) {
                setImage(scaledImgV1);
            } else {
                setImage(scaledImgV2);
            }
        } else {
            if ((frameCounter / 30) % 2 == 0) {
                setImage(scaledImgH1);
            } else {
                setImage(scaledImgH2);
            }
        }
        // Mode-aware movement
        if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
            // Horizontal movement for vertical mode
            x += dx;
            
            // Horizontal boundaries
            if (x <= 0) x = 0;
            if (x >= BOARD_WIDTH - PLAYER_WIDTH) x = BOARD_WIDTH - PLAYER_WIDTH;
        } else {
            // Vertical movement for horizontal mode (current)
            y += dx;
            
            // Vertical boundaries
            if (y <= 60) y = 60; // Below dashboard
            if (y >= BOARD_HEIGHT - PLAYER_HEIGHT) y = BOARD_HEIGHT - PLAYER_HEIGHT;
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
        
        if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
            // Horizontal movement for vertical mode
            if (key == KeyEvent.VK_LEFT) {
                dx = -currentSpeed;
            }
            if (key == KeyEvent.VK_RIGHT) {
                dx = +currentSpeed;
            }
        } else {
            // Vertical movement for horizontal mode (current)
            if (key == KeyEvent.VK_UP) {
                dx = -currentSpeed; // Move up
            }
            if (key == KeyEvent.VK_DOWN) {
                dx = currentSpeed; // Move down
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) {
                dx = 0;
            }
        } else {
            if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
                dx = 0;
            }
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
        autoFireCooldown = 8; // 8 frames between auto-shots (reasonable machinegun speed)
    }
    
    // UPDATED: Max shots based on current bullet count (for Scene 1)
    public int getMaxShots() {
        // During multishot, allow unlimited bullets (machinegun mode)
        if (hasMultishot()) {
            return 999; // Effectively unlimited
        }
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
    
    // NEW: Mode-aware shot spawn position
    public int getShotStartX() {
        return Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL ? 
            x + PLAYER_WIDTH/2 : x + PLAYER_WIDTH;
    }
    
    public int getShotStartY() {
        return Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL ? 
            y : y + PLAYER_HEIGHT/2;
    }
}