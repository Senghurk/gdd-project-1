package gdd.sprite;

import static gdd.Global.*;
import javax.swing.ImageIcon;

public class Alien1 extends Enemy {

    private EnemyBomb bomb;
    private int frameCounter = 0;
    private int initialY;

    // Add static flag to indicate if we are in level 2
    public static boolean IS_LEVEL2 = false;
    private boolean hasShotBomb = false;

    public Alien1(int x, int y) {
        super(x, y);
        // Set fixed collision bounds for Alien1 - cover full sprite image
        setCollisionBounds(30, 30); // Alien1 collision box - covers full sprite
        this.initialY = y;
        initEnemy(x, y);
    }

    private void initEnemy(int x, int y) {

        this.x = x;
        this.y = y;

        bomb = new EnemyBomb(x, y, 2);

        var ii = new ImageIcon(IMG_ENEMY);

        // Scale the image to use the global scaling factor
        var scaledImage = ii.getImage().getScaledInstance(ii.getIconWidth() * SCALE_FACTOR,
                ii.getIconHeight() * SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }

    public void act(int direction) {
        frameCounter++;
        
        // Basic leftward movement
        this.x -= Math.abs(direction);
        
        // Add sine wave vertical movement (gentle bobbing)
        double sineOffset = Math.sin(frameCounter * 0.08) * 20; // Small amplitude
        this.y = (int)(initialY + sineOffset);
        
        // Keep within screen bounds
        if (this.y < 50) this.y = 50;
        if (this.y > BOARD_HEIGHT - 100) this.y = BOARD_HEIGHT - 100;
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
