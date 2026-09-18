package game;

import java.awt.Graphics2D;

/**
 * Item = benda yang bisa dibawa chef (bahan mentah/potong, atau masakan jadi).
 *
 * Contoh ABSTRACTION: subclass wajib menyediakan nama & cara menggambar dirinya.
 */
public abstract class Item {

    /** Nama singkat untuk ditampilkan di layar. */
    public abstract String label();

    /** Gambar item dengan titik tengah (cx, cy) dan ukuran kira-kira size px. */
    public abstract void draw(Graphics2D g, int cx, int cy, int size);
}
