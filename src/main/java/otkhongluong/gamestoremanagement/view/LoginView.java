package otkhongluong.gamestoremanagement.view;

import otkhongluong.gamestoremanagement.controller.LoginController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class LoginView extends JFrame {

    // ── Palette (theo đúng ảnh) ────────────────────────────
    // Nền: tím đậm, radial gradient từ tím sáng giữa ra tím đêm rìa
    private static final Color BG_CENTER   = new Color(110, 50,  210);   // tím sáng giữa
    private static final Color BG_EDGE     = new Color(30,   8,   80);   // tím đậm rìa

    // Card: tím mờ (rgba ~130,80,200, alpha ~0.35)
    private static final Color CARD_BG     = new Color(130,  80, 200,  90);
    private static final Color CARD_BORDER = new Color(190, 160, 255,  80);

    // Input: nền TRẮNG, icon và placeholder xám tối
    private static final Color INPUT_BG    = Color.WHITE;
    private static final Color INPUT_FG    = new Color( 30,  30,  30);   // text đen
    private static final Color INPUT_PH    = new Color(150, 150, 150);   // placeholder xám
    private static final Color ICON_COLOR  = new Color(100, 100, 120);   // icon xám

    // Button Sign in: đen gần như tuyệt đối
    private static final Color BTN_BG      = new Color(  8,   6,  16);
    private static final Color BTN_HOVER   = new Color( 20,  14,  40);

    // Text options row
    private static final Color TEXT_OPT    = new Color(220, 210, 255);   // trắng tím nhạt
    private static final Color TEXT_FORGOT = new Color(200, 185, 245);

    // Links Terms/Privacy
    private static final Color LINK_COLOR  = new Color(180, 150, 255);

    // Checkbox fill
    private static final Color CHK_BG     = new Color(120,  60, 200);

    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private JButton        btnLogin, btnToggle;
    private JCheckBox      chkRemember;
    private boolean        pwVisible = false;
    private LoginController controller;

    // ── Constructor ────────────────────────────────────────
    public LoginView(LoginController controller) {
        this.controller = controller;
        controller.setView(this);

        setTitle("QABAP Gaming — Login");
        setSize(840, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JLayeredPane layered = new JLayeredPane();
        layered.setPreferredSize(new Dimension(840, 620));
        setContentPane(layered);

        // Layer 0: gradient background (tím)
        GradientBg bg = new GradientBg();
        bg.setBounds(0, 0, 840, 620);
        layered.add(bg, JLayeredPane.DEFAULT_LAYER);

        // Layer 1: centered form
        JPanel formWrapper = buildForm();
        formWrapper.setBounds(0, 0, 840, 620);
        formWrapper.setOpaque(false);
        layered.add(formWrapper, JLayeredPane.PALETTE_LAYER);

        pack();
        loadRememberedCredentials();
    }

    // ══════════════════════════════════════════════════════
    // FORM — layout giống ảnh:
    //   [controller icon to]
    //   [QABAP GAMING title]   ← ảnh có title này
    //   [card tím mờ]
    //     [input Username]
    //     [input Password]
    //     [Remember me]  [forgot password]
    //     [Sign in button đen]
    //     [Terms]  [Privacy]
    // ══════════════════════════════════════════════════════
    private JPanel buildForm() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setMaximumSize(new Dimension(400, 9999));

        // ── Controller icon (to hơn, 200x148) ─────────────
        JLabel logoIcon = new JLabel(buildControllerIcon());
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Title "QABAP GAMING" ───────────────────────────
        JLabel title = new JLabel("QABAP GAMING");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Glass card ─────────────────────────────────────
        GlassCard card = new GlassCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(22, 22, 18, 22));
        card.setMaximumSize(new Dimension(400, 9999));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Input username ─────────────────────────────────
        JPanel userRow = buildUsernameRow();

        // ── Input password ─────────────────────────────────
        JPanel passRow = buildPasswordRow();

        // ── Options row ────────────────────────────────────
        JPanel options = new JPanel(new BorderLayout());
        options.setOpaque(false);
        options.setMaximumSize(new Dimension(9999, 30));

        // Checkbox "Ghi nhớ đăng nhập"
        chkRemember = new JCheckBox("Ghi nhớ đăng nhập");
        chkRemember.setOpaque(false);
        chkRemember.setForeground(TEXT_OPT);
        chkRemember.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkRemember.setFocusPainted(false);
        chkRemember.setIcon(buildCheckIcon(false));
        chkRemember.setSelectedIcon(buildCheckIcon(true));
        chkRemember.setSelected(true); // mặc định checked như ảnh
        chkRemember.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        options.add(chkRemember, BorderLayout.WEST);

        // ── Sign in button (đen) ───────────────────────────
        DarkButton signIn = new DarkButton("Sign in");
        this.btnLogin = signIn;
        signIn.setAlignmentX(Component.CENTER_ALIGNMENT);
        signIn.addActionListener(e -> handleLogin());

        // ── Terms / Privacy ────────────────────────────────
        JPanel linksRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        linksRow.setOpaque(false);
        linksRow.setMaximumSize(new Dimension(9999, 26));
        linksRow.add(buildLink("Terms"));
        linksRow.add(buildLink("Privacy"));

        // ── Assemble card ──────────────────────────────────
        card.add(userRow);
        card.add(Box.createVerticalStrut(10));
        card.add(passRow);
        card.add(Box.createVerticalStrut(8));
        card.add(options);
        card.add(Box.createVerticalStrut(16));
        card.add(signIn);
        card.add(Box.createVerticalStrut(12));
        card.add(linksRow);

        // ── Assemble full column ───────────────────────────
        center.add(logoIcon);
        center.add(Box.createVerticalStrut(10));
        center.add(title);
        center.add(Box.createVerticalStrut(18));
        center.add(card);

        wrapper.add(center);
        return wrapper;
    }

    // ── Username input row ─────────────────────────────────
    private JPanel buildUsernameRow() {
        WhiteInput panel = new WhiteInput();
        JLabel icon = new JLabel(buildUserIcon());
        icon.setBorder(new EmptyBorder(0, 0, 0, 8));
        txtUsername = new JTextField();
        txtUsername.setOpaque(false);
        txtUsername.setBorder(null);
        txtUsername.setForeground(INPUT_FG);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setCaretColor(INPUT_FG);
        addPlaceholder(txtUsername, "Username");
        panel.add(icon, BorderLayout.WEST);
        panel.add(txtUsername, BorderLayout.CENTER);
        return panel;
    }

    // ── Password input row ─────────────────────────────────
    private JPanel buildPasswordRow() {
        WhiteInput panel = new WhiteInput();
        JLabel icon = new JLabel(buildLockIcon());
        icon.setBorder(new EmptyBorder(0, 0, 0, 8));

        txtPassword = new JPasswordField();
        txtPassword.setOpaque(false);
        txtPassword.setBorder(null);
        txtPassword.setForeground(INPUT_PH);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setCaretColor(INPUT_FG);
        txtPassword.setEchoChar((char) 0);
        addPlaceholderPass(txtPassword, "Password");

        btnToggle = new JButton(buildEyeIcon(false));
        btnToggle.setBorder(new EmptyBorder(0, 6, 0, 0));
        btnToggle.setContentAreaFilled(false);
        btnToggle.setBorderPainted(false);
        btnToggle.setFocusPainted(false);
        btnToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnToggle.addActionListener(e -> togglePassword());

        panel.add(icon, BorderLayout.WEST);
        panel.add(txtPassword, BorderLayout.CENTER);
        panel.add(btnToggle, BorderLayout.EAST);
        return panel;
    }

    // ── Placeholder helpers ────────────────────────────────
    private void addPlaceholder(JTextField f, String ph) {
        f.setText(ph);
        f.setForeground(INPUT_PH);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(ph)) { f.setText(""); f.setForeground(INPUT_FG); }
            }
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) { f.setText(ph); f.setForeground(INPUT_PH); }
            }
        });
    }

    private void addPlaceholderPass(JPasswordField f, String ph) {
        f.setText(ph);
        f.setForeground(INPUT_PH);
        f.setEchoChar((char) 0);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (String.valueOf(f.getPassword()).equals(ph)) {
                    f.setText(""); f.setForeground(INPUT_FG);
                    f.setEchoChar(pwVisible ? (char) 0 : '•');
                }
            }
            public void focusLost(FocusEvent e) {
                if (String.valueOf(f.getPassword()).isEmpty()) {
                    f.setText(ph); f.setForeground(INPUT_PH); f.setEchoChar((char) 0);
                }
            }
        });
    }

    private void togglePassword() {
        pwVisible = !pwVisible;
        String cur = String.valueOf(txtPassword.getPassword());
        if (!cur.equals("Password")) {
            txtPassword.setEchoChar(pwVisible ? (char) 0 : '•');
        }
        btnToggle.setIcon(buildEyeIcon(pwVisible));
    }

    private void handleLogin() {
        String u = txtUsername.getText().equals("Username") ? "" : txtUsername.getText();
        String p = String.valueOf(txtPassword.getPassword()).equals("Password")
                ? "" : new String(txtPassword.getPassword());
        boolean remember = chkRemember.isSelected();
        controller.handleLogin(u, p, remember);
    }

    private void loadRememberedCredentials() {
        try {
            java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userRoot().node("gamestoremanagement_login");
            boolean remember = prefs.getBoolean("remember", false);
            chkRemember.setSelected(remember);
            if (remember) {
                String u = prefs.get("username", "");
                String p = prefs.get("password", "");
                if (!u.isEmpty()) {
                    txtUsername.setText(u);
                    txtUsername.setForeground(INPUT_FG);
                }
                if (!p.isEmpty()) {
                    txtPassword.setText(p);
                    txtPassword.setForeground(INPUT_FG);
                    txtPassword.setEchoChar(pwVisible ? (char) 0 : '•');
                }
            }
        } catch (Exception ignored) {}
    }

    public void showError(String msg)   { JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE); }
    public void showSuccess(String msg) { JOptionPane.showMessageDialog(this, msg); }

    // ══════════════════════════════════════════════════════
    // ICONS
    // ══════════════════════════════════════════════════════

    /** Controller: load từ file resource, scale về 200×148 */
    private ImageIcon buildControllerIcon() {
        try {
            java.net.URL url = getClass().getResource("/icons/controller_icon.png");
            if (url != null) {
                ImageIcon raw = new ImageIcon(url);
                Image scaled = raw.getImage().getScaledInstance(200, 148, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception ignored) {}
        // fallback: trả về icon rỗng nếu không tìm thấy file
        return new ImageIcon(new BufferedImage(200, 148, BufferedImage.TYPE_INT_ARGB));
    }

    /** User icon: màu xám tối cho nền trắng */
    private ImageIcon buildUserIcon() {
        BufferedImage img = new BufferedImage(22, 22, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(ICON_COLOR);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Ellipse2D.Float(7, 2, 8, 8));
        g.draw(new Arc2D.Float(2, 13, 18, 11, 0, 180, Arc2D.OPEN));
        g.dispose();
        return new ImageIcon(img);
    }

    /** Lock icon: màu xám tối */
    private ImageIcon buildLockIcon() {
        BufferedImage img = new BufferedImage(22, 22, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(ICON_COLOR);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Float(5, 2, 12, 11, 0, 180, Arc2D.OPEN));
        g.draw(new RoundRectangle2D.Float(3, 11, 16, 10, 4, 4));
        g.fill(new Ellipse2D.Float(9.5f, 14f, 3, 3));
        g.dispose();
        return new ImageIcon(img);
    }

    /** Eye icon: xám tối, slash khi ẩn */
    private ImageIcon buildEyeIcon(boolean open) {
        BufferedImage img = new BufferedImage(22, 22, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(ICON_COLOR);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Float(2, 7, 18, 8, 0, 180, Arc2D.OPEN));
        g.draw(new Arc2D.Float(2, 7, 18, 8, 180, 180, Arc2D.OPEN));
        g.draw(new Ellipse2D.Float(8, 8, 6, 6));
        if (!open) g.drawLine(4, 18, 18, 4);
        g.dispose();
        return new ImageIcon(img);
    }

    /** Checkbox icon: tròn tím, tick trắng khi checked */
    private ImageIcon buildCheckIcon(boolean checked) {
        int sz = 18;
        BufferedImage img = new BufferedImage(sz, sz, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (checked) {
            g.setColor(CHK_BG);
            g.fill(new Ellipse2D.Float(0, 0, sz, sz));
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawPolyline(new int[]{4, 7, 14}, new int[]{9, 13, 5}, 3);
        } else {
            g.setColor(new Color(255, 255, 255, 40));
            g.fill(new Ellipse2D.Float(0, 0, sz, sz));
            g.setColor(new Color(255, 255, 255, 100));
            g.setStroke(new BasicStroke(1.4f));
            g.draw(new Ellipse2D.Float(0.7f, 0.7f, sz - 1.4f, sz - 1.4f));
        }
        g.dispose();
        return new ImageIcon(img);
    }

    private JLabel buildLink(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(LINK_COLOR);
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lbl.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { lbl.setText("<html><u>" + text + "</u></html>"); }
            public void mouseExited(MouseEvent e)  { lbl.setText(text); }
        });
        return lbl;
    }

    // ══════════════════════════════════════════════════════
    // INNER CLASSES
    // ══════════════════════════════════════════════════════

    /** Nền tím radial gradient — đúng theo ảnh */
    class GradientBg extends JPanel {
        GradientBg() { setOpaque(true); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int W = getWidth(), H = getHeight();
            // Radial: tím sáng ở giữa, tím đêm ở rìa
            RadialGradientPaint rg = new RadialGradientPaint(
                new Point2D.Float(W * 0.55f, H * 0.42f),
                Math.max(W, H) * 0.75f,
                new float[]{0f, 0.45f, 1f},
                new Color[]{
                    new Color(110, 50, 210),   // tím sáng giữa
                    new Color(60,  18, 140),   // tím trung
                    new Color(20,   5,  60)    // tím đêm rìa
                }
            );
            g2.setPaint(rg);
            g2.fillRect(0, 0, W, H);
            g2.dispose();
        }
    }

    /** Card tím mờ: glassmorphism */
    class GlassCard extends JPanel {
        GlassCard() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Fill tím mờ
            g2.setColor(CARD_BG);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 22, 22));
            // Border trắng mờ
            g2.setColor(CARD_BORDER);
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, getWidth()-1.2f, getHeight()-1.2f, 22, 22));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Input field: nền TRẮNG solid */
    class WhiteInput extends JPanel {
        WhiteInput() {
            super(new BorderLayout());
            setOpaque(false);
            setMaximumSize(new Dimension(9999, 50));
            setPreferredSize(new Dimension(340, 50));
            setBorder(new EmptyBorder(0, 14, 0, 14));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(INPUT_BG);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Sign in button: đen gần như tuyệt đối */
    class DarkButton extends JButton {
        private boolean hovered = false;
        DarkButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 16));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(9999, 50));
            setPreferredSize(new Dimension(340, 50));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hovered ? BTN_HOVER : BTN_BG);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            g2.setColor(Color.WHITE);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
            int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), tx, ty);
            g2.dispose();
        }
    }
}