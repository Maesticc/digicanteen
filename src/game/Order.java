package game;

import model.OrderDetail;
import model.Pembeli;

/**
 * Order = tiket pesanan yang muncul di papan pesanan. Setiap tiket dimiliki
 * seorang Pembeli (class model existing) dan punya batas waktu.
 *
 * Saat pesanan selesai, dicatat sebagai model.OrderDetail - sama seperti
 * sistem kantin aslinya.
 */
public class Order {

    private final Recipe recipe;
    private final Pembeli customer;   // model OOP existing
    private final double totalTime;

    private double timeLeft;
    private boolean finished;
    private OrderDetail detail;

    public Order(Recipe recipe, Pembeli customer, double totalTime) {
        this.recipe = recipe;
        this.customer = customer;
        this.totalTime = totalTime;
        this.timeLeft = totalTime;
    }

    public void update(double dt) {
        if (finished) {
            return;
        }
        timeLeft -= dt;
        if (timeLeft < 0) {
            timeLeft = 0;
        }
    }

    public boolean isExpired() {
        return !finished && timeLeft <= 0;
    }

    public boolean isFinished() {
        return finished;
    }

    /** Tandai pesanan selesai & catat sebagai OrderDetail. */
    public OrderDetail complete() {
        finished = true;
        detail = new OrderDetail(recipe.getMenu(), 1);
        return detail;
    }

    public OrderDetail getDetail() {
        return detail;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public Pembeli getCustomer() {
        return customer;
    }

    public double getTimeLeft() {
        return timeLeft;
    }

    public double getTotalTime() {
        return totalTime;
    }

    /** Sisa waktu dalam rasio 0..1 (untuk bar di tiket). */
    public double timeRatio() {
        return Math.max(0, Math.min(1, timeLeft / totalTime));
    }

    /** Bonus tip: makin cepat diantar, makin besar. */
    public double tipMultiplier() {
        double r = timeRatio();
        if (r >= 0.6) return 1.5;
        if (r >= 0.3) return 1.2;
        return 1.0;
    }
}
