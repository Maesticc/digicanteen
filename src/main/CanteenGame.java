package main;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import model.CashPayment;
import model.Egopay;
import model.Eovo;
import model.Keranjang;
import model.Menu;
import model.OrderDetail;
import model.Payment;
import model.Pembeli;

/**
 * CANTEEN RUSH - game simpel berbasis OOP.
 *
 * Kamu berperan sebagai Pembeli di kantin digital. Setiap hari (ronde) kantin
 * menyediakan menu acak. Tugasmu: belanja secerdik mungkin untuk mengumpulkan
 * POIN sebanyak-banyaknya sebelum saldo habis atau hari terakhir tiba.
 *
 * Game ini memanfaatkan class OOP yang sudah dibuat:
 *  - Menu, OrderDetail, Keranjang  (item & keranjang belanja)
 *  - Pembeli extends User          (pemain)
 *  - Payment -> Ewallet -> Eovo/Egopay, dan CashPayment (polymorphism pembayaran)
 */
public class CanteenGame {

    private static final Scanner sc = new Scanner(System.in);
    private static final Random rng = new Random();

    // Daftar nama menu acak (semua >= 5 char sesuai aturan class Menu)
    private static final String[] NAMA_MENU = {
            "Nasi Goreng", "Mie Ayam Bakso", "Es Teh Manis", "Ayam Geprek",
            "Soto Ayam Kudus", "Kopi Susu Gula Aren", "Roti Bakar Coklat",
            "Sate Ayam Madura", "Jus Alpukat Segar", "Batagor Bandung"
    };

    private static final int TOTAL_HARI = 5;   // jumlah ronde
    private static final int TARGET_POIN = 100; // target menang

    public static void main(String[] args) {
        printBanner();

        // Pemain adalah objek Pembeli (dari model OOP existing)
        String nama = mintaNama();
        double saldoAwal = mintaSaldoAwal();
        Pembeli pemain = new Pembeli(nama, "player1", "081234567890", saldoAwal);

        double saldo = saldoAwal; // tracking saldo di sisi game (Pembeli.saldo private)
        int poin = 0;

        for (int hari = 1; hari <= TOTAL_HARI; hari++) {
            System.out.println("\n===================================");
            System.out.println("            HARI KE-" + hari + " / " + TOTAL_HARI);
            System.out.println("===================================");
            System.out.println("Saldo : Rp" + (int) saldo + "   |   Poin : " + poin + " / " + TARGET_POIN);

            if (saldo <= 0) {
                System.out.println("\nSaldomu habis! Tidak bisa belanja lagi.");
                break;
            }

            // Kantin menyediakan menu acak hari ini
            ArrayList<Menu> menuHariIni = buatMenuAcak();

            // Reset keranjang tiap hari (Keranjang.items bersifat static)
            Keranjang.items.clear();

            belanjaHariIni(pemain, menuHariIni);

            double totalBelanja = new Keranjang().total();
            if (totalBelanja <= 0) {
                System.out.println("Kamu tidak jadi belanja hari ini.");
                continue;
            }

            if (totalBelanja > saldo) {
                System.out.println("\nTotal belanja Rp" + (int) totalBelanja
                        + " melebihi saldo Rp" + (int) saldo + ". Transaksi dibatalkan!");
                Keranjang.items.clear();
                continue;
            }

            // Bayar dengan polymorphism Payment
            boolean bayarSukses = prosesPembayaran(pemain, totalBelanja);
            if (!bayarSukses) {
                System.out.println("Pembayaran gagal, belanja dibatalkan.");
                Keranjang.items.clear();
                continue;
            }

            saldo -= totalBelanja;

            // Skoring: tiap item = poin, ada bonus acak "menu favorit hari ini"
            int poinDidapat = hitungPoin();
            poin += poinDidapat;
            System.out.println(">> Kamu mendapatkan " + poinDidapat + " poin hari ini!");

            Keranjang.items.clear();

            if (poin >= TARGET_POIN) {
                break;
            }
        }

        tampilkanHasil(pemain, poin, saldo);
    }

