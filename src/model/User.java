package model;

public abstract class User {
	protected String username;
	protected String password;
	protected String phoneNum;
	
	public User(String username, String password, String phoneNum) {
		super();
		
		if(username.length() < 3) {
			System.out.println("Username min 3 Char: ");
			return;
		}
		if(password.length() < 4) {
			System.out.println("Password min 4 Char: ");
			return;
		}
		if(!phoneNum.matches("08[0-9]{8,12}")) {
			System.out.println("Phone Number invalid: ");
			return;
		}
		this.username = username;
		this.password = password;
		this.phoneNum = phoneNum;
	}
	


	public String getUsername() {
		return username;
	}



	public void setUsername(String username) {
		this.username = username;
	}



	public String getPassword() {
		return password;
	}



	public void setPassword(String password) {
		this.password = password;
	}



	public String getPhoneNum() {
		return phoneNum;
	}



	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	public abstract void showUser();

}

