package game;

import java.awt.BasicStroke;
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
import java.util.List;
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
 * DIGICANTEEN OVERCOOKED - game memasak bergaya Overcooked.
 *
 * Cara main:
 *   - Gerak: W A S D atau tombol panah
 *   - Aksi : SPACE (atau E) pada perabot terdekat (yang tersorot kuning)
 *
 * Alur memasak:
 *   1) Ambil bahan mentah dari PETI BAHAN
 *   2) Taruh di TALENAN sampai terpotong, lalu ambil lagi
 *   3) Masukkan bahan terpotong ke PANCI (boleh 1-2 bahan sesuai resep)
 *   4) Tunggu matang, ambil masakannya
 *   5) Antar ke JENDELA SAJI yang cocok dengan tiket pesanan di atas layar
 *
 * Tiket pesanan punya batas waktu. Kalau kedaluwarsa, nyawa berkurang.
 * Capai target pendapatan sebelum waktu level habis.
 *
 * Class OOP existing yang dipakai: model.Menu (resep), model.Pembeli
 * (pemesan), model.OrderDetail (catatan pesanan), dan model.Payment beserta
 * turunannya Eovo / Egopay / CashPayment (polymorphism pembayaran).
 */
public class GamePanel extends JPanel implements ActionListener, KeyListener {

    private static final long serialVersionUID = 1L;

    public static final int WIDTH = 900;
    public static final int HEIGHT = 640;

    private static final int HEADER_H = 56;   // bar judul + HUD
    private static final int TICKET_H = 80;   // papan tiket pesanan
    private static final int FLOOR_TOP = HEADER_H + TICKET_H;
    private static final int TILE = 64;

    private static final int MAX_ORDERS = 4;
    private static final int MAX_LIVES = 3;
    private static final double LEVEL_TIME = 110;

    private final Random rng = new Random();
    private final Timer loop;
    private long lastTick;

    // dunia game
    private Chef chef;
    private final List<Station> stations = new ArrayList<>();
    private final List<Recipe> recipes = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();
    private ServeStation serveStation;

    // efek
    private final List<FloatingText> floaters = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();

    // status permainan
    private int level = 1;
    private double revenue;
    private double target;
    private int totalRevenue;
    private int served;
    private int lives = MAX_LIVES;
    private int score;

    private double timeLeft;
    private double orderTimer;
    private double orderInterval;
    private double orderLife;

    private boolean gameOver;
    private boolean levelClear;
    private double overlayFade;

    private boolean actionDown; // supaya SPACE tidak berulang saat ditahan

