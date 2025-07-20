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
        // Clear any hardcoded spawns - all spawning now handled by phase generators
        // Phase-based spawning provides better control and balancing
        
        // 5-minute gameplay (18000 frames at 60fps)
        // Phase 1: 0-90 seconds (frames 0-5400) - Learning phase, Alien1 only
        generatePhase1Spawns();
        
        // Phase 2: 90-210 seconds (frames 5400-12600) - Threat introduction, Alien1 only
        generatePhase2Spawns();
        
        // Phase 3: 210-300 seconds (frames 12600-18000) - Escalation, mixed enemies
        generatePhase3Spawns();
        
        // NEW: Distribute powerups evenly across the entire gameplay
        distributePowerupsEvenly();
    }
    
    private void distributePowerupsEvenly() {
        // Spawn one SpeedUp and one AddBullet at game start (avoid frame conflicts)
        spawnMap.put(250, new SpawnDetails("PowerUp-SpeedUp", BOARD_WIDTH - 50, 200));
        spawnMap.put(500, new SpawnDetails("PowerUp-AddBullet", BOARD_WIDTH - 50, 250));
        
        // Add MultiShot powerups earlier than before (15-20 seconds earlier)
        // First MultiShot at ~30 seconds instead of ~47 seconds
        spawnMap.put(1800, new SpawnDetails("PowerUp-MultiShot", BOARD_WIDTH - 50, 300)); // 30 seconds
        spawnMap.put(4800, new SpawnDetails("PowerUp-MultiShot", BOARD_WIDTH - 50, 200)); // 80 seconds
        spawnMap.put(9000, new SpawnDetails("PowerUp-MultiShot", BOARD_WIDTH - 50, 350)); // 150 seconds
        spawnMap.put(13200, new SpawnDetails("PowerUp-MultiShot", BOARD_WIDTH - 50, 250)); // 220 seconds
        
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
        // Phase 1: Frames 0-5400 (0-90 seconds) - Learning phase with sparse Alien1 only
        for (int frame = 1500; frame <= 5400; frame += 150) { // Every 2.5 seconds (much sparser)
            int y = 100 + randomizer.nextInt(400); // Random Y position
            // Only spawn Alien1 enemies - no Alien2 in phase 1
            spawnMap.put(frame, new SpawnDetails("Alien1", BOARD_WIDTH - 50, y));
            
            // No formations in Phase 1 - single enemies only for learning
        }
    }
    
    private void generatePhase2Spawns() {
        // Phase 2: Frames 5400-12600 (90-210 seconds) - Threat introduction with Alien1 only
        
        // Remove complex wave events for Phase 2 - keep it simple
        
        for (int frame = 5400; frame <= 12600; frame += 120) { // Every 2 seconds (sparser than before)
            int y = 100 + randomizer.nextInt(400);
            // Only spawn Alien1 enemies - no Alien2 in phase 2
            spawnMap.put(frame, new SpawnDetails("Alien1", BOARD_WIDTH - 50, y));
            
            // Simple pair formations occasionally (10% chance)
            if (randomizer.nextInt(10) == 0) {
                // Create simple pair formation
                spawnMap.put(frame + 30, new SpawnDetails("Alien1", BOARD_WIDTH - 50, y - 60));
            }
        }
    }
    
    private void generatePhase3Spawns() {
        // Phase 3: Frames 12600-18000 (210-300 seconds) - Escalation with mixed enemies
        // Extended to full 5 minutes - no dead time
        
        // Add moderate wave events for variety
        spawnMap.put(14400, new SpawnDetails("WAVE_MIXED", 0, 0)); // 4 minutes in
        spawnMap.put(16800, new SpawnDetails("WAVE_ELITE", 0, 0)); // 4.7 minutes in
        
        for (int frame = 12600; frame <= 18000; frame += 90) { // Every 1.5 seconds - balanced pace
            int y = 100 + randomizer.nextInt(400);
            // 70% Alien1, 30% Alien2 mix for escalation
            String enemyType = randomizer.nextInt(10) < 7 ? "Alien1" : "Alien2";
            spawnMap.put(frame, new SpawnDetails(enemyType, BOARD_WIDTH - 50, y));
            
            // Line formations occasionally (20% chance)
            if (randomizer.nextInt(5) == 0) { 
                // Create 3-enemy line formation
                spawnMap.put(frame + 20, new SpawnDetails(enemyType, BOARD_WIDTH - 50, y - 50));
                spawnMap.put(frame + 40, new SpawnDetails(enemyType, BOARD_WIDTH - 50, y + 50));
            }
            
            // Small cluster formations occasionally (10% chance)
            if (randomizer.nextInt(10) == 0) {
                spawnMap.put(frame + 15, new SpawnDetails("Alien1", BOARD_WIDTH - 50, y - 30));
                spawnMap.put(frame + 30, new SpawnDetails("Alien1", BOARD_WIDTH - 50, y + 30));
            }
        }
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