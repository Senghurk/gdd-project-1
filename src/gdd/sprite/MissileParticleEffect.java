package gdd.sprite;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import gdd.Global;

public class MissileParticleEffect {
    static class Particle {
        double x, y;
        double vx, vy;
        int lifetime;
        int initialLifetime;
        Color color;

        public Particle(double x, double y, double vx, double vy, int lifetime) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.lifetime = lifetime;
            this.initialLifetime = lifetime;
            this.color = new Color(255, 255, 255, 255); // Bright white for visibility on black background
        }

        public void update() {
            // Apply deceleration (air resistance)
            vx *= 0.95;
            vy *= 0.95;
            // Update position
            x += vx;
            y += vy;
            lifetime--;

            // Update color and transparency based on remaining lifetime
            if (lifetime > 0) {
                float ratio = (float) lifetime / initialLifetime;
                int red = 255;
                int green = (int) (200 * ratio); // Bright orange to red transition
                int blue = (int) (100 * ratio);  // Add some blue for brightness
                int alpha = Math.max(180, (int) (255 * ratio)); // Keep particles more visible
                color = new Color(red, green, blue, alpha); // Bright orange to red, highly visible
            }
        }

        public void draw(Graphics g) {
            g.setColor(color);
            g.fillOval((int) x, (int) y, 2, 2); // Much smaller 2x2 pixels for subtle effect
        }

        public boolean isAlive() {
            return lifetime > 0;
        }
    }

    private List<Particle> particles = new ArrayList<>();

    // Updated method with speedRatio parameter for intensity scaling
    public void createMissileExhaust(double missileX, double missileY, double speedRatio) {
        // Much fewer particles to avoid visual obstruction (3-8 particles)
        int baseParticles = 3;
        int bonusParticles = (int)(5 * speedRatio);
        int numParticles = baseParticles + bonusParticles;
        
        double spreadAngle = Math.PI / 6; // Narrower 30-degree spread for more focused exhaust

        for (int i = 0; i < numParticles; i++) {
            double angle, speed;
            double particleX = missileX;
            double particleY = missileY;
            
            if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
                // Vertical mode: particles shoot upward, slightly to the right of missile center
                particleX += 4; // Reduced offset - less to the right
                particleY += 5; // Offset slightly down from missile tip
                angle = -Math.PI/2 + (Math.random() - 0.5) * spreadAngle; // Upward with narrow spread
                speed = (1 + Math.random() * 2) * (0.3 + speedRatio * 0.4); // Slower, subtler particles
            } else {
                // Horizontal mode: particles shoot to the right (opposite of missile direction)
                particleX += 5; // Offset from missile tip  
                particleY += 6; // Center vertically
                angle = 0 + (Math.random() - 0.5) * spreadAngle; // Rightward with narrow spread
                speed = (1 + Math.random() * 2) * (0.3 + speedRatio * 0.4); // Slower, subtler particles
            }
            
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            
            // Shorter lifetime for subtler effect (10-20 frames)
            int lifetime = (int)(10 + 10 * speedRatio);
            particles.add(new Particle(particleX, particleY, vx, vy, lifetime));
        }
    }
    
    // Keep old method for backwards compatibility
    public void createMissileExhaust(double missileX, double missileY) {
        createMissileExhaust(missileX, missileY, 0.5); // Default to medium intensity
    }

    // Update all particles in the game loop
    public void update() {
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle p = iterator.next();
            p.update();
            if (!p.isAlive()) {
                iterator.remove(); // Safely remove expired particles
            }
        }
    }

    // Draw all particles in the paint method
    public void draw(Graphics g) {
        for (Particle p : particles) {
            p.draw(g);
        }
    }
    
    // Get number of active particles (for debugging/performance monitoring)
    public int getParticleCount() {
        return particles.size();
    }
}