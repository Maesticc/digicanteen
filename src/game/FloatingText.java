package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * FloatingText = teks yang melayang naik lalu memudar. Dipakai untuk
 * menampilkan feedback seperti "+Rp15.000" saat pelanggan dilayani.
 */
public class FloatingText {

    private double x, y;
    private final String text;
    private final Color color;
    private double life;          // sisa umur (detik)
    private final double maxLife;
    private final int size;

    public FloatingText(double x, double y, String text, Color color, int size) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
        this.maxLife = 1.2;
        this.life = maxLife;
        this.size = size;
    }

    public void update(double dt) {
        y -= 40 * dt;   // melayang naik
        life -= dt;
    }

    public boolean isDead() {
        return life <= 0;
    }

    public void draw(Graphics2D g) {
        float alpha = (float) Math.max(0, Math.min(1, life / maxLife));
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255)));
        g.setFont(new Font("SansSerif", Font.BOLD, size));
        g.drawString(text, (int) x, (int) y);
    }
}
