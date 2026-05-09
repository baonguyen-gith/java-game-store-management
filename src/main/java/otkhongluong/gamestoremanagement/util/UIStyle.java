package otkhongluong.gamestoremanagement.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class UIStyle {

    // ══════════════════════════════════════════════════════════
    // PALETTE — dùng xuyên suốt toàn bộ app
    // ══════════════════════════════════════════════════════════

    /** Nền chính — tím than rất tối */
    public static final Color BG_MAIN      = new Color(14, 12, 38);

    /** Nền sidebar */
    public static final Color BG_SIDEBAR   = new Color(22, 19, 54);

    /** Nền card / panel con */
    public static final Color BG_CARD      = new Color(28, 24, 64);

    /** Nền topbar */
    public static final Color BG_TOPBAR    = new Color(20, 17, 50);

    /** Accent chính — tím neon */
    public static final Color ACCENT       = new Color(120, 80, 220);

    /** Accent sáng hơn — dùng cho hover, gradient */
    public static final Color ACCENT_LIGHT = new Color(160, 110, 255);

    /** Viền card thường */
    public static final Color BORDER_CARD  = new Color(55, 45, 100);

    /** Viền card khi hover */
    public static final Color BORDER_HOVER = new Color(120, 80, 220);

    /** Text trắng chính */
    public static final Color TEXT_PRIMARY = Color.WHITE;

    /** Text phụ — xám tím nhạt */
    public static final Color TEXT_MUTED   = new Color(140, 130, 175);

    /** Màu active button sidebar */
    public static final Color SIDEBAR_ACTIVE = new Color(38, 32, 90);

    // ── Giữ tương thích tên cũ (không xóa để tránh compile error) ──
    public static Color BG_MAIN_COMPAT = BG_MAIN;
    public static Color BG_CARD_COMPAT = BG_CARD;
    public static Color SIDEBAR        = BG_SIDEBAR;
    public static Color HOVER          = ACCENT;

    // ══════════════════════════════════════════════════════════
    // FONTS
    // ══════════════════════════════════════════════════════════

    public static final Font FONT_LOGO    = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_MENU    = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BADGE   = new Font("Segoe UI", Font.BOLD, 10);

    // ══════════════════════════════════════════════════════════
    // SIDEBAR BUTTON
    // ══════════════════════════════════════════════════════════

    /**
     * Áp dụng style cho nút sidebar.
     * Tự vẽ nền — không dùng Swing default painter để tránh viền/artifact.
     */
    public static void steamButton(JButton btn) {
        btn.setForeground(TEXT_MUTED);
        btn.setBackground(BG_SIDEBAR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);   // tự vẽ trong paintComponent
        btn.setOpaque(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(11, 18, 11, 10));
        btn.setFont(FONT_MENU);
        btn.setIconTextGap(10);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setRolloverEnabled(false);

        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = b.getBackground();
                if (bg != null && !bg.equals(BG_SIDEBAR)) {
                    // active state — highlight accent bar kiri
                    g2.setColor(SIDEBAR_ACTIVE);
                    g2.fillRoundRect(4, 2, b.getWidth() - 8, b.getHeight() - 4, 10, 10);
                    // accent bar bên trái
                    g2.setColor(ACCENT);
                    g2.fillRoundRect(4, 6, 3, b.getHeight() - 12, 3, 3);
                    b.setForeground(TEXT_PRIMARY);
                } else {
                    b.setForeground(TEXT_MUTED);
                }

                g2.dispose();
                super.paint(g, c);
            }
        });
    }

    // ══════════════════════════════════════════════════════════
    // CARD HOVER
    // ══════════════════════════════════════════════════════════

    /**
     * Hiệu ứng hover cho JPanel card — bo góc, viền accent.
     * Panel phải setOpaque(false) và tự vẽ nền trong paintComponent.
     */
    public static void cardHover(JPanel card) {
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                card.putClientProperty("hovered", Boolean.TRUE);
                card.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                card.putClientProperty("hovered", Boolean.FALSE);
                card.repaint();
            }
        });
    }

    /**
     * Vẽ nền card bo góc — gọi từ paintComponent của JPanel.
     * Tự động đổi màu theo clientProperty "hovered".
     */
    public static void paintCard(Graphics g, JPanel card) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        boolean hovered = Boolean.TRUE.equals(card.getClientProperty("hovered"));

        // Nền
        g2.setColor(hovered ? new Color(38, 33, 85) : BG_CARD);
        g2.fill(new RoundRectangle2D.Float(0, 0, card.getWidth(), card.getHeight(), 14, 14));

        // Viền
        g2.setColor(hovered ? BORDER_HOVER : BORDER_CARD);
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f,
                card.getWidth() - 1.2f, card.getHeight() - 1.2f, 14, 14));

        // Shimmer ở trên khi hover
        if (hovered) {
            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(255, 255, 255, 18),
                    0, card.getHeight() / 2f, new Color(255, 255, 255, 0));
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Float(0, 0,
                    card.getWidth(), card.getHeight() / 2f, 14, 14));
        }

        g2.dispose();
    }

    // ══════════════════════════════════════════════════════════
    // SEPARATOR GRADIENT
    // ══════════════════════════════════════════════════════════

    /** Vẽ đường kẻ ngang gradient accent — dùng cho header. */
    public static JPanel accentSeparator() {
        JPanel sep = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(
                        0, 0, ACCENT,
                        getWidth(), 0, new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 0));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        sep.setPreferredSize(new Dimension(0, 2));
        sep.setOpaque(false);
        return sep;
    }

    // ══════════════════════════════════════════════════════════
    // SEARCH FIELD
    // ══════════════════════════════════════════════════════════

    /** Tạo search panel bo góc đồng bộ với topbar. */
    public static JPanel buildSearchPanel(JTextField txtSearch) {
        JPanel searchPanel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(35, 30, 75));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
                g2.setColor(new Color(255, 255, 255, 35));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
                g2.dispose();
            }
        };
        searchPanel.setOpaque(false);
        searchPanel.setBorder(new EmptyBorder(5, 12, 5, 12));

        txtSearch.setBorder(null);
        txtSearch.setOpaque(false);
        txtSearch.setForeground(new Color(180, 170, 220));
        txtSearch.setCaretColor(Color.WHITE);
        txtSearch.setFont(FONT_BODY);

        searchPanel.add(txtSearch, BorderLayout.CENTER);
        return searchPanel;
    }
}