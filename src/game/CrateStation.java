package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * CrateStation = peti bahan. Menyediakan satu jenis bahan tanpa batas.
 * Chef bertangan kosong dapat mengambil bahan mentah dari sini.
 */
public class CrateStation extends Station {

    private final IngredientType type;

    public CrateStation(int x, int y, int width, int height, IngredientType type) {
        super(x, y, width, height, type.getLabel());
        this.type = type;
    }

    public IngredientType getType() {
        return type;
    }

    @Override
    public void interact(Chef chef, GamePanel game) {
        if (!chef.handsFree()) {
            game.flash("Tangan penuh - taruh dulu barangnya");
            return;
        }
        chef.setCarrying(new Ingredient(type, Ingredient.Prep.RAW));
        game.flash("Ambil " + type.getLabel() + " mentah");
    }

    @Override
    public void draw(Graphics2D g) {
        drawBase(g, new Color(0x8D, 0x6E, 0x3F), new Color(0xA9, 0x87, 0x50));

        // tumpukan bahan di atas peti
        int cx = centerX();
        int cy = y + height / 2 + 2;
        g.setColor(type.getColor());
        g.fillOval(cx - 12, cy - 10, 24, 20);
        g.setColor(type.getColor().darker());
        g.drawOval(cx - 12, cy - 10, 24, 20);

        // label bahan
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.setColor(new Color(0xFF, 0xF6, 0xE0));
        int tw = g.getFontMetrics().stringWidth(type.getLabel());
        g.drawString(type.getLabel(), cx - tw / 2, y + height - 6);
    }
}
