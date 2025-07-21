package gdd;

public class SpawnDetails {
    // For string-based spawn details (Scene1)
    public String type;
    public int x;
    public int y;

    // For count-based spawn details (Scene2)
    public int alien1Count;
    public int alien2Count;
    public boolean spawnSpeedPowerup;
    public boolean spawnBulletPowerup;
    public boolean spawnMultiShotPowerup;
    public boolean spawnHealthPowerup;

    // Constructor for string-based spawn details (Scene1)
    public SpawnDetails(String type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
        
        // Initialize count-based fields to defaults
        this.alien1Count = 0;
        this.alien2Count = 0;
        this.spawnSpeedPowerup = false;
        this.spawnBulletPowerup = false;
        this.spawnMultiShotPowerup = false;
        this.spawnHealthPowerup = false;
    }

    // Constructor for count-based spawn details (Scene2)
    public SpawnDetails(int alien1Count, int alien2Count, boolean spawnSpeedPowerup, boolean spawnBulletPowerup, boolean spawnMultiShotPowerup) {
        this(alien1Count, alien2Count, spawnSpeedPowerup, spawnBulletPowerup, spawnMultiShotPowerup, false);
    }
    
    // Constructor for count-based spawn details (Scene2) with health pickup
    public SpawnDetails(int alien1Count, int alien2Count, boolean spawnSpeedPowerup, boolean spawnBulletPowerup, boolean spawnMultiShotPowerup, boolean spawnHealthPowerup) {
        this.alien1Count = alien1Count;
        this.alien2Count = alien2Count;
        this.spawnSpeedPowerup = spawnSpeedPowerup;
        this.spawnBulletPowerup = spawnBulletPowerup;
        this.spawnMultiShotPowerup = spawnMultiShotPowerup;
        this.spawnHealthPowerup = spawnHealthPowerup;
        
        // Initialize string-based fields to defaults
        this.type = "";
        this.x = 0;
        this.y = 0;
    }

    // Constructor for Scene2 multishot-only powerups
    public SpawnDetails(int alien1Count, int alien2Count, boolean spawnMultiShotPowerup) {
        this(alien1Count, alien2Count, false, false, spawnMultiShotPowerup);
    }

}
