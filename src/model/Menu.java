package model;

public class Menu {

	private String name;
	private double price;
	private int stock;
	
	public Menu(String name, double price, int stock) {
		super();
		if(name.length() < 5) {
			System.out.println("Nama menu terlalu pendek!");
			return;
		}else if(price <= 0) {
			System.out.println("Harga harus lebih dari 0!");
			return;
		}else if(stock <= 0 ) {
			System.out.println("Stok tidak boleh kosong!");
			return;
		}
		this.name = name;
		this.price = price;
		this.stock = stock;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getStock() {
		return stock;
	}

	public void setStock(int stock) {
		this.stock = stock;
	}
	
	public boolean cekStock(int qty) {
		return qty <= stock;
	}
	
	public void minStock(int qty) {
		stock -= qty;
	}
	
	
	
	

}
