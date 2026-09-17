package model;

public class Pembeli extends User {

    private double saldo;
    private Keranjang cart;

    
    public Pembeli(String username, String password, String noTelp, double saldo) {
        super(username, password, noTelp);

        if (saldo < 0) {
            System.out.println("Saldo tidak boleh negatif!");
            return;
        }

        this.saldo = saldo;
        this.cart = new Keranjang();
    }

    public void addToCart(Menu menu, int qty) {
        cart.addItem(menu, qty);  
    }

    public void payOrder(Payment payment) {

        if (cart.isEmpty()) {
            System.out.println("Keranjang masih kosong!");
            return;
        }

        double total = cart.total();  //

        boolean succes = payment.paymentProses(total);

        if (!succes) {
            System.out.println("Pembayaran gagal!");
            return;
        }

        for (OrderDetail od : cart.getItems()) {
            od.getMenu().minStock(od.getQty());
        }

        payment.receipt();
        cart.clear();  //
    }

    @Override
    public void showUser() {
        System.out.println("Pembeli: " + username + " | Saldo: Rp" + saldo);
    }
}
