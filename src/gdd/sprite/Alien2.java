package gdd.sprite;

import static gdd.Global.*;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import javax.swing.ImageIcon;

public class Alien2 extends Enemy {

    private Bomb bomb;

    public Alien2(int x, int y) {
        super(x, y);
        initEnemy(x, y);
    }

    private void initEnemy(int x, int y) {

        this.x = x;
        this.y = y;

        bomb = new Bomb(x, y);

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

    public Bomb getBomb() {
        return bomb;
    }

    public class Bomb extends Sprite {

        private boolean destroyed;

        public Bomb(int x, int y) {
            initBomb(x, y);
        }

        private void initBomb(int x, int y) {

            setDestroyed(true);

            this.x = x;
            this.y = y;

            var bombImg = "src/images/bomb.png";
            var ii = new ImageIcon(bombImg);
            
            setImage(ii.getImage());
        }

        public void setDestroyed(boolean destroyed) {
            this.destroyed = destroyed;
        }

        public boolean isDestroyed() {
            return destroyed;
        }

        public void act() {
            if (!destroyed) {
                this.x -= 3; // Faster bomb speed than Alien1
            }
        }

        public void act(int direction) {
            act();
        }
    }
}