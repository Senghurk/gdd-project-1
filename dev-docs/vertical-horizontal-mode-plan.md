# Emergency Plan: Adding Vertical/Horizontal Scrolling Modes

## 🚨 **CRITICAL ASSESSMENT**

**Difficulty Level: HIGH (7/10)**
**Time Estimate: 4-6 hours for vertical mode, +2 hours for switcher**
**Risk Level: MEDIUM** - Major gameplay mechanics changes

---

## 📋 **SCOPE OF CHANGES**

### **Current System (Horizontal)**
- Enemies spawn from **right edge**, move **left**
- Player moves **vertically** on left side
- Shots travel **left-to-right**
- Enemy bombs travel **right-to-left**

### **Target System (Vertical)**
- Enemies spawn from **top edge**, move **down**
- Player moves **horizontally** on bottom
- Shots travel **bottom-to-top**
- Enemy bombs travel **top-to-bottom**

---

## 🔧 **REQUIRED FILE CHANGES**

### **1. Core Game Files (MAJOR CHANGES)**

#### **`Global.java`** - Constants
```java
// NEW: Game mode constants
public static final int MODE_HORIZONTAL = 0;
public static final int MODE_VERTICAL = 1;
public static int CURRENT_GAME_MODE = MODE_HORIZONTAL; // Default

// NEW: Mode-specific spawn positions
public static int getEnemySpawnX() { 
    return CURRENT_GAME_MODE == MODE_VERTICAL ? 
        100 + new Random().nextInt(BOARD_WIDTH - 200) : BOARD_WIDTH; 
}
public static int getEnemySpawnY() { 
    return CURRENT_GAME_MODE == MODE_VERTICAL ? -50 : 
        100 + new Random().nextInt(BOARD_HEIGHT - 200); 
}

// NEW: Mode-specific player start positions
public static int getPlayerStartX() {
    return CURRENT_GAME_MODE == MODE_VERTICAL ? BOARD_WIDTH/2 : 50;
}
public static int getPlayerStartY() {
    return CURRENT_GAME_MODE == MODE_VERTICAL ? BOARD_HEIGHT - 100 : BOARD_HEIGHT/2;
}
```

#### **`Game.java`** - Mode Management
```java
// NEW: Store selected mode
private int gameMode = Global.MODE_HORIZONTAL;

// NEW: Pass mode to scenes
public void setGameMode(int mode) {
    this.gameMode = mode;
    Global.CURRENT_GAME_MODE = mode;
}

// NEW: Method to get current mode
public int getGameMode() {
    return gameMode;
}
```

### **2. Scene Files (MAJOR CHANGES)**

#### **`TitleScene.java`** - Mode Selector
```java
// NEW: Mode selection UI
private int selectedMode = Global.MODE_HORIZONTAL;
private String[] modeNames = {"Horizontal Scrolling", "Vertical Scrolling"};

// NEW: Key handlers for mode switching
private void handleModeSelection(KeyEvent e) {
    int key = e.getKeyCode();
    if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
        selectedMode = (selectedMode == Global.MODE_HORIZONTAL) ? 
            Global.MODE_VERTICAL : Global.MODE_HORIZONTAL;
    }
}

// NEW: Draw mode selection UI
private void drawModeSelector(Graphics g) {
    g.setColor(Color.YELLOW);
    g.setFont(new Font("Arial", Font.BOLD, 18));
    g.drawString("Game Mode:", 50, 400);
    
    for (int i = 0; i < modeNames.length; i++) {
        if (i == selectedMode) {
            g.setColor(Color.WHITE);
            g.drawString("> " + modeNames[i] + " <", 50, 430 + i * 30);
        } else {
            g.setColor(Color.GRAY);
            g.drawString("  " + modeNames[i], 50, 430 + i * 30);
        }
    }
}

// NEW: Pass selected mode to game
public void startGame() {
    game.setGameMode(selectedMode);
    game.loadScene1();
}
```

#### **`Scene1.java` & `Scene2.java`** - Gameplay Logic
```java
// NEW: Mode-aware player positioning
private void gameInit() {
    // ... existing code ...
    
    // Create player at mode-appropriate position
    if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
        player = new Player(Global.getPlayerStartX(), Global.getPlayerStartY());
    } else {
        player = new Player(Global.getPlayerStartX(), Global.getPlayerStartY());
    }
}

// NEW: Mode-aware star field movement
private void updateStarField() {
    for (Star star : stars) {
        if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
            star.y += star.speed; // Stars fall down
            if (star.y > BOARD_HEIGHT + 10) {
                star.y = -10;
                star.x = randomizer.nextInt(BOARD_WIDTH);
            }
        } else {
            star.x -= star.speed; // Stars move left (current)
            if (star.x < -10) {
                star.x = BOARD_WIDTH + 10;
                star.y = randomizer.nextInt(BOARD_HEIGHT);
            }
        }
    }
}

// NEW: Mode-aware enemy cleanup
private void cleanupOffscreenEnemies() {
    for (Enemy enemy : enemies) {
        if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
            if (enemy.getY() > BOARD_HEIGHT + 50) {
                enemy.die();
            }
        } else {
            if (enemy.getX() < -50) {
                enemy.die();
            }
        }
    }
}
```

