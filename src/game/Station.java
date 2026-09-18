package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Station = perabot dapur tempat chef melakukan sesuatu (peti bahan, talenan,
 * panci, jendela penyajian, tempat sampah).
 *
 * Contoh ABSTRACTION + POLYMORPHISM: setiap station punya reaksi berbeda
 * ketika chef menekan tombol aksi di depannya (method interact).
 */
public abstract class Station extends GameEntity {

    protected final String title;
    protected boolean highlighted; // disorot saat menjadi target interaksi

    public Station(int x, int y, int width, int height, String title) {
        super(x, y, width, height);
        this.title = title;
    }

    /** Aksi ketika chef berinteraksi dengan station ini. */
    public abstract void interact(Chef chef, GamePanel game);

    /** Dipanggil setiap frame (default: tidak melakukan apa-apa). */
    public void update(double dt) {
        // subclass boleh menimpa (override)
    }

    /** Station padat = chef tidak bisa menembusnya. */
    public boolean isSolid() {
        return true;
    }

    public void setHighlighted(boolean h) {
        this.highlighted = h;
    }

    public String getTitle() {
        return title;
    }

    // ------------------------------------------------------------------
    // Helper menggambar (dipakai subclass)
    // ------------------------------------------------------------------

    /** Menggambar badan meja dengan permukaan atas berwarna berbeda. */
    protected void drawBase(Graphics2D g, Color body, Color top) {
        // bayangan
        g.setColor(new Color(0, 0, 0, 45));
        g.fillRoundRect(x + 3, y + 5, width, height, 10, 10);

        // badan meja
        g.setColor(body);
        g.fillRoundRect(x, y, width, height, 10, 10);

        // permukaan atas
        g.setColor(top);
        g.fillRoundRect(x, y, width, Math.max(12, height / 3), 10, 10);

        // garis tepi
        g.setColor(body.darker());
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x, y, width, height, 10, 10);

        // sorot saat jadi target interaksi
        if (highlighted) {
            g.setColor(new Color(0xF1, 0xC4, 0x0F));
            g.setStroke(new BasicStroke(3f));
            g.drawRoundRect(x - 3, y - 3, width + 6, height + 6, 12, 12);
        }
    }

    /** Menggambar nama station di bagian bawah meja. */
    protected void drawTitle(Graphics2D g, Color color) {
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.setColor(color);
        int tw = g.getFontMetrics().stringWidth(title);
        g.drawString(title, centerX() - tw / 2, y + height - 6);
    }
}
