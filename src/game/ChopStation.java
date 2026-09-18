package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * ChopStation = talenan. Bahan mentah yang diletakkan di sini akan dipotong
 * otomatis (ada progress bar). Setelah selesai, bahan siap dimasukkan ke panci.
 */
public class ChopStation extends Station {

    private static final double CHOP_TIME = 1.6; // detik

    private Ingredient slot;   // bahan yang sedang di talenan
    private double progress;   // 0..1
    private double animTime;

    public ChopStation(int x, int y, int width, int height) {
        super(x, y, width, height, "Talenan");
    }

    @Override
    public void update(double dt) {
        animTime += dt;
        if (slot != null && slot.getPrep() == Ingredient.Prep.RAW) {
            progress += dt / CHOP_TIME;
            if (progress >= 1.0) {
                progress = 1.0;
                slot.setPrep(Ingredient.Prep.CHOPPED);
            }
        }
    }

    @Override
    public void interact(Chef chef, GamePanel game) {
        Item held = chef.getCarrying();

        // tangan kosong -> ambil bahan dari talenan
        if (held == null) {
            if (slot == null) {
                game.flash("Talenan kosong - bawa bahan mentah ke sini");
                return;
            }
            chef.setCarrying(slot);
            boolean sudah = slot.getPrep() == Ingredient.Prep.CHOPPED;
            slot = null;
            progress = 0;
            game.flash(sudah ? "Ambil bahan yang sudah dipotong" : "Bahan diambil kembali");
            return;
        }

        // membawa bahan -> taruh di talenan
        if (held instanceof Ingredient) {
            Ingredient ing = (Ingredient) held;
            if (ing.getPrep() == Ingredient.Prep.CHOPPED) {
                game.flash("Sudah dipotong - bawa ke panci");
                return;
            }
            if (slot != null) {
                game.flash("Talenan masih terisi");
                return;
            }
            slot = ing;
            chef.setCarrying(null);
            progress = 0;
            game.flash("Memotong " + ing.getType().getLabel() + "...");
            return;
        }

        game.flash("Masakan jadi tidak perlu dipotong");
    }

    @Override
    public void draw(Graphics2D g) {
        drawBase(g, new Color(0x9C, 0x7A, 0x4A), new Color(0xC8, 0xA3, 0x6A));

        int cx = centerX();
        int cy = y + height / 2 + 2;

        // papan talenan
        g.setColor(new Color(0xDE, 0xB8, 0x87));
        g.fillRoundRect(cx - 22, cy - 12, 44, 24, 6, 6);
        g.setColor(new Color(0x8B, 0x6A, 0x3F));
        g.drawRoundRect(cx - 22, cy - 12, 44, 24, 6, 6);

        if (slot != null) {
            slot.draw(g, cx, cy, 16);

            if (slot.getPrep() == Ingredient.Prep.RAW) {
                // pisau bergerak naik-turun saat memotong
                int knifeY = cy - 22 + (int) (Math.abs(Math.sin(animTime * 12)) * 8);
                g.setColor(new Color(0xBD, 0xC3, 0xC7));
                g.fillRect(cx + 10, knifeY, 3, 14);
                g.setColor(new Color(0x5D, 0x40, 0x25));
                g.fillRect(cx + 8, knifeY + 14, 7, 5);

                // progress bar
                int barW = width - 16;
                g.setColor(new Color(0x2C, 0x2C, 0x2C));
                g.fillRoundRect(x + 8, y + height - 16, barW, 7, 4, 4);
                g.setColor(new Color(0xF3, 0x9C, 0x12));
                g.fillRoundRect(x + 8, y + height - 16, (int) (barW * progress), 7, 4, 4);
            } else {
                // tanda siap diambil
                g.setFont(new Font("SansSerif", Font.BOLD, 10));
                g.setColor(new Color(0x14, 0x6C, 0x43));
                String s = "siap";
                int tw = g.getFontMetrics().stringWidth(s);
                g.drawString(s, cx - tw / 2, y + height - 6);
            }
        } else {
            drawTitle(g, new Color(0xFF, 0xF6, 0xE0));
        }
    }
}