### **3. Sprite Files (MODERATE TO MAJOR CHANGES)**

#### **`Player.java`** - Movement & Positioning
```java
// NEW: Mode-aware movement
public void keyPressed(KeyEvent e) {
    int key = e.getKeyCode();
    
    if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
        // Horizontal movement for vertical mode
        if (key == KeyEvent.VK_LEFT) {
            dx = -currentSpeed;
        }
        if (key == KeyEvent.VK_RIGHT) {
            dx = +currentSpeed;
        }
    } else {
        // Vertical movement for horizontal mode (current)
        if (key == KeyEvent.VK_UP) {
            dy = -currentSpeed;
        }
        if (key == KeyEvent.VK_DOWN) {
            dy = +currentSpeed;
        }
    }
}

public void keyReleased(KeyEvent e) {
    int key = e.getKeyCode();
    
    if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) {
            dx = 0;
        }
    } else {
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
            dy = 0;
        }
    }
}

// NEW: Mode-aware boundary checking
public void act() {
    x += dx;
    y += dy;
    
    if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
        // Horizontal boundaries
        if (x <= 0) x = 0;
        if (x >= BOARD_WIDTH - PLAYER_WIDTH) x = BOARD_WIDTH - PLAYER_WIDTH;
    } else {
        // Vertical boundaries (current)
        if (y <= 60) y = 60; // Below dashboard
        if (y >= BOARD_HEIGHT - PLAYER_HEIGHT) y = BOARD_HEIGHT - PLAYER_HEIGHT;
    }
    
    // Update multishot and invincibility timers
    updatePowerupTimers();
}

// NEW: Mode-aware shot spawn position
public int getShotStartX() {
    return Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL ? 
        x + PLAYER_WIDTH/2 : x + PLAYER_WIDTH;
}

public int getShotStartY() {
    return Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL ? 
        y : y + PLAYER_HEIGHT/2;
}
```

#### **`Shot.java`** - Projectile Direction
```java
public void act() {
    if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
        this.y -= 12; // Move up
    } else {
        this.x += 12; // Move right (current)
    }
}

// NEW: Mode-aware boundary checking
public boolean isOffScreen() {
    if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
        return y < -10; // Off top edge
    } else {
        return x > BOARD_WIDTH + 10; // Off right edge
    }
}
```

#### **`Alien1.java` & `Alien2.java`** - Movement Patterns
```java
// In Alien1.java
private int initialX; // NEW: For vertical mode

public Alien1(int x, int y) {
    super(x, y);
    setCollisionBounds(30, 30);
    this.initialY = y;
    this.initialX = x; // NEW
    initEnemy(x, y);
}

public void act(int direction) {
    frameCounter++;
    
    if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
        // Move downward with horizontal sine wave
        this.y += Math.abs(direction);
        double sineOffset = Math.sin(frameCounter * 0.08) * 20;
        this.x = (int)(initialX + sineOffset);
        
        // Keep within horizontal bounds
        if (this.x < 50) this.x = 50;
        if (this.x > BOARD_WIDTH - 100) this.x = BOARD_WIDTH - 100;
    } else {
        // Current horizontal movement
        this.x -= Math.abs(direction);
        double sineOffset = Math.sin(frameCounter * 0.08) * 20;
        this.y = (int)(initialY + sineOffset);
        
        // Keep within vertical bounds
        if (this.y < 50) this.y = 50;
        if (this.y > BOARD_HEIGHT - 100) this.y = BOARD_HEIGHT - 100;
    }
}

// Similar changes needed in Alien2.java with more complex movement patterns
```

#### **`EnemyBomb.java` & `BossBomb.java`** - Bomb Direction
```java
// In EnemyBomb.java
public void act() {
    if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
        this.y += speed; // Move down toward player
        if (this.y > BOARD_HEIGHT + 50) {
            setDestroyed(true);
        }
    } else {
        this.x -= speed; // Move left toward player (current)
        if (this.x < -50) {
            setDestroyed(true);
        }
    }
}
```

