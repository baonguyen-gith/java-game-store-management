package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.model.Game;
import otkhongluong.gamestoremanagement.view.dialog.BillAddDialog;
import otkhongluong.gamestoremanagement.view.dialog.RentAddDialog;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;

/**
 * GameDetailPanel — hiển thị chi tiết 1 game.
 * Được nhúng vào CardLayout cùng với GamePanel.
 * Gọi onBack.run() để quay về danh sách.
 */
public class GameDetailPanel extends JPanel {

    /* ── Palette ── */
    private static final Color BG_DARK     = new Color(20, 15, 50);
    private static final Color BG_CARD     = new Color(28, 22, 62);
    private static final Color ACCENT      = new Color(130, 90, 230);
    private static final Color ACCENT_L    = new Color(170, 140, 255);
    private static final Color GREEN_ACC   = new Color(80, 220, 150);
    private static final Color BTN_CD      = new Color(190, 40, 40);
    private static final Color BTN_ROM     = new Color(160, 35, 35);
    private static final Color BTN_RENT    = new Color(140, 30, 30);
    private static final Color TEXT_WHITE  = Color.WHITE;
    private static final Color TEXT_GRAY   = new Color(190, 185, 220);
    private static final Color TEXT_MUTED  = new Color(120, 110, 160);
    private static final Color LABEL_COLOR = new Color(100, 90, 140);
    private static final Color VALUE_ACCENT= new Color(150, 220, 140);

    /* ── Fonts ── */
    private static final Font F_BACK    = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font F_TITLE   = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font F_DESC    = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font F_LABEL   = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font F_VALUE   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font F_BTN_TOP = new Font("Segoe UI", Font.BOLD, 10);
    private static final Font F_BTN_BOT = new Font("Segoe UI", Font.BOLD, 14);

    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /* ── State ── */
    private Game     game;
    private Runnable onBack;   // callback → quay lại GamePanel

    /* ── Components ── */
    private JLabel    imgLabel;
    private JPanel    btnOverlay;
    private JLabel    lblTitle;
    private JLabel    lblDesc;
    private JPanel    infoGrid;

