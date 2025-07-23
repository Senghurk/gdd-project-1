package gdd.sprite;

import static gdd.Global.*;
import javax.swing.ImageIcon;

public class Enemy extends Sprite {

    // private Bomb bomb;

    private java.awt.Image scaledImg1;
    private java.awt.Image scaledImg2;
    
    public Enemy(int x, int y) {
        // Set fixed collision bounds for enemies - smaller than visual size to prevent false collisions
        setCollisionBounds(20, 20); // Enemy collision box - smaller than visual
        initEnemy(x, y);
    }

    private void initEnemy(int x, int y) {

        this.x = x;
        this.y = y;

        // bomb = new Bomb(x, y);

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
        this.x -= Math.abs(direction); // Move leftward regardless of direction sign
    }

    public void act() {
        act(1); // Default direction for autonomous movement
    }
/* 
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
    }
*/
}
