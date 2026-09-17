package model;

public class BankBNI extends BankTF {

	public BankBNI(String noRek) {
		super(noRek);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean bankCode() {
		// TODO Auto-generated method stub
		return noRek.startsWith("009");
	}

	@Override
	public void receipt() {
		// TODO Auto-generated method stub
		System.out.println("BNI Receipt");
		System.out.println("Total: Rp" + totalPayment);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
