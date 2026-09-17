package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import model.Menu;

/**
 * Player = kurir kantin. Bergerak dengan keyboard dan bisa membawa SATU Menu
 * dari counter untuk diantar ke meja pelanggan.
 *
 * Menunjukkan INHERITANCE (extends GameEntity) dan penggunaan class model.Menu.
 */
public class Player extends GameEntity {

    private int speed = 4;
    private Menu carrying; // menu yang sedang dibawa (null = tangan kosong)

    // arah gerak aktif
    private boolean up, down, left, right;

    // batas area gerak
    private final int fieldWidth;
    private final int fieldHeight;

    public Player(int x, int y, int fieldWidth, int fieldHeight) {
        super(x, y, 32, 32);
        this.fieldWidth = fieldWidth;
        this.fieldHeight = fieldHeight;
    }

    public void update() {
        if (up)    y -= speed;
        if (down)  y += speed;
        if (left)  x -= speed;
        if (right) x += speed;

        // jaga agar tetap di dalam lapangan
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + width > fieldWidth)  x = fieldWidth - width;
        if (y + height > fieldHeight) y = fieldHeight - height;
    }

    @Override
    public void draw(Graphics2D g) {
        // badan kurir
        g.setColor(new Color(0x2E, 0x86, 0xC1));
        g.fillRoundRect(x, y, width, height, 8, 8);

        // "topi" biar kelihatan seperti pelayan
        g.setColor(Color.WHITE);
        g.fillRect(x + 6, y - 4, width - 12, 6);

        // kalau sedang membawa menu, tampilkan labelnya di atas kepala
        if (carrying != null) {
            g.setColor(new Color(0, 0, 0, 160));
            g.fillRoundRect(x - 10, y - 24, width + 20, 16, 6, 6);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            g.drawString(carrying.getName(), x - 6, y - 12);
        }
    }

    // --- membawa menu ---

    public boolean isCarrying() {
        return carrying != null;
    }

    public Menu getCarrying() {
        return carrying;
    }

    public void pickUp(Menu menu) {
        this.carrying = menu;
    }

    public Menu dropCarrying() {
        Menu m = carrying;
        carrying = null;
        return m;
    }

    // --- kontrol arah (dipanggil dari key listener) ---

    public void setUp(boolean v)    { this.up = v; }
    public void setDown(boolean v)  { this.down = v; }
    public void setLeft(boolean v)  { this.left = v; }
    public void setRight(boolean v) { this.right = v; }
}
