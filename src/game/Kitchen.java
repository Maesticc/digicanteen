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
    private double animTime; // untuk animasi uap

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
        animTime += deltaSeconds;
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
        g.setColor(new Color(0x2C, 0x3E, 0x50));
        g.fillRoundRect(x, y, width, height, 14, 14);
        g.setColor(new Color(0x1B, 0x26, 0x31));
        g.drawRoundRect(x, y, width, height, 14, 14);

        // header
        g.setColor(new Color(0xE6, 0x7E, 0x22));
        g.fillRoundRect(x, y, width, 30, 14, 14);
        g.fillRect(x, y + 15, width, 15);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("DAPUR   " + cooking.size() + "/" + capacity + " kompor", x + 12, y + 20);

        // daftar masakan yang sedang berjalan
        int rowY = y + 46;
        for (CookTask t : cooking) {
            drawCookSlot(g, rowY, t);
            rowY += 42;
        }

        // slot kompor kosong (kapasitas sisa)
        for (int i = cooking.size(); i < capacity; i++) {
            g.setColor(new Color(255, 255, 255, 30));
            g.fillRoundRect(x + 12, rowY, width - 24, 34, 8, 8);
            g.setColor(new Color(0xBD, 0xC3, 0xC7));
            g.setFont(new Font("SansSerif", Font.ITALIC, 12));
            g.drawString("kompor kosong", x + 24, rowY + 22);
            rowY += 42;
        }
    }

    private void drawCookSlot(Graphics2D g, int rowY, CookTask t) {
        // panel slot
        g.setColor(new Color(255, 255, 255, 22));
        g.fillRoundRect(x + 12, rowY, width - 24, 34, 8, 8);

        // panci
        int panX = x + 22;
        int panY = rowY + 8;
        g.setColor(new Color(0x17, 0x20, 0x2A));
        g.fillRoundRect(panX, panY, 26, 18, 6, 6);
        g.setColor(new Color(0x0B, 0x0F, 0x14));
        g.fillRect(panX - 4, panY + 4, 4, 4);
        g.fillRect(panX + 26, panY + 4, 4, 4);

        // uap mengepul (3 gumpalan naik-turun)
        for (int s = 0; s < 3; s++) {
            double phase = animTime * 2 + s * 1.1;
            int sy = panY - 4 - (int) ((Math.sin(phase) + 1) * 6) - s * 4;
            int alpha = (int) (90 - Math.abs(Math.sin(phase)) * 50);
            g.setColor(new Color(255, 255, 255, Math.max(20, alpha)));
            g.fillOval(panX + 6 + s * 6, sy, 8, 8);
        }

        // nama menu
        g.setColor(new Color(0xEC, 0xF0, 0xF1));
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString(t.customer.getOrder().getName(), panX + 44, rowY + 14);

        // progress bar dengan kilau
        int barX = panX + 44;
        int barY = rowY + 20;
        int barW = width - (barX - x) - 24;
        g.setColor(new Color(0x12, 0x1A, 0x22));
        g.fillRoundRect(barX, barY, barW, 10, 5, 5);
        int fill = (int) (barW * t.progress());
        g.setColor(new Color(0xF3, 0x9C, 0x12));
        g.fillRoundRect(barX, barY, fill, 10, 5, 5);
        // kilau bergerak
        if (fill > 6) {
            int glow = barX + (int) ((Math.sin(animTime * 4) * 0.5 + 0.5) * fill);
            g.setColor(new Color(255, 255, 255, 120));
            g.fillRect(Math.min(glow, barX + fill - 3), barY, 3, 10);
        }

        // persen
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.drawString((int) (t.progress() * 100) + "%", barX + barW + 2, barY + 9);
    }
}
