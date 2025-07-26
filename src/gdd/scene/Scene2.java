package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import gdd.Global;
import static gdd.Global.*;
import gdd.SoundEffectPlayer;
import gdd.SpawnDetails;
import gdd.powerup.HealthPickup;
import gdd.powerup.PowerUp;
import gdd.sprite.Alien1;
import gdd.sprite.Alien2;
import gdd.sprite.Boss;
import gdd.sprite.BossBomb;
import gdd.sprite.Enemy;
import gdd.sprite.EnemyBomb;
import gdd.sprite.Explosion;
import gdd.sprite.Missile;
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

public class Scene2 extends JPanel {
    private int frame = 0;
    private List<PowerUp> powerups;
    private List<Enemy> enemies;
    private List<Explosion> explosions;
    private List<Shot> shots;
    private Player player;
    private Boss boss;

    private int direction = -1;
    private int deaths = 0;
    private int score = 0;
    private int lives = 3;
    private int gameTimeSeconds = 0;
    private int framesSinceLastSecond = 0;
    
    // Phase-based enemy behavior
    private int currentPhase = 1;
    private boolean bossSpawned = false;
    private boolean bossDefeated = false;

    private boolean inGame = true;
    private String message = "Game Over";

    private final Dimension d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
    private final Random randomizer = new Random();

    private Timer timer;
    private final Game game;

    private List<Star> stars = new ArrayList<>();

    private boolean bossIntroPlayed = false; // Track if boss intro has been played

    private boolean gameOverSoundPlayed = false; // Track if game over sound has been played

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
    private AudioPlayer bossIntroAudioPlayer;
    
    // Boss intro timing (11 seconds = 660 frames at 60fps)
    private static final int BOSS_INTRO_DURATION_FRAMES = 460; // 11 seconds
    private int bossIntroFramesRemaining = 0;



    private List<EnemyBomb> bombs = new ArrayList<>();
    private List<Missile> missiles = new ArrayList<>();
    private List<BossBomb> bossBombs = new ArrayList<>();

    public Scene2(Game game) {
        this.game = game;
        this.spawnManager = new SpawnManager(spawnMap, randomizer);
        loadSpawnDetails();
    }

    private void initAudio() {
        try {
            String filePath = "src/audio/new_ost/stage2.wav";
            System.out.println("Scene2: Loading audio from: " + filePath);
            audioPlayer = new AudioPlayer(filePath);
            System.out.println("Scene2: AudioPlayer created successfully");
            audioPlayer.play();
            System.out.println("Scene2: Audio started playing");
        } catch (Exception e) {
            System.err.println("Scene2: Error initializing audio player: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSpawnDetails() {
        // Use SpawnManager to handle all spawning logic
        spawnManager.loadScene2SpawnDetails();
    }
    
    public void start() {
        // Indicate to Alien1 that this is level 2
        gdd.sprite.Alien1.IS_LEVEL2 = true;

        addKeyListener(new TAdapter());
        setFocusable(true);
        requestFocusInWindow();
        setBackground(Color.black);

        // Reset frame counter
        frame = 0;

        gameOverSoundPlayed = false; // Reset game over sound flag

        timer = new Timer(16, new GameCycle());
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
            if (bossIntroAudioPlayer != null) {
                bossIntroAudioPlayer.stop();
                bossIntroAudioPlayer = null;
            }
            
            // Restore background music volume if it was ducked
            if (audioPlayer != null && audioPlayer.isDucked()) {
                audioPlayer.unduck();
            }
        } catch (Exception e) {
            System.err.println("Error closing audio player.");
        }
        // Reset Alien1 level2 flag
        gdd.sprite.Alien1.IS_LEVEL2 = false;
    }

    private void gameInit() {
        enemies = new ArrayList<>();
        powerups = new ArrayList<>();
        explosions = new ArrayList<>();
        shots = new ArrayList<>();

        initStarField();

        // Check if we have player data from Scene1
        Player playerFromScene1 = game.getPlayerFromScene1();
        if (playerFromScene1 != null) {
            // Use player from Scene1 but reset position for current mode
            player = playerFromScene1;
            player.setX(Global.getPlayerStartX());
            player.setY(Global.getPlayerStartY());
            // Keep all powerups and stats from Scene1
        } else {
            // TESTING: Create new player with Scene2 default stats (speed 8, bullet 5)
            player = new Player();
            // Set speed to 8
            while (player.getCurrentSpeed() < 8) {
                player.increaseSpeed();
            }
            // Set bullet count to 5
            while (player.getCurrentBulletCount() < 5) {
                player.increaseBulletCount();
            }
        }
    }

    /**
     * Initialize star field background with mode-aware positioning for Scene2
     */
    private void initStarField() {
        // Initialize star field with more stars and different colors for level 2
        for (int i = 0; i < 100; i++) {
            int x, y, speed;
            
            if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
                // Vertical mode: stars spawn across width and fall downward
                x = randomizer.nextInt(BOARD_WIDTH);
                y = randomizer.nextInt(BOARD_HEIGHT + 200); // Some start above screen
                speed = randomizer.nextInt(3) + 2; // Variable speed 2-4 for vertical Scene2
            } else {
                // Horizontal mode: stars spawn across height and move leftward
                x = randomizer.nextInt(BOARD_WIDTH);
                y = randomizer.nextInt(BOARD_HEIGHT);
                speed = randomizer.nextInt(3) + 2; // Variable speed 2-4 for horizontal Scene2
            }
            
            int size = randomizer.nextInt(3) + 1;
            
            // Level 2 uses more reddish colors for stars
            Color[] colors = {
                new Color(255, 200, 200), // Light red
                new Color(255, 255, 200), // Yellow-white
                new Color(255, 150, 150)  // Medium red
            };
            Color color = colors[randomizer.nextInt(colors.length)];
            
            stars.add(new Star(x, y, size, speed, color));
        }
    }

