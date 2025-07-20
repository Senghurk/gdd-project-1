package gdd.sprite;

import gdd.Global;
import static gdd.Global.*;
import javax.swing.ImageIcon;

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
        
        var ii = new ImageIcon(IMG_BOSS_SHOT);
        
        // Scale the image to use the global scaling factor
        var scaledImage = ii.getImage().getScaledInstance(ii.getIconWidth() * (SCALE_FACTOR-1),
                ii.getIconHeight() * (SCALE_FACTOR-1),
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
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