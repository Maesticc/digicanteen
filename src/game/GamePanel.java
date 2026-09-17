package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.Timer;

import model.CashPayment;
import model.Menu;
import model.Payment;
import model.Pembeli;

/**
 * GamePanel = area utama game. Menjalankan game loop (Swing Timer ~60 FPS),
 * menggambar semua entitas, menangani input keyboard, serta mengatur logika
 * skor, timer permainan, dan pembayaran memakai class model.Payment.
 *
 * Cara main:
 *  - Gerak: WASD atau tombol panah
 *  - Ambil menu: dekati counter lalu tekan SPACE
 *  - Antar menu: dekati meja yang memesan menu itu lalu tekan SPACE
 *  - Antar pesanan yang benar untuk dapat skor sebelum waktu habis
 */
public class GamePanel extends JPanel implements ActionListener, KeyListener {

    private static final long serialVersionUID = 1L;

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    private static final int GAME_SECONDS = 60;
    private static final double INTERACT_DISTANCE = 60;

    private final Timer loop;
    private final Random rng = new Random();

    private Player player;
    private final ArrayList<Counter> counters = new ArrayList<>();
    private final ArrayList<Table> tables = new ArrayList<>();

    private int score;
    private double revenue;   // total pendapatan (dari subtotal OrderDetail)
    private int served;       // jumlah meja terlayani
    private long startTime;
    private int timeLeft;
    private boolean gameOver;

    // pesan singkat di layar (feedback aksi)
    private String flash = "";
    private long flashUntil;

    // daftar nama menu (>=5 char sesuai aturan class Menu)
    private static final String[] NAMA_MENU = {
            "Nasi Goreng", "Mie Ayam Bakso", "Es Teh Manis", "Ayam Geprek",
            "Soto Ayam Kudus", "Kopi Susu Aren", "Roti Bakar", "Sate Madura"
    };

    public GamePanel() {
        setPreferredSize(new java.awt.Dimension(WIDTH, HEIGHT));
        setBackground(new Color(0xFA, 0xF3, 0xE0));
        setFocusable(true);
        addKeyListener(this);

        setupLevel();

        startTime = System.currentTimeMillis();
        timeLeft = GAME_SECONDS;

        loop = new Timer(16, this); // ~60 FPS
        loop.start();
    }

    private void setupLevel() {
        player = new Player(WIDTH / 2 - 16, HEIGHT / 2 - 16, WIDTH, HEIGHT);

        // Buat beberapa menu berbeda untuk counter
        ArrayList<Menu> menuPool = new ArrayList<>();
        String[] dipakai = { NAMA_MENU[0], NAMA_MENU[1], NAMA_MENU[3], NAMA_MENU[4] };
        for (String nama : dipakai) {
            int harga = (5 + rng.nextInt(20)) * 1000; // 5.000 - 25.000
            menuPool.add(new Menu(nama, harga, 99));
        }

        // Counter di sisi atas
        int cx = 40;
        for (Menu menu : menuPool) {
            counters.add(new Counter(cx, 20, menu));
            cx += 110;
        }

        // Meja pelanggan tersebar di bawah, masing-masing minta menu acak
        int[][] pos = {
                { 80, 200 }, { 300, 220 }, { 560, 200 },
                { 160, 400 }, { 440, 420 }, { 640, 380 }
        };
        for (int i = 0; i < pos.length; i++) {
            Menu diminta = menuPool.get(rng.nextInt(menuPool.size()));
            // Pelanggan = objek Pembeli (model existing)
            Pembeli pelanggan = new Pembeli("Guest" + (i + 1), "pass1", "081234567890", 100000);
            tables.add(new Table(pos[i][0], pos[i][1], i + 1, pelanggan, diminta));
        }
    }

    // ------------------------------------------------------------------
    // Game loop
    // ------------------------------------------------------------------

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            player.update();

