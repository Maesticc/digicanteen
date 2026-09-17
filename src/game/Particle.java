package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/**
 * Particle = partikel kecil (koin/bintang) yang menyembur lalu jatuh & memudar.
 * Dipakai untuk efek "burst" saat pelanggan berhasil dilayani.
 */
public class Particle {

    private static final Random RNG = new Random();

    private double x, y;
    private double vx, vy;
    private double life;
    private final double maxLife;
    private final Color color;
    private final int size;

    public Particle(double x, double y, Color color) {
        this.x = x;
        this.y = y;
        double angle = RNG.nextDouble() * Math.PI * 2;
        double speed = 60 + RNG.nextDouble() * 140;
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed - 60; // sedikit ke atas
        this.maxLife = 0.6 + RNG.nextDouble() * 0.5;
        this.life = maxLife;
        this.color = color;
        this.size = 4 + RNG.nextInt(5);
    }

    public void update(double dt) {
        x += vx * dt;
        y += vy * dt;
        vy += 320 * dt; // gravitasi
        life -= dt;
    }

    public boolean isDead() {
        return life <= 0;
    }

    public void draw(Graphics2D g) {
        float alpha = (float) Math.max(0, Math.min(1, life / maxLife));
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255)));
        g.fillOval((int) x, (int) y, size, size);
    }
}
