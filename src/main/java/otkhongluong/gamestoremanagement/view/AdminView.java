package otkhongluong.gamestoremanagement.view;


import otkhongluong.gamestoremanagement.model.User;
import otkhongluong.gamestoremanagement.util.*;
import otkhongluong.gamestoremanagement.controller.LoginController;
import otkhongluong.gamestoremanagement.view.panel.*;

import javax.swing.*;
import java.awt.*;

public class AdminView extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private User currentUser;
    private JButton activeButton;
    private JButton btnHome;
    private final Color MENU_TEXT = new Color(180,180,200); // xám gaming

    private final Color SIDEBAR_COLOR = new Color(35,32,70);
    private final Color ACTIVE_COLOR  = new Color(18,16,45); // tím rất đậm

    public AdminView(User user) {

        this.currentUser = user;

        if (user.getMaRole() != 1) {
            JOptionPane.showMessageDialog(this, "Không có quyền!");
            dispose();
            return;
        }

        setTitle("QABAP GAMING");
        setSize(1300,800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createMainPanel(), BorderLayout.CENTER);

        SwingUtilities.invokeLater(() ->
                switchTab(btnHome,"HOME")
        );
    }

    // ================= LOAD ICON =================
    private ImageIcon loadIcon(String path, int size){
        ImageIcon icon =
                new ImageIcon(getClass().getResource(path));

        Image img = icon.getImage()
                .getScaledInstance(size,size,Image.SCALE_SMOOTH);

        return new ImageIcon(img);
    }

    // ================= LOAD GAME IMAGE =================
    private JLabel loadGameImage(String path,int w,int h){

        ImageIcon icon =
                new ImageIcon(getClass().getResource(path));

        Image img = icon.getImage();

        int imgW = img.getWidth(null);
        int imgH = img.getHeight(null);

        double scale = Math.max(
                (double) w / imgW,
                (double) h / imgH
        );

        int newW = (int)(imgW * scale);
        int newH = (int)(imgH * scale);

        Image scaled =
                img.getScaledInstance(newW,newH,Image.SCALE_SMOOTH);

        JLabel label = new JLabel(new ImageIcon(scaled)){
            @Override
            protected void paintComponent(Graphics g){

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(
                        0,0,getWidth(),getHeight(),20,20));

                int x = (getWidth()-newW)/2;
                int y = (getHeight()-newH)/2;

                g2.drawImage(scaled,x,y,null);
                g2.dispose();
            }
        };

        label.setPreferredSize(new Dimension(w,h));

        return label;
    }

    // ================= SIDEBAR =================
    private JPanel createSidebar(){

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(230,0));
        sidebar.setBackground(SIDEBAR_COLOR);

        JLabel logo = new JLabel("QABAP GAMING");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Arial",Font.BOLD,22));
        logo.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        JPanel menu = new JPanel(new GridLayout(10,1,5,10));
        menu.setBackground(SIDEBAR_COLOR);

        btnHome = createMenuButton("Trang chủ","/icons/home_icon.png");
        JButton btnGame   = createMenuButton("Game","/icons/game_icon.png");
        JButton btnAdmin  = createMenuButton("Quản trị","/icons/manage_icon.png");
        JButton btnSales  = createMenuButton("Bán hàng","/icons/sales_icon.png");
        JButton btnReport = createMenuButton("Thống kê","/icons/statistic_icon.png");
        JButton btnPoint  = createMenuButton("Điểm khách hàng","/icons/star_icon.png");
        JButton btnLogout = createMenuButton("Đăng xuất","/icons/logout_icon.png");

        btnHome.setFont(new Font("Lato",Font.PLAIN,19));
        btnGame.setFont(new Font("Lato",Font.PLAIN,19));
        btnAdmin.setFont(new Font("Lato",Font.PLAIN,19));
        btnSales.setFont(new Font("Lato",Font.PLAIN,19));
        btnReport.setFont(new Font("Lato",Font.PLAIN,19));
        btnPoint.setFont(new Font("Lato",Font.PLAIN,19));
        btnLogout.setFont(new Font("Lato",Font.PLAIN,19));
        
        menu.add(btnHome);
        menu.add(btnGame);
        menu.add(btnAdmin);
        menu.add(btnSales);
        menu.add(btnReport);
        menu.add(btnPoint);

        sidebar.add(logo,BorderLayout.NORTH);
        sidebar.add(menu,BorderLayout.CENTER);
        sidebar.add(btnLogout,BorderLayout.SOUTH);

        btnHome.addActionListener(e -> switchTab(btnHome,"HOME"));
        btnGame.addActionListener(e -> switchTab(btnGame,"GAME"));
        btnAdmin.addActionListener(e -> switchTab(btnAdmin,"ADMIN"));
        btnSales.addActionListener(e -> switchTab(btnSales,"SALES"));
        btnReport.addActionListener(e -> switchTab(btnReport,"REPORT"));
        btnPoint.addActionListener(e -> switchTab(btnPoint,"POINT"));

        btnLogout.addActionListener(e -> {
            dispose();
            new LoginView(new LoginController()).setVisible(true);
        });

        return sidebar;
    }

    private void switchTab(JButton btn,String name){

        if(activeButton!=null){
            activeButton.setBackground(SIDEBAR_COLOR);
        }

        activeButton = btn;
        activeButton.setBackground(ACTIVE_COLOR);

        cardLayout.show(contentPanel,name);
    }

    // ================= MAIN =================
    private JPanel createMainPanel(){

        JPanel main = new JPanel(new BorderLayout());

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        contentPanel.add(createDashboardPanel(),"HOME");
        contentPanel.add(new AdminPanel(currentUser),"ADMIN");
        contentPanel.add(createGameWrapper(),"GAME");
        contentPanel.add(new ReportPanel(),"REPORT");
        contentPanel.add(new PointPanel(),"POINT");
        contentPanel.add(new SalesPanel(),"SALES");

        main.add(contentPanel,BorderLayout.CENTER);

        return main;
    }

    // ================= TOP BAR =================
    private JPanel createTopBar(){

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(45,48,70));
        topBar.setPreferredSize(new Dimension(0,65));
        topBar.setBorder(BorderFactory.createEmptyBorder(10,15,10,15));

        // ================= SEARCH PANEL =================
        JPanel searchPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // nền
                g2.setColor(new Color(70,75,95));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),30,30);

                // viền
                g2.setColor(new Color(255,255,255,160));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,30,30);

                g2.dispose();
            }
        };

        JLabel iconSearch =
                new JLabel(loadIcon("/icons/searching_icon.png",22));

        iconSearch.setBorder(
                BorderFactory.createEmptyBorder(0,0,0,10));

        JTextField txtSearch = new JTextField("Tìm kiếm");
        txtSearch.setBorder(null);
        txtSearch.setForeground(Color.LIGHT_GRAY);
        txtSearch.setBackground(new Color(70,75,95));
        txtSearch.setCaretColor(Color.WHITE);
        txtSearch.setFont(new Font("Segoe UI",Font.PLAIN,14));

        // placeholder
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter(){
            public void focusGained(java.awt.event.FocusEvent e){
                if(txtSearch.getText().equals("Tìm kiếm")){
                    txtSearch.setText("");
                    txtSearch.setForeground(Color.WHITE);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e){
                if(txtSearch.getText().isEmpty()){
                    txtSearch.setText("Tìm kiếm");
                    txtSearch.setForeground(Color.LIGHT_GRAY);
                }
            }
        });

        searchPanel.add(iconSearch,BorderLayout.WEST);
        searchPanel.add(txtSearch,BorderLayout.CENTER);
        searchPanel.setOpaque(false);
        searchPanel.setPreferredSize(new Dimension(700,40));
        searchPanel.setBorder(
                BorderFactory.createEmptyBorder(5,8,5,10));

        // ================= RIGHT USER =================
        JPanel rightPanel =
                new JPanel(new FlowLayout(FlowLayout.RIGHT,15,0));
        rightPanel.setOpaque(false);

        JLabel userIcon =
                new JLabel(loadIcon("/icons/user_icon.png",35));
        userIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPopupMenu userDropdown = new JPopupMenu();
        userDropdown.setLayout(new BorderLayout());
        userDropdown.add(new EmployeeDashboardPanel());

        userIcon.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseClicked(java.awt.event.MouseEvent e){
                userDropdown.show(
                        userIcon,
                        userIcon.getWidth()-260,
                        userIcon.getHeight()
                );
            }
        });

        rightPanel.add(userIcon);

    // ================= WRAPPER =================
    JPanel wrapper =
            new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
    wrapper.setOpaque(false);
    wrapper.add(searchPanel);
    wrapper.setBorder(
            BorderFactory.createEmptyBorder(0,0,0,50));

    topBar.add(wrapper,BorderLayout.CENTER);
    topBar.add(rightPanel,BorderLayout.EAST);

    return topBar;
}

    // ================= DASHBOARD =================
    private JPanel createDashboardPanel(){

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(20,20,50));

        wrapper.add(createTopBar(),BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setBackground(new Color(20,20,50));
        content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        content.add(createBannerGrid());
        content.add(Box.createVerticalStrut(25));
        content.add(createTrending());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(20,20,50));

        wrapper.add(scroll,BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel createGameWrapper(){
        JPanel panel=new JPanel(new BorderLayout());
        panel.add(createTopBar(),BorderLayout.NORTH);
        panel.add(new GamePanel(),BorderLayout.CENTER);
        return panel;
    }
    
    // ================= BANNER =================
    private JPanel createBannerGrid(){

        JPanel panel=new JPanel(new GridLayout(2,2,20,20));
        panel.setBackground(new Color(20,20,50));

        panel.add(createBanner("Skyrim","/icons/skyrim.png"));
        panel.add(createBanner("PUBG","/icons/pubg.png"));
        panel.add(createBanner("Minecraft","/icons/mc.png"));
        panel.add(createBanner("Hollow Knight","/icons/hollow.png"));

        return panel;
    }

    private JPanel createBanner(String title,String img){

        JPanel banner = new JPanel(new BorderLayout());
        banner.setOpaque(false);

        JLabel image = loadGameImage(img,400,170);

        JLabel name = new JLabel(title);
        name.setForeground(Color.WHITE);
        name.setFont(new Font("Segoe UI",Font.BOLD,18));
        name.setBorder(BorderFactory.createEmptyBorder(10,15,10,15));

        image.setLayout(new BorderLayout());
        image.add(name,BorderLayout.SOUTH);

        banner.add(image,BorderLayout.CENTER);

        UIStyle.cardHover(banner);

        return banner;
    }

    // ================= TRENDING =================
    private JPanel createTrending(){

        JPanel section=new JPanel(new BorderLayout());
        section.setBackground(new Color(20,20,50));

        JLabel title=new JLabel("TRENDING NOW");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial",Font.BOLD,22));

        JPanel grid=new JPanel(new GridLayout(1,4,20,20));
        grid.setBackground(new Color(20,20,50));

        grid.add(createTrendingCard("Destiny 2","/icons/destiny.png"));
        grid.add(createTrendingCard("Left 4 Dead 2","/icons/l4d2.png"));
        grid.add(createTrendingCard("Once Human","/icons/once.png"));
        grid.add(createTrendingCard("Devil May Cry 5","/icons/devil.png"));

        section.add(title,BorderLayout.NORTH);
        section.add(grid,BorderLayout.CENTER);

        return section;
    }

    private JPanel createTrendingCard(String name,String img){

        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(false);

        JLabel image = loadGameImage(img,200,260);

        JLabel lbl = new JLabel(name);
        lbl.setForeground(Color.WHITE);
        lbl.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        image.setLayout(new BorderLayout());
        image.add(lbl,BorderLayout.SOUTH);

        card.add(image,BorderLayout.CENTER);

        UIStyle.cardHover(card);

        return card;
    }

    // ================= BUTTON STYLE =================
    private JButton createMenuButton(String text,String iconPath){

        JButton btn=new JButton(text);

        btn.setIcon(loadIcon(iconPath,20));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(12);

        UIStyle.steamButton(btn);

        btn.setBackground(SIDEBAR_COLOR);
        btn.setForeground(MENU_TEXT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);

        return btn;
    }
}
