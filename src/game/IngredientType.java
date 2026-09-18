package game;

import java.awt.Color;

/**
 * Jenis bahan makanan yang tersedia di dapur.
 * Setiap bahan punya nama tampilan dan warna ikonnya.
 */
public enum IngredientType {

    NASI("Nasi", new Color(0xF7, 0xF1, 0xE3)),
    AYAM("Ayam", new Color(0xE8, 0x8A, 0x3C)),
    SAYUR("Sayur", new Color(0x4C, 0xAF, 0x50)),
    MIE("Mie", new Color(0xF1, 0xC4, 0x0F)),
    TELUR("Telur", new Color(0xFD, 0xE3, 0x89));

    private final String label;
    private final Color color;

    IngredientType(String label, Color color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public Color getColor() {
        return color;
    }
}
