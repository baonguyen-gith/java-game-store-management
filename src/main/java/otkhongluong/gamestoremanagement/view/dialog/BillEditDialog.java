package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.dao.HoaDonDAO;
import otkhongluong.gamestoremanagement.dao.KhachHangDAO;
import otkhongluong.gamestoremanagement.dao.NhanVienDAO;
import otkhongluong.gamestoremanagement.model.HoaDon;
import otkhongluong.gamestoremanagement.model.KhachHang;
import otkhongluong.gamestoremanagement.model.NhanVien;
import otkhongluong.gamestoremanagement.service.HoaDonService;
import otkhongluong.gamestoremanagement.util.DBConnection;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * BillEditDialog — Sửa thông tin hóa đơn.
 *
 * Tab 1 "Thông tin đầu phiếu":
 *   - Đổi Khách hàng (tìm theo SĐT) -> tự động chuyển điểm sang KH mới
 *   - Đổi Nhân viên  (tìm theo mã NV)
 *   - Đổi Ngày lập   (dd/MM/yyyy)
 *
 * Tab 2 "Chi tiết sản phẩm":
 *   - Xem danh sách SP hiện có (read-only rows)
 *   - Xóa SP: rollback CD->SanSang hoặc ROM SoLuotBan--
 *   - Thêm SP: chọn từ catalog game/SP còn hàng, CD -> DaBan, ROM SoLuotBan++
 *   - TongTien tự động tính lại và cập nhật HOADON
 *
 * Tất cả thay đổi chỉ được áp vào DB khi nhấn "Lưu tất cả".
 */
public class BillEditDialog extends JDialog {

    /* ══════════════════════════════════════════════════════
       PALETTE — đồng bộ RentEditDialog
    ══════════════════════════════════════════════════════ */
    private static final Color BG           = new Color(35, 20, 85);
    private static final Color CARD_BG      = new Color(50, 30, 105);
    private static final Color ACCENT       = new Color(130, 90, 230);
    private static final Color ACCENT_LIGHT = new Color(155, 135, 245);
    private static final Color GREEN        = new Color(104, 211, 145);
    private static final Color RED          = new Color(252, 129, 129);
    private static final Color YELLOW       = new Color(255, 210, 80);
    private static final Color MUTED        = new Color(160, 150, 200);
    private static final Color WHITE        = Color.WHITE;
    private static final Color INPUT_BG     = Color.WHITE;
    private static final Color TEXT_DARK    = new Color(30, 30, 30);
    private static final Color ROW_ODD      = new Color(245, 242, 255);
    private static final Color TBL_HDR      = new Color(155, 135, 245);
    private static final Color ROW_NEW      = new Color(200, 255, 220);

    /* ══════════════════════════════════════════════════════
       FONTS
    ══════════════════════════════════════════════════════ */
    private static final Font F_TITLE  = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font F_LABEL  = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font F_INPUT  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_HINT   = new Font("Segoe UI", Font.ITALIC, 11);
    private static final Font F_RESULT = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font F_CELL   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font F_HDR    = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font F_MONEY  = new Font("Segoe UI", Font.BOLD, 16);

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /* ══════════════════════════════════════════════════════
       DAO / SERVICE
    ══════════════════════════════════════════════════════ */
    private final KhachHangDAO khDAO = new KhachHangDAO();
    private final NhanVienDAO  nvDAO = new NhanVienDAO();
    private final HoaDonService hdSvc = new HoaDonService();

    /* ══════════════════════════════════════════════════════
       STATE — bản gốc
    ══════════════════════════════════════════════════════ */
    private final HoaDon hd;

    /* ══════════════════════════════════════════════════════
       STATE — Tab 1
    ══════════════════════════════════════════════════════ */
    private KhachHang foundKH   = null;
    private NhanVien  foundNV   = null;
    private LocalDate newNgayLap = null;

    /* ══════════════════════════════════════════════════════
       STATE — Tab 2
    ══════════════════════════════════════════════════════ */
    /** Working copy của các SP trong HĐ (chưa lưu DB). */
    private final List<SpRow> workingItems = new ArrayList<>();
    /** SP mới thêm trong phiên này. */
    private final List<SpRow> addedItems   = new ArrayList<>();
    /** SP bị xóa trong phiên này (cần rollback khi lưu). */
    private final List<SpRow> removedItems = new ArrayList<>();
    /** Catalog dùng trong popup "Thêm SP". */
    private List<Object[]> gameList = new ArrayList<>();
    private List<Object[]> spList   = new ArrayList<>();
    private static final double DIEM_PER_DONG = 100_000.0;
    
    /* ══════════════════════════════════════════════════════
       COMPONENTS — Tab 1
    ══════════════════════════════════════════════════════ */
    private JTextField txtSdt, txtMaNV, txtNgayLap;
    private JLabel     lblKHResult, lblNVResult, lblNgayResult;

    /* ══════════════════════════════════════════════════════
       COMPONENTS — Tab 2
    ══════════════════════════════════════════════════════ */
    private DefaultTableModel detailModel;
    private JTable            detailTable;
    private JLabel            lblTongTien;

    /* ══════════════════════════════════════════════════════
       INNER MODEL
    ══════════════════════════════════════════════════════ */
    private static class SpRow {
        int    maSP, maCD;
        String tenGame, loai;
        double donGia;
        int    soLuong;
        boolean isNew;

