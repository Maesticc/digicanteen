package game;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * TrashStation = tempat sampah. Dipakai untuk membuang bahan/masakan yang
 * salah supaya chef bisa mulai ulang.
 */
public class TrashStation extends Station {

    public TrashStation(int x, int y, int width, int height) {
        super(x, y, width, height, "Sampah");
    }

    @Override
    public void interact(Chef chef, GamePanel game) {
        if (chef.handsFree()) {
            game.flash("Tempat sampah kosong");
            return;
        }
        String nama = chef.getCarrying().label();
        chef.setCarrying(null);
        game.flash(nama + " dibuang");
    }

    @Override
    public void draw(Graphics2D g) {
        drawBase(g, new Color(0x5D, 0x6D, 0x7E), new Color(0x7F, 0x8C, 0x8D));

        int cx = centerX();
        int cy = y + height / 2 + 4;

        // tong sampah
        g.setColor(new Color(0x34, 0x49, 0x5E));
        g.fillRoundRect(cx - 14, cy - 8, 28, 22, 5, 5);
        g.setColor(new Color(0x21, 0x2F, 0x3D));
        g.fillRoundRect(cx - 17, cy - 13, 34, 6, 3, 3);
        // garis-garis tong
        g.setColor(new Color(0x4A, 0x5F, 0x74));
        g.drawLine(cx - 6, cy - 4, cx - 6, cy + 10);
        g.drawLine(cx + 2, cy - 4, cx + 2, cy + 10);

        drawTitle(g, new Color(0xEC, 0xF0, 0xF1));
    }
}
