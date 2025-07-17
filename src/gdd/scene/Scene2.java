package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.*;
import gdd.SpawnDetails;
import gdd.powerup.PowerUp;
import gdd.powerup.MultiShotPowerUp;
import gdd.sprite.Alien1;
import gdd.sprite.Alien2;
import gdd.sprite.Enemy;
import gdd.sprite.Explosion;
import gdd.sprite.Player;
import gdd.sprite.Shot;
import gdd.sprite.EnemyBomb;
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

public class Scene2 extends JPanel {
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

    private boolean isDirectAccess = false;

    public Scene2(Game game) {
        this.game = game;
        this.spawnManager = new SpawnManager(spawnMap, randomizer);
        loadSpawnDetails();
    }

    private void initAudio() {
        try {
            String filePath = "src/audio/scene2.wav";
            audioPlayer = new AudioPlayer(filePath);
            audioPlayer.play();
        } catch (Exception e) {
            System.err.println("Error initializing audio player: " + e.getMessage());
        }
    }

    private void loadSpawnDetails() {
        // Use SpawnManager to handle all spawning logic
        spawnManager.loadScene2SpawnDetails();
    }
    
    public void start() {
        // Check if this is direct access (shortcut) or transition from Scene1
        isDirectAccess = gameTimeSeconds == 0;

        addKeyListener(new TAdapter());
        setFocusable(true);
        requestFocusInWindow();
        setBackground(Color.black);

        // Reset frame counter
        frame = 0;

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
        } catch (Exception e) {
            System.err.println("Error closing audio player.");
        }
    }

    private void gameInit() {
        enemies = new ArrayList<>();
        powerups = new ArrayList<>();
        explosions = new ArrayList<>();
        shots = new ArrayList<>();

        initStarField();
        
        // If direct access, create new player with testing stats
        if (isDirectAccess) {
            // Create new player instance
            player = new Player();
            
            // Reset timer and frame
            gameTimeSeconds = 0;
            framesSinceLastSecond = 0;
            frame = 0;
            
            // Set default stats for testing
            for (int i = 0; i < 6; i++) { // Add 6 speed powerups to reach 11
                player.increaseSpeed();
            }
            // Set bullet count to exactly 5
            while (player.getCurrentBulletCount() < 5) {
                player.increaseBulletCount();
            }
        } else {
            // For transition from Scene1, use the player from Scene1
            Player scene1Player = game.getPlayerFromScene1();
            if (scene1Player != null) {
                player = scene1Player;
                // Don't reset timer - continue from Scene1's time
                // Don't modify player stats - keep them from Scene1
            } else {
                // Fallback: create new player if no Scene1 player
                player = new Player();
            }
        }
    }

    private void initStarField() {
        // Initialize star field with more stars and different colors for level 2
        for (int i = 0; i < 100; i++) {
            int x = randomizer.nextInt(BOARD_WIDTH);
            int y = randomizer.nextInt(BOARD_HEIGHT);
            int size = randomizer.nextInt(3) + 1;
            int speed = randomizer.nextInt(3) + 2;
            
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

    private void updateStarField() {
        for (Star star : stars) {
            star.x -= star.speed;
            if (star.x < 0) {
                star.x = BOARD_WIDTH;
                star.y = randomizer.nextInt(BOARD_HEIGHT);
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
        // Dashboard background
        g.setColor(new Color(0, 0, 0, 150));
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
        g.setColor(Color.red); // Red for level 2
        String phaseText = "Phase " + currentPhase;
        switch (currentPhase) {
            case 1:
                phaseText += " (Warm Up)";
                break;
            case 2:
                phaseText += " (Attack!)";
                break;
            case 3:
                phaseText += " (MAYHEM!)";
                break;
        }
        g.drawString(phaseText, 550, 20);
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
        
        // Handle victory condition
        if (spawnResult.gameOver) {
            inGame = false;
            message = spawnResult.victoryMessage;
        }

        // Victory condition: Survive 4 minutes (240 seconds)
        if (isDirectAccess) {
            // For direct access, check against actual time
            if (gameTimeSeconds >= 240) {
                inGame = false;
                timer.stop();
                message = "Congratulations! You've completed Scene 2!";
            }
        } else {
            // For transition from Scene1, check against relative time (5 minutes + 4 minutes)
            if (gameTimeSeconds >= 540) { // 300 (Scene1) + 240 (Scene2) seconds
                inGame = false;
                timer.stop();
                message = "Congratulations! You've completed the game!";
            }
        }

        // player
        player.act();
        
        // Auto-fire for multishot powerup
        if (player.canAutoFire() && shots.size() < player.getMaxShots() - 2) {
            int x = player.getX();
            int y = player.getY();
            
            Shot autoShot = new Shot(x, y);
            shots.add(autoShot);
            
            if (player.hasMultishot() && shots.size() < player.getMaxShots()) {
                Shot spreadShot = new Shot(x, y + 25);
                shots.add(spreadShot);
            }
            
            player.triggerAutoFire();
        }

        // Power-ups - only process multishot in level 2
        for (PowerUp powerup : powerups) {
            if (powerup.isVisible()) {
                powerup.act();
                if (powerup.collidesWith(player)) {
                    powerup.upgrade(player);
                }
            }
        }

        // Enemies
        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                enemy.act(direction);
                if (enemy.getX() < -50) {
                    enemy.die();
                }
            }
        }

        // Shots
        List<Shot> shotsToRemove = new ArrayList<>();
        for (Shot shot : shots) {
            if (shot.isVisible()) {
                shot.act();
                
                for (Enemy enemy : enemies) {
                    if (enemy.isVisible() && shot.isVisible() && shot.collidesWith(enemy)) {
                        var ii = new ImageIcon(IMG_EXPLOSION);
                        enemy.setImage(ii.getImage());
                        enemy.setDying(true);
                        explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                        deaths++;
                        
                        if (enemy instanceof Alien2) {
                            score += 300; // More points in level 2
                        } else {
                            score += 150; // More points in level 2
                        }
                        
                        shot.die();
                        shotsToRemove.add(shot);
                        break;
                    }
                }

                if (shot.getX() > BOARD_WIDTH) {
                    shot.die();
                    shotsToRemove.add(shot);
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
        // Level 2 phases are shorter and more intense
        if (gameTimeSeconds < 60) {
            currentPhase = 1;
        } else if (gameTimeSeconds < 120) {
            currentPhase = 2;
        } else {
            currentPhase = 3;
        }
    }

    private void restartGame() {
        game.loadScene2();
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

                int x = player.getX();
                int y = player.getY();

                // One bullet per space press, but more bullets can be on screen
                if (key == KeyEvent.VK_SPACE) {
                    int maxShots = player.getMaxShots();
                    if (shots.size() < maxShots) {
                        Shot shot = new Shot(x, y);
                        shots.add(shot);
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