    private String flash = "";
    private long flashUntil;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(0x2B, 0x2F, 0x36));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gameOver) restartGame();
                else if (levelClear) startLevel(level + 1);
            }
        });

        startLevel(1);

        lastTick = System.nanoTime();
        loop = new Timer(16, this);
        loop.start();
    }

    // ------------------------------------------------------------------
    // Setup
    // ------------------------------------------------------------------

    private void restartGame() {
        totalRevenue = 0;
        score = 0;
        startLevel(1);
    }

    private void startLevel(int lvl) {
        this.level = lvl;
        this.revenue = 0;
        this.served = 0;
        this.lives = MAX_LIVES;
        this.gameOver = false;
        this.levelClear = false;
        this.overlayFade = 0;
        this.timeLeft = LEVEL_TIME;

        orders.clear();
        floaters.clear();
        particles.clear();
        stations.clear();
        recipes.clear();

        buildRecipes();
        buildStations(lvl);

        chef = new Chef(430, 330);

        this.orderInterval = Math.max(3.5, 7.5 - lvl * 0.5);
        this.orderLife = Math.max(20, 38 - lvl * 2);
        this.orderTimer = 1.5;
        this.target = 55000 + (lvl - 1) * 35000;

        flash("LEVEL " + lvl + " - Target Rp" + (int) target);
    }

    /** Resep kantin: memakai model.Menu untuk nama & harga. */
    private void buildRecipes() {
        recipes.add(new Recipe(new Menu("Nasi Putih", 9000, 999),
                IngredientType.NASI));
        recipes.add(new Recipe(new Menu("Nasi Goreng", 16000, 999),
                IngredientType.NASI, IngredientType.TELUR));
        recipes.add(new Recipe(new Menu("Mie Ayam Spesial", 19000, 999),
                IngredientType.MIE, IngredientType.AYAM));
        recipes.add(new Recipe(new Menu("Sop Sayur Ayam", 21000, 999),
                IngredientType.SAYUR, IngredientType.AYAM));
        recipes.add(new Recipe(new Menu("Capcay Telur", 17000, 999),
                IngredientType.SAYUR, IngredientType.TELUR));
    }

    /** Tata letak dapur: peti bahan & talenan di atas, panci di bawah. */
    private void buildStations(int lvl) {
        int topY = FLOOR_TOP + 14;

        // peti bahan (5 jenis)
        IngredientType[] bahan = IngredientType.values();
        for (int i = 0; i < bahan.length; i++) {
            stations.add(new CrateStation(20 + i * 96, topY, 86, 56, bahan[i]));
        }

        // talenan (2)
        stations.add(new ChopStation(520, topY, 100, 56));
        stations.add(new ChopStation(632, topY, 100, 56));

        // jendela saji (kanan bawah supaya ada jarak tempuh)
        serveStation = new ServeStation(756, 470, 120, 64);
        stations.add(serveStation);

        // panci (masak makin cepat di level tinggi)
        double cookTime = Math.max(4.0, 6.5 - lvl * 0.25);
        stations.add(new PotStation(330, 470, 104, 64, recipes, cookTime));
        stations.add(new PotStation(460, 470, 104, 64, recipes, cookTime));

        // tempat sampah
        stations.add(new TrashStation(60, 470, 86, 64));
    }

    // ------------------------------------------------------------------
    // Game loop
    // ------------------------------------------------------------------

    @Override
    public void actionPerformed(ActionEvent e) {
        long now = System.nanoTime();
        double dt = (now - lastTick) / 1_000_000_000.0;
        if (dt > 0.1) dt = 0.1;
        lastTick = now;

        updateEffects(dt);

        if (!gameOver && !levelClear) {
            update(dt);
        } else {
            overlayFade = Math.min(1.0, overlayFade + dt * 3);
        }
        repaint();
    }

    private void update(double dt) {
        // waktu level
        timeLeft -= dt;
        if (timeLeft <= 0) {
            timeLeft = 0;
            if (revenue >= target) levelClear = true;
            else gameOver = true;
            return;
        }

        // chef & perabot
        chef.update(dt, stations, 6, FLOOR_TOP + 6, WIDTH - 6, HEIGHT - 6);
        for (Station s : stations) {
            s.update(dt);
        }

        // sorot perabot terdekat
        Station targetSt = targetStation();
        for (Station s : stations) {
            s.setHighlighted(s == targetSt);
        }

        // tiket pesanan baru
        orderTimer -= dt;
        if (orderTimer <= 0 && orders.size() < MAX_ORDERS) {
            spawnOrder();
            orderTimer = orderInterval;
        }

        // hitung waktu tiket & buang yang kedaluwarsa
        Iterator<Order> it = orders.iterator();
        while (it.hasNext()) {
            Order o = it.next();
            o.update(dt);
            if (o.isExpired()) {
                lives--;
                flash("Pesanan " + o.getRecipe().getName() + " kedaluwarsa!");
                floaters.add(new FloatingText(WIDTH / 2 - 40, FLOOR_TOP + 40,
                        "Pesanan hangus!", new Color(0xC0, 0x39, 0x2B), 16));
                it.remove();
                if (lives <= 0) {
                    gameOver = true;
                    return;
                }
            }
        }
    }

    private void spawnOrder() {
        Recipe r = recipes.get(rng.nextInt(recipes.size()));
        Pembeli p = new Pembeli("Guest" + (rng.nextInt(900) + 100),
                "pass1", "081234567890", 200000);
        orders.add(new Order(r, p, orderLife));
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

    // ------------------------------------------------------------------
    // Interaksi
    // ------------------------------------------------------------------

    /** Perabot yang sedang dituju chef (di depannya atau paling dekat). */
    private Station targetStation() {
        Station best = null;
        double bestD = Double.MAX_VALUE;
        for (Station s : stations) {
            if (s.getBounds().contains(chef.reachX(), chef.reachY())) {
                return s;
            }
            double d = chef.distanceTo(s);
            if (d < bestD) {
                bestD = d;
                best = s;
            }
        }
        return bestD <= 76 ? best : null;
    }

    private void doAction() {
        if (gameOver || levelClear) {
            return;
        }
        Station s = targetStation();
        if (s == null) {
            flash("Dekati peti / talenan / panci dulu");
            return;
        }
        s.interact(chef, this);
    }

    /**
     * Coba sajikan masakan. Dipanggil oleh ServeStation.
     * Mengembalikan true kalau ada tiket yang cocok (masakan diterima).
     */
    public boolean tryServe(Dish dish, Station at) {
        Order best = null;
        for (Order o : orders) {
            if (o.isFinished()) continue;
            if (!o.getRecipe().getName().equals(dish.getRecipe().getName())) continue;
            if (best == null || o.getTimeLeft() < best.getTimeLeft()) {
                best = o;
            }
        }

        if (best == null) {
            flash("Tidak ada pesanan " + dish.getRecipe().getName());
            return false;
        }

        double bayar = best.getRecipe().getPrice() * best.tipMultiplier();

        // POLYMORPHISM: metode bayar acak, semua bertipe Payment
        Payment payment = randomPayment(bayar);
        if (payment.paymentProses(bayar)) {
            revenue += bayar;
            totalRevenue += (int) bayar;
            served++;
            score += 100 + (int) ((best.tipMultiplier() - 1.0) * 200);
            flash("Terjual! " + dish.getRecipe().getName()
                    + " via " + payment.getClass().getSimpleName());
        } else {
            flash("Pembayaran gagal!");
        }

        best.complete();          // catat sebagai model.OrderDetail
        orders.remove(best);

        // efek di jendela saji
        floaters.add(new FloatingText(at.centerX() - 24, at.getY() - 10,
                "+Rp" + (int) bayar, new Color(0x1E, 0x8B, 0x3A), 16));
        Color[] pal = { new Color(0xF1, 0xC4, 0x0F), new Color(0xF3, 0x9C, 0x12),
                new Color(0x2E, 0xCC, 0x71) };
        for (int i = 0; i < 18; i++) {
            particles.add(new Particle(at.centerX(), at.centerY(),
                    pal[rng.nextInt(pal.length)]));
        }
        return true;
    }

    private Payment randomPayment(double amount) {
        switch (rng.nextInt(3)) {
            case 0: return new Eovo("081234567890");
            case 1: return new Egopay("081234567890");
            default: return new CashPayment(amount);
        }
    }

    /** Pesan singkat di layar (dipakai juga oleh Station). */
    public void flash(String msg) {
        this.flash = msg;
        this.flashUntil = System.currentTimeMillis() + 1600;
    }

    // ------------------------------------------------------------------
    // Rendering
    // ------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawFloor(g2);

        for (Station s : stations) {
            s.draw(g2);
        }
        chef.draw(g2);

        for (Particle p : particles) p.draw(g2);
        for (FloatingText f : floaters) f.draw(g2);

        drawTickets(g2);
        drawHeader(g2);
        drawHints(g2);

        if (gameOver) {
            drawOverlay(g2, "GAME OVER",
                    revenue >= target ? "Nyawa habis!" : "Target belum tercapai",
                    "Klik / tekan R untuk main lagi");
        } else if (levelClear) {
            drawOverlay(g2, "LEVEL " + level + " SELESAI!",
                    "Pendapatan Rp" + (int) revenue + " dari target Rp" + (int) target,
                    "Klik untuk lanjut ke level " + (level + 1));
        }
    }

    private void drawFloor(Graphics2D g2) {
        Color a = new Color(0x3E, 0x44, 0x4C);
        Color b = new Color(0x47, 0x4E, 0x57);
        for (int gy = FLOOR_TOP; gy < HEIGHT; gy += TILE) {
            for (int gx = 0; gx < WIDTH; gx += TILE) {
                g2.setColor((((gx / TILE) + (gy / TILE)) % 2 == 0) ? a : b);
                g2.fillRect(gx, gy, TILE, TILE);
            }
        }
        // garis pemisah dapur
        g2.setColor(new Color(0x2B, 0x2F, 0x36));
        g2.fillRect(0, FLOOR_TOP, WIDTH, 4);
    }

    /** Papan tiket pesanan di bagian atas (seperti Overcooked). */
    private void drawTickets(Graphics2D g2) {
        g2.setColor(new Color(0x21, 0x25, 0x2B));
        g2.fillRect(0, HEADER_H, WIDTH, TICKET_H);
        g2.setColor(new Color(0x15, 0x18, 0x1C));
        g2.fillRect(0, HEADER_H + TICKET_H - 3, WIDTH, 3);

        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.setColor(new Color(0x9E, 0xA7, 0xB0));
        g2.drawString("PESANAN", 14, HEADER_H + 16);

        int cardW = 168, cardH = 60;
        for (int i = 0; i < orders.size(); i++) {
            Order o = orders.get(i);
            int cx = 84 + i * 178;
            int cy = HEADER_H + 12;
            drawTicket(g2, o, cx, cy, cardW, cardH);
        }
    }

    private void drawTicket(Graphics2D g2, Order o, int x, int y, int w, int h) {
        double ratio = o.timeRatio();
        boolean urgent = ratio < 0.28;
        boolean blink = (System.currentTimeMillis() / 250) % 2 == 0;

        // kertas tiket
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillRoundRect(x + 2, y + 3, w, h, 10, 10);
        g2.setColor(urgent && blink ? new Color(0xFF, 0xE0, 0xDB) : new Color(0xFD, 0xF5, 0xE2));
        g2.fillRoundRect(x, y, w, h, 10, 10);
        g2.setColor(new Color(0xD9, 0xC8, 0xA1));
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(x, y, w, h, 10, 10);

        // nama masakan
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.setColor(new Color(0x3A, 0x2E, 0x1A));
        g2.drawString(o.getRecipe().getName(), x + 8, y + 17);

        // ikon bahan yang dibutuhkan
        List<IngredientType> need = o.getRecipe().getNeed();
        for (int i = 0; i < need.size(); i++) {
            IngredientType t = need.get(i);
            int ix = x + 14 + i * 40;
            int iy = y + 33;
            g2.setColor(t.getColor());
            g2.fillOval(ix - 8, iy - 8, 16, 16);
            g2.setColor(t.getColor().darker());
            g2.drawOval(ix - 8, iy - 8, 16, 16);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            g2.setColor(new Color(0x5B, 0x4B, 0x2E));
            g2.drawString(t.getLabel(), ix + 11, iy + 3);
        }

        // harga
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.setColor(new Color(0x1E, 0x8B, 0x3A));
        String hrg = "Rp" + (int) o.getRecipe().getPrice();
        g2.drawString(hrg, x + w - 8 - g2.getFontMetrics().stringWidth(hrg), y + 17);

        // bar waktu
        int barX = x + 8, barY = y + h - 12, barW = w - 16;
        g2.setColor(new Color(0xE0, 0xD6, 0xBC));
        g2.fillRoundRect(barX, barY, barW, 6, 3, 3);
        Color tc = ratio > 0.55 ? new Color(0x2E, 0xCC, 0x71)
                : ratio > 0.28 ? new Color(0xF1, 0xC4, 0x0F)
                : new Color(0xE7, 0x4C, 0x3C);
        g2.setColor(tc);
        g2.fillRoundRect(barX, barY, (int) (barW * ratio), 6, 3, 3);
    }

    private void drawHeader(Graphics2D g2) {
        g2.setColor(new Color(0xE9, 0x76, 0x2A));
        g2.fillRect(0, 0, WIDTH, HEADER_H);
        g2.setColor(new Color(0xC7, 0x5B, 0x17));
        g2.fillRect(0, HEADER_H - 4, WIDTH, 4);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 20));
        g2.drawString("DigiCanteen Overcooked", 14, 35);

        // bar target pendapatan
        int barX = 258, barY = 15, barW = 210, barH = 20;
        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillRoundRect(barX, barY, barW, barH, 10, 10);
        double p = Math.min(1.0, revenue / target);
        g2.setColor(new Color(0x27, 0xAE, 0x60));
        g2.fillRoundRect(barX, barY, (int) (barW * p), barH, 10, 10);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.drawString("Rp" + (int) revenue + " / " + (int) target, barX + 8, barY + 14);

        // waktu
        int menit = (int) timeLeft / 60;
        int detik = (int) timeLeft % 60;
        boolean lowTime = timeLeft <= 15;
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.setColor(lowTime && (System.currentTimeMillis() / 300) % 2 == 0
                ? new Color(0xFF, 0xE0, 0x66) : Color.WHITE);
        g2.drawString(String.format("%02d:%02d", menit, detik), 488, 35);

        // level & skor
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString("Lv " + level, 566, 22);
        g2.drawString("Skor " + score, 566, 40);
        g2.drawString("Terjual " + served, 646, 22);
        g2.drawString("Total Rp" + totalRevenue, 646, 40);

        // nyawa
        for (int i = 0; i < MAX_LIVES; i++) {
            boolean alive = i < lives;
            g2.setColor(alive ? new Color(0xE7, 0x4C, 0x3C) : new Color(255, 255, 255, 80));
            int hx = WIDTH - 86 + i * 24, hy = 18;
            g2.fillOval(hx, hy, 10, 10);
            g2.fillOval(hx + 8, hy, 10, 10);
            int[] xs = { hx, hx + 16, hx + 8 };
            int[] ys = { hy + 7, hy + 7, hy + 18 };
            g2.fillPolygon(xs, ys, 3);
        }
    }

    private void drawHints(Graphics2D g2) {
        // pesan aksi
        if (System.currentTimeMillis() < flashUntil) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            int tw = g2.getFontMetrics().stringWidth(flash);
            int fx = WIDTH / 2 - tw / 2;
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRoundRect(fx - 12, FLOOR_TOP + 10, tw + 24, 26, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawString(flash, fx, FLOOR_TOP + 28);
        }

        // kontrol
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(new Color(0xC8, 0xCF, 0xD6));
        g2.drawString("WASD / panah = jalan   •   SPACE = ambil / taruh / masak / antar", 14, HEIGHT - 12);
    }

    private void drawOverlay(Graphics2D g2, String title, String sub, String hint) {
        g2.setColor(new Color(0, 0, 0, (int) (195 * overlayFade)));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        int alpha = (int) (255 * overlayFade);
        g2.setColor(new Color(255, 255, 255, alpha));
        g2.setFont(new Font("SansSerif", Font.BOLD, 42));
        g2.drawString(title, WIDTH / 2 - g2.getFontMetrics().stringWidth(title) / 2, HEIGHT / 2 - 40);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 19));
        g2.drawString(sub, WIDTH / 2 - g2.getFontMetrics().stringWidth(sub) / 2, HEIGHT / 2 + 2);
        String tot = "Total pendapatan: Rp" + totalRevenue + "   |   Skor: " + score;
        g2.drawString(tot, WIDTH / 2 - g2.getFontMetrics().stringWidth(tot) / 2, HEIGHT / 2 + 32);

        if ((System.currentTimeMillis() / 500) % 2 == 0) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.drawString(hint, WIDTH / 2 - g2.getFontMetrics().stringWidth(hint) / 2, HEIGHT / 2 + 80);
        }
    }

    // ------------------------------------------------------------------
    // Input
    // ------------------------------------------------------------------

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W: case KeyEvent.VK_UP:    chef.setUp(true); break;
            case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  chef.setDown(true); break;
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  chef.setLeft(true); break;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: chef.setRight(true); break;
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_E:
                if (!actionDown) {
                    actionDown = true;
                    doAction();
                }
                break;
            case KeyEvent.VK_R:
                if (gameOver) restartGame();
                break;
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W: case KeyEvent.VK_UP:    chef.setUp(false); break;
            case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  chef.setDown(false); break;
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  chef.setLeft(false); break;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: chef.setRight(false); break;
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_E:
                actionDown = false;
                break;
            default:
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // tidak dipakai
    }
}
