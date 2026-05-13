package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.dao.CustomerDAO;
import otkhongluong.gamestoremanagement.model.Customer;
import otkhongluong.gamestoremanagement.util.DBConnection;
import otkhongluong.gamestoremanagement.util.Session;

import java.util.LinkedHashMap; // ✅ THÊM
import java.util.Map;           // ✅ THÊM
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * InvoiceAddDialog — Wizard 2 bước tạo hóa đơn mua game

Bước 1 : Chọn game + loại sản phẩm (CD / ROM), thêm vào giỏ hàng
Bước 2 : Nhập thông tin KH, dùng điểm, xác nhận thanh toán

LOGIC ĐIỂM:
  Cộng điểm  : cứ 100.000 VNĐ (tổng gốc) = 1 điểm
  Dùng điểm  : 1 điểm = 5.000 VNĐ giảm, không vượt quá tổng gốc

DATABASE:
  HOADON       : MaHD, MaKH, MaNV, NgayLap, TongTien, DiemSuDung, TienGiam, TrangThai
  CTHOADON     : MaHD, MaSP, SoLuong, DonGia
  CD           : TrangThai = 'DaBan' sau khi bán
  ROM          : SoLuotBan++ sau khi bán
  KHACHHANG    : DiemTichLuy cộng/trừ
  DIEM_LICHSU  : ghi log cộng/trừ điểm
 */
public class InvoiceAddDialog extends JDialog {

    // =========================================================
    // PALETTE — giữ nhất quán với RentAddDialog
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
    private static final Color BTN_ORANGE   = new Color(255, 160, 50);

