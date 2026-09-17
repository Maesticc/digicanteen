package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import model.Menu;
import model.Pembeli;

/**
 * Customer = pelanggan yang datang ke kantin dan mengantre.
 * Membungkus objek model.Pembeli dan memesan sebuah model.Menu.
 *
 * Versi animasi: slide-in saat datang, idle bobbing (badan naik-turun halus),
 * efek hover, patience bar beranimasi + berkedip saat kritis, dan ekspresi
 * wajah yang berubah sesuai state.
 */
public class Customer extends GameEntity {

    public enum State {
        WAITING, ORDER_TAKEN, SERVED, LEFT_ANGRY
    }

    private final Pembeli pembeli;
    private final Menu order;

    private double patience;
    private final double patienceDrain;
    private State state;

    // --- animasi ---
    private int targetX;          // posisi x tujuan (slide-in)
    private double animTime;      // waktu hidup untuk bobbing & efek
    private boolean hovered;      // kursor sedang di atasnya
    private double servedPop;     // efek "pop" saat baru dilayani
    private final int bobPhase;   // offset acak biar tidak seragam

    public Customer(int spawnX, int y, int targetX, Pembeli pembeli, Menu order, double patienceDrain) {
        super(spawnX, y, 48, 62);
        this.targetX = targetX;
        this.pembeli = pembeli;
        this.order = order;
        this.patience = 100;
        this.patienceDrain = patienceDrain;
        this.state = State.WAITING;
        this.bobPhase = (int) (Math.random() * 100);
    }

    public void update(double dt) {
        animTime += dt;

        // slide-in menuju targetX (easing)
        if (x != targetX) {
            double diff = targetX - x;
            x += (int) Math.signum(diff) * Math.max(1, Math.abs(diff) * 0.15);
            if (Math.abs(targetX - x) <= 2) x = targetX;
        }

        if (servedPop > 0) servedPop -= dt * 3;

        if (state == State.SERVED || state == State.LEFT_ANGRY) {
            return;
        }
        patience -= patienceDrain * dt;
        if (patience <= 0) {
            patience = 0;
            state = State.LEFT_ANGRY;
        }
    }

    /** Titik y setelah bobbing (badan naik-turun halus). */
    private int bobY() {
        if (state != State.WAITING) return y;
        return y + (int) (Math.sin(animTime * 3 + bobPhase) * 3);
    }

    public void setTargetX(int tx) {
        this.targetX = tx;
    }

    public void setHovered(boolean h) {
        this.hovered = h;
    }

    public boolean isActive() {
        return state == State.WAITING || state == State.ORDER_TAKEN;
    }

    public void takeOrder() {
        if (state == State.WAITING) state = State.ORDER_TAKEN;
    }

    public void serve() {
        if (state == State.ORDER_TAKEN) {
            state = State.SERVED;
            servedPop = 1.0;
        }
    }

    // --- getters ---
    public Pembeli getPembeli() { return pembeli; }
    public Menu getOrder() { return order; }
    public State getState() { return state; }
    public double getPatience() { return patience; }

    public double tipMultiplier() {
        if (patience >= 70) return 1.5;
        if (patience >= 40) return 1.2;
        return 1.0;
    }

