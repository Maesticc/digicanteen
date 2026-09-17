package model;

public class CashPayment extends Payment {
	private double cash;
	
	public CashPayment(double cash) {
		super();
		this.cash = cash;
	}

	@Override
	public boolean paymentProses(double total) {
		if(cash < total) {
			System.out.println("Uang tidak cukup!");
			return false;
		}
		this.totalPayment = total;
		return true;
	}

	@Override
	public void receipt() {
		// TODO Auto-generated method stub
		System.out.println("RECEIPT");
		System.out.println("Total: Rp" + totalPayment);
		System.out.println("Payment: Rp" + cash);
		System.out.println("Change: Rp" + (cash - totalPayment));
	}

}
