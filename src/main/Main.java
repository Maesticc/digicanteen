package main;

import java.util.ArrayList;
import java.util.*;

import model.*;

public class Main {
		public static ArrayList<Menu> m = new ArrayList<>();
		public static ArrayList<User> u = new ArrayList<>();
		public static User currentUser = null;
		public static Scanner sc = new Scanner(System.in);
	
		public static void main (String[] args) {
		// TODO Auto-generated method stub
		while(true) {
			System.out.println("DigiCanteen");
			System.out.println("1. User Registation");
			System.out.println("2. Login");
			System.out.println("3. Delete Users");
			System.out.println("4. Display Users");
			System.out.println("5. Logout");
			System.out.println("7. Buyer");
			System.out.println("8. Seller");
			System.out.println("9. Exit");
			
			if(!sc.hasNextInt()) {
				System.out.println("Invalid input"); sc.next();
				continue;
			}
			int choice = sc.nextInt(); sc.nextLine();
			switch(choice) {
			case 1:
				register();
				break;
			case 2:
				login();
				break;
			case 3:
				delete();
				break;
			case 4:
				display();
				break;
			case 5:
				logout();
				break;
			case 6:
				buyer();
				break;
			case 7:
				seller();
				break;
			case 8:
				exit();
				break;
			default:
				System.out.println("Invalid choice, please choose between 1 - 8");
			}
		}
		
	}
	private static void register() {
		// TODO Auto-generated method stub
		String username, password, noTelp;
		// input username;
		while(true) {
			System.out.println("Username [5 - 20 char]");
			username = sc.nextLine();
			if(username.length() < 5 && username.length() > 20) {
				System.out.println("Invalid username length");
				continue;
			}
			if(usernameexist(username)) {
				System.out.println("Username has already taken!");
				continue;
			}
			break;
		}
		
		// input password
		while(true) {
			System.out.println("Password must be unique [ 5 - 20 char ]!");
			password = sc.nextLine();
			if(password.length() < 5 && password.length() > 20) {
				System.out.println("Invalid password length");
				continue;
			}
			break;
		}
		
		while(true) {
			System.out.println("Phone number must be start with'08' and 8 - 12 digits!");
			noTelp = sc.nextLine();
			if(!noTelp.matches("08[0-9]{8,12}")) {
				System.out.println("Invalid phone number!");
				continue;
			}
			break;
		}
		
		int role;
		while(true) {
			System.out.println("Choose your role!");
			System.out.println("1. Buyer");
			System.out.println("2. Seller");
			System.out.println("Choose your role (1/2)!");
			if(!sc.hasNextInt()) {
				System.out.println("Input must be a number!");
				sc.next(); continue;
			}
			
			role = sc.nextInt(); sc.nextLine();
			if(role == 1 || role == 2) break;
			System.out.println("Role invalid, choose 1 or 2!");
		}
		 
		User newUser = null;
		if(role == 1) {
			double balance;
			
			while(true) {
				System.out.println("Input saldo awal: (>= 0):");
				if(!sc.hasNextDouble()) {
					System.out.println("Invalid input, must be numeric!"); sc.next();
					continue;
				}
				
				balance = sc.nextDouble(); sc.nextLine();
				if(balance < 0) {
					System.out.println("Balance cannot be negative");
					continue;
				}
				break;
			}
			
			newUser = new Pembeli(username, password, noTelp, balance);
			System.out.println("Buyer already register!");
		}else{
	        newUser = new Penjual(username, password, noTelp);
	        System.out.println("Seller already register!");
	    }
		
		u.add(newUser);
		System.out.println("User added: " + username);

	}

	private static boolean usernameexist(String username) {
		// TODO Auto-generated method stub\
		for(User users : u) {
			if(users.getUsername().equals(username)) {
				return true;
			}
		}
		return false;
	}
	
	private static void login() {
		// TODO Auto-generated method stub
		String username;
		String password;
		
		System.out.println("Enter username: ");
		username = sc.nextLine();
		
		System.out.println("Enter password: ");
		password = sc.nextLine();
		
		for(User users1  : u) {
			if(users1.getUsername().equalsIgnoreCase(username) && users1.getPassword().equals(password)) {
				currentUser = users1;
				System.out.println("Login Success!");
				return;
			}
		}
		System.out.println("Invalid username or password");
	}
	