    /** Bounds yang mengikuti posisi bobbing (untuk hit-test klik). */
    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, bobY() - 14, width, height + 20);
    }

    // --- drawing ---
    @Override
    public void draw(Graphics2D g) {
        int drawY = bobY();

        // efek pop saat baru dilayani (membesar sedikit)
        double scale = 1.0 + Math.max(0, servedPop) * 0.12;
        int w = (int) (width * scale);
        int h = (int) ((height - 14) * scale);
        int bx = x - (w - width) / 2;

        // bayangan
        g.setColor(new Color(0, 0, 0, 40));
        g.fillOval(x + 2, drawY + height - 6, width - 4, 10);

        // highlight hover
        if (hovered && isActive()) {
            g.setColor(new Color(255, 255, 255, 120));
            g.setStroke(new BasicStroke(3f));
            g.drawRoundRect(x - 6, drawY - 18, width + 12, height + 24, 14, 14);
        }

        // badan
        g.setColor(bodyColor());
        g.fillRoundRect(bx, drawY + 14, w, h, 12, 12);

        // kepala
        g.setColor(new Color(0xF5, 0xCB, 0xA7));
        g.fillOval(x + 8, drawY, width - 16, 30);

        // wajah (ekspresi per state)
        drawFace(g, x + 8, drawY, width - 16, 30);

        // bar kesabaran beranimasi
        drawPatienceBar(g, drawY);

        // gelembung pesanan
        drawBubble(g, drawY);
    }

    private Color bodyColor() {
        switch (state) {
            case ORDER_TAKEN: return new Color(0x5D, 0xAD, 0xE2);
            case SERVED:      return new Color(0x82, 0xE0, 0xAA);
            case LEFT_ANGRY:  return new Color(0x99, 0x99, 0x99);
            default:          return new Color(0xF5, 0xB0, 0x41);
        }
    }

    private void drawFace(Graphics2D g, int hx, int hy, int hw, int hh) {
        g.setColor(new Color(0x2C, 0x2C, 0x2C));
        int eyeY = hy + hh / 2 - 2;
        int eL = hx + hw / 3 - 2;
        int eR = hx + 2 * hw / 3 - 2;

        if (state == State.LEFT_ANGRY) {
            // mata marah (garis miring) + mulut cemberut
            g.drawLine(eL - 2, eyeY - 2, eL + 4, eyeY + 2);
            g.drawLine(eR + 4, eyeY - 2, eR - 2, eyeY + 2);
            g.drawArc(hx + hw / 3, hy + hh - 8, hw / 3, 8, 0, 180);
        } else {
            g.fillOval(eL, eyeY, 4, 4);
            g.fillOval(eR, eyeY, 4, 4);
            // mulut senyum jika sabar tinggi / sudah dilayani, datar jika mulai gelisah
            if (state == State.SERVED || patience >= 50) {
                g.drawArc(hx + hw / 3, hy + hh - 12, hw / 3, 8, 180, 180);
            } else {
                g.drawLine(hx + hw / 3, hy + hh - 6, hx + 2 * hw / 3, hy + hh - 6);
            }
        }
    }

    private void drawPatienceBar(Graphics2D g, int drawY) {
        if (state == State.SERVED || state == State.LEFT_ANGRY) return;

        int barW = width;
        int filled = (int) (barW * (patience / 100.0));

        // kedip saat kritis
        boolean critical = patience < 30;
        boolean blink = ((int) (animTime * 6)) % 2 == 0;

        g.setColor(new Color(0x33, 0x33, 0x33));
        g.fillRoundRect(x, drawY - 14, barW, 7, 4, 4);

        Color c = patienceColor();
        if (critical && blink) c = c.brighter();
        g.setColor(c);
        g.fillRoundRect(x, drawY - 14, filled, 7, 4, 4);
    }

    private void drawBubble(Graphics2D g, int drawY) {
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        if (state == State.WAITING) {
            int tw = g.getFontMetrics().stringWidth(order.getName());
            g.setColor(Color.WHITE);
            g.fillRoundRect(x - 6, drawY - 38, tw + 16, 20, 8, 8);
            g.setColor(new Color(0, 0, 0, 40));
            g.drawRoundRect(x - 6, drawY - 38, tw + 16, 20, 8, 8);
            g.setColor(Color.BLACK);
            g.drawString(order.getName(), x + 2, drawY - 24);
        } else if (state == State.ORDER_TAKEN) {
            g.setColor(new Color(0x21, 0x2F, 0x3D));
            g.drawString("memasak...", x - 2, drawY - 22);
        } else if (state == State.SERVED) {
            g.setColor(new Color(0x14, 0x6C, 0x43));
            g.drawString("Terima kasih!", x - 4, drawY - 22);
        }
    }

    private Color patienceColor() {
        if (patience >= 60) return new Color(0x2E, 0xCC, 0x71);
        if (patience >= 30) return new Color(0xF1, 0xC4, 0x0F);
        return new Color(0xE7, 0x4C, 0x3C);
    }
}
