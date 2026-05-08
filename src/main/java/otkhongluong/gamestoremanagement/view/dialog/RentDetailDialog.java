package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.util.DBConnection;

import javax.swing.*;
import javax.swing.border.*;
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
 *   TienThueBanDau  = SUM(ct.DonGiaThue) — tổng đơn giá gốc lúc lập phiếu (đã bao gồm số ngày).
 *   SoNgayGoc       = TienThueBanDau / SUM(sp.GiaThueNgay) — tính ngược từ tiền & giá hiện tại.
 *   TienGiaHan      = TienPhat trong DB (toàn bộ phần phát sinh sau gốc).
 *   PhatTreTamTinh  = phạt trễ ước tính hôm nay (chỉ khi đang thuê & quá hạn, chưa ghi DB).
 *   DoanhThu        = TienThueBanDau + TienPhatDB.
 *
 * Kết quả quyết toán:
 *   delta = TienCoc − TienThueBanDau − PhatTreTamTinh
 *   delta > 0 → hoàn tiền | = 0 → vừa đủ | < 0 → thu thêm
 */
public class RentDetailDialog extends JDialog {

    /* ===== COLORS ===== */
    private static final Color BG_DARK        = new Color(35, 20, 85);
    private static final Color BG_CARD        = new Color(55, 35, 110);
    private static final Color PURPLE_HEADER  = new Color(155, 135, 245);
    private static final Color ACCENT         = new Color(130, 90, 230);
    private static final Color TEXT_WHITE     = Color.WHITE;
    private static final Color TEXT_MUTED     = new Color(160, 148, 200);
    private static final Color BTN_CANCEL     = new Color(252, 129, 129);
    private static final Color COLOR_PENALTY  = new Color(220, 53, 69);
    private static final Color COLOR_OK       = new Color(40, 167, 69);
    private static final Color COLOR_WARN     = new Color(255, 165, 0);
    private static final Color COLOR_REVENUE  = new Color(80, 200, 160);
    private static final Color COLOR_EXTEND   = new Color(100, 180, 255);
    private static final Color COLOR_BASE     = new Color(200, 180, 255);
    private static final Color COLOR_BADGE_ACTIVE = new Color(255, 193, 7);
    private static final Color COLOR_BADGE_DONE   = new Color(40, 167, 69);

    /* ===== FONTS ===== */
    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_BIG    = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_MICRO  = new Font("Segoe UI", Font.PLAIN, 10);

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /* ===== WIDGETS — Info ===== */
    private JLabel lblTenKH, lblSDT, lblTrangThai;
    private JLabel lblNgayThue, lblNgayTraDK, lblNgayTraThucTe;

    /* ===== WIDGETS — CD Info Card ===== */
    private JLabel lblMaCD, lblMaGame, lblTenGame;
    private JLabel lblGiaThueNgayHT, lblDonGiaThueGoc;

    /* ===== WIDGETS — Summary ===== */
    private JLabel lblDoanhThu;
    private JPanel groupGiaHanPanel;
    private JLabel lblThueBanDau;
    private JLabel lblGiaHan;
    private JLabel lblTienPhatDB;
    private JLabel lblTienCoc;
    private JLabel lblPhatTreTT;
    private JLabel lblDelta;
    private JLabel lblCongThuc;

    private final int maPT;

    // Trạng thái tạm giữa 2 query
    private double _pendingTienThueBanDau = 0;
    private double _pendingTienPhatDB     = 0;
    private double _pendingPhatTreTT      = 0;
    private Timestamp _pendingTsThue = null;
    private Timestamp _pendingTsDK   = null;

    public RentDetailDialog(Frame parent, int maPT) {
        super(parent, "Chi tiết phiếu thuê", true);
        this.maPT = maPT;

        setMinimumSize(new Dimension(760, 600));
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

        // CD Info Card (thay thế bảng)
        p.add(buildCDCard(), BorderLayout.CENTER);

        p.add(buildSummary(), BorderLayout.SOUTH);
        return p;
    }

