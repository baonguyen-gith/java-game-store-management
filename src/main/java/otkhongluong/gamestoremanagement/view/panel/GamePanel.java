package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.model.Game;
import otkhongluong.gamestoremanagement.controller.GameController;
import otkhongluong.gamestoremanagement.view.dialog.InvoiceAddDialog;
import otkhongluong.gamestoremanagement.view.dialog.RentAddDialog;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.ArrayList;
/**
 * GamePanel — danh sách game + điều hướng sang GameDetailPanel.
 *
 * GamePanel tự quản lý CardLayout nội bộ:
 *   "LIST"   → danh sách game
 *   "DETAIL" → chi tiết game (GameDetailPanel)
 *
 * Không cần thay đổi gì ở MainFrame / JTabbedPane.
 */
public class GamePanel extends JPanel {

    /* ── Palette ── */
    private static final Color BG_DARK     = new Color(20, 15, 50);
    private static final Color CARD_BG     = new Color(30, 25, 65);
    private static final Color CARD_HOVER  = new Color(40, 35, 85);
    private static final Color CARD_BORDER = new Color(60, 50, 110);
    private static final Color ACCENT      = new Color(130, 90, 230);
    private static final Color BTN_CD      = new Color(200, 50,  50);   // đỏ     — Mua CD
    private static final Color BTN_ROM     = new Color(180, 40,  40);   // đỏ đậm — Mua ROM
    private static final Color BTN_RENT    = new Color(45,  105, 200);  // xanh   — Thuê CD
    private static final Color TEXT_WHITE  = Color.WHITE;
    private static final Color TEXT_MUTED  = new Color(130, 120, 170);

    /* ── Fonts ── */
    private static final Font F_NAME   = new Font("Segoe UI", Font.BOLD, 17);
    private static final Font F_DESC   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font F_HEADER = new Font("Segoe UI", Font.BOLD, 26);

    /* ── CardLayout keys ── */
    private static final String CARD_LIST   = "LIST";
    private static final String CARD_DETAIL = "DETAIL";

    private final GameController gameController = new GameController();
    private final CardLayout   cardLayout  = new CardLayout();
    private JPanel             listPanel;
    private GameDetailPanel    detailPanel;
    private List<Game> allGames = new ArrayList<>();

    // ═══════════════════════════════════════════════════════════
    public GamePanel() {
        setLayout(cardLayout);
        setBackground(BG_DARK);

        add(buildListView(), CARD_LIST);

        detailPanel = new GameDetailPanel(this::showList);
        add(detailPanel, CARD_DETAIL);

        cardLayout.show(this, CARD_LIST);
        loadGames();
    }

    // ── Navigate ────────────────────────────────────────────────
    private void showDetail(Game game) {
        detailPanel.loadGame(game);
        cardLayout.show(this, CARD_DETAIL);
    }

    private void showList() {
        cardLayout.show(this, CARD_LIST);
    }

    // ═══════════════════════════════════════════════════════════
    // LIST VIEW
    // ═══════════════════════════════════════════════════════════
    private JPanel buildListView() {
        JPanel view = new JPanel(new BorderLayout());
        view.setBackground(BG_DARK);
        view.add(buildHeader(), BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(BG_DARK);
        listPanel.setBorder(new EmptyBorder(0, 16, 16, 16));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        view.add(scroll, BorderLayout.CENTER);
        return view;
    }

    private JPanel buildHeader() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG_DARK);

        JPanel inner = new JPanel(new BorderLayout());
        inner.setBackground(BG_DARK);
        inner.setBorder(new EmptyBorder(20, 20, 12, 20));

        JLabel title = new JLabel("GAME STORE");
        title.setFont(F_HEADER);
        title.setForeground(TEXT_WHITE);
        inner.add(title, BorderLayout.WEST);

