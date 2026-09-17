package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import model.Menu;
import model.Pembeli;

/**
 * Customer = pelanggan yang datang ke kantin dan mengantre (kolom kiri).
 * Membungkus objek model.Pembeli dan memesan sebuah model.Menu.
 *
 * Gaya visual meniru Restaurant City: karakter dengan rambut warna-warni,
 * kartu tiket pesanan di sampingnya, plus animasi slide-in (sumbu Y),
 * idle bobbing, hover, dan bar kesabaran.
 */
public class Customer extends GameEntity {

    public enum State {
        WAITING, ORDER_TAKEN, SERVED, LEFT_ANGRY
    }

    private static final Color[] HAIR = {
            new Color(0x8E, 0x44, 0xAD), new Color(0x2E, 0x86, 0xC1),
            new Color(0x28, 0xB4, 0x63), new Color(0xE6, 0x7E, 0x22),
            new Color(0xC0, 0x39, 0x2B), new Color(0x34, 0x49, 0x5E)
    };
    private static final Color[] SHIRT = {
            new Color(0xF1, 0xC4, 0x0F), new Color(0xE7, 0x4C, 0x3C),
            new Color(0x34, 0x98, 0xDB), new Color(0x1A, 0xBC, 0x9C),
            new Color(0x9B, 0x59, 0xB6)
    };

    private final Pembeli pembeli;
    private final Menu order;

    private double patience;
    private final double patienceDrain;
    private State state;

    // animasi
    private int targetY;
    private double animTime;
    private boolean hovered;
    private double servedPop;
    private final int bobPhase;

    // gaya karakter
    private final Color hairColor;
    private final Color shirtColor;

    public Customer(int x, int spawnY, int targetY, Pembeli pembeli, Menu order, double patienceDrain) {
        super(x, spawnY, 54, 66);
        this.targetY = targetY;
        this.pembeli = pembeli;
        this.order = order;
        this.patience = 100;
        this.patienceDrain = patienceDrain;
        this.state = State.WAITING;
        this.bobPhase = (int) (Math.random() * 100);
        this.hairColor = HAIR[(int) (Math.random() * HAIR.length)];
        this.shirtColor = SHIRT[(int) (Math.random() * SHIRT.length)];
    }

    public void update(double dt) {
        animTime += dt;

        // slide-in menuju targetY (easing)
        if (y != targetY) {
            double diff = targetY - y;
            y += (int) Math.signum(diff) * Math.max(1, Math.abs(diff) * 0.15);
            if (Math.abs(targetY - y) <= 2) y = targetY;
        }

        if (servedPop > 0) servedPop -= dt * 3;

        if (state == State.SERVED || state == State.LEFT_ANGRY) return;

        patience -= patienceDrain * dt;
        if (patience <= 0) {
            patience = 0;
            state = State.LEFT_ANGRY;
        }
    }

    private int bobX() {
        if (state != State.WAITING) return x;
        return x + (int) (Math.sin(animTime * 3 + bobPhase) * 2);
    }

    public void setTargetY(int ty) { this.targetY = ty; }
    public void setHovered(boolean h) { this.hovered = h; }

    public boolean isActive() {
        return state == State.WAITING || state == State.ORDER_TAKEN;
    }

    public void takeOrder() { if (state == State.WAITING) state = State.ORDER_TAKEN; }

    public void serve() {
        if (state == State.ORDER_TAKEN) {
            state = State.SERVED;
            servedPop = 1.0;
        }
    }

    public Pembeli getPembeli() { return pembeli; }
    public Menu getOrder() { return order; }
    public State getState() { return state; }
    public double getPatience() { return patience; }

    public double tipMultiplier() {
        if (patience >= 70) return 1.5;
        if (patience >= 40) return 1.2;
        return 1.0;
    }

    @Override
    public Rectangle getBounds() {
        // mencakup karakter + kartu tiket di kanannya
        return new Rectangle(bobX() - 4, y - 16, width + 40, height + 20);
    }

    // ------------------------------------------------------------------
    // Drawing
    // ------------------------------------------------------------------

    @Override
    public void draw(Graphics2D g) {
        int dx = bobX();

        double scale = 1.0 + Math.max(0, servedPop) * 0.12;
        int headSize = (int) (26 * scale);

        // bayangan
        g.setColor(new Color(0, 0, 0, 50));
        g.fillOval(dx + 6, y + height - 8, width - 20, 10);

        // hover glow
        if (hovered && isActive()) {
            g.setColor(new Color(255, 255, 100, 90));
            g.fillRoundRect(dx - 6, y - 16, width + 44, height + 22, 16, 16);
        }

        // --- karakter ---
        int cx = dx + 14;

        // badan (baju)
        g.setColor(shirtColor);
        g.fillRoundRect(cx - 2, y + 24, 28, 34, 12, 12);
        // lengan
        g.setColor(shirtColor.darker());
        g.fillRoundRect(cx - 8, y + 28, 8, 22, 6, 6);
        g.fillRoundRect(cx + 26, y + 28, 8, 22, 6, 6);

        // kepala
        g.setColor(new Color(0xF5, 0xCB, 0xA7));
        g.fillOval(cx, y + 4, headSize, headSize);
        // rambut
        g.setColor(hairColor);
        g.fillArc(cx - 2, y, headSize + 4, headSize, 20, 140);
        g.fillRoundRect(cx - 2, y + 4, headSize + 4, 8, 6, 6);

        // wajah
        drawFace(g, cx, y + 4, headSize);

        // bar kesabaran
        drawPatienceBar(g, dx);

        // kartu tiket pesanan di kanan
        drawTicket(g, dx + width - 2, y + 6);
    }

