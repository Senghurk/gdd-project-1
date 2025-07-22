package gdd.sprite;

import gdd.Global;
import static gdd.Global.*;
import javax.swing.ImageIcon;

public class Alien2 extends Enemy {

    private Missile missile;
    private int frameCounter = 0;
    private int initialY;
    private int initialX; // NEW: For vertical mode
    private double phaseOffset;
    private javax.swing.ImageIcon img1;
    private javax.swing.ImageIcon img2;
    private java.awt.Image scaledImg1;
    private java.awt.Image scaledImg2;
    private boolean hasGuaranteedFired = false; // Track if guaranteed shot has been fired

    public Alien2(int x, int y) {
        super(x, y);
        setCollisionBounds(35, 35); // Alien2 collision box - covers full sprite, slightly larger than Alien1
        this.initialY = y;
        this.initialX = x; // NEW: Store initial X for vertical mode
        this.phaseOffset = Math.random() * Math.PI * 2;
        initEnemy(x, y);
    }

    private void initEnemy(int x, int y) {
        this.x = x;
        this.y = y;
        missile = new Missile(x, y, 4); // Increased speed from 3 to 4
        img1 = new ImageIcon(IMG_ENEMY2_1);
        img2 = new ImageIcon(IMG_ENEMY2_2);
        scaledImg1 = img1.getImage().getScaledInstance(img1.getIconWidth() * (SCALE_FACTOR-1),
                img1.getIconHeight() * (SCALE_FACTOR-1),
                java.awt.Image.SCALE_SMOOTH);
        scaledImg2 = img2.getImage().getScaledInstance(img2.getIconWidth() * (SCALE_FACTOR-1),
                img2.getIconHeight() * (SCALE_FACTOR-1),
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImg1);
    }

    public void act(int direction) {
        frameCounter++;
        
        if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
            // Move downward with complex horizontal movement (spiral + curve)
            this.y += Math.abs(direction) * 1.5;
            double spiralOffset = Math.sin(frameCounter * 0.12 + phaseOffset) * 35;
            double curveOffset = Math.cos(frameCounter * 0.06) * 15;
            this.x = (int)(initialX + spiralOffset + curveOffset);
            
            // Keep within horizontal bounds
            if (this.x < 50) this.x = 50;
            if (this.x > BOARD_WIDTH - 100) this.x = BOARD_WIDTH - 100;
        } else {
            // Current horizontal movement with complex vertical patterns
            this.x -= Math.abs(direction) * 1.5;
            double spiralOffset = Math.sin(frameCounter * 0.12 + phaseOffset) * 35;
            double curveOffset = Math.cos(frameCounter * 0.06) * 15;
            this.y = (int)(initialY + spiralOffset + curveOffset);
            
            // Keep within vertical bounds
            if (this.y < 50) this.y = 50;
            if (this.y > BOARD_HEIGHT - 100) this.y = BOARD_HEIGHT - 100;
        }
        
        // Missile firing logic
        // Guaranteed missile at 2 seconds (120 frames at 60fps)
        if (frameCounter == 120 && !hasGuaranteedFired) {
            fireMissile();
            hasGuaranteedFired = true;
        }
        // After guaranteed shot, chance-based firing (roughly every 3-4 seconds)
        else if (hasGuaranteedFired && frameCounter % 60 == 0 && Math.random() < 0.3) {
            fireMissile();
        }
        
        // Animation: alternate every 20 frames
        if ((frameCounter / 20) % 2 == 0) {
            setImage(scaledImg1);
        } else {
            setImage(scaledImg2);
        }
    }
    
    private void fireMissile() {
        if (missile.isDestroyed()) {
            missile.setDestroyed(false);
            missile.x = this.x;
            missile.y = this.y;
        }
    }

    public void act() {
        act(1);
    }

    public Missile getMissile() {
        return missile;
    }
}