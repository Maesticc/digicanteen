package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.Timer;

import model.CashPayment;
import model.Egopay;
import model.Eovo;
import model.Menu;
import model.Payment;
import model.Pembeli;

/**
 * DigiCanteen Tycoon - game manajemen kantin berbasis mouse.
 *
 * Alur main (semua pakai KLIK MOUSE):
 *   1) Pelanggan datang & mengantre, masing-masing memesan sebuah Menu.
 *      Bar kesabaran mereka terus menurun.
 *   2) Klik pelanggan (state MENUNGGU) untuk MENGAMBIL pesanan -> masuk dapur.
 *   3) Dapur memasak (butuh waktu, kompor terbatas). Saat matang, pelanggan
 *      ditandai "SIAP" (border putih berkedip).
 *   4) Klik pelanggan yang "SIAP" untuk MENYAJIKAN -> pelanggan bayar memakai
 *      metode acak (Eovo / Egopay / CashPayment = polymorphism Payment).
 *   5) Kejar target pendapatan tiap level sebelum terlalu banyak pelanggan kabur.
 *
 * Memakai class model existing: Menu, Pembeli, OrderDetail (via Customer),
 * Payment + turunannya.
 */
public class GamePanel extends JPanel implements ActionListener, KeyListener {

    private static final long serialVersionUID = 1L;

    public static final int WIDTH = 900;
    public static final int HEIGHT = 640;

    private static final int MAX_ANTREAN = 5;   // maksimal pelanggan sekaligus
    private static final int MAX_KABUR = 3;      // kalau lebih, game over

    private final Random rng = new Random();
    private final Timer loop;
    private long lastTick;

    // dunia game
    private Kitchen kitchen;
    private final ArrayList<Customer> customers = new ArrayList<>();
    private final ArrayList<Customer> ready = new ArrayList<>(); // makanan siap saji
    private ArrayList<Menu> menuList;

    // status permainan
    private int level = 1;
    private double revenue;     // pendapatan level ini
    private double target;      // target pendapatan level ini
    private int totalRevenue;   // total sepanjang game
    private int served;
    private int kabur;
    private int score;

    private double spawnTimer;
    private double spawnInterval; // detik antar kedatangan
    private double patienceDrain; // kecepatan turun kesabaran per level

    private boolean gameOver;
    private boolean levelClear;

    // feedback teks
    private String flash = "";
    private long flashUntil;

