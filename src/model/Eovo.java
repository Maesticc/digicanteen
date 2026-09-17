package model;

public class Eovo extends Ewallet {

	public Eovo(String accountNum) {
		super(accountNum);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean accountValid() {
		// TODO Auto-generated method stub
		return accountNum.startsWith("08");
	}

	@Override
	public void receipt() {
		// TODO Auto-generated method stub
		System.out.println("Ovo Receipt");
		System.out.println("Total: Rp" + totalPayment);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
