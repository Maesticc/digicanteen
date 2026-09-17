package model;

import java.awt.Menu;
import java.util.ArrayList;

public class Penjual extends User {

	public static ArrayList<Menu> M = new ArrayList<>();
	public Penjual(String username, String password, String phoneNum) {
		super(username, password, phoneNum);
		// TODO Auto-generated constructor stub
	}
	
	public void addMenu(Menu menu) {
		if(menu == null) {
			System.out.println("Menu tidak ada");
			return;
		}
		M.add(menu);
	}
	
	
	@Override
	public void showUser() {
		// TODO Auto-generated method stub
		System.out.println("Penjual: " + username);
	}

}
