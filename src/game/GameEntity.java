package game;

import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Base class untuk semua objek di dunia game (pemain, meja, counter).
 * Menyimpan posisi & ukuran, serta menyediakan deteksi tabrakan sederhana.
 *
 * Ini contoh ABSTRACTION + INHERITANCE: subclass wajib mengimplementasikan draw().
 */
public abstract class GameEntity {

    protected int x;
    protected int y;
    protected int width;
    protected int height;

    public GameEntity(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /** Setiap entitas menggambar dirinya sendiri (polymorphism). */
    public abstract void draw(Graphics2D g);

    /** Kotak batas untuk deteksi tabrakan. */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    /** True jika entitas ini bertabrakan / bersentuhan dengan entitas lain. */
    public boolean intersects(GameEntity other) {
        return getBounds().intersects(other.getBounds());
    }

    /** Jarak antar titik tengah dua entitas. */
    public double distanceTo(GameEntity other) {
        int dx = centerX() - other.centerX();
        int dy = centerY() - other.centerY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public int centerX() {
        return x + width / 2;
    }

    public int centerY() {
        return y + height / 2;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
