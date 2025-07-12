package gdd.sprite;

import static gdd.Global.*;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import javax.swing.ImageIcon;

public class Shot extends Sprite {

    private static final int H_SPACE = 1; // Horizontal offset from player 
    private static final int V_SPACE = 20; // Vertical offset from player center

    public Shot() {
    }

    public Shot(int x, int y) {

        initShot(x, y);
    }

    private void initShot(int x, int y) {

        var ii = new ImageIcon(IMG_SHOT);

        // Scale the image to use the global scaling factor
        var scaledImage = ii.getImage().getScaledInstance(ii.getIconWidth() * SCALE_FACTOR,
                ii.getIconHeight() * SCALE_FACTOR, 
                java.awt.Image.SCALE_SMOOTH);
        
        // TODO: Re-enable rotation once image loading is fixed
        // var rotatedImage = rotateImage(scaledImage, 90);
        // setImage(rotatedImage);
        setImage(scaledImage);

        setX(x + H_SPACE); // Spawn shot slightly to the right of player
        setY(y + V_SPACE); // Spawn shot at player's vertical center
    }

    private BufferedImage rotateImage(java.awt.Image image, double angle) {
        // Ensure the image is fully loaded
        if (image.getWidth(null) <= 0 || image.getHeight(null) <= 0) {
            // If dimensions are not available, return a simple BufferedImage conversion
            BufferedImage bufferedImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bufferedImage.createGraphics();
            g.drawImage(image, 0, 0, 16, 16, null);
            g.dispose();
            image = bufferedImage;
        }
        
        // Convert to BufferedImage if needed
        BufferedImage bufferedImage;
        if (image instanceof BufferedImage) {
            bufferedImage = (BufferedImage) image;
        } else {
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bufferedImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
        }

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        // Create new image with swapped dimensions for 90-degree rotation
        BufferedImage rotatedImage = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotatedImage.createGraphics();

        // Rotate around center
        AffineTransform transform = new AffineTransform();
        transform.translate(height / 2.0, width / 2.0);
        transform.rotate(Math.toRadians(angle));
        transform.translate(-width / 2.0, -height / 2.0);

        g2d.setTransform(transform);
        g2d.drawImage(bufferedImage, 0, 0, null);
        g2d.dispose();

        return rotatedImage;
    }

    public void act() {
        this.x += 8; // Increased speed for faster pacing (was 4)
    }

    public void act(int direction) {
        // Shots move rightward for sideways gameplay
        act();
    }
}
