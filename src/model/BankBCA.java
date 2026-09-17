package model;

public class BankBCA extends BankTF {

	public BankBCA(String noRek) {
		super(noRek);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean bankCode() {
		// TODO Auto-generated method stub
		return noRek.startsWith("014");
	}

	@Override
	public void receipt() {
		// TODO Auto-generated method stub
		System.out.println("BCA Receipt");
		System.out.println("Total: Rp" + totalPayment);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
