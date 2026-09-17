package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import model.Menu;
import model.OrderDetail;
import model.Pembeli;

/**
 * Table = meja pelanggan. Setiap meja "ditempati" oleh seorang Pembeli
 * (class model existing) yang memesan sebuah Menu tertentu.
 *
 * Ketika kurir mengantar Menu yang benar, pesanan dicatat sebagai OrderDetail
 * dan meja dianggap selesai (served).
 */
public class Table extends GameEntity {

    private final int tableNumber;
    private final Pembeli customer;   // pelanggan di meja ini (model.Pembeli)
    private Menu requestedMenu;       // menu yang diminta (model.Menu)
    private OrderDetail completedOrder; // terisi setelah dilayani
    private boolean served;

    public Table(int x, int y, int tableNumber, Pembeli customer, Menu requestedMenu) {
        super(x, y, 70, 70);
        this.tableNumber = tableNumber;
        this.customer = customer;
        this.requestedMenu = requestedMenu;
        this.served = false;
    }

    /**
     * Coba layani meja ini dengan menu yang dibawa kurir.
     * @return true jika menu cocok dan meja berhasil dilayani.
     */
    public boolean serve(Menu delivered) {
        if (served || delivered == null) {
            return false;
        }
        if (delivered.getName().equals(requestedMenu.getName())) {
            completedOrder = new OrderDetail(delivered, 1);
            served = true;
            return true;
        }
        return false;
    }

    public boolean isServed() {
        return served;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public Menu getRequestedMenu() {
        return requestedMenu;
    }

    public Pembeli getCustomer() {
        return customer;
    }

    /** Subtotal pesanan yang sudah selesai (0 jika belum dilayani). */
    public double getOrderSubtotal() {
        return completedOrder == null ? 0 : completedOrder.subTotal();
    }

    @Override
    public void draw(Graphics2D g) {
        // warna meja: hijau jika sudah dilayani, oranye jika masih menunggu
        if (served) {
            g.setColor(new Color(0x27, 0xAE, 0x60));
        } else {
            g.setColor(new Color(0xE6, 0x7E, 0x22));
        }
        g.fillRoundRect(x, y, width, height, 12, 12);

        // pelanggan (lingkaran kecil di tengah meja)
        g.setColor(new Color(0xF4, 0xD0, 0x3F));
        g.fillOval(centerX() - 12, y + 10, 24, 24);

        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.drawString("Meja " + tableNumber, x + 8, y + 52);

        // gelembung pesanan
        if (!served) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 9));
            g.drawString(requestedMenu.getName(), x - 4, y - 6);
        } else {
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString("OK", centerX() - 8, y + 66);
        }
    }
}
