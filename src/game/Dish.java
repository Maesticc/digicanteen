package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Dish = masakan jadi hasil memasak bahan di panci, siap diantar ke
 * jendela penyajian.
 *
 * Contoh INHERITANCE + POLYMORPHISM: Dish dan Ingredient sama-sama Item,
 * tetapi menggambar dirinya dengan cara yang berbeda.
 */
public class Dish extends Item {

    private final Recipe recipe;

    public Dish(Recipe recipe) {
        this.recipe = recipe;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    @Override
    public String label() {
        return recipe.getName();
    }

    @Override
    public void draw(Graphics2D g, int cx, int cy, int size) {
        // piring
        g.setColor(new Color(0, 0, 0, 40));
        g.fillOval(cx - size / 2, cy - size / 3 + 2, size, (size * 2) / 3);
        g.setColor(Color.WHITE);
        g.fillOval(cx - size / 2, cy - size / 3, size, (size * 2) / 3);
        g.setColor(new Color(0xD5, 0xD8, 0xDC));
        g.drawOval(cx - size / 2, cy - size / 3, size, (size * 2) / 3);

        // isi masakan: warna bahan-bahannya
        List<IngredientType> need = recipe.getNeed();
        int n = Math.max(1, need.size());
        int fs = Math.max(4, size / 4);
        int startX = cx - (n * fs) / 2;
        for (int i = 0; i < n; i++) {
            g.setColor(need.get(i).getColor());
            g.fillOval(startX + i * fs, cy - fs / 2, fs, fs);
        }
    }
}
