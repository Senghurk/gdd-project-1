package gdd.sprite;

import gdd.Global;
import static gdd.Global.*;
import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class EnemyBomb extends Sprite {
    
    private boolean destroyed;
    private int speed;

    public EnemyBomb(int x, int y, int speed) {
        // Set fixed collision bounds for enemy bombs
        setCollisionBounds(8, 8); // Small collision box for bombs
        initBomb(x, y, speed);
    }
    
    private void initBomb(int x, int y, int speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.destroyed = true;
        
        // Use appropriate bomb image based on game mode
        var bombImg = (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) 
            ? Global.IMG_BOMB_VERTICAL 
            : Global.IMG_BOMB;
        var ii = new ImageIcon(bombImg);
        
        // Apply bright color tinting for better contrast in both modes
        if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
            setImage(applyColorTint(ii.getImage(), Color.YELLOW)); // Bright yellow for vertical
        } else {
            setImage(applyColorTint(ii.getImage(), Color.RED)); // Bright red for horizontal
        }
    }
    
    private BufferedImage applyColorTint(java.awt.Image originalImage, Color tintColor) {
        int width = originalImage.getWidth(null);
        int height = originalImage.getHeight(null);
        
        BufferedImage tintedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = tintedImage.createGraphics();
        
        // Draw the original image
        g2d.drawImage(originalImage, 0, 0, null);
        
        // Apply the tint with multiply blend mode effect - stronger contrast
        g2d.setColor(new Color(tintColor.getRed(), tintColor.getGreen(), tintColor.getBlue(), 180));
        g2d.fillRect(0, 0, width, height);
        
        g2d.dispose();
        return tintedImage;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void act() {
        if (!destroyed) {
            // Mode-aware bomb movement
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