    /**
     * Card thông tin CD — hiển thị cố định thay vì bảng danh sách.
     * Layout: 5 ô ngang trong 1 card có viền đậm.
     */
    private JPanel buildCDCard() {
        lblMaCD           = cardValue();
        lblMaGame         = cardValue();
        lblTenGame        = cardValue();
        lblGiaThueNgayHT  = cardValue();
        lblDonGiaThueGoc  = cardValue();

        // Header nhãn
        JPanel headerRow = new JPanel(new GridLayout(1, 4, 10, 0));
        headerRow.setBackground(PURPLE_HEADER);
        headerRow.setBorder(new EmptyBorder(8, 14, 8, 14));
        for (String h : new String[]{"Mã CD", "Mã Game", "Tên Game", "Giá thuê/ngày"}) {
            JLabel lbl = new JLabel(h);
            lbl.setFont(FONT_HEADER);
            lbl.setForeground(Color.WHITE);
            headerRow.add(lbl);
        }

        // Value row
        JPanel valueRow = new JPanel(new GridLayout(1, 4, 10, 0));
        valueRow.setBackground(new Color(55, 35, 110));
        valueRow.setBorder(new EmptyBorder(12, 14, 12, 14));
        valueRow.add(lblMaCD);
        valueRow.add(lblMaGame);
        valueRow.add(lblTenGame);
        valueRow.add(lblGiaThueNgayHT);

        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(new Color(55, 35, 110));
        card.setBorder(new CompoundBorder(
            new LineBorder(ACCENT, 1, true),
            new EmptyBorder(0, 0, 0, 0)
        ));
        card.add(headerRow, BorderLayout.NORTH);
        card.add(valueRow,  BorderLayout.CENTER);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setPreferredSize(new Dimension(0, 100));
        return card;
    }

