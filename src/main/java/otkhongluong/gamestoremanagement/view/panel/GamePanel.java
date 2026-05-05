package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.model.Game;
import otkhongluong.gamestoremanagement.service.GameService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GamePanel extends JPanel {

    private final GameService gameService = new GameService();
    private JPanel listPanel;

    public GamePanel() {

        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 50));

        JLabel title = new JLabel("GAME STORE");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(title, BorderLayout.NORTH);

        // ===== LIST PANEL (IMPORTANT) =====
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(20, 20, 50));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(20, 20, 50));

        add(scroll, BorderLayout.CENTER);

        loadGames();
    }

    // ================= LOAD =================
    private void loadGames() {

        listPanel.removeAll();

        List<Game> games = gameService.getAllGames();

        for (Game g : games) {
            listPanel.add(createGameRow(g));
            listPanel.add(Box.createVerticalStrut(15));
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    // ================= ROW UI =================
    private JPanel createGameRow(Game game) {

        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(new Color(45, 48, 70));
        row.setPreferredSize(new Dimension(1000, 140));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        row.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== IMAGE =====
        JLabel img = new JLabel();
        img.setPreferredSize(new Dimension(120, 120));

        try {
            img.setIcon(loadImage(game.getHinhAnh()));
        } catch (Exception e) {
            img.setText("NO IMAGE");
            img.setForeground(Color.WHITE);
        }

        // ===== INFO =====
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JLabel name = new JLabel(game.getTenGame());
        name.setForeground(Color.WHITE);
        name.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel desc = new JLabel(game.getGhiChu());
        desc.setForeground(Color.LIGHT_GRAY);

        center.add(name);
        center.add(Box.createVerticalStrut(8));
        center.add(desc);

        // ===== BUTTON AREA =====
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);

        // ===== CD BUTTON =====
        if (game.getGiaCD() != null && game.getGiaCD() > 0) {

            JButton btnCD =
                new JButton("THUÊ CD • " + game.getGiaCDText());

            styleButton(btnCD, Color.RED);

            btnCD.addActionListener(e -> {
                JOptionPane.showMessageDialog(
                    this,
                    "Mua CD: " + game.getTenGame()
                );
            });

            right.add(btnCD);
            right.add(Box.createVerticalStrut(10));
        }

        // ===== ROM BUTTON =====
        if (game.getGiaROM() != null && game.getGiaROM() > 0) {

            JButton btnROM =
                new JButton("MUA NGAY • " + game.getGiaROMText());

            styleButton(btnROM, new Color(255, 80, 80));

            btnROM.addActionListener(e -> {
                JOptionPane.showMessageDialog(
                    this,
                    "Mua ROM: " + game.getTenGame()
                );
            });

            right.add(btnROM);
        }

        row.add(img, BorderLayout.WEST);
        row.add(center, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);

        return row;
    }

    // ================= BUTTON STYLE =================
    private void styleButton(JButton btn, Color color) {

        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private ImageIcon loadImage(String path) {

        try {

            if (path == null || path.isBlank())
                throw new Exception();

            java.net.URL imgURL = getClass().getResource(path);

            if (imgURL == null)
                throw new Exception();

            ImageIcon icon = new ImageIcon(imgURL);

            Image scaled =
                    icon.getImage()
                            .getScaledInstance(120,120,Image.SCALE_SMOOTH);

            return new ImageIcon(scaled);

        } catch (Exception e) {

            // fallback image
            java.net.URL defaultImg =
                    getClass().getResource("/icons/no-image.png");

            return new ImageIcon(defaultImg);
        }
    }
}