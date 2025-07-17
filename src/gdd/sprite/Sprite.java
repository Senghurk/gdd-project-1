package gdd.sprite;

import java.awt.Image;
import java.awt.Rectangle;

abstract public class Sprite {

    protected boolean visible;
    protected Image image;
    protected boolean dying;
    protected int visibleFrames = 10;

    protected int x;
    protected int y;
    protected int dx;
    
    // Fixed collision bounds - override in subclasses
    protected int collisionWidth = 20;
    protected int collisionHeight = 20;

    public Sprite() {
        visible = true;
    }

    abstract public void act();

    public boolean collidesWith(Sprite other) {
        if (other == null || !this.isVisible() || !other.isVisible()) {
            return false;
        }
        
        // Use fixed collision bounds instead of image dimensions
        Rectangle thisRect = new Rectangle(this.x, this.y, this.collisionWidth, this.collisionHeight);
        Rectangle otherRect = new Rectangle(other.x, other.y, other.collisionWidth, other.collisionHeight);
        
        return thisRect.intersects(otherRect);
    }
    
    // Getters for collision bounds
    public int getCollisionWidth() {
        return collisionWidth;
    }
    
    public int getCollisionHeight() {
        return collisionHeight;
    }
    
    // Setters for collision bounds
    protected void setCollisionBounds(int width, int height) {
        this.collisionWidth = width;
        this.collisionHeight = height;
    }

    public void die() {
        visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public void visibleCountDown() {
        if (visibleFrames > 0) {
            visibleFrames--;
        } else {
            visible = false;
        }
    }

    protected void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Image getImage() {
        return image;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public void setDying(boolean dying) {
        this.dying = dying;
    }

    public boolean isDying() {
        return this.dying;
    }
}