#### **`Boss.java`** - Boss Positioning & Movement
```java
// NEW: Mode-aware boss positioning and movement
public Boss(int x, int y) {
    setCollisionBounds(100, 100);
    this.maxHealth = BOSS_HEALTH;
    this.health = maxHealth;
    
    if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
        // Position at top-center for vertical mode
        initBoss(BOARD_WIDTH/2 - 50, 50);
    } else {
        // Current right-side positioning
        initBoss(x, y);
    }
}

public void act() {
    // ... existing animation code ...
    
    if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
        // Horizontal movement at top of screen
        moveCounter++;
        if (moveCounter >= MOVE_INTERVAL) {
            moveCounter = 0;
            moveDirection *= -1;
        }
        
        this.x += moveDirection * MOVE_SPEED;
        
        // Keep boss within horizontal bounds
        if (this.x < 50) {
            this.x = 50;
            moveDirection = 1;
        } else if (this.x > BOARD_WIDTH - 150) {
            this.x = BOARD_WIDTH - 150;
            moveDirection = -1;
        }
    } else {
        // Current vertical movement code
        moveCounter++;
        if (moveCounter >= MOVE_INTERVAL) {
            moveCounter = 0;
            moveDirection *= -1;
        }
        
        this.y += moveDirection * MOVE_SPEED;
        
        if (this.y < 50) {
            this.y = 50;
            moveDirection = 1;
        } else if (this.y > BOARD_HEIGHT - 150) {
            this.y = BOARD_HEIGHT - 150;
            moveDirection = -1;
        }
    }
    
    shotCounter++;
}

// NEW: Mode-aware boss shooting
public List<BossBomb> shoot() {
    List<BossBomb> bombs = new ArrayList<>();
    
    if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
        // Downward spread for vertical mode
        bombs.add(new BossBomb(this.x + 40, this.y + 100)); // center
        bombs.add(new BossBomb(this.x + 10, this.y + 100)); // left
        bombs.add(new BossBomb(this.x + 70, this.y + 100)); // right
    } else {
        // Current leftward spread
        bombs.add(new BossBomb(this.x, this.y + 40)); // center
        bombs.add(new BossBomb(this.x, this.y + 10)); // up
        bombs.add(new BossBomb(this.x, this.y + 70)); // down
    }
    
    return bombs;
}
```

### **4. Spawn System (MODERATE CHANGES)**

#### **`SpawnManager.java`** - Spawn Positions
```java
// NEW: Mode-aware enemy creation
private Enemy createAlien1() {
    if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
        int x = 100 + randomizer.nextInt(BOARD_WIDTH - 200);
        return new Alien1(x, -50); // Top edge
    } else {
        int y = 100 + randomizer.nextInt(BOARD_HEIGHT - 200);
        return new Alien1(BOARD_WIDTH, y); // Right edge (current)
    }
}

private Enemy createAlien2() {
    if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
        int x = 100 + randomizer.nextInt(BOARD_WIDTH - 200);
        return new Alien2(x, -50); // Top edge
    } else {
        int y = 100 + randomizer.nextInt(BOARD_HEIGHT - 200);
        return new Alien2(BOARD_WIDTH, y); // Right edge (current)
    }
}

// NEW: Mode-aware powerup creation
private PowerUp createPowerUp(String type) {
    int x, y;
    
    if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
        x = 100 + randomizer.nextInt(BOARD_WIDTH - 200);
        y = -50;
    } else {
        x = BOARD_WIDTH;
        y = 100 + randomizer.nextInt(BOARD_HEIGHT - 200);
    }
    
    switch (type) {
        case "PowerUp-SpeedUp":
            return new SpeedUp(x, y);
        case "PowerUp-AddBullet":
            return new AddBulletPowerUp(x, y);
        case "PowerUp-MultiShot":
            return new MultiShotPowerUp(x, y);
        default:
            return null;
    }
}

// UPDATE: All spawn generation methods to use new creation methods
private void generatePhase1Spawns() {
    for (int frame = 1500; frame <= 5400; frame += 150) {
        SpawnDetails spawn = new SpawnDetails("Alien1", 0, 0); // Position ignored
        spawnMap.put(frame, spawn);
    }
}

// UPDATE: handleSpawnDetails method
private void handleSpawnDetails(SpawnDetails sd, SpawnResult result, Player player, int frame) {
    switch (sd.type) {
        case "Alien1":
            result.enemies.add(createAlien1());
            break;
        case "Alien2":
            result.enemies.add(createAlien2());
            break;
        case "PowerUp-SpeedUp":
            if (player.getCurrentSpeed() < player.getMaxSpeed()) {
                result.powerups.add(createPowerUp("PowerUp-SpeedUp"));
            }
            break;
        // ... other cases
    }
}
```

### **5. Background System (MINOR CHANGES)**

