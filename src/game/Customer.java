package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import model.Menu;
import model.Pembeli;

/**
 * Customer = pelanggan yang datang ke kantin dan mengantre.
 * Membungkus objek model.Pembeli dan memesan sebuah model.Menu.
 *
 * Punya "patience" (kesabaran) yang terus menurun. Jika habis, pelanggan pergi.
 * State pesanan: WAITING -> ORDER_TAKEN -> SERVED.
 */
public class Customer extends GameEntity {

    public enum State {
        WAITING,       // baru datang, pesanan belum diambil
        ORDER_TAKEN,   // pesanan sudah diambil, menunggu makanan
        SERVED,        // sudah dilayani (selesai)
        LEFT_ANGRY     // kabur karena kehabisan kesabaran
    }

    private final Pembeli pembeli;   // pelanggan (model OOP existing)
    private final Menu order;        // menu yang dipesan (model OOP existing)

    private double patience;         // 0 - 100
    private final double patienceDrain; // pengurangan per detik
    private State state;

    public Customer(int x, int y, Pembeli pembeli, Menu order, double patienceDrain) {
        super(x, y, 48, 60);
        this.pembeli = pembeli;
        this.order = order;
        this.patience = 100;
        this.patienceDrain = patienceDrain;
        this.state = State.WAITING;
    }

    /** Kurangi kesabaran berdasarkan waktu (deltaSeconds). */
    public void update(double deltaSeconds) {
        if (state == State.SERVED || state == State.LEFT_ANGRY) {
            return;
        }
        patience -= patienceDrain * deltaSeconds;
        if (patience <= 0) {
            patience = 0;
            state = State.LEFT_ANGRY;
        }
    }

    public boolean isActive() {
        return state == State.WAITING || state == State.ORDER_TAKEN;
    }

    public void takeOrder() {
        if (state == State.WAITING) {
            state = State.ORDER_TAKEN;
        }
    }

    public void serve() {
        if (state == State.ORDER_TAKEN) {
            state = State.SERVED;
        }
    }

    // --- getters ---

    public Pembeli getPembeli() {
        return pembeli;
    }

    public Menu getOrder() {
        return order;
    }

    public State getState() {
        return state;
    }

    public double getPatience() {
        return patience;
    }

    /** Bonus tip lebih besar jika dilayani saat kesabaran masih tinggi. */
    public double tipMultiplier() {
        if (patience >= 70) return 1.5;
        if (patience >= 40) return 1.2;
        return 1.0;
    }

    // --- drawing ---

    @Override
    public void draw(Graphics2D g) {
        // warna badan berdasarkan state
        Color body;
        switch (state) {
            case ORDER_TAKEN: body = new Color(0x5D, 0xAD, 0xE2); break; // biru: sedang dimasak
            case SERVED:      body = new Color(0x82, 0xE0, 0xAA); break; // hijau: selesai
            case LEFT_ANGRY:  body = new Color(0x99, 0x99, 0x99); break; // abu: kabur
            default:          body = new Color(0xF5, 0xB0, 0x41);        // oranye: menunggu
        }

        // badan
        g.setColor(body);
        g.fillRoundRect(x, y + 14, width, height - 14, 10, 10);
        // kepala
        g.setColor(new Color(0xF5, 0xCB, 0xA7));
        g.fillOval(x + 8, y, width - 16, 30);

        // bar kesabaran di atas kepala
        int barW = width;
        int filled = (int) (barW * (patience / 100.0));
        g.setColor(new Color(0x33, 0x33, 0x33));
        g.fillRect(x, y - 12, barW, 6);
        g.setColor(patienceColor());
        g.fillRect(x, y - 12, filled, 6);

        // gelembung pesanan
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        if (state == State.WAITING) {
            g.setColor(Color.WHITE);
            g.fillRoundRect(x - 6, y - 34, width + 30, 18, 6, 6);
            g.setColor(Color.BLACK);
            g.drawString(order.getName(), x - 2, y - 21);
        } else if (state == State.ORDER_TAKEN) {
            g.setColor(new Color(0x21, 0x2F, 0x3D));
            g.drawString("memasak...", x - 2, y - 18);
        } else if (state == State.SERVED) {
            g.setColor(new Color(0x14, 0x6C, 0x43));
            g.drawString("Terima kasih!", x - 4, y - 18);
        }
    }

    private Color patienceColor() {
        if (patience >= 60) return new Color(0x2E, 0xCC, 0x71);
        if (patience >= 30) return new Color(0xF1, 0xC4, 0x0F);
        return new Color(0xE7, 0x4C, 0x3C);
    }
}
