package gdd;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FontManager {
    private static Font unicodeFont = null;
    private static Font unicodeFontBold = null;
    
    static {
        initializeFonts();
    }
    
    private static void initializeFonts() {
        String[] preferredFonts = {
            "Apple Symbols",
            "Segoe UI Emoji",
            "Segoe UI Symbol",
            "Apple Color Emoji",
            "Noto Sans Symbols",
            "DejaVu Sans",
            "Symbola",
            "Arial Unicode MS",
            "Lucida Sans Unicode",
            "Dialog"
        };
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Set<String> availableFonts = new HashSet<>(Arrays.asList(ge.getAvailableFontFamilyNames()));
        
        for (String fontName : preferredFonts) {
            if (availableFonts.contains(fontName)) {
                unicodeFont = new Font(fontName, Font.PLAIN, 14);
                unicodeFontBold = new Font(fontName, Font.BOLD, 14);
                System.out.println("Using Unicode font: " + fontName);
                break;
            }
        }
        
        if (unicodeFont == null) {
            for (String fontName : availableFonts) {
                if (fontName.toLowerCase().contains("symbol") || 
                    fontName.toLowerCase().contains("unicode") ||
                    fontName.toLowerCase().contains("emoji")) {
                    unicodeFont = new Font(fontName, Font.PLAIN, 14);
                    unicodeFontBold = new Font(fontName, Font.BOLD, 14);
                    System.out.println("Using fallback Unicode font: " + fontName);
                    break;
                }
            }
        }
        
        if (unicodeFont == null) {
            unicodeFont = new Font("Dialog", Font.PLAIN, 14);
            unicodeFontBold = new Font("Dialog", Font.BOLD, 14);
            System.out.println("Warning: No Unicode font found, using Dialog");
        }
    }
    
    public static Font getUnicodeFont(int size) {
        return unicodeFont.deriveFont((float) size);
    }
    
    public static Font getUnicodeFontBold(int size) {
        return unicodeFontBold.deriveFont((float) size);
    }
}