            timeLeft = GAME_SECONDS - (int) ((System.currentTimeMillis() - startTime) / 1000);
            if (timeLeft <= 0) {
                timeLeft = 0;
                gameOver = true;
            }
            if (served == tables.size()) {
                gameOver = true; // semua meja terlayani
            }
        }
        repaint();
    }

    /** Aksi ambil/antar saat SPACE ditekan. */
    private void interact() {
        if (gameOver) return;

        // 1) Kalau tangan kosong, coba ambil dari counter terdekat
        if (!player.isCarrying()) {
            for (Counter c : counters) {
                if (player.distanceTo(c) <= INTERACT_DISTANCE) {
                    player.pickUp(c.getMenu());
                    setFlash("Ambil: " + c.getMenu().getName());
                    return;
                }
            }
            setFlash("Dekati counter untuk ambil menu");
            return;
        }

        // 2) Kalau sedang membawa menu, coba antar ke meja terdekat
        for (Table t : tables) {
            if (!t.isServed() && player.distanceTo(t) <= INTERACT_DISTANCE) {
                Menu bawaan = player.getCarrying();
                if (t.serve(bawaan)) {
                    // Pembayaran memakai polymorphism: pelanggan bayar cash pas
                    double subtotal = t.getOrderSubtotal();
                    Payment payment = new CashPayment(subtotal);
                    if (payment.paymentProses(subtotal)) {
                        revenue += subtotal;
                    }
                    score += 100;
                    served++;
                    player.dropCarrying();
                    setFlash("Meja " + t.getTableNumber() + " terlayani! +100");
                } else {
                    setFlash("Menu salah untuk meja ini!");
                }
                return;
            }
        }
        setFlash("Dekati meja yang memesan menu ini");
    }

    private void setFlash(String msg) {
        flash = msg;
        flashUntil = System.currentTimeMillis() + 1500;
    }

    // ------------------------------------------------------------------
    // Rendering
    // ------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // area dapur (atas)
        g2.setColor(new Color(0xD5, 0xF5, 0xE3));
        g2.fillRect(0, 0, WIDTH, 90);

        for (Counter c : counters) c.draw(g2);
        for (Table t : tables) t.draw(g2);
        player.draw(g2);

        drawHud(g2);

        if (gameOver) {
            drawGameOver(g2);
        }
    }

    private void drawHud(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRoundRect(10, HEIGHT - 40, WIDTH - 20, 30, 10, 10);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("Skor: " + score
                + "    Terlayani: " + served + "/" + tables.size()
                + "    Pendapatan: Rp" + (int) revenue
                + "    Waktu: " + timeLeft + "s", 20, HEIGHT - 20);

        // pesan flash
        if (System.currentTimeMillis() < flashUntil) {
            g2.setColor(new Color(0x21, 0x2F, 0x3D));
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.drawString(flash, 20, 120);
        }
    }

    private void drawGameOver(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 190));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 40));
        String judul = (served == tables.size()) ? "SEMUA MEJA TERLAYANI!" : "WAKTU HABIS!";
        g2.drawString(judul, WIDTH / 2 - 210, HEIGHT / 2 - 40);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 22));
        g2.drawString("Skor akhir: " + score, WIDTH / 2 - 80, HEIGHT / 2 + 10);
        g2.drawString("Pendapatan: Rp" + (int) revenue, WIDTH / 2 - 90, HEIGHT / 2 + 45);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g2.drawString("Tekan R untuk main lagi", WIDTH / 2 - 90, HEIGHT / 2 + 90);
    }

    private void restart() {
        counters.clear();
        tables.clear();
        score = 0;
        revenue = 0;
        served = 0;
        gameOver = false;
        flash = "";
        setupLevel();
        startTime = System.currentTimeMillis();
        timeLeft = GAME_SECONDS;
    }

    // ------------------------------------------------------------------
    // Input keyboard
    // ------------------------------------------------------------------

    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        switch (k) {
            case KeyEvent.VK_W: case KeyEvent.VK_UP:    player.setUp(true); break;
            case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  player.setDown(true); break;
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  player.setLeft(true); break;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: player.setRight(true); break;
            case KeyEvent.VK_SPACE: interact(); break;
            case KeyEvent.VK_R: if (gameOver) restart(); break;
            default: break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        switch (k) {
            case KeyEvent.VK_W: case KeyEvent.VK_UP:    player.setUp(false); break;
            case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  player.setDown(false); break;
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  player.setLeft(false); break;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: player.setRight(false); break;
            default: break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // tidak dipakai
    }
}
