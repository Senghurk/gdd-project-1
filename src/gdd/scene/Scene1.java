package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import gdd.Global;
import static gdd.Global.*;
import gdd.SoundEffectPlayer;
import gdd.SpawnDetails;
import gdd.powerup.PowerUp;
import gdd.sprite.Alien1;
import gdd.sprite.Alien2;
import gdd.sprite.Enemy;
import gdd.sprite.EnemyBomb;
import gdd.sprite.Explosion;
import gdd.sprite.Player;
import gdd.sprite.Shot;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Scene1 extends JPanel {

    private int frame = 0;
    private List<PowerUp> powerups;
    private List<Enemy> enemies;
    private List<Explosion> explosions;
    private List<Shot> shots;
    private Player player;

    private int direction = -1;
    private int deaths = 0;
    private int score = 0;
    private int lives = 3;
    private int gameTimeSeconds = 0;
    private int framesSinceLastSecond = 0;
    
    // Phase-based enemy behavior
    private int currentPhase = 1;

    private boolean inGame = true;
    private String message = "Game Over";

    private final Dimension d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
    private final Random randomizer = new Random();

    private Timer timer;
    private final Game game;

    private List<Star> stars = new ArrayList<>();

    private boolean gameOverSoundPlayed = false; // Flag to prevent multiple game over sounds

    // Star class for random background stars
    private static class Star {
        int x, y, size, speed;
        Color color;
        
        public Star(int x, int y, int size, int speed, Color color) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.speed = speed;
            this.color = color;
        }
    }

    private HashMap<Integer, SpawnDetails> spawnMap = new HashMap<>();
    private AudioPlayer audioPlayer;
    private SpawnManager spawnManager;

    public Scene1(Game game) {
        this.game = game;
        this.spawnManager = new SpawnManager(spawnMap, randomizer);
        loadSpawnDetails();
    }

    private void initAudio() {
        try {
            String filePath = "src/audio/scene1.wav";
            audioPlayer = new AudioPlayer(filePath);
            // Volume is already set to 50% in AudioPlayer constructor
            audioPlayer.play();
        } catch (Exception e) {
            System.err.println("Error initializing audio player: " + e.getMessage());
        }
    }

    private void loadSpawnDetails() {
        // Use SpawnManager to handle all spawning logic
        spawnManager.loadSpawnDetails();
    }
    
    public void start() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        requestFocusInWindow();
        setBackground(Color.black);

        timer = new Timer(16, new GameCycle()); // Exactly 16ms for 60fps (more precise)
        timer.start();

        gameInit();
        initAudio();
    }

    public void stop() {
        timer.stop();
        try {
            if (audioPlayer != null) {
                audioPlayer.stop();
            }
        } catch (Exception e) {
            System.err.println("Error closing audio player.");
        }
    }

    private void gameInit() {

        enemies = new ArrayList<>();
        powerups = new ArrayList<>();
        explosions = new ArrayList<>();
        shots = new ArrayList<>();

        // Initialize the star field for background
        initStarField();

        player = new Player();
    }

    private void drawStarField(Graphics g) {
        // Draw the scrolling star field (optimized)
        for (Star star : stars) {
            g.setColor(star.color);
            if (star.size == 1) {
                g.drawLine(star.x, star.y, star.x, star.y); // Faster for single pixels
            } else {
                g.fillOval(star.x, star.y, star.size, star.size);
            }
        }
    }

    private void drawAliens(Graphics g) {

        for (Enemy enemy : enemies) {

            if (enemy.isVisible()) {

                g.drawImage(enemy.getImage(), enemy.getX(), enemy.getY(), this);
            }

            if (enemy.isDying()) {

                enemy.die();
            }
        }
    }

    private void drawPowreUps(Graphics g) {

        for (PowerUp p : powerups) {

            if (p.isVisible()) {

                g.drawImage(p.getImage(), p.getX(), p.getY(), this);
            }

            if (p.isDying()) {

                p.die();
            }
        }
    }

    private void drawPlayer(Graphics g) {
        if (player.isVisible()) {
            // Blink effect during invincibility
            if (!player.isInvincible() || (frame % 8 < 4)) {
                g.drawImage(player.getImage(), player.getX(), player.getY(), this);
            }
        }

        if (player.isDying()) {

            player.die();
            inGame = false;

            // Play game over sound 
            if (!gameOverSoundPlayed) {
                SoundEffectPlayer.playGameOverSound();
                gameOverSoundPlayed = true;
            }
        }
    }

    private void drawShot(Graphics g) {

        for (Shot shot : shots) {

            if (shot.isVisible()) {
                g.drawImage(shot.getImage(), shot.getX(), shot.getY(), this);
            }
        }
    }

    private void drawBombing(Graphics g) {
        for (Enemy e : enemies) {
            EnemyBomb bomb = null;
            if (e instanceof Alien1) {
                bomb = ((Alien1) e).getBomb();
            } else if (e instanceof Alien2) {
                bomb = ((Alien2) e).getBomb();
            }
            
            if (bomb != null && !bomb.isDestroyed()) {
                g.drawImage(bomb.getImage(), bomb.getX(), bomb.getY(), this);
            }
        }
    }

    private void drawExplosions(Graphics g) {

        List<Explosion> toRemove = new ArrayList<>();

        for (Explosion explosion : explosions) {

            if (explosion.isVisible()) {
                g.drawImage(explosion.getImage(), explosion.getX(), explosion.getY(), this);
                explosion.visibleCountDown();
                if (!explosion.isVisible()) {
                    toRemove.add(explosion);
                }
            }
        }

        explosions.removeAll(toRemove);
    }

    private void drawLevelText(Graphics g) {
        // Only draw during first 3 seconds (180 frames)
        if (frame <= 180) {
            // Flicker every 20 frames
            if ((frame / 20) % 2 == 0) {
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 36));
                
                // Get text dimensions for centering
                String levelText = "Level 1";
                int textWidth = g.getFontMetrics().stringWidth(levelText);
                
                // Position text in upper middle, below dashboard
                g.drawString(levelText, (BOARD_WIDTH - textWidth) / 2, 100);
            }
        }
    }

    private void drawDashboard(Graphics g) {
        // Dashboard background
        g.setColor(new Color(0, 0, 0, 150)); // Semi-transparent black
        g.fillRect(0, 0, BOARD_WIDTH, 60);
        
        g.setColor(Color.white);
        g.setFont(g.getFont().deriveFont(14f));
        
        // Score
        g.drawString("Score: " + score, 10, 20);
        
        // Lives
        g.drawString("Lives: " + lives, 10, 40);
        
        // Time
        int minutes = gameTimeSeconds / 60;
        int seconds = gameTimeSeconds % 60;
        g.drawString(String.format("Time: %d:%02d", minutes, seconds), 150, 20);
        
        // Shots info
        int maxShots = player.getMaxShots();
        g.drawString("Shots: " + shots.size() + "/" + maxShots, 150, 40);
        
        // Show current powerup status
        g.setColor(Color.yellow);
        g.drawString("Bullets: " + player.getCurrentBulletCount() + "/" + player.getMaxBulletCount(), 300, 20);
        
        g.setColor(Color.cyan);
        g.drawString("Speed: " + player.getCurrentSpeed() + "/" + player.getMaxSpeed(), 300, 40);
        
        // Multishot status
        g.setColor(Color.white);
        if (player.hasMultishot()) {
            int remainingSeconds = player.getMultishotFramesRemaining() / 60;
            g.setColor(Color.yellow);
            g.drawString("🔥 MULTISHOT: " + remainingSeconds + "s", 450, 20);
            g.setColor(Color.red);
            g.drawString("AUTO-FIRE ACTIVE!", 450, 40);
        }
        
        // Phase information
        g.setColor(Color.orange);
        String phaseText = "Phase " + currentPhase;
        switch (currentPhase) {
            case 1:
                phaseText += " (Safe)";
                break;
            case 2:
                phaseText += " (Danger)";
                break;
            case 3:
                phaseText += " (WAR!)";
                break;
        }
        g.drawString(phaseText, 550, 20);
        
        // Wave information
        // The wave system is now managed by SpawnManager, so we don't need to draw wave info here.
        
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Enable rendering hints for better performance
        if (g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        }

        doDrawing(g);
    }

    private void doDrawing(Graphics g) {

        g.setColor(Color.black);
        g.fillRect(0, 0, d.width, d.height);

        g.setColor(Color.white);
        g.drawString("FRAME: " + frame, 10, 10);

        if (inGame) {

            drawStarField(g);  // Draw background stars first
            drawExplosions(g);
            drawPowreUps(g);
            drawAliens(g);
            drawPlayer(g);
            drawShot(g);
            drawBombing(g); // Draw enemy bombs
            drawDashboard(g); // Draw dashboard on top
            drawLevelText(g); // Draw level text on top

        } else {

            if (timer.isRunning()) {
                timer.stop();
            }

            gameOver(g);
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private void gameOver(Graphics g) {

        g.setColor(Color.black);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        g.setColor(new Color(0, 32, 48));
        g.fillRect(50, BOARD_WIDTH / 2 - 30, BOARD_WIDTH - 100, 50);
        g.setColor(Color.white);
        g.drawRect(50, BOARD_WIDTH / 2 - 30, BOARD_WIDTH - 100, 50);

        var small = new Font("Helvetica", Font.BOLD, 14);
        var fontMetrics = this.getFontMetrics(small);

        g.setColor(Color.white);
        g.setFont(small);
        g.drawString(message, (BOARD_WIDTH - fontMetrics.stringWidth(message)) / 2,
                BOARD_WIDTH / 2);

        // Add restart instruction
        g.setColor(Color.yellow);
        g.setFont(g.getFont().deriveFont(12f));
        String restartText = "Press R to Restart";
        int restartWidth = g.getFontMetrics().stringWidth(restartText);
        g.drawString(restartText, (BOARD_WIDTH - restartWidth) / 2, BOARD_WIDTH / 2 + 50);
    }

    private void update() {

        // Update game timer
        framesSinceLastSecond++;
        if (framesSinceLastSecond >= 60) { // 60 FPS
            gameTimeSeconds++;
            framesSinceLastSecond = 0;
        }
        
        // Update phase-based enemy behavior
        updateEnemyPhase();

        // Update background stars
        updateStarField();

        // Handle spawning through SpawnManager
        SpawnManager.SpawnResult spawnResult = spawnManager.spawnEnemies(frame, player);
        
        // Add spawned enemies to the game
        enemies.addAll(spawnResult.enemies);
        
        // Add spawned powerups to the game
        powerups.addAll(spawnResult.powerups);
        
        // Handle victory condition
        if (spawnResult.gameOver) {
            inGame = false;
            message = spawnResult.victoryMessage;
        }

        // Victory condition: Survive 5 minutes (300 seconds)
        if (gameTimeSeconds >= 300) {
            inGame = false;
            timer.stop();
            message = "Level 1 Complete!You survived 5 minutes!Press SPACE to continue to Level 2";
        }

        // player
        player.act();
        
        // Auto-fire for multishot powerup
        if (player.canAutoFire() && shots.size() < player.getMaxShots() - 2) {
            int x = player.getX();
            int y = player.getY();
            
            Shot autoShot = new Shot(x, y);
            shots.add(autoShot);

            SoundEffectPlayer.playShootSound(); // Play shooting sound
            
            // Create additional spread shot
            if (player.hasMultishot() && shots.size() < player.getMaxShots()) {
                Shot spreadShot = new Shot(x, y + 25);
                shots.add(spreadShot);
            }
            
            player.triggerAutoFire();
        }

        // Power-ups
        for (PowerUp powerup : powerups) {
            if (powerup.isVisible()) {
                powerup.act();
                if (powerup.collidesWith(player)) {

                    SoundEffectPlayer.playCatchPowerUpSound(); // Play power-up sound

                    powerup.upgrade(player);
                }
            }
        }

        // Enemies with mode-aware cleanup
        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                enemy.act(direction);
                
                // Mode-aware enemy cleanup when they go offscreen
                if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
                    // Vertical mode: remove enemies that fall off bottom
                    if (enemy.getY() > BOARD_HEIGHT + 50) {
                        enemy.die(); // Mark enemy as invisible so it gets cleaned up
                    }
                } else {
                    // Horizontal mode: remove enemies that go off left side (current behavior)
                    if (enemy.getX() < -50) { // Give some buffer for image width
                        enemy.die(); // Mark enemy as invisible so it gets cleaned up
                    }
                }
            }
        }

        // shot
        List<Shot> shotsToRemove = new ArrayList<>();
        for (Shot shot : shots) {

            if (shot.isVisible()) {
                shot.act(); // Update shot position using its movement logic
                
                for (Enemy enemy : enemies) {
                    // Use proper rectangle-to-rectangle collision detection
                    if (enemy.isVisible() && shot.isVisible() && shot.collidesWith(enemy)) {

                        var ii = new ImageIcon(IMG_EXPLOSION);
                        enemy.setImage(ii.getImage());
                        enemy.setDying(true);
                        explosions.add(new Explosion(enemy.getX(), enemy.getY()));

                        SoundEffectPlayer.playEnemyExplodeSound(); // Play enemy explosion sound

                        deaths++;
                        
                        // Add score based on enemy type
                        if (enemy instanceof Alien2) {
                            score += 200; // Alien2 worth more points
                        } else {
                            score += 100; // Alien1 base points
                        }
                        
                        shot.die();
                        shotsToRemove.add(shot);
                        break; // Exit inner loop since shot is destroyed
                    }
                }

                // Mode-aware shot cleanup when they go offscreen
                if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
                    // Vertical mode: remove shots that go off top
                    if (shot.getY() < -10) {
                        shot.die();
                        shotsToRemove.add(shot);
                    }
                } else {
                    // Horizontal mode: remove shots that go off right side (current behavior)
                    if (shot.getX() > BOARD_WIDTH) {
                        shot.die();
                        shotsToRemove.add(shot);
                    }
                }
            }
        }
        shots.removeAll(shotsToRemove);

        // Enemy bomb collision detection
        for (Enemy enemy : enemies) {

            // Phase-based shooting behavior
            boolean canShoot = false;
            int shootChance = 0;
            
            switch (currentPhase) {
                case 1:
                    // Phase 1: No shooting (0-90 seconds) - pure learning
                    canShoot = false;
                    break;
                case 2:
                    // Phase 2: Very occasional shooting (90-210 seconds) - introduce threat
                    canShoot = true;
                    shootChance = 1200; // Very rare shooting (~20 second intervals)
                    break;
                case 3:
                    // Phase 3: More frequent shooting (210-300 seconds) - escalation
                    canShoot = true;
                    shootChance = 600; // More frequent shooting (~10 second intervals)
                    break;
            }
            
            int chance = randomizer.nextInt(shootChance > 0 ? shootChance : 1000);
            
            // Both Alien1 and Alien2 can shoot bombs
            if (enemy instanceof Alien1) {
                Alien1 alien = (Alien1) enemy;
                EnemyBomb bomb = alien.getBomb();

                if (canShoot && chance == CHANCE && enemy.isVisible() && bomb.isDestroyed()) {

                    bomb.setDestroyed(false);
                    bomb.setX(enemy.getX());
                    bomb.setY(enemy.getY());
                }

                int bombX = bomb.getX();
                int bombY = bomb.getY();
                int playerX = player.getX();
                int playerY = player.getY();

                if (player.isVisible() && !player.isInvincible() && !bomb.isDestroyed()
                        && bombX >= (playerX)
                        && bombX <= (playerX + PLAYER_WIDTH)
                        && bombY >= (playerY)
                        && bombY <= (playerY + PLAYER_HEIGHT)) {

                    bomb.setDestroyed(true);
                    explosions.add(new Explosion(playerX, playerY));
                    player.takeDamage();
                    
                    SoundEffectPlayer.playPlayerHitSound();

                    // Decrement lives instead of instant death
                    lives--;
                    if (lives <= 0) {
                        var ii = new ImageIcon(IMG_EXPLOSION);
                        player.setImage(ii.getImage());
                        player.setDying(true);
                        inGame = false;
                        message = "Game Over!";

                    // Play game over sound 
                    if (!gameOverSoundPlayed) {
                        SoundEffectPlayer.playGameOverSound();
                        gameOverSoundPlayed = true;
            }
        }
    }

                if (!bomb.isDestroyed()) {
                    bomb.act();
                    // Remove bombs that go off the left side
                    if (bomb.getX() < 0) {
                        bomb.setDestroyed(true);
                    }
                }
            }
            
            // Handle Alien2 bombs
            if (enemy instanceof Alien2) {
                Alien2 alien = (Alien2) enemy;
                EnemyBomb bomb = alien.getBomb();

                if (canShoot && chance == CHANCE && enemy.isVisible() && bomb.isDestroyed()) {

                    bomb.setDestroyed(false);
                    bomb.setX(enemy.getX());
                    bomb.setY(enemy.getY());
                }

                int bombX = bomb.getX();
                int bombY = bomb.getY();
                int playerX = player.getX();
                int playerY = player.getY();

                if (player.isVisible() && !player.isInvincible() && !bomb.isDestroyed()
                        && bombX >= (playerX)
                        && bombX <= (playerX + PLAYER_WIDTH)
                        && bombY >= (playerY)
                        && bombY <= (playerY + PLAYER_HEIGHT)) {

                    bomb.setDestroyed(true);
                    explosions.add(new Explosion(playerX, playerY));
                    player.takeDamage();
                    
                    // Decrement lives instead of instant death
                    lives--;
                    if (lives <= 0) {
                        var ii = new ImageIcon(IMG_EXPLOSION);
                        player.setImage(ii.getImage());
                        player.setDying(true);
                        inGame = false;
                        message = "Game Over!";
                    }
                }

                if (!bomb.isDestroyed()) {
                    bomb.act();
                    // Remove bombs that go off the left side
                    if (bomb.getX() < 0) {
                        bomb.setDestroyed(true);
                    }
                }
            }
        }
        
        // Update and remove completed explosions
        for (Explosion explosion : explosions) {
            explosion.act();
        }
        explosions.removeIf(explosion -> !explosion.isVisible());
    }
    
    private void updateEnemyPhase() {
        // Phase 1: 0-90 seconds (no shooting)
        if (gameTimeSeconds < 90) {
            currentPhase = 1;
        }
        // Phase 2: 90-210 seconds (occasional shooting)
        else if (gameTimeSeconds < 210) {
            currentPhase = 2;
        }
        // Phase 3: 210-300 seconds (regular shooting every 5 seconds)
        else {
            currentPhase = 3;
        }
    }

    /**
     * Initialize star field background with mode-aware positioning
     */
    private void initStarField() {
        // Initialize random stars across the screen
        for (int i = 0; i < 100; i++) {
            int x, y, speed;
            
            if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
                // Vertical mode: stars spawn across width and fall downward
                x = randomizer.nextInt(BOARD_WIDTH);
                y = randomizer.nextInt(BOARD_HEIGHT + 200); // Some start above screen
                speed = 1 + randomizer.nextInt(3); // Variable speed 1-3 for vertical
            } else {
                // Horizontal mode: stars spawn across height and move leftward
                x = randomizer.nextInt(BOARD_WIDTH + 200); // Start some stars off-screen
                y = randomizer.nextInt(BOARD_HEIGHT);
                speed = 1; // Constant speed for horizontal
            }
            
            int size = randomizer.nextInt(3) + 1; // Stars size 1-3
            
            // Create different colored stars
            Color color = getRandomStarColor();
            
            stars.add(new Star(x, y, size, speed, color));
        }
    }
    
    /**
     * Generate random star colors following the established distribution
     */
    private Color getRandomStarColor() {
        int colorChoice = randomizer.nextInt(10);
        if (colorChoice < 7) {
            return Color.WHITE;
        } else if (colorChoice < 9) {
            return new Color(200, 200, 255); // Light blue
        } else {
            return new Color(255, 255, 200); // Light yellow
        }
    }

    /**
     * Update star field movement with mode-aware direction
     */
    private void updateStarField() {
        for (Star star : stars) {
            if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
                // Vertical mode: stars fall downward
                star.y += star.speed;
                
                // If star goes off the bottom, respawn it at the top
                if (star.y > BOARD_HEIGHT + 10) {
                    star.y = -10;
                    star.x = randomizer.nextInt(BOARD_WIDTH);
                }
            } else {
                // Horizontal mode: stars move leftward (current behavior)
                star.x -= star.speed;
                
                // If star goes off the left side, respawn it on the right side
                if (star.x < -10) {
                    star.x = BOARD_WIDTH + randomizer.nextInt(100);
                    star.y = randomizer.nextInt(BOARD_HEIGHT);
                }
            }
        }
        
        // Occasionally add new stars with mode-aware positioning
        if (randomizer.nextInt(20) == 0) {
            int x, y, size, speed;
            
            if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
                // Vertical mode: spawn from top
                x = randomizer.nextInt(BOARD_WIDTH);
                y = -10;
                size = randomizer.nextInt(3) + 1;
                speed = 1 + randomizer.nextInt(3); // Variable speed for vertical
            } else {
                // Horizontal mode: spawn from right
                x = BOARD_WIDTH + 10;
                y = randomizer.nextInt(BOARD_HEIGHT);
                size = randomizer.nextInt(3) + 1;
                speed = 1; // Constant speed for horizontal
            }
            
            Color color = getRandomStarColor();
            stars.add(new Star(x, y, size, speed, color));
        }
        
        // Remove excess stars to prevent memory issues
        if (stars.size() > 150) {
            stars.remove(0);
        }
    }

    private void restartGame() {
        // Reset game state
        inGame = true;
        deaths = 0;
        frame = 0;
        message = "Game Over";
        gameOverSoundPlayed = false; // Reset game over sound flag
        
        // Reset phase system
        currentPhase = 1;
        gameTimeSeconds = 0;
        framesSinceLastSecond = 0;
        score = 0;
        lives = 3;

        // Clear all game objects
        enemies.clear();
        powerups.clear();
        explosions.clear();
        shots.clear();
        stars.clear();

        // Reinitialize everything
        gameInit();

        // Restart timer if it was stopped
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    private class GameCycle implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            doGameCycle();
        }
    }

    private void doGameCycle() {
        update();
        repaint();
        frame++;
    }

    private class TAdapter extends KeyAdapter {
        @Override
        public void keyReleased(KeyEvent e) {
            // Only handle player key releases when game is running
            if (inGame) {
                player.keyReleased(e);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            // Handle game over actions
            if (!inGame) {
                if (key == KeyEvent.VK_R) {
                    restartGame();
                    return;
                } else if (key == KeyEvent.VK_SPACE && message.contains("Level 2")) {
                    game.loadScene2(); // Move to Scene2
                    return;
                }
            }

            // Only handle player controls when game is running
            if (inGame) {
                player.keyPressed(e);

                int x = player.getX();
                int y = player.getY();

                // One bullet per space press, but more bullets can be on screen
                if (key == KeyEvent.VK_SPACE) {
                    int maxShots = player.getMaxShots();
                    if (shots.size() < maxShots) {
                        Shot shot = new Shot(x, y);
                        shots.add(shot);

                        SoundEffectPlayer.playShootSound(); // Play shooting sound

                    }
                }
            }
        }
    }
}