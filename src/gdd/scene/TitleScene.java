package gdd.scene;

import gdd.AudioPlayer;
import gdd.FontManager;
import gdd.Game;
import gdd.Global;
import static gdd.Global.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
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
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g.setColor(Color.black);
        g.fillRect(0, 0, d.width, d.height);

        // Team name in top left corner - stylized
        // Shadow effect
        g2d.setColor(new Color(0, 0, 0, 150)); // Semi-transparent black shadow
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Team : UbiRiotHoyoverse", 17, 27);
        
        // Main text with gradient-like effect
        g2d.setColor(new Color(255, 215, 0)); // Gold color
        g2d.drawString("Team : UbiRiotHoyoverse", 15, 25);
        
        // Subtle highlight
        g2d.setColor(new Color(255, 255, 255, 100)); // Semi-transparent white
        g2d.drawString("Team : UbiRiotHoyoverse", 14, 24);

        // Draw title image first - centered but allowing space for text
        if (image != null) {
            int imageWidth = image.getWidth(null);
            int imageHeight = image.getHeight(null);
            
            // Scale image to fit nicely in upper portion (leaving space for text)
            double maxImageHeight = d.height * 0.5; // Use only 50% of screen height
            double scale = Math.min(1.0, maxImageHeight / imageHeight);
            
            int scaledWidth = (int)(imageWidth * scale);
            int scaledHeight = (int)(imageHeight * scale);
            
            int imageX = (BOARD_WIDTH - scaledWidth) / 2;
            int imageY = 50; // Slightly lower to avoid team name
            
            g.drawImage(image, imageX, imageY, scaledWidth, scaledHeight, null);
            
            // Text area starts after image with clear separation
            int textStartY = imageY + scaledHeight + 40; // 40px gap after image
            
            drawTextContent(g2d, textStartY);
        } else {
            // Fallback if image fails to load
            drawTextContent(g2d, 100);
        }

        Toolkit.getDefaultToolkit().sync();
    }
    
    private void drawTextContent(Graphics2D g2d, int startY) {
        // Semi-transparent background for text area
        g2d.setColor(new Color(0, 0, 0, 100)); // Semi-transparent black
        g2d.fillRoundRect(50, startY - 20, d.width - 100, d.height - startY - 50, 15, 15);
        
        // Team info section
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        String teamTitle = "Team Members:";
        int teamTitleX = (d.width - g2d.getFontMetrics().stringWidth(teamTitle)) / 2;
        g2d.drawString(teamTitle, teamTitleX, startY + 20);
        
        // Team members
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        String[] teamMembers = {
            "KHAING THIN ZAR SEIN - ID: 6530381",
            "SAI OAN HSENG HURK - ID: 6440041", 
            "LIN MYAT PHYO - ID: 6530201"
        };
        
        for (int i = 0; i < teamMembers.length; i++) {
            int memberX = (d.width - g2d.getFontMetrics().stringWidth(teamMembers[i])) / 2;
            g2d.drawString(teamMembers[i], memberX, startY + 50 + (i * 20));
        }

        // Mode selector
        drawModeSelector(g2d, startY + 140);
        
        // Controls instruction - bottom
        g2d.setColor(Color.CYAN);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        String controls = "Mode Controls: UP/DOWN arrows, ENTER to select";
        int controlsX = (d.width - g2d.getFontMetrics().stringWidth(controls)) / 2;
        g2d.drawString(controls, controlsX, d.height - 42);
        
        // Game credit - bottom corner (smaller, less prominent)
        g2d.setColor(new Color(128, 128, 128));
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.drawString("Game Development Project", 10, d.height - 10);
    }
    
    private void drawModeSelector(Graphics2D g2d, int startY) {
        // Mode selector title
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        String modeTitle = "Select Game Mode:";
        int titleX = (d.width - g2d.getFontMetrics().stringWidth(modeTitle)) / 2;
        g2d.drawString(modeTitle, titleX, startY);
        
        // Mode options
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        for (int i = 0; i < modeNames.length; i++) {
            String modeText = modeNames[i];
            int modeX = (d.width - g2d.getFontMetrics().stringWidth(modeText)) / 2;
            
            if (i == selectedMode) {
                // Selected mode background
                g2d.setColor(new Color(255, 215, 0, 150)); // Semi-transparent gold
                g2d.fillRoundRect(modeX - 15, startY + 15 + (i * 30), 
                                g2d.getFontMetrics().stringWidth(modeText) + 30, 25, 10, 10);
                
                // Selected mode text with glow effect
                g2d.setColor(new Color(255, 255, 255, 100)); // Subtle glow
                Font unicodeFont = FontManager.getUnicodeFont(16);
                g2d.setFont(unicodeFont);
                g2d.drawString("▶ " + modeText + " ◀", modeX - 21, startY + 31 + (i * 30));
                g2d.setColor(Color.YELLOW);
                g2d.drawString("▶ " + modeText + " ◀", modeX - 20, startY + 30 + (i * 30));
            } else {
                // Unselected mode with better contrast
                g2d.setColor(new Color(200, 200, 200));
                g2d.drawString(modeText, modeX, startY + 30 + (i * 30));
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
