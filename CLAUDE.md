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
- `src/gdd/sprite/Player.java` - Player-controlled ship
- `src/gdd/sprite/Enemy.java` - Base enemy class
- `src/gdd/sprite/Alien1.java` - Specific alien enemy with bombing capability
- `src/gdd/sprite/Shot.java` - Player projectiles
- `src/gdd/sprite/Explosion.java` - Visual explosion effects

### Power-up System
- `src/gdd/powerup/PowerUp.java` - Base power-up class
- `src/gdd/powerup/SpeedUp.java` - Speed boost power-up implementation

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
Enemies and power-ups are spawned based on frame timing using `spawnMap` in Scene1. Currently hardcoded but designed to be loaded from external files.

### Audio System
- Background music for title screen and gameplay
- Uses WAV files in `src/audio/` directory

## Asset Structure
- `src/images/` - All sprite images (PNG format)
- `src/audio/` - Sound files (WAV and MP3 formats)

## Game Controls
- Arrow keys: Move player up/down
- SPACE: Fire shots (max 4 simultaneous shots)
- R: Restart game when game over
- SPACE on title screen: Start game

## Important Notes
- The game uses a 60 FPS timer for smooth animation
- Collision detection uses rectangle-based collision for all sprites
- Game state is managed through boolean flags (`inGame`, visibility states)
- All constants are centralized in `Global.java` for easy modification
- The project structure follows standard Java package conventions