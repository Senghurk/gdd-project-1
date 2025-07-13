package gdd.sprite;

import static gdd.Global.*;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import javax.swing.ImageIcon;

public class Alien2 extends Enemy {

    private EnemyBomb bomb;

    public Alien2(int x, int y) {
        super(x, y);
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
        this.x -= Math.abs(direction) * 2; // Move faster than Alien1
    }

    public void act() {
        act(1);
    }

    public EnemyBomb getBomb() {
        return bomb;
    }
}