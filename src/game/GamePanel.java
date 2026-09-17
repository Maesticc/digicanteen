package game;

import java.awt.Color;
import java.awt.Cursor;
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
import java.awt.event.MouseMotionAdapter;
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
 * DigiCanteen Tycoon - game manajemen kantin berbasis mouse, versi beranimasi.
 *
 * Alur main (semua pakai KLIK MOUSE):
 *   1) Pelanggan meluncur masuk & mengantre, memesan sebuah Menu. Bar kesabaran
 *      terus menurun.
 *   2) Klik pelanggan MENUNGGU untuk mengambil pesanan -> masuk dapur (uap!).
 *   3) Dapur memasak (kompor terbatas). Saat matang, pelanggan ditandai "SIAP".
 *   4) Klik pelanggan SIAP untuk menyajikan -> bayar via metode acak
 *      (Eovo / Egopay / CashPayment = polymorphism Payment), muncul teks uang
 *      melayang + ledakan partikel.
 *   5) Kejar target pendapatan tiap level sebelum 3 pelanggan kabur.
 */
public class GamePanel extends JPanel implements ActionListener, KeyListener {

    private static final long serialVersionUID = 1L;

    public static final int WIDTH = 900;
    public static final int HEIGHT = 640;

    private static final int MAX_ANTREAN = 5;
    private static final int MAX_KABUR = 3;

    // Layout ala Restaurant City: header tipis, counter dapur di atas,
    // lantai ubin catur, antrean pelanggan sebagai KOLOM di kiri.
    private static final int HEADER_H = 54;      // bar judul + HUD
    private static final int COUNTER_H = 96;     // tinggi bar dapur/counter
    private static final int FLOOR_TOP = HEADER_H + COUNTER_H; // mulai lantai
    private static final int TILE = 64;          // ukuran ubin

    private static final int QUEUE_X = 24;       // kolom antrean di kiri
    private static final int QUEUE_START_Y = FLOOR_TOP + 60;
    private static final int QUEUE_GAP = 92;     // jarak vertikal antar pelanggan

    private final Random rng = new Random();
    private final Timer loop;
    private long lastTick;

    private Kitchen kitchen;
    private final ArrayList<Customer> customers = new ArrayList<>();
    private final ArrayList<Customer> ready = new ArrayList<>();
    private final ArrayList<FloatingText> floaters = new ArrayList<>();
    private final ArrayList<Particle> particles = new ArrayList<>();
    private ArrayList<Menu> menuList;

    private int level = 1;
    private double revenue;
    private double target;
    private int totalRevenue;
    private int served;
    private int kabur;
    private int score;

    private double spawnTimer;
    private double spawnInterval;
    private double patienceDrain;

    private boolean gameOver;
    private boolean levelClear;
    private double overlayFade;   // transisi overlay 0..1
    private double bgAnim;        // animasi background halus