        SpRow(int maSP, int maCD, String tenGame, String loai,
              double donGia, int soLuong, boolean isNew) {
            this.maSP = maSP; this.maCD = maCD;
            this.tenGame = tenGame; this.loai = loai;
            this.donGia = donGia; this.soLuong = soLuong;
            this.isNew = isNew;
        }
        double thanhTien() { return donGia * soLuong; }
        /** Unique key to detect duplicates in working list. */
        String key() { return "CD".equals(loai) ? "CD_" + maCD : "ROM_" + maSP; }
    }

    /* ══════════════════════════════════════════════════════
       CONSTRUCTOR
    ══════════════════════════════════════════════════════ */
    public BillEditDialog(Frame parent, int maHD) {
        super(parent, "Sửa Hóa Đơn", true);

        HoaDon loaded = hdSvc.getHoaDonById(maHD);
        if (loaded == null) {
            JOptionPane.showMessageDialog(parent,
                "Không tìm thấy hóa đơn HD" + String.format("%03d", maHD),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            this.hd = new HoaDon();
            dispose();
            return;
        }
        this.hd = loaded;

        setSize(700, 680);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildTabs(),    BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);

        loadWorkingItems();
        prefillTab1();
    }

    /* ══════════════════════════════════════════════════════
       HEADER
    ══════════════════════════════════════════════════════ */
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(20, 24, 0, 24));

        JLabel title = new JLabel("✏  Sửa Hóa Đơn  —  HD" + String.format("%03d", hd.getMaHD()));
        title.setFont(F_TITLE);
        title.setForeground(WHITE);
        p.add(title, BorderLayout.WEST);

        boolean paid = "DaThanhToan".equalsIgnoreCase(hd.getTrangThai());
        JLabel badge = new JLabel(paid ? "✔ Đã thanh toán" : "⏳ Chưa thanh toán");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(paid ? GREEN : YELLOW);
        p.add(badge, BorderLayout.EAST);

        JPanel sep = new JPanel();
        sep.setBackground(ACCENT_LIGHT);
        sep.setPreferredSize(new Dimension(0, 1));
        p.add(sep, BorderLayout.SOUTH);
        return p;
    }

    /* ══════════════════════════════════════════════════════
       TABS
    ══════════════════════════════════════════════════════ */
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabs.setBackground(BG);
        tabs.setForeground(WHITE);
        tabs.setBorder(new EmptyBorder(10, 16, 0, 16));
        tabs.addTab("📋  Thông tin đầu phiếu", buildTab1());
        tabs.addTab("🛒  Chi tiết sản phẩm",   buildTab2());
        return tabs;
    }

    /* ══════════════════════════════════════════════════════
       TAB 1 — Thông tin đầu phiếu
    ══════════════════════════════════════════════════════ */
    private JPanel buildTab1() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(16, 24, 8, 24));

        body.add(buildInfoCards());
        body.add(Box.createVerticalStrut(18));
        body.add(buildDivider("Chỉnh sửa thông tin nhập nhầm"));
        body.add(Box.createVerticalStrut(14));
        body.add(buildSdtSection());
        body.add(Box.createVerticalStrut(14));
        body.add(buildMaNVSection());
        body.add(Box.createVerticalStrut(14));
        body.add(buildNgayLapSection());
        body.add(Box.createVerticalGlue());
        return body;
    }

    private JPanel buildInfoCards() {
        JPanel row = new JPanel(new GridLayout(1, 3, 10, 0));
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        row.add(infoCard("Ngày lập",
            hd.getNgayLap() != null ? hd.getNgayLap().format(FMT) : "—"));
        row.add(infoCard("Tổng tiền",
            String.format("%,.0f đ", hd.getTongTien())));
        row.add(infoCard("Nhân viên",
            "NV" + String.format("%03d", hd.getMaNV())));
        return row;
    }

    private JPanel infoCard(String label, String value) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
            new LineBorder(ACCENT, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        JLabel lbl = new JLabel(label);
        lbl.setFont(F_LABEL); lbl.setForeground(MUTED);
        JLabel val = new JLabel(value);
        val.setFont(F_RESULT); val.setForeground(WHITE);
        card.add(lbl, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildDivider(String text) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(BG);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        JPanel l1 = new JPanel(); l1.setBackground(ACCENT); l1.setPreferredSize(new Dimension(0, 1));
        JPanel l2 = new JPanel(); l2.setBackground(ACCENT); l2.setPreferredSize(new Dimension(0, 1));
        JLabel lbl = new JLabel(text);
        lbl.setFont(F_HINT); lbl.setForeground(ACCENT_LIGHT);
        p.add(l1, BorderLayout.WEST);
        p.add(lbl, BorderLayout.CENTER);
        p.add(l2, BorderLayout.EAST);
        return p;
    }

    private JPanel buildSdtSection() {
        JPanel s = section();
        s.add(sectionLabel("Khách hàng  —  nhập số điện thoại"));
        s.add(Box.createVerticalStrut(6));
        txtSdt = styledInput("Số điện thoại khách hàng...");
        RoundBtn btn = new RoundBtn("Tìm", ACCENT, WHITE);
        btn.setPreferredSize(new Dimension(70, 40));
        btn.addActionListener(e -> lookupKhachHang());
        txtSdt.addActionListener(e -> lookupKhachHang());
        s.add(inputRow(txtSdt, btn));
        s.add(Box.createVerticalStrut(6));
        lblKHResult = resultLabel("  ");
        s.add(lblKHResult);
        return s;
    }

    private JPanel buildMaNVSection() {
        JPanel s = section();
        s.add(sectionLabel("Nhân viên  —  nhập mã nhân viên"));
        s.add(Box.createVerticalStrut(6));
        txtMaNV = styledInput("Mã nhân viên (VD: NV001)...");
        RoundBtn btn = new RoundBtn("Tìm", ACCENT, WHITE);
        btn.setPreferredSize(new Dimension(70, 40));
        btn.addActionListener(e -> lookupNhanVien());
        txtMaNV.addActionListener(e -> lookupNhanVien());
        s.add(inputRow(txtMaNV, btn));
        s.add(Box.createVerticalStrut(6));
        lblNVResult = resultLabel("  ");
        s.add(lblNVResult);
        return s;
    }

    private JPanel buildNgayLapSection() {
        JPanel s = section();
        s.add(sectionLabel("Ngày lập  —  định dạng dd/MM/yyyy"));
        s.add(Box.createVerticalStrut(6));
        txtNgayLap = styledInput("dd/MM/yyyy — VD: 25/06/2025");
        RoundBtn btn = new RoundBtn("Xác nhận", ACCENT, WHITE);
        btn.setPreferredSize(new Dimension(90, 40));
        btn.addActionListener(e -> validateNgayLap());
        txtNgayLap.addActionListener(e -> validateNgayLap());
        s.add(inputRow(txtNgayLap, btn));
        s.add(Box.createVerticalStrut(6));
        lblNgayResult = resultLabel("  ");
        s.add(lblNgayResult);
        return s;
    }

    /* ══════════════════════════════════════════════════════
       TAB 2 — Chi tiết sản phẩm
    ══════════════════════════════════════════════════════ */
    private JPanel buildTab2() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(12, 18, 8, 18));

        /* toolbar */
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(BG);
        JLabel lbl = new JLabel("Sản phẩm trong hóa đơn");
        lbl.setFont(F_RESULT); lbl.setForeground(ACCENT_LIGHT);
        toolbar.add(lbl, BorderLayout.WEST);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBar.setBackground(BG);
        RoundBtn btnAdd = new RoundBtn("+  Thêm SP", GREEN, TEXT_DARK);
        btnAdd.setPreferredSize(new Dimension(110, 34));
        btnAdd.addActionListener(e -> openAddSpDialog());
        RoundBtn btnDel = new RoundBtn("✕  Xóa SP", RED, WHITE);
        btnDel.setPreferredSize(new Dimension(100, 34));
        btnDel.addActionListener(e -> removeSelectedSp());
        btnBar.add(btnAdd);
        btnBar.add(btnDel);
        toolbar.add(btnBar, BorderLayout.EAST);
        p.add(toolbar, BorderLayout.NORTH);

        /* table */
        String[] cols = {"Tên game", "Loại", "Mã CD/SP", "Đơn giá", "SL", "Thành tiền", "Trạng thái"};
        detailModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        detailTable = new JTable(detailModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(ACCENT); c.setForeground(WHITE);
                } else if (row < workingItems.size() && workingItems.get(row).isNew) {
                    c.setBackground(ROW_NEW); c.setForeground(TEXT_DARK);
                } else {
                    c.setBackground(row % 2 == 0 ? ROW_ODD : WHITE);
                    c.setForeground(TEXT_DARK);
                }
                return c;
            }
        };
        detailTable.setFont(F_CELL);
        detailTable.setRowHeight(34);
        detailTable.setShowGrid(false);
        detailTable.setIntercellSpacing(new Dimension(0, 0));
        detailTable.setSelectionBackground(ACCENT);
        detailTable.setSelectionForeground(WHITE);
        detailTable.setBackground(WHITE);

        JTableHeader hdr = detailTable.getTableHeader();
        hdr.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel l = new JLabel(v == null ? "" : v.toString());
                l.setFont(F_HDR); l.setForeground(WHITE);
                l.setBackground(TBL_HDR); l.setOpaque(true);
                l.setBorder(new EmptyBorder(8, 10, 8, 10));
                return l;
            }
        });
        hdr.setBackground(TBL_HDR);
        hdr.setPreferredSize(new Dimension(0, 38));
        hdr.setReorderingAllowed(false);

        int[] widths = {200, 50, 90, 110, 35, 110, 80};
        for (int i = 0; i < widths.length; i++)
            detailTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane sp = new JScrollPane(detailTable);
        sp.setBorder(new LineBorder(TBL_HDR, 1, true));
        sp.getViewport().setBackground(WHITE);
        p.add(sp, BorderLayout.CENTER);

        /* bottom bar */
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(BG);
        bottom.setBorder(new EmptyBorder(6, 0, 0, 0));
        JLabel hint = new JLabel("  🟢 Xanh = SP mới thêm  •  Chưa lưu vào DB cho đến khi nhấn Lưu tất cả");
        hint.setFont(F_HINT); hint.setForeground(MUTED);
        bottom.add(hint, BorderLayout.WEST);
        lblTongTien = new JLabel("Tổng tiền: —");
        lblTongTien.setFont(F_MONEY); lblTongTien.setForeground(new Color(80, 200, 160));
        bottom.add(lblTongTien, BorderLayout.EAST);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    /* ══════════════════════════════════════════════════════
       FOOTER
    ══════════════════════════════════════════════════════ */
    private JPanel buildFooter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(0, 24, 18, 24));

        JPanel sep = new JPanel();
        sep.setBackground(ACCENT_LIGHT);
        sep.setPreferredSize(new Dimension(0, 1));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnRow.setBackground(BG);

        RoundBtn btnCancel = new RoundBtn("✕  Hủy", RED, WHITE);
        btnCancel.setPreferredSize(new Dimension(110, 40));
        btnCancel.addActionListener(e -> dispose());

        RoundBtn btnSave = new RoundBtn("✔  Lưu tất cả", GREEN, TEXT_DARK);
        btnSave.setPreferredSize(new Dimension(140, 40));
        btnSave.addActionListener(e -> doSaveAll());

        btnRow.add(btnCancel);
        btnRow.add(btnSave);
        p.add(sep, BorderLayout.NORTH);
        p.add(btnRow, BorderLayout.CENTER);
        return p;
    }

    /* ══════════════════════════════════════════════════════
       INIT DATA
    ══════════════════════════════════════════════════════ */
    private void loadWorkingItems() {
        workingItems.clear();
        if (hd.getDanhSachChiTiet() == null) return;

        // For each line, look up MaCD (if CD) from CTHOADON+CD
        String sqlCD =
            "SELECT cd.MaCD FROM CTHOADON ct " +
            "JOIN CD cd ON cd.MaSP = ct.MaSP " +
            "WHERE ct.MaHD = ? AND ct.MaSP = ? AND cd.TrangThai = N'DaBan' " +
            "ORDER BY cd.MaCD ASC";

        try (Connection con = DBConnection.getConnection()) {
            for (HoaDon.ChiTietHoaDon ct : hd.getDanhSachChiTiet()) {
                int maCD = -1;
                if ("CD".equalsIgnoreCase(ct.getLoaiSanPham())) {
                    try (PreparedStatement ps = con.prepareStatement(sqlCD)) {
                        ps.setInt(1, hd.getMaHD());
                        ps.setInt(2, ct.getMaSP());
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) maCD = rs.getInt("MaCD");
                    }
                }
                workingItems.add(new SpRow(
                    ct.getMaSP(), maCD,
                    ct.getTenGame(), ct.getLoaiSanPham(),
                    ct.getDonGia(), ct.getSoLuong(), false
                ));
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        refreshDetailTable();
    }

    private void prefillTab1() {
        if (hd.getSoDienThoai() != null && !hd.getSoDienThoai().isBlank()) {
            txtSdt.setText(hd.getSoDienThoai());
            lookupKhachHang();
        }
        txtMaNV.setText("NV" + String.format("%03d", hd.getMaNV()));
        lookupNhanVien();
        if (hd.getNgayLap() != null) {
            txtNgayLap.setText(hd.getNgayLap().format(FMT));
            setResult(lblNgayResult, "Ngày hiện tại: " + hd.getNgayLap().format(FMT), MUTED);
        }
    }

    /* ══════════════════════════════════════════════════════
       TAB 1 — LOGIC
    ══════════════════════════════════════════════════════ */
    private void lookupKhachHang() {
        String sdt = txtSdt.getText().trim();
        if (sdt.isEmpty()) {
            setResult(lblKHResult, "⚠  Nhập số điện thoại trước!", MUTED);
            foundKH = null; return;
        }
        KhachHang kh = khDAO.findBySDT(sdt);
        if (kh == null) {
            setResult(lblKHResult, "✗  Không tìm thấy khách hàng với SĐT: " + sdt, RED);
            foundKH = null;
        } else {
            setResult(lblKHResult,
                "✓  " + kh.getHoTen() + "  (KH" + String.format("%03d", kh.getMaKH()) + ")", GREEN);
            foundKH = kh;
        }
    }

    private void lookupNhanVien() {
        String raw = txtMaNV.getText().trim().replaceAll("(?i)nv", "");
        if (raw.isEmpty()) {
            setResult(lblNVResult, "⚠  Nhập mã nhân viên trước!", MUTED);
            foundNV = null; return;
        }
        int maNV;
        try { maNV = Integer.parseInt(raw); }
        catch (NumberFormatException ex) {
            setResult(lblNVResult, "✗  Mã NV phải là số (VD: NV001 hoặc 1)", RED);
            foundNV = null; return;
        }
        NhanVien nv = nvDAO.findById(maNV);
        if (nv == null) {
            setResult(lblNVResult, "✗  Không tìm thấy nhân viên mã: " + maNV, RED);
            foundNV = null;
        } else {
            setResult(lblNVResult,
                "✓  " + nv.getHoTen() + "  (NV" + String.format("%03d", nv.getMaNV()) + ")", GREEN);
            foundNV = nv;
        }
    }

    private void validateNgayLap() {
        String raw = txtNgayLap.getText().trim();
        if (raw.isEmpty()) {
            setResult(lblNgayResult, "⚠  Nhập ngày lập trước!", MUTED);
            newNgayLap = null; return;
        }
        try {
            LocalDate d = LocalDate.parse(raw, FMT);
            LocalDate old = hd.getNgayLap() != null ? hd.getNgayLap().toLocalDate() : null;
            if (d.equals(old)) {
                setResult(lblNgayResult, "ℹ  Ngày không đổi: " + d.format(FMT), MUTED);
                newNgayLap = null;
            } else {
                newNgayLap = d;
                setResult(lblNgayResult, "✓  Ngày mới: " + d.format(FMT), GREEN);
            }
        } catch (DateTimeParseException ex) {
            setResult(lblNgayResult, "✗  Định dạng không hợp lệ. Vui lòng nhập dd/MM/yyyy", RED);
            newNgayLap = null;
        }
    }

    /* ══════════════════════════════════════════════════════
       TAB 2 — LOGIC
    ══════════════════════════════════════════════════════ */
    private void refreshDetailTable() {
        detailModel.setRowCount(0);
        double total = 0;
        for (SpRow row : workingItems) {
            String cdInfo = "CD".equals(row.loai)
                ? (row.maCD > 0 ? "CD" + row.maCD : "—")
                : "SP" + row.maSP;
            detailModel.addRow(new Object[]{
                row.tenGame,
                row.loai,
                cdInfo,
                String.format("%,.0f đ", row.donGia),
                row.soLuong,
                String.format("%,.0f đ", row.thanhTien()),
                row.isNew ? "🟢 Mới thêm" : "✔ Hiện có"
            });
            total += row.thanhTien();
        }
        lblTongTien.setText(String.format("Tổng tiền: %,.0f đ", total));
    }

    private void removeSelectedSp() {
        int row = detailTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Chọn sản phẩm cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        SpRow sp = workingItems.get(row);
        String rollbackNote = sp.isNew
            ? "(SP này mới thêm trong phiên này, chưa vào DB)"
            : "CD".equals(sp.loai)
                ? "Khi Lưu: CD" + sp.maCD + " sẽ trở về Sẵn Sàng."
                : "Khi Lưu: SoLuotBan ROM sẽ giảm " + sp.soLuong + ".";

        int yn = JOptionPane.showConfirmDialog(this,
            "Xóa \"" + sp.tenGame + "\" [" + sp.loai + "] khỏi hóa đơn?\n" + rollbackNote,
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (yn != JOptionPane.YES_OPTION) return;

        workingItems.remove(row);
        if (!sp.isNew) removedItems.add(sp);
        else addedItems.remove(sp); // hủy luôn, không cần rollback
        refreshDetailTable();
    }

    /* ══════════════════════════════════════════════════════
       ADD SP POPUP
    ══════════════════════════════════════════════════════ */
    private void openAddSpDialog() {
        JDialog popup = new JDialog(this, "Chọn sản phẩm thêm vào hóa đơn", true);
        popup.setSize(740, 480);
        popup.setLocationRelativeTo(this);
        popup.setLayout(new BorderLayout(0, 0));
        popup.getContentPane().setBackground(BG);

        /* Game table */
        DefaultTableModel gModel = new DefaultTableModel(
            new String[]{"Mã", "Tên game", "Thể loại"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblGame = makeTable(gModel);
        tblGame.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblGame.getColumnModel().getColumn(1).setPreferredWidth(210);
        tblGame.getColumnModel().getColumn(2).setPreferredWidth(100);

        /* SP table */
        DefaultTableModel sModel = new DefaultTableModel(
            new String[]{"Mã SP", "Loại", "Giá bán", "Tình trạng / Dung lượng"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblSP = makeTable(sModel);
        tblSP.getColumnModel().getColumn(0).setPreferredWidth(60);
        tblSP.getColumnModel().getColumn(1).setPreferredWidth(50);
        tblSP.getColumnModel().getColumn(2).setPreferredWidth(120);
        tblSP.getColumnModel().getColumn(3).setPreferredWidth(180);

        // Tô đỏ dòng hết hàng
        tblSP.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel && spList != null && row < spList.size()) {
                    boolean avail = (boolean) spList.get(row)[5];
                    if (!avail) {
                        c.setBackground(new Color(255, 220, 220));
                        c.setForeground(new Color(180, 60, 60));
                    } else {
                        c.setBackground(row % 2 == 0 ? ROW_ODD : WHITE);
                        c.setForeground(TEXT_DARK);
                    }
                }
                return c;
            }
        });

        gameList = new ArrayList<>();
        spList   = new ArrayList<>();
        loadGameCatalog(gModel);

        tblGame.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSpCatalog(tblGame, sModel);
        });

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            wrapScroll(tblGame), wrapScroll(tblSP));
        split.setDividerLocation(310);
        split.setBorder(null);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG);
        center.setBorder(new EmptyBorder(10, 12, 0, 12));
        center.add(split, BorderLayout.CENTER);
        popup.add(center, BorderLayout.CENTER);

        /* Footer */
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(BG);
        footer.setBorder(new EmptyBorder(0, 12, 8, 12));

        RoundBtn btnOk = new RoundBtn("+ Thêm vào HĐ", GREEN, TEXT_DARK);
        btnOk.setPreferredSize(new Dimension(140, 36));
        btnOk.addActionListener(e -> {
            int gameRow = tblGame.getSelectedRow();
            int spRow   = tblSP.getSelectedRow();
            if (gameRow < 0 || spRow < 0) {
                JOptionPane.showMessageDialog(popup, "Chọn game và sản phẩm trước!"); return;
            }
            if (spList == null || spRow >= spList.size()) return;
            Object[] sp = spList.get(spRow);
            if (!(boolean) sp[5]) {
                JOptionPane.showMessageDialog(popup, "Sản phẩm này đã hết hàng!"); return;
            }
            int    maSP   = (int)    sp[0];
            int    maCD   = (int)    sp[1];
            String loai   = (String) sp[2];
            double giaBan = (double) sp[3];
            String ten    = (String) sp[7];

            SpRow newRow = new SpRow(maSP, maCD, ten, loai, giaBan, 1, true);
            // Kiểm tra trùng với working list
            for (SpRow ex : workingItems) {
                if (ex.key().equals(newRow.key())) {
                    JOptionPane.showMessageDialog(popup,
                        "Sản phẩm này đã có trong hóa đơn!"); return;
                }
            }
            workingItems.add(newRow);
            addedItems.add(newRow);
            refreshDetailTable();
            popup.dispose();
        });

        RoundBtn btnX = new RoundBtn("Hủy", RED, WHITE);
        btnX.setPreferredSize(new Dimension(90, 36));
        btnX.addActionListener(e -> popup.dispose());

        footer.add(btnX);
        footer.add(btnOk);
        popup.add(footer, BorderLayout.SOUTH);
        popup.setVisible(true);
    }

    private void loadGameCatalog(DefaultTableModel model) {
        model.setRowCount(0); gameList.clear();
        String sql =
            "SELECT g.MaGame, g.TenGame, g.TheLoai FROM GAME g " +
            "WHERE EXISTS (" +
            "  SELECT 1 FROM SANPHAM sp WHERE sp.MaGame = g.MaGame " +
            "  AND sp.GiaBan IS NOT NULL AND sp.GiaBan > 0" +
            ") ORDER BY g.TenGame";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int mg = rs.getInt("MaGame");
                String tn = rs.getString("TenGame");
                String tl = nvl(rs.getString("TheLoai"));
                gameList.add(new Object[]{mg, tn, tl});
                model.addRow(new Object[]{"G" + mg, tn, tl});
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void loadSpCatalog(JTable tblGame, DefaultTableModel model) {
        model.setRowCount(0); spList = new ArrayList<>();
        int row = tblGame.getSelectedRow();
        if (row < 0 || row >= gameList.size()) return;
        int    maGame  = (int)    gameList.get(row)[0];
        String tenGame = (String) gameList.get(row)[1];

        // CD còn SanSang
        String sqlCD =
            "SELECT sp.MaSP, cd.MaCD, sp.GiaBan, cd.TinhTrang " +
            "FROM SANPHAM sp JOIN CD cd ON sp.MaSP = cd.MaSP " +
            "WHERE sp.MaGame = ? AND cd.TrangThai = N'SanSang' " +
            "  AND sp.GiaBan IS NOT NULL AND sp.GiaBan > 0";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlCD)) {
            ps.setInt(1, maGame);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int maSP = rs.getInt("MaSP"), maCD = rs.getInt("MaCD");
                double g = rs.getDouble("GiaBan");
                String tt = nvl(rs.getString("TinhTrang"));
                spList.add(new Object[]{maSP, maCD, "CD", g,
                    "CD" + maCD + " — " + tt, true, maGame, tenGame});
                model.addRow(new Object[]{"SP" + maSP, "CD",
                    String.format("%,.0f VNĐ", g), "CD" + maCD + " — " + tt});
            }
        } catch (SQLException ex) { ex.printStackTrace(); }

        // ROM
        String sqlROM =
            "SELECT sp.MaSP, r.DungLuong, sp.GiaBan " +
            "FROM SANPHAM sp JOIN ROM r ON sp.MaSP = r.MaSP " +
            "WHERE sp.MaGame = ? AND sp.GiaBan IS NOT NULL AND sp.GiaBan > 0";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlROM)) {
            ps.setInt(1, maGame);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int maSP = rs.getInt("MaSP");
                String dl = nvl(rs.getString("DungLuong"));
                double g  = rs.getDouble("GiaBan");
                spList.add(new Object[]{maSP, -1, "ROM", g,
                    "Tải về — " + dl, true, maGame, tenGame});
                model.addRow(new Object[]{"SP" + maSP, "ROM",
                    String.format("%,.0f VNĐ", g), "Tải về — " + dl});
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    /* ══════════════════════════════════════════════════════
       SAVE ALL  (single transaction)
    ══════════════════════════════════════════════════════ */
    private void doSaveAll() {
        boolean khChanged   = foundKH    != null && foundKH.getMaKH() != hd.getMaKH();
        boolean nvChanged   = foundNV    != null && foundNV.getMaNV() != hd.getMaNV();
        boolean ngayChanged = newNgayLap != null;
        boolean spChanged   = !removedItems.isEmpty() || !addedItems.isEmpty();

        if (!khChanged && !nvChanged && !ngayChanged && !spChanged) {
            JOptionPane.showMessageDialog(this,
                "Không có thay đổi nào để lưu.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Tổng tiền mới (từ working list)
        double tongMoi = workingItems.stream().mapToDouble(SpRow::thanhTien).sum();
        double tongCu  = hd.getTongTien();

        // Điểm theo tổng tiền (làm tròn xuống)
        int diemCu  = (int)(tongCu  / DIEM_PER_DONG);
        int diemMoi = (int)(tongMoi / DIEM_PER_DONG);

        int maKHCu  = hd.getMaKH();
        int maKHMoi = khChanged ? foundKH.getMaKH() : maKHCu;

        // ── Confirm dialog ──
        StringBuilder sb = new StringBuilder("Xác nhận cập nhật HD")
            .append(String.format("%03d", hd.getMaHD())).append("?\n\n");
        if (khChanged)
            sb.append("• Khách hàng : ").append(nvl(hd.getTenKhachHang()))
              .append("  →  ").append(foundKH.getHoTen()).append("\n");
        if (nvChanged)
            sb.append("• Nhân viên  : NV").append(String.format("%03d", hd.getMaNV()))
              .append("  →  NV").append(String.format("%03d", foundNV.getMaNV())).append("\n");
        if (ngayChanged)
            sb.append("• Ngày lập   : ")
              .append(hd.getNgayLap() != null ? hd.getNgayLap().format(FMT) : "—")
              .append("  →  ").append(newNgayLap.format(FMT)).append("\n");
        if (spChanged) {
            if (!removedItems.isEmpty())
                sb.append("• Xóa SP     : ").append(removedItems.size()).append(" sản phẩm\n");
            if (!addedItems.isEmpty())
                sb.append("• Thêm SP    : ").append(addedItems.size()).append(" sản phẩm\n");
            sb.append(String.format("• Tổng tiền  : %,.0f đ  →  %,.0f đ%n", tongCu, tongMoi));
        }
        // Thông báo điểm
        if (khChanged) {
            sb.append(String.format(
                "• Điểm TL    : KH%03d trừ %d điểm  |  KH%03d +%d điểm%n",
                maKHCu, diemCu, maKHMoi, diemMoi));
        } else if (diemMoi != diemCu) {
            sb.append(String.format(
                "• Điểm TL    : %d  →  %d điểm (KH%03d)%n",
                diemCu, diemMoi, maKHCu));
        }

        int yn = JOptionPane.showConfirmDialog(this, sb.toString(),
            "Xác nhận sửa", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (yn != JOptionPane.YES_OPTION) return;

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {

                /* ── 1. Cập nhật KH / NV ── */
                if (khChanged || nvChanged) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE HOADON SET MaKH=?, MaNV=? WHERE MaHD=?")) {
                        ps.setInt(1, maKHMoi);
                        ps.setInt(2, nvChanged ? foundNV.getMaNV() : hd.getMaNV());
                        ps.setInt(3, hd.getMaHD());
                        ps.executeUpdate();
                    }
                    if (khChanged) {
                        // KH cũ: trừ điểm theo tổng tiền CŨ
                        adjustDiem(con, maKHCu, -diemCu);
                        // KH mới: cộng điểm theo tổng tiền MỚI
                        adjustDiem(con, maKHMoi, +diemMoi);
                        // Chuyển log điểm sang KH mới
                        transferPointLogs(con, hd.getMaHD(), maKHCu, maKHMoi);
                    }
                }

                /* ── 2. Ngày lập ── */
                if (ngayChanged) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE HOADON SET NgayLap=? WHERE MaHD=?")) {
                        ps.setTimestamp(1, Timestamp.valueOf(newNgayLap.atStartOfDay()));
                        ps.setInt(2, hd.getMaHD());
                        ps.executeUpdate();
                    }
                }

                /* ── 3. Xóa SP (rollback tồn kho) ── */
                for (SpRow sp : removedItems) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "DELETE FROM CTHOADON WHERE MaHD=? AND MaSP=?")) {
                        ps.setInt(1, hd.getMaHD()); ps.setInt(2, sp.maSP);
                        ps.executeUpdate();
                    }
                    if ("CD".equals(sp.loai) && sp.maCD > 0) {
                        try (PreparedStatement ps = con.prepareStatement(
                                "UPDATE CD SET TrangThai=N'SanSang' WHERE MaCD=?")) {
                            ps.setInt(1, sp.maCD); ps.executeUpdate();
                        }
                    } else if ("ROM".equals(sp.loai)) {
                        try (PreparedStatement ps = con.prepareStatement(
                                "UPDATE ROM SET SoLuotBan=" +
                                "CASE WHEN SoLuotBan-?<0 THEN 0 ELSE SoLuotBan-? END WHERE MaSP=?")) {
                            ps.setInt(1, sp.soLuong); ps.setInt(2, sp.soLuong);
                            ps.setInt(3, sp.maSP); ps.executeUpdate();
                        }
                    }
                }

                /* ── 4. Thêm SP mới ── */
                for (SpRow sp : addedItems) {
                    if ("CD".equals(sp.loai)) {
                        try (PreparedStatement ps = con.prepareStatement(
                                "SELECT TrangThai FROM CD WHERE MaCD=?")) {
                            ps.setInt(1, sp.maCD);
                            ResultSet rs = ps.executeQuery();
                            if (rs.next() && !"SanSang".equals(rs.getString("TrangThai"))) {
                                con.rollback();
                                JOptionPane.showMessageDialog(this,
                                    "CD" + sp.maCD + " — \"" + sp.tenGame + "\" vừa bị bán mất!\n"
                                    + "Vui lòng chọn lại.", "Xung đột dữ liệu", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                    }
                    String upsert =
                        "IF EXISTS (SELECT 1 FROM CTHOADON WHERE MaHD=? AND MaSP=?) " +
                        "  UPDATE CTHOADON SET SoLuong=SoLuong+?,DonGia=? WHERE MaHD=? AND MaSP=? " +
                        "ELSE " +
                        "  INSERT INTO CTHOADON(MaHD,MaSP,SoLuong,DonGia) VALUES(?,?,?,?)";
                    try (PreparedStatement ps = con.prepareStatement(upsert)) {
                        ps.setInt(1, hd.getMaHD()); ps.setInt(2, sp.maSP);
                        ps.setInt(3, sp.soLuong);   ps.setDouble(4, sp.donGia);
                        ps.setInt(5, hd.getMaHD()); ps.setInt(6, sp.maSP);
                        ps.setInt(7, hd.getMaHD()); ps.setInt(8, sp.maSP);
                        ps.setInt(9, sp.soLuong);   ps.setDouble(10, sp.donGia);
                        ps.executeUpdate();
                    }
                    if ("CD".equals(sp.loai) && sp.maCD > 0) {
                        try (PreparedStatement ps = con.prepareStatement(
                                "UPDATE CD SET TrangThai=N'DaBan' WHERE MaCD=?")) {
                            ps.setInt(1, sp.maCD); ps.executeUpdate();
                        }
                    } else if ("ROM".equals(sp.loai)) {
                        try (PreparedStatement ps = con.prepareStatement(
                                "UPDATE ROM SET SoLuotBan=SoLuotBan+? WHERE MaSP=?")) {
                            ps.setInt(1, sp.soLuong); ps.setInt(2, sp.maSP);
                            ps.executeUpdate();
                        }
                    }
                }

                /* ── 5. Cập nhật TongTien + điểm KH (nếu SP thay đổi, KH không đổi) ── */
                if (spChanged) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE HOADON SET TongTien=? WHERE MaHD=?")) {
                        ps.setDouble(1, tongMoi); ps.setInt(2, hd.getMaHD());
                        ps.executeUpdate();
                    }
                    // Chỉ cập nhật điểm KH nếu KH không đổi
                    // (nếu KH đổi thì bước 1 đã xử lý rồi)
                    if (!khChanged && diemMoi != diemCu) {
                        adjustDiem(con, maKHCu, diemMoi - diemCu);
                    }
                }

                con.commit();
                JOptionPane.showMessageDialog(this,
                    "✅ Cập nhật hóa đơn HD" + String.format("%03d", hd.getMaHD()) + " thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();

            } catch (Exception ex) {
                con.rollback();
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "❌ Lỗi khi lưu:\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "❌ Lỗi kết nối:\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Chuyển điểm từ KH cũ sang KH mới cho các log điểm liên quan HĐ này.
     * Đảo ngược hiệu ứng điểm ở KH cũ, áp lại cho KH mới.
     */
    /**
 * Điều chỉnh DiemTichLuy của KH — delta dương = cộng, âm = trừ.
 * Không cho xuống dưới 0.
 */
        private void adjustDiem(Connection con, int maKH, int delta) throws SQLException {
            if (delta == 0) return;
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE KHACHHANG SET DiemTichLuy = " +
                    "CASE WHEN DiemTichLuy+?<0 THEN 0 ELSE DiemTichLuy+? END " +
                    "WHERE MaKH=?")) {
                ps.setInt(1, delta); ps.setInt(2, delta); ps.setInt(3, maKH);
                ps.executeUpdate();
            }
        }

        /**
         * Chuyển ownership các log điểm liên quan HĐ này từ KH cũ sang KH mới.
         * Không tính lại điểm vì adjustDiem() đã làm ở bước 1.
         */
        private void transferPointLogs(Connection con, int maHD, int maKHCu, int maKHMoi)
                throws SQLException {
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE DIEM_LICHSU SET MaKH=? " +
                    "WHERE MaKH=? AND MaPT IS NULL AND GhiChu LIKE N'%HĐ" + maHD + "%'")) {
                ps.setInt(1, maKHMoi); ps.setInt(2, maKHCu);
                ps.executeUpdate();
            }
        }

    /* ══════════════════════════════════════════════════════
       UI HELPERS
    ══════════════════════════════════════════════════════ */
    private JPanel section() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        return p;
    }

    private JPanel inputRow(JTextField tf, JButton btn) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.add(tf, BorderLayout.CENTER);
        row.add(btn, BorderLayout.EAST);
        return row;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_LABEL); l.setForeground(ACCENT_LIGHT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel resultLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_RESULT); l.setForeground(MUTED);
        return l;
    }

    private void setResult(JLabel lbl, String text, Color color) {
        lbl.setText(text); lbl.setForeground(color);
    }

    private JTextField styledInput(String placeholder) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(new Color(160, 150, 200));
                    g2.setFont(F_HINT);
                    g2.drawString(placeholder, 10, getHeight() / 2 + 5);
                }
            }
        };
        tf.setBackground(INPUT_BG); tf.setForeground(TEXT_DARK);
        tf.setCaretColor(ACCENT); tf.setFont(F_INPUT);
        tf.setBorder(new CompoundBorder(
            new LineBorder(ACCENT, 1, true), new EmptyBorder(6, 10, 6, 10)));
        tf.setPreferredSize(new Dimension(0, 40));
        return tf;
    }

    private JTable makeTable(DefaultTableModel model) {
        JTable t = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? ROW_ODD : WHITE);
                    c.setForeground(TEXT_DARK);
                } else { c.setBackground(ACCENT); c.setForeground(WHITE); }
                return c;
            }
        };
        t.setFont(F_CELL); t.setRowHeight(32);
        t.setShowGrid(false); t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(ACCENT); t.setSelectionForeground(WHITE);
        t.setBackground(WHITE);
        JTableHeader h = t.getTableHeader();
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel l = new JLabel(v == null ? "" : v.toString());
                l.setFont(F_HDR); l.setForeground(WHITE); l.setBackground(TBL_HDR);
                l.setOpaque(true); l.setBorder(new EmptyBorder(6, 8, 6, 8));
                return l;
            }
        });
        h.setBackground(TBL_HDR); h.setPreferredSize(new Dimension(0, 34));
        h.setReorderingAllowed(false);
        return t;
    }

    private JScrollPane wrapScroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(new LineBorder(TBL_HDR, 1, true));
        sp.getViewport().setBackground(WHITE);
        return sp;
    }

    private String nvl(String s) { return s == null ? "" : s; }

    /* ══════════════════════════════════════════════════════
       ROUND BUTTON — đồng bộ RentEditDialog
    ══════════════════════════════════════════════════════ */
    static class RoundBtn extends JButton {
        private final Color bg, fg;
        RoundBtn(String text, Color bg, Color fg) {
            super(text); this.bg = bg; this.fg = fg;
            setFocusPainted(false); setContentAreaFilled(false); setBorderPainted(false);
            setForeground(fg); setFont(new Font("Segoe UI", Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            super.paintComponent(g2); g2.dispose();
        }
    }
}