    /**
     * Update star field movement with mode-aware direction for Scene2
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
                if (star.x < 0) {
                    star.x = BOARD_WIDTH;
                    star.y = randomizer.nextInt(BOARD_HEIGHT);
                }
            }
        }
    }

    private void drawStarField(Graphics g) {
        for (Star star : stars) {
            g.setColor(star.color);
            if (star.size == 1) {
                g.drawLine(star.x, star.y, star.x, star.y);
            } else {
                g.fillOval(star.x, star.y, star.size, star.size);
            }
        }
    }

    private void drawLevelText(Graphics g) {
        // Only draw during first 3 seconds (180 frames)
        if (frame <= 180) {
            // Flicker every 20 frames
            if ((frame / 20) % 2 == 0) {
                g.setColor(Color.RED); // Red color for Level 2
                g.setFont(new Font("Arial", Font.BOLD, 36));
                
                String levelText = "Level 2";
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
        
        // Top accent line (red for Scene2)
        g2d.setColor(new Color(255, 50, 50, 200));
        g2d.fillRect(0, 0, BOARD_WIDTH, 3);
        
        // === LEFT SECTION: Core Stats ===
        drawStatsSection(g2d, 15, 15);
        
        // === CENTER SECTION: Progress Bars ===
        drawProgressSection(g2d, 220, 15);
        
        // === RIGHT SECTION: Status/Boss ===
        drawStatusSection(g2d, 450, 15);
        
        // === BOTTOM SECTION: Multishot Status ===
        if (player.hasMultishot()) {
            drawMultishotStatus(g2d);
        }
        
        // === BOSS SECTION: Boss Health Bar ===
        if (boss != null && boss.isVisible()) {
            drawBossHealthBar(g2d);
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
        
        // Bullets progress bar (fixed at max for Scene2)
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawString("BULLETS", x, y + 10);
        drawProgressBar(g2d, x, y + 15, 120, 8, 5, 5, new Color(255, 215, 0), new Color(100, 100, 0));
        
        // Speed progress bar (fixed at max for Scene2)
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawString("SPEED", x, y + 35);
        drawProgressBar(g2d, x, y + 40, 120, 8, 5, 5, new Color(0, 255, 255), new Color(0, 100, 100));
        
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
        
        // Phase indicator (consistent with Scene1)
        int currentPhase = getScene2Phase();
        String phaseText = "PHASE " + currentPhase;
        g2d.setColor(getScene2PhaseColor(currentPhase));
        g2d.drawString(phaseText, x, y + 15);
        
        // Boss status or phase info
        if (boss != null && boss.isVisible()) {
            g2d.setColor(new Color(255, 50, 50));
            g2d.drawString("⚠ BOSS FIGHT", x, y + 35);
        } else if (bossDefeated) {
            g2d.setColor(new Color(50, 255, 50));
            g2d.drawString("✓ BOSS DEFEATED", x, y + 35);
        } else {
            // Show phase description
            drawPhaseDescription(g2d, x, y + 35);
        }
    }
    
    private int getScene2Phase() {
        if (gameTimeSeconds < 180) { // First 3 minutes
            return 1; // Combat Phase
        } else { // After 3 minutes
            return 2; // Final Phase
        }
    }
    
    private Color getScene2PhaseColor(int phase) {
        switch (phase) {
            case 1: return new Color(255, 200, 100); // Orange for Scene2 Phase 1
            case 2: return new Color(255, 100, 100); // Red for Scene2 Phase 2
            default: return Color.WHITE;
        }
    }
    
    private void drawPhaseDescription(Graphics2D g2d, int x, int y) {
        Font phaseFont = new Font("Arial", Font.PLAIN, 10);
        g2d.setFont(phaseFont);
        
        // Show phase names based on game time
        String phaseDescription;
        Color phaseColor;
        
        if (gameTimeSeconds < 180) { // First 3 minutes
            phaseDescription = "Combat Phase";
            phaseColor = new Color(255, 200, 100, 180); // Light orange
        } else { // After 3 minutes
            phaseDescription = "Final Phase";
            phaseColor = new Color(255, 100, 100, 180); // Light red
        }
        
        g2d.setColor(phaseColor);
        g2d.drawString(phaseDescription, x, y);
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
    
    private void drawBossHealthBar(Graphics2D g2d) {
        // Ultra-compact boss bar
        int barWidth = 300; // Much smaller (was 400)
        int barHeight = 12; // Much thinner (was 15)
        int barX = (BOARD_WIDTH - barWidth) / 2; // Center it
        int barY = player.hasMultishot() ? 110 : 90; // Higher, less obstructive
        
        // Boss health bar background - very transparent
        g2d.setColor(new Color(60, 0, 0, 120)); // Much more transparent
        g2d.fillRoundRect(barX, barY, barWidth, barHeight, 6, 6);
        
        // Boss health fill
        if (boss != null) {
            double healthPercent = (double) boss.getHealth() / boss.getMaxHealth();
            int fillWidth = (int) (barWidth * healthPercent);
            
            // Subtler colors
            Color healthColor;
            if (healthPercent > 0.6) {
                healthColor = new Color(255, 60, 60, 180); // More subtle
            } else if (healthPercent > 0.3) {
                healthColor = new Color(255, 100, 0, 180); // More subtle
            } else {
                healthColor = new Color(255, 30, 30, 180); // More subtle
            }
            
            g2d.setColor(healthColor);
            g2d.fillRoundRect(barX, barY, fillWidth, barHeight, 6, 6);
        }
        
        // Very thin, subtle border
        g2d.setColor(new Color(255, 0, 0, 150));
        g2d.setStroke(new BasicStroke(0.5f));
        g2d.drawRoundRect(barX, barY, barWidth, barHeight, 6, 6);
        
        // Much smaller boss health text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 9)); // Much smaller font
        String healthText = boss != null ? 
            "BOSS: " + boss.getHealth() + "/" + boss.getMaxHealth() : // Removed emoji
            "BOSS INCOMING";
        
        FontMetrics fm = g2d.getFontMetrics();
        int textX = barX + (barWidth - fm.stringWidth(healthText)) / 2;
        g2d.drawString(healthText, textX, barY + 9);
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
    
    private String getEnemyPhaseText() {
        if (gameTimeSeconds < 180) { // First 3 minutes
            return "⚔ COMBAT PHASE";
        } else { // After 3 minutes
            return "🏁 FINAL PHASE";
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
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

        if (inGame) {
            drawStarField(g);
            drawExplosions(g);
            drawPowerUps(g);
            drawAliens(g);
            drawBoss(g);
            drawPlayer(g);
            drawShot(g);
            drawBombing(g);
            drawDashboard(g);
            drawLevelText(g);
        } else {
            if (timer.isRunning()) {
                timer.stop();
            }
            gameOver(g);
        }

        Toolkit.getDefaultToolkit().sync();
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

    private void drawPowerUps(Graphics g) {
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

    private void drawBoss(Graphics g) {
        if (boss != null && boss.isVisible()) {
            g.drawImage(boss.getImage(), boss.getX(), boss.getY(), this);
        }
    }

    private void drawBombing(Graphics g) {
        // Draw all bombs from the bombs list
        for (EnemyBomb bomb : bombs) {
            if (!bomb.isDestroyed()) {
                g.drawImage(bomb.getImage(), bomb.getX(), bomb.getY(), this);
            }
        }
        
        // Draw all missiles from the missiles list with particle effects
        for (Missile missile : missiles) {
            // Draw missile particle effects first (behind missile)
            missile.drawParticleEffects(g);
            
            if (!missile.isDestroyed()) {
                g.drawImage(missile.getImage(), missile.getX(), missile.getY(), this);
            }
        }
        
        // Draw boss bombs
        for (BossBomb bossBomb : bossBombs) {
            if (!bossBomb.isDestroyed()) {
                g.drawImage(bossBomb.getImage(), bossBomb.getX(), bossBomb.getY(), this);
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

    private void gameOver(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        g.setColor(new Color(32, 0, 0)); // Dark red background for game over
        g.fillRect(50, BOARD_WIDTH / 2 - 30, BOARD_WIDTH - 100, 50);
        g.setColor(Color.red);
        g.drawRect(50, BOARD_WIDTH / 2 - 30, BOARD_WIDTH - 100, 50);

        var small = new Font("Helvetica", Font.BOLD, 14);
        var fontMetrics = this.getFontMetrics(small);

        g.setColor(Color.red);
        g.setFont(small);
        g.drawString(message, (BOARD_WIDTH - fontMetrics.stringWidth(message)) / 2,
                BOARD_WIDTH / 2);

        g.setColor(Color.yellow);
        g.setFont(g.getFont().deriveFont(12f));
        String restartText = "Press R to Restart";
        int restartWidth = g.getFontMetrics().stringWidth(restartText);
        g.drawString(restartText, (BOARD_WIDTH - restartWidth) / 2, BOARD_WIDTH / 2 + 50);
    }

    private void update() {
        // Update frame counter first
        frame++;
        
        // Update boss intro audio timing (13-second duration)
        if (bossIntroFramesRemaining > 0) {
            bossIntroFramesRemaining--;
            
            // When boss intro finishes, restore background music volume
            if (bossIntroFramesRemaining <= 0) {
                if (audioPlayer != null && audioPlayer.isDucked()) {
                    audioPlayer.unduck();
                    System.out.println("Scene2: Restored background music volume after boss intro");
                }
                
                // Stop boss intro audio
                if (bossIntroAudioPlayer != null) {
                    try {
                        bossIntroAudioPlayer.stop();
                        bossIntroAudioPlayer = null;
                        System.out.println("Scene2: Boss intro audio completed and stopped");
                    } catch (Exception e) {
                        System.err.println("Error stopping boss intro audio: " + e.getMessage());
                    }
                }
            }
        }
        
        // Update game timer
        framesSinceLastSecond++;
        if (framesSinceLastSecond >= 60) {
            gameTimeSeconds++;
            framesSinceLastSecond = 0;
        }
        
        updateEnemyPhase();
        updateStarField();

        // Handle spawning through SpawnManager
        SpawnManager.SpawnResult spawnResult = spawnManager.spawnEnemies(frame, player);
        
        // Debug print for spawn events
        if (spawnResult.enemies != null && !spawnResult.enemies.isEmpty()) {
            System.out.println("Frame " + frame + ": Spawning " + spawnResult.enemies.size() + " enemies");
        }
        
        // Add spawned enemies to the game
        if (spawnResult.enemies != null) {
            enemies.addAll(spawnResult.enemies);
        }
        
        // Add spawned powerups to the game
        if (spawnResult.powerups != null) {
            powerups.addAll(spawnResult.powerups);
        }
        
        // Dynamic spawning after boss defeat - continue spawning easier aliens until 5 minutes
        int postBossStartTime = Global.TESTING_MODE ? 10 : 180; // Start after boss spawn time
        if (bossDefeated && gameTimeSeconds >= postBossStartTime && gameTimeSeconds < 300) {
            // Spawn easier aliens every 3 seconds after boss defeat (more manageable)
            if (frame % 180 == 0) { // Every 3 seconds at 60 FPS
                // 75% Alien1, 25% Alien2 - easier than regular gameplay
                String enemyType = randomizer.nextInt(4) < 3 ? "Alien1" : "Alien2";
                // Use mode-aware positioning
                Enemy enemy = enemyType.equals("Alien2") ? 
                    new Alien2(Global.getEnemySpawnX(), Global.getEnemySpawnY()) : 
                    new Alien1(Global.getEnemySpawnX(), Global.getEnemySpawnY());
                enemies.add(enemy);
                System.out.println("Frame " + frame + ": Spawning easier post-boss " + enemyType);
            }
            
            // Occasional double spawns to maintain some challenge (every 10 seconds)
            if (frame % 600 == 0) { // Every 10 seconds
                Enemy extraEnemy = new Alien1(Global.getEnemySpawnX(), Global.getEnemySpawnY());
                enemies.add(extraEnemy);
                System.out.println("Frame " + frame + ": Extra post-boss Alien1");
            }
        }
        
        // Handle victory condition
        if (spawnResult.gameOver) {
            inGame = false;
            message = spawnResult.victoryMessage;
        }

        // Testing mode or normal boss intro timing
        int bossIntroTime = Global.TESTING_MODE ? 9 : 179; // 9 seconds in testing, 179 in normal
        int bossSpawnTime = Global.TESTING_MODE ? 10 : 180; // 10 seconds in testing, 180 in normal
        
        if (!bossIntroPlayed && gameTimeSeconds >= bossIntroTime) {
            // Start boss intro audio transition
            try {
                // Duck the stage2 background music to 30% volume
                if (audioPlayer != null) {
                    audioPlayer.duck();
                    System.out.println("Scene2: Ducked background music for boss intro");
                }
                
                // Play boss intro from original file for 13 seconds at high volume
                bossIntroAudioPlayer = new AudioPlayer("src/audio/new_ost/boss_intro.wav");
                bossIntroAudioPlayer.play();
                bossIntroFramesRemaining = BOSS_INTRO_DURATION_FRAMES;
                bossIntroPlayed = true;
                System.out.println("Scene2: Started boss intro audio for 11 seconds");
            } catch (Exception e) {
                System.err.println("Error playing boss intro audio: " + e.getMessage());
                // Fallback to sound effect if file loading fails
                SoundEffectPlayer.playBossIntroSound();
                bossIntroPlayed = true;
            }
        }

        // Boss spawning logic - spawn boss at 10 seconds (testing) or 3 minutes (normal)
        if (!bossSpawned && gameTimeSeconds >= bossSpawnTime) {
            // Mode-aware boss positioning
            int bossX, bossY;
            if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
                // Vertical mode: boss spawns at top center of screen
                bossX = BOARD_WIDTH / 2 - 50;
                bossY = -100; // Start above screen, will move down
            } else {
                // Horizontal mode: boss spawns at right side
                bossX = BOARD_WIDTH - 300;
                bossY = BOARD_HEIGHT / 2 - 50;
            }
            boss = new Boss(bossX, bossY);
            bossSpawned = true;
            System.out.println("BOSS SPAWNED!");
        }
        
        // Boss management
        if (boss != null && boss.isVisible()) {
            boss.act();
            
            // Boss shooting logic
            if (boss.shouldShoot()) {
                List<BossBomb> newBombs = boss.shoot();
                bossBombs.addAll(newBombs);
            }
            
            // Check if boss is defeated
            if (boss.isDying()) {
                boss.die();
                bossDefeated = true;
                explosions.add(new Explosion(boss.getX(), boss.getY()));
                System.out.println("BOSS DEFEATED!");
            }
        }
        
        // Simple Victory/Defeat conditions
        if (gameTimeSeconds >= 300) { // 5 minutes reached default 300
            if (bossSpawned && !bossDefeated) {
                // Boss not defeated - player loses (had 2 minutes to fight boss)
                inGame = false;
                timer.stop();
                message = "Game Over! Boss was not defeated in time!";
                handleGameOverSound();
            } else {
                // Boss defeated or no boss spawned - player wins
                inGame = false;
                timer.stop();
                message = "Congratulations! You've completed Scene 2!";              
                handleVictorySound();
            }
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

        // Power-ups - only process multishot in level 2
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
        bombs.clear();
        missiles.clear();
        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                enemy.act(direction);
                
                // Mode-aware enemy cleanup when they go offscreen
                if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
                    // Vertical mode: remove enemies that fall off bottom
                    if (enemy.getY() > BOARD_HEIGHT + 50) {
                        enemy.die(); // Mark enemy as invisible so it gets cleaned up
                        // Missile continues even after enemy goes offscreen
                    }
                } else {
                    // Horizontal mode: remove enemies that go off left side (current behavior)
                    if (enemy.getX() < -50) {
                        enemy.die();
                        // Missile continues even after enemy goes offscreen
                    }
                }
            }
            // Collect bombs from Alien1 and missiles from Alien2
            if (enemy instanceof Alien1) {
                EnemyBomb bomb = ((Alien1) enemy).getBomb();
                if (bomb != null) bombs.add(bomb);
            } else if (enemy instanceof Alien2) {
                Missile missile = ((Alien2) enemy).getMissile();
                if (missile != null) missiles.add(missile);
            }
        }
        // Update all bombs
        for (EnemyBomb bomb : bombs) {
            bomb.act();
            // Check collision with player
            if (!bomb.isDestroyed() && player.isVisible()
                && bomb.getX() >= player.getX()
                && bomb.getX() <= (player.getX() + PLAYER_WIDTH)
                && bomb.getY() >= player.getY()
                && bomb.getY() <= (player.getY() + PLAYER_HEIGHT)) {
                bomb.setDestroyed(true);
                explosions.add(new Explosion(player.getX(), player.getY()));

                SoundEffectPlayer.playPlayerHitSound(); // Play player hit sound

                player.takeDamage();
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
        }
        
        // Update all missiles
        for (Missile missile : missiles) {
            if (!missile.isDestroyed()) {
                missile.act();
            } else {
                // Ensure particle effects are cleared when missile is destroyed
                missile.getParticleEffect().clearAllParticles();
            }
            // Check collision with player
            if (!missile.isDestroyed() && player.isVisible() && !player.isInvincible()
                && missile.getX() >= player.getX()
                && missile.getX() <= (player.getX() + PLAYER_WIDTH)
                && missile.getY() >= player.getY()
                && missile.getY() <= (player.getY() + PLAYER_HEIGHT)) {
                missile.setDestroyed(true);
                explosions.add(new Explosion(player.getX(), player.getY()));

                SoundEffectPlayer.playPlayerHitSound(); // Play player hit sound

                player.takeDamage();
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
        }
        
        // Update boss bombs
        List<BossBomb> bossBombsToRemove = new ArrayList<>();
        for (BossBomb bossBomb : bossBombs) {
            bossBomb.act();
            
            // Check collision with player
            if (!bossBomb.isDestroyed() && player.isVisible()
                && bossBomb.getX() >= player.getX()
                && bossBomb.getX() <= (player.getX() + PLAYER_WIDTH)
                && bossBomb.getY() >= player.getY()
                && bossBomb.getY() <= (player.getY() + PLAYER_HEIGHT)) {
                bossBomb.setDestroyed(true);
                explosions.add(new Explosion(player.getX(), player.getY()));
                
                SoundEffectPlayer.playPlayerHitSound();

                player.takeDamage();
                lives--;
                if (lives <= 0) {
                    var ii = new ImageIcon(IMG_EXPLOSION);
                    player.setImage(ii.getImage());
                    player.setDying(true);
                    inGame = false;
                    message = "Game Over!";

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
            }
            
            // Remove destroyed boss bombs
            if (bossBomb.isDestroyed()) {
                bossBombsToRemove.add(bossBomb);
            }
        }
        bossBombs.removeAll(bossBombsToRemove);

        // Shots
        List<Shot> shotsToRemove = new ArrayList<>();
        for (Shot shot : shots) {
            if (shot.isVisible()) {
                shot.act();
                
                // Check collision with boss first
                if (boss != null && boss.isVisible() && shot.isVisible() && shot.collidesWith(boss)) {
                    boss.takeDamage();
                    explosions.add(new Explosion(shot.getX(), shot.getY()));

                    SoundEffectPlayer.playEnemyExplodeSound(); // Play boss hit sound


                    score += 50; // Points for hitting boss
                    shot.die();
                    shotsToRemove.add(shot);
                    continue; // Skip enemy collision check
                }
                
                // Check collision with regular enemies
                for (Enemy enemy : enemies) {
                    if (enemy.isVisible() && shot.isVisible() && shot.collidesWith(enemy)) {
                        var ii = new ImageIcon(IMG_EXPLOSION);
                        enemy.setImage(ii.getImage());
                        enemy.setDying(true);
                        explosions.add(new Explosion(enemy.getX(), enemy.getY()));

                        SoundEffectPlayer.playEnemyExplodeSound(); // Play enemy explosion sound


                        deaths++;
                        
                        if (enemy instanceof Alien2) {
                            score += 300; // More points in level 2
                            // Missile continues even after enemy is destroyed
                        } else {
                            score += 150; // More points in level 2
                        }
                        
                        shot.die();
                        shotsToRemove.add(shot);
                        break;
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

        // Update and remove completed explosions
        for (Explosion explosion : explosions) {
            explosion.act();
        }
        explosions.removeIf(explosion -> !explosion.isVisible());
    }
    
    private void updateEnemyPhase() {
        // Level 2 phases for 5-minute duration with boss at 3:00
        if (gameTimeSeconds < 120) { // First 2 minutes
            currentPhase = 1;
        } else if (gameTimeSeconds < 180) { // Minutes 2-3
            currentPhase = 2;
        } else if (gameTimeSeconds >= 180 && !bossSpawned) { // Boss spawns at 3 minutes
            currentPhase = 3;
        } else if (bossSpawned && !bossDefeated) { // Boss fight phase (2 minutes)
            currentPhase = 4;
        } else if (bossDefeated && gameTimeSeconds < 300) { // Continue fighting until 5 minutes
            currentPhase = 5;
        } else {
            currentPhase = 6; // Victory at 5 minutes
        }
    }

    private void restartGame() {
        game.loadScene1();
    }

    private void handleGameOverSound() {
        if (!gameOverSoundPlayed) {
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

    private void handleVictorySound() {
        if (audioPlayer != null) {
            try {
                audioPlayer.stop();
            } catch (Exception e) {
                System.err.println("Error stopping audio: " + e.getMessage());
            }
        }
        SoundEffectPlayer.playVictorySound();
    }

    private class TAdapter extends KeyAdapter {
        @Override
        public void keyReleased(KeyEvent e) {
            if (inGame) {
                player.keyReleased(e);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (!inGame) {
                if (key == KeyEvent.VK_R) {
                    restartGame();
                    return;
                }
            }

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

    private class GameCycle implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            doGameCycle();
        }
    }

    private void doGameCycle() {
        update();
        repaint();
    }
} 