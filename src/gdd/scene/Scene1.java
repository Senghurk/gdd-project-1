package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import gdd.Global;
import static gdd.Global.*;
import gdd.SoundEffectPlayer;
import gdd.SpawnDetails;
import gdd.powerup.PowerUp;
import gdd.powerup.HealthPickup;
import gdd.sprite.Alien1;
import gdd.sprite.Alien2;
import gdd.sprite.Enemy;
import gdd.sprite.EnemyBomb;
import gdd.sprite.Missile;
import gdd.sprite.Explosion;
import gdd.sprite.Player;
import gdd.sprite.Shot;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
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
    private List<EnemyBomb> bombs; // NEW: Central bomb list

    public Scene1(Game game) {
        this.game = game;
        this.spawnManager = new SpawnManager(spawnMap, randomizer);
        loadSpawnDetails();
    }

    private void initAudio() {
        try {
            String filePath = "src/audio/new_ost/stage1.wav";
            System.out.println("Scene1: Loading audio from: " + filePath);
            audioPlayer = new AudioPlayer(filePath);
            System.out.println("Scene1: AudioPlayer created successfully");
            audioPlayer.play();
            System.out.println("Scene1: Audio started playing");
        } catch (Exception e) {
            System.err.println("Scene1: Error initializing audio player: " + e.getMessage());
            e.printStackTrace();
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
    
    // NEW: Method to get player data for Scene2 transition
    public Player getPlayer() {
        return player;
    }

    private void gameInit() {

        enemies = new ArrayList<>();
        powerups = new ArrayList<>();
        explosions = new ArrayList<>();
        shots = new ArrayList<>();
        bombs = new ArrayList<>(); // NEW: Initialize bomb list

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
            handleGameOverSound();
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
        // Draw all bombs in the central list
        for (EnemyBomb bomb : bombs) {
            if (!bomb.isDestroyed()) {
                g.drawImage(bomb.getImage(), bomb.getX(), bomb.getY(), this);
            }
        }
        // Draw Alien2 missiles as before
        for (Enemy e : enemies) {
            if (e instanceof Alien2) {
                Missile missile = ((Alien2) e).getMissile();
                if (missile != null) {
                    missile.drawParticleEffects(g);
                    if (!missile.isDestroyed()) {
                        g.drawImage(missile.getImage(), missile.getX(), missile.getY(), this);
                    }
                }
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
        // Modern dashboard background with gradient effect
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Main dashboard background
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(0, 0, BOARD_WIDTH, 80, 0, 0);
        
        // Top accent line
        g2d.setColor(new Color(0, 150, 255, 200));
        g2d.fillRect(0, 0, BOARD_WIDTH, 3);
        
        // === LEFT SECTION: Core Stats ===
        drawStatsSection(g2d, 15, 15);
        
        // === CENTER SECTION: Progress Bars ===
        drawProgressSection(g2d, 220, 15);
        
        // === RIGHT SECTION: Status/Powerups ===
        drawStatusSection(g2d, 450, 15);
        
        // === BOTTOM SECTION: Multishot Status ===
        if (player.hasMultishot()) {
            drawMultishotStatus(g2d);
        }
    }
    
    private void drawStatsSection(Graphics2D g2d, int x, int y) {
        Font boldFont = new Font("Arial", Font.BOLD, 13); // Slightly smaller
        Font regularFont = new Font("Arial", Font.PLAIN, 11); // Slightly smaller
        
        // Score with icon
        g2d.setColor(new Color(255, 215, 0)); // Gold
        g2d.setFont(boldFont);
        g2d.drawString("★ " + score, x, y + 15);
        
        // Lives with heart icons
        g2d.setColor(new Color(255, 100, 100)); // Light red
        StringBuilder livesDisplay = new StringBuilder();
        for (int i = 0; i < lives; i++) {
            livesDisplay.append("♥ ");
        }
        if (lives == 0) livesDisplay.append("☠");
        g2d.drawString(livesDisplay.toString(), x, y + 35);
        
        // Time with clock icon
        int minutes = gameTimeSeconds / 60;
        int seconds = gameTimeSeconds % 60;
        g2d.setColor(Color.WHITE);
        g2d.setFont(regularFont);
        g2d.drawString(String.format("⏰ %d:%02d", minutes, seconds), x, y + 50);
    }
    
    private void drawProgressSection(Graphics2D g2d, int x, int y) {
        Font labelFont = new Font("Arial", Font.PLAIN, 10); // Smaller labels
        g2d.setFont(labelFont);
        
        // Bullets progress bar
        int currentBullets = player.getCurrentBulletCount();
        int maxBullets = player.getMaxBulletCount();
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawString("BULLETS", x, y + 10);
        drawProgressBar(g2d, x, y + 15, 120, 8, currentBullets, maxBullets, new Color(255, 215, 0), new Color(100, 100, 0));
        
        // Speed progress bar
        int currentSpeed = player.getCurrentSpeed();
        int maxSpeed = player.getMaxSpeed();
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawString("SPEED", x, y + 35);
        drawProgressBar(g2d, x, y + 40, 120, 8, currentSpeed - 3, maxSpeed - 3, new Color(0, 255, 255), new Color(0, 100, 100));
        
        // Active shots indicator
        int activeShots = shots.size();
        int maxShots = player.getMaxShots();
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawString("SHOTS", x + 130, y + 10); // Shorter label
        drawProgressBar(g2d, x + 130, y + 15, 80, 8, activeShots, maxShots, new Color(255, 100, 255), new Color(100, 0, 100));
    }
    
    private void drawStatusSection(Graphics2D g2d, int x, int y) {
        Font statusFont = new Font("Arial", Font.BOLD, 11); // Smaller
        g2d.setFont(statusFont);
        
        // Phase indicator
        String phaseText = "PHASE " + currentPhase;
        g2d.setColor(getPhaseColor(currentPhase));
        g2d.drawString(phaseText, x, y + 15);
        
        // Powerup availability hints
        drawPowerupHints(g2d, x, y + 35);
    }
    
    private void drawMultishotStatus(Graphics2D g2d) {
        int remainingSeconds = player.getMultishotFramesRemaining() / 60;
        
        // Much smaller, compact multishot status bar
        g2d.setColor(new Color(255, 165, 0, 180)); // More transparent
        g2d.fillRoundRect(10, 85, 200, 18, 6, 6); // Smaller: 200x18 (was 300x25)
        
        // Thinner border
        g2d.setColor(new Color(255, 215, 0));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(10, 85, 200, 18, 6, 6);
        
        // Smaller text
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 10)); // Much smaller font (was 14)
        g2d.drawString("≡ MULTISHOT: " + remainingSeconds + "s", 15, 96);
        
        // Compact auto-fire indicator
        if (frame % 30 < 15) { // Blinking effect
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 9)); // Even smaller
            g2d.drawString("● AUTO", 145, 96);
        }
    }
    
    private void drawProgressBar(Graphics2D g2d, int x, int y, int width, int height, int current, int max, Color fillColor, Color bgColor) {
        // Background
        g2d.setColor(bgColor);
        g2d.fillRoundRect(x, y, width, height, 4, 4);
        
        // Fill
        if (max > 0) {
            int fillWidth = (int) ((double) current / max * width);
            g2d.setColor(fillColor);
            g2d.fillRoundRect(x, y, fillWidth, height, 4, 4);
        }
        
        // Border
        g2d.setColor(new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(x, y, width, height, 4, 4);
        
        // Value text with contrasting colors
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        String valueText = current + "/" + max;
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (width - fm.stringWidth(valueText)) / 2;
        int textY = y + height - 2;
        
        // Use contrasting text color based on fill percentage
        double fillPercent = max > 0 ? (double) current / max : 0;
        if (fillPercent > 0.5) {
            g2d.setColor(Color.BLACK); // Dark text on bright fill
        } else {
            g2d.setColor(Color.WHITE); // Light text on dark background
        }
        g2d.drawString(valueText, textX, textY);
    }
    
    private Color getPhaseColor(int phase) {
        switch (phase) {
            case 1: return new Color(0, 255, 0); // Green - Learning
            case 2: return new Color(255, 255, 0); // Yellow - Threat
            case 3: return new Color(255, 100, 100); // Red - Escalation
            default: return Color.WHITE;
        }
    }
    
    private void drawPowerupHints(Graphics2D g2d, int x, int y) {
        Font hintFont = new Font("Arial", Font.PLAIN, 10);
        g2d.setFont(hintFont);
        
        // Show phase names instead of powerup availability
        String phaseDescription;
        Color phaseColor;
        
        switch (currentPhase) {
            case 1:
                phaseDescription = "Learning Phase";
                phaseColor = new Color(100, 255, 100, 180); // Light green
                break;
            case 2:
                phaseDescription = "Threat Phase";
                phaseColor = new Color(255, 255, 100, 180); // Light yellow
                break;
            case 3:
                phaseDescription = "War Phase";
                phaseColor = new Color(255, 100, 100, 180); // Light red
                break;
            default:
                phaseDescription = "Unknown Phase";
                phaseColor = new Color(150, 150, 150);
                break;
        }
        
        g2d.setColor(phaseColor);
        g2d.drawString(phaseDescription, x, y);
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
            // Stop scene1 music and play victory sound
            try {
                if (audioPlayer != null) {
                    audioPlayer.stop();
                }
                SoundEffectPlayer.playVictorySound();
            } catch (Exception e) {
                System.out.println("Error playing victory sound: " + e.getMessage());
            }
            
            message = "Level 1 Complete!You survived 5 minutes!Press SPACE to continue to Level 2";
        }

        // player
        player.act();
        
        // Auto-fire for multishot powerup (machinegun mode)
        if (player.canAutoFire()) {
            // Calculate shot origin from tip of player sprite, slightly right of center
            int x = player.getX() + (PLAYER_WIDTH / 2) + 3; // Slightly right of center
            int y;
            if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
                y = player.getY(); // Top of sprite in vertical mode (shots go up)
            } else {
                y = player.getY() + PLAYER_HEIGHT / 2; // Middle height in horizontal mode (shots go right)
            }
            
            // Main shot
            Shot autoShot = new Shot(x, y);
            shots.add(autoShot);

            // Add spread shots for even more firepower during multishot
            if (player.hasMultishot()) {
                if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
                    Shot spreadShot1 = new Shot(x - 10, y); // Left
                    Shot spreadShot2 = new Shot(x + 10, y); // Right
                    shots.add(spreadShot1);
                    shots.add(spreadShot2);
                } else {
                    Shot spreadShot1 = new Shot(x, y - 10); // Above
                    Shot spreadShot2 = new Shot(x, y + 10); // Below
                    shots.add(spreadShot1);
                    shots.add(spreadShot2);
                }
            }

            // Only play sound occasionally to avoid audio spam
            if (shots.size() % 3 == 0) {
                SoundEffectPlayer.playShootSound();
            }
            
            player.triggerAutoFire();
        }

        // Power-ups
        for (PowerUp powerup : powerups) {
            if (powerup.isVisible()) {
                powerup.act();
                if (powerup.collidesWith(player)) {

                    SoundEffectPlayer.playCatchPowerUpSound(); // Play power-up sound

                    // Special handling for health pickup
                    if (powerup instanceof HealthPickup && lives < 3) {
                        lives++; // Restore one life
                        powerup.die();
                    } else if (!(powerup instanceof HealthPickup)) {
                        powerup.upgrade(player);
                    } else {
                        // Health pickup when already at max lives - just remove it
                        powerup.die();
                    }
                }
            }
        }

        // Enemies with mode-aware cleanup
        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                enemy.act(direction);
                // If Alien1 just shot a bomb, add it to bombs list if not already present and not destroyed
                if (enemy instanceof Alien1) {
                    EnemyBomb bomb = ((Alien1) enemy).getBomb();
                    if (!bomb.isDestroyed() && !bombs.contains(bomb)) {
                        bombs.add(bomb);
                    }
                }
                // Mode-aware enemy cleanup when they go offscreen
                if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
                    if (enemy.getY() > BOARD_HEIGHT + 50) {
                        enemy.die();
                    }
                } else {
                    if (enemy.getX() < -50) {
                        enemy.die();
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
                            // Missile continues even after enemy is destroyed
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
                        handleGameOverSound();
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
            
            // Handle Alien2 missiles
            if (enemy instanceof Alien2) {
                Alien2 alien = (Alien2) enemy;
                Missile missile = alien.getMissile();

                // Missile collision detection with player
                int missileX = missile.getX();
                int missileY = missile.getY();
                int playerX = player.getX();
                int playerY = player.getY();

                if (player.isVisible() && !player.isInvincible() && !missile.isDestroyed()
                        && missileX >= (playerX)
                        && missileX <= (playerX + PLAYER_WIDTH)
                        && missileY >= (playerY)
                        && missileY <= (playerY + PLAYER_HEIGHT)) {

                    missile.setDestroyed(true);
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
                        handleGameOverSound();
                    }
                }

                if (!missile.isDestroyed()) {
                    missile.act();
                } else {
                    // Ensure particle effects are cleared when missile is destroyed
                    missile.getParticleEffect().clearAllParticles();
                }
            }
        }
        
        // Update and cleanup bombs independently
        List<EnemyBomb> bombsToRemove = new ArrayList<>();
        for (EnemyBomb bomb : bombs) {
            bomb.act();
            if (bomb.isDestroyed()) {
                bombsToRemove.add(bomb);
            }
        }
        bombs.removeAll(bombsToRemove);

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
        // Stop any currently playing audio (scene1.wav or gameOver.wav)
        try {
            if (audioPlayer != null) {
                audioPlayer.stop();
            }
        } catch (Exception e) {
            System.err.println("Error stopping audio during restart: " + e.getMessage());
        }

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
        bombs.clear(); // Clear bombs list

        // Reinitialize everything
        gameInit();

        // Restart scene1.wav
        initAudio();

        // Restart timer if it was stopped
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    private void handleGameOverSound() {
        if (!gameOverSoundPlayed) {
            // Stop background music first, then play game over sound
            if (audioPlayer != null) {
                try {
                    audioPlayer.stop();
                } catch (Exception e) {
                    System.err.println("Error stopping audio: " + e.getMessage());
                }
            }
            SoundEffectPlayer.playGameOverSound();
            gameOverSoundPlayed = true;
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
                } else if (key == KeyEvent.VK_SPACE && (message.contains("Level 2") || message.contains("Level 1 Complete"))) {
                    game.loadScene2(); // Move to Scene2
                    return;
                }
            }

            // Only handle player controls when game is running
            if (inGame) {
                player.keyPressed(e);

                // Calculate shot origin from tip of player sprite, slightly right of center
                int x = player.getX() + (PLAYER_WIDTH / 2) + 3; // Slightly right of center
                int y;
                if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
                    y = player.getY(); // Top of sprite in vertical mode (shots go up)
                } else {
                    y = player.getY() + PLAYER_HEIGHT / 2; // Middle height in horizontal mode (shots go right)
                }

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