    // ------------------------------------------------------------------
    // Setup awal
    // ------------------------------------------------------------------

    private static void printBanner() {
        System.out.println("========================================");
        System.out.println("            C A N T E E N   R U S H      ");
        System.out.println("========================================");
        System.out.println("Belanja pintar di kantin, kumpulkan poin,");
        System.out.println("dan capai target sebelum saldo habis!");
        System.out.println("Target: " + TARGET_POIN + " poin dalam " + TOTAL_HARI + " hari.\n");
    }

    private static String mintaNama() {
        System.out.print("Masukkan nama pemain: ");
        String nama = sc.nextLine().trim();
        if (nama.length() < 3) {
            nama = "Player"; // Pembeli/User butuh minimal 3 char
            System.out.println("Nama terlalu pendek, memakai nama default: " + nama);
        }
        return nama;
    }

    private static double mintaSaldoAwal() {
        while (true) {
            System.out.print("Masukkan saldo awal (disarankan 50000 - 100000): ");
            if (!sc.hasNextDouble()) {
                System.out.println("Input harus angka!");
                sc.next();
                continue;
            }
            double s = sc.nextDouble();
            sc.nextLine();
            if (s <= 0) {
                System.out.println("Saldo harus lebih dari 0!");
                continue;
            }
            return s;
        }
    }

    // ------------------------------------------------------------------
    // Menu acak per hari
    // ------------------------------------------------------------------

    private static ArrayList<Menu> buatMenuAcak() {
        ArrayList<Menu> hasil = new ArrayList<>();
        int jumlah = 3 + rng.nextInt(3); // 3 - 5 menu

        ArrayList<String> pool = new ArrayList<>();
        for (String n : NAMA_MENU) {
            pool.add(n);
        }

        for (int i = 0; i < jumlah && !pool.isEmpty(); i++) {
            String nama = pool.remove(rng.nextInt(pool.size()));
            int harga = (5 + rng.nextInt(26)) * 1000; // 5.000 - 30.000
            int stok = 1 + rng.nextInt(10);           // 1 - 10
            hasil.add(new Menu(nama, harga, stok));
        }
        return hasil;
    }

    private static void tampilkanMenu(ArrayList<Menu> menu) {
        System.out.println("\n--- MENU KANTIN HARI INI ---");
        for (int i = 0; i < menu.size(); i++) {
            Menu m = menu.get(i);
            System.out.println((i + 1) + ". " + m.getName()
                    + "  | Rp" + (int) m.getPrice()
                    + "  | Stok: " + m.getStock());
        }
        System.out.println("0. Selesai belanja");
    }

    // ------------------------------------------------------------------
    // Loop belanja
    // ------------------------------------------------------------------

    private static void belanjaHariIni(Pembeli pemain, ArrayList<Menu> menu) {
        while (true) {
            tampilkanMenu(menu);
            System.out.print("Pilih nomor menu untuk dibeli: ");

            if (!sc.hasNextInt()) {
                System.out.println("Input tidak valid!");
                sc.next();
                continue;
            }
            int pilih = sc.nextInt();
            sc.nextLine();

            if (pilih == 0) {
                return;
            }
            if (pilih < 1 || pilih > menu.size()) {
                System.out.println("Nomor menu tidak ada!");
                continue;
            }

            Menu dipilih = menu.get(pilih - 1);

            System.out.print("Jumlah (stok tersedia " + dipilih.getStock() + "): ");
            if (!sc.hasNextInt()) {
                System.out.println("Input tidak valid!");
                sc.next();
                continue;
            }
            int qty = sc.nextInt();
            sc.nextLine();

            if (qty <= 0) {
                System.out.println("Jumlah harus lebih dari 0!");
                continue;
            }

            // Pakai method Pembeli.addToCart -> Keranjang.addItem (validasi stok di dalamnya)
            pemain.addToCart(dipilih, qty);

            System.out.println("Subtotal keranjang saat ini: Rp" + (int) new Keranjang().total());
        }
    }

