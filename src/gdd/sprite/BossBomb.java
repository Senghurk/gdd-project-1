package gdd.sprite;

import gdd.Global;
import static gdd.Global.*;
import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class BossBomb extends Sprite {
    
    private boolean destroyed = false;
    
    public BossBomb(int x, int y) {
        // Set collision bounds for boss bomb
        setCollisionBounds(25, 25);
        initBomb(x, y);
    }
    
    private void initBomb(int x, int y) {
        this.x = x;
        this.y = y;
        
        // Use mode-aware boss shot image
        String bossShot = (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) 
            ? Global.IMG_BOSS_SHOT_VERTICAL 
            : IMG_BOSS_SHOT;
        var ii = new ImageIcon(bossShot);
        
        // Check if image loaded properly
        if (ii.getIconWidth() <= 0 || ii.getIconHeight() <= 0) {
            System.err.println("Failed to load boss shot image: " + bossShot);
            // Use a fallback - create a simple colored rectangle
            BufferedImage fallbackImage = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = fallbackImage.createGraphics();
            g.setColor(Color.YELLOW);
            g.fillRect(0, 0, 20, 20);
            g.dispose();
            setImage(fallbackImage);
            return;
        }
        
        // Scale the image to use the global scaling factor
        var scaledImage = ii.getImage().getScaledInstance(ii.getIconWidth() * (SCALE_FACTOR-1),
                ii.getIconHeight() * (SCALE_FACTOR-1),
                java.awt.Image.SCALE_SMOOTH);
        
        // Apply color tinting for vertical mode only
        if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
            setImage(applyColorTint(scaledImage, Color.RED));
        } else {
            setImage(scaledImage);
        }
    }
    
    private BufferedImage applyColorTint(java.awt.Image originalImage, Color tintColor) {
        int width = originalImage.getWidth(null);
        int height = originalImage.getHeight(null);
        
        // Check for invalid dimensions
        if (width <= 0 || height <= 0) {
            System.err.println("Invalid image dimensions for color tint: " + width + "x" + height);
            // Return a fallback colored image
            BufferedImage fallbackImage = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = fallbackImage.createGraphics();
            g.setColor(tintColor);
            g.fillRect(0, 0, 20, 20);
            g.dispose();
            return fallbackImage;
        }
        
        BufferedImage tintedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = tintedImage.createGraphics();
        
        // Draw the original image
        g2d.drawImage(originalImage, 0, 0, null);
        
        // Apply the tint with multiply blend mode effect
        g2d.setColor(new Color(tintColor.getRed(), tintColor.getGreen(), tintColor.getBlue(), 100));
        g2d.fillRect(0, 0, width, height);
        
        g2d.dispose();
        return tintedImage;
    }
    
    @Override
    public void act() {
        // Mode-aware boss bomb movement - faster than regular bombs
        if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
            this.y += 3; // Move down toward player in vertical mode
            if (this.y > BOARD_HEIGHT + 50) {
                setDestroyed(true);
            }
        } else {
            this.x -= 3; // Move left toward player in horizontal mode
            if (this.x < -50) {
                setDestroyed(true);
            }
        }
    }
    
    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
        if (destroyed) {
            setVisible(false);
        }
    }
    
    public boolean isDestroyed() {
        return destroyed;
    }
} 