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
- `src/gdd/scene/Scene1.java` - Main gameplay scene with sideways scrolling mechanics

### Sprite System
All game entities inherit from a common sprite base:
- `src/gdd/sprite/Sprite.java` - Base sprite class with position, image, collision detection
- `src/gdd/sprite/Player.java` - Player-controlled ship with multishot capability
- `src/gdd/sprite/Enemy.java` - Base enemy class
- `src/gdd/sprite/Alien1.java` - Basic alien enemy with bombing capability (100 points)
- `src/gdd/sprite/Alien2.java` - Advanced alien enemy, faster movement and bombs (200 points)
- `src/gdd/sprite/Shot.java` - Player projectiles (speed: 8 pixels/frame)
- `src/gdd/sprite/Explosion.java` - Visual explosion effects

### Power-up System
- `src/gdd/powerup/PowerUp.java` - Base power-up class
- `src/gdd/powerup/SpeedUp.java` - Speed boost power-up implementation
- `src/gdd/powerup/MultiShotPowerUp.java` - Multi-shot power-up (4 shots, 15 seconds duration)

### Supporting Classes
- `src/gdd/AudioPlayer.java` - Sound effect and music playback
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

**Phase 1 (Minutes 1-2)**: Moderate difficulty
- Spawn every 2 seconds
- 33% Alien2, 67% Alien1
- Powerups every 10 seconds

**Phase 2 (Minutes 2-4)**: Increased difficulty  
- Spawn every 1.5 seconds
- 50% Alien2, 50% Alien1
- Powerups every 8 seconds
- 25% chance of double spawns

**Phase 3 (Minutes 4-5)**: High difficulty
- Spawn every 1 second
- 66% Alien2, 33% Alien1
- Powerups every 6 seconds
- 33% chance of double spawns + 50% chance of triple spawns

### Audio System
- Background music for title screen and gameplay (50% volume by default)
- Volume control with +/- keys on title screen
- Uses WAV files in `src/audio/` directory

## Asset Structure
- `src/images/` - All sprite images (PNG format)
  - `alien.png` - Alien1 sprite
  - `alien2.png` - Alien2 sprite (orange, more aggressive)
  - `powerup-s.png` - Speed powerup sprite
  - `powerup-multishot.png` - Multi-shot powerup sprite
- `src/audio/` - Sound files (WAV and MP3 formats)

## Game Controls
- Arrow keys: Move player up/down
- SPACE: Fire shots (dynamic limit: 4 normal, 8 with multishot)
- R: Restart game when game over
- SPACE on title screen: Start game
- +/- keys on title screen: Volume control

## Dashboard Features
- Score tracking (Alien1: 100pts, Alien2: 200pts)
- Lives system (starts with 3 lives)
- Game timer (5-minute gameplay loop)
- Shot counter with dynamic limits
- Active powerup status display

## Victory/Completion Conditions
- **Stage 1 Complete**: Survive for exactly 5 minutes (18,000 frames at 60 FPS)
- **Game Over**: Lives reach 0 (collision system not yet implemented)
- **Restart**: Press 'R' key when game ends

## Performance Optimizations
- Bullet speed increased to 8 pixels/frame for faster pacing
- Dynamic bullet limits (4 normal, 8 with multishot powerup)
- Efficient sprite collision detection using rectangle intersection
- Star field background with optimized rendering

## Team Information
- Mock team data displayed on title screen (3 members with IDs)
- Can be easily modified in `TitleScene.java`

## Important Notes
- The game uses a 60 FPS timer for smooth animation
- Collision detection uses rectangle-based collision for all sprites
- Game state is managed through boolean flags (`inGame`, visibility states)
- All constants are centralized in `Global.java` for easy modification
- The project structure follows standard Java package conventions
- Powerup constants: `MULTISHOT_DURATION_FRAMES = 900`, `MULTISHOT_EXTRA_SHOTS = 3`