package model;

public abstract class BankTF extends Payment {
	protected String noRek;
	
	public BankTF(String noRek) {
		super();
		if(!noRek.matches("[0-9] {10.16}")) {
			System.out.println("Nomor rekening salah!");
			return;
		}
		this.noRek = noRek;
	}

	public abstract boolean bankCode();
	@Override
	public boolean paymentProses(double total) {
		// TODO Auto-generated method stub
		System.out.println("Rekening yang anda masukan tidak valid!");
		return false;
	}
}
