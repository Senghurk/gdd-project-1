package gdd;

import gdd.scene.Scene1;
import gdd.scene.Scene2;
import gdd.scene.TitleScene;
import gdd.sprite.Player;
import javax.swing.JFrame;

public class Game extends JFrame  {

    TitleScene titleScene;
    Scene1 scene1;
    Scene2 scene2;
    private boolean scene1Started = false;
    private boolean titleStarted = false;
    private Player playerFromScene1 = null;
    
    // NEW: Mode management
    private int gameMode = Global.MODE_VERTICAL; // Default to horizontal
    
    // TESTING: Set mode for direct scene testing (when skipping title screen)
    // Change this to Global.MODE_VERTICAL to test Scene2 in vertical mode
    private static final int TESTING_MODE = Global.MODE_HORIZONTAL;

    public Game() {
        titleScene = new TitleScene(this);
        scene1 = new Scene1(this);
        scene2 = new Scene2(this);
        initUI();
        
        /*  Comment - Uncomment these lines to switch between normal start and Scene2 testing 
         *  
         *  FOR SCENE2 TESTING:
         *  1. Change TESTING_MODE above to Global.MODE_VERTICAL for vertical mode testing
         *  2. Uncomment loadScene2() line below
         *  3. Comment out loadTitle() line below
         */

        //loadTitle(); // Start with title screen
        loadScene2(); // Uncomment to test Scene2 directly
    }

    private void initUI() {
        setTitle("Space Invaders");
        setSize(Global.BOARD_WIDTH, Global.BOARD_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    public void loadTitle() {
        getContentPane().removeAll();
        add(titleScene);
        titleScene.start();
        titleStarted = true;
        revalidate();
        repaint();
    }

    public void loadScene1() {
        getContentPane().removeAll();
        add(scene1);
        if (titleStarted) {
            titleScene.stop();
        }
        scene1.start();
        scene1Started = true;
        revalidate();
        repaint();
    }

    public void loadScene2() {
        getContentPane().removeAll();
        add(scene2);
        if (scene1Started) {
            // Store player data before stopping Scene1
            playerFromScene1 = scene1.getPlayer();
            scene1.stop();
            // Mode is already set from Scene1, don't override it
        } else {
            // TESTING: Only set mode when jumping directly to Scene2 (bypassing Scene1)
            if (titleStarted) {
                titleScene.stop();
            }
            setGameMode(TESTING_MODE);
        }
        
        scene2.start();
        revalidate();
        repaint();
    }

    public Player getPlayerFromScene1() {
        return playerFromScene1;
    }
    
    // NEW: Method to set player data from Scene1
    public void setPlayerFromScene1(Player player) {
        this.playerFromScene1 = player;
    }
    
    // NEW: Mode management methods
    public void setGameMode(int mode) {
        this.gameMode = mode;
        Global.CURRENT_GAME_MODE = mode;
    }
    
    public int getGameMode() {
        return gameMode;
    }
}