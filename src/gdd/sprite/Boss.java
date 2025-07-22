package gdd.sprite;

import gdd.Global;
import static gdd.Global.*;
import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.List;

public class Boss extends Sprite {
    
    private int health;
    private int maxHealth;
    private int imageSwitchCounter = 0;
    private boolean usingBoss1 = true;
    private int shotCounter = 0;
    private static final int SHOT_INTERVAL = 200; // over 3 seconds at 60 FPS
    
    // Movement variables for up/down motion
    private int moveDirection = 1; // 1 for down, -1 for up
    private int moveCounter = 0;
    private static final int MOVE_INTERVAL = 60; // Change direction every 1 second
    private static final int MOVE_SPEED = 2; // Pixels per frame
    
    // Calculate boss health based on player's bullet capacity
    // Player has 5 bullets, can fire continuously for 1 minute
    // At 60 FPS, that's 3600 frames, so approximately 3600 shots
    // But we'll make it more reasonable - about 300 shots to kill the boss
    private static final int BOSS_HEALTH = 150;
    
    public Boss(int x, int y) {
        // Set collision bounds for boss - larger than regular enemies
        setCollisionBounds(100, 100); // Boss collision box
        this.maxHealth = BOSS_HEALTH;
        this.health = maxHealth;
        initBoss(x, y);
    }
    
    private void initBoss(int x, int y) {
        this.x = x;
        this.y = y;
        
        // Start with boss1.png
        loadBossImage(true);
    }
    
    private void loadBossImage(boolean useBoss1) {
        String imagePath = useBoss1 ? IMG_BOSS1 : IMG_BOSS2;
        var ii = new ImageIcon(imagePath);
        
        // Scale the image to use the global scaling factor
        var scaledImage = ii.getImage().getScaledInstance(ii.getIconWidth() * SCALE_FACTOR,
                ii.getIconHeight() * SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }
    
    @Override
    public void act() {
        // Switch images every 30 frames (0.5 seconds) for animation effect
        imageSwitchCounter++;
        if (imageSwitchCounter >= 30) {
            imageSwitchCounter = 0;
            usingBoss1 = !usingBoss1;
            loadBossImage(usingBoss1);
        }
        
        // Mode-aware boss movement
        moveCounter++;
        if (moveCounter >= MOVE_INTERVAL) {
            moveCounter = 0;
            moveDirection *= -1; // Reverse direction
        }
        
        if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
            // Vertical mode: boss moves left and right at top of screen
            this.x += moveDirection * MOVE_SPEED;
            
            // Keep boss within screen bounds horizontally
            if (this.x < 50) {
                this.x = 50;
                moveDirection = 1; // Start moving right
            } else if (this.x > BOARD_WIDTH - 150) {
                this.x = BOARD_WIDTH - 150;
                moveDirection = -1; // Start moving left
            }
            
            // Keep boss near the top (move down slowly toward battle position)
            if (this.y < 100) {
                this.y += 1; // Slowly enter the screen
            }
        } else {
            // Horizontal mode: boss moves up and down near the right margin
            this.y += moveDirection * MOVE_SPEED;
            
            // Keep boss within screen bounds vertically
            if (this.y < 50) {
                this.y = 50;
                moveDirection = 1; // Start moving down
            } else if (this.y > BOARD_HEIGHT - 150) {
                this.y = BOARD_HEIGHT - 150;
                moveDirection = -1; // Start moving up
            }
            
            // Keep boss near the right margin (don't move left)
            if (this.x < BOARD_WIDTH - 150) {
                this.x = BOARD_WIDTH - 150;
            }
        }
        
        // Shooting logic
        shotCounter++;
    }
    
    public boolean shouldShoot() {
        if (shotCounter >= SHOT_INTERVAL) {
            shotCounter = 0;
            return true; // Signal to shoot
        }
        return false; // Don't shoot
    }
    
    public List<BossBomb> shoot() {
        List<BossBomb> bombs = new ArrayList<>();
        
        if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
            // Vertical mode: 3-way horizontal spread downward
            bombs.add(new BossBomb(this.x + 40, this.y + 100)); // center down
            bombs.add(new BossBomb(this.x + 10, this.y + 100)); // left down
            bombs.add(new BossBomb(this.x + 70, this.y + 100)); // right down
        } else {
            // Horizontal mode: 3-way vertical spread leftward
            bombs.add(new BossBomb(this.x, this.y + 40)); // center
            bombs.add(new BossBomb(this.x, this.y + 10)); // up
            bombs.add(new BossBomb(this.x, this.y + 70)); // down
        }
        
        return bombs;
    }
    
    public void takeDamage() {
        health--;
        if (health <= 0) {
            setDying(true);
        }
    }
    
    public int getHealth() {
        return health;
    }
    
    public int getMaxHealth() {
        return maxHealth;
    }
    
    public double getHealthPercentage() {
        return (double) health / maxHealth;
    }
    
    public boolean isDead() {
        return health <= 0;
    }
} 