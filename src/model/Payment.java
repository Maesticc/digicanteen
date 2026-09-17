package model;

public abstract class Payment {
	protected double totalPayment;

    public abstract boolean paymentProses(double total);

    public abstract void receipt();
}