    private int mouseX, mouseY;

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
            @Override public void mousePressed(MouseEvent e) { handleClick(e.getX(), e.getY()); }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
        });

        startLevel(1);
        lastTick = System.nanoTime();
        loop = new Timer(16, this);
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
        this.overlayFade = 0;
        customers.clear();
        ready.clear();
        floaters.clear();
        particles.clear();

        menuList = new ArrayList<>();
        String[] dipakai = { NAMA_MENU[0], NAMA_MENU[1], NAMA_MENU[3], NAMA_MENU[5] };
        for (String nama : dipakai) {
            int harga = (8 + rng.nextInt(18)) * 1000;
            menuList.add(new Menu(nama, harga, 999));
        }

        this.spawnInterval = Math.max(1.6, 4.0 - lvl * 0.35);
        this.patienceDrain = 5.0 + lvl * 1.5;
        this.spawnTimer = 1.0;
        int kompor = Math.min(4, 2 + lvl / 2);
        double cookTime = Math.max(1.5, 3.0 - lvl * 0.15);
        // panel status masak diletakkan di kanan area lantai
        this.kitchen = new Kitchen(WIDTH - 300, FLOOR_TOP + 20, 280, 240, kompor, cookTime);

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
        if (dt > 0.1) dt = 0.1; // hindari lompatan besar
        lastTick = now;

        bgAnim += dt;
        updateEffects(dt);
        updateHover();

        if (!gameOver && !levelClear) {
            update(dt);
        } else {
            overlayFade = Math.min(1.0, overlayFade + dt * 3);
        }
        repaint();
    }

    private void update(double dt) {
        spawnTimer -= dt;
        if (spawnTimer <= 0 && customers.size() < MAX_ANTREAN) {
            spawnCustomer();
            spawnTimer = spawnInterval;
        }

        Iterator<Customer> it = customers.iterator();
        while (it.hasNext()) {
            Customer c = it.next();
            c.update(dt);
            if (c.getState() == Customer.State.LEFT_ANGRY) {
                kabur++;
                ready.remove(c);
                spawnAngryEffect(c);
                setFlash(c.getPembeli().getUsername() + " kabur! (-1 nyawa)");
                it.remove();
                if (kabur >= MAX_KABUR) {
                    gameOver = true;
                }
            }
        }
        repositionQueue();

        ArrayList<Customer> selesai = kitchen.update(dt);
        for (Customer c : selesai) {
            if (c.getState() == Customer.State.ORDER_TAKEN) {
                ready.add(c);
                floaters.add(new FloatingText(c.getX(), c.getY() - 30, "Siap!", new Color(0x27, 0xAE, 0x60), 14));
            }
        }

        if (revenue >= target) {
            levelClear = true;
        }
    }

    private void updateEffects(double dt) {
        for (Iterator<FloatingText> it = floaters.iterator(); it.hasNext();) {
            FloatingText f = it.next();
            f.update(dt);
            if (f.isDead()) it.remove();
        }
        for (Iterator<Particle> it = particles.iterator(); it.hasNext();) {
            Particle p = it.next();
            p.update(dt);
            if (p.isDead()) it.remove();
        }
    }

    private void updateHover() {
        boolean anyHover = false;
        for (Customer c : customers) {
            boolean h = c.getBounds().contains(mouseX, mouseY) && c.isActive();
            c.setHovered(h);
            if (h) anyHover = true;
        }
        setCursor(Cursor.getPredefinedCursor(anyHover ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    private void spawnCustomer() {
        int slot = customers.size();
        int targetY = QUEUE_START_Y + slot * QUEUE_GAP;
        int spawnY = HEIGHT + 60; // meluncur masuk dari bawah kolom antrean
        Menu pesanan = menuList.get(rng.nextInt(menuList.size()));
        Pembeli p = new Pembeli("Guest" + (rng.nextInt(900) + 100), "pass1", "081234567890", 200000);
        customers.add(new Customer(QUEUE_X, spawnY, targetY, p, pesanan, patienceDrain));
    }

    private void repositionQueue() {
        for (int i = 0; i < customers.size(); i++) {
            customers.get(i).setTargetY(QUEUE_START_Y + i * QUEUE_GAP);
        }
    }

    // ------------------------------------------------------------------
    // Efek
    // ------------------------------------------------------------------

    private void spawnServeEffect(Customer c, double amount) {
        floaters.add(new FloatingText(c.getX() - 6, c.getY() - 20,
                "+Rp" + (int) amount, new Color(0x1E, 0x8B, 0x3A), 16));
        Color[] palette = {
                new Color(0xF1, 0xC4, 0x0F), new Color(0xF3, 0x9C, 0x12), new Color(0x2E, 0xCC, 0x71)
        };
        for (int i = 0; i < 18; i++) {
            particles.add(new Particle(c.centerX(), c.centerY(), palette[rng.nextInt(palette.length)]));
        }
    }

    private void spawnAngryEffect(Customer c) {
        floaters.add(new FloatingText(c.getX(), c.getY() - 20, "Kabur!", new Color(0xC0, 0x39, 0x2B), 15));
        for (int i = 0; i < 8; i++) {
            particles.add(new Particle(c.centerX(), c.centerY(), new Color(0xE7, 0x4C, 0x3C)));
        }
    }

    // ------------------------------------------------------------------
    // Interaksi mouse
    // ------------------------------------------------------------------

    private void handleClick(int mx, int my) {
        if (gameOver) {
            totalRevenue = 0; score = 0;
            startLevel(1);
            return;
        }
        if (levelClear) {
            startLevel(level + 1);
            return;
        }
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
                if (kitchen.isFull()) {
                    setFlash("Dapur penuh! Tunggu masakan selesai.");
                    floaters.add(new FloatingText(c.getX(), c.getY() - 20, "Dapur penuh", new Color(0xC0, 0x39, 0x2B), 13));
                    return;
                }
                c.takeOrder();
                kitchen.startCooking(c);
                floaters.add(new FloatingText(c.getX(), c.getY() - 20, "Dimasak!", new Color(0x2E, 0x86, 0xC1), 13));
                setFlash("Pesanan " + c.getOrder().getName() + " mulai dimasak");
                break;
            case ORDER_TAKEN:
                if (ready.contains(c)) serveCustomer(c);
                else setFlash("Masakan belum matang...");
                break;
            default:
                break;
        }
    }

    private void serveCustomer(Customer c) {
        c.serve();
        ready.remove(c);

        double harga = c.getOrder().getPrice();
        double bayar = harga * c.tipMultiplier();

        Payment payment = randomPayment(bayar);
        boolean ok = payment.paymentProses(bayar);
        if (ok) {
            revenue += bayar;
            totalRevenue += (int) bayar;
            served++;
            score += 100 + (int) ((c.tipMultiplier() - 1.0) * 100);
            spawnServeEffect(c, bayar);
            setFlash("Dibayar via " + payment.getClass().getSimpleName());
        } else {
            setFlash("Pembayaran gagal!");
        }
        customers.remove(c);
        repositionQueue();
    }

    private Payment randomPayment(double amount) {
        switch (rng.nextInt(3)) {
            case 0: return new Eovo("081234567890");
            case 1: return new Egopay("081234567890");
            default: return new CashPayment(amount);
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

        for (Customer c : customers) {
            c.draw(g2);
            if (ready.contains(c)) drawReadyMarker(g2, c);
        }

        for (Particle p : particles) p.draw(g2);
        for (FloatingText f : floaters) f.draw(g2);

        drawHud(g2);

        if (gameOver) drawOverlay(g2, "GAME OVER", "Terlalu banyak pelanggan kabur!", "Klik untuk main lagi");
        else if (levelClear) drawOverlay(g2, "LEVEL " + level + " SELESAI!", "Target tercapai!", "Klik untuk lanjut");
    }

    private void drawReadyMarker(Graphics2D g2, Customer c) {
        java.awt.Rectangle b = c.getBounds();
        boolean blink = (System.currentTimeMillis() / 300) % 2 == 0;
        g2.setColor(blink ? Color.WHITE : new Color(0x27, 0xAE, 0x60));
        g2.drawRoundRect(b.x - 4, b.y - 4, b.width + 8, b.height + 8, 14, 14);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.setColor(new Color(0x0E, 0x50, 0x33));
        g2.drawString("SIAP!", b.x + b.width - 4, b.y + b.height / 2);
    }

    private void drawBackground(Graphics2D g2) {
        // 1) LANTAI UBIN CATUR TEAL (ala Restaurant City)
        Color tileA = new Color(0x1F, 0x6E, 0x63); // teal gelap
        Color tileB = new Color(0x2A, 0x8C, 0x7F); // teal terang
        for (int gy = FLOOR_TOP; gy < HEIGHT; gy += TILE) {
            for (int gx = 0; gx < WIDTH; gx += TILE) {
                boolean even = (((gx / TILE) + (gy / TILE)) % 2 == 0);
                g2.setColor(even ? tileA : tileB);
                g2.fillRect(gx, gy, TILE, TILE);
            }
        }

        // 2) COUNTER DAPUR memanjang di atas (dinding + meja stainless + chef)
        drawKitchenBar(g2);

        // 3) HEADER tipis (judul)
        g2.setColor(new Color(0xE9, 0x76, 0x2A));
        g2.fillRect(0, 0, WIDTH, HEADER_H);
        g2.setColor(new Color(0xD3, 0x5C, 0x1D));
        g2.fillRect(0, HEADER_H - 4, WIDTH, 4);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        g2.drawString("DigiCanteen Tycoon", 16, 34);

        // 4) DEKOR KIRI: lorong masuk + tiang antrean + papan OPEN
        drawEntranceDecor(g2);
    }

    /** Bar dapur memanjang di bawah header: dinding, meja, chef, makanan pajangan. */
    private void drawKitchenBar(Graphics2D g2) {
        int top = HEADER_H;
        // dinding dapur
        g2.setColor(new Color(0x14, 0x4A, 0x43));
        g2.fillRect(0, top, WIDTH, COUNTER_H);
        // list kayu bawah counter
        g2.setColor(new Color(0x6E, 0x3F, 0x1E));
        g2.fillRect(0, top + COUNTER_H - 10, WIDTH, 10);

        // meja stainless counter
        g2.setColor(new Color(0xBD, 0xC3, 0xC7));
        g2.fillRect(0, top + COUNTER_H - 34, WIDTH, 24);
        g2.setColor(new Color(0x95, 0xA5, 0xA6));
        g2.fillRect(0, top + COUNTER_H - 14, WIDTH, 4);

        // pajangan makanan + chef berjejer
        for (int i = 0; i < 6; i++) {
            int sx = 40 + i * 150;
            // etalase makanan
            g2.setColor(new Color(0xE7, 0x4C, 0x3C));
            g2.fillRoundRect(sx, top + 30, 40, 20, 6, 6);
            g2.setColor(new Color(0xF5, 0xB0, 0x41));
            g2.fillOval(sx + 8, top + 20, 24, 16);
            // chef di belakang counter (kepala + topi)
            if (i % 2 == 0) drawChef(g2, sx + 80, top + 16);
        }
    }

    private void drawChef(Graphics2D g2, int x, int y) {
        // topi chef
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x - 2, y - 12, 24, 14, 8, 8);
        g2.fillRoundRect(x + 2, y - 6, 16, 12, 4, 4);
        // wajah
        g2.setColor(new Color(0xF5, 0xCB, 0xA7));
        g2.fillOval(x, y, 20, 20);
        // mata
        g2.setColor(new Color(0x2C, 0x2C, 0x2C));
        g2.fillOval(x + 5, y + 8, 3, 3);
        g2.fillOval(x + 12, y + 8, 3, 3);
    }

    /** Lorong masuk kiri: aspal, tiang antrean spiral merah-putih, papan OPEN. */
    private void drawEntranceDecor(Graphics2D g2) {
        // "trotoar" gelap di tepi kiri
        g2.setColor(new Color(0x3A, 0x3F, 0x44));
        g2.fillRect(0, FLOOR_TOP, 12, HEIGHT - FLOOR_TOP);

        // tiang antrean spiral merah-putih
        int px = QUEUE_X + 70;
        int py = FLOOR_TOP + 20;
        int ph = 150;
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(px, py, 16, ph, 8, 8);
        g2.setColor(new Color(0xE7, 0x4C, 0x3C));
        for (int s = 0; s < ph; s += 24) {
            g2.fillArc(px - 2, py + s, 20, 20, 0, 180);
        }
        g2.setColor(new Color(0x2C, 0x2C, 0x2C));
        g2.fillRect(px - 4, py + ph, 24, 6);

        // papan OPEN
        int ox = QUEUE_X + 50, oy = FLOOR_TOP + 190;
        g2.setColor(new Color(0x2C, 0x2C, 0x2C));
        g2.fillRoundRect(ox, oy, 70, 26, 6, 6);
        g2.setColor(new Color(0xF1, 0xC4, 0x0F));
        g2.fillRoundRect(ox + 4, oy + 4, 62, 18, 4, 4);
        g2.setColor(new Color(0xC0, 0x39, 0x2B));
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("OPEN", ox + 14, oy + 17);
    }

    private void drawHud(Graphics2D g2) {
        // semua HUD berada di dalam header (tinggi HEADER_H)
        // target pendapatan bar di tengah header
        int barX = 250, barY = 16, barW = 300, barH = 20;
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillRoundRect(barX, barY, barW, barH, 10, 10);
        double p = Math.min(1.0, revenue / target);
        g2.setColor(new Color(0x27, 0xAE, 0x60));
        g2.fillRoundRect(barX, barY, (int) (barW * p), barH, 10, 10);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.drawString("Rp" + (int) revenue + " / Rp" + (int) target, barX + 10, barY + 15);

        // level & skor
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("Lv " + level, barX + barW + 16, 24);
        g2.drawString("Skor " + score, barX + barW + 16, 42);

        // nyawa berbentuk hati (kanan atas)
        for (int i = 0; i < MAX_KABUR; i++) {
            boolean alive = i < (MAX_KABUR - kabur);
            g2.setColor(alive ? new Color(0xE7, 0x4C, 0x3C) : new Color(255, 255, 255, 90));
            int hx = WIDTH - 90 + i * 22, hy = 18;
            g2.fillOval(hx, hy, 9, 9);
            g2.fillOval(hx + 7, hy, 9, 9);
            int[] xs = { hx, hx + 14, hx + 7 };
            int[] ys = { hy + 6, hy + 6, hy + 16 };
            g2.fillPolygon(xs, ys, 3);
        }

        // flash message (di area lantai, bawah counter)
        if (System.currentTimeMillis() < flashUntil) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            String msg = flash;
            int tw = g2.getFontMetrics().stringWidth(msg);
            int fx = WIDTH / 2 - tw / 2;
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(fx - 12, FLOOR_TOP + 6, tw + 24, 24, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawString(msg, fx, FLOOR_TOP + 23);
        }

        // petunjuk di bawah
        g2.setColor(new Color(0xEC, 0xF0, 0xF1));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString("Klik pelanggan untuk ambil pesanan  •  klik lagi saat SIAP untuk sajikan & terima bayaran",
                QUEUE_X, HEIGHT - 14);
    }

    private void drawOverlay(Graphics2D g2, String title, String sub, String hint) {
        int alpha = (int) (190 * overlayFade);
        g2.setColor(new Color(0, 0, 0, alpha));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        int textAlpha = (int) (255 * overlayFade);
        g2.setColor(new Color(255, 255, 255, textAlpha));
        g2.setFont(new Font("SansSerif", Font.BOLD, 44));
        g2.drawString(title, WIDTH / 2 - g2.getFontMetrics().stringWidth(title) / 2, HEIGHT / 2 - 40);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 20));
        g2.drawString(sub, WIDTH / 2 - g2.getFontMetrics().stringWidth(sub) / 2, HEIGHT / 2 + 4);
        String tot = "Total pendapatan: Rp" + totalRevenue;
        g2.drawString(tot, WIDTH / 2 - g2.getFontMetrics().stringWidth(tot) / 2, HEIGHT / 2 + 36);

        boolean blink = (System.currentTimeMillis() / 500) % 2 == 0;
        if (blink) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.drawString(hint, WIDTH / 2 - g2.getFontMetrics().stringWidth(hint) / 2, HEIGHT / 2 + 84);
        }
    }

    // ------------------------------------------------------------------
    // Keyboard
    // ------------------------------------------------------------------

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_R && gameOver) {
            totalRevenue = 0; score = 0;
            startLevel(1);
        }
    }

    @Override public void keyReleased(KeyEvent e) { }
    @Override public void keyTyped(KeyEvent e) { }
}
