package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Kitchen = dapur tempat pesanan dimasak. Setiap pesanan yang sedang dimasak
 * disimpan sebagai CookTask dengan sisa waktu. Ketika waktu habis, makanan
 * "matang" dan pelanggan siap dilayani.
 *
 * Kapasitas masak terbatas (jumlah kompor) supaya ada elemen strategi.
 */
public class Kitchen {

    /** Satu proses memasak untuk satu Customer. */
    public static class CookTask {
        final Customer customer;
        double remaining; // detik tersisa
        final double total;

        CookTask(Customer customer, double cookTime) {
            this.customer = customer;
            this.remaining = cookTime;
            this.total = cookTime;
        }

        public Customer getCustomer() {
            return customer;
        }

        public double progress() {
            return 1.0 - (remaining / total);
        }
    }

    private final int x, y, width, height;
    private final int capacity; // jumlah kompor
    private final double cookTime;
    private final ArrayList<CookTask> cooking = new ArrayList<>();

    public Kitchen(int x, int y, int width, int height, int capacity, double cookTime) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.capacity = capacity;
        this.cookTime = cookTime;
    }

    public boolean isFull() {
        return cooking.size() >= capacity;
    }

    /** Mulai memasak pesanan seorang customer. */
    public boolean startCooking(Customer c) {
        if (isFull()) {
            return false;
        }
        cooking.add(new CookTask(c, cookTime));
        return true;
    }

    /** True jika customer ini sedang dimasak. */
    public boolean isCooking(Customer c) {
        for (CookTask t : cooking) {
            if (t.customer == c) return true;
        }
        return false;
    }

    /**
     * Update semua proses masak. Customer yang selesai dimasak dikembalikan
     * dalam list agar GamePanel bisa menandainya "siap disajikan".
     */
    public ArrayList<Customer> update(double deltaSeconds) {
        ArrayList<Customer> done = new ArrayList<>();
        Iterator<CookTask> it = cooking.iterator();
        while (it.hasNext()) {
            CookTask t = it.next();
            // kalau pelanggan sudah kabur, batalkan masakannya
            if (t.customer.getState() == Customer.State.LEFT_ANGRY) {
                it.remove();
                continue;
            }
            t.remaining -= deltaSeconds;
            if (t.remaining <= 0) {
                done.add(t.customer);
                it.remove();
            }
        }
        return done;
    }

    public void draw(Graphics2D g) {
        // area dapur
        g.setColor(new Color(0x34, 0x49, 0x5E));
        g.fillRoundRect(x, y, width, height, 12, 12);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("DAPUR  (" + cooking.size() + "/" + capacity + " kompor)", x + 12, y + 22);

        // daftar masakan yang sedang berjalan
        int rowY = y + 40;
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        for (CookTask t : cooking) {
            g.setColor(new Color(0xEC, 0xF0, 0xF1));
            g.drawString(t.customer.getOrder().getName(), x + 12, rowY + 12);

            // progress bar masak
            int barX = x + 150;
            int barW = width - 170;
            g.setColor(new Color(0x1B, 0x26, 0x31));
            g.fillRect(barX, rowY, barW, 12);
            g.setColor(new Color(0xE6, 0x7E, 0x22));
            g.fillRect(barX, rowY, (int) (barW * t.progress()), 12);

            rowY += 22;
        }
    }
}
