package model;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class LoginLogout {
	public static ArrayList<User> u = new ArrayList<>();
	private User currentUser = null;
	
	public void register(User user) {
		if(user == null) {
			System.out.println("Tidak ada user untuk saat ini");
			return;
		}
		for (User user1 : u) {
			if(user1.getUsername().equalsIgnoreCase(user1.getUsername())) {
				System.out.println("Username sudah ada!");
				return;
			}
		}
		u.add(user);
		System.out.println("Berhasil menambahkan user: " + user.getUsername());
	}
	
	public User login(String username, String password) {
		for (User user : u) {
			if(user.getUsername().equalsIgnoreCase(username) && user.getPassword().equals(password)) {
				currentUser = user;
				System.out.println("Login telah berhasil, " + user.getUsername());
				return user;
			}
		}
		System.out.println("Login gagal! Cek Username atau Password!");
		return null;
	}

	public void logout() {
		if(currentUser != null) {
			System.out.println("Logout berhasil: " + currentUser);
			currentUser = null;
		}else {
			System.out.println("Tidak ada user saat ini!");
		}
	}

	public User getCurrentUser() {
		return currentUser;
	}
	
	//buat tampilin semua user yang ada di login di main page atau yang udah ada
	public ArrayList<User> getUser(){
		return u;
		
	}

}