    private void drawFace(Graphics2D g, int hx, int hy, int hs) {
        g.setColor(new Color(0x2C, 0x2C, 0x2C));
        int eyeY = hy + hs / 2;
        int eL = hx + hs / 3;
        int eR = hx + 2 * hs / 3;

        if (state == State.LEFT_ANGRY) {
            g.setStroke(new BasicStroke(2f));
            g.drawLine(eL - 2, eyeY - 2, eL + 3, eyeY + 2);
            g.drawLine(eR + 3, eyeY - 2, eR - 2, eyeY + 2);
            g.drawArc(hx + hs / 3, hy + hs - 6, hs / 3, 6, 0, 180);
        } else {
            g.fillOval(eL, eyeY, 3, 3);
            g.fillOval(eR, eyeY, 3, 3);
            if (state == State.SERVED || patience >= 50) {
                g.drawArc(hx + hs / 3, hy + hs - 10, hs / 3, 8, 180, 180);
            } else {
                g.drawLine(hx + hs / 3, hy + hs - 4, hx + 2 * hs / 3, hy + hs - 4);
            }
        }
    }

    private void drawPatienceBar(Graphics2D g, int dx) {
        if (state == State.SERVED || state == State.LEFT_ANGRY) return;
        int barW = width - 10;
        int filled = (int) (barW * (patience / 100.0));
        boolean critical = patience < 30;
        boolean blink = ((int) (animTime * 6)) % 2 == 0;

        g.setColor(new Color(0x22, 0x22, 0x22));
        g.fillRoundRect(dx + 6, y - 8, barW, 6, 4, 4);
        Color c = patienceColor();
        if (critical && blink) c = c.brighter();
        g.setColor(c);
        g.fillRoundRect(dx + 6, y - 8, filled, 6, 4, 4);
    }

    /** Kartu tiket pesanan vertikal ala Restaurant City (kertas krem). */
    private void drawTicket(Graphics2D g, int tx, int ty) {
        int tw = 30, th = 46;

        // kertas
        Color paper = (state == State.SERVED) ? new Color(0xD5, 0xF5, 0xE3)
                : new Color(0xFD, 0xF3, 0xD8);
        g.setColor(new Color(0, 0, 0, 40));
        g.fillRoundRect(tx + 2, ty + 2, tw, th, 8, 8);
        g.setColor(paper);
        g.fillRoundRect(tx, ty, tw, th, 8, 8);
        g.setColor(new Color(0xE0, 0xC8, 0x9A));
        g.drawRoundRect(tx, ty, tw, th, 8, 8);

        // ikon makanan (piring + isi warna berdasarkan menu)
        int px = tx + tw / 2;
        int py = ty + 16;
        g.setColor(Color.WHITE);
        g.fillOval(px - 11, py - 8, 22, 16);
        g.setColor(foodColor());
        g.fillOval(px - 7, py - 5, 14, 10);

        // status di bawah ikon
        g.setFont(new Font("SansSerif", Font.BOLD, 9));
        if (state == State.WAITING) {
            g.setColor(new Color(0x7F, 0x6A, 0x33));
            g.drawString("pesan", tx + 3, ty + th - 6);
        } else if (state == State.ORDER_TAKEN) {
            g.setColor(new Color(0x2E, 0x86, 0xC1));
            g.drawString("masak", tx + 3, ty + th - 6);
        } else if (state == State.SERVED) {
            g.setColor(new Color(0x14, 0x6C, 0x43));
            g.drawString("done", tx + 5, ty + th - 6);
        }
    }

    private Color foodColor() {
        int h = Math.abs(order.getName().hashCode());
        Color[] foods = {
                new Color(0xE6, 0x7E, 0x22), new Color(0xE7, 0x4C, 0x3C),
                new Color(0xF1, 0xC4, 0x0F), new Color(0x8E, 0x44, 0xAD)
        };
        return foods[h % foods.length];
    }

    private Color patienceColor() {
        if (patience >= 60) return new Color(0x2E, 0xCC, 0x71);
        if (patience >= 30) return new Color(0xF1, 0xC4, 0x0F);
        return new Color(0xE7, 0x4C, 0x3C);
    }
}
