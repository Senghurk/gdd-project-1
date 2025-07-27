package gdd;

import java.awt.EventQueue;

public class Main {
    public static void main(String[] args) {

        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("file.encoding", "UTF-8");
        
        EventQueue.invokeLater(() -> {
            var game = new Game();
            game.setVisible(true);
        });
    }
}