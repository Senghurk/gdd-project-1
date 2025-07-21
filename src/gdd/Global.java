package gdd;

import java.util.Random;

public class Global {
    private Global() {
        // Prevent instantiation
    }

    public static final int SCALE_FACTOR = 3; // Scaling factor for sprites
    
    // NEW: Game mode constants
    public static final int MODE_HORIZONTAL = 0;
    public static final int MODE_VERTICAL = 1;
    public static int CURRENT_GAME_MODE = MODE_HORIZONTAL; // Default horizontal
    
    // TESTING MODE - set to true for quick testing (boss spawns at 10 seconds)
    public static final boolean TESTING_MODE = true;
    
    private static final Random modeRandom = new Random();

    public static final int BOARD_WIDTH = 716; // Doubled from 358
    public static final int BOARD_HEIGHT = 700; // Doubled from 350
    public static final int BORDER_RIGHT = 60; // Doubled from 30
    public static final int BORDER_LEFT = 10; // Doubled from 5

    public static final int GROUND = 580; // Doubled from 290
    public static final int BOMB_HEIGHT = 10; // Doubled from 5

    public static final int ALIEN_HEIGHT = 24; // Doubled from 12
    public static final int ALIEN_WIDTH = 24; // Doubled from 12
    public static final int ALIEN_INIT_X = 300; // Doubled from 150
    public static final int ALIEN_INIT_Y = 10; // Doubled from 5
    public static final int ALIEN_GAP = 30; // Gap between aliens

    public static final int GO_DOWN = 30; // Doubled from 15
    public static final int NUMBER_OF_ALIENS_TO_DESTROY = 24;
    public static final int CHANCE = 5;
    public static final int DELAY = 17;
    public static final int PLAYER_WIDTH = 30; // Doubled from 15
    public static final int PLAYER_HEIGHT = 20; // Doubled from 10

    // Images
    public static final String IMG_ENEMY = "src/images/alien.png";
    public static final String IMG_ENEMY2_1 = "src/images/alien2-1.png";
    public static final String IMG_ENEMY2_2 = "src/images/alien2-2.png";
    public static final String IMG_PLAYER = "src/images/player.png";
    public static final String IMG_SHOT = "src/images/shot.png";
    public static final String IMG_EXPLOSION = "src/images/explosion.png";
    public static final String IMG_TITLE = "src/images/title.png";
    public static final String IMG_POWERUP_SPEEDUP = "src/images/powerup-s.png";
    public static final String IMG_POWERUP_ADDBULLET = "src/images/powerup_bullet.png";
    public static final String IMG_POWERUP_MULTISHOT = "src/images/powerup_autoshot.png";
    public static final String IMG_POWERUP_HEALTH = "src/images/health-pickup.png";
    public static final String IMG_BOSS1 = "src/images/boss1.png";
    public static final String IMG_BOSS2 = "src/images/boss2.png";
    public static final String IMG_BOSS_SHOT = "src/images/boss_shot.png";
    public static final String IMG_BOSS_SHOT_VERTICAL = "src/images/boss_shot-vertical.png";
    public static final String IMG_BOMB = "src/images/bomb.png";
    public static final String IMG_BOMB_VERTICAL = "src/images/bomb-vertical.png";
    
    // Power-up constants (rebalanced for better gameplay)
    public static final int MULTISHOT_DURATION_FRAMES = 600; // 10 seconds at 60 FPS
    public static final int MULTISHOT_EXTRA_SHOTS = 3; // Additional shots per fire (4 total shots)
    
    // Speed PowerUp constants
    public static final int INITIAL_PLAYER_SPEED = 3;
    public static final int SPEED_BOOST_AMOUNT = 2; // Speed increase per powerup
    public static final int MAX_PLAYER_SPEED = 11; // Maximum speed (3 + 4*2)
    public static final int TOTAL_SPEED_POWERUPS = 4; // Total speed powerups in game
    
    // Bullet Count PowerUp constants
    public static final int INITIAL_BULLET_COUNT = 1;
    public static final int BULLET_COUNT_INCREASE = 1; // Bullet increase per powerup
    public static final int MAX_BULLET_COUNT = 5; // Maximum bullets (1 + 4*1)
    public static final int TOTAL_BULLET_POWERUPS = 4; // Total bullet powerups in game
    
    // NEW: Mode-specific helper methods
    public static int getEnemySpawnX() {
        return CURRENT_GAME_MODE == MODE_VERTICAL ? 
            100 + modeRandom.nextInt(BOARD_WIDTH - 200) : BOARD_WIDTH;
    }
    
    public static int getEnemySpawnY() {
        return CURRENT_GAME_MODE == MODE_VERTICAL ? -50 : 
            100 + modeRandom.nextInt(BOARD_HEIGHT - 200);
    }
    
    public static int getPlayerStartX() {
        return CURRENT_GAME_MODE == MODE_VERTICAL ? BOARD_WIDTH/2 : 50;
    }
    
    public static int getPlayerStartY() {
        return CURRENT_GAME_MODE == MODE_VERTICAL ? BOARD_HEIGHT - 100 : BOARD_HEIGHT/2;
    }
}