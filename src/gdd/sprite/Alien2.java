package gdd.sprite;

import static gdd.Global.*;
import javax.swing.ImageIcon;

public class Alien2 extends Enemy {

    private EnemyBomb bomb;
    private int frameCounter = 0;
    private int initialY;
    private double phaseOffset;
    private javax.swing.ImageIcon img1;
    private javax.swing.ImageIcon img2;
    private java.awt.Image scaledImg1;
    private java.awt.Image scaledImg2;

    public Alien2(int x, int y) {
        super(x, y);
        setCollisionBounds(35, 35); // Alien2 collision box - covers full sprite, slightly larger than Alien1
        this.initialY = y;
        this.phaseOffset = Math.random() * Math.PI * 2;
        initEnemy(x, y);
    }

    private void initEnemy(int x, int y) {
        this.x = x;
        this.y = y;
        bomb = new EnemyBomb(x, y, 3);
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
        this.x -= Math.abs(direction) * 1.5;
        double spiralOffset = Math.sin(frameCounter * 0.12 + phaseOffset) * 35;
        double curveOffset = Math.cos(frameCounter * 0.06) * 15;
        this.y = (int)(initialY + spiralOffset + curveOffset);
        if (this.y < 50) this.y = 50;
        if (this.y > BOARD_HEIGHT - 100) this.y = BOARD_HEIGHT - 100;
        // Animation: alternate every 20 frames
        if ((frameCounter / 20) % 2 == 0) {
            setImage(scaledImg1);
        } else {
            setImage(scaledImg2);
        }
    }

    public void act() {
        act(1);
    }

    public EnemyBomb getBomb() {
        return bomb;
    }
}