    // ------------------------------------------------------------------
    // Pembayaran (polymorphism Payment)
    // ------------------------------------------------------------------

    private static boolean prosesPembayaran(Pembeli pemain, double total) {
        System.out.println("\n--- PEMBAYARAN ---");
        System.out.println("Total belanja: Rp" + (int) total);
        System.out.println("Pilih metode pembayaran:");
        System.out.println("1. OVO");
        System.out.println("2. GoPay");
        System.out.println("3. Cash");

        int pilih;
        while (true) {
            System.out.print("Pilihan: ");
            if (!sc.hasNextInt()) {
                System.out.println("Input tidak valid!");
                sc.next();
                continue;
            }
            pilih = sc.nextInt();
            sc.nextLine();
            if (pilih >= 1 && pilih <= 3) {
                break;
            }
            System.out.println("Pilih 1, 2, atau 3!");
        }

        Payment payment;
        switch (pilih) {
            case 1:
                payment = new Eovo(mintaNomorEwallet("OVO"));
                break;
            case 2:
                payment = new Egopay(mintaNomorEwallet("GoPay"));
                break;
            default:
                payment = new CashPayment(mintaUangCash(total));
                break;
        }

        // payOrder milik Pembeli akan memproses payment + kurangi stok + cetak receipt
        // tapi kita panggil manual agar bisa tahu status sukses/gagal.
        boolean sukses = payment.paymentProses(total);
        if (sukses) {
            for (OrderDetail od : new Keranjang().getItems()) {
                od.getMenu().minStock(od.getQty());
            }
            payment.receipt();
        }
        return sukses;
    }

    private static String mintaNomorEwallet(String namaWallet) {
        System.out.print("Masukkan nomor " + namaWallet + " (contoh 081234567890): ");
        String no = sc.nextLine().trim();
        if (no.isEmpty()) {
            no = "081234567890"; // default valid supaya game tetap jalan
        }
        return no;
    }

    private static double mintaUangCash(double total) {
        while (true) {
            System.out.print("Masukkan jumlah uang cash: ");
            if (!sc.hasNextDouble()) {
                System.out.println("Input harus angka!");
                sc.next();
                continue;
            }
            double cash = sc.nextDouble();
            sc.nextLine();
            return cash;
        }
    }

    // ------------------------------------------------------------------
    // Skoring
    // ------------------------------------------------------------------

    private static int hitungPoin() {
        int poin = 0;
        for (OrderDetail od : new Keranjang().getItems()) {
            poin += 10 * od.getQty(); // tiap item bernilai 10 poin
        }
        // Bonus acak "menu favorit" untuk elemen kejutan
        if (rng.nextInt(100) < 30) { // 30% peluang
            int bonus = 15 + rng.nextInt(16); // 15 - 30
            System.out.println(">> BONUS! Menu favorit terbeli, +" + bonus + " poin!");
            poin += bonus;
        }
        return poin;
    }

    // ------------------------------------------------------------------
    // Hasil akhir
    // ------------------------------------------------------------------

    private static void tampilkanHasil(Pembeli pemain, int poin, double saldo) {
        System.out.println("\n========================================");
        System.out.println("               GAME SELESAI             ");
        System.out.println("========================================");
        pemain.showUser(); // polymorphism: menampilkan info pemain
        System.out.println("Poin akhir : " + poin);
        System.out.println("Sisa saldo : Rp" + (int) saldo);

        if (poin >= TARGET_POIN) {
            System.out.println("\n*** SELAMAT! Kamu MENANG! ***");
            System.out.println("Kamu berhasil mencapai target " + TARGET_POIN + " poin!");
        } else {
            System.out.println("\nBelum berhasil kali ini. Target " + TARGET_POIN
                    + " poin belum tercapai.");
            System.out.println("Coba lagi dan atur strategi belanjamu!");
        }
        System.out.println("Terima kasih sudah bermain Canteen Rush!");
    }
}
