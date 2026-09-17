package model;

import java.util.ArrayList;

public class Keranjang {

    public static ArrayList<OrderDetail> items = new ArrayList<>();

    public Keranjang() {

    }
    public void addItem(Menu menu, int qty) {

        if (menu == null) {
            System.out.println("Menu tidak ada!");
            return;
        }else if(!menu.cekStock(qty)) {
        	System.out.println("Stock tidak cukup!");
            return;
        }
        items.add(new OrderDetail(menu, qty));
        System.out.println("Item ditambahkan ke keranjang.");
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public ArrayList<OrderDetail> getItems() {
        return items;
    }

    public double total() {
        double total = 0;
        for (OrderDetail od : items) {
			total += od.subTotal();
		}
        return total;
    }

    public void clear() {
        items.clear();
    }
}
