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
 * MVC: Dialog chỉ gọi RentController.loadRentDetail() —
 * không có DBConnection, SQL, hay PointDAO trực tiếp.
 *
 * Logic tiền (tính trong Service/Controller, dialog chỉ hiển thị):
 *   TienThueBanDau   = SUM(ct.DonGiaThue)
 *   GiamDiem         = SUM điểm từ DIEM_LICHSU × 5.000
 *   TienThueNetDiem  = MAX(0, TienThueBanDau - GiamDiem)
 *   TienGiaHan       = soNgayGiaHan × GiaThueNgay
 *   TreHanDaDong     = TienPhat DB − TienGiaHan
 *   PhatTreTamTinh   = ngày trễ × 10.000 (chỉ khi đang thuê & quá hạn)
 *   delta            = TienCoc − TienThueNetDiem − PhatTreTamTinh − TreHanDaDong
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

    /* ===== FONTS ===== */
    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_BIG    = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_MICRO  = new Font("Segoe UI", Font.PLAIN, 10);

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /* ===== CONTROLLER (MVC) ===== */
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
    private JLabel lblGiaHan;
    private JLabel lblTienPhatDB;
    private JLabel lblTienCoc;
    private JLabel lblPhatTreTT;
    private JLabel lblDelta;
    private JLabel lblCongThuc;

    private final int maPT;

    public RentDetailDialog(Frame parent, int maPT) {
        super(parent, "Chi tiết phiếu thuê", true);
        this.maPT = maPT;

        setMinimumSize(new Dimension(900, 620));
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
        lblGiaHan          = cardValue();
        lblTienPhatDB      = cardValue();
        lblTienCoc         = cardValue();
        lblPhatTreTT       = cardValue();
        lblDelta           = cardValue(); lblDelta.setFont(FONT_BIG);
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
        JLabel lblDTNote = new JLabel("= Tiền thuê sau giảm điểm + Tiền phát sinh (gia hạn + phạt trễ đã đóng)");
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

        JPanel row1 = new JPanel(new GridLayout(1, 5, 8, 0));
        row1.setBackground(BG_DARK);
        row1.add(cardThueBanDau);
        row1.add(summaryCard("Giảm từ điểm tích lũy",  lblGiamDiem,        false, COLOR_DIEM));
        row1.add(summaryCard("Tiền thuê sau giảm điểm", lblTienThueNetDiem, false, COLOR_NET));
        row1.add(summaryCard("Tiền gia hạn (đã trả)",   lblGiaHan,          false, COLOR_EXTEND));
        row1.add(summaryCard("Tiền phạt đã đóng",       lblTienPhatDB,      false, COLOR_WARN));
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        row1.setPreferredSize(new Dimension(0, 82));

        JPanel sectionLabelB = sectionLabel("QUYẾT TOÁN CỌC");
        JPanel rowB = new JPanel(new GridLayout(1, 3, 8, 0));
        rowB.setBackground(BG_DARK);
        rowB.add(summaryCard("Tiền cọc ban đầu",   lblTienCoc,   false, PURPLE_HEADER));
        rowB.add(summaryCard("Phạt trễ tạm tính",  lblPhatTreTT, false, COLOR_WARN));
        rowB.add(summaryCard("Kết quả quyết toán", lblDelta,     true,  new Color(155, 135, 245)));
        rowB.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        rowB.setPreferredSize(new Dimension(0, 82));

        JPanel noteRow = new JPanel(new BorderLayout());
        noteRow.setBackground(BG_DARK);
        noteRow.setBorder(new EmptyBorder(4, 2, 0, 0));
        JPanel noteInner = new JPanel(new GridLayout(2, 1, 0, 1));
        noteInner.setBackground(BG_DARK);
        noteInner.add(lblCongThuc);
        JLabel noteStatic = new JLabel(
            "* Phạt trễ tạm tính: ước tính chưa chốt — chỉ chốt khi trả CD.  "
          + "| Giảm điểm: từ DIEM_LICHSU (Loai='TRU', MaPT).  "
          + "| 1 điểm = " + String.format("%,d", DIEM_TO_VND) + " VNĐ.");
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

    /* ==================== LOAD DATA (MVC) ==================== */
    /**
     * MVC: toàn bộ SQL + logic tính toán đã được chuyển vào
     * RentalService.loadRentDetail() và gọi qua RentController.
     * Dialog chỉ nhận RentDetailData và hiển thị.
     */
    private void loadData() {
        RentDetailData data = ctrl.loadRentDetail(maPT);
        if (data == null) {
            JOptionPane.showMessageDialog(this,
                "Phiếu thuê PT" + maPT + " không tìm thấy.",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // ── Thông tin KH / trạng thái ──
        lblTenKH.setText(data.tenKH);
        lblSDT.setText(data.sdt);
        lblTrangThai.setText(data.trangThai);
        lblTrangThai.setForeground(data.daTra ? COLOR_BADGE_DONE : COLOR_BADGE_ACTIVE);
        lblThueBanDauTitle.setText(data.daTra
            ? "Tiền thuê ban đầu (đã trả)"
            : "Tiền thuê ban đầu (chưa trả)");

        // ── Ngày tháng ──
        lblNgayThue.setText(data.ngayThue != null
            ? data.ngayThue.toLocalDateTime().format(FMT) : "—");
        lblNgayTraDK.setText(data.ngayTraDK != null
            ? data.ngayTraDK.toLocalDateTime().format(FMT) : "—");
        lblNgayTraThucTe.setText(data.ngayTraTT != null
            ? data.ngayTraTT.toLocalDateTime().format(FMT) : "— (chưa trả)");

        // ── CD ──
        lblMaCD.setText("CD" + data.maCD);
        lblMaGame.setText("G" + data.maGame);
        lblTenGame.setText(data.tenGame);
        lblGiaThueNgayHT.setText(String.format("%,.0f VNĐ", data.giaThueNgayHT));

        // ── Tiền thuê ban đầu ──
        lblThueBanDau.setText(String.format("%,.0f VNĐ  (%d ngày)",
            data.tienThueBanDau, data.soNgayGoc));
        lblThueBanDau.setForeground(COLOR_BASE);

        // ── Giảm điểm ──
        if (data.giamDiem > 0) {
            int soDiem = (int) Math.round(data.giamDiem / DIEM_TO_VND);
            lblGiamDiem.setText(String.format("-%,.0f VNĐ  (%d điểm)", data.giamDiem, soDiem));
            lblGiamDiem.setForeground(COLOR_DIEM);
        } else {
            lblGiamDiem.setText("Không dùng điểm");
            lblGiamDiem.setForeground(TEXT_MUTED);
        }

        // ── Tiền thuê sau giảm ──
        lblTienThueNetDiem.setText(String.format("%,.0f VNĐ", data.tienThueNetDiem));
        lblTienThueNetDiem.setForeground(COLOR_NET);

        // ── Gia hạn & phạt đã đóng ──
        if (data.tienPhatDB > 0) {
            lblGiaHan.setText(String.format("%,.0f VNĐ", data.tienGiaHan));
            lblGiaHan.setForeground(COLOR_EXTEND);
            if (data.treHanDaDong > 0) {
                lblTienPhatDB.setText(String.format("%,.0f VNĐ", data.treHanDaDong));
                lblTienPhatDB.setForeground(COLOR_WARN);
            } else {
                lblTienPhatDB.setText("Không có!");
                lblTienPhatDB.setForeground(COLOR_OK);
            }
        } else {
            lblGiaHan.setText("Không có!");
            lblGiaHan.setForeground(COLOR_OK);
            lblTienPhatDB.setText("Không có!");
            lblTienPhatDB.setForeground(COLOR_OK);
        }

        // ── Doanh thu ──
        double doanhThu = data.tienThueNetDiem + data.tienPhatDB;
        lblDoanhThu.setText(String.format("%,.0f VNĐ", doanhThu));
        lblDoanhThu.setForeground(COLOR_REVENUE);

        // ── Cọc / Phạt trễ tạm tính ──
        lblTienCoc.setText(String.format("%,.0f VNĐ", data.tienCoc));
        lblTienCoc.setForeground(TEXT_WHITE);

        if (data.daTra) {
            lblPhatTreTT.setText("Đã chốt khi trả!");
            lblPhatTreTT.setForeground(TEXT_MUTED);
            lblPhatTreTT.setFont(FONT_SMALL);
        } else if (data.phatTreTamTinh > 0) {
            lblPhatTreTT.setText(String.format("~%,.0f VNĐ", data.phatTreTamTinh));
            lblPhatTreTT.setForeground(COLOR_PENALTY);
        } else {
            lblPhatTreTT.setText("Không có!");
            lblPhatTreTT.setForeground(COLOR_OK);
        }

        // ── Delta quyết toán ──
        double delta = data.tienCoc - data.tienThueNetDiem - data.phatTreTamTinh - data.treHanDaDong;

        // ── Công thức ──
        if (data.daTra) {
            if (data.giamDiem > 0) {
                lblCongThuc.setText(String.format(
                    "Quyết toán: %.0f (cọc) − (%.0f − %.0f giảm điểm) − %.0f (phạt đã đóng) = %.0f VNĐ",
                    data.tienCoc, data.tienThueBanDau, data.giamDiem, data.treHanDaDong, delta));
            } else {
                lblCongThuc.setText(String.format(
                    "Quyết toán: %.0f (cọc) − %.0f (thuê) − %.0f (phạt đã đóng) = %.0f VNĐ",
                    data.tienCoc, data.tienThueBanDau, data.treHanDaDong, delta));
            }
        } else {
            if (data.giamDiem > 0) {
                lblCongThuc.setText(String.format(
                    "Ước tính: %.0f (cọc) − (%.0f − %.0f giảm điểm) − %.0f (phạt trễ TT) − %.0f (phạt đã đóng) = %.0f VNĐ",
                    data.tienCoc, data.tienThueBanDau, data.giamDiem,
                    data.phatTreTamTinh, data.treHanDaDong, delta));
            } else {
                lblCongThuc.setText(String.format(
                    "Ước tính: %.0f (cọc) − %.0f (thuê) − %.0f (phạt trễ TT) − %.0f (phạt đã đóng) = %.0f VNĐ",
                    data.tienCoc, data.tienThueBanDau,
                    data.phatTreTamTinh, data.treHanDaDong, delta));
            }
        }

        // ── Hiển thị kết quả ──
        if (data.daTra) {
            if (delta > 0) {
                lblDelta.setText(String.format("Đã hoàn %,.0f VNĐ", delta));
                lblDelta.setForeground(COLOR_OK);
            } else if (delta == 0) {
                lblDelta.setText("Không hoàn cọc");
                lblDelta.setForeground(TEXT_MUTED);
            } else {
                lblDelta.setText(String.format("Đã thu thêm %,.0f VNĐ", Math.abs(delta)));
                lblDelta.setForeground(COLOR_PENALTY);
            }
        } else {
            if (delta > 0) {
                lblDelta.setText(String.format("Dự kiến hoàn %,.0f VNĐ", delta));
                lblDelta.setForeground(COLOR_OK);
            } else if (delta == 0) {
                lblDelta.setText("Dự kiến hòa");
                lblDelta.setForeground(TEXT_MUTED);
            } else {
                lblDelta.setText(String.format("Dự kiến thu %,.0f VNĐ", Math.abs(delta)));
                lblDelta.setForeground(COLOR_WARN);
            }
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