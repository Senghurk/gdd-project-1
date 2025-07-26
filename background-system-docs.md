# Background System Architecture

## Core Components

**Star Class**: Simple data structure for background elements
```java
private static class Star {
    int x, y, size, speed;
    Color color;
}
```

**Star Field**: Dynamic scrolling background with 100+ stars per scene
- Scene1: White/blue/yellow stars (70%/20%/10% distribution)
- Scene2: Reddish theme (light red, yellow-white, medium red)

## Mode-Aware Movement

**Vertical Mode**: Stars fall downward
- Spawn: Random X across width, Y = -10 (top)
- Movement: `star.y += star.speed`
- Respawn: When Y > BOARD_HEIGHT + 10, reset to Y = -10

**Horizontal Mode**: Stars move leftward  
- Spawn: X = BOARD_WIDTH + 10 (right), random Y across height
- Movement: `star.x -= star.speed`
- Respawn: When X < -10, reset to X = BOARD_WIDTH + random offset

## Rendering Pipeline

**Draw Order** (back to front):
1. `drawStarField(g)` - Background stars first
2. Game entities (explosions, powerups, enemies, player, shots)
3. UI elements (dashboard, level text) - Top layer

**Optimization**: Single-pixel stars use `drawLine()` instead of `fillOval()`

## Star Generation

**Initial Setup**: `initStarField()` creates 100 stars with mode-aware positioning
**Dynamic Addition**: 5% chance per frame to add new star (prevents memory bloat)
**Memory Management**: Remove excess stars when count > 150

## Speed Characteristics

**Scene1**: 
- Vertical: Variable speed 1-3
- Horizontal: Constant speed 1

**Scene2**: 
- Both modes: Variable speed 2-4 (faster than Scene1)

## Color Systems

**Scene1 Distribution**:
- 70% White
- 20% Light blue (200,200,255)
- 10% Light yellow (255,255,200)

**Scene2 Reddish Theme**:
- Light red (255,200,200)
- Yellow-white (255,255,200)  
- Medium red (255,150,150)

## Key Methods

- `initStarField()` - Initial star field setup with mode detection
- `updateStarField()` - Movement and respawn logic per frame
- `drawStarField(Graphics g)` - Optimized rendering with size-based drawing
- `getRandomStarColor()` - Scene1 color distribution logic

## Background Layers

**Base**: Black fill (`Color.black` fillRect)
**Stars**: Dynamic scrolling field
**UI**: Dashboard with semi-transparent backgrounds
**Game Over**: Dark themed overlays (Scene2 uses dark red tint) 