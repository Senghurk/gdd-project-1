package gdd.sprite;

import gdd.Global;
import static gdd.Global.*;
import javax.swing.ImageIcon;
import java.awt.Image;

public class Missile extends Sprite {
    
    private boolean destroyed;
    private double speed; // Changed to double for smooth acceleration
    private double acceleration = 0.15; // Acceleration per frame
    private double maxSpeed = 8.0; // Maximum speed
    private MissileParticleEffect particleEffect;
    private int frameCount = 0; // Track frames for particle generation

    public Missile(int x, int y, int speed) {
        setCollisionBounds(10, 12); // Slightly larger than bomb collision
        initMissile(x, y, speed);
    }
    
    private void initMissile(int x, int y, int initialSpeed) {
        this.x = x;
        this.y = y;
        this.speed = 1.0; // Start slow regardless of initialSpeed parameter
        this.destroyed = true;
        this.particleEffect = new MissileParticleEffect();
        this.frameCount = 0;
        
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
        if (!destroyed) {
            // Reset speed and frame count when missile becomes active
            this.speed = 1.0;
            this.frameCount = 0;
        }
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void act() {
        if (!destroyed) {
            frameCount++;
            
            // Accelerate missile - starts slow, speeds up
            if (speed < maxSpeed) {
                speed += acceleration;
                if (speed > maxSpeed) {
                    speed = maxSpeed;
                }
            }
            
            // Generate particles continuously - less frequent for subtle effect
            double speedRatio = speed / maxSpeed;
            int particleFrequency = Math.max(3, (int)(8 - 5 * speedRatio)); // Every 8 frames when slow, every 3 frames when fast
            
            if (frameCount % particleFrequency == 0) {
                particleEffect.createMissileExhaust(this.x + getCollisionWidth()/2, this.y + getCollisionHeight()/2, speedRatio);
            }
            
            // Move missile
            if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
                this.y += (int)speed; // Move down toward player in vertical mode
                if (this.y > BOARD_HEIGHT + 50) {
                    setDestroyed(true);
                }
            } else {
                this.x -= (int)speed; // Move left toward player in horizontal mode
                if (this.x < -50) {
                    setDestroyed(true);
                }
            }
            
            // Update particle effects only when missile is active
            particleEffect.update();
        } else {
            // When missile is destroyed, clear all particles immediately
            particleEffect.clearAllParticles();
        }
    }

    public void act(int direction) {
        act();
    }
    
    // Method to draw particle effects
    public void drawParticleEffects(java.awt.Graphics g) {
        particleEffect.draw(g);
    }
    
    // Method to get particle effect object (for external drawing)
    public MissileParticleEffect getParticleEffect() {
        return particleEffect;
    }
    
    // Get current speed for debugging
    public double getCurrentSpeed() {
        return speed;
    }
}