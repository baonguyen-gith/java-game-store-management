package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.util.DBConnection;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * RentDetailDialog — xem chi tiết phiếu thuê.
 *
 * Logic tiền:
 *   TienCoc         = tiền cọc thu lúc tạo phiếu (KHÔNG thay đổi khi gia hạn).
 *   TienThueBanDau  = SUM(ct.DonGiaThue) — giá thuê gốc lúc lập phiếu.
 *   TienGiaHan      = (NgayTraDuKien − NgayThue) * GiaThueNgay − TienThueBanDau (phần gia hạn thêm).
 *   TienPhatDB      = TienPhat trong DB: phạt trễ gia hạn + phạt hỏng đã ghi nhận.
 *   PhatTreTamTinh  = phạt trễ ước tính hôm nay (chỉ khi đang thuê & quá hạn, chưa ghi DB).
 *   DoanhThu        = TienThueBanDau + TienGiaHan + TienPhatDB + PhatTreTamTinh (ước tính).
 *
 * Kết quả quyết toán:
 *   delta = TienCoc − (TienPhatDB + PhatTreTamTinh)   [dựa trên cọc và phạt]
 *   delta > 0 → hoàn tiền | = 0 → vừa đủ | < 0 → thu thêm
 */
public class RentDetailDialog extends JDialog {

    /* ===== COLORS ===== */
    private static final Color BG_DARK        = new Color(35, 20, 85);
    private static final Color BG_CARD        = new Color(55, 35, 110);
    private static final Color BG_CARD_DARK   = new Color(42, 26, 95);
    private static final Color PURPLE_HEADER  = new Color(155, 135, 245);
    private static final Color PURPLE_ROW     = new Color(245, 242, 255);
    private static final Color PURPLE_ALT     = Color.WHITE;
    private static final Color ACCENT         = new Color(130, 90, 230);
    private static final Color TEXT_WHITE     = Color.WHITE;
    private static final Color TEXT_DARK      = new Color(40, 40, 40);
    private static final Color TEXT_MUTED     = new Color(160, 148, 200);
    private static final Color BTN_CANCEL     = new Color(252, 129, 129);
    private static final Color COLOR_PENALTY  = new Color(220, 53, 69);
    private static final Color COLOR_OK       = new Color(40, 167, 69);
    private static final Color COLOR_WARN     = new Color(255, 165, 0);
    private static final Color COLOR_REVENUE  = new Color(80, 200, 160);   // xanh ngọc — doanh thu
    private static final Color COLOR_EXTEND   = new Color(100, 180, 255);  // xanh dương — gia hạn
    private static final Color COLOR_BASE     = new Color(200, 180, 255);  // tím nhạt — thuê gốc
    private static final Color COLOR_BADGE_ACTIVE = new Color(255, 193, 7);
    private static final Color COLOR_BADGE_DONE   = new Color(40, 167, 69);

    /* ===== FONTS ===== */
    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_CELL   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_BIG    = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_MICRO  = new Font("Segoe UI", Font.PLAIN, 10);

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /* ===== WIDGETS ===== */
    private JLabel lblTenKH, lblSDT, lblTrangThai;
    private JLabel lblNgayThue, lblNgayTraDK, lblNgayTraThucTe;

    // Hàng 1 — Doanh thu & chi tiết nguồn thu
    private JLabel lblDoanhThu;           // TỔNG doanh thu (accent lớn)
    private JLabel lblThueBanDau;         // Tiền thuê ban đầu (DonGiaThue)
    private JLabel lblGiaHan;             // Tiền gia hạn thêm
    private JLabel lblTienPhatDB;         // Phí đã ghi nhận (phạt + sửa)

    // Hàng 2 — Cọc & quyết toán
    private JLabel lblTienCoc;            // Tiền cọc ban đầu
    private JLabel lblPhatTreTT;          // Phạt trễ tạm tính
    private JLabel lblDelta;              // Kết quả quyết toán

    private JLabel lblCongThuc;
    private DefaultTableModel tableModel;

    private final int maPT;

    public RentDetailDialog(Frame parent, int maPT) {
        super(parent, "Chi tiết phiếu thuê", true);
        this.maPT = maPT;

        setMinimumSize(new Dimension(760, 620));
        pack();
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        getRootPane().setBorder(BorderFactory.createLineBorder(ACCENT, 2));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        loadData();
    }

