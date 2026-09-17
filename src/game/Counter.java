package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import model.Menu;

/**
 * Counter = tempat pengambilan makanan. Kurir menghampiri counter untuk
 * mengambil (pickup) Menu yang cocok dengan pesanan sebuah meja.
 *
 * Setiap counter menyediakan satu jenis Menu (dari class model.Menu).
 */
public class Counter extends GameEntity {

    private final Menu menu;

    public Counter(int x, int y, Menu menu) {
        super(x, y, 90, 50);
        this.menu = menu;
    }

    public Menu getMenu() {
        return menu;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(new Color(0x8B, 0x57, 0x2A)); // meja counter coklat
        g.fillRoundRect(x, y, width, height, 10, 10);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.drawString(menu.getName(), x + 6, y + 20);
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.drawString("Rp" + (int) menu.getPrice(), x + 6, y + 36);
    }
}
