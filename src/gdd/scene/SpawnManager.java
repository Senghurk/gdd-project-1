package gdd.scene;

import gdd.Global;
import static gdd.Global.*;
import gdd.SpawnDetails;
import gdd.sprite.Alien1;
import gdd.sprite.Alien2;
import gdd.sprite.Enemy;
import gdd.powerup.PowerUp;
import gdd.powerup.SpeedUp;
import gdd.powerup.AddBulletPowerUp;
import gdd.powerup.MultiShotPowerUp;
import gdd.powerup.HealthPickup;
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
                // Spawn Alien1s - USE MODE-AWARE POSITIONING
                for (int i = 0; i < sd.alien1Count; i++) {
                    Enemy alien = createAlien1(); // Use mode-aware creation method
                    result.enemies.add(alien);
                }
                
                // Spawn Alien2s - USE MODE-AWARE POSITIONING  
                for (int i = 0; i < sd.alien2Count; i++) {
                    Enemy alien = createAlien2(); // Use mode-aware creation method
                    result.enemies.add(alien);
                }
                
                // Handle powerups - USE MODE-AWARE POSITIONING
                if (sd.spawnSpeedPowerup) {
                    PowerUp speedup = createPowerUp("PowerUp-SpeedUp");
                    result.powerups.add(speedup);
                }
                
                if (sd.spawnBulletPowerup) {
                    PowerUp bulletup = createPowerUp("PowerUp-AddBullet");
                    result.powerups.add(bulletup);
                }
                
                if (sd.spawnMultiShotPowerup) {
                    PowerUp multishot = createPowerUp("PowerUp-MultiShot");
                    result.powerups.add(multishot);
                }
                
                if (sd.spawnHealthPowerup) {
                    PowerUp health = createPowerUp("PowerUp-Health");
                    result.powerups.add(health);
                }
            }
        }
        
        return result;
    }
    
    // NEW: Mode-aware enemy creation methods (DRY principle)
    private Enemy createAlien1() {
        return new Alien1(Global.getEnemySpawnX(), Global.getEnemySpawnY());
    }
    
    private Enemy createAlien2() {
        return new Alien2(Global.getEnemySpawnX(), Global.getEnemySpawnY());
    }
    
    // NEW: Mode-aware powerup creation method (DRY principle)
    private PowerUp createPowerUp(String type) {
        int x = Global.getEnemySpawnX(); // Use same spawn logic as enemies
        int y = Global.getEnemySpawnY();
        
        switch (type) {
            case "PowerUp-SpeedUp":
                return new SpeedUp(x, y);
            case "PowerUp-AddBullet":
                return new AddBulletPowerUp(x, y);
            case "PowerUp-MultiShot":
                return new MultiShotPowerUp(x, y);
            case "PowerUp-Health":
                return new HealthPickup(x, y);
            default:
                return null;
        }
    }
    
    private void handleSpawnDetails(SpawnDetails sd, SpawnResult result, Player player, int frame) {
        switch (sd.type) {
            case "Alien1":
                result.enemies.add(createAlien1());
                break;
            case "Alien2":
                result.enemies.add(createAlien2());
                break;
            case "PowerUp-SpeedUp":
                // Only spawn speed if player can actually use it
                if (player.getCurrentSpeed() < player.getMaxSpeed()) {
                    result.powerups.add(createPowerUp("PowerUp-SpeedUp"));
                }
                break;
            case "PowerUp-AddBullet":
                // Only spawn bullet powerup if player can actually use it
                if (player.getCurrentBulletCount() < player.getMaxBulletCount()) {
                    result.powerups.add(createPowerUp("PowerUp-AddBullet"));
                }
                break;
            case "PowerUp-MultiShot":
                result.powerups.add(createPowerUp("PowerUp-MultiShot"));
                break;
            case "PowerUp-Health":
                result.powerups.add(createPowerUp("PowerUp-Health"));
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
        switch (waveType) {
            case "SWARM":
                // 6 weak enemies in tight formation using mode-aware positioning
                for (int i = 0; i < 6; i++) {
                    result.enemies.add(createAlien1());
                    enemiesInCurrentWave++;
                }
                break;
                
            case "ELITE":
                // 3 strong enemies spread out using mode-aware positioning
                result.enemies.add(createAlien2());
                result.enemies.add(createAlien2());
                result.enemies.add(createAlien2());
                enemiesInCurrentWave += 3;
                break;
                
            case "MIXED":
                // 2 Alien2 flanking 3 Alien1 using mode-aware positioning
                result.enemies.add(createAlien2());
                result.enemies.add(createAlien1());
                result.enemies.add(createAlien1());
                result.enemies.add(createAlien1());
                result.enemies.add(createAlien2());
                enemiesInCurrentWave += 5;
                break;
                
            case "FINAL":
                // 4 Alien2 in diamond formation using mode-aware positioning
                result.enemies.add(createAlien2());
                result.enemies.add(createAlien2());
                result.enemies.add(createAlien2());
                result.enemies.add(createAlien2());
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
        // Use SpawnDetails with just the type - position will be handled by createPowerUp method
        spawnMap.put(250, new SpawnDetails("PowerUp-SpeedUp", 0, 0));
        spawnMap.put(500, new SpawnDetails("PowerUp-AddBullet", 0, 0));
        
        // Add MultiShot powerups earlier than before (15-20 seconds earlier)
        // First MultiShot at ~30 seconds instead of ~47 seconds
        spawnMap.put(1800, new SpawnDetails("PowerUp-MultiShot", 0, 0)); // 30 seconds
        spawnMap.put(4800, new SpawnDetails("PowerUp-MultiShot", 0, 0)); // 80 seconds
        spawnMap.put(9000, new SpawnDetails("PowerUp-MultiShot", 0, 0)); // 150 seconds
        spawnMap.put(13200, new SpawnDetails("PowerUp-MultiShot", 0, 0)); // 220 seconds
        
        // Add Health pickups at specified times for Scene1
        spawnMap.put(10200, new SpawnDetails("PowerUp-Health", 0, 0)); // 2:50 (170 seconds)
        spawnMap.put(16200, new SpawnDetails("PowerUp-Health", 0, 0)); // 4:30 (270 seconds)
        
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
            
            // Add to spawn map - position will be handled by createPowerUp method
            spawnMap.put(spawnFrame, new SpawnDetails(powerupTypes[i], 0, 0));
        }
    }

    private void generatePhase1Spawns() {
        // Phase 1: Frames 0-5400 (0-90 seconds) - Learning phase with gradually increasing Alien1
        int frameCounter = 0;
        
        for (int frame = 600; frame <= 5400; frame += 150) { // Start at 10 seconds, every 2.5 seconds
            frameCounter++;
            
            // Gradually increase enemy count as phase progresses
            // First 10 spawns (25 seconds): single enemies
            // Next 10 spawns (25 seconds): occasional doubles (20% chance)
            // Final 12 spawns (40 seconds): more doubles (40% chance) and rare triples (10% chance)
            
            // Always spawn at least one Alien1
            spawnMap.put(frame, new SpawnDetails("Alien1", 0, 0));
            
            if (frameCounter > 10 && frameCounter <= 20) {
                // Middle of phase 1: 20% chance for a second enemy
                if (randomizer.nextInt(5) == 0) {
                    spawnMap.put(frame + 40, new SpawnDetails("Alien1", 0, 0));
                }
            } else if (frameCounter > 20) {
                // End of phase 1: more aggressive spawning
                if (randomizer.nextInt(5) < 2) { // 40% chance for second enemy
                    spawnMap.put(frame + 40, new SpawnDetails("Alien1", 0, 0));
                }
                if (randomizer.nextInt(10) == 0) { // 10% chance for third enemy
                    spawnMap.put(frame + 80, new SpawnDetails("Alien1", 0, 0));
                }
            }
        }
    }
    
    private void generatePhase2Spawns() {
        // Phase 2: Frames 5400-12600 (90-210 seconds) - Threat introduction with moderate formations
        
        int spawnCounter = 0;
        
        for (int frame = 5400; frame <= 12600; frame += 120) { // Every 2 seconds
            spawnCounter++;
            
            // Always spawn at least one Alien1
            spawnMap.put(frame, new SpawnDetails("Alien1", 0, 0));
            
            // Gradually increase formation complexity and frequency
            // First third (30 spawns): 15% chance for pairs
            // Second third (30 spawns): 25% chance for pairs, 5% chance for triangle formation
            // Final third (30 spawns): 30% chance for pairs, 10% chance for triangle, 5% chance for line formation
            
            if (spawnCounter <= 30) {
                // Early Phase 2: simple pairs occasionally
                if (randomizer.nextInt(20) < 3) { // 15% chance
                    spawnMap.put(frame + 30, new SpawnDetails("Alien1", 0, 0));
                }
            } else if (spawnCounter <= 60) {
                // Mid Phase 2: more pairs and triangle formations
                if (randomizer.nextInt(20) < 5) { // 25% chance for pair
                    spawnMap.put(frame + 30, new SpawnDetails("Alien1", 0, 0));
                }
                if (randomizer.nextInt(20) == 0) { // 5% chance for triangle
                    spawnMap.put(frame + 20, new SpawnDetails("Alien1", 0, 0));
                    spawnMap.put(frame + 50, new SpawnDetails("Alien1", 0, 0));
                }
            } else {
                // Late Phase 2: more complex formations but not overwhelming
                if (randomizer.nextInt(20) < 6) { // 30% chance for pair
                    spawnMap.put(frame + 30, new SpawnDetails("Alien1", 0, 0));
                }
                if (randomizer.nextInt(20) < 2) { // 10% chance for triangle
                    spawnMap.put(frame + 20, new SpawnDetails("Alien1", 0, 0));
                    spawnMap.put(frame + 50, new SpawnDetails("Alien1", 0, 0));
                }
                if (randomizer.nextInt(20) == 0) { // 5% chance for line formation
                    spawnMap.put(frame + 15, new SpawnDetails("Alien1", 0, 0));
                    spawnMap.put(frame + 45, new SpawnDetails("Alien1", 0, 0));
                    spawnMap.put(frame + 75, new SpawnDetails("Alien1", 0, 0));
                }
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
            // 70% Alien1, 30% Alien2 mix for escalation
            String enemyType = randomizer.nextInt(10) < 7 ? "Alien1" : "Alien2";
            // Position will be handled by createAlien1/createAlien2 methods
            spawnMap.put(frame, new SpawnDetails(enemyType, 0, 0));
            
            // Line formations occasionally (20% chance)
            if (randomizer.nextInt(5) == 0) { 
                // Create 3-enemy line formation
                spawnMap.put(frame + 20, new SpawnDetails(enemyType, 0, 0));
                spawnMap.put(frame + 40, new SpawnDetails(enemyType, 0, 0));
            }
            
            // Small cluster formations occasionally (10% chance)
            if (randomizer.nextInt(10) == 0) {
                spawnMap.put(frame + 15, new SpawnDetails("Alien1", 0, 0));
                spawnMap.put(frame + 30, new SpawnDetails("Alien1", 0, 0));
            }
        }
    }

    public void loadScene2SpawnDetails() {
        // Clear any existing spawn details
        spawnMap.clear();
        
        // Initial buffer period with "Level 2" text (3 seconds)
        // First wave starts at 4 seconds (frame 240)
        spawnMap.put(240, new SpawnDetails(2, 1, false, false, true)); // 2 Alien1s and 1 Alien2 with initial multishot
        
        // Major waves every 30 seconds (1800 frames) - STOP at 10800 (3 minutes)
        for (int frame = 600; frame < 10800; frame += 1800) {
            int a1 = 2 + randomizer.nextInt(4); // 2-5 Alien1s
            int a2 = 1 + randomizer.nextInt(3); // 1-3 Alien2s
            spawnMap.put(frame, new SpawnDetails(a1, a2, false, false, false));
        }
        // Multishot powerups every 45 seconds (2700 frames), not on frames with major waves
        for (int frame = 1800; frame < 10800; frame += 2700) {
            if (!spawnMap.containsKey(frame)) {
                spawnMap.put(frame, new SpawnDetails(0, 0, false, false, true));
            }
        }
        
        // Add Health pickups at specified times for Scene2
        spawnMap.put(10200, new SpawnDetails(0, 0, false, false, false, true)); // 2:50 (170 seconds)
        spawnMap.put(13800, new SpawnDetails(0, 0, false, false, false, true)); // 3:50 (230 seconds)  
        spawnMap.put(16200, new SpawnDetails(0, 0, false, false, false, true)); // 4:30 (270 seconds)
        // Continuous action: spawn smaller waves every 5 seconds (300 frames) - STOP at 10800 (3 minutes)
        for (int frame = 300; frame < 10800; frame += 300) {
            if (!spawnMap.containsKey(frame)) {
                int alien1Count = 1 + randomizer.nextInt(3); // 1-3 Alien1s
                int alien2Count = randomizer.nextInt(2); // 0-1 Alien2s
                spawnMap.put(frame, new SpawnDetails(alien1Count, alien2Count, false, false, false));
            }
        }
        // After 3 minutes (frames 10800-18000): Boss fight then easier enemies until victory
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