package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Ingredient = bahan makanan yang dibawa chef.
 * Punya dua tahap: RAW (mentah, perlu dipotong) dan CHOPPED (siap dimasak).
 *
 * Contoh INHERITANCE: Ingredient adalah turunan dari Item.
 */
public class Ingredient extends Item {

    public enum Prep {
        RAW, CHOPPED
    }

    private final IngredientType type;
    private Prep prep;

    public Ingredient(IngredientType type, Prep prep) {
        this.type = type;
        this.prep = prep;
    }

    public IngredientType getType() {
        return type;
    }

    public Prep getPrep() {
        return prep;
    }

    public void setPrep(Prep prep) {
        this.prep = prep;
    }

    @Override
    public String label() {
        return type.getLabel() + (prep == Prep.CHOPPED ? " (potong)" : "");
    }

    @Override
    public void draw(Graphics2D g, int cx, int cy, int size) {
        Color c = type.getColor();
        if (prep == Prep.RAW) {
            // bahan utuh: satu bulatan
            g.setColor(c);
            g.fillOval(cx - size / 2, cy - size / 2, size, size);
            g.setColor(c.darker());
            g.setStroke(new BasicStroke(1.5f));
            g.drawOval(cx - size / 2, cy - size / 2, size, size);
        } else {
            // bahan sudah dipotong: beberapa kubus kecil
            int s = Math.max(3, size / 3);
            g.setColor(c);
            g.fillRect(cx - s - 1, cy - s - 1, s, s);
            g.fillRect(cx + 1, cy - s - 1, s, s);
            g.fillRect(cx - s - 1, cy + 1, s, s);
            g.fillRect(cx + 1, cy + 1, s, s);
            g.setColor(c.darker());
            g.drawRect(cx - s - 1, cy - s - 1, s, s);
            g.drawRect(cx + 1, cy - s - 1, s, s);
            g.drawRect(cx - s - 1, cy + 1, s, s);
            g.drawRect(cx + 1, cy + 1, s, s);
        }
    }
}