	private static void delete() {
		// TODO Auto-generated method stub
		if(u.isEmpty()) {
			System.out.println("No users right now !");
			return;
		}
		
		System.out.println("User List");
		for(int i = 0; i < u.size(); i++) {
			System.out.println((i+1) + ". " + u.get(i).getUsername());
		}
		System.out.println("Choose participants to delete [0 = cancel]: ");
		
		if(!sc.hasNextInt()) {
			System.out.println("Invalid Input");
			sc.next();
			return;
		}
		int index = sc.nextInt();
		sc.nextLine();
		
		if(index == 0) return;
		if(index < 1 || index > u.size()) {
			System.out.println("Invalid Index");
			return;
		}
		
		u.remove(index - 1);
		System.out.println("User succeed deleted");
	}
	
	
	private static void display() {
		// TODO Auto-generated method stub
		if(u.isEmpty()) {
			System.out.println("There is no User!");
			return;
		}else {
			int count = 1;
			for(User users2 : u) {
				System.out.println("User no. " + count);
				users2.showUser();
			}
		}
	}
	
	private static void logout() {
		// TODO Auto-generated method stub
		if(currentUser == null) {
			System.out.println("There is no User logged in.!");
			return;
		}
		System.out.println("Logout: " + currentUser.getUsername());
		currentUser = null;
	}
	
	
	private static void buyer() {
		// TODO Auto-generated method stub
		if(!(currentUser instanceof Pembeli)) {
			System.out.println("Login first as buyer!");
			return;
		}
		
		Pembeli buyer = (Pembeli) currentUser;
		
		while(true) {
			System.out.println("\n Buyer Menu");
			System.out.println("1. View menu");
			System.out.println("2. Add to cart");
			System.err.println("3. Pay");
			System.out.println("4. Back");
			
			if(!sc.hasNextInt()) {
				System.out.println("Invalid input"); sc.next();
				continue;
			}
			
			int choice;
			choice = sc.nextInt(); sc.nextLine();
			switch(choice) {
			// buat liat menunya
			case 1:
				if(m.isEmpty()){
					System.out.println("There is no menu!!");
					break;
				}
				System.out.println("\n Canteen Menu");
				for(int i = 0; i < m.size(); i++) {
					Menu menu = m.get(i);
					System.out.println((i + 1) + ". " + menu.getName() + " | Price: " + menu.getPrice() + " | Stock: " + menu.getStock());
				}
				break;
			// buat tambahin barang ke keranjang
			case 2:
				if(m.isEmpty()) {
					System.out.println("There is no menu!!");
					break;
				}
				System.out.println("\n Select a menu number to add to cart: ");
	            if (!sc.hasNextInt()) {
	                System.out.println("Invalid Input!");
	                sc.next();
	                break;
	            }
	            
	            int index = sc.nextInt();
	    		sc.nextLine();
	    		if(index < 1 || index > m.size()) {
	    			System.out.println("Invalid Index");
	    			return;
	    		}
	    		
	    		Menu chooseMenu = m.get(index - 1);
	    		
	    		System.out.println("Inout Quantity: ");
	    		if (!sc.hasNextInt()) {
	                System.out.println("Invalid Input!");
	                sc.next();
	                break;
	            }
	    		
	    		int qty = sc.nextInt(); sc.nextLine();
	    		if(qty <= 0) {
	    			System.out.println("Quantity must > 0!");
	    			break;
	    		}
	    		buyer.addToCart(chooseMenu, qty);
	    		System.out.println("Item success add to cart!");
	    		break;
	    		
	        case 3:
	            System.out.println("\n Choose your payment method");
	            System.out.println("1. OVO");
	            System.out.println("2. GoPay");

	            if (!sc.hasNextInt()) {
	                System.out.println("Invalid input!"); 
	                sc.next();
	                break;
	            }

	            int payChoice = sc.nextInt();
	            sc.nextLine();

	            Payment payment = null;
	            String acc;

	            switch (payChoice) {
	            case 1:
	                System.out.print("Input OVO number: ");
	                acc = sc.nextLine();
	                payment = new Eovo(acc);
	                break;
	            case 2:
	                System.out.print("Input Gopay number: ");
	                acc = sc.nextLine();
	                payment = new Egopay(acc);
	                break;
	            default:
	                System.out.println("Payment invalid!");
	                continue;
	            }
	            buyer.payOrder(payment);
	            break;
	        case 4:
	            System.out.println("Back to main menu....");
	            return;

	        default:
	            System.out.println("Invalid input!");
			}
		}
	}
	
	
	private static void seller() {
		// instance of itu kayak adalah jadi misal 
		//dia currenUser instanceof penjual berarti artinya user skrg itu adalah penjual
		if(!(currentUser instanceof Penjual)) {
			System.out.println("Login first as seller!");
			return;
		}
		
		Penjual seller = (Penjual) currentUser;

	    while (true) {
	        System.out.println("\n Seller Menu ");
	        System.out.println("1. Add Menu");
	        System.out.println("2. View Menu");
	        System.out.println("3. Update Menu");
	        System.out.println("4. Delete Menu");
	        System.out.println("5. Back");

	        if (!sc.hasNextInt()) {
	            System.out.println("Invalid input!");
	            sc.next();
	            continue;
	        }

	        int choice = sc.nextInt();
	        sc.nextLine();

	        switch (choice) {
	        case 1:
	            addMenu();
	            break;
	        case 2:
	            viewMenu();
	            break;
	        case 3:
	            updateMenu();
	            break;
	        case 4:
	            deleteMenu();
	            break;
	        case 5:
	            return;
	        default:
	            System.out.println("Invalid choice!");
	        }
	    }
	}
	
