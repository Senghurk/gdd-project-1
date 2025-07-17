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

    public Game() {
        titleScene = new TitleScene(this);
        scene1 = new Scene1(this);
        scene2 = new Scene2(this);
        initUI();
        
        // Comment/uncomment these lines to switch between normal start and Scene2 testing
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
            // Pass player from Scene1 to Scene2
            playerFromScene1 = scene1.getPlayer();
            scene1.stop();
        }
        if (titleStarted) {
            titleScene.stop();
        }
        scene2.start();
        revalidate();
        repaint();
    }

    public Player getPlayerFromScene1() {
        return playerFromScene1;
    }
}