        JPanel sep = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, ACCENT, getWidth(), 0,
                    new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 0));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sep.setPreferredSize(new Dimension(0, 2));
        sep.setBackground(BG_DARK);

        wrap.add(inner, BorderLayout.CENTER);
        wrap.add(sep,   BorderLayout.SOUTH);
        return wrap;
    }

    private void loadGames() {
        listPanel.removeAll();
        allGames = gameController.loadAllGames();
        for (Game g : allGames) {
            listPanel.add(Box.createVerticalStrut(10));
            listPanel.add(createGameCard(g));
        }
        listPanel.add(Box.createVerticalStrut(10));
        listPanel.revalidate();
        listPanel.repaint();
    }

    // ── GAME CARD ───────────────────────────────────────────────
    private JPanel createGameCard(Game game) {

        // Tính chiều cao card theo số nút thực tế
        // ── Tính chiều cao card theo số nút thực tế ───────────────
        int btnCount = 0;
        if (game.getGiaCD()       != null && game.getGiaCD()       > 0) btnCount++; // Mua CD
        if (game.getGiaROM()      != null && game.getGiaROM()      > 0) btnCount++; // Mua ROM
        if (game.getGiaThueNgay() != null && game.getGiaThueNgay() > 0) btnCount++; // Thuê CD  ← đúng điều kiện
        final int cardH = Math.max(130, 56 + btnCount * 48 + Math.max(0, btnCount - 1) * 6);

        JPanel card = new JPanel(new BorderLayout()) {
            private boolean hovered = false;
            {
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    @Override public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) {
                        Component src = SwingUtilities.getDeepestComponentAt(
                            GamePanel.this,
                            e.getXOnScreen() - GamePanel.this.getLocationOnScreen().x,
                            e.getYOnScreen() - GamePanel.this.getLocationOnScreen().y);
                        if (src instanceof JButton) return;
                        showDetail(game);
                    }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? CARD_HOVER : CARD_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(hovered ? ACCENT : CARD_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, getWidth()-1.2f, getHeight()-1.2f, 12, 12));
                g2.dispose();
            }
            @Override protected void paintChildren(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                super.paintChildren(g2);
                g2.dispose();
            }
        };
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, cardH));
        card.setPreferredSize(new Dimension(800, cardH));

        // ── Ảnh ──
        JLabel imgLabel = new JLabel();
        imgLabel.setPreferredSize(new Dimension(110, cardH));
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imgLabel.setBackground(new Color(15, 10, 40));
        imgLabel.setOpaque(true);
        ImageIcon icon = loadImage(game.getHinhAnh(), 110, cardH);
        if (icon != null) imgLabel.setIcon(icon);
        else { imgLabel.setText("NO IMG"); imgLabel.setForeground(TEXT_MUTED); imgLabel.setFont(F_DESC); }

        // ── Thông tin ──
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(14, 16, 14, 12));

        JLabel lblName = new JLabel(nvl(game.getTenGame()));
        lblName.setFont(F_NAME);
        lblName.setForeground(TEXT_WHITE);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);

        String badge = nvl(game.getTheLoai());
        JLabel lblBadge = new JLabel(badge.isEmpty() ? "" : "  " + badge + "  ");
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblBadge.setForeground(new Color(180, 160, 255));
        lblBadge.setOpaque(true);
        lblBadge.setBackground(new Color(80, 55, 160));
        lblBadge.setBorder(new EmptyBorder(2, 6, 2, 6));
        lblBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        String desc = nvl(game.getMoTa()).isEmpty() ? nvl(game.getGhiChu()) : nvl(game.getMoTa());
        JLabel lblDesc = new JLabel(
            "<html><body style='width:420px;color:#B4AFD2;font-size:11px'>" + desc + "</body></html>");
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(lblName);
        if (!badge.isEmpty()) { center.add(Box.createVerticalStrut(5)); center.add(lblBadge); }
        center.add(Box.createVerticalStrut(6));
        center.add(lblDesc);

        // ── Cột nút bên phải ──────────────────────────────────────
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);
        right.setBorder(new EmptyBorder(14, 8, 14, 14));

        boolean hasAnyBtn = false;

        // ── Mua CD — chỉ hiện khi GiaCD > 0 ──────────────────────
        if (game.getGiaCD() != null && game.getGiaCD() > 0) {
            JButton btnCD = makeActionButton("Mua CD", game.getGiaCDText(), BTN_CD);
            btnCD.addActionListener(e -> {
                Frame f = (Frame) SwingUtilities.getWindowAncestor(GamePanel.this);
                InvoiceAddDialog.openAndPreselectGame(f, game.getMaGame(), "CD");
            });
            right.add(btnCD);
            hasAnyBtn = true;
        }

        // ── Mua ROM — chỉ hiện khi GiaROM > 0 ────────────────────
        if (game.getGiaROM() != null && game.getGiaROM() > 0) {
            if (hasAnyBtn) right.add(Box.createVerticalStrut(6));
            JButton btnROM = makeActionButton("Mua ROM", game.getGiaROMText(), BTN_ROM);
            btnROM.addActionListener(e -> {
                Frame f = (Frame) SwingUtilities.getWindowAncestor(GamePanel.this);
                InvoiceAddDialog.openAndPreselectGame(f, game.getMaGame(), "ROM");
            });
            right.add(btnROM);
            hasAnyBtn = true;
        }

        // ── Thuê CD — chỉ hiện khi GiaThueNgay > 0 (TÁCH RIÊNG, KHÔNG dùng GiaCD) ──
        if (game.getGiaThueNgay() != null && game.getGiaThueNgay() > 0) {
            if (hasAnyBtn) right.add(Box.createVerticalStrut(6));
            JButton btnRent = makeActionButton("Thuê CD", game.getGiaThueText(), BTN_RENT);
            btnRent.addActionListener(e -> {
                Frame f = (Frame) SwingUtilities.getWindowAncestor(GamePanel.this);
                RentAddDialog.openAndPreselectByGameName(f, game.getTenGame());
            });
            right.add(btnRent);
            hasAnyBtn = true;
        }

        card.add(imgLabel, BorderLayout.WEST);
        card.add(center,   BorderLayout.CENTER);
        card.add(right,    BorderLayout.EAST);
        return card;
    }

    // ═══════════════════════════════════════════════════════════
    // ACTION BUTTON — dùng chung cho cả 3 nút
    // ═══════════════════════════════════════════════════════════
    /**
     * Nút hành động 2 dòng text:
     *   Dòng trên  — {@code label}    : tên hành động, Bold 10 (VD: "Mua CD", "Thuê CD")
     *   Dòng dưới  — {@code subLabel} : giá tiền / phụ chú, Bold 12
     *
     * Màu nền {@code bg} phân biệt 3 nút:
     *   Mua CD  → BTN_CD  (đỏ)
     *   Mua ROM → BTN_ROM (đỏ đậm)
     *   Thuê CD → BTN_RENT (xanh dương)
     */
    private JButton makeActionButton(String label, String subLabel, Color bg) {
        JButton btn = new JButton() {
            private boolean hov = false;
            {
                setFocusPainted(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Nền — sáng nhẹ khi hover
                g2.setColor(hov ? bg.brighter() : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));

                // Viền trắng mờ — nổi bật trên nền card tối
                g2.setColor(new Color(255, 255, 255, 55));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 8, 8));

                g2.setColor(Color.WHITE);

                // Dòng trên — nhãn hành động (nhỏ, mô tả)
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                FontMetrics fm1 = g2.getFontMetrics();
                int x1 = (getWidth() - fm1.stringWidth(label)) / 2;
                g2.drawString(label, x1, getHeight() / 2 - 3);

                // Dòng dưới — giá / phụ chú (lớn hơn, nổi bật)
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm2 = g2.getFontMetrics();
                int x2 = (getWidth() - fm2.stringWidth(subLabel)) / 2;
                g2.drawString(subLabel, x2, getHeight() / 2 + 13);

                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(88, 48));
        btn.setMaximumSize(new Dimension(88, 48));
        btn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        return btn;
    }

    // ── IMAGE LOADER ─────────────────────────────────────────────
    private ImageIcon loadImage(String path, int w, int h) {
        try {
            if (path == null || path.isBlank()) throw new Exception();
            java.net.URL url = getClass().getResource(path);
            if (url == null) throw new Exception();
            return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            try {
                java.net.URL def = getClass().getResource("/icons/no-image.png");
                if (def != null)
                    return new ImageIcon(new ImageIcon(def).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
            } catch (Exception ignored) {}
            return null;
        }
    }

    private String nvl(String s) { return s == null ? "" : s; }
    
        public void filterGames(String keyword) {
        listPanel.removeAll();

        List<Game> filtered = gameController.filterByKeyword(keyword);

        if (filtered.isEmpty()) {
            JLabel none = new JLabel("Không tìm thấy game nào phù hợp");
            none.setFont(F_DESC);
            none.setForeground(TEXT_MUTED);
            none.setBorder(new EmptyBorder(30, 20, 0, 0));
            listPanel.add(none);
        } else {
            for (Game g : filtered) {
                listPanel.add(Box.createVerticalStrut(10));
                listPanel.add(createGameCard(g));
            }
        }
        listPanel.add(Box.createVerticalStrut(10));
        listPanel.revalidate();
        listPanel.repaint();
    }
}