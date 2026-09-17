package model;

public class OrderDetail {

	private Menu menu;
	private int qty;
	public OrderDetail(Menu menu, int qty) {
		super();
		this.menu = menu;
		this.qty = qty;
	}
	public Menu getMenu() {
		return menu;
	}
	public void setMenu(Menu menu) {
		this.menu = menu;
	}
	public int getQty() {
		return qty;
	}
	public void setQty(int qty) {
		this.qty = qty;
	}
	
	public double subTotal() {
		return menu.getPrice() * qty;
	}
}
