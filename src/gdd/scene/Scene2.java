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
import gdd.sprite.Boss;
import gdd.sprite.BossBomb;
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



    private List<EnemyBomb> bombs = new ArrayList<>();
    private List<BossBomb> bossBombs = new ArrayList<>();

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
        
        // Show fixed powerup status
        g.setColor(Color.yellow);
        g.drawString("Bullets: 5", 300, 20);
        
        g.setColor(Color.cyan);
        g.drawString("Speed: 8", 300, 40);
        
        // Multishot status
        g.setColor(Color.white);
        if (player.hasMultishot()) {
            int remainingSeconds = player.getMultishotFramesRemaining() / 60;
            g.setColor(Color.yellow);
            g.drawString("≡ MULTISHOT: " + remainingSeconds + "s", 10, 60);
            g.setColor(Color.red);
            g.drawString("AUTO-FIRE ACTIVE!", 10, 80);
        }
        
        // Boss status
        if (boss != null && boss.isVisible()) {
            g.setColor(Color.red);
            g.drawString("BOSS HEALTH: " + boss.getHealth() + "/" + boss.getMaxHealth(), 400, 20);
            g.setColor(Color.orange);
            g.drawString("BOSS INCOMING!", 400, 40);
        } else if (bossDefeated) {
            g.setColor(Color.green);
            g.drawString("BOSS DEFEATED!", 400, 20);
        }
        
        // Phase information - only show if boss is not present
        if (boss == null || !boss.isVisible()) {
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
                    phaseText += " (BOSS SPAWN!)";
                    break;
                case 4:
                    phaseText += " (BOSS FIGHT!)";
                    break;
                case 5:
                    phaseText += " (VICTORY!)";
                    break;
                case 6:
                    phaseText += " (COMPLETE!)";
                    break;
            }
            g.drawString(phaseText, 550, 20);
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
        
        // Dynamic spawning after boss defeat - continue spawning aliens until 5 minutes
        if (bossDefeated && gameTimeSeconds >= 240 && gameTimeSeconds < 300) {
            // Spawn additional aliens every 2 seconds after boss defeat
            if (frame % 120 == 0) { // Every 2 seconds at 60 FPS
                String enemyType = randomizer.nextInt(2) == 0 ? "Alien2" : "Alien1"; // 50% each
                // Use mode-aware positioning
                Enemy enemy = enemyType.equals("Alien2") ? 
                    new Alien2(Global.getEnemySpawnX(), Global.getEnemySpawnY()) : 
                    new Alien1(Global.getEnemySpawnX(), Global.getEnemySpawnY());
                enemies.add(enemy);
                System.out.println("Frame " + frame + ": Spawning post-boss " + enemyType);
            }
        }
        
        // Handle victory condition
        if (spawnResult.gameOver) {
            inGame = false;
            message = spawnResult.victoryMessage;
        }

        if (!bossIntroPlayed && gameTimeSeconds >= 239) {
            // Play boss intro sound if not already played
            SoundEffectPlayer.playBossIntroSound(); // Play 1 second before boss spawn
            bossIntroPlayed = true;

        }

        // Boss spawning logic - spawn boss at 4 minutes (240 seconds)
        if (!bossSpawned && gameTimeSeconds >= 240) {
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

                SoundEffectPlayer.playVictorySound(); // Play victory sound

                System.out.println("BOSS DEFEATED!");
            }
        }
        
        // Simple Victory/Defeat conditions
        if (gameTimeSeconds >= 300) { // 5 minutes reached
            if (bossSpawned && !bossDefeated) {
                // Boss not defeated - player loses
                inGame = false;
                timer.stop();
                message = "Game Over! Boss was not defeated in time!";
                
                if (!gameOverSoundPlayed) {
                    SoundEffectPlayer.playGameOverSound(); // Play game over sound
                    gameOverSoundPlayed = true;
                }
            } else {
                // Boss defeated or no boss spawned - player wins
                inGame = false;
                timer.stop();
                message = "Congratulations! You've completed Scene 2!";

                if (!bossDefeated) {
                    SoundEffectPlayer.playVictorySound();
                }

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

            SoundEffectPlayer.playShootSound(); // Play shooting sound

            
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

                    SoundEffectPlayer.playCatchPowerUpSound(); // Play power-up sound

                    powerup.upgrade(player);
                }
            }
        }

        // Enemies with mode-aware cleanup
        bombs.clear();
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
                    if (enemy.getX() < -50) {
                        enemy.die();
                    }
                }
            }
            // Collect bombs from Alien1 and Alien2
            if (enemy instanceof Alien1) {
                EnemyBomb bomb = ((Alien1) enemy).getBomb();
                if (bomb != null) bombs.add(bomb);
            } else if (enemy instanceof Alien2) {
                EnemyBomb bomb = ((Alien2) enemy).getBomb();
                if (bomb != null) bombs.add(bomb);
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
                
                // Play game over sound
                if (!gameOverSoundPlayed) {
                    SoundEffectPlayer.playGameOverSound();
                    gameOverSoundPlayed = true;
                    }    

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
        // Level 2 phases for 5-minute duration
        if (gameTimeSeconds < 120) { // First 2 minutes
            currentPhase = 1;
        } else if (gameTimeSeconds < 240) { // Minutes 2-4
            currentPhase = 2;
        } else if (gameTimeSeconds >= 240 && !bossSpawned) { // Boss spawns at 4 minutes
            currentPhase = 3;
        } else if (bossSpawned && !bossDefeated) { // Boss fight phase
            currentPhase = 4;
        } else if (bossDefeated && gameTimeSeconds < 300) { // Continue fighting until 5 minutes
            currentPhase = 5;
        } else {
            currentPhase = 6; // Victory at 5 minutes
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