package otkhongluong.gamestoremanagement.view;

import otkhongluong.gamestoremanagement.controller.Navigator;
import otkhongluong.gamestoremanagement.model.User;
import otkhongluong.gamestoremanagement.util.UIStyle;
import otkhongluong.gamestoremanagement.view.panel.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * StaffView — màn hình chính dành cho nhân viên (MaRole == 2).
 *
 * Tabs:
 *   ✅ Trang chủ
 *   ✅ Game
 *   ✅ Bán hàng
 *   ✅ Quản trị (quản lý khách hàng, game, sản phẩm — KHÔNG có quản lý user/role)
 *   ❌ Điểm tích lũy (đã bỏ)
 *   ❌ Thống kê toàn hệ thống (dữ liệu nhạy cảm)
 */
public class StaffView extends JFrame {

    private CardLayout cardLayout;
    private JPanel     contentPanel;
    private final User      currentUser;
    private final Navigator navigator;
    private JButton    activeButton;
    private JButton    btnHome;

    private JButton    activeAdminBtn;

    public StaffView(User user, Navigator navigator) {
        this.currentUser = user;
        this.navigator   = navigator;

        if (user.getMaRole() != 3) {
            JOptionPane.showMessageDialog(this, "Không có quyền truy cập!");
            dispose();
            return;
        }

        setTitle("QABAP GAMING — Nhân viên");
        setSize(1300, 800);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIStyle.BG_MAIN);

