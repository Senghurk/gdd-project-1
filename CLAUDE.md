# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java-based Space Invaders game built with Swing. The project is based on a Space Invaders repository and modified for game development project purposes. The game has been adapted for sideways/horizontal scrolling gameplay instead of traditional vertical movement.

## Development Commands

### Compilation and Running
```bash
# Compile all Java files
javac -d . src/gdd/*.java src/gdd/**/*.java

# Run the game
java gdd.Main

# Alternative: Use the manifest file to create and run a JAR
jar cfm game.jar MANIFEST.MF gdd/
java -jar game.jar
```

## Architecture

### Main Entry Point
- `src/gdd/Main.java` - Application entry point that creates and displays the Game window

### Core Game Structure
- `src/gdd/Game.java` - Main game window (JFrame) that manages scene transitions
- `src/gdd/Global.java` - Central configuration class containing all game constants, dimensions, image paths, and gameplay parameters

### Scene System
The game uses a scene-based architecture:
- `src/gdd/scene/TitleScene.java` - Title screen with animated "Press SPACE to Start" text and background music
- `src/gdd/scene/Scene1.java` - Stage 1 gameplay scene with sideways scrolling mechanics (5-minute survival)
- `src/gdd/scene/Scene2.java` - Stage 2 gameplay scene with boss battle mechanics (5-minute boss fight)
- `src/gdd/scene/SpawnManager.java` - Centralized enemy and powerup spawning logic for both scenes

### Sprite System
All game entities inherit from a common sprite base:
- `src/gdd/sprite/Sprite.java` - Base sprite class with position, image, collision detection
- `src/gdd/sprite/Player.java` - Player-controlled ship with multishot capability and bullet count management
- `src/gdd/sprite/Enemy.java` - Base enemy class
- `src/gdd/sprite/Alien1.java` - Basic alien enemy with bombing capability (Stage 1: 100pts, Stage 2: 150pts)
- `src/gdd/sprite/Alien2.java` - Advanced alien enemy, faster movement and bombs (Stage 1: 200pts, Stage 2: 300pts)
- `src/gdd/sprite/Boss.java` - **NEW**: Large boss enemy with health system, animated sprites, and multi-projectile attacks
- `src/gdd/sprite/Shot.java` - Player projectiles (speed: 8 pixels/frame)
- `src/gdd/sprite/EnemyBomb.java` - Regular enemy projectiles
- `src/gdd/sprite/BossBomb.java` - **NEW**: Boss projectiles with higher damage and speed
- `src/gdd/sprite/Explosion.java` - Visual explosion effects

### Power-up System (Enhanced)
- `src/gdd/powerup/PowerUp.java` - Base power-up class
- `src/gdd/powerup/SpeedUp.java` - Speed boost power-up implementation (permanent +2 speed, capped at 14)
- `src/gdd/powerup/MultiShotPowerUp.java` - Multi-shot power-up (4 shots, 10 seconds duration) with auto-fire capability
- `src/gdd/powerup/AddBulletPowerUp.java` - **NEW**: Bullet capacity power-up (permanent +1 bullet count)
- **Stage-specific spawning**: Stage 1 has sparse speed powerups, Stage 2 focuses on multishot powerups
- **Permanent upgrades**: Speed and bullet count boosts are permanent across stages

### Supporting Classes
- `src/gdd/AudioPlayer.java` - Background music playback with volume control
- `src/gdd/SoundEffectPlayer.java` - **NEW**: Non-blocking sound effect system with thread pool
- `src/gdd/SpawnDetails.java` - Data structure for enemy/powerup spawn configuration

## Key Features

### Sideways Gameplay
The game has been modified from traditional Space Invaders to use horizontal movement:
- Player moves vertically on the left side of screen
- Enemies spawn from the right side and move left
- Shots travel from left to right
- Enemy bombs travel from right to left

### Dynamic Background
- Scrolling star field with randomly generated stars of different sizes and colors
- Stars move from right to left to simulate forward movement

### Spawn System
Enemies and power-ups are spawned based on frame timing using `spawnMap` in Scene1. Features a 3-phase difficulty progression system:

**Phase 1 (0-90 seconds)**: Learning phase
- Basic enemy spawning every 1.3 seconds
- 33% Alien2, 67% Alien1 with sine wave movement patterns
- Speed powerups very rarely (every 30 seconds, 1/8 chance)
- Formation spawning: V-formations occasionally

**Phase 2 (90-210 seconds)**: Wave-based combat  
- Regular spawning every 1 second
- 50% Alien2, 50% Alien1 with complex movement patterns
- Powerups every 20 seconds: Multishot common, speed extremely rare (1/10 chance)
- Formation types: Line formations, diamond formations
- **Special Wave Events**: SWARM, ELITE, and MIXED waves

**Phase 3 (210-300 seconds)**: Intense finale
- Rapid spawning every 0.75 seconds  
- 75% Alien2, 25% Alien1
- Multishot powerups prioritized, speed ultra-rare (1/20 chance)
- Elite formations: Swarm attacks, pincer movements
- **Special Wave Events**: ELITE, SWARM, and FINAL boss waves

