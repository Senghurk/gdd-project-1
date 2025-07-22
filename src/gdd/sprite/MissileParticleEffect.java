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
            g.fillOval((int) x, (int) y, 4, 4); // Draw as a 4x4 circle for better visibility
        }

        public boolean isAlive() {
            return lifetime > 0;
        }
    }

    private List<Particle> particles = new ArrayList<>();

    // Call this when the missile is fired - adapted for our game modes
    public void createMissileExhaust(double missileX, double missileY) {
        int numParticles = 25; // Increased for better trail effect
        double spreadAngle = Math.PI / 3; // 60-degree spread for wider exhaust cone

        for (int i = 0; i < numParticles; i++) {
            double angle, speed;
            
            if (Global.CURRENT_GAME_MODE == Global.MODE_VERTICAL) {
                // Vertical mode: particles shoot upward (opposite of missile direction)
                angle = -Math.PI/2 + (Math.random() - 0.5) * spreadAngle; // Upward with spread
                speed = 2 + Math.random() * 3; // Random speed between 2 and 5
            } else {
                // Horizontal mode: particles shoot to the right (opposite of missile direction)  
                angle = 0 + (Math.random() - 0.5) * spreadAngle; // Rightward with spread
                speed = 2 + Math.random() * 3;
            }
            
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            particles.add(new Particle(missileX, missileY, vx, vy, 25)); // 25-frame lifetime for longer trail
        }
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