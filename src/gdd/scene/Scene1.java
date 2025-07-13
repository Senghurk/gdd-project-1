package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.*;
import gdd.SpawnDetails;
import gdd.powerup.PowerUp;
import gdd.powerup.SpeedUp;
import gdd.powerup.MultiShotPowerUp;
import gdd.sprite.Alien1;
import gdd.sprite.Alien2;
import gdd.sprite.Enemy;
import gdd.sprite.Explosion;
import gdd.sprite.Player;
import gdd.sprite.Shot;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
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
    // private Shot shot;

    final int BLOCKHEIGHT = 50;
    final int BLOCKWIDTH = 50;

    final int BLOCKS_TO_DRAW = BOARD_HEIGHT / BLOCKHEIGHT;

    private int direction = -1;
    private int deaths = 0;
    private int score = 0;
    private int lives = 3;
    private int gameTimeSeconds = 0;
    private int framesSinceLastSecond = 0;
    
    // Phase-based enemy behavior
    private int currentPhase = 1;
    private int lastPhaseChangeTime = 0;

    private boolean inGame = true;
    private String message = "Game Over";

    private final Dimension d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
    private final Random randomizer = new Random();

    private Timer timer;
    private final Game game;

    private int currentRow = -1;
    // TODO load this map from a file
    // Replace MAP array with dynamic star generation
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

    // Remove the old MAP array - replace with dynamic star generation
    // The MAP array is no longer used with the new star field system
    
    private HashMap<Integer, SpawnDetails> spawnMap = new HashMap<>();
    private AudioPlayer audioPlayer;
    private int lastRowToShow;
    private int firstRowToShow;

    public Scene1(Game game) {
        this.game = game;
        // initBoard();
        // gameInit();
        loadSpawnDetails();
    }

    private void initAudio() {
        try {
            String filePath = "src/audio/scene1.wav";
            audioPlayer = new AudioPlayer(filePath);
            audioPlayer.play();
        } catch (Exception e) {
            System.err.println("Error initializing audio player: " + e.getMessage());
        }
    }

    private void loadSpawnDetails() {
        // TODO load this from a file
        // For sideways gameplay, spawn enemies from the right side (x = BOARD_WIDTH) with varying y positions
        spawnMap.put(50, new SpawnDetails("PowerUp-SpeedUp", BOARD_WIDTH - 50, 100));
        spawnMap.put(200, new SpawnDetails("Alien1", BOARD_WIDTH - 50, 200));
        spawnMap.put(300, new SpawnDetails("Alien1", BOARD_WIDTH - 50, 300));

        spawnMap.put(400, new SpawnDetails("Alien1", BOARD_WIDTH - 50, 100));
        spawnMap.put(401, new SpawnDetails("Alien1", BOARD_WIDTH - 50, 150));
        spawnMap.put(402, new SpawnDetails("Alien1", BOARD_WIDTH - 50, 200));
        spawnMap.put(403, new SpawnDetails("Alien1", BOARD_WIDTH - 50, 250));

        spawnMap.put(500, new SpawnDetails("Alien1", BOARD_WIDTH - 50, 300));
        spawnMap.put(501, new SpawnDetails("Alien1", BOARD_WIDTH - 50, 350));
        spawnMap.put(502, new SpawnDetails("Alien1", BOARD_WIDTH - 50, 400));
        spawnMap.put(503, new SpawnDetails("Alien1", BOARD_WIDTH - 50, 450));
        
        // Add Alien2 enemies and MultiShot powerup
        spawnMap.put(250, new SpawnDetails("PowerUp-MultiShot", BOARD_WIDTH - 50, 250));
        spawnMap.put(600, new SpawnDetails("Alien2", BOARD_WIDTH - 50, 150));
        spawnMap.put(650, new SpawnDetails("Alien2", BOARD_WIDTH - 50, 350));
        spawnMap.put(700, new SpawnDetails("Alien2", BOARD_WIDTH - 50, 250));
        
        // More varied spawns for extended gameplay
        spawnMap.put(800, new SpawnDetails("Alien1", BOARD_WIDTH - 50, 100));
        spawnMap.put(850, new SpawnDetails("Alien2", BOARD_WIDTH - 50, 200));
        spawnMap.put(900, new SpawnDetails("PowerUp-SpeedUp", BOARD_WIDTH - 50, 300));
        spawnMap.put(950, new SpawnDetails("Alien1", BOARD_WIDTH - 50, 400));
        spawnMap.put(1000, new SpawnDetails("Alien2", BOARD_WIDTH - 50, 150));
        
        // 5-minute gameplay (18000 frames at 60fps)
        // Phase 1: Minutes 1-2 (frames 1000-7200) - Moderate difficulty
        generatePhase1Spawns();
        
        // Phase 2: Minutes 2-4 (frames 7200-14400) - Increased difficulty  
        generatePhase2Spawns();
        
        // Phase 3: Minutes 4-5 (frames 14400-18000) - High difficulty
        generatePhase3Spawns();
    }

    private void generatePhase1Spawns() {
        // Phase 1: Frames 1200-7200 (Minutes 1-2) - Moderate difficulty
        for (int frame = 1200; frame <= 7200; frame += 80) { // Every 1.3 seconds (tighter)
            int y = 100 + randomizer.nextInt(400); // Random Y position
            if (frame % 480 == 0) { // Every 8 seconds add powerup
                String powerupType = randomizer.nextBoolean() ? "PowerUp-SpeedUp" : "PowerUp-MultiShot";
                spawnMap.put(frame, new SpawnDetails(powerupType, BOARD_WIDTH - 50, y));
            } else {
                String enemyType = randomizer.nextInt(3) == 0 ? "Alien2" : "Alien1"; // 33% Alien2
                spawnMap.put(frame, new SpawnDetails(enemyType, BOARD_WIDTH - 50, y));
                
                // Add occasional double spawns
                if (randomizer.nextInt(4) == 0) {
                    int y2 = 100 + randomizer.nextInt(400);
                    spawnMap.put(frame + 20, new SpawnDetails(enemyType, BOARD_WIDTH - 50, y2));
                }
            }
        }
    }
    
    private void generatePhase2Spawns() {
        // Phase 2: Frames 7200-14400 (Minutes 2-4) - Increased difficulty
        for (int frame = 7200; frame <= 14400; frame += 60) { // Every 1 second (much tighter)
            int y = 100 + randomizer.nextInt(400);
            if (frame % 420 == 0) { // Every 7 seconds add powerup
                String powerupType = randomizer.nextBoolean() ? "PowerUp-SpeedUp" : "PowerUp-MultiShot";
                spawnMap.put(frame, new SpawnDetails(powerupType, BOARD_WIDTH - 50, y));
            } else {
                String enemyType = randomizer.nextInt(2) == 0 ? "Alien2" : "Alien1"; // 50% Alien2
                spawnMap.put(frame, new SpawnDetails(enemyType, BOARD_WIDTH - 50, y));
                
                // More frequent multiple spawns
                if (randomizer.nextInt(3) == 0) {
                    int y2 = 100 + randomizer.nextInt(400);
                    spawnMap.put(frame + 15, new SpawnDetails(enemyType, BOARD_WIDTH - 50, y2));
                }
                
                // Triple spawns occasionally
                if (randomizer.nextInt(6) == 0) {
                    int y3 = 100 + randomizer.nextInt(400);
                    spawnMap.put(frame + 30, new SpawnDetails("Alien2", BOARD_WIDTH - 50, y3));
                }
            }
        }
    }
    
    private void generatePhase3Spawns() {
        // Phase 3: Frames 14400-18000 (Minutes 4-5) - High difficulty
        for (int frame = 14400; frame <= 18000; frame += 45) { // Every 0.75 seconds (intense)
            int y = 100 + randomizer.nextInt(400);
            if (frame % 360 == 0) { // Every 6 seconds add powerup
                String powerupType = randomizer.nextBoolean() ? "PowerUp-SpeedUp" : "PowerUp-MultiShot";
                spawnMap.put(frame, new SpawnDetails(powerupType, BOARD_WIDTH - 50, y));
            } else {
                String enemyType = randomizer.nextInt(4) < 3 ? "Alien2" : "Alien1"; // 75% Alien2
                spawnMap.put(frame, new SpawnDetails(enemyType, BOARD_WIDTH - 50, y));
                
                // Frequently spawn multiple enemies
                if (randomizer.nextInt(2) == 0) { // 50% chance
                    int y2 = 100 + randomizer.nextInt(400);
                    spawnMap.put(frame + 10, new SpawnDetails(enemyType, BOARD_WIDTH - 50, y2));
                    
                    if (randomizer.nextInt(3) == 0) { // 33% chance for triple
                        int y3 = 100 + randomizer.nextInt(400);
                        spawnMap.put(frame + 20, new SpawnDetails("Alien2", BOARD_WIDTH - 50, y3));
                    }
                }
            }
        }
        
        // Remove old victory condition since we handle it in update()
        // spawnMap.put(18000, new SpawnDetails("VICTORY", 0, 0));
    }

    private void initBoard() {

    }

    public void start() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        requestFocusInWindow();
        setBackground(Color.black);

        timer = new Timer(1000 / 60, new GameCycle());
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

        // for (int i = 0; i < 4; i++) {
        // for (int j = 0; j < 6; j++) {
        // var enemy = new Enemy(ALIEN_INIT_X + (ALIEN_WIDTH + ALIEN_GAP) * j,
        // ALIEN_INIT_Y + (ALIEN_HEIGHT + ALIEN_GAP) * i);
        // enemies.add(enemy);
        // }
        // }
        player = new Player();
        // shot = new Shot();
    }

    private void drawStarField(Graphics g) {
        // Draw the scrolling star field
        for (Star star : stars) {
            g.setColor(star.color);
            g.fillOval(star.x, star.y, star.size, star.size);
            
            // Add a subtle glow effect for larger stars
            if (star.size > 1) {
                g.setColor(new Color(star.color.getRed(), star.color.getGreen(), star.color.getBlue(), 50));
                g.fillOval(star.x - 1, star.y - 1, star.size + 2, star.size + 2);
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

            g.drawImage(player.getImage(), player.getX(), player.getY(), this);
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
            // Both Alien1 and Alien2 have bombs
            if (e instanceof Alien1) {
                Alien1 alien = (Alien1) e;
                Alien1.Bomb b = alien.getBomb();
                if (!b.isDestroyed()) {
                    g.drawImage(b.getImage(), b.getX(), b.getY(), this);
                }
            } else if (e instanceof Alien2) {
                Alien2 alien = (Alien2) e;
                Alien2.Bomb b = alien.getBomb();
                if (!b.isDestroyed()) {
                    g.drawImage(b.getImage(), b.getX(), b.getY(), this);
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
        
        // Powerup status
        if (player.hasMultishot()) {
            int remainingSeconds = player.getMultishotFramesRemaining() / 60;
            g.setColor(Color.yellow);
            g.drawString("🔥 MULTISHOT: " + remainingSeconds + "s", 300, 20);
            g.setColor(Color.red);
            g.drawString("AUTO-FIRE ACTIVE!", 300, 35);
        }
        
        // Speed status
        if (player.getSpeed() > 2) {
            g.setColor(Color.cyan);
            g.drawString("⚡ SPEED BOOST: " + player.getSpeed(), 300, 50);
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
        g.drawString(phaseText, 450, 20);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        doDrawing(g);
    }

    private void doDrawing(Graphics g) {

        g.setColor(Color.black);
        g.fillRect(0, 0, d.width, d.height);

        g.setColor(Color.white);
        g.drawString("FRAME: " + frame, 10, 10);

        g.setColor(Color.green);

        if (inGame) {

            drawStarField(g);  // Draw background stars first
            drawExplosions(g);
            drawPowreUps(g);
            drawAliens(g);
            drawPlayer(g);
            drawShot(g);
            drawBombing(g); // Draw enemy bombs
            drawDashboard(g); // Draw dashboard on top

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
        g.drawString(restartText, (BOARD_WIDTH - restartWidth) / 2, BOARD_WIDTH / 2 + 30);
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

        // Check enemy spawn
        // TODO this approach can only spawn one enemy at a frame
        SpawnDetails sd = spawnMap.get(frame);
        if (sd != null) {
            // Create a new enemy based on the spawn details
            switch (sd.type) {
                case "Alien1":
                    Enemy enemy = new Alien1(sd.x, sd.y);
                    enemies.add(enemy);
                    break;
                case "Alien2":
                    Enemy enemy2 = new Alien2(sd.x, sd.y);
                    enemies.add(enemy2);
                    break;
                case "PowerUp-SpeedUp":
                    PowerUp speedUp = new SpeedUp(sd.x, sd.y);
                    powerups.add(speedUp);
                    break;
                case "PowerUp-MultiShot":
                    PowerUp multiShot = new MultiShotPowerUp(sd.x, sd.y);
                    powerups.add(multiShot);
                    break;
                case "VICTORY":
                    // Player survived 5 minutes!
                    inGame = false;
                    message = "Stage 1 Complete! You survived 5 minutes!";
                    break;
                default:
                    System.out.println("Unknown enemy type: " + sd.type);
                    break;
            }
        }

        // Victory condition: Survive 5 minutes (300 seconds)
        if (gameTimeSeconds >= 300) {
            inGame = false;
            timer.stop();
            message = "Stage 1 Complete! You survived 5 minutes!";
        }

        // player
        player.act();
        
        // Auto-fire when multishot is active
        if (player.canAutoFire() && shots.size() < player.getMaxShots()) {
            int x = player.getX();
            int y = player.getY();
            
            // Create auto-fire shot
            Shot autoShot = new Shot(x, y);
            shots.add(autoShot);
            
            // Create additional spread shots
            if (player.hasMultishot() && shots.size() < player.getMaxShots() - 2) {
                Shot spreadShot1 = new Shot(x, y - 20);
                Shot spreadShot2 = new Shot(x, y + 20);
                shots.add(spreadShot1);
                shots.add(spreadShot2);
            }
            
            player.triggerAutoFire();
        }

        // Power-ups
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
                // Check if enemy has gone off the left side of the screen
                if (enemy.getX() < -50) { // Give some buffer for image width
                    inGame = false;
                    message = "Invasion!";
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

                int x = shot.getX();
                // Remove old shot movement code since Shot.act() now handles movement
                // For sideways gameplay, remove shots that go off the right side
                if (x > BOARD_WIDTH) {
                    shot.die();
                    shotsToRemove.add(shot);
                }
            }
        }
        shots.removeAll(shotsToRemove);

        // enemies
        // for (Enemy enemy : enemies) {
        //     int x = enemy.getX();
        //     if (x >= BOARD_WIDTH - BORDER_RIGHT && direction != -1) {
        //         direction = -1;
        //         for (Enemy e2 : enemies) {
        //             e2.setY(e2.getY() + GO_DOWN);
        //         }
        //     }
        //     if (x <= BORDER_LEFT && direction != 1) {
        //         direction = 1;
        //         for (Enemy e : enemies) {
        //             e.setY(e.getY() + GO_DOWN);
        //         }
        //     }
        // }
        // for (Enemy enemy : enemies) {
        //     if (enemy.isVisible()) {
        //         int y = enemy.getY();
        //         if (y > GROUND - ALIEN_HEIGHT) {
        //             inGame = false;
        //             message = "Invasion!";
        //         }
        //         enemy.act(direction);
        //     }
        // }
        // bombs - collision detection
        // Bomb is with enemy, so it loops over enemies
        for (Enemy enemy : enemies) {

            // Phase-based shooting behavior
            boolean canShoot = false;
            int shootChance = 0;
            
            switch (currentPhase) {
                case 1:
                    // Phase 1: No shooting (0-90 seconds)
                    canShoot = false;
                    break;
                case 2:
                    // Phase 2: Occasional shooting (90-210 seconds)
                    canShoot = true;
                    shootChance = 800; // Very rare shooting
                    break;
                case 3:
                    // Phase 3: Regular shooting every 5 seconds (210-300 seconds)
                    canShoot = true;
                    shootChance = 300; // More frequent shooting
                    break;
            }
            
            int chance = randomizer.nextInt(shootChance > 0 ? shootChance : 1000);
            
            // Both Alien1 and Alien2 can shoot bombs
            if (enemy instanceof Alien1) {
                Alien1 alien = (Alien1) enemy;
                Alien1.Bomb bomb = alien.getBomb();

                if (canShoot && chance == CHANCE && enemy.isVisible() && bomb.isDestroyed()) {

                    bomb.setDestroyed(false);
                    bomb.setX(enemy.getX());
                    bomb.setY(enemy.getY());
                }

                int bombX = bomb.getX();
                int bombY = bomb.getY();
                int playerX = player.getX();
                int playerY = player.getY();

                if (player.isVisible() && !bomb.isDestroyed()
                        && bombX >= (playerX)
                        && bombX <= (playerX + PLAYER_WIDTH)
                        && bombY >= (playerY)
                        && bombY <= (playerY + PLAYER_HEIGHT)) {

                    bomb.setDestroyed(true);
                    explosions.add(new Explosion(playerX, playerY));
                    
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
                    bomb.act(); // Use bomb's act method for movement
                    // For sideways gameplay, remove bombs that go off the left side
                    if (bomb.getX() < 0) {
                        bomb.setDestroyed(true);
                    }
                }
            }
            
            // Handle Alien2 bombs
            if (enemy instanceof Alien2) {
                Alien2 alien = (Alien2) enemy;
                Alien2.Bomb bomb = alien.getBomb();

                if (canShoot && chance == CHANCE && enemy.isVisible() && bomb.isDestroyed()) {

                    bomb.setDestroyed(false);
                    bomb.setX(enemy.getX());
                    bomb.setY(enemy.getY());
                }

                int bombX = bomb.getX();
                int bombY = bomb.getY();
                int playerX = player.getX();
                int playerY = player.getY();

                if (player.isVisible() && !bomb.isDestroyed()
                        && bombX >= (playerX)
                        && bombX <= (playerX + PLAYER_WIDTH)
                        && bombY >= (playerY)
                        && bombY <= (playerY + PLAYER_HEIGHT)) {

                    bomb.setDestroyed(true);
                    explosions.add(new Explosion(playerX, playerY));
                    
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
                    bomb.act(); // Use bomb's act method for movement
                    // For sideways gameplay, remove bombs that go off the left side
                    if (bomb.getX() < 0) {
                        bomb.setDestroyed(true);
                    }
                }
            }
        }
        
        // Update explosions
        for (Explosion explosion : explosions) {
            explosion.act(); // Handle explosion animation lifecycle
        }
        
        // Remove completed explosions
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

    private void doGameCycle() {
        frame++;
        update();
        repaint();
    }

    private class GameCycle implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            doGameCycle();
        }
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
            System.out.println("Scene2.keyPressed: " + e.getKeyCode());

            int key = e.getKeyCode();

            // Handle restart when game is over
            if (!inGame && key == KeyEvent.VK_R) {
                restartGame();
                return;
            }

            // Only handle player controls when game is running
            if (inGame) {
                player.keyPressed(e);

                int x = player.getX();
                int y = player.getY();

                if (key == KeyEvent.VK_SPACE) {
                    int maxShots = player.getMaxShots();
                    System.out.println("Shots: " + shots.size() + "/" + maxShots);
                    if (shots.size() < maxShots) {
                        // Create primary shot
                        Shot shot = new Shot(x, y);
                        shots.add(shot);
                        
                        // Create additional shots if player has multishot active
                        if (player.hasMultishot()) {
                            int extraShots = player.getExtraShots();
                            for (int i = 1; i <= extraShots && shots.size() < maxShots; i++) {
                                // Create shots with spread pattern
                                int spreadY = y + (i * 25) - (extraShots * 12); // Wider spread
                                Shot extraShot = new Shot(x, spreadY);
                                shots.add(extraShot);
                            }
                            
                            // Add rapid-fire burst - create 3 more shots with slight delay
                            if (shots.size() < maxShots - 2) {
                                for (int burst = 0; burst < 3 && shots.size() < maxShots; burst++) {
                                    Shot burstShot = new Shot(x + (burst * 8), y + (burst * 5) - 5);
                                    shots.add(burstShot);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void initStarField() {
        // Initialize random stars across the screen
        for (int i = 0; i < 100; i++) {
            int x = randomizer.nextInt(BOARD_WIDTH + 200); // Start some stars off-screen
            int y = randomizer.nextInt(BOARD_HEIGHT);
            int size = randomizer.nextInt(3) + 1; // Stars size 1-3
            int speed = 1; // All stars move at the same slow speed
            
            // Create different colored stars
            Color color = Color.WHITE;
            int colorChoice = randomizer.nextInt(10);
            if (colorChoice < 7) {
                color = Color.WHITE;
            } else if (colorChoice < 9) {
                color = new Color(200, 200, 255); // Light blue
            } else {
                color = new Color(255, 255, 200); // Light yellow
            }
            
            stars.add(new Star(x, y, size, speed, color));
        }
    }

    private void updateStarField() {
        // Move stars from right to left (simulating forward movement)
        for (Star star : stars) {
            star.x -= star.speed;
            
            // If star goes off the left side, respawn it on the right side
            if (star.x < -10) {
                star.x = BOARD_WIDTH + randomizer.nextInt(100);
                star.y = randomizer.nextInt(BOARD_HEIGHT);
            }
        }
        
        // Occasionally add new stars from the right
        if (randomizer.nextInt(20) == 0) {
            int y = randomizer.nextInt(BOARD_HEIGHT);
            int size = randomizer.nextInt(3) + 1;
            int speed = 1; // All new stars also move at speed 1
            
            Color color = Color.WHITE;
            int colorChoice = randomizer.nextInt(10);
            if (colorChoice < 7) {
                color = Color.WHITE;
            } else if (colorChoice < 9) {
                color = new Color(200, 200, 255);
            } else {
                color = new Color(255, 255, 200);
            }
            
            stars.add(new Star(BOARD_WIDTH + 10, y, size, speed, color));
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
        
        // Reset phase system
        currentPhase = 1;
        lastPhaseChangeTime = 0;
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

        System.out.println("Game restarted!");
    }
}
