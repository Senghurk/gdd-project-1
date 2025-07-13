package gdd.sprite;

import static gdd.Global.*;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import javax.swing.ImageIcon;

public class Alien1 extends Enemy {

    private EnemyBomb bomb;

    public Alien1(int x, int y) {
        super(x, y);
        initEnemy(x, y); // Uncomment to enable bomb functionality
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
        this.x -= Math.abs(direction); // Move alien leftward instead of downward
    }

    public void act() {
        act(1); // Default direction for autonomous movement
    }

    public EnemyBomb getBomb() {
        return bomb;
    }

}
