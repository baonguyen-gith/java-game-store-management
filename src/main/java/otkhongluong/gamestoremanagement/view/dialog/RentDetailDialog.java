package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.controller.RentController;
import otkhongluong.gamestoremanagement.model.RentDetailData;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * RentDetailDialog — xem chi tiết phiếu thuê.
 *
 * FIX BUG 1 (UI): Thêm nhánh hiển thị "Phạt đã thu khi gia hạn"
 * cho trường hợp đang thuê, còn hạn, nhưng phatTreTamTinh = tienPhatDB > 0.
 * Trước đây label chỉ hiển thị "Chưa quá hạn" dù có phạt đã thu, gây nhầm lẫn.
 */
public class RentDetailDialog extends JDialog {

    private static final int DIEM_TO_VND = 5_000;

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
    private static final Color COLOR_DIEM     = new Color(72, 199, 142);
    private static final Color COLOR_NET      = new Color(168, 144, 255);
    private static final Color COLOR_BADGE_ACTIVE = new Color(255, 193, 7);
    private static final Color COLOR_BADGE_DONE   = new Color(40, 167, 69);
    private static final Color COLOR_HOAN_COC     = new Color(80, 200, 160);
    private static final Color COLOR_NO_THEM      = new Color(220, 53, 69);

    /* ===== FONTS ===== */
    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_BIG    = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_MICRO  = new Font("Segoe UI", Font.PLAIN, 10);

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RentController ctrl = new RentController();

    /* ===== WIDGETS — Info ===== */
    private JLabel lblTenKH, lblSDT, lblTrangThai;
    private JLabel lblNgayThue, lblNgayTraDK, lblNgayTraThucTe;

    /* ===== WIDGETS — CD Info Card ===== */
    private JLabel lblMaCD, lblMaGame, lblTenGame;
    private JLabel lblGiaThueNgayHT;

    /* ===== WIDGETS — Summary ===== */
    private JLabel lblDoanhThu;
    private JLabel lblThueBanDau;
    private JLabel lblThueBanDauTitle;
    private JLabel lblGiamDiem;
    private JLabel lblTienThueNetDiem;
    private JLabel lblTienPhatSinh;
    private JLabel lblPhatTreTT;
    private JLabel lblCongThuc;

    private final int maPT;

    public RentDetailDialog(Frame parent, int maPT) {
        super(parent, "Chi tiết phiếu thuê", true);
        this.maPT = maPT;

        setSize(new Dimension(960, 600));
        setMinimumSize(new Dimension(900, 580));
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

        lblTenKH     = cardValue();
        lblSDT       = cardValue();
        lblTrangThai = cardValue();
        JPanel row1 = infoRow(
            infoCard("Khách hàng",    lblTenKH),
            infoCard("Số điện thoại", lblSDT),
            infoCard("Trạng thái",    lblTrangThai)
        );
        row1.setPreferredSize(new Dimension(0, 68));

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

        p.add(buildCDCard(),  BorderLayout.CENTER);
        p.add(buildSummary(), BorderLayout.SOUTH);
        return p;
    }

