/* 
// Java program to play an Audio
// file using Clip Object
package gdd;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.FloatControl;

public class AudioPlayer {

    // to store current position
    Long currentFrame;
    Clip clip;

    // current status of clip
    String status;

    AudioInputStream audioInputStream;
    String filePath;

    // constructor to initialize streams and clip
    public AudioPlayer(String filePath)
            throws UnsupportedAudioFileException,
            IOException, LineUnavailableException {
        // create AudioInputStream object
        this.filePath = filePath;
        audioInputStream
                = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());

        // create clip reference
        clip = AudioSystem.getClip();

        // open audioInputStream to the clip
        clip.open(audioInputStream);

        clip.loop(Clip.LOOP_CONTINUOUSLY);
        
        // Set default volume to 50% (audible but not too loud)
        setVolume(0.5f);
    }

    public static void main(String[] args) {
        try {
            String filePath = "src/audio/title.wav";
            AudioPlayer audioPlayer = new AudioPlayer(filePath);

            audioPlayer.play();
            Scanner sc = new Scanner(System.in);

            while (true) {
                System.out.println("1. pause");
                System.out.println("2. resume");
                System.out.println("3. restart");
                System.out.println("4. stop");
                System.out.println("5. Jump to specific time");
                int c = sc.nextInt();
                audioPlayer.gotoChoice(c);
                if (c == 4) {
                    break;
                }
            }
            sc.close();
        } catch (Exception ex) {
            System.out.println("Error with playing sound.");
            ex.printStackTrace();

        }
    }

    // Work as the user enters his choice
    private void gotoChoice(int c)
            throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        switch (c) {
            case 1:
                pause();
                break;
            case 2:
                resumeAudio();
                break;
            case 3:
                restart();
                break;
            case 4:
                stop();
                break;
            case 5:
                System.out.println("Enter time (" + 0
                        + ", " + clip.getMicrosecondLength() + ")");
                Scanner sc = new Scanner(System.in);
                long c1 = sc.nextLong();
                jump(c1);
                break;

        }

    }

    // Method to play the audio
    public void play() {
        //start the clip
        clip.start();

        status = "play";
        
        // Reset boss intro counters
        if (isBossIntro) {
            playFrames = 0;
            fadingOut = false;
            fadeFrames = 0;
        }
    }


    // Method to pause the audio
    public void pause() {
        if (status.equals("paused")) {
            System.out.println("audio is already paused");
            return;
        }
        this.currentFrame
                = this.clip.getMicrosecondPosition();
        clip.stop();
        status = "paused";
    }

    // Method to resume the audio
    public void resumeAudio() throws UnsupportedAudioFileException,
            IOException, LineUnavailableException {
        if (status.equals("play")) {
            System.out.println("Audio is already "
                    + "being played");
            return;
        }
        clip.close();
        resetAudioStream();
        clip.setMicrosecondPosition(currentFrame);
        this.play();
    }

    // Method to restart the audio
    public void restart() throws IOException, LineUnavailableException,
            UnsupportedAudioFileException {
        clip.stop();
        clip.close();
        resetAudioStream();
        currentFrame = 0L;
        clip.setMicrosecondPosition(0);
        this.play();
    }

    // Method to stop the audio
    public void stop() throws UnsupportedAudioFileException,
            IOException, LineUnavailableException {
        currentFrame = 0L;
        clip.stop();
        clip.close();
    }

    // Method to jump over a specific part
    public void jump(long c) throws UnsupportedAudioFileException, IOException,
            LineUnavailableException {
        if (c > 0 && c < clip.getMicrosecondLength()) {
            clip.stop();
            clip.close();
            resetAudioStream();
            currentFrame = c;
            clip.setMicrosecondPosition(c);
            this.play();
        }
    }

    // Method to reset audio stream
    public void resetAudioStream() throws UnsupportedAudioFileException, IOException,
            LineUnavailableException {
        audioInputStream = AudioSystem.getAudioInputStream(
                new File(filePath).getAbsoluteFile());
        clip.open(audioInputStream);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        // Restore volume after reset
        setVolume(0.5f);
    }
    
    // Method to set volume (0.0f to 1.0f)
    public void setVolume(float volume) {
        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();
            
            // Convert linear volume (0.0 to 1.0) to decibel range
            // Use a better formula for volume scaling
            float range = max - min;
            float gain = min + (range * volume);
            
            // Clamp the gain value to valid range
            gain = Math.max(min, Math.min(max, gain));
            gainControl.setValue(gain);
        }
    }

}
*/

