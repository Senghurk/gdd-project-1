package gdd;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.*;

public class SoundEffectPlayer {
    
    // Thread pool for playing sounds asynchronously
    private static final ExecutorService soundExecutor = Executors.newCachedThreadPool();
    
    // Static method to play a sound effect once (NON-BLOCKING)
    public static void playSound(String filePath) {
        // Run sound loading and playing in a separate thread
        soundExecutor.submit(() -> {
            try {
                // Check if file exists
                File audioFile = new File(filePath);
                if (!audioFile.exists()) {
                    return; // Silently fail if file doesn't exist
                }
                
                // Create AudioInputStream object
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile.getAbsoluteFile());
                
                // Create clip reference
                Clip clip = AudioSystem.getClip();
                
                // Open audioInputStream to the clip
                clip.open(audioInputStream);
                
                // Set volume to a lower level (30%) to avoid overwhelming background music
                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    float min = gainControl.getMinimum();
                    float max = gainControl.getMaximum();
                    float range = max - min;
                    float gain = min + (range * 1.0f); // 100% volume for sound effects
                    gainControl.setValue(gain);
                }
                
                // Play the sound once (DO NOT LOOP for sound effects)
                clip.start();
                
                // Add listener to clean up when sound finishes
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                        try {
                            audioInputStream.close();
                        } catch (IOException e) {
                            // Ignore close errors
                        }
                    }
                });
                
            } catch (Exception e) {
                // Silently ignore sound errors to not disrupt gameplay
                // System.err.println("Sound error: " + e.getMessage());
            }
        });
    }
    
    // Convenience methods for specific sound effects
    public static void playShootSound() {
        playSound("src/audio/Shoot.wav");
    }
    
    public static void playEnemyExplodeSound() {
        playSound("src/audio/enemyExplode.wav");
    }
    
    public static void playPlayerHitSound() {
        playSound("src/audio/playerDown.wav");
    }
    
    public static void playBossIntroSound() {
        playSound("src/audio/bossIntro.wav");
    }

    public static void playVictorySound() {
        playSound("src/audio/Victory.wav");
    }
    
    public static void playGameOverSound() {
        playSound("src/audio/gameOver.wav");
    }

    public static void playCatchPowerUpSound() {
        playSound("src/audio/catchPowerUp.wav");
    }
    
    // Cleanup method - call this when the game exits
    public static void shutdown() {
        soundExecutor.shutdown();
    }
}