package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.controller.InvoiceController;
import otkhongluong.gamestoremanagement.controller.InvoiceController.ActionResult;
import otkhongluong.gamestoremanagement.model.CartItem;
import otkhongluong.gamestoremanagement.model.Customer;


import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * InvoiceAddDialog — Wizard 2 bước tạo hóa đơn mua game.
 * View thuần: không chứa SQL, DAO, hay Connection.
 * Mọi nghiệp vụ đều đi qua InvoiceController.
 */
public class InvoiceAddDialog extends JDialog {

    // =========================================================
    // PALETTE
    // =========================================================
    private static final Color BG           = new Color(28, 16, 72);
    private static final Color BG_PANEL     = new Color(38, 24, 90);
    private static final Color BG_CARD      = new Color(52, 34, 118);
    private static final Color ACCENT       = new Color(124, 92, 220);
    private static final Color ACCENT_LIGHT = new Color(168, 144, 255);
    private static final Color DIVIDER      = new Color(72, 52, 148);
    private static final Color WHITE        = Color.WHITE;
    private static final Color TEXT_MAIN    = new Color(235, 230, 255);
    private static final Color TEXT_MUTED   = new Color(148, 132, 196);
    private static final Color TEXT_DARK    = new Color(28, 16, 72);
    private static final Color INPUT_BG     = new Color(245, 242, 255);
    private static final Color SUCCESS      = new Color(72, 199, 142);
    private static final Color DANGER       = new Color(240, 80, 80);
    private static final Color GOLD         = new Color(255, 196, 64);
    private static final Color BTN_GREEN    = new Color(72, 199, 142);
    private static final Color BTN_RED      = new Color(240, 80, 80);

    // =========================================================
    // FONTS
    // =========================================================
    private static final Font F_TITLE   = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font F_SECTION = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font F_LABEL   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font F_CELL    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_HDR     = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font F_VALUE   = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font F_HINT    = new Font("Segoe UI", Font.ITALIC, 11);
    private static final Font F_BTN     = new Font("Segoe UI", Font.BOLD, 12);

    // =========================================================
    // CONSTANTS (chỉ dùng cho UI — tính toán thực ở Service)
    // =========================================================
    private static final int DIEM_TO_VND  = 5_000;
    private static final int VND_PER_DIEM = 100_000;

    // =========================================================
    // CONTROLLER — điểm liên lạc duy nhất với tầng dưới
    // =========================================================
    private final InvoiceController ctrl = new InvoiceController();

    // =========================================================
    // STATE — Bước 1
    // =========================================================
    private final List<CartItem> cart = new ArrayList<>();

    private JTable            tblGame;
    private DefaultTableModel tblGameModel;
    List<Object[]>  gameList;   // {MaGame, TenGame, TheLoai, NenTang}

    private JTable            tblSP;
    private DefaultTableModel tblSPModel;
    List<Object[]>  spList;     // {MaSP, MaCD_or_-1, loaiSP, giaBan, thongTin, available, MaGame}

    private JTable            tblCart;
    private DefaultTableModel tblCartModel;
    private JLabel            lblCartTotal;

    // =========================================================
    // STATE — Bước 2
    // =========================================================
    private JTextField txtSDT;
    private JLabel     lblTenKH, lblDiemHienCo;
    private JTextField txtDiemSuDung;
    private JLabel     lblTongGoc, lblGiamDiem, lblTongPhaiTra, lblDiemSeSau;

    private Customer currentKH  = null;
    private int      maKH       = -1;
    private int      diemHienCo = 0;

    private DefaultTableModel summaryTableModel;

    // =========================================================
    // LAYOUT
    // =========================================================
    private int     currentStep = 1;
    private JPanel  contentPanel;
    private JPanel  stepIndicator;
    private JButton btnBack, btnNext, btnConfirm;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public InvoiceAddDialog(Frame parent) {
        super(parent, "Tạo hóa đơn mua game", true);
        setSize(900, 670);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setBackground(BG);
        getRootPane().setBorder(BorderFactory.createLineBorder(ACCENT, 2));

        add(buildHeader(),   BorderLayout.NORTH);

        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(BG);
        contentPanel.add(buildStep1(), "step1");
        contentPanel.add(buildStep2(), "step2");
        add(contentPanel, BorderLayout.CENTER);

        add(buildFooter(), BorderLayout.SOUTH);
        showStep(1);
    }

