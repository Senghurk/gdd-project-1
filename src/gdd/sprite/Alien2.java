package gdd.sprite;

import static gdd.Global.*;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import javax.swing.ImageIcon;

public class Alien2 extends Enemy {

    private EnemyBomb bomb;
    private int frameCounter = 0;
    private int initialY;
    private double phaseOffset;

    public Alien2(int x, int y) {
        super(x, y);
        this.initialY = y;
        this.phaseOffset = Math.random() * Math.PI * 2; // Random starting phase
        initEnemy(x, y);
    }

    private void initEnemy(int x, int y) {

        this.x = x;
        this.y = y;

        bomb = new EnemyBomb(x, y, 3);

        var ii = new ImageIcon(IMG_ENEMY2);

        var scaledImage = ii.getImage().getScaledInstance(ii.getIconWidth() * SCALE_FACTOR,
                ii.getIconHeight() * SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }

    public void act(int direction) {
        frameCounter++;
        
        // Faster leftward movement than Alien1
        this.x -= Math.abs(direction) * 2;
        
        // More aggressive spiral/curved movement
        double spiralOffset = Math.sin(frameCounter * 0.12 + phaseOffset) * 35; // Larger amplitude
        double curveOffset = Math.cos(frameCounter * 0.06) * 15; // Secondary curve
        this.y = (int)(initialY + spiralOffset + curveOffset);
        
        // Keep within screen bounds
        if (this.y < 50) this.y = 50;
        if (this.y > BOARD_HEIGHT - 100) this.y = BOARD_HEIGHT - 100;
    }

    public void act() {
        act(1);
    }

    public EnemyBomb getBomb() {
        return bomb;
    }
}