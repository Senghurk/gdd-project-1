# Celestial Aegis - Vertical Shooter

A classic arcade-style space shooter built in Java, inspired by the timeless game Space Invaders. Navigate your ship, dodge enemy fire, and survive waves of extraterrestrial threats to save the galaxy!

## Team Information

**Team Name:** UbiRiotHoyoverse

**Team Members:**

- Lin Myat Phyo         — 6530201  
- Khaing Thin Zar Sein  — 6530381  
- Sai Oan Hseng Hurk    — 6440041

## Table of Contents
* [About The Game](#about-the-game)
* [Features](#features)
* [Gameplay Mechanics](#gameplay-mechanics)
    * [Game Modes](#game-modes)
    * [Player Ship](#player-ship)
    * [Enemies](#enemies)
    * [Level 1: The Invasion Begins](#level-1-the-invasion-begins)
    * [Level 2: The Final Stand](#level-2-the-final-stand)
* [How to Play](#how-to-play)
* [Getting Started](#getting-started)
    * [Prerequisites](#prerequisites)
    * [Running the Game](#running-the-game)
* [References](#references)

## About The Game

Celestial Aegis is a 2D shooter that challenges players with progressively difficult waves of enemies across two distinct levels. The game offers two different screen orientations for gameplay (Horizontal and Vertical) and features a dynamic power-up system to aid you in your mission. Survive the initial onslaught, power up your ship, and prepare to face the final boss to achieve victory.

## Features

* **Two Game Modes**: Choose between Horizontal or Vertical gameplay at the title screen.
* **Two Challenging Levels**: Progress from a survival-based mission to a climactic boss battle.
* **Diverse Enemies**: Battle against two types of hostiles: the nimble UFO and the aggressive Alien.
* **Progressive Difficulty**: Each level is phased to gradually increase the challenge.
* **Epic Boss Battle**: Face off against a powerful boss in the final stage.
* **Dynamic Power-Up System**: Collect power-ups to enhance your ship's speed, firepower, and survivability.
* **Classic Arcade Feel**: Simple controls and engaging mechanics that are easy to learn but hard to master.

## Gameplay Mechanics

### Game Modes

At the title screen, the player can select one of two modes:
* **Horizontal Mode**: The player ship moves left and right at the bottom of the screen, and enemies attack from the top.
* **Vertical Mode**: The player ship moves up and down on the left side of the screen, and enemies attack from the right.

### Player Ship

You start the game with the following stats:
* **Lives**: 3
* **Speed**: 3 units
* **Bullet Count**: 1 (can only have 1 bullet on screen at a time)

### Enemies

* **UFO**: A basic enemy unit. Its attack patterns change as you progress.
* **Alien**: A more advanced enemy that appears later in the game.

### Level 1: The Invasion Begins

**Objective**: Survive for 5 minutes.

This level is broken down into three phases:
* **Phase 1 (0:00 - 1:30)**: An easy introduction. Only UFOs are spawned, and they do not shoot.
* **Phase 2 (1:30 - 3:30)**: The threat increases. UFOs begin to shoot, but at a low fire rate.
* **Phase 3 (3:30 - 5:00)**: All-out attack! Aliens begin to spawn alongside UFOs. Both enemy types will shoot at the player.

**Power-ups in Level 1**:
* **Speed Up**: Increases ship speed. (Max speed: 11)
* **Bullet Count Add**: Increases the maximum number of bullets you can have on-screen at once. (Max bullets: 5)
* **Lives Add**: If your lives are below 3, this adds one life.

### Level 2: The Final Stand

**Objective**: Defeat the boss before the 5-minute timer runs out.

* **(0:00 - 3:00)**: The level begins with both UFOs and Aliens spawning and shooting. Survive the intense wave of attacks.
* **(3:00)**: The Boss appears!
* **(3:00 - 5:00)**: You have 2 minutes to destroy the boss. If the boss is defeated before the 5-minute mark for the level, you win the game! If you fail to destroy it in time, or if you run out of lives, it's game over.

**Power-ups in Level 2**:
* **Auto-Shoot**: For 10 seconds, your ship will automatically fire a continuous stream of bullets.
* **Lives Add**: Functions the same as in Level 1.

## How to Play

The controls are simple and intuitive:
* **Select Mode**: Up/Down to move the selection and Enter to select
* **Move Ship**: Arrow Keys
* **Shoot**: Spacebar

## Getting Started

Follow these instructions to get a copy of the project up and running on your local machine.

### Prerequisites

You need to have the Java Development Kit (JDK) installed on your system. Version 11 or higher is recommended.
* [Download Java JDK](https://www.oracle.com/java/technologies/downloads/)

### Running the Game

**Compiling from Source**

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/your-repo-name.git
   ```

2. Navigate to the source directory:
   ```bash
   cd your-repo-name/src
   ```

3. Compile the Java files. Assuming your main class is Game.java:
   ```bash
   javac Game.java
   ```

4. Run the game:
   ```bash
   java Game
   ```

## References

This project is adapted from the following repository:  
[Java Space Invaders by Jan Bodnar](https://github.com/janbodnar/Java-Space-Invaders)

This repository was forked from [Arjarn - CHAYAPOL MOEMENG](https://github.com/mchayapol/gdd-space-invaders-project) which contains the starter codebase for the project.

This is [Project 1 - Vertical Shooter] of the Game Design and Development (CSX4515) course. It is a Space Invaders-style arcade game using Java, with 2D game development principles and object-oriented programming concepts.