    /* ==================== CD CARD ==================== */
    private JPanel buildCDCard() {
        lblMaCD          = cardValue();
        lblMaGame        = cardValue();
        lblTenGame       = cardValue();
        lblGiaThueNgayHT = cardValue();

        JPanel headerRow = new JPanel(new GridLayout(1, 4, 10, 0));
        headerRow.setBackground(PURPLE_HEADER);
        headerRow.setBorder(new EmptyBorder(8, 14, 8, 14));
        for (String h : new String[]{"Mã CD", "Mã Game", "Tên Game", "Giá thuê/ngày"}) {
            JLabel lbl = new JLabel(h);
            lbl.setFont(FONT_HEADER);
            lbl.setForeground(Color.WHITE);
            headerRow.add(lbl);
        }

        JPanel valueRow = new JPanel(new GridLayout(1, 4, 10, 0));
        valueRow.setBackground(new Color(55, 35, 110));
        valueRow.setBorder(new EmptyBorder(12, 14, 12, 14));
        valueRow.add(lblMaCD);
        valueRow.add(lblMaGame);
        valueRow.add(lblTenGame);
        valueRow.add(lblGiaThueNgayHT);

        JPanel card = new JPanel(new BorderLayout());
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
        lblDoanhThu        = cardValue(); lblDoanhThu.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblThueBanDau      = cardValue();
        lblGiamDiem        = cardValue();
        lblTienThueNetDiem = cardValue();
        lblTienPhatSinh    = cardValue();
        lblPhatTreTT       = cardValue();
        lblCongThuc        = new JLabel(" ");
        lblCongThuc.setFont(FONT_MICRO);
        lblCongThuc.setForeground(TEXT_MUTED);

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
        JLabel lblDTNote = new JLabel(
            "= Tiền thuê sau giảm điểm + Phát sinh (gia hạn + phạt trễ đã đóng + phạt trễ tạm tính nếu đang quá hạn)");
        lblDTNote.setFont(FONT_MICRO);
        lblDTNote.setForeground(TEXT_MUTED);
        doanhThuTop.add(lblDTTitle, BorderLayout.WEST);
        doanhThuTop.add(lblDTNote,  BorderLayout.EAST);
        cardDoanhThu.add(doanhThuTop, BorderLayout.NORTH);
        cardDoanhThu.add(lblDoanhThu, BorderLayout.CENTER);
        cardDoanhThu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        cardDoanhThu.setPreferredSize(new Dimension(0, 68));

        lblThueBanDauTitle = new JLabel("Tiền thuê ban đầu");
        lblThueBanDauTitle.setFont(FONT_SMALL);
        lblThueBanDauTitle.setForeground(TEXT_MUTED);
        JPanel cardThueBanDau = new JPanel(new BorderLayout(0, 4));
        cardThueBanDau.setBackground(BG_CARD);
        cardThueBanDau.setBorder(new CompoundBorder(
            new LineBorder(COLOR_BASE, 1, true),
            new EmptyBorder(10, 14, 10, 14)
        ));
        cardThueBanDau.add(lblThueBanDauTitle, BorderLayout.NORTH);
        cardThueBanDau.add(lblThueBanDau,      BorderLayout.CENTER);

        JPanel row1 = new JPanel(new GridLayout(1, 4, 8, 0));
        row1.setBackground(BG_DARK);
        row1.add(cardThueBanDau);
        row1.add(summaryCard("Giảm từ điểm tích lũy",              lblGiamDiem,        false, COLOR_DIEM));
        row1.add(summaryCard("Tiền thuê sau giảm điểm",            lblTienThueNetDiem, false, COLOR_NET));
        row1.add(summaryCard("Phát sinh (gia hạn + phạt đã đóng)", lblTienPhatSinh,    false, COLOR_WARN));
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        row1.setPreferredSize(new Dimension(0, 82));

        // Hàng phụ: Phạt trễ tạm tính (chỉ hiện khi đang thuê)
        JPanel row2 = new JPanel(new GridLayout(1, 1, 8, 0));
        row2.setBackground(BG_DARK);
        row2.add(summaryCard("Phạt trễ tạm tính", lblPhatTreTT, false, COLOR_WARN));
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row2.setPreferredSize(new Dimension(0, 70));

        JPanel noteRow = new JPanel(new BorderLayout());
        noteRow.setBackground(BG_DARK);
        noteRow.setBorder(new EmptyBorder(4, 2, 0, 0));
        JPanel noteInner = new JPanel(new GridLayout(2, 1, 0, 1));
        noteInner.setBackground(BG_DARK);
        noteInner.add(lblCongThuc);
        JLabel noteStatic = new JLabel(
            "* Phạt trễ tạm tính: ước tính chưa chốt — chỉ chốt khi trả CD.  "
          + "| Giảm điểm: từ DIEM_LICHSU (Loai='TRU', MaPT).  "
          + "| Phạt trễ = số ngày trễ × giá thuê/ngày × 1.5  "
          + "| 1 điểm = " + String.format("%,d", DIEM_TO_VND) + " VNĐ");
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
        wrap.add(Box.createVerticalStrut(6));
        wrap.add(row2);
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
        RentDetailData data = ctrl.loadRentDetail(maPT);
        if (data == null) {
            JOptionPane.showMessageDialog(this,
                "Phiếu thuê PT" + maPT + " không tìm thấy.",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        lblTenKH.setText(data.tenKH);
        lblSDT.setText(data.sdt);
        lblTrangThai.setText(data.trangThai);
        lblTrangThai.setForeground(data.daTra ? COLOR_BADGE_DONE : COLOR_BADGE_ACTIVE);
        lblThueBanDauTitle.setText(data.daTra
            ? "Tiền thuê ban đầu (đã trả)"
            : "Tiền thuê ban đầu (chưa trả)");

        lblNgayThue.setText(data.ngayThue != null
            ? data.ngayThue.toLocalDateTime().format(FMT) : "—");
        lblNgayTraDK.setText(data.ngayTraDK != null
            ? data.ngayTraDK.toLocalDateTime().format(FMT) : "—");
        lblNgayTraThucTe.setText(data.ngayTraTT != null
            ? data.ngayTraTT.toLocalDateTime().format(FMT) : "— (chưa trả)");

        lblMaCD.setText("CD" + data.maCD);
        lblMaGame.setText("G" + data.maGame);
        lblTenGame.setText(data.tenGame);
        lblGiaThueNgayHT.setText(String.format("%,.0f VNĐ/ngày", data.giaThueNgayHT));

        lblThueBanDau.setText(String.format("%,.0f VNĐ  (%d ngày)",
            data.tienThueBanDau, data.soNgayGoc));
        lblThueBanDau.setForeground(COLOR_BASE);

        if (data.giamDiem > 0) {
            int soDiem = (int) Math.round(data.giamDiem / DIEM_TO_VND);
            lblGiamDiem.setText(String.format("-%,.0f VNĐ  (%d điểm)", data.giamDiem, soDiem));
            lblGiamDiem.setForeground(COLOR_DIEM);
        } else {
            lblGiamDiem.setText("Không dùng điểm");
            lblGiamDiem.setForeground(TEXT_MUTED);
        }

        lblTienThueNetDiem.setText(String.format("%,.0f VNĐ", data.tienThueNetDiem));
        lblTienThueNetDiem.setForeground(COLOR_NET);

        double tienPhatSinh = data.tienGiaHan + data.treHanDaDong;
        if (tienPhatSinh > 0) {
            lblTienPhatSinh.setText(String.format("%,.0f VNĐ", tienPhatSinh));
            lblTienPhatSinh.setForeground(COLOR_WARN);
        } else {
            lblTienPhatSinh.setText(data.daTra ? "Không có!" : "— (chưa trả)");
            lblTienPhatSinh.setForeground(data.daTra ? COLOR_OK : TEXT_MUTED);
        }

        double doanhThu = data.daTra
            ? data.tienThueNetDiem + tienPhatSinh
            : data.tienThueNetDiem + tienPhatSinh + data.phatTreTamTinh;
        lblDoanhThu.setText(String.format("%,.0f VNĐ", doanhThu));
        lblDoanhThu.setForeground(COLOR_REVENUE);

        // ── Phạt trễ / đã thu khi gia hạn (tạm tính) ──
        if (data.daTra) {
            lblPhatTreTT.setText("Đã chốt khi trả");
            lblPhatTreTT.setForeground(TEXT_MUTED);
            lblPhatTreTT.setFont(FONT_SMALL);
        } else if (data.phatTreTamTinh > 0) {
            double phatThuanTuy = data.phatTreTamTinh - data.tienPhatDB;
            if (phatThuanTuy <= 0) {
                lblPhatTreTT.setText(String.format("%,.0f VNĐ ", data.tienPhatDB));
                lblPhatTreTT.setForeground(COLOR_WARN);
                lblPhatTreTT.setFont(FONT_HEADER);
            } else {
                String ghiChu = data.tienPhatDB > 0
                    ? String.format(" (phạt trễ mới %,.0f + đã thu GH %,.0f)", phatThuanTuy, data.tienPhatDB)
                    : "";
                lblPhatTreTT.setText(String.format("~%,.0f VNĐ%s", data.phatTreTamTinh, ghiChu));
                lblPhatTreTT.setForeground(COLOR_PENALTY);
                lblPhatTreTT.setFont(FONT_HEADER);
            }
        } else {
            lblPhatTreTT.setText("Chưa quá hạn");
            lblPhatTreTT.setForeground(COLOR_OK);
        }

        // Công thức tóm tắt
        String phatStr = data.daTra
            ? (tienPhatSinh > 0 ? String.format(" + %,.0f phát sinh", tienPhatSinh) : "")
            : (data.phatTreTamTinh > 0 ? String.format(" + ~%,.0f phạt/đã thu", data.phatTreTamTinh) : "");

        if (data.giamDiem > 0) {
            lblCongThuc.setText(String.format(
                "Doanh thu: (%,.0f − %,.0f giảm điểm)%s = %,.0f VNĐ",
                data.tienThueBanDau, data.giamDiem, phatStr, doanhThu));
        } else {
            lblCongThuc.setText(String.format(
                "Doanh thu: %,.0f thuê%s = %,.0f VNĐ",
                data.tienThueBanDau, phatStr, doanhThu));
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