    /* ==================== HEADER ==================== */
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(18, 24, 10, 24));

        JLabel title = new JLabel("CHI TIẾT PHIẾU THUÊ  —  PT" + maPT);
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_WHITE);
        p.add(title, BorderLayout.WEST);

        JPanel sep = new JPanel();
        sep.setBackground(PURPLE_HEADER);
        sep.setPreferredSize(new Dimension(0, 1));
        p.add(sep, BorderLayout.SOUTH);
        return p;
    }

    /* ==================== BODY ==================== */
    private JPanel buildBody() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(14, 24, 0, 24));

        // Row 1: Khách hàng / SĐT / Trạng thái
        lblTenKH     = cardValue();
        lblSDT       = cardValue();
        lblTrangThai = cardValue();
        JPanel row1 = infoRow(
            infoCard("Khách hàng",    lblTenKH),
            infoCard("Số điện thoại", lblSDT),
            infoCard("Trạng thái",    lblTrangThai)
        );
        row1.setPreferredSize(new Dimension(0, 68));

        // Row 2: Ngày thuê / Ngày trả DK / Ngày trả thực tế
        lblNgayThue      = cardValue();
        lblNgayTraDK     = cardValue();
        lblNgayTraThucTe = cardValue();
        JPanel row2 = infoRow(
            infoCard("Ngày thuê",        lblNgayThue),
            infoCard("Ngày trả dự kiến", lblNgayTraDK),
            infoCard("Ngày trả thực tế", lblNgayTraThucTe)
        );
        row2.setPreferredSize(new Dimension(0, 68));

        JPanel topBlock = new JPanel(new GridLayout(2, 1, 0, 8));
        topBlock.setBackground(BG_DARK);
        topBlock.add(row1);
        topBlock.add(row2);
        p.add(topBlock, BorderLayout.NORTH);

        // Bảng CD
        String[] cols = {"Mã CD", "Mã Game", "Tên Game", "Giá thuê hiện tại/ngày", "Đơn giá ban đầu/ngày"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = buildStyledTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        table.getColumnModel().getColumn(2).setPreferredWidth(240);
        table.getColumnModel().getColumn(3).setPreferredWidth(155);
        table.getColumnModel().getColumn(4).setPreferredWidth(155);
        p.add(styledScroll(table), BorderLayout.CENTER);

        p.add(buildSummary(), BorderLayout.SOUTH);
        return p;
    }

    /**
     * Panel tổng kết tài chính — 2 hàng:
     *
     * Hàng A — DOANH THU (nguồn thu):
     *   [★ Tổng doanh thu] | [Thuê ban đầu] | [Gia hạn thêm] | [Phí đã ghi nhận]
     *
     * Hàng B — QUYẾT TOÁN CỌC:
     *   [Tiền cọc ban đầu] | [Phạt trễ tạm tính] | [Kết quả quyết toán]
     *
     * Đường kẻ phân tách + nhãn nhóm giúp dễ đọc.
     */
    private JPanel buildSummary() {
        // ── Khởi tạo labels ──
        lblDoanhThu    = cardValue(); lblDoanhThu.setFont(FONT_BIG);
        lblThueBanDau  = cardValue();
        lblGiaHan      = cardValue();
        lblTienPhatDB  = cardValue();
        lblTienCoc     = cardValue();
        lblPhatTreTT   = cardValue();
        lblDelta       = cardValue(); lblDelta.setFont(FONT_BIG);
        lblCongThuc    = new JLabel(" ");
        lblCongThuc.setFont(FONT_MICRO);
        lblCongThuc.setForeground(TEXT_MUTED);

        // ── Hàng A: Doanh thu ──
        JPanel sectionLabelA = sectionLabel("📊  DOANH THU");
        JPanel rowA = new JPanel(new GridLayout(1, 4, 8, 0));
        rowA.setBackground(BG_DARK);
        rowA.add(summaryCard("★ Tổng doanh thu",    lblDoanhThu,   true,  COLOR_REVENUE));
        rowA.add(summaryCard("Thuê ban đầu",         lblThueBanDau, false, COLOR_BASE));
        rowA.add(summaryCard("Gia hạn thêm (đã trả)",         lblGiaHan,     false, COLOR_EXTEND));
        rowA.add(summaryCard("Phí đã ghi nhận (DB)", lblTienPhatDB, false, COLOR_WARN));

        // ── Hàng B: Quyết toán cọc ──
        JPanel sectionLabelB = sectionLabel("💰  QUYẾT TOÁN CỌC");
        JPanel rowB = new JPanel(new GridLayout(1, 3, 8, 0));
        rowB.setBackground(BG_DARK);
        rowB.add(summaryCard("Tiền cọc ban đầu",     lblTienCoc,  false, PURPLE_HEADER));
        rowB.add(summaryCard("Phạt trễ tạm tính",    lblPhatTreTT,false, COLOR_WARN));
        rowB.add(summaryCard("Kết quả quyết toán",   lblDelta,    true,  new Color(155, 135, 245)));

        // ── Ghi chú ──
        JPanel noteRow = new JPanel(new BorderLayout());
        noteRow.setBackground(BG_DARK);
        noteRow.setBorder(new EmptyBorder(4, 2, 0, 0));
        JPanel noteInner = new JPanel(new GridLayout(2, 1, 0, 1));
        noteInner.setBackground(BG_DARK);
        noteInner.add(lblCongThuc);
        JLabel noteStatic = new JLabel(
            "* Gia hạn thêm = (NgayTraDK − NgayThue) × GiáThuê/ngày − ĐơnGiáGốc.   " +
            "* Phạt trễ tạm tính: ước tính chưa chốt — chỉ chốt khi trả CD.");
        noteStatic.setFont(FONT_MICRO);
        noteStatic.setForeground(TEXT_MUTED);
        noteInner.add(noteStatic);
        noteRow.add(noteInner, BorderLayout.WEST);

        // ── Ghép tất cả ──
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBackground(BG_DARK);
        wrap.setBorder(new EmptyBorder(10, 0, 0, 0));

        wrap.add(sectionLabelA);
        wrap.add(Box.createVerticalStrut(4));
        rowA.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        rowA.setPreferredSize(new Dimension(0, 82));
        wrap.add(rowA);
        wrap.add(Box.createVerticalStrut(8));
        wrap.add(separator());
        wrap.add(Box.createVerticalStrut(6));
        wrap.add(sectionLabelB);
        wrap.add(Box.createVerticalStrut(4));
        rowB.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        rowB.setPreferredSize(new Dimension(0, 82));
        wrap.add(rowB);
        wrap.add(Box.createVerticalStrut(4));
        wrap.add(noteRow);

        return wrap;
    }

    /* ==================== FOOTER ==================== */
    private JPanel buildFooter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(10, 24, 16, 24));

        JPanel sep = new JPanel();
        sep.setBackground(PURPLE_HEADER);
        sep.setPreferredSize(new Dimension(0, 1));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnRow.setBackground(BG_DARK);

        RoundBtn btnClose = new RoundBtn("✕  Đóng", BTN_CANCEL, TEXT_WHITE);
        btnClose.setPreferredSize(new Dimension(110, 40));
        btnClose.addActionListener(e -> dispose());
        btnRow.add(btnClose);

        p.add(sep,    BorderLayout.NORTH);
        p.add(btnRow, BorderLayout.CENTER);
        return p;
    }

    /* ==================== LOAD DATA ==================== */
    private void loadData() {
        // Lấy DonGiaThue từ CTPHIEUTHUE (giá thuê gốc lúc lập phiếu)
        String sqlHeader =
            "SELECT pt.NgayThue, pt.NgayTraDuKien, pt.NgayTraThucTe, " +
            "       pt.TienCoc, pt.TienPhat, pt.TrangThai, " +
            "       kh.HoTen, kh.SDT, " +
            "       SUM(ct.DonGiaThue) AS TongDonGiaThue " +
            "FROM PHIEUTHUE pt " +
            "LEFT JOIN KHACHHANG kh ON pt.MaKH = kh.MaKH " +
            "LEFT JOIN CTPHIEUTHUE ct ON pt.MaPT = ct.MaPT " +
            "WHERE pt.MaPT = ? " +
            "GROUP BY pt.MaPT, pt.NgayThue, pt.NgayTraDuKien, pt.NgayTraThucTe, " +
            "         pt.TienCoc, pt.TienPhat, pt.TrangThai, kh.HoTen, kh.SDT";

        // Chi tiết CD — thêm cột DonGiaThue (giá/ngày lúc lập phiếu)
        String sqlDetail =
            "SELECT cd.MaCD, g.MaGame, g.TenGame, sp.GiaThueNgay, ct.DonGiaThue " +
            "FROM CTPHIEUTHUE ct " +
            "JOIN CD      cd ON ct.MaCD   = cd.MaCD " +
            "JOIN SANPHAM sp ON cd.MaSP   = sp.MaSP " +
            "JOIN GAME    g  ON sp.MaGame = g.MaGame " +
            "WHERE ct.MaPT = ?";

        try (Connection con = DBConnection.getConnection()) {

            // ── Header phiếu thuê ──
            try (PreparedStatement ps = con.prepareStatement(sqlHeader)) {
                ps.setInt(1, maPT);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String trangThai = rs.getString("TrangThai");
                        boolean daTra = "DaTra".equalsIgnoreCase(trangThai);

                        lblTenKH.setText(nvl(rs.getString("HoTen"), "—"));
                        lblSDT.setText(nvl(rs.getString("SDT"), "—"));
                        lblTrangThai.setText(daTra ? "✔ Đã trả" : "⏳ Đang thuê");
                        lblTrangThai.setForeground(daTra ? COLOR_BADGE_DONE : COLOR_BADGE_ACTIVE);

                        Timestamp tsThue = rs.getTimestamp("NgayThue");
                        Timestamp tsDK   = rs.getTimestamp("NgayTraDuKien");
                        Timestamp tsTT   = rs.getTimestamp("NgayTraThucTe");

                        lblNgayThue.setText(formatTs(tsThue));
                        lblNgayTraDK.setText(formatTs(tsDK));
                        lblNgayTraThucTe.setText(tsTT != null ? formatTs(tsTT) : "— (chưa trả)");

                        double tienCoc       = rs.getDouble("TienCoc");
                        double tienPhatDB    = rs.getDouble("TienPhat");
                        double donGiaThueGoc = rs.getDouble("TongDonGiaThue"); // SUM(ct.DonGiaThue)/ngày

                        // ── Tiền thuê ban đầu (tổng đơn giá gốc × 1 ngày cơ bản) ──
                        // Hiểu là: phí thuê lúc lập phiếu = donGiaThueGoc (đã là tổng /ngày)
                        // Để tính đúng số ngày thuê ban đầu cần NgayThue & NgayTraDuKien ban đầu,
                        // nhưng ta lưu DonGiaThue = giá/ngày → TienThueBanDau = DonGiaThue (×1 mặc định)
                        // hoặc bạn có thể lưu DonGiaThue = tổng tiền đã thu lúc lập phiếu.
                        // Giả định: DonGiaThue = giá/ngày/CD → TienThueBanDau = SUM(DonGiaThue) × soNgayGoc
                        // Tính soNgayGoc từ NgayThue → NgayTraDuKien hiện tại (xấp xỉ)
                        long soNgayThue = 1;
                        if (tsThue != null && tsDK != null) {
                            soNgayThue = java.time.temporal.ChronoUnit.DAYS.between(
                                tsThue.toLocalDateTime().toLocalDate(),
                                tsDK.toLocalDateTime().toLocalDate());
                            if (soNgayThue <= 0) soNgayThue = 1;
                        }
                        // TienThueBanDau = đơn giá gốc/ngày × số ngày gốc
                        double tienThueBanDau = donGiaThueGoc;

                        // ── Tiền gia hạn thêm ──
                        // GiaThueNgay hiện tại lấy từ bảng detail (tổng sau khi query),
                        // nhưng ta tính ở đây sau khi có tổng GiaThueNgay từ query detail
                        // → defer sang sau query detail; tạm để 0, set lại bên dưới.
                        // Lưu tienThueBanDau để dùng sau.
                        final double _tienThueBanDau = tienThueBanDau;
                        final double _soNgayThue     = soNgayThue;
                        final double _tienCoc        = tienCoc;
                        final double _tienPhatDB     = tienPhatDB;
                        final boolean _daTra         = daTra;
                        final Timestamp _tsDK        = tsDK;

                        // Tiền cọc
                        lblTienCoc.setText(String.format("%,.0f VNĐ", tienCoc));
                        lblTienCoc.setForeground(TEXT_WHITE);

                        // Phí đã ghi nhận trong DB
                        if (tienPhatDB > 0) {
                            lblTienPhatDB.setText(String.format("%,.0f VNĐ", tienPhatDB));
                            lblTienPhatDB.setForeground(COLOR_WARN);
                        } else {
                            lblTienPhatDB.setText("0 VNĐ  ✓");
                            lblTienPhatDB.setForeground(COLOR_OK);
                        }

                        // Tiền thuê ban đầu
                        lblThueBanDau.setText(String.format("%,.0f VNĐ", tienThueBanDau));
                        lblThueBanDau.setForeground(COLOR_BASE);

                        // ── Phạt trễ tạm tính ──
                        double phatTreTamTinh = 0;
                        if (!daTra && tsDK != null) {
                            LocalDateTime ngayDK = tsDK.toLocalDateTime();
                            LocalDateTime homNay = LocalDate.now().atStartOfDay();
                            if (homNay.isAfter(ngayDK)) {
                                long ngayTre = java.time.temporal.ChronoUnit.DAYS.between(
                                    ngayDK.toLocalDate(), homNay.toLocalDate());
                                if (ngayTre <= 0) ngayTre = 1;
                                phatTreTamTinh = ngayTre * 10_000;
                            }
                        }

                        if (daTra) {
                            lblPhatTreTT.setText("Đã chốt khi trả ✓");
                            lblPhatTreTT.setForeground(TEXT_MUTED);
                            lblPhatTreTT.setFont(FONT_SMALL);
                        } else if (phatTreTamTinh > 0) {
                            lblPhatTreTT.setText(String.format("~%,.0f VNĐ ⚠", phatTreTamTinh));
                            lblPhatTreTT.setForeground(COLOR_PENALTY);
                        } else {
                            lblPhatTreTT.setText("Không có  ✓");
                            lblPhatTreTT.setForeground(COLOR_OK);
                        }

                        // Kết quả quyết toán
                        double tongPhatUocTinh = tienPhatDB + phatTreTamTinh;
                        double delta = tienCoc - tongPhatUocTinh;

                        if (daTra) {
                            lblCongThuc.setText(String.format(
                                "Quyết toán: %.0f (cọc) − %.0f (phí đã thu) = %.0f VNĐ",
                                tienCoc, tienPhatDB, delta));
                        } else {
                            lblCongThuc.setText(String.format(
                                "Ước tính: %.0f (cọc) − %.0f (phí DB) − %.0f (phạt trễ TT) = %.0f VNĐ",
                                tienCoc, tienPhatDB, phatTreTamTinh, delta));
                        }

                        if (daTra) {
                            if (delta > 0) {
                                lblDelta.setText(String.format("Hoàn %,.0f VNĐ", delta));
                                lblDelta.setForeground(COLOR_OK);
                            } else if (delta == 0) {
                                lblDelta.setText("Vừa đủ ✓");
                                lblDelta.setForeground(COLOR_OK);
                            } else {
                                lblDelta.setText(String.format("Thu thêm %,.0f VNĐ", Math.abs(delta)));
                                lblDelta.setForeground(COLOR_PENALTY);
                            }
                        } else {
                            if (delta > 0) {
                                lblDelta.setText(String.format("~Hoàn %,.0f VNĐ", delta));
                                lblDelta.setForeground(COLOR_OK);
                            } else if (delta == 0) {
                                lblDelta.setText("~Vừa đủ");
                                lblDelta.setForeground(COLOR_OK);
                            } else {
                                lblDelta.setText(String.format("~Thu thêm %,.0f VNĐ", Math.abs(delta)));
                                lblDelta.setForeground(COLOR_WARN);
                            }
                        }

                        // Lưu tạm để tính DoanhThu sau khi có GiaThueNgay từ detail
                        // → dùng field tạm thời
                        _pendingTienThueBanDau = tienThueBanDau;
                        _pendingTienPhatDB     = tienPhatDB;
                        _pendingPhatTreTT      = phatTreTamTinh;
                        _pendingSoNgayThue     = _soNgayThue;
                    }
                }
            }

            // ── Chi tiết CD ──
            double tongGiaThueNgayHienTai = 0; // SUM(GiaThueNgay) hiện tại
            try (PreparedStatement ps = con.prepareStatement(sqlDetail)) {
                ps.setInt(1, maPT);
                try (ResultSet rsDetail = ps.executeQuery()) {
                    tableModel.setRowCount(0);
                    while (rsDetail.next()) {
                        double giaThueNgay = rsDetail.getDouble("GiaThueNgay");
                        double donGiaThue  = rsDetail.getDouble("DonGiaThue");
                        tongGiaThueNgayHienTai += giaThueNgay;
                        tableModel.addRow(new Object[]{
                            "CD" + rsDetail.getInt("MaCD"),
                            "G"  + rsDetail.getInt("MaGame"),
                            rsDetail.getString("TenGame"),
                            String.format("%,.0f VNĐ", giaThueNgay),
                            String.format("%,.0f VNĐ", donGiaThue)
                        });
                    }
                    if (tableModel.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(this,
                            "Phiếu thuê PT" + maPT + " không có CD nào.",
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }

            // ── Tính Tiền Gia Hạn & Doanh Thu (sau khi có GiaThueNgay) ──
            // TienGiaHan = SUM(GiaThueNgay) × soNgayThue − TienThueBanDau
            // (phần chênh lệch do giá tăng hoặc gia hạn thêm ngày)
            double tienGiaHan = tongGiaThueNgayHienTai * _pendingSoNgayThue - _pendingTienThueBanDau;
            if (tienGiaHan < 0) tienGiaHan = 0; // không âm

            double doanhThu = _pendingTienThueBanDau + _pendingTienPhatDB;

            lblGiaHan.setText(tienGiaHan > 0
                ? String.format("%,.0f VNĐ", tienGiaHan)
                : "Không có  ✓");
            lblGiaHan.setForeground(tienGiaHan > 0 ? COLOR_EXTEND : COLOR_OK);

            lblDoanhThu.setText(String.format("%,.0f VNĐ", doanhThu));
            lblDoanhThu.setForeground(COLOR_REVENUE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi tải dữ liệu: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Biến tạm giữa 2 query trong loadData()
    private double _pendingTienThueBanDau = 0;
    private double _pendingTienPhatDB     = 0;
    private double _pendingPhatTreTT      = 0;
    private double _pendingSoNgayThue     = 1;

    /* ==================== UI HELPERS ==================== */

    private JPanel infoRow(JPanel... cards) {
        JPanel row = new JPanel(new GridLayout(1, cards.length, 10, 0));
        row.setBackground(BG_DARK);
        for (JPanel c : cards) row.add(c);
        return row;
    }

    private JPanel infoCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(ACCENT, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_SMALL);
        titleLbl.setForeground(TEXT_MUTED);
        card.add(titleLbl,   BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    /**
     * summaryCard với màu viền tuỳ theo loại ô.
     * accent=true → nền đậm hơn, nổi bật hơn.
     */
    private JPanel summaryCard(String title, JLabel valueLabel, boolean accent, Color borderColor) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(accent ? new Color(50, 32, 108) : BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(borderColor, accent ? 2 : 1, true),
            new EmptyBorder(10, 14, 10, 14)
        ));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_SMALL);
        titleLbl.setForeground(TEXT_MUTED);
        card.add(titleLbl,   BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JLabel cardValue() {
        JLabel l = new JLabel("—");
        l.setFont(FONT_HEADER);
        l.setForeground(TEXT_WHITE);
        return l;
    }

    /** Nhãn tiêu đề nhóm (DOANH THU / QUYẾT TOÁN CỌC) */
    private JPanel sectionLabel(String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setBackground(BG_DARK);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(PURPLE_HEADER);
        p.add(lbl);
        return p;
    }

    /** Đường kẻ ngang phân tách 2 hàng summary */
    private JPanel separator() {
        JPanel sep = new JPanel();
        sep.setBackground(new Color(80, 60, 140));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setPreferredSize(new Dimension(0, 1));
        return sep;
    }

    private JTable buildStyledTable(DefaultTableModel model) {
        JTable t = new JTable(model) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? PURPLE_ROW : PURPLE_ALT);
                    c.setForeground(TEXT_DARK);
                } else {
                    c.setBackground(ACCENT);
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        };
        t.setFont(FONT_CELL);
        t.setRowHeight(36);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(ACCENT);
        t.setSelectionForeground(Color.WHITE);
        t.setBackground(PURPLE_ALT);

        JTableHeader header = t.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable tbl, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = new JLabel(v == null ? "" : v.toString());
                lbl.setFont(FONT_HEADER);
                lbl.setForeground(Color.WHITE);
                lbl.setBackground(PURPLE_HEADER);
                lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(8, 12, 8, 12));
                return lbl;
            }
        });
        header.setBackground(PURPLE_HEADER);
        header.setPreferredSize(new Dimension(0, 38));
        return t;
    }

    private JScrollPane styledScroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(new LineBorder(PURPLE_HEADER, 1, true));
        sp.getViewport().setBackground(PURPLE_ALT);
        return sp;
    }

    private String formatTs(Timestamp ts) {
        return ts == null ? "—" : ts.toLocalDateTime().format(FMT);
    }

    private String nvl(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }

    /* ==================== ROUND BUTTON ==================== */
    static class RoundBtn extends JButton {
        private final Color bg, fg;
        RoundBtn(String text, Color bg, Color fg) {
            super(text);
            this.bg = bg; this.fg = fg;
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setForeground(fg);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}