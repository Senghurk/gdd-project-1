package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;
import javax.swing.ImageIcon;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


public class SpeedUp extends PowerUp {

    public SpeedUp(int x, int y) {
        super(x, y);
        // Set image
        ImageIcon ii = new ImageIcon(IMG_POWERUP_SPEEDUP);
        var scaledImage = ii.getImage().getScaledInstance(ii.getIconWidth() ,
                ii.getIconHeight() ,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }

    public void act() {
        // SpeedUp specific behavior for sideways gameplay
        // Move from right to left like enemies
        this.x -= 2; // Move left by 2 pixels each frame
        
        // Remove power-up if it goes off the left side of the screen
        if (this.x < -50) {
            this.die();
        }
    }

    // UPDATED: Modified to enable multiple shots capability
    public void upgrade(Player player) {
        // Enable multiple shots capability (main feature)
        player.enableMultipleShots();
        
        // Also apply speed boost as secondary benefit
        player.applySpeedBoost(SPEED_BOOST_AMOUNT);
        
        this.die(); // Remove the power-up after use
    }

}