    // ═══════════════════════════════════════════════════════════
    /**
     * @param onBack  Runnable được gọi khi nhấn nút "← Quay lại"
     */
    public GameDetailPanel(Runnable onBack) {
        this.onBack = onBack;
        setLayout(new BorderLayout());
        setBackground(BG_DARK);

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildContent(),   BorderLayout.CENTER);
    }

    // ── PUBLIC: nạp game mới ────────────────────────────────────
    public void loadGame(Game g) {
        this.game = g;
        refresh();
    }

    // ═══════════════════════════════════════════════════════════
    // TOP BAR: nút ← Quay lại
    // ═══════════════════════════════════════════════════════════
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setBackground(BG_DARK);
        bar.setBorder(new EmptyBorder(14, 16, 6, 16));

        JButton btnBack = new JButton("←  Quay lại danh sách") {
            private boolean hov = false;
            {
                setFocusPainted(false); setContentAreaFilled(false);
                setBorderPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                });
                addActionListener(e -> { if (onBack != null) onBack.run(); });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? new Color(60, 45, 120) : new Color(45, 32, 95));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(ACCENT_L);
                g2.setFont(F_BACK);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnBack.setPreferredSize(new Dimension(190, 34));
        btnBack.setForeground(ACCENT_L);

        bar.add(btnBack);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════
    // CONTENT
    // ═══════════════════════════════════════════════════════════
    private JScrollPane buildContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_DARK);
        content.setBorder(new EmptyBorder(4, 16, 20, 16));

        // ── Banner image + button overlay ──
        content.add(buildImageSection());
        content.add(Box.createVerticalStrut(14));

        // ── Title ──
        lblTitle = new JLabel("—");
        lblTitle.setFont(F_TITLE);
        lblTitle.setForeground(TEXT_WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblTitle);
        content.add(Box.createVerticalStrut(8));

        // ── Description ──
        lblDesc = makeHtmlLabel("", 580);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblDesc);
        content.add(Box.createVerticalStrut(16));

        // ── Divider ──
        content.add(makeDivider());
        content.add(Box.createVerticalStrut(12));

        // ── Info grid ──
        infoGrid = new JPanel(new GridLayout(0, 2, 0, 4));
        infoGrid.setBackground(BG_DARK);
        infoGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 999));
        content.add(infoGrid);

        JScrollPane sp = new JScrollPane(content);
        sp.setBorder(null);
        sp.getViewport().setBackground(BG_DARK);
        sp.getVerticalScrollBar().setUnitIncrement(12);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return sp;
    }

    // ── IMAGE SECTION ───────────────────────────────────────────
    private JPanel buildImageSection() {
        // Container with relative layout: image + buttons overlay at bottom
        JPanel wrapper = new JPanel(null) {
            @Override public Dimension getPreferredSize() {
                return new Dimension(600, 280);
            }
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, 280);
            }
        };
        wrapper.setBackground(new Color(12, 8, 30));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Image label — fills wrapper
        imgLabel = new JLabel("", SwingConstants.CENTER);
        imgLabel.setBackground(new Color(12, 8, 30));
        imgLabel.setOpaque(true);
        imgLabel.setBounds(0, 0, 1920, 280); // will stretch with resize
        wrapper.add(imgLabel);

        // Gradient overlay panel (dark bottom fade)
        JPanel fade = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(
                    0, 0,    new Color(0,0,0,0),
                    0, getHeight(), new Color(20,15,50,230));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        fade.setOpaque(false);
        fade.setBounds(0, 100, 1920, 180);
        wrapper.add(fade);

        // Buttons overlay — bottom strip
        btnOverlay = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnOverlay.setOpaque(false);
        btnOverlay.setBounds(16, 230, 700, 54);
        wrapper.add(btnOverlay);

        // Resize listener to reposition overlay
        wrapper.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                int w = wrapper.getWidth();
                imgLabel.setBounds(0, 0, w, 280);
                fade.setBounds(0, 100, w, 180);
                btnOverlay.setBounds(16, 230, w - 32, 54);
            }
        });

        return wrapper;
    }

    // ── DIVIDER ─────────────────────────────────────────────────
    private JPanel makeDivider() {
        JPanel d = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(60, 50, 100));
                g2.fillRect(0, getHeight()/2, getWidth(), 1);
                g2.dispose();
            }
        };
        d.setBackground(BG_DARK);
        d.setPreferredSize(new Dimension(0, 8));
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        d.setAlignmentX(Component.LEFT_ALIGNMENT);
        return d;
    }

    // ═══════════════════════════════════════════════════════════
    // REFRESH — nạp dữ liệu game vào UI
    // ═══════════════════════════════════════════════════════════
    private void refresh() {
        if (game == null) return;

        // ── Ảnh banner ──
        ImageIcon icon = loadImage(game.getHinhAnh(), 900, 280);
        imgLabel.setIcon(icon);
        if (icon == null) {
            imgLabel.setText("NO IMAGE");
            imgLabel.setForeground(TEXT_MUTED);
            imgLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        } else {
            imgLabel.setText("");
        }

        // ── Nút mua/thuê trên ảnh ──
        btnOverlay.removeAll();
        if (game.getGiaCD() != null && game.getGiaCD() > 0)
            btnOverlay.add(makeBannerBtn("BUY CD", game.getGiaCDText(), BTN_CD,
                e -> openBillAdd("CD")));
        if (game.getGiaROM() != null && game.getGiaROM() > 0)
            btnOverlay.add(makeBannerBtn("BUY ROM", game.getGiaROMText(), BTN_ROM,
                e -> openBillAdd("ROM")));
        if (game.getGiaThueNgay() != null && game.getGiaThueNgay() > 0)
            btnOverlay.add(makeBannerBtn("RENT", game.getGiaThueText(), BTN_RENT,
                e -> openRent()));
        btnOverlay.revalidate();
        btnOverlay.repaint();

        // ── Title ──
        lblTitle.setText(nvl(game.getTenGame()));

        // ── Mô tả (ưu tiên moTa từ GAME_CHITIET, fallback về ghiChu) ──
        String desc = nvl(game.getMoTa()).isEmpty() ? nvl(game.getGhiChu()) : nvl(game.getMoTa());
        lblDesc.setText("<html><body style='width:580px;color:#BBBAD8;font-size:12px;line-height:1.5'>"
            + desc + "</body></html>");

        // ── Info grid ──
        infoGrid.removeAll();
        addInfoRow("CATEGORY:",       nvl(game.getTheLoai()),       false);
        addInfoRow("TITLE:",          nvl(game.getTenGame()),        false);
        addInfoRow("PLATFORM:",       nvl(game.getNenTang()),        false);
        addInfoRow("RATING:",         nvl(game.getRating()),         true);
        addInfoRow("GENRE:",          nvl(game.getGenre()),          true);
        addInfoRow("DELIVERY METHOD:",nvl(game.getDeliveryMethod()), false);
        addInfoRow("RELEASE DATE:",
            game.getReleaseDate() != null
                ? game.getReleaseDate().format(DATE_FMT) : "TBA",   false);
        addInfoRow("REGION:",         nvl(game.getRegion()),         true);

        // Features có thể multiline
        String features = nvl(game.getFeatures());
        if (!features.isEmpty()) {
            addInfoRow("FEATURES:", features.replace(",", ",\n"), false);
        }

        addInfoRow("LANGUAGE:",       nvl(game.getLanguage()),       false);
        addInfoRow("ACCEPT CURRENCY:",nvl(game.getCurrency()),       true);

        infoGrid.revalidate();
        infoGrid.repaint();
        revalidate();
        repaint();
    }

    // ── INFO ROW helper ──────────────────────────────────────────
    private void addInfoRow(String labelText, String valueText, boolean accentValue) {
        // Label cell
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(F_LABEL);
        lbl.setForeground(LABEL_COLOR);
        lbl.setBorder(new EmptyBorder(2, 0, 2, 8));

        // Value cell
        JLabel val;
        if (valueText.contains("\n")) {
            // multiline: use html
            val = makeHtmlLabel(valueText.replace("\n", "<br>"), 340);
        } else {
            val = new JLabel(valueText.isEmpty() ? "—" : valueText);
            val.setFont(F_VALUE);
        }
        val.setForeground(accentValue ? VALUE_ACCENT : TEXT_GRAY);
        val.setBorder(new EmptyBorder(2, 0, 2, 0));

        infoGrid.add(lbl);
        infoGrid.add(val);
    }

    // ── BANNER BUTTON ───────────────────────────────────────────
    private JButton makeBannerBtn(String topText, String bottomText,
                                   Color bg, ActionListener action) {
        JButton btn = new JButton() {
            private boolean hov = false;
            {
                setFocusPainted(false); setContentAreaFilled(false);
                setBorderPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hov = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hov = false; repaint(); }
                });
                addActionListener(action);
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hov ? bg.brighter() : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));

                // top line
                g2.setColor(new Color(255,255,255,180));
                g2.setFont(F_BTN_TOP);
                FontMetrics fm1 = g2.getFontMetrics();
                g2.drawString(topText,
                    (getWidth() - fm1.stringWidth(topText)) / 2,
                    getHeight() / 2 - 1);

                // bottom line (price)
                g2.setColor(Color.WHITE);
                g2.setFont(F_BTN_BOT);
                FontMetrics fm2 = g2.getFontMetrics();
                g2.drawString(bottomText,
                    (getWidth() - fm2.stringWidth(bottomText)) / 2,
                    getHeight() / 2 + 15);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(120, 50));
        return btn;
    }

    // ── ACTIONS ─────────────────────────────────────────────────
    private void openBillAdd(String loai) {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        // TODO: truyền game + loai vào BillAddDialog nếu dialog hỗ trợ pre-fill
        new BillAddDialog(parent).setVisible(true);
    }

    private void openRent() {
        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        // TODO: truyền game vào RentAddDialog nếu dialog hỗ trợ pre-fill
        JOptionPane.showMessageDialog(this,
            "Mở phiếu thuê cho: " + nvl(game.getTenGame()),
            "Thuê game", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── HELPERS ─────────────────────────────────────────────────
    private JLabel makeHtmlLabel(String html, int width) {
        JLabel l = new JLabel("<html><body style='width:" + width + "px;font-size:12px'>"
            + html + "</body></html>");
        l.setFont(F_DESC);
        l.setForeground(TEXT_GRAY);
        return l;
    }

    private ImageIcon loadImage(String path, int w, int h) {
        try {
            if (path == null || path.isBlank()) throw new Exception();
            java.net.URL url = getClass().getResource(path);
            if (url == null) throw new Exception();
            Image scaled = new ImageIcon(url).getImage()
                .getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception ex) {
            try {
                java.net.URL def = getClass().getResource("/icons/no-image.png");
                if (def != null) {
                    Image scaled = new ImageIcon(def).getImage()
                        .getScaledInstance(w, h, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
            } catch (Exception ignored) {}
            return null;
        }
    }

    private String nvl(String s) { return s == null ? "" : s; }
}