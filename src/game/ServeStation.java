package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * ServeStation = jendela penyajian. Chef mengantar Dish ke sini; kalau ada
 * tiket pesanan yang cocok, pesanan selesai dan pembayaran diproses
 * (memakai polymorphism model.Payment di GamePanel).
 */
public class ServeStation extends Station {

    private double animTime;

    public ServeStation(int x, int y, int width, int height) {
        super(x, y, width, height, "Jendela Saji");
    }

    @Override
    public void update(double dt) {
        animTime += dt;
    }

    @Override
    public void interact(Chef chef, GamePanel game) {
        Item held = chef.getCarrying();

        if (held == null) {
            game.flash("Bawa masakan jadi ke sini");
            return;
        }
        if (!(held instanceof Dish)) {
            game.flash("Hanya masakan jadi yang bisa disajikan");
            return;
        }
        Dish dish = (Dish) held;
        if (game.tryServe(dish, this)) {
            chef.setCarrying(null);
        }
    }

    @Override
    public void draw(Graphics2D g) {
        drawBase(g, new Color(0x2E, 0x86, 0xC1), new Color(0x5D, 0xAD, 0xE2));

        int cx = centerX();
        int cy = y + height / 2 + 2;

        // meja saji putih
        g.setColor(new Color(0xF4, 0xF6, 0xF7));
        g.fillRoundRect(cx - 26, cy - 10, 52, 20, 6, 6);

        // panah keluar berkedip
        int off = (int) (Math.sin(animTime * 3) * 3);
        g.setColor(new Color(0x1A, 0x5A, 0x86));
        int[] xs = { cx + 14 + off, cx + 24 + off, cx + 14 + off };
        int[] ys = { cy - 6, cy, cy + 6 };
        g.fillPolygon(xs, ys, 3);

        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.setColor(Color.WHITE);
        String s = "ANTAR";
        int tw = g.getFontMetrics().stringWidth(s);
        g.drawString(s, cx - tw / 2, y + height - 6);
    }
}
