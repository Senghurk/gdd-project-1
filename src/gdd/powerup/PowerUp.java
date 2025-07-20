/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package gdd.powerup;

import gdd.Global;
import gdd.sprite.Player;
import gdd.sprite.Sprite;


abstract public class PowerUp extends Sprite {
    PowerUp(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void act() {
        // Mode-aware powerup movement
        if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
            // Vertical mode: powerups fall downward
            this.y += 2; // Move down by 2 pixels each frame
            
            // Remove power-up if it goes off the bottom of the screen
            if (this.y > Global.BOARD_HEIGHT + 50) {
                this.die();
            }
        } else {
            // Horizontal mode: powerups move leftward (current behavior)
            this.x -= 2; // Move left by 2 pixels each frame
            
            // Remove power-up if it goes off the left side of the screen
            if (this.x < -50) {
                this.die();
            }
        }
    }

    abstract public void upgrade(Player player);
}