        add(createSidebar(),    BorderLayout.WEST);
        add(createMainPanel(), BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> switchTab(btnHome, "HOME"));
    }

    // ══════════════════════════════════════════════════════════
    // LOGOUT
    // ══════════════════════════════════════════════════════════

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn đăng xuất?", "Đăng xuất",
            JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        dispose();
        navigator.goToLogin();
    }

    // ══════════════════════════════════════════════════════════
    // ICON LOADER
    // ══════════════════════════════════════════════════════════

    private ImageIcon loadIcon(String path, int size) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) return blankIcon(size);
            Image img = new ImageIcon(url).getImage()
                    .getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            return blankIcon(size);
        }
    }

    private ImageIcon blankIcon(int size) {
        return new ImageIcon(new java.awt.image.BufferedImage(
                size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB));
    }

    // ══════════════════════════════════════════════════════════
    // GAME IMAGE LOADER
    // ══════════════════════════════════════════════════════════

    private JLabel loadGameImage(String path, int w, int h) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) throw new Exception("not found");
            Image src = new ImageIcon(url).getImage();
            int iw = src.getWidth(null), ih = src.getHeight(null);
            double scale = Math.max((double) w / iw, (double) h / ih);
            int nw = (int)(iw * scale), nh = (int)(ih * scale);
            Image scaled = src.getScaledInstance(nw, nh, Image.SCALE_SMOOTH);

            JLabel lbl = new JLabel(new ImageIcon(scaled)) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                    int x = (getWidth()  - nw) / 2;
                    int y = (getHeight() - nh) / 2;
                    g2.drawImage(scaled, x, y, null);
                    GradientPaint gp = new GradientPaint(
                            0, getHeight() * 0.5f, new Color(0, 0, 0, 0),
                            0, getHeight(),         new Color(0, 0, 0, 180));
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            };
            lbl.setPreferredSize(new Dimension(w, h));
            return lbl;
        } catch (Exception e) {
            JLabel lbl = new JLabel("NO IMG", SwingConstants.CENTER);
            lbl.setForeground(UIStyle.TEXT_MUTED);
            lbl.setFont(UIStyle.FONT_BADGE);
            lbl.setPreferredSize(new Dimension(w, h));
            lbl.setOpaque(true);
            lbl.setBackground(UIStyle.BG_CARD);
            return lbl;
        }
    }

    // ══════════════════════════════════════════════════════════
    // SIDEBAR
    // ══════════════════════════════════════════════════════════

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(UIStyle.BG_SIDEBAR);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1,
                new Color(55, 45, 100)));

        // Logo
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(UIStyle.BG_SIDEBAR);
        logoPanel.setBorder(new EmptyBorder(22, 20, 18, 16));

        JLabel logo = new JLabel("QABAP");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);

        JLabel logoSub = new JLabel("GAMING");
        logoSub.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoSub.setForeground(UIStyle.ACCENT_LIGHT);

        JPanel logoText = new JPanel();
        logoText.setLayout(new BoxLayout(logoText, BoxLayout.Y_AXIS));
        logoText.setOpaque(false);
        logoText.add(logo);
        logoText.add(logoSub);

        JLabel logoIcon = new JLabel(loadIcon("/icons/game_icon.png", 28));
        logoIcon.setBorder(new EmptyBorder(0, 0, 0, 10));

        logoPanel.add(logoIcon, BorderLayout.WEST);
        logoPanel.add(logoText, BorderLayout.CENTER);

        JPanel logoWrap = new JPanel(new BorderLayout());
        logoWrap.setBackground(UIStyle.BG_SIDEBAR);
        logoWrap.add(logoPanel, BorderLayout.CENTER);
        logoWrap.add(UIStyle.accentSeparator(), BorderLayout.SOUTH);

        // Badge role
        JLabel roleLabel = new JLabel("Welcome!", JLabel.LEFT);
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        roleLabel.setForeground(new Color(160, 200, 255));
        roleLabel.setBorder(new EmptyBorder(6, 20, 6, 0));
        roleLabel.setOpaque(true);
        roleLabel.setBackground(UIStyle.BG_SIDEBAR);

        JPanel logoAndBadge = new JPanel(new BorderLayout());
        logoAndBadge.setBackground(UIStyle.BG_SIDEBAR);
        logoAndBadge.add(logoWrap,  BorderLayout.CENTER);
        logoAndBadge.add(roleLabel, BorderLayout.SOUTH);

        // Menu
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(UIStyle.BG_SIDEBAR);
        menu.setBorder(new EmptyBorder(12, 8, 8, 8));

        btnHome                  = createMenuButton("Trang chủ", "/icons/home_icon.png");
        JButton btnGame          = createMenuButton("Game",       "/icons/game_icon.png");
        JButton btnSales         = createMenuButton("Bán hàng",   "/icons/sales_icon.png");
        JButton btnStaffAdmin    = createMenuButton("Quản trị",   "/icons/manage_icon.png");

        for (JButton b : new JButton[]{btnHome, btnGame, btnSales, btnStaffAdmin}) {
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            menu.add(b);
            menu.add(Box.createVerticalStrut(2));
        }

        // Logout
        JButton btnLogout = createMenuButton("Đăng xuất", "/icons/logout_icon.png");
        btnLogout.setForeground(new Color(220, 100, 100));
        btnLogout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(UIStyle.BG_SIDEBAR);
        bottomPanel.setBorder(new EmptyBorder(8, 8, 14, 8));

        JPanel sepWrap = new JPanel(new BorderLayout());
        sepWrap.setBackground(UIStyle.BG_SIDEBAR);
        sepWrap.setBorder(new EmptyBorder(0, 0, 8, 0));
        sepWrap.add(UIStyle.accentSeparator());
        bottomPanel.add(sepWrap,   BorderLayout.NORTH);
        bottomPanel.add(btnLogout, BorderLayout.CENTER);

        sidebar.add(logoAndBadge, BorderLayout.NORTH);
        sidebar.add(menu,         BorderLayout.CENTER);
        sidebar.add(bottomPanel,  BorderLayout.SOUTH);

        btnHome.addActionListener(e       -> switchTab(btnHome,       "HOME"));
        btnGame.addActionListener(e       -> switchTab(btnGame,       "GAME"));
        btnSales.addActionListener(e      -> switchTab(btnSales,      "SALES"));
        btnStaffAdmin.addActionListener(e -> switchTab(btnStaffAdmin, "STAFF_ADMIN"));
        btnLogout.addActionListener(e     -> handleLogout());

        return sidebar;
    }

    private void switchTab(JButton btn, String name) {
        if (activeButton != null) {
            activeButton.setBackground(UIStyle.BG_SIDEBAR);
            activeButton.setForeground(UIStyle.TEXT_MUTED);
        }
        activeButton = btn;
        activeButton.setBackground(UIStyle.SIDEBAR_ACTIVE);
        activeButton.setForeground(UIStyle.TEXT_PRIMARY);
        cardLayout.show(contentPanel, name);
    }

    // ══════════════════════════════════════════════════════════
    // MAIN PANEL
    // ══════════════════════════════════════════════════════════

    private JPanel createMainPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIStyle.BG_MAIN);

        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIStyle.BG_MAIN);

        contentPanel.add(createDashboardPanel(),    "HOME");
        contentPanel.add(createGameWrapper(),       "GAME");
        contentPanel.add(new SalesPanel(),          "SALES");
        contentPanel.add(createStaffAdminPanel(),   "STAFF_ADMIN");

        main.add(contentPanel, BorderLayout.CENTER);
        return main;
    }

    // ══════════════════════════════════════════════════════════
    // STAFF ADMIN PANEL
    // Tab "Quản trị" của nhân viên: khách hàng, game, sản phẩm
    // KHÔNG có quản lý user/role như AdminPanel
    // ══════════════════════════════════════════════════════════

    private JPanel createStaffAdminPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UIStyle.BG_MAIN);
        wrapper.add(createTopBar(), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(13, 13, 35));
        tabbedPane.setOpaque(true);
        tabbedPane.setUI(new CustomTabbedPaneUI());

        tabbedPane.addTab("Khách hàng", new CustomerPanel());

        wrapper.add(tabbedPane, BorderLayout.CENTER);
        return wrapper;
    }

    // ══════════════════════════════════════════════════════════
    // TOP BAR
    // ══════════════════════════════════════════════════════════

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UIStyle.BG_TOPBAR);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(55, 45, 100));
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
                g2.dispose();
            }
        };
        topBar.setOpaque(false);
        topBar.setPreferredSize(new Dimension(0, 62));
        topBar.setBorder(new EmptyBorder(10, 18, 10, 18));

        // Bỏ toàn bộ phần txtGameSearch / searchBox / searchWrap

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);

        JLabel roleBadge = new JLabel("Nhân viên");
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        roleBadge.setForeground(new Color(100, 200, 255));
        roleBadge.setBackground(new Color(30, 60, 100));
        roleBadge.setOpaque(true);
        roleBadge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 120, 180), 1, true),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));

        JLabel lblUser = new JLabel(
            currentUser.getUsername() != null ? currentUser.getUsername() : "Staff");
        lblUser.setFont(UIStyle.FONT_BODY);
        lblUser.setForeground(UIStyle.TEXT_MUTED);

        JLabel userIcon = new JLabel(loadIcon("/icons/user_icon.png", 32));
        userIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPopupMenu userDropdown = new JPopupMenu();
        userDropdown.setLayout(new BorderLayout());
        EmployeeDashboardPanel dashPanel = new EmployeeDashboardPanel(currentUser);
        dashPanel.setPreferredSize(new Dimension(280, 560));
        userDropdown.add(dashPanel, BorderLayout.CENTER);
        userDropdown.setPreferredSize(new Dimension(280, 560));
        userIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                userDropdown.show(userIcon, userIcon.getWidth() - 260, userIcon.getHeight());
            }
        });

        rightPanel.add(roleBadge);
        rightPanel.add(lblUser);
        rightPanel.add(userIcon);

        topBar.add(rightPanel, BorderLayout.EAST);
        return topBar;
    }

    // ══════════════════════════════════════════════════════════
    // DASHBOARD
    // ══════════════════════════════════════════════════════════

    private JPanel createDashboardPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UIStyle.BG_MAIN);
        wrapper.add(createTopBar(), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setBackground(UIStyle.BG_MAIN);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel heading = new JLabel("Trang chủ");
        heading.setFont(UIStyle.FONT_HEADING);
        heading.setForeground(Color.WHITE);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(heading);
        content.add(Box.createVerticalStrut(4));

        JPanel sep = UIStyle.accentSeparator();
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        content.add(sep);
        content.add(Box.createVerticalStrut(20));

        content.add(createBannerGrid());
        content.add(Box.createVerticalStrut(28));
        content.add(createTrending());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIStyle.BG_MAIN);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createGameWrapper() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIStyle.BG_MAIN);
        GamePanel gamePanel = new GamePanel();

        // Top bar riêng cho tab Game, có search box
        JPanel topBar = createTopBar();

        // Tạo search box riêng chỉ cho tab này
        JTextField txtSearch = new JTextField(22);
        txtSearch.setText("Tìm kiếm game...");
        txtSearch.setForeground(UIStyle.TEXT_MUTED);
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if ("Tìm kiếm game...".equals(txtSearch.getText())) {
                    txtSearch.setText("");
                    txtSearch.setForeground(Color.WHITE);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText("Tìm kiếm game...");
                    txtSearch.setForeground(UIStyle.TEXT_MUTED);
                }
            }
        });
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                String text = txtSearch.getText().trim();
                if (text.equals("Tìm kiếm game...") || text.isEmpty()) {
                    gamePanel.filterGames("");
                } else {
                    gamePanel.filterGames(text);
                }
            }
        });

        JPanel searchBox = UIStyle.buildSearchPanel(txtSearch);
        searchBox.setPreferredSize(new Dimension(320, 38));
        JPanel searchWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchWrap.setOpaque(false);
        searchWrap.add(searchBox);

        topBar.add(searchWrap, BorderLayout.WEST);

        panel.add(topBar,     BorderLayout.NORTH);
        panel.add(gamePanel,  BorderLayout.CENTER);
        return panel;
    }

    // ══════════════════════════════════════════════════════════
    // BANNER GRID
    // ══════════════════════════════════════════════════════════

    private JPanel createBannerGrid() {
        JLabel sectionTitle = new JLabel("Featured");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        sectionTitle.setForeground(UIStyle.TEXT_MUTED);

        JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14));
        grid.setBackground(UIStyle.BG_MAIN);
        grid.setOpaque(false);

        grid.add(createBanner("Skyrim",        "/icons/skyrim.png"));
        grid.add(createBanner("PUBG",          "/icons/pubg.png"));
        grid.add(createBanner("Minecraft",     "/icons/mc.png"));
        grid.add(createBanner("Hollow Knight", "/icons/hollow.png"));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(sectionTitle, BorderLayout.NORTH);
        wrap.add(Box.createVerticalStrut(10), BorderLayout.BEFORE_FIRST_LINE);
        wrap.add(grid, BorderLayout.CENTER);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 420));
        return wrap;
    }

    private JPanel createBanner(String title, String img) {
        JPanel banner = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                UIStyle.paintCard(g, this);
            }
        };
        banner.setOpaque(false);

        JLabel image = loadGameImage(img, 400, 170);
        image.setLayout(new BorderLayout());

        JLabel name = new JLabel("  " + title);
        name.setForeground(Color.WHITE);
        name.setFont(new Font("Segoe UI", Font.BOLD, 15));
        name.setBorder(new EmptyBorder(0, 8, 10, 8));
        image.add(name, BorderLayout.SOUTH);

        banner.add(image, BorderLayout.CENTER);
        UIStyle.cardHover(banner);
        return banner;
    }

    // ══════════════════════════════════════════════════════════
    // TRENDING
    // ══════════════════════════════════════════════════════════

    private JPanel createTrending() {
        JPanel section = new JPanel(new BorderLayout());
        section.setOpaque(false);

        JLabel title = new JLabel("Trending Now");
        title.setForeground(UIStyle.TEXT_MUTED);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel grid = new JPanel(new GridLayout(1, 4, 14, 0));
        grid.setOpaque(false);

        grid.add(createTrendingCard("Destiny 2",       "/icons/destiny.png"));
        grid.add(createTrendingCard("Left 4 Dead 2",   "/icons/l4d2.png"));
        grid.add(createTrendingCard("Once Human",      "/icons/once.png"));
        grid.add(createTrendingCard("Devil May Cry 5", "/icons/devil.png"));

        section.add(title, BorderLayout.NORTH);
        section.add(grid,  BorderLayout.CENTER);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 310));
        return section;
    }

    private JPanel createTrendingCard(String name, String img) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                UIStyle.paintCard(g, this);
            }
        };
        card.setOpaque(false);

        JLabel image = loadGameImage(img, 200, 240);
        image.setLayout(new BorderLayout());

        JLabel lbl = new JLabel("  " + name);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setBorder(new EmptyBorder(0, 6, 10, 6));
        image.add(lbl, BorderLayout.SOUTH);

        card.add(image, BorderLayout.CENTER);
        UIStyle.cardHover(card);
        return card;
    }

    // ══════════════════════════════════════════════════════════
    // MENU BUTTON
    // ══════════════════════════════════════════════════════════

    private JButton createMenuButton(String text, String iconPath) {
        JButton btn = new JButton(text);
        btn.setIcon(loadIcon(iconPath, 18));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        UIStyle.steamButton(btn);
        return btn;
    }
}