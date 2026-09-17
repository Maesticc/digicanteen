package model;

public abstract class Ewallet extends Payment {
	
	protected String accountNum;
	
	public Ewallet(String accountNum) {
		super();
		if(!accountNum.matches("08[0-9]{8,12}")) {
			System.out.println("No akun tidak valid!");
			return;
		}
		this.accountNum = accountNum;
	}
	
	public abstract boolean accountValid();
	
	
	@Override
	public boolean paymentProses(double total) {
		
		if(!accountValid()) {
			System.out.println("Akun E-Wallet tidak valid!");
			return false;
		}
		
		this.totalPayment = total;
		return true;
	}
}
