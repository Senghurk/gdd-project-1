package gdd.sprite;

import gdd.Global;
import static gdd.Global.*;
import javax.swing.ImageIcon;
import java.awt.Image;

public class Missile extends Sprite {
    
    private boolean destroyed;
    private int speed;

    public Missile(int x, int y, int speed) {
        setCollisionBounds(10, 12); // Slightly larger than bomb collision
        initMissile(x, y, speed);
    }
    
    private void initMissile(int x, int y, int speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.destroyed = true;
        
        var missileImg = new ImageIcon(Global.IMG_MISSILE);
        Image scaledImage;
        
        if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
            // Vertical mode: missile is already in correct orientation
            scaledImage = missileImg.getImage();
        } else {
            // Horizontal mode: rotate missile 90 degrees clockwise and resize
            int originalWidth = missileImg.getIconWidth();
            int originalHeight = missileImg.getIconHeight();
            
            // Create rotated image (90 degrees clockwise)
            java.awt.image.BufferedImage rotatedImage = new java.awt.image.BufferedImage(
                originalHeight, originalWidth, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g2d = rotatedImage.createGraphics();
            g2d.translate(originalHeight/2.0, originalWidth/2.0);
            g2d.rotate(Math.PI/2);
            g2d.translate(-originalWidth/2.0, -originalHeight/2.0);
            g2d.drawImage(missileImg.getImage(), 0, 0, null);
            g2d.dispose();
            
            scaledImage = rotatedImage;
        }
        
        setImage(scaledImage);
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void act() {
        if (!destroyed) {
            if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
                this.y += speed; // Move down toward player in vertical mode
                if (this.y > BOARD_HEIGHT + 50) {
                    setDestroyed(true);
                }
            } else {
                this.x -= speed; // Move left toward player in horizontal mode
                if (this.x < -50) {
                    setDestroyed(true);
                }
            }
        }
    }

    public void act(int direction) {
        act();
    }
}