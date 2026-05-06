package otkhongluong.gamestoremanagement.view;
import otkhongluong.gamestoremanagement.util.Session;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import otkhongluong.gamestoremanagement.controller.LoginController;
import otkhongluong.gamestoremanagement.model.User;

public class LoginView extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnToggle;
    private boolean isVisible = false;

    private LoginController controller;

    public LoginView(LoginController controller) {
        this.controller = controller;

        setTitle("Login");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setContentPane(new GradientPanel());
        setLayout(new GridBagLayout());

        // ===== CONTAINER =====
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        // ===== ICON LỚN (GIỮ LOADICON CỦA BẠN) =====
        JLabel logo = new JLabel(loadIcon("icons/controller_icon.png", 200, 200));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // ===== CARD =====
        JPanel card = new RoundedPanel(30);
        card.setBackground(new Color(255, 255, 255, 80));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        card.setMaximumSize(new Dimension(550, Integer.MAX_VALUE));

        // ===== USERNAME =====
        JPanel userPanel = createInput("icons/account_icon.png", true);
        card.add(userPanel);
        card.add(Box.createVerticalStrut(5));

        // ===== PASSWORD =====
        JPanel passPanel = createPasswordInput();
        card.add(passPanel);
        card.add(Box.createVerticalStrut(10));

        // ===== OPTIONS =====
        JPanel options = new JPanel(new BorderLayout());
        options.setOpaque(false);

        JCheckBox remember = new JCheckBox("Remember me");
        remember.setFont(new Font("Arial", Font.PLAIN, 16));
        remember.setOpaque(false);
        remember.setForeground(Color.WHITE);

        JLabel forgot = new JLabel("Forgot password?");
        forgot.setFont(new Font("Arial", Font.PLAIN, 16));
        forgot.setForeground(Color.WHITE);

        options.add(remember, BorderLayout.WEST);
        options.add(forgot, BorderLayout.EAST);

        card.add(options);
        card.add(Box.createVerticalStrut(10));

        // ===== BUTTON =====
        btnLogin = new JButton("Sign in");
        btnLogin.setFont(new Font("Arial", Font.BOLD, 20));
        btnLogin.setBackground(Color.BLACK);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        btnLogin.setPreferredSize(new Dimension(550, 70));
        btnLogin.setMaximumSize(new Dimension(550, 70));

        card.add(btnLogin);

        // ===== ADD =====
        container.add(logo);
        container.add(card);

        add(container);

        btnLogin.addActionListener(e -> handleLogin());
    }

    // ===== Input có icon =====
    private JPanel createInput(String iconPath, boolean isTextField) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.setMaximumSize(new Dimension(500, 60));
        panel.setPreferredSize(new Dimension(500, 60));

        JLabel lbl = new JLabel(loadIcon(iconPath, 30, 30));
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        txtUsername = new JTextField("Username");
        txtUsername.setBorder(null);
        txtUsername.setForeground(Color.GRAY);
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 14));

        // Placeholder
        txtUsername.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (txtUsername.getText().equals("Username")) {
                    txtUsername.setText("");
                    txtUsername.setForeground(Color.BLACK);
                }
            }

            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtUsername.getText().isEmpty()) {
                    txtUsername.setText("Username");
                    txtUsername.setForeground(Color.GRAY);
                }
            }
        });

        panel.add(lbl, BorderLayout.WEST);
        panel.add(txtUsername, BorderLayout.CENTER);

        return panel;
    }

    // ===== Password input =====
    private JPanel createPasswordInput() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.setMaximumSize(new Dimension(500, 60));
        panel.setPreferredSize(new Dimension(500, 60));
        
        JLabel lbl = new JLabel(loadIcon("icons/password_icon.png", 30, 30));
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        txtPassword = new JPasswordField("Password");
        txtPassword.setBorder(null);
        txtPassword.setForeground(Color.GRAY);
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPassword.setEchoChar((char) 0); // hiện chữ Password

        // Placeholder
        txtPassword.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (String.valueOf(txtPassword.getPassword()).equals("Password")) {
                    txtPassword.setText("");
                    txtPassword.setForeground(Color.BLACK);
                    txtPassword.setEchoChar('•'); // ẩn mật khẩu
                }
            }

            public void focusLost(java.awt.event.FocusEvent e) {
                if (String.valueOf(txtPassword.getPassword()).isEmpty()) {
                    txtPassword.setText("Password");
                    txtPassword.setForeground(Color.GRAY);
                    txtPassword.setEchoChar((char) 0); // hiện lại chữ
                }
            }
        });

        btnToggle = new JButton(loadIcon("icons/password_hidden_icon.png", 30, 30));
        btnToggle.setBorder(null);
        btnToggle.setContentAreaFilled(false);
        btnToggle.setPreferredSize(new Dimension(40, 40));

        btnToggle.addActionListener(e -> togglePassword());

        panel.add(lbl, BorderLayout.WEST);
        panel.add(txtPassword, BorderLayout.CENTER);
        panel.add(btnToggle, BorderLayout.EAST);

        return panel;
    }
    // ===== Toggle password =====
    private void togglePassword() {
        if (isVisible) {
            txtPassword.setEchoChar('•');
            btnToggle.setIcon(loadIcon("icons/password_hidden_icon.png", 30, 30));
        } else {
            txtPassword.setEchoChar((char) 0);
            btnToggle.setIcon(loadIcon("icons/password_open_icon.png", 30, 30));
        }
        isVisible = !isVisible;
    }

    // ===== Load icon chuẩn nét (BICUBIC + giữ tỉ lệ) =====
    private ImageIcon loadIcon(String path, int w, int h) {
        java.net.URL url = ClassLoader.getSystemResource(path);

        if (url == null) {
            System.out.println("Không tìm thấy: " + path);
            return null;
        }

        Image img = new ImageIcon(url).getImage();

        BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resized.createGraphics();

        // Chất lượng cao
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int imgW = img.getWidth(null);
        int imgH = img.getHeight(null);

        double scale = Math.min((double) w / imgW, (double) h / imgH);
        int newW = (int) (imgW * scale);
        int newH = (int) (imgH * scale);

        int x = (w - newW) / 2;
        int y = (h - newH) / 2;

        g2.drawImage(img, x, y, newW, newH, null);
        g2.dispose();

        return new ImageIcon(resized);
    }

    // ===== Login xử lý =====
    private void handleLogin() {
        try {
            String username = txtUsername.getText();
            String password = new String(txtPassword.getPassword());

            if (username.equals("Username")) username = "";
            if (password.equals("Password")) password = "";

            User user = controller.login(username, password);

            if (user != null) {
                JOptionPane.showMessageDialog(this, "Login thành công!");
                
                Session.setMaNV(user.getMaUser());
                this.dispose(); // đóng login

                // 👉 PHÂN QUYỀN TẠI ĐÂY
                if (controller.isAdmin(user)) {
                    new AdminView(user).setVisible(true);
                } else if (controller.isStaff(user)) {
                    new StaffView(user).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Không xác định role!");
                }

            } else {
                JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!");
            }

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // ===== Gradient background =====
    class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(30, 0, 60),
                    getWidth(), getHeight(), new Color(120, 70, 200)
            );

            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }
    class RoundedPanel extends JPanel {
        private int radius;

        public RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fill(new java.awt.geom.RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radius, radius));

            super.paintComponent(g);
        }
    }
}