// Java program to play an Audio
// file using Clip Object
package gdd;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioPlayer {

    // to store current position
    Long currentFrame;
    Clip clip;

    // current status of clip
    String status;

    AudioInputStream audioInputStream;
    String filePath;
    
    // Boss intro fade-out functionality
    private boolean isBossIntro = false;
    private boolean fadingOut = false;
    private int fadeFrames = 0;
    private static final int FADE_DURATION_FRAMES = 600; // 10 seconds at 60 FPS
    private static final int MAX_PLAY_FRAMES = 600; // Play for exactly 10 seconds
    private int playFrames = 0;
    
    // Volume ducking functionality for background music during boss intro
    private boolean isDucked = false;
    private float originalVolume = 0.8f;
    private float duckedVolume = 0.3f; // 30% volume when ducked

    // constructor to initialize streams and clip
    public AudioPlayer(String filePath)
            throws UnsupportedAudioFileException,
            IOException, LineUnavailableException {
        // create AudioInputStream object
        this.filePath = filePath;
        audioInputStream
                = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());

        // create clip reference
        clip = AudioSystem.getClip();

        // open audioInputStream to the clip
        clip.open(audioInputStream);

        clip.loop(Clip.LOOP_CONTINUOUSLY);
        
        // Set volume to 80% for audible background music
        setVolume(0.8f);
    }
    
    // Boss intro constructor - no looping, fade-out enabled
    public AudioPlayer(String filePath, boolean bossIntroMode)
            throws UnsupportedAudioFileException,
            IOException, LineUnavailableException {
        this.filePath = filePath;
        this.isBossIntro = bossIntroMode;
        
        audioInputStream = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());
        clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        
        // Boss intro doesn't loop
        if (!isBossIntro) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
        
        // Set volume to 80% for boss intro
        setVolume(0.8f);
    }

    public static void main(String[] args) {
        try {
            String filePath = "src/audio/title.wav";
            AudioPlayer audioPlayer = new AudioPlayer(filePath);

            audioPlayer.play();
            Scanner sc = new Scanner(System.in);

            while (true) {
                System.out.println("1. pause");
                System.out.println("2. resume");
                System.out.println("3. restart");
                System.out.println("4. stop");
                System.out.println("5. Jump to specific time");
                int c = sc.nextInt();
                audioPlayer.gotoChoice(c);
                if (c == 4) {
                    break;
                }
            }
            sc.close();
        } catch (Exception ex) {
            System.out.println("Error with playing sound.");
            ex.printStackTrace();

        }
    }

    // Work as the user enters his choice
    private void gotoChoice(int c)
            throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        switch (c) {
            case 1:
                pause();
                break;
            case 2:
                resumeAudio();
                break;
            case 3:
                restart();
                break;
            case 4:
                stop();
                break;
            case 5:
                System.out.println("Enter time (" + 0
                        + ", " + clip.getMicrosecondLength() + ")");
                Scanner sc = new Scanner(System.in);
                long c1 = sc.nextLong();
                jump(c1);
                break;

        }

    }

    // Method to play the audio
    public void play() {
        //start the clip
        clip.start();

        status = "play";
        
        // Reset boss intro counters
        if (isBossIntro) {
            playFrames = 0;
            fadingOut = false;
            fadeFrames = 0;
        }
    }


    // Method to pause the audio
    public void pause() {
        if (status.equals("paused")) {
            System.out.println("audio is already paused");
            return;
        }
        this.currentFrame
                = this.clip.getMicrosecondPosition();
        clip.stop();
        status = "paused";
    }

    // Method to resume the audio
    public void resumeAudio() throws UnsupportedAudioFileException,
            IOException, LineUnavailableException {
        if (status.equals("play")) {
            System.out.println("Audio is already "
                    + "being played");
            return;
        }
        clip.close();
        resetAudioStream();
        clip.setMicrosecondPosition(currentFrame);
        this.play();
    }

    // Method to restart the audio
    public void restart() throws IOException, LineUnavailableException,
            UnsupportedAudioFileException {
        clip.stop();
        clip.close();
        resetAudioStream();
        currentFrame = 0L;
        clip.setMicrosecondPosition(0);
        this.play();
    }

    // Method to stop the audio
    public void stop() throws UnsupportedAudioFileException,
            IOException, LineUnavailableException {
        currentFrame = 0L;
        clip.stop();
        clip.close();
    }

    // Method to jump over a specific part
    public void jump(long c) throws UnsupportedAudioFileException, IOException,
            LineUnavailableException {
        if (c > 0 && c < clip.getMicrosecondLength()) {
            clip.stop();
            clip.close();
            resetAudioStream();
            currentFrame = c;
            clip.setMicrosecondPosition(c);
            this.play();
        }
    }

    // Method to reset audio stream
    public void resetAudioStream() throws UnsupportedAudioFileException, IOException,
            LineUnavailableException {
        audioInputStream = AudioSystem.getAudioInputStream(
                new File(filePath).getAbsoluteFile());
        clip.open(audioInputStream);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        // Restore volume after reset
        setVolume(0.8f);
    }
    
    // Method to set volume (0.0f to 1.0f)
    public void setVolume(float volume) {
        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();
            
            // Convert linear volume (0.0 to 1.0) to decibel range
            float range = max - min;
            float gain = min + (range * volume);
            
            // Clamp the gain value to valid range
            gain = Math.max(min, Math.min(max, gain));
            gainControl.setValue(gain);
        }
    }
    
    // Update method for boss intro fade-out (call this every frame)
    public void update() {
        if (isBossIntro && clip != null && clip.isRunning()) {
            playFrames++;
            
            // Start fade-out immediately since we only want 10 seconds
            if (playFrames >= MAX_PLAY_FRAMES - FADE_DURATION_FRAMES && !fadingOut) {
                fadingOut = true;
                fadeFrames = 0;
            }
            
            // Apply fade-out effect
            if (fadingOut) {
                fadeFrames++;
                float fadeRatio = 1.0f - ((float) fadeFrames / FADE_DURATION_FRAMES);
                fadeRatio = Math.max(0.0f, fadeRatio); // Clamp to 0
                
                // Apply fade volume (base 80% * fade ratio)
                setVolume(0.8f * fadeRatio);
                
                // Stop when fade is complete or max time reached
                if (fadeFrames >= FADE_DURATION_FRAMES || playFrames >= MAX_PLAY_FRAMES) {
                    try {
                        stop();
                    } catch (Exception e) {
                        // Ignore stop errors during fade
                    }
                }
            }
        }
    }
    
    public boolean isPlaying() {
        return clip != null && clip.isRunning();
    }
    
    // Volume ducking methods for smooth audio transitions
    public void duck() {
        if (!isDucked) {
            isDucked = true;
            setVolume(duckedVolume);
        }
    }
    
    public void unduck() {
        if (isDucked) {
            isDucked = false;
            setVolume(originalVolume);
        }
    }
    
    public boolean isDucked() {
        return isDucked;
    }

}