    // =========================================================
    // FONTS
    // =========================================================
    private static final Font F_TITLE    = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font F_SECTION  = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font F_LABEL    = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font F_BOLD     = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font F_CELL     = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_HDR      = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font F_VALUE    = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font F_MONEY    = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font F_BTN      = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font F_HINT     = new Font("Segoe UI", Font.ITALIC, 11);
    private static final Font F_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);

    // =========================================================
    // CONSTANTS
    // =========================================================
    private static final int    VND_PER_DIEM  = 100_000; // 100k = 1 điểm tích lũy
    private static final int    DIEM_TO_VND   = 5_000;   // 1 điểm = 5k giảm

    // =========================================================
    // DAO
    // =========================================================
    private final CustomerDAO khDAO = new CustomerDAO();

    // =========================================================
    // STATE — Bước 1
    // =========================================================
    /** Mỗi mục trong giỏ: {MaSP, MaCD_or_-1, TenGame, LoaiSP, DonGia, SoLuong, MaGame} */
    private final List<CartItem> cart = new ArrayList<>();

    // Bảng game bên trái
    private JTable          tblGame;
    private DefaultTableModel tblGameModel;
    List<Object[]>  gameList; // {MaGame, TenGame, TheLoai, NenTang}

    // Bảng sản phẩm bên phải (CD/ROM của game đang chọn)
    private JTable          tblSP;
    private DefaultTableModel tblSPModel;
    List<Object[]>  spList;  // {MaSP, LoaiSP, GiaBan, TonKho_or_SoLuotBan, ThongTin}

    // Bảng giỏ hàng bên dưới
    private JTable          tblCart;
    private DefaultTableModel tblCartModel;

    private JLabel          lblCartTotal;

    // =========================================================
    // STATE — Bước 2
    // =========================================================
    private JTextField      txtSDT;
    private JLabel          lblTenKH, lblDiemHienCo;
    private JTextField      txtDiemSuDung;
    private JLabel          lblTongGoc, lblGiamDiem, lblTongPhaiTra, lblDiemSeSau;

    private Customer       currentKH   = null;
    private int             maKH        = -1;
    private int             diemHienCo  = 0;

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
        setSize(900, 620);
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

        add(buildFooter(),   BorderLayout.SOUTH);
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
                    lbl.setForeground(s == active ? ACCENT_LIGHT : (s < active ? SUCCESS : TEXT_MUTED));
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

        // === PHẦN TRÊN: game list (trái) + SP list (phải) ===
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

        // Tô màu dòng dựa trên tình trạng (hết hàng → đỏ nhạt)
        tblSP.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected && spList != null && row < spList.size()) {
                    Object[] sp = spList.get(row);
                    boolean available = (boolean) sp[5];
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

        // Nút Thêm vào giỏ
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

        // === PHẦN DƯỚI: Giỏ hàng ===
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
            public boolean isCellEditable(int r, int c) { return c == 5; } // SoLuong chỉ ROM
        };
        tblCart = makeTable(tblCartModel);
        tblCart.getColumnModel().getColumn(0).setPreferredWidth(40);
        tblCart.getColumnModel().getColumn(1).setPreferredWidth(240);
        tblCart.getColumnModel().getColumn(2).setPreferredWidth(60);
        tblCart.getColumnModel().getColumn(3).setPreferredWidth(90);
        tblCart.getColumnModel().getColumn(4).setPreferredWidth(130);
        tblCart.getColumnModel().getColumn(5).setPreferredWidth(50);
        tblCart.getColumnModel().getColumn(6).setPreferredWidth(130);

        // Cho phép sửa số lượng của ROM trực tiếp trên bảng
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

        // Load dữ liệu ban đầu
        loadGameTable();
        return p;
    }

    private void loadGameTable() {
        tblGameModel.setRowCount(0);
        gameList = new ArrayList<>();
        String sql = "SELECT g.MaGame, g.TenGame, g.TheLoai, g.NenTang " +
                     "FROM GAME g " +
                     "WHERE EXISTS (" +
                     "  SELECT 1 FROM SANPHAM sp " +
                     "  WHERE sp.MaGame = g.MaGame AND sp.GiaBan IS NOT NULL AND sp.GiaBan > 0" +
                     ") " +
                     "ORDER BY g.TenGame";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int    maGame   = rs.getInt("MaGame");
                String tenGame  = rs.getString("TenGame");
                String theLoai  = rs.getString("TheLoai") != null ? rs.getString("TheLoai") : "";
                String nenTang  = rs.getString("NenTang") != null ? rs.getString("NenTang") : "";
                gameList.add(new Object[]{maGame, tenGame, theLoai, nenTang});
                tblGameModel.addRow(new Object[]{"G" + maGame, tenGame, theLoai});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showMsg("Lỗi tải danh sách game: " + ex.getMessage());
        }
        tblSPModel.setRowCount(0);
        spList = null;
    }

    private void loadSPTable() {
        tblSPModel.setRowCount(0);
        spList = new ArrayList<>();
        int row = tblGame.getSelectedRow();
        if (row < 0 || gameList == null || row >= gameList.size()) return;
        int maGame = (int) gameList.get(row)[0];

        // Load CD còn sẵn sàng
        String sqlCD = "SELECT sp.MaSP, cd.MaCD, sp.GiaBan, cd.TinhTrang " +
                       "FROM SANPHAM sp JOIN CD cd ON sp.MaSP = cd.MaSP " +
                       "WHERE sp.MaGame = ? AND cd.TrangThai = N'SanSang' AND sp.GiaBan IS NOT NULL AND sp.GiaBan > 0";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlCD)) {
            ps.setInt(1, maGame);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int    maSP      = rs.getInt("MaSP");
                int    maCD      = rs.getInt("MaCD");
                double giaBan    = rs.getDouble("GiaBan");
                String tinhTrang = rs.getString("TinhTrang") != null ? rs.getString("TinhTrang") : "Tốt";
                // {MaSP, MaCD_or_-1, loaiSP, giaBan, thongTin, available, MaGame}
                spList.add(new Object[]{maSP, maCD, "CD", giaBan, "Tình trạng: " + tinhTrang, true, maGame});
                tblSPModel.addRow(new Object[]{
                    "SP" + maSP,
                    "CD",
                    String.format("%,.0f VNĐ", giaBan),
                    "CD" + maCD + " — " + tinhTrang
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Đếm CD hết hàng (TrangThai != SanSang)
        String sqlCDHet =
            "SELECT " +
            "  SUM(CASE WHEN cd.TrangThai='DangThue' THEN 1 ELSE 0 END) as SoDangThue, " +
            "  SUM(CASE WHEN cd.TrangThai='DaBan'    THEN 1 ELSE 0 END) as SoDaBan, " +
            "  SUM(CASE WHEN cd.TrangThai='Hong'     THEN 1 ELSE 0 END) as SoHong " +
            "FROM CD cd JOIN SANPHAM sp ON cd.MaSP = sp.MaSP " +
            "WHERE sp.MaGame = ? AND cd.TrangThai != N'SanSang'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlCDHet)) {
            ps.setInt(1, maGame);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int dangThue = rs.getInt("SoDangThue");
                int daBan    = rs.getInt("SoDaBan");
                int hong     = rs.getInt("SoHong");

                if (dangThue > 0) {
                    spList.add(new Object[]{-1, -1, "CD", 0.0, "DangThue", false, maGame});
                    tblSPModel.addRow(new Object[]{"—", "CD", "—",
                        "🔒 " + dangThue + " CD đang được thuê"});
                }
                if (daBan > 0) {
                    spList.add(new Object[]{-1, -1, "CD", 0.0, "DaBan", false, maGame});
                    tblSPModel.addRow(new Object[]{"—", "CD", "—",
                        "💰 " + daBan + " CD đã bán"});
                }
                if (hong > 0) {
                    spList.add(new Object[]{-1, -1, "CD", 0.0, "Hong", false, maGame});
                    tblSPModel.addRow(new Object[]{"—", "CD", "—",
                        "⚠ " + hong + " CD hỏng / mất"});
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }

        // Load ROM
        String sqlROM = "SELECT sp.MaSP, r.DungLuong, r.LinkLuuTru, sp.GiaBan " +
                        "FROM SANPHAM sp JOIN ROM r ON sp.MaSP = r.MaSP " +
                        "WHERE sp.MaGame = ? AND sp.GiaBan IS NOT NULL AND sp.GiaBan > 0";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlROM)) {
            ps.setInt(1, maGame);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int    maSP     = rs.getInt("MaSP");
                String dungLuong = rs.getString("DungLuong") != null ? rs.getString("DungLuong") : "N/A";
                double giaBan   = rs.getDouble("GiaBan");
                spList.add(new Object[]{maSP, -1, "ROM", giaBan, dungLuong, true, maGame});
                tblSPModel.addRow(new Object[]{
                    "SP" + maSP,
                    "ROM",
                    String.format("%,.0f VNĐ", giaBan),
                    "Tải về — " + dungLuong
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void doAddToCart() {
        int gameRow = tblGame.getSelectedRow();
        int spRow   = tblSP.getSelectedRow();
        if (gameRow < 0) { showMsg("Vui lòng chọn game!"); return; }
        if (spRow < 0)   { showMsg("Vui lòng chọn loại sản phẩm (CD hoặc ROM)!"); return; }
        if (spList == null || spRow >= spList.size()) return;

        Object[] sp = spList.get(spRow);
        boolean available = (boolean) sp[5];
        if (!available) {
            showMsg("Sản phẩm này đã hết hàng!\nVui lòng chọn CD khác hoặc chọn ROM.");
            return;
        }

        int    maSP    = (int)    sp[0];
        int    maCD    = (int)    sp[1];
        String loai    = (String) sp[2];
        double giaBan  = (double) sp[3];
        int    maGame  = (int)    sp[6];
        String tenGame = (String) gameList.get(gameRow)[1];

        // Kiểm tra trùng: CD thì trùng theo MaSP+MaCD, ROM trùng theo MaSP
        String newKey = "CD".equals(loai)
            ? "CD_" + maCD          // CD: unique theo MaCD
            : "ROM_" + maSP;        // ROM: unique theo MaSP

        for (CartItem item : cart) {
            if (item.cartKey.equals(newKey)) {
                if ("CD".equals(loai))
                    showMsg("CD" + maCD + " này đã có trong giỏ hàng!");
                else
                    showMsg("ROM game này đã có trong giỏ!\nBạn có thể điều chỉnh số lượng trong cột SL.");
                return;
            }
        }

        CartItem item = new CartItem();
        item.maSP    = maSP;
        item.maCD    = maCD;
        item.maGame  = maGame;
        item.tenGame = tenGame;
        item.loaiSP  = loai;
        item.donGia  = giaBan;
        item.soLuong = 1;
        item.cartKey = "CD".equals(loai) ? "CD_" + maCD : "ROM_" + maSP;  // ← gán key
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

        // --- Tóm tắt giỏ hàng (bảng nhỏ) ---
        JPanel cartSummaryPanel = new JPanel(new BorderLayout(0, 4));
        cartSummaryPanel.setBackground(BG);
        JLabel lblCartSum = new JLabel("Giỏ hàng đã chọn:");
        lblCartSum.setFont(F_SECTION);
        lblCartSum.setForeground(GOLD);
        cartSummaryPanel.add(lblCartSum, BorderLayout.NORTH);

        String[] summCols = {"Tên game", "Loại", "Đơn giá", "SL", "Thành tiền"};
        DefaultTableModel summModel = new DefaultTableModel(summCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        // Sẽ được điền lúc loadStep2Data()
        JTable tblSumm = makeTable(summModel);
        tblSumm.getColumnModel().getColumn(0).setPreferredWidth(280);
        tblSumm.getColumnModel().getColumn(1).setPreferredWidth(60);
        tblSumm.getColumnModel().getColumn(2).setPreferredWidth(130);
        tblSumm.getColumnModel().getColumn(3).setPreferredWidth(40);
        tblSumm.getColumnModel().getColumn(4).setPreferredWidth(130);
        JScrollPane spSumm = wrapScroll(tblSumm);
        spSumm.setPreferredSize(new Dimension(0, 150));
        cartSummaryPanel.add(spSumm, BorderLayout.CENTER);

        // Lưu ref để điền dữ liệu sau
        this.summaryTableModel = summModel;

        p.add(cartSummaryPanel, BorderLayout.NORTH);

        // --- Form KH & điểm ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 0, 5, 10);

        int row = 0;

        // SĐT + Tìm + Tạo
        txtSDT = makeInput();
        PillButton btnTim = new PillButton("Tìm", ACCENT, WHITE);
        btnTim.setPreferredSize(new Dimension(80, 34));
        btnTim.addActionListener(e -> doFindKH());
        PillButton btnTao = new PillButton("Tạo mới", SUCCESS, TEXT_DARK);
        btnTao.setPreferredSize(new Dimension(100, 34));
        btnTao.addActionListener(e -> doCreateKH());
        PillButton btnSkip = new PillButton("Bỏ qua", new Color(90, 70, 140), WHITE);
        btnSkip.setPreferredSize(new Dimension(90, 34));
        btnSkip.setToolTipText("Khách không cung cấp SĐT — bỏ qua tích điểm");
        btnSkip.addActionListener(e -> doSkipKH());

        txtSDT.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { doFindKH(); }
            public void keyPressed(KeyEvent e)  { if (e.getKeyCode() == KeyEvent.VK_ENTER) doFindKH(); }
        });

        addFormLabel(form, gc, "Số điện thoại KH:", row, 0, 140);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0;
        form.add(txtSDT, gc);
        gc.gridx = 2; gc.weightx = 0;
        form.add(btnTim, gc);
        gc.gridx = 3;
        form.add(btnTao, gc);
        gc.gridx = 4;
        form.add(btnSkip, gc);
        gc.gridwidth = 1;

        // Tên KH
        row++;
        lblTenKH = new JLabel("---");
        lblTenKH.setFont(F_VALUE);
        lblTenKH.setForeground(TEXT_MAIN);
        addFormLabel(form, gc, "Tên khách hàng:", row, 0, 140);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 4;
        form.add(lblTenKH, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        // Điểm hiện có
        row++;
        lblDiemHienCo = new JLabel("---");
        lblDiemHienCo.setFont(F_VALUE);
        lblDiemHienCo.setForeground(GOLD);
        addFormLabel(form, gc, "Điểm tích lũy:", row, 0, 140);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 4;
        form.add(lblDiemHienCo, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        // Divider
        row++;
        addDivider(form, gc, row, 5);

        // Điểm sử dụng
        row++;
        txtDiemSuDung = makeInput();
        txtDiemSuDung.setText("0");
        txtDiemSuDung.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { recalc(); }
        });
        JLabel lblHintDiem = new JLabel("(1 điểm = 5.000 VNĐ giảm, không vượt quá tổng gốc)");
        lblHintDiem.setFont(F_HINT);
        lblHintDiem.setForeground(TEXT_MUTED);

        addFormLabel(form, gc, "Điểm muốn dùng:", row, 0, 140);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 0.4;
        form.add(txtDiemSuDung, gc);
        gc.gridx = 2; gc.weightx = 0.6; gc.gridwidth = 3;
        form.add(lblHintDiem, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        // Divider
        row++;
        addDivider(form, gc, row, 5);

        // Kết quả tính
        row++;
        lblTongGoc = resultLabel("---", TEXT_MAIN);
        addFormLabel(form, gc, "Tổng tiền gốc:", row, 0, 140);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 4;
        form.add(lblTongGoc, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        row++;
        lblGiamDiem = resultLabel("---", SUCCESS);
        addFormLabel(form, gc, "Giảm từ điểm:", row, 0, 140);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 4;
        form.add(lblGiamDiem, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        row++;
        lblTongPhaiTra = resultLabel("---", ACCENT_LIGHT);
        addFormLabel(form, gc, "Tổng tiền phải trả:", row, 0, 140);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 4;
        form.add(lblTongPhaiTra, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        row++;
        lblDiemSeSau = resultLabel("---", GOLD);
        addFormLabel(form, gc, "Điểm sau giao dịch:", row, 0, 140);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 4;
        form.add(lblDiemSeSau, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        // Filler
        row++;
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 5; gc.weighty = 1.0;
        gc.fill = GridBagConstraints.BOTH;
        form.add(Box.createVerticalGlue(), gc);
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weighty = 0;

        p.add(form, BorderLayout.CENTER);
        return p;
    }

    // ref để điền bảng tóm tắt giỏ hàng ở bước 2
    private DefaultTableModel summaryTableModel;

    private void loadStep2Data() {
        // Điền bảng tóm tắt
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

        // Reset KH
        txtSDT.setText("");
        txtDiemSuDung.setText("0");
        lblTenKH.setText("Chưa tìm kiếm — nhập SĐT hoặc nhấn \"Bỏ qua\"");
        lblTenKH.setForeground(TEXT_MUTED);
        lblDiemHienCo.setText("---");
        currentKH = null; maKH = -1; diemHienCo = 0;
        recalc();
    }

    private void doFindKH() {
        String sdt = txtSDT.getText().trim();
        if (sdt.isEmpty()) {
            currentKH = null; maKH = -1; diemHienCo = 0;
            lblTenKH.setText("Chưa nhập số điện thoại");
            lblTenKH.setForeground(TEXT_MUTED);
            lblDiemHienCo.setText("---");
            recalc(); return;
        }
        Customer kh = khDAO.findBySDT(sdt);
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

    private void doCreateKH() {
        String sdt = txtSDT.getText().trim();
        if (sdt.isEmpty()) { showMsg("Vui lòng nhập số điện thoại trước!"); return; }

        Customer existing = khDAO.findBySDT(sdt);
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
        if (hoTen.isEmpty()) { showMsg("Họ và tên không được để trống!"); return; }

        Customer kh = new Customer();
        kh.setHoTen(hoTen); kh.setSdt(sdt); kh.setEmail(email.isEmpty() ? null : email);
        kh.setDiemTichLuy(0);
        if (khDAO.insert(kh)) {
            currentKH = khDAO.findBySDT(sdt); maKH = currentKH.getMaKH(); diemHienCo = 0;
            lblTenKH.setText(currentKH.getHoTen() + "  (mới tạo)");
            lblTenKH.setForeground(SUCCESS);
            lblDiemHienCo.setText("0 điểm");
            showMsg("Tạo khách hàng thành công!");
        } else {
            showMsg("Tạo khách hàng thất bại!");
        }
        recalc();
    }

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

        // Điểm cộng sau mua = floor(tongGoc / VND_PER_DIEM)
        int diemCong    = (int) Math.floor(tongGoc / VND_PER_DIEM);
        int diemSauGD   = diemHienCo - diemThucDung + diemCong;

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
            showMsg("Giỏ hàng đang trống!\nVui lòng chọn ít nhất một sản phẩm."); return;
        }
        loadStep2Data();
        showStep(2);
    }

    // =========================================================
    // XÁC NHẬN — Tạo hóa đơn
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

        double tongGoc = calcTongGoc();
        double giamTienDiem = Math.min((double) diemDung * DIEM_TO_VND, tongGoc);
        int    diemThucDung = (int) Math.floor(giamTienDiem / DIEM_TO_VND);
        giamTienDiem = diemThucDung * DIEM_TO_VND;
        double tongPhaiTra  = Math.max(0, tongGoc - giamTienDiem);
        int    diemCong     = (int) Math.floor(tongGoc / VND_PER_DIEM);

        // Popup xác nhận
        StringBuilder sb = new StringBuilder();
        sb.append("Xác nhận thanh toán hóa đơn?\n\n");
        sb.append("  Khách hàng    : ").append(currentKH != null ? currentKH.getHoTen() : "Khách vãng lai").append("\n");
        sb.append(String.format("  Tổng gốc      : %,.0f VNĐ%n", tongGoc));
        if (diemThucDung > 0)
            sb.append(String.format("  Giảm điểm     : -%,.0f VNĐ (%d điểm)%n", giamTienDiem, diemThucDung));
        sb.append(String.format("  Tổng phải trả : %,.0f VNĐ%n", tongPhaiTra));
        if (maKH > 0)
            sb.append(String.format("  Điểm cộng     : +%d điểm (sau GD: %d điểm)%n",
                diemCong, diemHienCo - diemThucDung + diemCong));
        sb.append("\nSản phẩm:\n");
        for (CartItem item : cart) {
            sb.append("  • ").append(item.tenGame)
              .append(" [").append(item.loaiSP).append("]")
              .append(String.format("  x%d  %,.0f VNĐ%n", item.soLuong, item.donGia * item.soLuong));
        }

        int confirm = JOptionPane.showConfirmDialog(this, sb.toString(),
            "Xác nhận thanh toán", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        // Thực hiện tạo hóa đơn trong transaction
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. Kiểm tra lại tình trạng các CD (phòng race condition)
                for (CartItem item : cart) {
                    if ("CD".equals(item.loaiSP)) {
                        String chkSQL = "SELECT TrangThai FROM CD WHERE MaCD = ?";
                        try (PreparedStatement ps = con.prepareStatement(chkSQL)) {
                            ps.setInt(1, item.maCD);
                            ResultSet rs = ps.executeQuery();
                            if (rs.next()) {
                                String trangThai = rs.getString("TrangThai");
                                if (!"SanSang".equals(trangThai)) {
                                    con.rollback();
                                    showMsg("CD" + item.maCD + " — \"" + item.tenGame + "\" vừa bị người khác mua!\n"
                                           + "Vui lòng quay lại và chọn CD khác hoặc chọn ROM.");
                                    return;
                                }
                            } else {
                                con.rollback();
                                showMsg("Không tìm thấy CD" + item.maCD + "!"); return;
                            }
                        }
                    }
                }

                // 2. Xử lý khách vãng lai nếu chưa có KH
                // ✅ Khách vãng lai: chỉ reset điểm, không INSERT gì cả
                if (maKH == -1) {
                    diemThucDung = 0;
                    giamTienDiem = 0;
                }

                // 3. Tạo HOADON
                String insHD = "INSERT INTO HOADON (MaKH, MaNV, NgayLap, TongTien, DiemSuDung, TienGiam, TrangThai) " +
               "VALUES (?, ?, GETDATE(), ?, ?, ?, N'DaThanhToan')";
                int maHD = -1;   // ← khởi tạo mặc định
                try (PreparedStatement ps = con.prepareStatement(insHD, Statement.RETURN_GENERATED_KEYS)) {
                    if (maKH > 0) ps.setInt(1, maKH);
                    else          ps.setNull(1, java.sql.Types.INTEGER);
                    int maNV = Session.getMaNV();
                    if (maNV > 0) ps.setInt(2, maNV);
                    else          ps.setNull(2, java.sql.Types.INTEGER);
                    ps.setDouble(3, tongPhaiTra);
                    ps.setInt(4, diemThucDung);
                    ps.setDouble(5, giamTienDiem);
                    ps.executeUpdate();
                    ResultSet gk = ps.getGeneratedKeys();
                    if (!gk.next()) throw new SQLException("Không tạo được hóa đơn!");
                    maHD = gk.getInt(1);   // gán vào biến đã khai báo ngoài
                }
                if (maHD == -1) throw new SQLException("Không lấy được mã hóa đơn!");

                // ✅ FIX — gộp SoLuong của các CartItem cùng MaSP
                // Bước 1: gộp cart theo MaSP
                Map<Integer, double[]> spMap = new LinkedHashMap<>();
                // key = MaSP, value = [tongSoLuong, donGia]
                for (CartItem item : cart) {
                    if (spMap.containsKey(item.maSP)) {
                        spMap.get(item.maSP)[0] += item.soLuong;
                    } else {
                        spMap.put(item.maSP, new double[]{item.soLuong, item.donGia});
                    }
                }

                // Bước 2: insert CTHOADON từ map (đã gộp, không còn duplicate)
                String insCT = "INSERT INTO CTHOADON (MaHD, MaSP, SoLuong, DonGia) VALUES (?, ?, ?, ?)";
                for (Map.Entry<Integer, double[]> entry : spMap.entrySet()) {
                    try (PreparedStatement ps = con.prepareStatement(insCT)) {
                        ps.setInt(1, maHD);
                        ps.setInt(2, entry.getKey());               // MaSP
                        ps.setInt(3, (int) entry.getValue()[0]);    // tổng SoLuong
                        ps.setDouble(4, entry.getValue()[1]);        // DonGia
                        ps.executeUpdate();
                    }
                }

                // Bước 3: update CD/ROM vẫn dùng cart gốc (từng item riêng)
                String updCD  = "UPDATE CD SET TrangThai = N'DaBan' WHERE MaCD = ?";
                String updROM = "UPDATE ROM SET SoLuotBan = SoLuotBan + ? WHERE MaSP = ?";
                for (CartItem item : cart) {
                    if ("CD".equals(item.loaiSP)) {
                        try (PreparedStatement ps = con.prepareStatement(updCD)) {
                            ps.setInt(1, item.maCD);
                            ps.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement ps = con.prepareStatement(updROM)) {
                            ps.setInt(1, item.soLuong);
                            ps.setInt(2, item.maSP);
                            ps.executeUpdate();
                        }
                    }
                }

                // 5. Cập nhật điểm KH
                if (maKH > 0 && currentKH != null) {
                    // Trừ điểm đã dùng
                    if (diemThucDung > 0) {
                        khDAO.updatePoint(maKH, -diemThucDung);
                        String logTru = "INSERT INTO DIEM_LICHSU (MaKH, Loai, SoDiem, GhiChu) VALUES (?, N'TRU', ?, ?)";
                        try (PreparedStatement ps = con.prepareStatement(logTru)) {
                            ps.setInt(1, maKH);
                            ps.setInt(2, diemThucDung);
                            ps.setNString(3, "Dùng điểm mua game — HĐ" + maHD);
                            ps.executeUpdate();
                        }
                    }
                    // Cộng điểm mới
                    if (diemCong > 0) {
                        khDAO.updatePoint(maKH, diemCong);
                        String logCong = "INSERT INTO DIEM_LICHSU (MaKH, Loai, SoDiem, GhiChu) VALUES (?, N'CONG', ?, ?)";
                        try (PreparedStatement ps = con.prepareStatement(logCong)) {
                            ps.setInt(1, maKH);
                            ps.setInt(2, diemCong);
                            ps.setNString(3, "Mua game — HĐ" + maHD + String.format(" (tổng %,.0f VNĐ)", tongGoc));
                            ps.executeUpdate();
                        }
                    }
                }

                con.commit();

                // 6. Thông báo hoàn tất + hiển thị link ROM nếu có
                StringBuilder doneMsg = new StringBuilder();
                doneMsg.append(String.format("✅ Tạo hóa đơn thành công!  (HĐ%d)%n%n", maHD));
                doneMsg.append(String.format("  Tổng tiền  : %,.0f VNĐ%n", tongPhaiTra));
                if (diemCong > 0)
                    doneMsg.append(String.format("  Điểm cộng  : +%d điểm%n", diemCong));

                boolean hasROM = cart.stream().anyMatch(i -> "ROM".equals(i.loaiSP));
                boolean hasCD  = cart.stream().anyMatch(i -> "CD".equals(i.loaiSP));

                if (hasCD)  doneMsg.append("%nCD: vui lòng giao đĩa cho khách.%n".formatted());
                if (hasROM) {
                    doneMsg.append("%n📥 ROM — Link tải game:%n".formatted());
                    for (CartItem item : cart) {
                        if ("ROM".equals(item.loaiSP)) {
                            String link = getROMLink(item.maSP);
                            doneMsg.append("  • ").append(item.tenGame).append(": ")
                                   .append(link != null ? link : "(chưa có link)").append("%n".formatted());
                        }
                    }
                }

                JOptionPane.showMessageDialog(this, doneMsg.toString(),
                    "Thanh toán thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();

            } catch (Exception ex) {
                con.rollback();
                ex.printStackTrace();
                showMsg("Thanh toán thất bại!\n" + ex.getMessage() + "\nVui lòng thử lại.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showMsg("Lỗi kết nối cơ sở dữ liệu: " + ex.getMessage());
        }
    }

    private String getROMLink(int maSP) {
        String sql = "SELECT LinkLuuTru FROM ROM WHERE MaSP = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maSP);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("LinkLuuTru");
        } catch (SQLException ignored) {}
        return null;
    }

    // =========================================================
    // HELPER MODEL
    // =========================================================
    private static class CartItem {
        int    maSP, maCD, maGame, soLuong;
        String tenGame, loaiSP, cartKey;   // ← thêm cartKey
        double donGia;
    }

    // =========================================================
    // UI HELPERS
    // =========================================================
    private void showMsg(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private JTextField makeInput() {
        JTextField tf = new JTextField();
        tf.setBackground(INPUT_BG);
        tf.setForeground(TEXT_DARK);
        tf.setCaretColor(ACCENT);
        tf.setFont(F_CELL);
        tf.setBorder(new CompoundBorder(
            new LineBorder(DIVIDER, 1, true),
            new EmptyBorder(5, 8, 5, 8)
        ));
        return tf;
    }

    private JLabel resultLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(F_VALUE);
        l.setForeground(color);
        return l;
    }

    private void addFormLabel(JPanel form, GridBagConstraints gc,
                               String text, int row, int col, int w) {
        gc.gridx = col; gc.gridy = row; gc.weightx = 0; gc.gridwidth = 1;
        JLabel l = new JLabel(text);
        l.setFont(F_LABEL);
        l.setForeground(TEXT_MUTED);
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
                    c.setBackground(ACCENT);
                    c.setForeground(WHITE);
                }
                return c;
            }
        };
        t.setFont(F_CELL);
        t.setRowHeight(32);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(ACCENT);
        t.setSelectionForeground(WHITE);
        t.setBackground(WHITE);

        JTableHeader h = t.getTableHeader();
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable tbl, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = new JLabel(v == null ? "" : v.toString());
                lbl.setFont(F_HDR);
                lbl.setForeground(WHITE);
                lbl.setBackground(ACCENT);
                lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(6, 10, 6, 10));
                return lbl;
            }
        });
        h.setBackground(ACCENT);
        h.setPreferredSize(new Dimension(0, 34));
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
            super(text);
            this.bg = bg; this.fg = fg;
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setForeground(fg);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover() ? bg.darker() : bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            super.paintComponent(g2);
            g2.dispose();
        }
    }
    
    public static void openAndPreselectGame(Frame parent, int maGame, String loaiSP) {
        InvoiceAddDialog dlg = new InvoiceAddDialog(parent);
 
        SwingUtilities.invokeLater(() -> {
            // Bước 1: tìm & chọn dòng game
            int gameRow = -1;
            if (dlg.gameList != null) {
                for (int i = 0; i < dlg.gameList.size(); i++) {
                    if ((int) dlg.gameList.get(i)[0] == maGame) {
                        gameRow = i;
                        break;
                    }
                }
            }
 
            if (gameRow >= 0) {
                final int row = gameRow;
                dlg.tblGame.setRowSelectionInterval(row, row);
                dlg.tblGame.scrollRectToVisible(dlg.tblGame.getCellRect(row, 0, true));
                // ListSelectionListener sẽ gọi loadSPTable() tự động
 
                // Bước 2: chờ loadSPTable() xong rồi chọn SP
                SwingUtilities.invokeLater(() -> {
                    if (dlg.spList != null) {
                        for (int j = 0; j < dlg.spList.size(); j++) {
                            Object[] sp = dlg.spList.get(j);
                            boolean typeMatch     = loaiSP.equals(sp[2]);
                            boolean available     = (boolean) sp[5];
                            if (typeMatch && available) {
                                dlg.tblSP.setRowSelectionInterval(j, j);
                                dlg.tblSP.scrollRectToVisible(dlg.tblSP.getCellRect(j, 0, true));
                                break;
                            }
                        }
                    }
                });
            }
        });
 
        dlg.setVisible(true);   // modal — luồng dừng ở đây cho đến khi đóng dialog
    }
}