    // =========================================================
    // HEADER
    // =========================================================
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(16, 20, 0, 20));

        JLabel title = new JLabel("MUA GAME / TẠO HÓA ĐƠN");
        title.setFont(F_TITLE);
        title.setForeground(WHITE);

        stepIndicator = buildStepIndicator();

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG);
        top.add(title, BorderLayout.WEST);
        top.add(stepIndicator, BorderLayout.EAST);

        JSeparator sep = new JSeparator();
        sep.setForeground(DIVIDER);
        sep.setBackground(DIVIDER);

        JPanel wrap = new JPanel(new BorderLayout(0, 10));
        wrap.setBackground(BG);
        wrap.add(top, BorderLayout.CENTER);
        wrap.add(sep, BorderLayout.SOUTH);

        p.add(wrap);
        return p;
    }

    private JPanel buildStepIndicator() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        p.setBackground(BG);
        String[] labels = {"1. Chọn sản phẩm", "2. Thanh toán"};
        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel(labels[i]);
            lbl.setName("step_" + (i + 1));
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setForeground(TEXT_MUTED);
            lbl.setBorder(new EmptyBorder(2, 8, 2, 8));
            p.add(lbl);
            if (i < labels.length - 1) {
                JLabel arrow = new JLabel(">");
                arrow.setForeground(DIVIDER);
                arrow.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                p.add(arrow);
            }
        }
        return p;
    }

    private void updateStepIndicator(int active) {
        for (Component c : stepIndicator.getComponents()) {
            if (c instanceof JLabel) {
                JLabel lbl = (JLabel) c;

                if (lbl.getName() != null && lbl.getName().startsWith("step_")) {
                    int s = Integer.parseInt(lbl.getName().split("_")[1]);

                    if (s == active) {
                        lbl.setForeground(ACCENT_LIGHT);
                    } else if (s < active) {
                        lbl.setForeground(SUCCESS);
                    } else {
                        lbl.setForeground(TEXT_MUTED);
                    }
                }
            }
        }
        stepIndicator.repaint();
    }

    // =========================================================
    // BƯỚC 1 — Chọn sản phẩm + giỏ hàng
    // =========================================================
    private JPanel buildStep1() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(12, 20, 0, 20));

        JPanel topSection = new JPanel(new GridLayout(1, 2, 10, 0));
        topSection.setBackground(BG);
        topSection.setPreferredSize(new Dimension(0, 290));

        // --- Bảng Game ---
        JPanel gamePanel = new JPanel(new BorderLayout(0, 6));
        gamePanel.setBackground(BG);

        JPanel gameTopBar = new JPanel(new BorderLayout());
        gameTopBar.setBackground(BG);
        JLabel lblGame = new JLabel("Danh sách game");
        lblGame.setFont(F_SECTION);
        lblGame.setForeground(TEXT_MAIN);
        PillButton btnRefreshGame = new PillButton("Làm mới", ACCENT, WHITE);
        btnRefreshGame.setPreferredSize(new Dimension(85, 28));
        btnRefreshGame.addActionListener(e -> loadGameTable());
        gameTopBar.add(lblGame, BorderLayout.WEST);
        gameTopBar.add(btnRefreshGame, BorderLayout.EAST);
        gamePanel.add(gameTopBar, BorderLayout.NORTH);

        String[] gameCols = {"Mã", "Tên game", "Thể loại"};
        tblGameModel = new DefaultTableModel(gameCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblGame = makeTable(tblGameModel);
        tblGame.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblGame.getColumnModel().getColumn(1).setPreferredWidth(200);
        tblGame.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblGame.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSPTable();
        });
        gamePanel.add(wrapScroll(tblGame), BorderLayout.CENTER);

        // --- Bảng Sản phẩm ---
        JPanel spPanel = new JPanel(new BorderLayout(0, 6));
        spPanel.setBackground(BG);

        JPanel spTopBar = new JPanel(new BorderLayout());
        spTopBar.setBackground(BG);
        JLabel lblSP = new JLabel("Chọn loại sản phẩm (CD / ROM)");
        lblSP.setFont(F_SECTION);
        lblSP.setForeground(TEXT_MAIN);
        spTopBar.add(lblSP, BorderLayout.WEST);
        spPanel.add(spTopBar, BorderLayout.NORTH);

        String[] spCols = {"Mã SP", "Loại", "Giá bán", "Tình trạng / Tồn kho"};
        tblSPModel = new DefaultTableModel(spCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblSP = makeTable(tblSPModel);
        tblSP.getColumnModel().getColumn(0).setPreferredWidth(60);
        tblSP.getColumnModel().getColumn(1).setPreferredWidth(55);
        tblSP.getColumnModel().getColumn(2).setPreferredWidth(130);
        tblSP.getColumnModel().getColumn(3).setPreferredWidth(160);

        tblSP.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected && spList != null && row < spList.size()) {
                    boolean available = (boolean) spList.get(row)[5];
                    if (!available) {
                        c.setBackground(new Color(255, 220, 220));
                        c.setForeground(new Color(180, 60, 60));
                    } else {
                        c.setBackground(row % 2 == 0 ? new Color(248, 246, 255) : WHITE);
                        c.setForeground(TEXT_DARK);
                    }
                }
                return c;
            }
        });
        spPanel.add(wrapScroll(tblSP), BorderLayout.CENTER);

        PillButton btnAddCart = new PillButton("+ Thêm vào giỏ", BTN_GREEN, TEXT_DARK);
        btnAddCart.setPreferredSize(new Dimension(160, 32));
        btnAddCart.addActionListener(e -> doAddToCart());
        JPanel btnAddRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        btnAddRow.setBackground(BG);
        btnAddRow.add(btnAddCart);
        spPanel.add(btnAddRow, BorderLayout.SOUTH);

        topSection.add(gamePanel);
        topSection.add(spPanel);
        p.add(topSection, BorderLayout.CENTER);

        // === Giỏ hàng ===
        JPanel cartSection = new JPanel(new BorderLayout(0, 6));
        cartSection.setBackground(BG);
        cartSection.setBorder(new EmptyBorder(4, 0, 0, 0));
        cartSection.setPreferredSize(new Dimension(0, 210));

        JPanel cartTopBar = new JPanel(new BorderLayout());
        cartTopBar.setBackground(BG);
        JLabel lblCart = new JLabel("🛒  Giỏ hàng");
        lblCart.setFont(F_SECTION);
        lblCart.setForeground(GOLD);
        cartTopBar.add(lblCart, BorderLayout.WEST);

        JPanel cartBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        cartBtnRow.setBackground(BG);
        PillButton btnRemove = new PillButton("Xóa mục chọn", BTN_RED, WHITE);
        btnRemove.setPreferredSize(new Dimension(120, 28));
        btnRemove.addActionListener(e -> doRemoveFromCart());
        PillButton btnClearCart = new PillButton("Xóa tất cả", new Color(100, 60, 160), WHITE);
        btnClearCart.setPreferredSize(new Dimension(100, 28));
        btnClearCart.addActionListener(e -> doClearCart());
        cartBtnRow.add(btnRemove);
        cartBtnRow.add(btnClearCart);
        cartTopBar.add(cartBtnRow, BorderLayout.EAST);
        cartSection.add(cartTopBar, BorderLayout.NORTH);

        String[] cartCols = {"STT", "Tên game", "Loại SP", "Mã SP / CD", "Đơn giá", "SL", "Thành tiền"};
        tblCartModel = new DefaultTableModel(cartCols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 5; }
        };
        tblCart = makeTable(tblCartModel);
        tblCart.getColumnModel().getColumn(0).setPreferredWidth(40);
        tblCart.getColumnModel().getColumn(1).setPreferredWidth(240);
        tblCart.getColumnModel().getColumn(2).setPreferredWidth(60);
        tblCart.getColumnModel().getColumn(3).setPreferredWidth(90);
        tblCart.getColumnModel().getColumn(4).setPreferredWidth(130);
        tblCart.getColumnModel().getColumn(5).setPreferredWidth(50);
        tblCart.getColumnModel().getColumn(6).setPreferredWidth(130);

        tblCart.getModel().addTableModelListener(e -> {
            int row = e.getFirstRow();
            int col = e.getColumn();
            if (col == 5 && row >= 0 && row < cart.size()) {
                CartItem item = cart.get(row);
                if ("ROM".equals(item.loaiSP)) {
                    try {
                        int qty = Integer.parseInt(tblCartModel.getValueAt(row, 5).toString().trim());
                        if (qty < 1) qty = 1;
                        item.soLuong = qty;
                    } catch (NumberFormatException ignored) {
                        item.soLuong = 1;
                    }
                    refreshCartTable();
                }
            }
        });

        JPanel cartBottom = new JPanel(new BorderLayout());
        cartBottom.setBackground(BG);
        lblCartTotal = new JLabel("Tổng giỏ hàng: 0 VNĐ");
        lblCartTotal.setFont(F_VALUE);
        lblCartTotal.setForeground(GOLD);
        lblCartTotal.setBorder(new EmptyBorder(4, 0, 0, 0));
        cartBottom.add(lblCartTotal, BorderLayout.WEST);
        JLabel hintCart = new JLabel("Mục ROM: sửa số lượng trực tiếp trong cột SL  |  CD: luôn SL = 1");
        hintCart.setFont(F_HINT);
        hintCart.setForeground(TEXT_MUTED);
        cartBottom.add(hintCart, BorderLayout.EAST);

        cartSection.add(wrapScroll(tblCart), BorderLayout.CENTER);
        cartSection.add(cartBottom, BorderLayout.SOUTH);
        p.add(cartSection, BorderLayout.SOUTH);

        loadGameTable();
        return p;
    }

    // ── Load game qua Controller ──────────────────────────────
    private void loadGameTable() {
        tblGameModel.setRowCount(0);
        gameList = ctrl.loadGameCatalog();
        for (Object[] g : gameList) {
            tblGameModel.addRow(new Object[]{"G" + g[0], g[1], g[2]});
        }
        tblSPModel.setRowCount(0);
        spList = null;
    }

    // ── Load SP của game đang chọn qua Controller ─────────────
    private void loadSPTable() {
        tblSPModel.setRowCount(0);
        spList = new ArrayList<>();
        int row = tblGame.getSelectedRow();
        if (row < 0 || gameList == null || row >= gameList.size()) return;
        int maGame = (int) gameList.get(row)[0];

        spList = ctrl.loadSpCatalog(maGame);
        for (Object[] sp : spList) {
            boolean available = (boolean) sp[5];
            if (available) {
                tblSPModel.addRow(new Object[]{
                    "SP" + sp[0],
                    sp[2],
                    String.format("%,.0f VNĐ", (double) sp[3]),
                    sp[4]
                });
            } else {
                tblSPModel.addRow(new Object[]{"—", sp[2], "—", sp[4]});
            }
        }
    }

    // ── Thêm vào giỏ ─────────────────────────────────────────
    private void doAddToCart() {
        int gameRow = tblGame.getSelectedRow();
        int spRow   = tblSP.getSelectedRow();
        if (gameRow < 0) { showMsg("Vui lòng chọn game!"); return; }
        if (spRow   < 0) { showMsg("Vui lòng chọn loại sản phẩm (CD hoặc ROM)!"); return; }
        if (spList == null || spRow >= spList.size()) return;

        Object[] sp        = spList.get(spRow);
        boolean  available = (boolean) sp[5];
        if (!available) {
            showMsg("Sản phẩm này đã hết hàng!\nVui lòng chọn CD khác hoặc chọn ROM.");
            return;
        }

        int    maSP   = (int)    sp[0];
        int    maCD   = (int)    sp[1];
        String loai   = (String) sp[2];
        double giaBan = (double) sp[3];
        int    maGame = (int)    sp[6];
        String tenGame = (String) gameList.get(gameRow)[1];

        String newKey = "CD".equals(loai) ? "CD_" + maCD : "ROM_" + maSP;
        for (CartItem item : cart) {
            if (item.cartKey.equals(newKey)) {
                if ("CD".equals(loai))
                    showMsg("CD" + maCD + " này đã có trong giỏ hàng!");
                else
                    showMsg("ROM game này đã có trong giỏ!\nBạn có thể điều chỉnh số lượng trong cột SL.");
                return;
            }
        }

        CartItem item = new CartItem(maSP, maCD, maGame, 1, tenGame, loai, newKey, giaBan);
        cart.add(item);
        refreshCartTable();
    }

    private void doRemoveFromCart() {
        int row = tblCart.getSelectedRow();
        if (row < 0 || row >= cart.size()) { showMsg("Chọn mục cần xóa!"); return; }
        cart.remove(row);
        refreshCartTable();
    }

    private void doClearCart() {
        if (cart.isEmpty()) return;
        int yn = JOptionPane.showConfirmDialog(this, "Xóa toàn bộ giỏ hàng?",
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (yn != JOptionPane.YES_OPTION) return;
        cart.clear();
        refreshCartTable();
    }

    private void refreshCartTable() {
        tblCartModel.setRowCount(0);
        double total = 0;
        for (int i = 0; i < cart.size(); i++) {
            CartItem item = cart.get(i);
            double thanh = item.donGia * item.soLuong;
            total += thanh;
            tblCartModel.addRow(new Object[]{
                i + 1,
                item.tenGame,
                item.loaiSP,
                "CD".equals(item.loaiSP) ? "CD" + item.maCD : "SP" + item.maSP,
                String.format("%,.0f VNĐ", item.donGia),
                item.soLuong,
                String.format("%,.0f VNĐ", thanh)
            });
        }
        lblCartTotal.setText(String.format("Tổng giỏ hàng: %,.0f VNĐ  (%d sản phẩm)", total, cart.size()));
    }

    // =========================================================
    // BƯỚC 2 — Thông tin KH + điểm + xác nhận
    // =========================================================
    private JPanel buildStep2() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(14, 20, 0, 20));

        JPanel cartSummaryPanel = new JPanel(new BorderLayout(0, 4));
        cartSummaryPanel.setBackground(BG);
        JLabel lblCartSum = new JLabel("Giỏ hàng đã chọn:");
        lblCartSum.setFont(F_SECTION);
        lblCartSum.setForeground(GOLD);
        cartSummaryPanel.add(lblCartSum, BorderLayout.NORTH);

        String[] summCols = {"Tên game", "Loại", "Đơn giá", "SL", "Thành tiền"};
        summaryTableModel = new DefaultTableModel(summCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblSumm = makeTable(summaryTableModel);
        tblSumm.getColumnModel().getColumn(0).setPreferredWidth(280);
        tblSumm.getColumnModel().getColumn(1).setPreferredWidth(60);
        tblSumm.getColumnModel().getColumn(2).setPreferredWidth(130);
        tblSumm.getColumnModel().getColumn(3).setPreferredWidth(40);
        tblSumm.getColumnModel().getColumn(4).setPreferredWidth(130);
        JScrollPane spSumm = wrapScroll(tblSumm);
        spSumm.setPreferredSize(new Dimension(0, 150));
        cartSummaryPanel.add(spSumm, BorderLayout.CENTER);
        p.add(cartSummaryPanel, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 0, 5, 10);

        int row = 0;

        // SĐT + Tìm + Tạo + Bỏ qua (Được gộp vào một Panel phụ để tránh bị GridBagLayout kéo dãn nút)
        txtSDT = makeInput();
        txtSDT.setPreferredSize(new Dimension(220, 40));
        txtSDT.setFont(new Font("Segoe UI", Font.BOLD, 15));

        PillButton btnTim  = new PillButton("Tìm",    ACCENT,                    WHITE);
        PillButton btnTao  = new PillButton("Tạo mới", SUCCESS,                  TEXT_DARK);
        PillButton btnSkip = new PillButton("Bỏ qua",  new Color(90, 70, 140),   WHITE);
        btnTim.setPreferredSize(new Dimension(80, 40));
        btnTao.setPreferredSize(new Dimension(100, 40));
        btnSkip.setPreferredSize(new Dimension(90, 40));
        btnSkip.setToolTipText("Khách không cung cấp SĐT — bỏ qua tích điểm");
        btnTim.addActionListener(e  -> doFindKH());
        btnTao.addActionListener(e  -> doCreateKH());
        btnSkip.addActionListener(e -> doSkipKH());
        txtSDT.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { doFindKH(); }
            public void keyPressed(KeyEvent e)  { if (e.getKeyCode() == KeyEvent.VK_ENTER) doFindKH(); }
        });

        JPanel sdtPanel = new JPanel(new GridBagLayout());
        sdtPanel.setOpaque(false);
        GridBagConstraints sdtGc = new GridBagConstraints();
        sdtGc.insets = new Insets(0, 0, 0, 8);
        sdtGc.fill = GridBagConstraints.HORIZONTAL;
        sdtGc.gridy = 0;

        sdtGc.gridx = 0; sdtGc.weightx = 1.0;
        sdtPanel.add(txtSDT, sdtGc);

        sdtGc.fill = GridBagConstraints.NONE;
        sdtGc.weightx = 0.0;

        sdtGc.gridx = 1;
        sdtPanel.add(btnTim, sdtGc);

        sdtGc.gridx = 2;
        sdtPanel.add(btnTao, sdtGc);

        sdtGc.gridx = 3;
        sdtGc.insets = new Insets(0, 0, 0, 0);
        sdtPanel.add(btnSkip, sdtGc);

        addFormLabel(form, gc, "Số điện thoại KH:", row, 0, 140);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 4;
        form.add(sdtPanel, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        // Tên KH
        row++;
        lblTenKH = new JLabel("---");
        lblTenKH.setFont(F_VALUE); lblTenKH.setForeground(TEXT_MAIN);
        addFormLabel(form, gc, "Tên khách hàng:", row, 0, 140);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 4;
        form.add(lblTenKH, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        // Điểm hiện có
        row++;
        lblDiemHienCo = new JLabel("---");
        lblDiemHienCo.setFont(F_VALUE); lblDiemHienCo.setForeground(GOLD);
        addFormLabel(form, gc, "Điểm tích lũy:", row, 0, 140);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 4;
        form.add(lblDiemHienCo, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        row++; addDivider(form, gc, row, 5);

        // Điểm sử dụng
        row++;
        txtDiemSuDung = makeInput();
        txtDiemSuDung.setText("0");
        txtDiemSuDung.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { recalc(); }
        });
        JLabel lblHintDiem = new JLabel("(1 điểm = 5.000 VNĐ giảm, không vượt quá tổng gốc)");
        lblHintDiem.setFont(F_HINT); lblHintDiem.setForeground(TEXT_MUTED);
        addFormLabel(form, gc, "Điểm muốn dùng:", row, 0, 140);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 0.4; form.add(txtDiemSuDung, gc);
        gc.gridx = 2; gc.weightx = 0.6; gc.gridwidth = 3; form.add(lblHintDiem, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        row++; addDivider(form, gc, row, 5);

        // Kết quả tính
        row++;
        lblTongGoc = resultLabel("---", TEXT_MAIN);
        addFormLabel(form, gc, "Tổng tiền gốc:", row, 0, 140);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 4;
        form.add(lblTongGoc, gc); gc.gridwidth = 1; gc.weightx = 0;

        row++;
        lblGiamDiem = resultLabel("---", SUCCESS);
        addFormLabel(form, gc, "Giảm từ điểm:", row, 0, 140);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 4;
        form.add(lblGiamDiem, gc); gc.gridwidth = 1; gc.weightx = 0;

        row++;
        lblTongPhaiTra = resultLabel("---", ACCENT_LIGHT);
        addFormLabel(form, gc, "Tổng tiền phải trả:", row, 0, 140);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 4;
        form.add(lblTongPhaiTra, gc); gc.gridwidth = 1; gc.weightx = 0;

        row++;
        lblDiemSeSau = resultLabel("---", GOLD);
        addFormLabel(form, gc, "Điểm sau giao dịch:", row, 0, 140);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 4;
        form.add(lblDiemSeSau, gc); gc.gridwidth = 1; gc.weightx = 0;

        row++;
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 5; gc.weighty = 1.0;
        gc.fill = GridBagConstraints.BOTH;
        form.add(Box.createVerticalGlue(), gc);
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weighty = 0;

        p.add(form, BorderLayout.CENTER);
        return p;
    }

    private void loadStep2Data() {
        summaryTableModel.setRowCount(0);
        for (CartItem item : cart) {
            summaryTableModel.addRow(new Object[]{
                item.tenGame,
                item.loaiSP,
                String.format("%,.0f VNĐ", item.donGia),
                item.soLuong,
                String.format("%,.0f VNĐ", item.donGia * item.soLuong)
            });
        }
        txtSDT.setText("");
        txtDiemSuDung.setText("0");
        lblTenKH.setText("Chưa tìm kiếm — nhập SĐT hoặc nhấn \"Bỏ qua\"");
        lblTenKH.setForeground(TEXT_MUTED);
        lblDiemHienCo.setText("---");
        currentKH = null; maKH = -1; diemHienCo = 0;
        recalc();
    }

    // ── Tìm KH qua Controller ────────────────────────────────
    private void doFindKH() {
        String sdt = txtSDT.getText().trim();
        if (sdt.isEmpty()) {
            currentKH = null; maKH = -1; diemHienCo = 0;
            lblTenKH.setText("Chưa nhập số điện thoại");
            lblTenKH.setForeground(TEXT_MUTED);
            lblDiemHienCo.setText("---");
            recalc(); return;
        }
        Customer kh = ctrl.findKHBySDT(sdt);
        if (kh != null) {
            currentKH = kh; maKH = kh.getMaKH(); diemHienCo = kh.getDiemTichLuy();
            lblTenKH.setText(kh.getHoTen());
            lblTenKH.setForeground(SUCCESS);
            lblDiemHienCo.setText(diemHienCo + " điểm  ~  "
                + String.format("%,.0f VNĐ", (double) diemHienCo * DIEM_TO_VND));
        } else {
            currentKH = null; maKH = -1; diemHienCo = 0;
            lblTenKH.setText("Không tìm thấy  —  Nhấn \"Tạo mới\" hoặc \"Bỏ qua\"");
            lblTenKH.setForeground(DANGER);
            lblDiemHienCo.setText("0 điểm");
        }
        recalc();
    }

    private void doSkipKH() {
        currentKH = null; maKH = -1; diemHienCo = 0;
        txtSDT.setText("");
        txtDiemSuDung.setText("0");
        lblTenKH.setText("Khách vãng lai (không tích điểm)");
        lblTenKH.setForeground(TEXT_MUTED);
        lblDiemHienCo.setText("0 điểm");
        recalc();
    }

    // ── Tạo KH mới qua Controller ────────────────────────────
    private void doCreateKH() {
        String sdt = txtSDT.getText().trim();
        if (sdt.isEmpty()) { showMsg("Vui lòng nhập số điện thoại trước!"); return; }

        // Nếu đã tồn tại thì chỉ load lên
        Customer existing = ctrl.findKHBySDT(sdt);
        if (existing != null) {
            showMsg("Khách hàng với SĐT " + sdt + " đã tồn tại!");
            currentKH = existing; maKH = existing.getMaKH(); diemHienCo = existing.getDiemTichLuy();
            lblTenKH.setText(existing.getHoTen()); lblTenKH.setForeground(SUCCESS);
            lblDiemHienCo.setText(diemHienCo + " điểm  ~  "
                + String.format("%,.0f VNĐ", (double) diemHienCo * DIEM_TO_VND));
            recalc(); return;
        }

        JTextField fldTen   = new JTextField(20);
        JTextField fldEmail = new JTextField(20);
        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(new JLabel("Số điện thoại:"));  panel.add(new JLabel(sdt));
        panel.add(new JLabel("Họ và tên (*):"));  panel.add(fldTen);
        panel.add(new JLabel("Email:"));           panel.add(fldEmail);
        panel.add(new JLabel(""));
        panel.add(new JLabel("<html><i style='color:gray'>Điểm ban đầu = 0</i></html>"));

        int result = JOptionPane.showConfirmDialog(this, panel, "Tạo khách hàng mới",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String hoTen = fldTen.getText().trim();
        String email = fldEmail.getText().trim();

        // Gọi Controller — không gọi DAO trực tiếp
        ActionResult ar = ctrl.createKH(hoTen, sdt, email.isEmpty() ? null : email);
        if (ar.success) {
            currentKH = ctrl.findKHAfterCreate(sdt);
            if (currentKH != null) {
                maKH = currentKH.getMaKH(); diemHienCo = 0;
                lblTenKH.setText(currentKH.getHoTen() + "  (mới tạo)");
                lblTenKH.setForeground(SUCCESS);
                lblDiemHienCo.setText("0 điểm");
            }
            showMsg("Tạo khách hàng thành công!");
        } else {
            showMsg(ar.message);
        }
        recalc();
    }

    // ── Tính lại UI (không có SQL) ────────────────────────────
    private void recalc() {
        double tongGoc = calcTongGoc();

        int diemDung = 0;
        try { diemDung = Math.max(0, Integer.parseInt(txtDiemSuDung.getText().trim())); }
        catch (NumberFormatException ignored) {}

        if (diemDung > diemHienCo) {
            diemDung = diemHienCo;
            txtDiemSuDung.setText(String.valueOf(diemDung));
        }

        double giamTienDiem = Math.min((double) diemDung * DIEM_TO_VND, tongGoc);
        int    diemThucDung = (int) Math.floor(giamTienDiem / DIEM_TO_VND);
        giamTienDiem = diemThucDung * DIEM_TO_VND;

        double tongPhaiTra = Math.max(0, tongGoc - giamTienDiem);
        int    diemCong    = (int) Math.floor(tongGoc / VND_PER_DIEM);
        int    diemSauGD   = diemHienCo - diemThucDung + diemCong;

        lblTongGoc.setText(String.format("%,.0f VNĐ", tongGoc));
        lblTongGoc.setForeground(TEXT_MAIN);

        if (diemThucDung > 0) {
            lblGiamDiem.setText(String.format("-%,.0f VNĐ  (%d điểm)", giamTienDiem, diemThucDung));
            lblGiamDiem.setForeground(SUCCESS);
        } else {
            lblGiamDiem.setText("Không sử dụng điểm");
            lblGiamDiem.setForeground(TEXT_MUTED);
        }

        lblTongPhaiTra.setText(String.format("%,.0f VNĐ", tongPhaiTra));
        lblTongPhaiTra.setForeground(ACCENT_LIGHT);

        if (maKH > 0) {
            lblDiemSeSau.setText(String.format(
                "%d điểm  (hiện: %d  −  dùng: %d  +  cộng: %d)",
                diemSauGD, diemHienCo, diemThucDung, diemCong));
            lblDiemSeSau.setForeground(GOLD);
        } else {
            lblDiemSeSau.setText("Không tích điểm (khách vãng lai)");
            lblDiemSeSau.setForeground(TEXT_MUTED);
        }
    }

    private double calcTongGoc() {
        double total = 0;
        for (CartItem item : cart) total += item.donGia * item.soLuong;
        return total;
    }

    // =========================================================
    // FOOTER
    // =========================================================
    private JPanel buildFooter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(8, 20, 14, 20));

        JSeparator sep = new JSeparator();
        sep.setForeground(DIVIDER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnRow.setBackground(BG);

        btnBack = new PillButton("Quay lại", INPUT_BG, TEXT_DARK);
        btnBack.setPreferredSize(new Dimension(110, 36));
        btnBack.setVisible(false);
        btnBack.addActionListener(e -> { if (currentStep > 1) showStep(currentStep - 1); });

        btnNext = new PillButton("Tiếp theo →", ACCENT, WHITE);
        btnNext.setPreferredSize(new Dimension(130, 36));
        btnNext.addActionListener(e -> goToStep2());

        btnConfirm = new PillButton("Xác nhận thanh toán", BTN_GREEN, TEXT_DARK);
        btnConfirm.setPreferredSize(new Dimension(180, 36));
        btnConfirm.setVisible(false);
        btnConfirm.addActionListener(e -> doConfirmPayment());

        PillButton btnCancel = new PillButton("Hủy", BTN_RED, WHITE);
        btnCancel.setPreferredSize(new Dimension(80, 36));
        btnCancel.addActionListener(e -> dispose());

        btnRow.add(btnCancel);
        btnRow.add(btnBack);
        btnRow.add(btnNext);
        btnRow.add(btnConfirm);

        p.add(sep, BorderLayout.NORTH);
        p.add(btnRow, BorderLayout.CENTER);
        return p;
    }

    // =========================================================
    // NAVIGATION
    // =========================================================
    private void showStep(int step) {
        currentStep = step;
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "step" + step);
        btnBack.setVisible(step > 1);
        btnNext.setVisible(step == 1);
        btnConfirm.setVisible(step == 2);
        updateStepIndicator(step);
    }

    private void goToStep2() {
        if (cart.isEmpty()) {
            showMsg("Giỏ hàng đang trống!\nVui lòng chọn ít nhất một sản phẩm.");
            return;
        }
        loadStep2Data();
        showStep(2);
    }

    // =========================================================
    // XÁC NHẬN THANH TOÁN — gọi Controller, không có SQL
    // =========================================================
    private void doConfirmPayment() {
        if (cart.isEmpty()) { showMsg("Giỏ hàng trống!"); return; }

        int diemDung = 0;
        try { diemDung = Math.max(0, Integer.parseInt(txtDiemSuDung.getText().trim())); }
        catch (NumberFormatException e) {
            showMsg("Điểm sử dụng phải là số nguyên hợp lệ!"); return;
        }
        if (diemDung > diemHienCo) {
            showMsg("Không đủ điểm!\nHiện có: " + diemHienCo + ", muốn dùng: " + diemDung);
            txtDiemSuDung.setText(String.valueOf(diemHienCo));
            recalc(); return;
        }

        // Preview xác nhận (tính ở UI — transaction thực ở Service)
        double tongGoc      = calcTongGoc();
        double giamTienDiem = Math.min((double) diemDung * DIEM_TO_VND, tongGoc);
        int    diemThucDung = (int) Math.floor(giamTienDiem / DIEM_TO_VND);
        giamTienDiem        = diemThucDung * DIEM_TO_VND;
        double tongPhaiTra  = Math.max(0, tongGoc - giamTienDiem);
        int    diemCong     = (int) Math.floor(tongGoc / VND_PER_DIEM);

        StringBuilder sb = new StringBuilder("Xác nhận thanh toán hóa đơn?\n\n");
        sb.append("  Khách hàng    : ").append(currentKH != null ? currentKH.getHoTen() : "Khách vãng lai").append("\n");
        sb.append(String.format("  Tổng gốc      : %,.0f VNĐ%n", tongGoc));
        if (diemThucDung > 0)
            sb.append(String.format("  Giảm điểm     : -%,.0f VNĐ (%d điểm)%n", giamTienDiem, diemThucDung));
        sb.append(String.format("  Tổng phải trả : %,.0f VNĐ%n", tongPhaiTra));
        if (maKH > 0)
            sb.append(String.format("  Điểm cộng     : +%d điểm (sau GD: %d điểm)%n",
                diemCong, diemHienCo - diemThucDung + diemCong));
        sb.append("\nSản phẩm:\n");
        for (CartItem item : cart)
            sb.append("  • ").append(item.tenGame)
              .append(" [").append(item.loaiSP).append("]")
              .append(String.format("  x%d  %,.0f VNĐ%n", item.soLuong, item.donGia * item.soLuong));

        int confirm = JOptionPane.showConfirmDialog(this, sb.toString(),
            "Xác nhận thanh toán", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        // ── Gọi Controller — toàn bộ transaction ở Service/DAO ──
        ActionResult result = ctrl.confirmPayment(maKH, diemThucDung, cart);

        if (result.success) {
            // result.message = "HĐ{id}"
            StringBuilder doneMsg = new StringBuilder();
            doneMsg.append(String.format("✅ Tạo hóa đơn thành công!  (%s)%n%n", result.message));
            doneMsg.append(String.format("  Tổng tiền  : %,.0f VNĐ%n", tongPhaiTra));
            if (diemCong > 0)
                doneMsg.append(String.format("  Điểm cộng  : +%d điểm%n", diemCong));

            boolean hasROM = cart.stream().anyMatch(i -> "ROM".equals(i.loaiSP));
            boolean hasCD  = cart.stream().anyMatch(i -> "CD".equals(i.loaiSP));
            if (hasCD)  doneMsg.append("\nCD: vui lòng giao đĩa cho khách.\n");
            if (hasROM) {
                doneMsg.append("\n📥 ROM — Link tải game:\n");
                for (CartItem item : cart) {
                    if ("ROM".equals(item.loaiSP)) {
                        String link = ctrl.getROMLink(item.maSP);
                        doneMsg.append("  • ").append(item.tenGame).append(": ")
                               .append(link != null ? link : "(chưa có link)").append("\n");
                    }
                }
            }
            JOptionPane.showMessageDialog(this, doneMsg.toString(),
                "Thanh toán thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            showMsg("Thanh toán thất bại!\n" + result.message);
        }
    }

    // =========================================================
    // STATIC HELPER — mở dialog và chọn sẵn game
    // =========================================================
    public static void openAndPreselectGame(Frame parent, int maGame, String loaiSP) {
        InvoiceAddDialog dlg = new InvoiceAddDialog(parent);
        SwingUtilities.invokeLater(() -> {
            int gameRow = -1;
            if (dlg.gameList != null) {
                for (int i = 0; i < dlg.gameList.size(); i++) {
                    if ((int) dlg.gameList.get(i)[0] == maGame) { gameRow = i; break; }
                }
            }
            if (gameRow >= 0) {
                final int row = gameRow;
                dlg.tblGame.setRowSelectionInterval(row, row);
                dlg.tblGame.scrollRectToVisible(dlg.tblGame.getCellRect(row, 0, true));
                SwingUtilities.invokeLater(() -> {
                    if (dlg.spList != null) {
                        for (int j = 0; j < dlg.spList.size(); j++) {
                            Object[] sp = dlg.spList.get(j);
                            if (loaiSP.equals(sp[2]) && (boolean) sp[5]) {
                                dlg.tblSP.setRowSelectionInterval(j, j);
                                dlg.tblSP.scrollRectToVisible(dlg.tblSP.getCellRect(j, 0, true));
                                break;
                            }
                        }
                    }
                });
            }
        });
        dlg.setVisible(true);
    }

    // =========================================================
    // UI HELPERS
    // =========================================================
    private void showMsg(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private JTextField makeInput() {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(180, 34));
        tf.setBackground(INPUT_BG); tf.setForeground(TEXT_DARK);
        tf.setCaretColor(ACCENT); tf.setFont(F_CELL);
        tf.setBorder(new CompoundBorder(
            new LineBorder(DIVIDER, 1, true), new EmptyBorder(5, 8, 5, 8)));
        return tf;
    }

    private JLabel resultLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(F_VALUE); l.setForeground(color);
        return l;
    }

    private void addFormLabel(JPanel form, GridBagConstraints gc,
                               String text, int row, int col, int w) {
        gc.gridx = col; gc.gridy = row; gc.weightx = 0; gc.gridwidth = 1;
        JLabel l = new JLabel(text);
        l.setFont(F_LABEL); l.setForeground(TEXT_MUTED);
        l.setPreferredSize(new Dimension(w, 30));
        form.add(l, gc);
    }

    private void addDivider(JPanel form, GridBagConstraints gc, int row, int cols) {
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = cols;
        JPanel div = new JPanel();
        div.setBackground(DIVIDER);
        div.setPreferredSize(new Dimension(0, 1));
        form.add(div, gc);
        gc.gridwidth = 1;
    }

    private JTable makeTable(DefaultTableModel model) {
        JTable t = new JTable(model) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? new Color(248, 246, 255) : WHITE);
                    c.setForeground(TEXT_DARK);
                } else {
                    c.setBackground(ACCENT); c.setForeground(WHITE);
                }
                return c;
            }
        };
        t.setFont(F_CELL); t.setRowHeight(32);
        t.setShowGrid(false); t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(ACCENT); t.setSelectionForeground(WHITE);
        t.setBackground(WHITE);
        JTableHeader h = t.getTableHeader();
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable tbl, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = new JLabel(v == null ? "" : v.toString());
                lbl.setFont(F_HDR); lbl.setForeground(WHITE);
                lbl.setBackground(ACCENT); lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(6, 10, 6, 10));
                return lbl;
            }
        });
        h.setBackground(ACCENT); h.setPreferredSize(new Dimension(0, 34));
        return t;
    }

    private JScrollPane wrapScroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(new LineBorder(DIVIDER, 1, true));
        sp.getViewport().setBackground(WHITE);
        return sp;
    }

    // =========================================================
    // PILL BUTTON
    // =========================================================
    static class PillButton extends JButton {
        private final Color bg, fg;
        PillButton(String text, Color bg, Color fg) {
            super(text); this.bg = bg; this.fg = fg;
            setFocusPainted(false); setContentAreaFilled(false); setBorderPainted(false);
            setForeground(fg); setFont(F_BTN);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover() ? bg.darker() : bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            super.paintComponent(g2); g2.dispose();
        }
    }
}