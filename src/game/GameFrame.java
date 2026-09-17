package game;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * GameFrame = jendela utama game "DigiCanteen Rush".
 * Jalankan class ini (method main) untuk memulai game.
 */
public class GameFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    public GameFrame() {
        setTitle("DigiCanteen Rush");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel panel = new GamePanel();
        add(panel);
        pack();

        setLocationRelativeTo(null); // tampil di tengah layar
        panel.requestFocusInWindow(); // agar keyboard langsung aktif
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameFrame frame = new GameFrame();
            frame.setVisible(true);
        });
    }
}