#### **Star Field in Scene Classes**
```java
// UPDATE: initStarField method
private void initStarField() {
    for (int i = 0; i < 100; i++) {
        int x, y, speed;
        
        if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
            x = randomizer.nextInt(BOARD_WIDTH);
            y = randomizer.nextInt(BOARD_HEIGHT + 200); // Some start above screen
            speed = 1 + randomizer.nextInt(3); // Variable speed 1-3
        } else {
            x = randomizer.nextInt(BOARD_WIDTH + 200); // Some start off-screen
            y = randomizer.nextInt(BOARD_HEIGHT);
            speed = 1; // Current constant speed
        }
        
        int size = randomizer.nextInt(3) + 1;
        Color color = getRandomStarColor();
        stars.add(new Star(x, y, size, speed, color));
    }
}
```

---

## 🆕 **NEW SYSTEMS REQUIRED**

### **1. Mode Management System**
- Global mode state tracking
- Mode switching validation
- Scene transition with mode preservation

### **2. UI Mode Selector**
- Title screen mode selection
- Visual indicators for current mode
- Keyboard navigation between modes

### **3. Coordinate Translation System**
- Movement direction mapping
- Boundary checking logic
- Collision detection adjustments

### **4. Asset Positioning System**
- Dynamic spawn point calculation
- Mode-aware initial positions
- Boundary management

---

## 🔢 **NEW VARIABLES NEEDED**

### **Global Variables**
```java
// In Global.java
public static int CURRENT_GAME_MODE;
public static final int MODE_HORIZONTAL = 0;
public static final int MODE_VERTICAL = 1;
```

### **Per-Sprite Variables**
```java
// In enemy sprites
private int initialX; // For vertical mode
private int initialY; // For horizontal mode (existing)

// In player
private int minX, maxX; // Vertical mode boundaries
private int minY, maxY; // Horizontal mode boundaries (existing)
```

### **Scene Variables**
```java
private boolean isVerticalMode;
private int playerStartX, playerStartY;
```

---

## ⚡ **IMPLEMENTATION PRIORITY**

### **Phase 1: Core Vertical Mode (PRIORITY)**
1. ✅ Add mode constants to `Global.java` - **COMPLETED**
2. ✅ Modify `Player.java` movement system - **COMPLETED**
3. ✅ Update `Shot.java` direction - **COMPLETED**
4. ✅ Fix enemy movement in `Alien1.java`/`Alien2.java` - **COMPLETED**
5. ✅ Update spawn positions in `SpawnManager.java` - **COMPLETED**

### **Phase 2: UI Switcher**
1. ✅ Add mode selector to `TitleScene.java` - **COMPLETED**
2. ✅ Implement mode switching logic - **COMPLETED**
3. ✅ Add visual indicators - **COMPLETED**

### **Phase 3: Scene Logic**
1. ✅ Update Scene1/Scene2 star field movement - **COMPLETED**
2. ✅ Update Scene1/Scene2 enemy cleanup logic - **COMPLETED**
3. ✅ Add mode-aware boundary management - **COMPLETED**

### **Phase 4: Testing & Validation**
1. ⚠️ Test collision detection in both modes
2. ⚠️ Verify boundary conditions work properly
3. ⚠️ Test mode switching functionality
4. ⚠️ Validate gameplay balance in vertical mode

---

## ⚠️ **CRITICAL RISKS**

1. **Collision Detection**: May need recalibration for vertical movement
2. **Performance**: Star field updates may need optimization
3. **Balance**: Vertical mode may be significantly easier/harder
4. **Boss Mechanics**: Boss AI needs complete rework for vertical mode
5. **Formation Spawning**: Wave formations need redesign

---

## 🎯 **RECOMMENDATION**

**START WITH:** Vertical mode implementation focusing on basic gameplay
**DEFER:** Complex formations and boss mechanics initially
**TEST EARLY:** Player movement and basic enemy spawning first

**Estimated breakdown:**
- ✅ **2 hours**: Basic vertical player/enemy movement
- ✅ **1 hour**: Shot mechanics and collision
- ✅ **1 hour**: Spawn system updates
- ⚠️ **2 hours**: Boss system rework (complex)
- ✅ **1 hour**: UI mode switcher

**Total: 7 hours** for full implementation with switcher.

---

## 📝 **TESTING CHECKLIST**

### **Vertical Mode Testing**
- [ ] Player moves horizontally at bottom
- [ ] Enemies spawn from top and move down
- [ ] Shots travel upward
- [ ] Enemy bombs fall downward
- [ ] Collision detection works properly
- [ ] Star field moves downward
- [ ] Boss appears at top and moves horizontally
- [ ] Powerups spawn and move correctly

### **Mode Switcher Testing**
- [ ] Title screen shows mode options
- [ ] Up/Down arrows switch between modes
- [ ] Selected mode is highlighted
- [ ] Mode selection persists to gameplay
- [ ] Can switch modes after game over

### **Cross-Mode Testing**
- [ ] Both modes have same difficulty progression
- [ ] Both modes have same powerup distribution
- [ ] Both modes have same scoring system
- [ ] Performance is similar in both modes