package gdd.sprite;

import gdd.Global;
import static gdd.Global.*;
import javax.swing.ImageIcon;

public class Alien1 extends Enemy {

    private EnemyBomb bomb;
    private int frameCounter = 0;
    private int initialY;
    private int initialX; // NEW: For vertical mode
    private java.awt.Image scaledImg1;
    private java.awt.Image scaledImg2;

    // Add static flag to indicate if we are in level 2
    public static boolean IS_LEVEL2 = false;
    private boolean hasShotBomb = false;

    public Alien1(int x, int y) {
        super(x, y);
        // Set fixed collision bounds for Alien1 - cover full sprite image
        setCollisionBounds(30, 30); // Alien1 collision box - covers full sprite
        this.initialY = y;
        this.initialX = x; // NEW: Store initial X for vertical mode
        initEnemy(x, y);
    }

    private void initEnemy(int x, int y) {
        this.x = x;
        this.y = y;

        bomb = new EnemyBomb(x, y, 2);

        // Load both animation frames
        var ii1 = new ImageIcon(IMG_ENEMY1_1);
        var ii2 = new ImageIcon(IMG_ENEMY1_2);

        // Scale both images
        scaledImg1 = ii1.getImage().getScaledInstance(ii1.getIconWidth() * SCALE_FACTOR,
                ii1.getIconHeight() * SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH);
        scaledImg2 = ii2.getImage().getScaledInstance(ii2.getIconWidth() * SCALE_FACTOR,
                ii2.getIconHeight() * SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH);


        // Set initial image
        setImage(scaledImg1);
    }

    public void act(int direction) {
        frameCounter++;
        
        if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
            // Move downward with horizontal sine wave
            this.y += Math.abs(direction);
            double sineOffset = Math.sin(frameCounter * 0.08) * 20;
            this.x = (int)(initialX + sineOffset);
            
            // Keep within horizontal bounds
            if (this.x < 50) this.x = 50;
            if (this.x > BOARD_WIDTH - 100) this.x = BOARD_WIDTH - 100;
        } else {
            // Current horizontal movement
            this.x -= Math.abs(direction);
            double sineOffset = Math.sin(frameCounter * 0.08) * 20;
            this.y = (int)(initialY + sineOffset);
            
            // Keep within vertical bounds
            if (this.y < 50) this.y = 50;
            if (this.y > BOARD_HEIGHT - 100) this.y = BOARD_HEIGHT - 100;
        }

        // Animate sprite every 20 frames
        if ((frameCounter / 20) % 2 == 0) {
            setImage(scaledImg1);
        } else {
            setImage(scaledImg2);
        }

        // Level 2: shoot one bomb if not already shot
        if (IS_LEVEL2 && !hasShotBomb && bomb.isDestroyed() && this.x < BOARD_WIDTH - 200) {
            bomb.setDestroyed(false);
            bomb.setX(this.x);
            bomb.setY(this.y);
            hasShotBomb = true;
        }
        // Move bomb if active
        bomb.act();
    }


    public EnemyBomb getBomb() {
        return bomb;
    }

}
