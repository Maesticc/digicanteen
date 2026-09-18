package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Chef = karakter yang dikendalikan pemain (gerak dengan WASD / tombol panah).
 * Chef hanya bisa membawa SATU Item sekaligus (bahan atau masakan jadi),
 * persis seperti gaya permainan Overcooked.
 *
 * Contoh INHERITANCE: Chef adalah turunan GameEntity.
 */
public class Chef extends GameEntity {

    private static final double SPEED = 210; // piksel per detik

    private boolean up, down, left, right;
    private Item carrying;

    private double animTime;
    private boolean moving;

    // arah hadap (untuk menentukan titik interaksi di depan chef)
    private int faceX = 0;
    private int faceY = 1;

    public Chef(int x, int y) {
        super(x, y, 34, 34);
    }

    /**
     * Gerakkan chef dengan collision terhadap station.
     * Sumbu X dan Y diuji terpisah supaya chef bisa menyusur tepi meja.
     */
    public void update(double dt, List<Station> stations,
                       int minX, int minY, int maxX, int maxY) {
        animTime += dt;

        double dx = 0, dy = 0;
        if (up) dy -= 1;
        if (down) dy += 1;
        if (left) dx -= 1;
        if (right) dx += 1;

        if (dx == 0 && dy == 0) {
            moving = false;
            return;
        }
        moving = true;

        double len = Math.sqrt(dx * dx + dy * dy);
        dx /= len;
        dy /= len;
        faceX = (int) Math.signum(dx);
        faceY = (int) Math.signum(dy);

        // --- gerak sumbu X ---
        int oldX = x;
        x += (int) Math.round(dx * SPEED * dt);
        if (x < minX) x = minX;
        if (x + width > maxX) x = maxX - width;
        if (hitsAny(stations)) x = oldX;

        // --- gerak sumbu Y ---
        int oldY = y;
        y += (int) Math.round(dy * SPEED * dt);
        if (y < minY) y = minY;
        if (y + height > maxY) y = maxY - height;
        if (hitsAny(stations)) y = oldY;
    }

    private boolean hitsAny(List<Station> stations) {
        for (Station s : stations) {
            if (s.isSolid() && intersects(s)) {
                return true;
            }
        }
        return false;
    }

    /** Titik di depan chef, dipakai untuk mencari station yang akan dipakai. */
    public int reachX() {
        return centerX() + faceX * 26;
    }

    public int reachY() {
        return centerY() + faceY * 26;
    }

    // --- membawa item ---

    public Item getCarrying() {
        return carrying;
    }

    public void setCarrying(Item item) {
        this.carrying = item;
    }

    public boolean handsFree() {
        return carrying == null;
    }

    // --- kontrol arah ---

    public void setUp(boolean v) { this.up = v; }
    public void setDown(boolean v) { this.down = v; }
    public void setLeft(boolean v) { this.left = v; }
    public void setRight(boolean v) { this.right = v; }

    // ------------------------------------------------------------------
    // Drawing
    // ------------------------------------------------------------------

    @Override
    public void draw(Graphics2D g) {
        // langkah kaki: badan sedikit naik-turun saat berjalan
        int bob = moving ? (int) (Math.sin(animTime * 14) * 2) : 0;
        int dy = y + bob;

        // bayangan
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(x + 3, y + height - 6, width - 6, 9);

        // badan (seragam chef putih)
        g.setColor(Color.WHITE);
        g.fillRoundRect(x + 3, dy + 14, width - 6, height - 14, 10, 10);
        g.setColor(new Color(0xD5, 0xD8, 0xDC));
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(x + 3, dy + 14, width - 6, height - 14, 10, 10);

        // dasi/kancing biar terlihat seperti koki
        g.setColor(new Color(0xE7, 0x4C, 0x3C));
        g.fillRect(x + width / 2 - 2, dy + 18, 4, 8);

        // kepala
        g.setColor(new Color(0xF5, 0xCB, 0xA7));
        g.fillOval(x + 6, dy, width - 12, 20);

        // topi chef
        g.setColor(Color.WHITE);
        g.fillRoundRect(x + 4, dy - 8, width - 8, 12, 8, 8);
        g.fillRoundRect(x + 7, dy - 2, width - 14, 8, 4, 4);

        // mata (mengikuti arah hadap)
        g.setColor(new Color(0x2C, 0x2C, 0x2C));
        int ex = x + width / 2 + faceX * 3;
        int ey = dy + 11;
        g.fillOval(ex - 5, ey, 3, 3);
        g.fillOval(ex + 2, ey, 3, 3);

        // item yang dibawa: tampil di atas kepala
        if (carrying != null) {
            int ix = centerX();
            int iy = dy - 22;
            g.setColor(new Color(0, 0, 0, 45));
            g.fillRoundRect(ix - 16, iy - 12, 32, 24, 8, 8);
            carrying.draw(g, ix, iy, 18);

            // nama item
            g.setFont(new Font("SansSerif", Font.BOLD, 9));
            String lbl = carrying.label();
            int tw = g.getFontMetrics().stringWidth(lbl);
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRoundRect(ix - tw / 2 - 4, iy - 28, tw + 8, 13, 6, 6);
            g.setColor(Color.WHITE);
            g.drawString(lbl, ix - tw / 2, iy - 18);
        }
    }
}
