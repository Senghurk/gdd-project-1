package gdd.scene;

import static gdd.Global.*;
import gdd.SpawnDetails;
import gdd.sprite.Alien1;
import gdd.sprite.Alien2;
import gdd.sprite.Enemy;
import gdd.powerup.PowerUp;
import gdd.powerup.SpeedUp;
import gdd.powerup.AddBulletPowerUp;
import gdd.powerup.MultiShotPowerUp;
import gdd.sprite.Player;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class SpawnManager {
    
    private HashMap<Integer, SpawnDetails> spawnMap;
    private Random randomizer;
    
    // Wave system state
    private boolean waveActive = false;
    private int waveEndFrame = 0;
    private String currentWaveType = "";
    private int enemiesInCurrentWave = 0;
    
    public SpawnManager(HashMap<Integer, SpawnDetails> spawnMap, Random randomizer) {
        this.spawnMap = spawnMap;
        this.randomizer = randomizer;
    }
    
    public SpawnResult spawnEnemies(int frame, Player player) {
        SpawnResult result = new SpawnResult();
        
        // Check enemy spawn
        SpawnDetails sd = spawnMap.get(frame);
        if (sd != null) {
            // Handle string-based spawn details (Scene1)
            if (sd.type != null && !sd.type.isEmpty()) {
                handleSpawnDetails(sd, result, player, frame);
            } 
            // Handle count-based spawn details (Scene2)
            else {
                // Spawn Alien1s
                for (int i = 0; i < sd.alien1Count; i++) {
                    int y = randomizer.nextInt(BOARD_HEIGHT - 100) + 50; // Random Y position
                    Enemy alien = new Alien1(BOARD_WIDTH, y);
                    result.enemies.add(alien);
                }
                
                // Spawn Alien2s
                for (int i = 0; i < sd.alien2Count; i++) {
                    int y = randomizer.nextInt(BOARD_HEIGHT - 100) + 50;
                    Enemy alien = new Alien2(BOARD_WIDTH, y);
                    result.enemies.add(alien);
                }
                
                // Handle powerups
                if (sd.spawnSpeedPowerup) {
                    int y = randomizer.nextInt(BOARD_HEIGHT - 100) + 50;
                    PowerUp speedup = new SpeedUp(BOARD_WIDTH, y);
                    result.powerups.add(speedup);
                }
                
                if (sd.spawnBulletPowerup) {
                    int y = randomizer.nextInt(BOARD_HEIGHT - 100) + 50;
                    PowerUp bulletup = new AddBulletPowerUp(BOARD_WIDTH, y);
                    result.powerups.add(bulletup);
                }
                
                if (sd.spawnMultiShotPowerup) {
                    int y = randomizer.nextInt(BOARD_HEIGHT - 100) + 50;
                    PowerUp multishot = new MultiShotPowerUp(BOARD_WIDTH, y);
                    result.powerups.add(multishot);
                }
            }
        }
        
        return result;
    }
    
    private void handleSpawnDetails(SpawnDetails sd, SpawnResult result, Player player, int frame) {
        switch (sd.type) {
            case "Alien1":
                Enemy enemy = new Alien1(sd.x, sd.y);
                result.enemies.add(enemy);
                break;
            case "Alien2":
                Enemy enemy2 = new Alien2(sd.x, sd.y);
                result.enemies.add(enemy2);
                break;
            case "PowerUp-SpeedUp":
                // Only spawn speed if player can actually use it
                if (player.getCurrentSpeed() < player.getMaxSpeed()) {
                    PowerUp speedUp = new SpeedUp(sd.x, sd.y);
                    result.powerups.add(speedUp);
                }
                break;
            case "PowerUp-AddBullet":
                // Only spawn bullet powerup if player can actually use it
                if (player.getCurrentBulletCount() < player.getMaxBulletCount()) {
                    PowerUp addBullet = new AddBulletPowerUp(sd.x, sd.y);
                    result.powerups.add(addBullet);
                }
                break;
            case "PowerUp-MultiShot":
                PowerUp multiShot = new MultiShotPowerUp(sd.x, sd.y);
                result.powerups.add(multiShot);
                break;
            case "VICTORY":
                // Player survived 5 minutes!
                result.gameOver = true;
                result.victoryMessage = "Stage 1 Complete! You survived 5 minutes!";
                break;
            case "WAVE_SWARM":
                startWave("SWARM", 360, frame, result); // 6 second swarm wave
                break;
            case "WAVE_ELITE":
                startWave("ELITE", 480, frame, result); // 8 second elite wave
                break;
            case "WAVE_MIXED":
                startWave("MIXED", 420, frame, result); // 7 second mixed wave
                break;
            case "WAVE_FINAL":
                startWave("FINAL", 600, frame, result); // 10 second final wave
                break;
            default:
                System.out.println("Unknown enemy type: " + sd.type);
                break;
        }
    }
    
    private void startWave(String waveType, int durationFrames, int currentFrame, SpawnResult result) {
        waveActive = true;
        currentWaveType = waveType;
        waveEndFrame = currentFrame + durationFrames;
        enemiesInCurrentWave = 0;
        
        // Spawn wave enemies immediately
        spawnWaveEnemies(waveType, result);
        
        // Set wave info in result
        result.waveActive = true;
        result.waveType = waveType;
        result.waveEndFrame = waveEndFrame;
    }
    
    private void spawnWaveEnemies(String waveType, SpawnResult result) {
        int centerY = BOARD_HEIGHT / 2;
        
        switch (waveType) {
            case "SWARM":
                // 6 weak enemies in tight formation
                for (int i = 0; i < 6; i++) {
                    int y = centerY - 60 + (i * 24);
                    result.enemies.add(new Alien1(BOARD_WIDTH - 50, y));
                    enemiesInCurrentWave++;
                }
                break;
                
            case "ELITE":
                // 3 strong enemies spread out
                result.enemies.add(new Alien2(BOARD_WIDTH - 50, centerY - 80));
                result.enemies.add(new Alien2(BOARD_WIDTH - 50, centerY));
                result.enemies.add(new Alien2(BOARD_WIDTH - 50, centerY + 80));
                enemiesInCurrentWave += 3;
                break;
                
            case "MIXED":
                // 2 Alien2 flanking 3 Alien1
                result.enemies.add(new Alien2(BOARD_WIDTH - 50, centerY - 100));
                result.enemies.add(new Alien1(BOARD_WIDTH - 50, centerY - 40));
                result.enemies.add(new Alien1(BOARD_WIDTH - 50, centerY));
                result.enemies.add(new Alien1(BOARD_WIDTH - 50, centerY + 40));
                result.enemies.add(new Alien2(BOARD_WIDTH - 50, centerY + 100));
                enemiesInCurrentWave += 5;
                break;
                
            case "FINAL":
                // 4 Alien2 in diamond formation
                result.enemies.add(new Alien2(BOARD_WIDTH - 50, centerY));        // Front
                result.enemies.add(new Alien2(BOARD_WIDTH - 20, centerY - 60));   // Top
                result.enemies.add(new Alien2(BOARD_WIDTH - 20, centerY + 60));   // Bottom
                result.enemies.add(new Alien2(BOARD_WIDTH + 10, centerY));        // Rear
                enemiesInCurrentWave += 4;
                break;
        }
    }
    

    
    // Getter methods for wave state
    public boolean isWaveActive() {
        return waveActive;
    }
    
    public String getCurrentWaveType() {
        return currentWaveType;
    }
    
    public int getWaveEndFrame() {
        return waveEndFrame;
    }
    
    public void loadSpawnDetails() {
        // TODO load this from a file
        // For sideways gameplay, spawn enemies from the right side (x = BOARD_WIDTH) with varying y positions
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
        
        // Add Alien2 enemies (powerups handled by distributePowerupsEvenly)
        spawnMap.put(600, new SpawnDetails("Alien2", BOARD_WIDTH - 50, 150));
        spawnMap.put(650, new SpawnDetails("Alien2", BOARD_WIDTH - 50, 350));
        spawnMap.put(700, new SpawnDetails("Alien2", BOARD_WIDTH - 50, 250));
        
        // More varied spawns for extended gameplay
        spawnMap.put(800, new SpawnDetails("Alien1", BOARD_WIDTH - 50, 100));
        spawnMap.put(850, new SpawnDetails("Alien2", BOARD_WIDTH - 50, 200));
        spawnMap.put(950, new SpawnDetails("Alien1", BOARD_WIDTH - 50, 400));
        spawnMap.put(1000, new SpawnDetails("Alien2", BOARD_WIDTH - 50, 150));
        
        // 5-minute gameplay (18000 frames at 60fps)
        // Phase 1: Minutes 1-2 (frames 1000-7200) - Moderate difficulty
        generatePhase1Spawns();
        
        // Phase 2: Minutes 2-4 (frames 7200-12600) - Increased difficulty  
        generatePhase2Spawns();
        
        // Phase 3: Minutes 4-5 (frames 12600-14400) - High difficulty
        // Last minute (frames 14400-18000): No enemies - Player relief period
        generatePhase3Spawns();
        
        // NEW: Distribute powerups evenly across the entire gameplay
        distributePowerupsEvenly();
    }
    
    private void distributePowerupsEvenly() {
        // Spawn one SpeedUp and one AddBullet at game start (avoid frame conflicts)
        spawnMap.put(250, new SpawnDetails("PowerUp-SpeedUp", BOARD_WIDTH - 50, 200));
        spawnMap.put(500, new SpawnDetails("PowerUp-AddBullet", BOARD_WIDTH - 50, 250));
        
        // 5-minute gameplay = 18000 frames at 60fps
        // 6 remaining powerups (3 speed + 3 bullet) spread evenly across rest of game
        // Spawn every ~2800 frames (46.7 seconds) with some randomization
        
        int totalFrames = 18000; // 5 minutes
        int remainingPowerups = 6; // 3 speed + 3 bullet (already spawned 2)
        int baseInterval = (totalFrames - 2000) / remainingPowerups; // ~2666 frames, start after frame 2000
        
        // Create list of remaining powerup types to distribute
        String[] powerupTypes = {"PowerUp-SpeedUp", "PowerUp-AddBullet", "PowerUp-SpeedUp", "PowerUp-AddBullet", 
                                "PowerUp-SpeedUp", "PowerUp-AddBullet"};
        
        // Shuffle the powerup types for random distribution
        for (int i = 0; i < powerupTypes.length; i++) {
            int randomIndex = randomizer.nextInt(powerupTypes.length);
            String temp = powerupTypes[i];
            powerupTypes[i] = powerupTypes[randomIndex];
            powerupTypes[randomIndex] = temp;
        }
        
        // Distribute remaining powerups evenly with some randomization
        for (int i = 0; i < remainingPowerups; i++) {
            // Calculate base spawn time with some randomization (±20 seconds)
            int baseTime = 2000 + (i + 1) * baseInterval; // Start after 2000 frames
            int randomOffset = randomizer.nextInt(2400) - 1200; // ±20 seconds
            int spawnFrame = Math.max(2000, Math.min(17000, baseTime + randomOffset));
            
            // Random Y position
            int y = 100 + randomizer.nextInt(400);
            
            // Add to spawn map
            spawnMap.put(spawnFrame, new SpawnDetails(powerupTypes[i], BOARD_WIDTH - 50, y));
        }
    }

    private void generatePhase1Spawns() {
        // Phase 1: Frames 0-5400 (0-90 seconds) - Moderate difficulty
        for (int frame = 1200; frame <= 5400; frame += 80) { // Every 1.3 seconds (tighter)
            int y = 100 + randomizer.nextInt(400); // Random Y position
            // Only spawn enemies, powerups are handled by distributePowerupsEvenly()
            String enemyType = randomizer.nextInt(3) == 0 ? "Alien2" : "Alien1"; // 33% Alien2
            spawnMap.put(frame, new SpawnDetails(enemyType, BOARD_WIDTH - 50, y));
            
            // Add formation spawns (V-formation)
            if (randomizer.nextInt(5) == 0) {
                // Create V-formation with 3 enemies
                spawnMap.put(frame + 15, new SpawnDetails(enemyType, BOARD_WIDTH - 50, y - 40));
                spawnMap.put(frame + 25, new SpawnDetails(enemyType, BOARD_WIDTH - 50, y + 40));
                spawnMap.put(frame + 35, new SpawnDetails(enemyType, BOARD_WIDTH - 50, y)); // Leader slightly behind
            }
        }
    }
    
    private void generatePhase2Spawns() {
        // Phase 2: Frames 5400-12600 (90-210 seconds) - Wave-based difficulty
        
        // Add special wave events
        spawnMap.put(6000, new SpawnDetails("WAVE_SWARM", 0, 0)); // 30 seconds in
        spawnMap.put(8400, new SpawnDetails("WAVE_ELITE", 0, 0)); // 70 seconds in  
        spawnMap.put(10800, new SpawnDetails("WAVE_MIXED", 0, 0)); // 110 seconds in
        
        for (int frame = 5400; frame <= 12600; frame += 60) { // Every 1 second (much tighter)
            int y = 100 + randomizer.nextInt(400);
            // Only spawn enemies, powerups are handled by distributePowerupsEvenly()
            String enemyType = randomizer.nextInt(2) == 0 ? "Alien2" : "Alien1"; // 50% Alien2
            spawnMap.put(frame, new SpawnDetails(enemyType, BOARD_WIDTH - 50, y));
            
            // Line formation spawns
            if (randomizer.nextInt(4) == 0) {
                // Create horizontal line formation
                spawnMap.put(frame + 10, new SpawnDetails(enemyType, BOARD_WIDTH - 50, y - 50));
                spawnMap.put(frame + 20, new SpawnDetails(enemyType, BOARD_WIDTH - 50, y + 50));
            }
            
            // Diamond formation occasionally  
            if (randomizer.nextInt(7) == 0) {
                spawnMap.put(frame + 15, new SpawnDetails("Alien2", BOARD_WIDTH - 50, y - 30));
                spawnMap.put(frame + 25, new SpawnDetails("Alien2", BOARD_WIDTH - 50, y + 30));
                spawnMap.put(frame + 35, new SpawnDetails("Alien1", BOARD_WIDTH - 50, y)); // Center
                spawnMap.put(frame + 45, new SpawnDetails("Alien2", BOARD_WIDTH - 50, y)); // Rear
            }
        }
    }
    
    private void generatePhase3Spawns() {
        // Phase 3: Frames 12600-14400 (210-240 seconds) - Intense waves
        // Boss spawns at 4 minutes (240 seconds = 14400 frames)
        
        // Add intense wave events  
        spawnMap.put(13200, new SpawnDetails("WAVE_ELITE", 0, 0)); // 30 seconds in
        spawnMap.put(13800, new SpawnDetails("WAVE_SWARM", 0, 0)); // 50 seconds in
        spawnMap.put(14200, new SpawnDetails("WAVE_FINAL", 0, 0)); // 70 seconds in
        
        for (int frame = 12600; frame <= 14400; frame += 45) { // Every 0.75 seconds (intense) - STOP at 14400
            int y = 100 + randomizer.nextInt(400);
            // Only spawn enemies, powerups are handled by distributePowerupsEvenly()
            String enemyType = randomizer.nextInt(4) < 3 ? "Alien2" : "Alien1"; // 75% Alien2
            spawnMap.put(frame, new SpawnDetails(enemyType, BOARD_WIDTH - 50, y));
            
            // Phase 3: Aggressive swarm and elite formations
            if (randomizer.nextInt(3) == 0) { 
                // Swarm formation - 4 enemies in close proximity
                spawnMap.put(frame + 8, new SpawnDetails("Alien2", BOARD_WIDTH - 50, y - 25));
                spawnMap.put(frame + 16, new SpawnDetails("Alien2", BOARD_WIDTH - 50, y + 25));
                spawnMap.put(frame + 24, new SpawnDetails("Alien1", BOARD_WIDTH - 50, y - 15));
                spawnMap.put(frame + 32, new SpawnDetails("Alien1", BOARD_WIDTH - 50, y + 15));
            }
            
            // Elite pincer formation occasionally
            if (randomizer.nextInt(8) == 0) {
                spawnMap.put(frame + 10, new SpawnDetails("Alien2", BOARD_WIDTH - 50, 100)); // Top pincer
                spawnMap.put(frame + 10, new SpawnDetails("Alien2", BOARD_WIDTH - 50, BOARD_HEIGHT - 150)); // Bottom pincer
                spawnMap.put(frame + 30, new SpawnDetails("Alien2", BOARD_WIDTH - 50, y)); // Center breakthrough
            }
        }
        
        // After boss defeat (frames 14400-18000): Continue spawning normal aliens until 5 minutes
        // This will be handled dynamically in Scene2 based on boss defeat status
    }

    public void loadScene2SpawnDetails() {
        // Clear any existing spawn details
        spawnMap.clear();
        
        // Initial buffer period with "Level 2" text (3 seconds)
        // First wave starts at 4 seconds (frame 240)
        spawnMap.put(240, new SpawnDetails(2, 1, false, false, true)); // 2 Alien1s and 1 Alien2 with initial multishot
        
        // Major waves every 30 seconds (1800 frames) - STOP at 14400 (4 minutes)
        for (int frame = 600; frame < 14400; frame += 1800) {
            int a1 = 2 + randomizer.nextInt(4); // 2-5 Alien1s
            int a2 = 1 + randomizer.nextInt(3); // 1-3 Alien2s
            spawnMap.put(frame, new SpawnDetails(a1, a2, false, false, false));
        }
        // Multishot powerups every 45 seconds (2700 frames), not on frames with major waves
        for (int frame = 1800; frame < 14400; frame += 2700) {
            if (!spawnMap.containsKey(frame)) {
                spawnMap.put(frame, new SpawnDetails(0, 0, false, false, true));
            }
        }
        // Continuous action: spawn smaller waves every 5 seconds (300 frames) - STOP at 14400
        for (int frame = 300; frame < 14400; frame += 300) {
            if (!spawnMap.containsKey(frame)) {
                int alien1Count = 1 + randomizer.nextInt(3); // 1-3 Alien1s
                int alien2Count = randomizer.nextInt(2); // 0-1 Alien2s
                spawnMap.put(frame, new SpawnDetails(alien1Count, alien2Count, false, false, false));
            }
        }
        // Last minute (frames 14400-18000): NO ENEMIES - Player gets a break before victory
    }

    // Result class to hold spawn results
    public static class SpawnResult {
        public List<Enemy> enemies = new ArrayList<>();
        public List<PowerUp> powerups = new ArrayList<>();
        public boolean gameOver = false;
        public String victoryMessage = "";
        public boolean waveActive = false;
        public String waveType = "";
        public int waveEndFrame = 0;
    }
} 