package gdd.sprite;

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
            this.x -= speed; // Move bomb leftward when active
        }
    }

    public void act(int direction) {
        act();
    }
}