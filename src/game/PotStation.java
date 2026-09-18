package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * PotStation = panci di atas kompor. Chef memasukkan bahan yang SUDAH DIPOTONG,
 * lalu panci memasak sendiri. Kalau kombinasi bahannya cocok dengan sebuah
 * Recipe, hasilnya menjadi masakan jadi (Dish) yang siap diambil. Kalau tidak
 * cocok, masakan hangus dan panci harus dikosongkan.
 */
public class PotStation extends Station {

    public enum PotState {
        EMPTY, COOKING, READY, BURNT
    }

    private static final int MAX_ISI = 2;

    private final List<IngredientType> contents = new ArrayList<>();
    private final List<Recipe> recipes;
    private final double cookTime;

    private PotState state = PotState.EMPTY;
    private double timer;
    private double animTime;
    private Recipe result;

    public PotStation(int x, int y, int width, int height, List<Recipe> recipes, double cookTime) {
        super(x, y, width, height, "Panci");
        this.recipes = recipes;
        this.cookTime = cookTime;
    }

    public PotState getState() {
        return state;
    }

    @Override
    public void update(double dt) {
        animTime += dt;
        if (state == PotState.COOKING) {
            timer += dt;
            if (timer >= cookTime) {
                Recipe cocok = findMatch();
                if (cocok != null) {
                    result = cocok;
                    state = PotState.READY;
                } else {
                    state = PotState.BURNT;
                }
            }
        }
    }

    private Recipe findMatch() {
        for (Recipe r : recipes) {
            if (r.matches(contents)) {
                return r;
            }
        }
        return null;
    }

    private void reset() {
        contents.clear();
        state = PotState.EMPTY;
        timer = 0;
        result = null;
    }

    @Override
    public void interact(Chef chef, GamePanel game) {
        Item held = chef.getCarrying();

        // masakan siap -> ambil
        if (state == PotState.READY) {
            if (held != null) {
                game.flash("Kosongkan tangan untuk ambil masakan");
                return;
            }
            chef.setCarrying(new Dish(result));
            game.flash(result.getName() + " siap diantar!");
            reset();
            return;
        }

        // hangus -> bersihkan
        if (state == PotState.BURNT) {
            if (held == null) {
                reset();
                game.flash("Panci dibersihkan");
            } else {
                game.flash("Panci hangus - kosongkan tangan dulu");
            }
            return;
        }

        // memasukkan bahan
        if (held instanceof Ingredient) {
            Ingredient ing = (Ingredient) held;
            if (ing.getPrep() != Ingredient.Prep.CHOPPED) {
                game.flash("Potong dulu di talenan!");
                return;
            }
            if (contents.size() >= MAX_ISI) {
                game.flash("Panci sudah penuh");
                return;
            }
            contents.add(ing.getType());
            chef.setCarrying(null);
            if (state == PotState.EMPTY) {
                state = PotState.COOKING;
                timer = 0;
            }
            game.flash(ing.getType().getLabel() + " masuk panci");
            return;
        }

        if (held instanceof Dish) {
            game.flash("Antar masakan itu ke jendela saji");
            return;
        }

        game.flash("Panci butuh bahan yang sudah dipotong");
    }

    @Override
    public void draw(Graphics2D g) {
        drawBase(g, new Color(0x4A, 0x4A, 0x52), new Color(0x63, 0x63, 0x6E));

        int cx = centerX();
        int cy = y + height / 2 + 4;

        // tungku
        g.setColor(state == PotState.COOKING ? new Color(0xE7, 0x4C, 0x3C) : new Color(0x2C, 0x2C, 0x2C));
        g.fillOval(cx - 20, cy + 6, 40, 12);

        // panci
        g.setColor(new Color(0x1C, 0x24, 0x2B));
        g.fillRoundRect(cx - 20, cy - 10, 40, 22, 6, 6);
        g.setColor(new Color(0x0F, 0x14, 0x18));
        g.fillRect(cx - 26, cy - 5, 6, 4);
        g.fillRect(cx + 20, cy - 5, 6, 4);

        // isi panci
        for (int i = 0; i < contents.size(); i++) {
            g.setColor(contents.get(i).getColor());
            g.fillOval(cx - 12 + i * 14, cy - 6, 12, 10);
        }

        if (state == PotState.COOKING) {
            // uap
            for (int s = 0; s < 3; s++) {
                double phase = animTime * 2.2 + s * 1.1;
                int sy = cy - 16 - (int) ((Math.sin(phase) + 1) * 6) - s * 4;
                int alpha = (int) (95 - Math.abs(Math.sin(phase)) * 55);
                g.setColor(new Color(255, 255, 255, Math.max(25, alpha)));
                g.fillOval(cx - 8 + s * 7, sy, 8, 8);
            }
            // progress bar
            int barW = width - 16;
            g.setColor(new Color(0x18, 0x18, 0x18));
            g.fillRoundRect(x + 8, y + height - 15, barW, 7, 4, 4);
            g.setColor(new Color(0xF3, 0x9C, 0x12));
            g.fillRoundRect(x + 8, y + height - 15, (int) (barW * Math.min(1.0, timer / cookTime)), 7, 4, 4);

        } else if (state == PotState.READY) {
            // sorot berkedip + nama masakan
            boolean blink = ((int) (animTime * 4)) % 2 == 0;
            g.setColor(blink ? new Color(0x2E, 0xCC, 0x71) : Color.WHITE);
            g.drawRoundRect(x - 4, y - 4, width + 8, height + 8, 12, 12);
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            g.setColor(Color.WHITE);
            String s = "SIAP";
            int tw = g.getFontMetrics().stringWidth(s);
            g.drawString(s, cx - tw / 2, y + height - 5);

        } else if (state == PotState.BURNT) {
            // asap hitam
            for (int s = 0; s < 3; s++) {
                double phase = animTime * 3 + s;
                int sy = cy - 18 - (int) ((Math.sin(phase) + 1) * 5) - s * 5;
                g.setColor(new Color(40, 40, 40, 150));
                g.fillOval(cx - 8 + s * 7, sy, 9, 9);
            }
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            g.setColor(new Color(0xE7, 0x4C, 0x3C));
            String s = "HANGUS";
            int tw = g.getFontMetrics().stringWidth(s);
            g.drawString(s, cx - tw / 2, y + height - 5);

        } else {
            drawTitle(g, new Color(0xEC, 0xF0, 0xF1));
        }
    }
}