	private static void addMenu() {
		// TODO Auto-generated method stub
		String name;
		int price, stock;
		
		System.out.println("Input menu name: ");
		name = sc.nextLine();
		
		   while (true) {
		        System.out.println("Input Price must be > 0: ");
		        if (!sc.hasNextInt()) {
		            System.out.println("Invalid input!");
		            sc.next();
		            continue;
		        }
		        price = sc.nextInt(); sc.nextLine();
		        if (price > 0)
		            break;
		        System.out.println("Price must be > 0");
		    }

		    while (true) {
		        System.out.println("Input Stock > 0: ");
		        if (!sc.hasNextInt()) {
		            System.out.println("Invalid input!");
		            sc.next();
		            continue;
		        }
		        stock = sc.nextInt(); sc.nextLine();
		        if (stock > 0)
		            break;
		        System.out.println("Stock must be > 0");
		    }

		    m.add(new Menu(name, price, stock));
		    System.out.println("Menu added!");
	}
	
	private static void viewMenu() {
		// TODO Auto-generated method stub
		if(m.isEmpty()) {
			System.out.println("Menu is empty!");
			return;
		}else {
			int count = 1;
			for(Menu menu : m) {
				System.out.println(count + ". " + menu.getName() + " | Rp" + menu.getPrice() + " | Stock" + menu.getStock());
				count++;
			}
		}
	}
	private static void updateMenu() {
		// TODO Auto-generated method stub
		if (m.isEmpty()) {
	        System.out.println("Menu is empty!");
	        return;
	    }

	    viewMenu();
	    System.out.println("Choose item to update [0 cancel]: ");

	    if (!sc.hasNextInt()) {
	        System.out.println("Invalid input!"); sc.next();
	        return;
	    }

	    int index = sc.nextInt(); sc.nextLine();

	    if (index == 0) return;
	    if (index < 1 || index > m.size()) {
	        System.out.println("Invalid index!");
	        return;
	    }

	    Menu menu = m.get(index - 1);

	    String newName;
	    int newPrice, newStock;

	    System.out.println("Input new name: ");
	    newName = sc.nextLine();

	    while (true) {
	        System.out.println("Input new price must be > 0: ");
	        if (!sc.hasNextInt()) {
	            System.out.println("Invalid input!");
	            sc.next();
	            continue;
	        }
	        newPrice = sc.nextInt(); sc.nextLine();
	        if (newPrice > 0) break;
	        System.out.println("Price must be > 0 !");
	    }

	    while (true) {
	        System.out.println("Input new stock must  be > 0: ");
	        if (!sc.hasNextInt()) {
	            System.out.println("Invalid input!");
	            sc.next();
	            continue;
	        }
	        newStock = sc.nextInt(); sc.nextLine();
	        if (newStock > 0) break;
	        System.out.println("Stock must be > 0!");
	    }

	    menu.setName(newName);
	    menu.setPrice(newPrice);
	    menu.setStock(newStock);

	    System.out.println("Menu updated!");
	}
	private static void deleteMenu() {
		// TODO Auto-generated method stub
		if(u.isEmpty()) {
			System.out.println("Menu is empty!");
			return;
		}
		
		
		System.out.println("Menu List");
		for(int i = 0; i < m.size(); i++) {
			System.out.println((i+1) + ". " + m.get(i).getName());
		}
		System.out.println("Choose menu to delete [0 = cancel]: ");
		
		if(!sc.hasNextInt()) {
			System.out.println("Invalid Input");
			sc.next();
			return;
		}
		
		int index = sc.nextInt();
		sc.nextLine();
		
		if(index == 0) return;
		if(index < 1 || index > m.size()) {
			System.out.println("Invalid Index");
			return;
		}
		
		m.remove(index - 1);
		System.out.println("Menu succeed deleted");
	}
	
	private static void exit() {
		// TODO Auto-generated method stub
		System.out.println("Exiting program...");
		System.exit(0);
	}

}
