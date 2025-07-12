# Stage 1 Development Plan

## Overview
Focus on completing Stage 1 requirements with 5-minute gameplay before moving to Stage 2.

## Current Status
- ✅ Basic side-scrolling mechanics working
- ✅ Player movement, shooting, enemies spawning
- ❌ Team member names on title screen
- ❌ 5-minute gameplay content
- ❌ Proper stage transition system

## Stage 1 Immediate Tasks

### 1. Title Screen Enhancement
- **Add team member names** to TitleScene.java
- Display prominently on title screen
- Maintain current "Press SPACE to Start" functionality

### 2. Aspect Ratio & Dimensions
- **Current**: 716x700 (nearly square - too much vertical space)
- **Proposed**: Wider format for better side-scrolling (e.g., 1000x400)
- Adjust player movement range accordingly
- Make dodging more strategic with narrower height

### 3. 5-Minute Gameplay Content
- **Current spawn system**: Frame-based hardcoded spawns
- **Need**: Extended spawn patterns for 5+ minutes
- **Approach**: 
  - Expand `spawnMap` in Scene1.java with more enemies
  - Add waves/phases of increasing difficulty
  - Vary enemy types and spawn rates
  - Include more powerup spawns

### 4. Stage Transition Preparation
- **End condition**: After 5 minutes or specific score/enemy count
- **Transition**: "Stage 1 Complete" message
- **Navigation**: Move to Stage 2 (placeholder for now)

## Implementation Priority
1. Team member names (quick win)
2. Dimension adjustments (gameplay improvement)
3. Extended spawn patterns (5-minute content)
4. Stage completion logic

## Stage 2 Considerations (Future)
- Different enemy patterns
- Boss fight
- New background/theme
- Increased difficulty
- Keep architecture flexible for easy expansion

## Technical Notes
- Maintain current sprite system
- Use existing powerup framework
- Extend spawn system rather than rewrite
- Keep Global.java centralized for easy adjustments