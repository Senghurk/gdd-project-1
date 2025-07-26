# Spawn System Architecture

## Core Components

**SpawnManager**: Central spawn controller using frame-based HashMap lookup
- `HashMap<Integer, SpawnDetails> spawnMap` - Frame → spawn configuration
- `spawnEnemies(frame, player)` - Main spawn method called every frame
- Supports two spawn modes: string-based (Scene1) and count-based (Scene2)

**SpawnDetails**: Dual-purpose spawn configuration
```java
// Scene1: String-based spawns
SpawnDetails(String type, int x, int y)

// Scene2: Count-based spawns  
SpawnDetails(int alien1Count, int alien2Count, boolean... powerups)
```

## Spawn Flow

1. **Frame Check**: `spawnMap.get(currentFrame)` 
2. **Mode Detection**: String type → Scene1 logic, Count → Scene2 logic
3. **Entity Creation**: Mode-aware positioning via `Global.getEnemySpawnX/Y()`
4. **Return**: `SpawnResult` with enemies/powerups lists

## Scene1 Spawn Types

**Enemies**: `"Alien1"`, `"Alien2"`
**Powerups**: `"PowerUp-SpeedUp"`, `"PowerUp-AddBullet"`, `"PowerUp-MultiShot"`, `"PowerUp-Health"`
**Waves**: `"WAVE_SWARM"` (6 Alien1), `"WAVE_ELITE"` (3 Alien2), `"WAVE_MIXED"` (5 mixed), `"WAVE_FINAL"` (4 Alien2)
**Victory**: `"VICTORY"` - triggers game completion

## Scene2 Spawn Logic

Count-based spawning with boolean flags:
- `alien1Count` - Number of Alien1 enemies
- `alien2Count` - Number of Alien2 enemies  
- Powerup flags for each type

## Phase System (Scene1)

**Phase 1** (0-90s): Learning - Alien1 only, gradual increase
**Phase 2** (90-210s): Threat introduction - formations, Alien1 only
**Phase 3** (210-300s): Escalation - mixed enemies, waves, complex patterns

## Mode-Aware Positioning

```java
// Vertical mode: spawn from top, random X
getEnemySpawnX() → random(100, BOARD_WIDTH-200)
getEnemySpawnY() → -50

// Horizontal mode: spawn from right, random Y  
getEnemySpawnX() → BOARD_WIDTH
getEnemySpawnY() → random(100, BOARD_HEIGHT-200)
```

## Key Methods

- `createAlien1/2()` - Mode-aware enemy creation
- `createPowerUp(type)` - Mode-aware powerup creation
- `handleSpawnDetails()` - Scene1 string-based processing
- `loadSpawnDetails()` - Scene1 phase generation
- `loadScene2SpawnDetails()` - Scene2 wave generation

## Smart Powerup Logic

Scene1 powerups check player state before spawning:
- SpeedUp: Only if `player.getCurrentSpeed() < maxSpeed`
- AddBullet: Only if `player.getCurrentBulletCount() < maxCount` 