### Audio System (Enhanced)
- Background music for title screen, Stage 1, and Stage 2 (50% volume by default)
- Volume control with +/- keys on title screen
- **NEW**: Comprehensive sound effects system:
  - Shooting sounds (`Shoot.wav`)
  - Enemy destruction (`enemyExplode.wav`)
  - Player hit (`playerDown.wav`)
  - Boss intro (`bossIntro.wav`)
  - Victory (`Victory.wav`)
  - Game over (`gameOver.wav`)
  - Power-up collection (`catchPowerUp.wav`)
- Non-blocking audio playback using thread pool
- Uses WAV files in `src/audio/` directory

## Asset Structure
- `src/images/` - All sprite images (PNG format)
  - `alien.png` - Alien1 sprite
  - `alien2.png`, `alien2-1.png`, `alien2-2.png` - Alien2 sprites with animation frames
  - `boss1.png`, `boss2.png` - **NEW**: Boss sprites with animation frames
  - `boss_shot.png` - **NEW**: Boss projectile sprite
  - `powerup-s.png` - Speed powerup sprite
  - `powerup_autoshot.png` - Multi-shot powerup sprite
  - `powerup_bullet.png` - **NEW**: Bullet capacity powerup sprite
  - `transparent/` - **NEW**: Transparent versions of sprites for advanced effects
- `src/audio/` - Sound files (WAV format)
  - Music: `title.wav`, `scene1.wav`, `scene2.wav`
  - Sound effects: `Shoot.wav`, `enemyExplode.wav`, `playerDown.wav`, `bossIntro.wav`, `Victory.wav`, `gameOver.wav`, `catchPowerUp.wav`

## Game Controls
- Arrow keys: Move player up/down (default speed: 8)
- SPACE: Fire shots (dynamic limit: 6 normal, 15 with multishot)
- R: Restart game when game over
- SPACE on title screen: Start game
- +/- keys on title screen: Volume control

## Dashboard Features
- Score tracking (Alien1: 100pts, Alien2: 200pts)
- Lives system (starts with 3 lives) with 2-second invincibility frames
- Game timer (5-minute gameplay loop)
- Shot counter with dynamic limits
- **Enhanced status display**:
  - Active powerup status with countdown timers
  - Current phase indicator (Safe/Danger/WAR!)
  - Wave notifications during special events
  - Current speed indicator when above default

## Victory/Completion Conditions
- **Stage 1 Complete**: Survive for exactly 5 minutes (18,000 frames at 60 FPS)
- **Stage 2 Complete**: Defeat the boss within 5 minutes AND survive until time expires
- **Game Over**: Lives reach 0 (fully implemented collision system with invincibility frames)
- **Boss Defeat**: Boss has 100 health points and requires sustained fire to defeat
- **Time Limit**: Both stages have 5-minute time limits with different victory conditions
- **Restart**: Press 'R' key when game ends

## Performance Optimizations  
- Precise 16ms timer for stable 60fps gameplay
- Bullet speed increased to 12 pixels/frame for intense action
- Dynamic bullet limits (6 normal, 15 with multishot powerup)
- Efficient sprite collision detection using rectangle intersection
- **Star field background**: Optimized rendering with reduced glow effects
- **Rendering hints**: Speed-optimized graphics for smooth performance

## Team Information
- Mock team data displayed on title screen (3 members with IDs)
- Can be easily modified in `TitleScene.java`

## Stage 2 Features (Boss Battle)
- **Two-Stage Progression**: Stage 1 completion unlocks Stage 2
- **Boss Battle System**: Large animated boss with 100 health points
- **Boss Mechanics**:
  - Animated sprites that switch every 0.5 seconds
  - Vertical movement patterns near right edge
  - Multi-projectile attacks (3-way spread)
  - Shoots every 3+ seconds
- **Enhanced Scoring**: Increased point values in Stage 2 (Alien1: 150pts, Alien2: 300pts, Boss hits: 50pts)
- **Phase System**: 6 distinct phases including boss spawn, boss fight, and victory
- **Player Advantages**: Fixed stats (Speed: 8, Bullets: 5) with multishot powerups
- **Auto-fire**: Multishot powerup enables automatic firing with spread shots
- **Victory Conditions**: Defeat boss before 5-minute timer expires

## Important Notes
- The game uses a precise 16ms timer for 60fps animation
- **Enhanced enemy AI**: Sine wave patterns for Alien1, spiral movements for Alien2
- **Wave system**: Special challenge events with unique enemy formations
- **Multi-stage progression**: Stage 1 survival leads to Stage 2 boss battle
- **Persistent player stats**: Speed and bullet upgrades carry between stages
- **Default player speed**: Increased to 8 for better responsiveness
- **Collision detection**: Rectangle-based collision with 2-second invincibility frames
- **Thread-safe audio**: Non-blocking sound effects using executor service
- Game state managed through boolean flags and frame counting
- All constants centralized in `Global.java` for easy balancing
- **Powerup constants**: 
  - `MULTISHOT_DURATION_FRAMES = 600` (10 seconds)
  - `SPEED_BOOST_AMOUNT = 2` (permanent speed increase)
  - `MAX_PLAYER_SPEED = 14` (speed cap allows 3 speed boosts)
  - Boss health: 100 HP, requires sustained damage to defeat