    /* ==================== SUMMARY ==================== */
    private JPanel buildSummary() {
        lblDoanhThu   = cardValue(); lblDoanhThu.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblThueBanDau = cardValue();
        lblGiaHan     = cardValue();
        lblTienPhatDB = cardValue();
        lblTienCoc    = cardValue();
        lblPhatTreTT  = cardValue();
        lblDelta      = cardValue(); lblDelta.setFont(FONT_BIG);
        lblCongThuc   = new JLabel(" ");
        lblCongThuc.setFont(FONT_MICRO);
        lblCongThuc.setForeground(TEXT_MUTED);

        // ── Hàng 0: Tổng doanh thu ──
        JPanel cardDoanhThu = new JPanel(new BorderLayout(0, 6));
        cardDoanhThu.setBackground(new Color(30, 90, 70));
        cardDoanhThu.setBorder(new CompoundBorder(
            new LineBorder(COLOR_REVENUE, 2, true),
            new EmptyBorder(12, 20, 12, 20)
        ));
        JPanel doanhThuTop = new JPanel(new BorderLayout());
        doanhThuTop.setBackground(new Color(30, 90, 70));
        JLabel lblDTTitle = new JLabel("TỔNG DOANH THU ƯỚC TÍNH");
        lblDTTitle.setFont(FONT_SMALL);
        lblDTTitle.setForeground(COLOR_REVENUE);
        JLabel lblDTNote = new JLabel("= Tiền thuê ban đầu + Tiền phát sinh (DB)");
        lblDTNote.setFont(FONT_MICRO);
        lblDTNote.setForeground(TEXT_MUTED);
        doanhThuTop.add(lblDTTitle, BorderLayout.WEST);
        doanhThuTop.add(lblDTNote,  BorderLayout.EAST);
        cardDoanhThu.add(doanhThuTop, BorderLayout.NORTH);
        cardDoanhThu.add(lblDoanhThu, BorderLayout.CENTER);
        cardDoanhThu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        cardDoanhThu.setPreferredSize(new Dimension(0, 68));

        // ── Hàng 1: Thuê ban đầu | Gia hạn ──
        JPanel groupThue = new JPanel(new BorderLayout(0, 6));
        groupThue.setBackground(new Color(70, 50, 130));
        groupThue.setBorder(new CompoundBorder(
            new LineBorder(new Color(100, 80, 180), 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        JLabel lblGThueTitle = new JLabel("THUÊ BAN ĐẦU");
        lblGThueTitle.setFont(FONT_SMALL);
        lblGThueTitle.setForeground(TEXT_MUTED);
        lblGThueTitle.setBorder(new EmptyBorder(0, 0, 4, 0));
        groupThue.add(lblGThueTitle, BorderLayout.NORTH);
        groupThue.add(summaryCard("Tiền thuê ban đầu", lblThueBanDau, false, COLOR_BASE), BorderLayout.CENTER);

        JPanel cardGiaHan = summaryCard("Tiền gia hạn (đã trả)", lblGiaHan,     false, COLOR_EXTEND);
        JPanel cardTreHan = summaryCard("Trễ hạn đã đóng",       lblTienPhatDB, false, COLOR_WARN);
        JPanel giaHanCards = new JPanel(new GridLayout(1, 2, 6, 0));
        giaHanCards.setBackground(new Color(30, 60, 100));
        giaHanCards.add(cardGiaHan);
        giaHanCards.add(cardTreHan);

        groupGiaHanPanel = new JPanel(new BorderLayout(0, 6));
        groupGiaHanPanel.setBackground(new Color(30, 60, 100));
        groupGiaHanPanel.setBorder(new CompoundBorder(
            new LineBorder(new Color(60, 100, 160), 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        JLabel lblGGHTitle = new JLabel("GIA HẠN");
        lblGGHTitle.setFont(FONT_SMALL);
        lblGGHTitle.setForeground(TEXT_MUTED);
        lblGGHTitle.setBorder(new EmptyBorder(0, 0, 4, 0));
        groupGiaHanPanel.add(lblGGHTitle, BorderLayout.NORTH);
        groupGiaHanPanel.add(giaHanCards, BorderLayout.CENTER);
        groupGiaHanPanel.setVisible(false);

        JPanel row1 = new JPanel(new GridLayout(1, 2, 10, 0));
        row1.setBackground(BG_DARK);
        row1.add(groupThue);
        row1.add(groupGiaHanPanel);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        row1.setPreferredSize(new Dimension(0, 110));

        // ── Hàng 2: Quyết toán cọc ──
        JPanel sectionLabelB = sectionLabel("QUYẾT TOÁN CỌC");
        JPanel rowB = new JPanel(new GridLayout(1, 3, 8, 0));
        rowB.setBackground(BG_DARK);
        rowB.add(summaryCard("Tiền cọc ban đầu",   lblTienCoc,   false, PURPLE_HEADER));
        rowB.add(summaryCard("Phạt trễ tạm tính",  lblPhatTreTT, false, COLOR_WARN));
        rowB.add(summaryCard("Kết quả quyết toán", lblDelta,     true,  new Color(155, 135, 245)));
        rowB.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        rowB.setPreferredSize(new Dimension(0, 82));

        // ── Ghi chú ──
        JPanel noteRow = new JPanel(new BorderLayout());
        noteRow.setBackground(BG_DARK);
        noteRow.setBorder(new EmptyBorder(4, 2, 0, 0));
        JPanel noteInner = new JPanel(new GridLayout(2, 1, 0, 1));
        noteInner.setBackground(BG_DARK);
        noteInner.add(lblCongThuc);
        JLabel noteStatic = new JLabel(
            "* Phạt trễ tạm tính: ước tính chưa chốt — chỉ chốt khi trả CD.");
        noteStatic.setFont(FONT_MICRO);
        noteStatic.setForeground(TEXT_MUTED);
        noteInner.add(noteStatic);
        noteRow.add(noteInner, BorderLayout.WEST);

        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBackground(BG_DARK);
        wrap.setBorder(new EmptyBorder(10, 0, 0, 0));
        wrap.add(cardDoanhThu);
        wrap.add(Box.createVerticalStrut(8));
        wrap.add(row1);
        wrap.add(Box.createVerticalStrut(8));
        wrap.add(separator());
        wrap.add(Box.createVerticalStrut(6));
        wrap.add(sectionLabelB);
        wrap.add(Box.createVerticalStrut(4));
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
        RoundBtn btnClose = new RoundBtn("Đóng", BTN_CANCEL, TEXT_WHITE);
        btnClose.setPreferredSize(new Dimension(110, 40));
        btnClose.addActionListener(e -> dispose());
        btnRow.add(btnClose);

        p.add(sep,    BorderLayout.NORTH);
        p.add(btnRow, BorderLayout.CENTER);
        return p;
    }

    /* ==================== LOAD DATA ==================== */
    private void loadData() {
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

        // Mỗi phiếu 1 CD → lấy 1 dòng duy nhất
        String sqlDetail =
            "SELECT TOP 1 cd.MaCD, g.MaGame, g.TenGame, sp.GiaThueNgay, ct.DonGiaThue " +
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
                        boolean daTra = "DaTra".equalsIgnoreCase(rs.getString("TrangThai"));

                        lblTenKH.setText(nvl(rs.getString("HoTen"), "—"));
                        lblSDT.setText(nvl(rs.getString("SDT"), "—"));
                        lblTrangThai.setText(daTra ? "Đã trả" : "Đang thuê");
                        lblTrangThai.setForeground(daTra ? COLOR_BADGE_DONE : COLOR_BADGE_ACTIVE);

                        Timestamp tsThue = rs.getTimestamp("NgayThue");
                        Timestamp tsDK   = rs.getTimestamp("NgayTraDuKien");
                        Timestamp tsTT   = rs.getTimestamp("NgayTraThucTe");

                        lblNgayThue.setText(formatTs(tsThue));
                        lblNgayTraDK.setText(formatTs(tsDK));
                        lblNgayTraThucTe.setText(tsTT != null ? formatTs(tsTT) : "— (chưa trả)");

                        double tienCoc        = rs.getDouble("TienCoc");
                        double tienPhatDB     = rs.getDouble("TienPhat");
                        // TienThueBanDau = SUM(ct.DonGiaThue) — đã là tổng tiền (có nhân ngày khi lập phiếu)
                        double tienThueBanDau = rs.getDouble("TongDonGiaThue");

                        lblTienCoc.setText(String.format("%,.0f VNĐ", tienCoc));
                        lblTienCoc.setForeground(TEXT_WHITE);

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
                            lblPhatTreTT.setText("Đã chốt khi trả!");
                            lblPhatTreTT.setForeground(TEXT_MUTED);
                            lblPhatTreTT.setFont(FONT_SMALL);
                        } else if (phatTreTamTinh > 0) {
                            lblPhatTreTT.setText(String.format("~%,.0f VNĐ", phatTreTamTinh));
                            lblPhatTreTT.setForeground(COLOR_PENALTY);
                        } else {
                            lblPhatTreTT.setText("Không có!");
                            lblPhatTreTT.setForeground(COLOR_OK);
                        }

                        // Kết quả quyết toán: Cọc − ThueBanDau − PhatTreTamTinh
                        double delta = tienCoc - tienThueBanDau - phatTreTamTinh;

                        if (daTra) {
                            lblCongThuc.setText(String.format(
                                "Quyết toán: %.0f (cọc) − %.0f (thuê) = %.0f VNĐ",
                                tienCoc, tienThueBanDau, delta));
                        } else {
                            lblCongThuc.setText(String.format(
                                "Ước tính: %.0f (cọc) − %.0f (thuê) − %.0f (phạt trễ TT) = %.0f VNĐ",
                                tienCoc, tienThueBanDau, phatTreTamTinh, delta));
                        }

                        if (daTra) {
                            if (delta > 0) {
                                lblDelta.setText(String.format("Hoàn %,.0f VNĐ", delta));
                                lblDelta.setForeground(COLOR_OK);
                            } else if (delta == 0) {
                                lblDelta.setText("Vừa đủ!");
                                lblDelta.setForeground(COLOR_OK);
                            } else {
                                lblDelta.setText(String.format("Thu thêm %,.0f VNĐ", Math.abs(delta)));
                                lblDelta.setForeground(COLOR_PENALTY);
                            }
                        } else {
                            if (delta > 0) {
                                lblDelta.setText(String.format("Hoàn %,.0f VNĐ", delta));
                                lblDelta.setForeground(COLOR_OK);
                            } else if (delta == 0) {
                                lblDelta.setText("~Vừa đủ");
                                lblDelta.setForeground(COLOR_OK);
                            } else {
                                lblDelta.setText(String.format("Thu thêm %,.0f VNĐ", Math.abs(delta)));
                                lblDelta.setForeground(COLOR_WARN);
                            }
                        }

                        _pendingTienThueBanDau = tienThueBanDau;
                        _pendingTienPhatDB     = tienPhatDB;
                        _pendingPhatTreTT      = phatTreTamTinh;
                        _pendingTsThue         = tsThue;   // thêm dòng này
                        _pendingTsDK           = tsDK;     // thêm dòng này
                    }
                }
            }

            // ── Chi tiết CD (1 dòng duy nhất) ──
            try (PreparedStatement ps = con.prepareStatement(sqlDetail)) {
                ps.setInt(1, maPT);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        double giaThueNgayHT = rs.getDouble("GiaThueNgay");
                        double donGiaThueGoc = rs.getDouble("DonGiaThue");

                        lblMaCD.setText("CD" + rs.getInt("MaCD"));
                        lblMaGame.setText("G" + rs.getInt("MaGame"));
                        lblTenGame.setText(nvl(rs.getString("TenGame"), "—"));
                        lblGiaThueNgayHT.setText(String.format("%,.0f VNĐ", giaThueNgayHT));
                        lblDonGiaThueGoc.setText(String.format("%,.0f VNĐ", donGiaThueGoc));

                        // SoNgayGoc = TienThueBanDau / GiaThueNgay_hiện_tại
                        long soNgayGoc = 1;
                        if (giaThueNgayHT > 0) {
                            soNgayGoc = Math.round(_pendingTienThueBanDau / giaThueNgayHT);
                            if (soNgayGoc <= 0) soNgayGoc = 1;
                        }

                        // Hiển thị tiền thuê ban đầu kèm số ngày tính được
                        lblThueBanDau.setText(String.format("%,.0f VNĐ  (%d ngày)",
                            _pendingTienThueBanDau, soNgayGoc));
                        lblThueBanDau.setForeground(COLOR_BASE);

                        // ── Gia hạn ──
                        // TienGiaHan = phần trong TienPhatDB dùng cho gia hạn (nếu có)
                        // Đây là phần "phát sinh" thêm ngoài thuê gốc lưu trong pt.TienPhat
                        // Logic: nếu TienPhat > 0 thì có thể là gia hạn hoặc phạt trễ
                        // Tách: TienGiaHan = GiaThueNgay × soNgayGocThem; TienTreHan = phần còn lại
                        // Để đơn giản và chính xác: hiển thị pt.TienPhat nguyên, không tách
                       if (_pendingTienPhatDB > 0) {
                            Timestamp tsThue = _pendingTsThue;
                            Timestamp tsDK   = _pendingTsDK;

                            long soNgayTong  = java.time.temporal.ChronoUnit.DAYS.between(
                                tsThue.toLocalDateTime().toLocalDate(),
                                tsDK.toLocalDateTime().toLocalDate()
                            );
                            long soNgayGocL  = (giaThueNgayHT > 0)
                                ? Math.round(_pendingTienThueBanDau / giaThueNgayHT) : 1;
                            long soNgayGiaHan = soNgayTong - soNgayGocL;
                            if (soNgayGiaHan < 0) soNgayGiaHan = 0;

                            double tienGiaHan  = soNgayGiaHan * giaThueNgayHT;
                            double treHanDaDong = _pendingTienPhatDB - tienGiaHan;
                            if (treHanDaDong < 0) treHanDaDong = 0;

                            lblGiaHan.setText(String.format("%,.0f VNĐ  (%d ngày)", tienGiaHan, soNgayGiaHan));
                            lblGiaHan.setForeground(COLOR_EXTEND);

                            if (treHanDaDong > 0) {
                                lblTienPhatDB.setText(String.format("%,.0f VNĐ", treHanDaDong));
                                lblTienPhatDB.setForeground(COLOR_WARN);
                            } else {
                                lblTienPhatDB.setText("Không có!");
                                lblTienPhatDB.setForeground(COLOR_OK);
                            }

                            groupGiaHanPanel.setVisible(true);
                        } else {
                            groupGiaHanPanel.setVisible(false);
                        }

                        // DoanhThu = TienThueBanDau + TienPhatDB
                        double doanhThu = _pendingTienThueBanDau + _pendingTienPhatDB;
                        lblDoanhThu.setText(String.format("%,.0f VNĐ", doanhThu));
                        lblDoanhThu.setForeground(COLOR_REVENUE);

                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Phiếu thuê PT" + maPT + " không tìm thấy CD.",
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi tải dữ liệu: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

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

    private JPanel separator() {
        JPanel sep = new JPanel();
        sep.setBackground(new Color(80, 60, 140));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setPreferredSize(new Dimension(0, 1));
        return sep;
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