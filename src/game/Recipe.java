package game;

import java.util.ArrayList;
import java.util.List;

import model.Menu;

/**
 * Recipe = resep masakan kantin. Membungkus objek model.Menu (nama & harga)
 * dan mencatat bahan-bahan yang dibutuhkan.
 *
 * Di sinilah class OOP existing (model.Menu) dipakai sebagai data harga/nama.
 */
public class Recipe {

    private final Menu menu;                     // model OOP existing
    private final List<IngredientType> need;     // bahan yang harus dimasak

    public Recipe(Menu menu, IngredientType... items) {
        this.menu = menu;
        this.need = new ArrayList<>();
        for (IngredientType it : items) {
            need.add(it);
        }
    }

    public Menu getMenu() {
        return menu;
    }

    public String getName() {
        return menu.getName();
    }

    public double getPrice() {
        return menu.getPrice();
    }

    public List<IngredientType> getNeed() {
        return need;
    }

    /**
     * True jika isi panci tepat sama dengan kebutuhan resep
     * (jumlah & jenis bahan sama, urutan tidak penting).
     */
    public boolean matches(List<IngredientType> contents) {
        if (contents.size() != need.size()) {
            return false;
        }
        List<IngredientType> sisa = new ArrayList<>(need);
        for (IngredientType it : contents) {
            if (!sisa.remove(it)) {
                return false;
            }
        }
        return sisa.isEmpty();
    }
}