    private static final String[] NAMA_MENU = {
            "Nasi Goreng", "Mie Ayam Bakso", "Es Teh Manis", "Ayam Geprek",
            "Soto Ayam Kudus", "Kopi Susu Aren", "Roti Bakar Coklat", "Sate Madura"
    };

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(0xFB, 0xF6, 0xE9));
        setFocusable(true);
        addKeyListener(this);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });

        startLevel(1);

        lastTick = System.nanoTime();
        loop = new Timer(16, this); // ~60 FPS
        loop.start();
    }

    // ------------------------------------------------------------------
    // Setup level
    // ------------------------------------------------------------------

    private void startLevel(int lvl) {
        this.level = lvl;
        this.revenue = 0;
        this.served = 0;
        this.kabur = 0;
        this.gameOver = false;
        this.levelClear = false;
        customers.clear();
        ready.clear();

        // menu tersedia (harga acak, semua nama >= 5 char sesuai aturan Menu)
        menuList = new ArrayList<>();
        String[] dipakai = { NAMA_MENU[0], NAMA_MENU[1], NAMA_MENU[3], NAMA_MENU[5] };
        for (String nama : dipakai) {
            int harga = (8 + rng.nextInt(18)) * 1000; // 8.000 - 25.000
            menuList.add(new Menu(nama, harga, 999));
        }

        // parameter kesulitan naik tiap level
        this.spawnInterval = Math.max(1.6, 4.0 - lvl * 0.35);
        this.patienceDrain = 5.0 + lvl * 1.5;
        this.spawnTimer = 1.0;
        int kompor = Math.min(4, 2 + lvl / 2);
        double cookTime = Math.max(1.5, 3.0 - lvl * 0.15);
        this.kitchen = new Kitchen(WIDTH - 360, 110, 340, 220, kompor, cookTime);

        // target pendapatan naik tiap level
        this.target = 60000 + (lvl - 1) * 40000;

        setFlash("LEVEL " + lvl + " - Target: Rp" + (int) target);
    }

    // ------------------------------------------------------------------
    // Game loop
    // ------------------------------------------------------------------

    @Override
    public void actionPerformed(ActionEvent e) {
        long now = System.nanoTime();
        double dt = (now - lastTick) / 1_000_000_000.0;
        lastTick = now;

        if (!gameOver && !levelClear) {
            update(dt);
        }
        repaint();
    }

    private void update(double dt) {
        // spawn pelanggan baru
        spawnTimer -= dt;
        if (spawnTimer <= 0 && customers.size() < MAX_ANTREAN) {
            spawnCustomer();
            spawnTimer = spawnInterval;
        }

        // update kesabaran + tangani yang kabur
        Iterator<Customer> it = customers.iterator();
        while (it.hasNext()) {
            Customer c = it.next();
            c.update(dt);
            if (c.getState() == Customer.State.LEFT_ANGRY) {
                kabur++;
                ready.remove(c);
                setFlash(c.getPembeli().getUsername() + " kabur! (-1 nyawa)");
                it.remove();
                if (kabur >= MAX_KABUR) {
                    gameOver = true;
                }
            }
        }

        // update dapur; makanan yang matang -> masuk daftar siap saji
        ArrayList<Customer> selesai = kitchen.update(dt);
        for (Customer c : selesai) {
            if (c.getState() == Customer.State.ORDER_TAKEN) {
                ready.add(c);
            }
        }

        // cek target level tercapai
        if (revenue >= target) {
            levelClear = true;
        }
    }

    private void spawnCustomer() {
        int slot = customers.size();
        int cx = 40 + slot * 100;
        int cy = 360;

        Menu pesanan = menuList.get(rng.nextInt(menuList.size()));
        Pembeli p = new Pembeli("Guest" + (rng.nextInt(900) + 100), "pass1", "081234567890", 200000);
        customers.add(new Customer(cx, cy, p, pesanan, patienceDrain));
    }

    private void repositionQueue() {
        for (int i = 0; i < customers.size(); i++) {
            Customer c = customers.get(i);
            c.x = 40 + i * 100;
            c.y = 360;
        }
    }

    // ------------------------------------------------------------------
    // Interaksi mouse
    // ------------------------------------------------------------------

    private void handleClick(int mx, int my) {
        if (gameOver) {
            startLevel(1);
            level = 1;
            totalRevenue = 0;
            score = 0;
            return;
        }
        if (levelClear) {
            startLevel(level + 1);
            return;
        }

        // cari pelanggan yang diklik
        for (Customer c : customers) {
            if (c.getBounds().contains(mx, my)) {
                onCustomerClicked(c);
                return;
            }
        }
    }

    private void onCustomerClicked(Customer c) {
        switch (c.getState()) {
            case WAITING:
                // ambil pesanan -> masukkan ke dapur (kalau kompor tersedia)
                if (kitchen.isFull()) {
                    setFlash("Dapur penuh! Tunggu masakan selesai.");
                    return;
                }
                c.takeOrder();
                kitchen.startCooking(c);
                setFlash("Pesanan " + c.getOrder().getName() + " mulai dimasak");
                break;

            case ORDER_TAKEN:
                if (ready.contains(c)) {
                    serveCustomer(c);
                } else {
                    setFlash("Masakan belum matang...");
                }
                break;

            default:
                break;
        }
    }

    /** Sajikan makanan matang & proses pembayaran (polymorphism Payment). */
    private void serveCustomer(Customer c) {
        c.serve();
        ready.remove(c);

        double harga = c.getOrder().getPrice();
        double bayar = harga * c.tipMultiplier(); // bonus jika masih sabar

        // metode bayar acak: OVO / GoPay / Cash -> semua turunan Payment
        Payment payment = randomPayment(bayar);
        boolean ok = payment.paymentProses(bayar);
        if (ok) {
            revenue += bayar;
            totalRevenue += (int) bayar;
            served++;
            score += 100 + (int) ((c.tipMultiplier() - 1.0) * 100);
            setFlash("Dibayar Rp" + (int) bayar + " (" + payment.getClass().getSimpleName() + ")");
        } else {
            setFlash("Pembayaran gagal!");
        }

        // buang pelanggan yang sudah dilayani dari antrean, lalu rapikan
        customers.remove(c);
        repositionQueue();
    }

    private Payment randomPayment(double amount) {
        int pick = rng.nextInt(3);
        switch (pick) {
            case 0: return new Eovo("081234567890");
            case 1: return new Egopay("081234567890");
            default: return new CashPayment(amount); // cash pas
        }
    }

    private void setFlash(String msg) {
        flash = msg;
        flashUntil = System.currentTimeMillis() + 1800;
    }

    // ------------------------------------------------------------------
    // Rendering
    // ------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2);

        kitchen.draw(g2);

        // pelanggan + tanda "siap"
        for (Customer c : customers) {
            c.draw(g2);
            if (ready.contains(c)) {
                boolean blink = (System.currentTimeMillis() / 300) % 2 == 0;
                g2.setColor(blink ? Color.WHITE : new Color(0x27, 0xAE, 0x60));
                g2.drawRoundRect(c.getX() - 4, c.getY() - 16, c.getWidth() + 8, c.getHeight() + 20, 12, 12);
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.setColor(new Color(0x14, 0x6C, 0x43));
                g2.drawString("SIAP - klik!", c.getX() - 2, c.getY() + c.getHeight() + 16);
            }
        }

        drawHud(g2);

        if (gameOver) drawOverlay(g2, "GAME OVER", "Terlalu banyak pelanggan kabur!", "Klik untuk main lagi");
        else if (levelClear) drawOverlay(g2, "LEVEL " + level + " SELESAI!", "Target tercapai!", "Klik untuk lanjut ke level " + (level + 1));
    }

    private void drawBackground(Graphics2D g2) {
        // lantai
        g2.setColor(new Color(0xFB, 0xF6, 0xE9));
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        // header bar
        g2.setColor(new Color(0xE9, 0x76, 0x2A));
        g2.fillRect(0, 0, WIDTH, 60);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 24));
        g2.drawString("DigiCanteen Tycoon", 20, 40);

        // label area antrean
        g2.setColor(new Color(0x7F, 0x8C, 0x8D));
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString("ANTREAN PELANGGAN", 40, 340);
    }

    private void drawHud(Graphics2D g2) {
        // progress target di header
        int barX = 260, barY = 22, barW = 360, barH = 18;
        g2.setColor(new Color(255, 255, 255, 120));
        g2.fillRoundRect(barX, barY, barW, barH, 8, 8);
        double p = Math.min(1.0, revenue / target);
        g2.setColor(new Color(0x27, 0xAE, 0x60));
        g2.fillRoundRect(barX, barY, (int) (barW * p), barH, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.drawString("Rp" + (int) revenue + " / Rp" + (int) target, barX + 8, barY + 14);

        // info kanan atas
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("Level " + level, WIDTH - 150, 25);
        g2.drawString("Skor: " + score, WIDTH - 150, 45);

        // nyawa (pelanggan kabur)
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString("Kabur: " + kabur + "/" + MAX_KABUR, 640, 40);

        // flash
        if (System.currentTimeMillis() < flashUntil) {
            g2.setColor(new Color(0x21, 0x2F, 0x3D));
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.drawString(flash, 40, 90);
        }

        // petunjuk bawah
        g2.setColor(new Color(0x7F, 0x8C, 0x8D));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString("Klik pelanggan MENUNGGU untuk ambil pesanan  |  Klik pelanggan SIAP untuk sajikan & terima bayaran",
                40, HEIGHT - 16);
    }

    private void drawOverlay(Graphics2D g2, String title, String sub, String hint) {
        g2.setColor(new Color(0, 0, 0, 190));
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 44));
        g2.drawString(title, WIDTH / 2 - g2.getFontMetrics().stringWidth(title) / 2, HEIGHT / 2 - 40);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 20));
        g2.drawString(sub, WIDTH / 2 - g2.getFontMetrics().stringWidth(sub) / 2, HEIGHT / 2 + 4);
        g2.drawString("Total pendapatan: Rp" + totalRevenue,
                WIDTH / 2 - 120, HEIGHT / 2 + 36);

        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString(hint, WIDTH / 2 - g2.getFontMetrics().stringWidth(hint) / 2, HEIGHT / 2 + 80);
    }

    // ------------------------------------------------------------------
    // Keyboard (opsional: R untuk restart)
    // ------------------------------------------------------------------

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_R && gameOver) {
            totalRevenue = 0;
            score = 0;
            startLevel(1);
        }
    }

    @Override public void keyReleased(KeyEvent e) { }
    @Override public void keyTyped(KeyEvent e) { }
}
