package gdd.sprite;

import gdd.Global;
import static gdd.Global.*;
import javax.swing.ImageIcon;

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