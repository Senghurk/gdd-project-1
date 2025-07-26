package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import gdd.Global;
import static gdd.Global.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class TitleScene extends JPanel {

    private int frame = 0;
    private Image image;
    private AudioPlayer audioPlayer;
    private final Dimension d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
    private Timer timer;
    private Game game;
    private float currentVolume = 0.25f; // Track current volume
    
    // NEW: Mode selection variables
    private int selectedMode = Global.MODE_HORIZONTAL; // Default to horizontal
    private String[] modeNames = {"Horizontal Scrolling", "Vertical Scrolling"};

    public TitleScene(Game game) {
        this.game = game;
        // initBoard();
        // initTitle();
    }



    public void start() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        setBackground(Color.black);

        timer = new Timer(1000 / 60, new GameCycle());
        timer.start();

        initTitle();
        initAudio();
    }

    public void stop() {
        try {
            if (timer != null) {
                timer.stop();
            }

            if (audioPlayer != null) {
                audioPlayer.stop();
            }
        } catch (Exception e) {
            System.err.println("Error closing audio player.");
        }
    }

    private void initTitle() {
        var ii = new ImageIcon(IMG_TITLE);
        image = ii.getImage();

    }

    private void initAudio() {
        try {
            String filePath = "src/audio/new_ost/title_scene.wav";
            System.out.println("TitleScene: Loading audio from: " + filePath);
            audioPlayer = new AudioPlayer(filePath);
            System.out.println("TitleScene: AudioPlayer created successfully");
            audioPlayer.play();
            System.out.println("TitleScene: Audio started playing");
        } catch (Exception e) {
            System.err.println("TitleScene: Error with playing sound: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        doDrawing(g);
    }

    private void doDrawing(Graphics g) {

        g.setColor(Color.black);
        g.fillRect(0, 0, d.width, d.height);

        int imageWidth = (int)(BOARD_WIDTH * 0.8); // Make image 80% of board width
        int imageHeight = (int)(BOARD_HEIGHT * 0.6); // Make image 60% of board height
        int imageX = (BOARD_WIDTH - imageWidth) / 2; // Center horizontally
        int imageY = 50; // Position from top
        g.drawImage(image, imageX, imageY, imageWidth, imageHeight, null);

        // Draw mode selector
        drawModeSelector(g);

        if (frame % 60 < 30) {
            g.setColor(Color.red);
        } else {
            g.setColor(Color.white);
        }

        /* 
        g.setFont(g.getFont().deriveFont(32f));
        String text = "Press SPACE to Start";
        int stringWidth = g.getFontMetrics().stringWidth(text);
        int x = (d.width - stringWidth) / 2;
        // int stringHeight = g.getFontMetrics().getAscent();
        // int y = (d.height + stringHeight) / 2;
        g.drawString(text, x, 600);
        */

        g.setColor(Color.white);
        g.setFont(g.getFont().deriveFont(16f));
        g.drawString("Team Members:", (d.width - g.getFontMetrics().stringWidth("Team Members:")) / 2, 450);
        
        g.setFont(g.getFont().deriveFont(14f));
        String[] teamMembers = {
            "KHAING THIN ZAR SEIN - ID: 6530381",
            "SAI OAN HSENG HURK - ID: 6440041", 
            "LIN MYAT PHYO - ID: 6530201"
        };
        
        for (int i = 0; i < teamMembers.length; i++) {
            int memberX = (d.width - g.getFontMetrics().stringWidth(teamMembers[i])) / 2;
            g.drawString(teamMembers[i], memberX, 480 + (i * 20));
        }
        
        // Mode selector controls
        g.setColor(Color.cyan);
        g.setFont(g.getFont().deriveFont(12f));
        g.drawString("Mode Controls: UP/DOWN arrows to move and ENTER to select", 10, d.height - 50);
        
        g.setColor(Color.gray);
        g.setFont(g.getFont().deriveFont(10f));
        g.drawString("Game by UbiRiotHoyoverse", 10, 20);

        Toolkit.getDefaultToolkit().sync();
    }
    
    /**
     * Draws the mode selector UI with highlighting for selected mode
     */
    private void drawModeSelector(Graphics g) {
        g.setColor(Color.white);
        g.setFont(g.getFont().deriveFont(18f));
        String modeTitle = "Game Mode:";
        int titleX = (d.width - g.getFontMetrics().stringWidth(modeTitle)) / 2;
        g.drawString(modeTitle, titleX - 5, 550);
        
        // Draw mode options with selection highlighting
        g.setFont(g.getFont().deriveFont(16f));
        for (int i = 0; i < modeNames.length; i++) {
            // Highlight selected mode
            if (i == selectedMode) {
                g.setColor(Color.yellow);
                g.drawString("> " + modeNames[i] + " <", 
                    (d.width - g.getFontMetrics().stringWidth("> " + modeNames[i] + " <")) / 2, 
                    570 + (i * 25));
            } else {
                g.setColor(Color.lightGray);
                g.drawString(modeNames[i], 
                    (d.width - g.getFontMetrics().stringWidth(modeNames[i])) / 2, 
                    570 + (i * 25));
            }
        }
    }

    private void update() {
        frame++;
    }

    private void doGameCycle() {
        update();
        repaint();
    }

    private class GameCycle implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            doGameCycle();
        }
    }

    private class TAdapter extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            System.out.println("Title.keyPressed: " + e.getKeyCode());
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_ENTER) {
                // Set the selected mode in the game and load Scene1
                game.setGameMode(selectedMode);
                game.loadScene1();
            } else if (key == KeyEvent.VK_UP) {
                // Navigate up in mode selector
                selectedMode = (selectedMode - 1 + modeNames.length) % modeNames.length;
                repaint(); // Update display immediately
            } else if (key == KeyEvent.VK_DOWN) {
                // Navigate down in mode selector
                selectedMode = (selectedMode + 1) % modeNames.length;
                repaint(); // Update display immediately
            } /*else if (key == KeyEvent.VK_PLUS || key == KeyEvent.VK_EQUALS) {
                // Increase volume
                currentVolume = Math.min(1.0f, currentVolume + 0.1f);
                if (audioPlayer != null) {
                    audioPlayer.setVolume(currentVolume);
                }
            } else if (key == KeyEvent.VK_MINUS) {
                // Decrease volume
                currentVolume = Math.max(0.0f, currentVolume - 0.1f);
                if (audioPlayer != null) {
                    audioPlayer.setVolume(currentVolume);
                }
            }*/

        }
    }
}
