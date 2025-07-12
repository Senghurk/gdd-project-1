package gdd.sprite;

import static gdd.Global.*;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import javax.swing.ImageIcon;

public class Alien1 extends Enemy {

    private Bomb bomb;

    public Alien1(int x, int y) {
        super(x, y);
        initEnemy(x, y); // Uncomment to enable bomb functionality
    }

    private void initEnemy(int x, int y) {

        this.x = x;
        this.y = y;

        bomb = new Bomb(x, y);

        var ii = new ImageIcon(IMG_ENEMY);

        // Scale the image to use the global scaling factor
        var scaledImage = ii.getImage().getScaledInstance(ii.getIconWidth() * SCALE_FACTOR,
                ii.getIconHeight() * SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }

    public void act(int direction) {
        this.x -= Math.abs(direction); // Move alien leftward instead of downward
    }

    public void act() {
        act(1); // Default direction for autonomous movement
    }

    public Bomb getBomb() {

        return bomb;
    }

    public class Bomb extends Sprite {

        private boolean destroyed;

        public Bomb(int x, int y) {

            initBomb(x, y);
        }

        private void initBomb(int x, int y) {

            setDestroyed(true);

            this.x = x;
            this.y = y;

            var bombImg = "src/images/bomb.png";
            var ii = new ImageIcon(bombImg);
            
            // TODO: Re-enable rotation once image loading is fixed
            // var rotatedImage = rotateImage(ii.getImage(), 90);
            // setImage(rotatedImage);
            setImage(ii.getImage());
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

        public void setDestroyed(boolean destroyed) {

            this.destroyed = destroyed;
        }

        public boolean isDestroyed() {

            return destroyed;
        }

        public void act() {
            if (!destroyed) {
                this.x -= 2; // Move bomb leftward when active instead of downward
            }
        }

        public void act(int direction) {
            // Bombs typically move leftward regardless of direction
            act();
        }
    }
}
