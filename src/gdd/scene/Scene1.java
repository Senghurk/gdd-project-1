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
            // Only Alien1 has bombs
            if (e instanceof Alien1) {
                Alien1 alien = (Alien1) e;
                Alien1.Bomb b = alien.getBomb();
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
                default:
                    System.out.println("Unknown enemy type: " + sd.type);
                    break;
            }
        }

        if (deaths == NUMBER_OF_ALIENS_TO_DESTROY) {
            inGame = false;
            timer.stop();
            message = "Game won!";
        }

        // player
        player.act();

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

            int chance = randomizer.nextInt(15);
            
            // Only Alien1 has bombs, so check if it's an Alien1
            if (enemy instanceof Alien1) {
                Alien1 alien = (Alien1) enemy;
                Alien1.Bomb bomb = alien.getBomb();

                if (chance == CHANCE && enemy.isVisible() && bomb.isDestroyed()) {

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

                    var ii = new ImageIcon(IMG_EXPLOSION);
                    player.setImage(ii.getImage());
                    player.setDying(true);
                    bomb.setDestroyed(true);
                    explosions.add(new Explosion(playerX, playerY));
                    inGame = false;
                    message = "Game Over!";
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
                    System.out.println("Shots: " + shots.size());
                    if (shots.size() < 4) {
                        // Create primary shot
                        Shot shot = new Shot(x, y);
                        shots.add(shot);
                        
                        // Create additional shots if player has multishot active
                        if (player.hasMultishot()) {
                            int extraShots = player.getExtraShots();
                            for (int i = 1; i <= extraShots && shots.size() < 4; i++) {
                                // Create shots with slight Y offset for spread effect
                                Shot extraShot = new Shot(x, y + (i * 15) - (extraShots * 7));
                                shots.add(extraShot);
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
