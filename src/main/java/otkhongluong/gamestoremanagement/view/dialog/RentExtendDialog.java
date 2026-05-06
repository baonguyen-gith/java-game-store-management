package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.model.PhieuThue;
import otkhongluong.gamestoremanagement.service.ThueService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RentExtendDialog — Gia hạn phiếu thuê (wizard 3 bước).
 *
 * Logic tiền khi gia hạn:
 *   - phatTre   = số ngày trễ × 10.000 đ  (0 nếu chưa trễ)
 *   - phiGiaHan = tổng đơn giá thuê các CD × số ngày gia hạn thêm
 *   - tongThuKhach = phatTre + phiGiaHan  → thu tiền mặt tại quầy
 *
 * Cập nhật DB sau khi xác nhận:
 *   - NgayTraDuKien += soNgay   ← CÓ cập nhật
 *   - TienPhat, TienCoc         ← KHÔNG thay đổi (thu tiền mặt ngoài hệ thống)
 */
public class RentExtendDialog extends JDialog {

    /* ===== MÀU SẮC ===== */
    private static final Color BG_MAIN       = new Color(24, 14, 60);
    private static final Color BG_CARD       = new Color(38, 24, 90);
    private static final Color BG_INPUT      = new Color(255, 255, 255);
    private static final Color ACCENT        = new Color(139, 92, 246);
    private static final Color ACCENT_LIGHT  = new Color(196, 181, 253);
    private static final Color ACCENT_DARK   = new Color(91, 33, 182);
    private static final Color SUCCESS       = new Color(52, 211, 153);
    private static final Color DANGER        = new Color(248, 113, 113);
    private static final Color WARNING       = new Color(251, 191, 36);
    private static final Color TEXT_PRIMARY  = Color.WHITE;
    private static final Color TEXT_SECONDARY= new Color(167, 139, 250);
    private static final Color TEXT_MUTED    = new Color(148, 163, 184);
    private static final Color TEXT_DARK     = new Color(30, 27, 75);
    private static final Color BORDER_COLOR  = new Color(109, 40, 217);
    private static final Color TBL_ROW_ODD   = new Color(245, 243, 255);
    private static final Color TBL_ROW_EVEN  = Color.WHITE;
    private static final Color TBL_HEADER    = new Color(139, 92, 246);

    /* ===== FONT ===== */
    private static final Font F_TITLE   = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font F_HEADER  = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font F_BODY    = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font F_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font F_BIG     = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font F_CAPTION = new Font("Segoe UI", Font.ITALIC, 11);

    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /* ===== TRẠNG THÁI ===== */
    private final ThueService service = new ThueService();
    private int currentStep = 1;

    // Bước 1
    private JTextField txtSDT;
    private JTable tblPhieu;
    private DefaultTableModel tblModel;
    private List<PhieuThue> searchResults;

    // Bước 2
    private PhieuThue selectedPhieu;
    private JLabel lblInfoNgayThue, lblInfoNgayDK, lblInfoTrangThai;
    private JSpinner spinnerSoNgay;
    private JLabel lblNgayTraMoi;
    private JLabel lblPhatTre, lblPhiGiaHan, lblTongThu;
    private JPanel pnlCanhBaoTre;

    // Bước 3
    private JLabel lblSum_PT, lblSum_NgayCu, lblSum_NgayMoi;
    private JLabel lblSum_PhatTre, lblSum_PhiGiaHan, lblSum_TongThu;
    private JLabel lblSum_TienCoc;

    /* ===== LAYOUT ===== */
    private JPanel contentPanel;
    private JPanel stepIndicator;
    private JButton btnBack, btnNext, btnConfirm;

    // ─────────────────────────────────────────────────
    public RentExtendDialog(Frame parent, int maPT) {
        super(parent, "Gia hạn thuê CD / Game", true);
        setSize(680, 560);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setBackground(BG_MAIN);
        getRootPane().setBorder(BorderFactory.createLineBorder(ACCENT, 2));

        add(xayDungHeader(), BorderLayout.NORTH);

        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(BG_MAIN);
        contentPanel.add(xayDungBuoc1(), "step1");
        contentPanel.add(xayDungBuoc2(), "step2");
        contentPanel.add(xayDungBuoc3(), "step3");
        add(contentPanel, BorderLayout.CENTER);
        add(xayDungFooter(), BorderLayout.SOUTH);

        hienThiBuoc(1);
        if (maPT > 0) tienDienMaPT(maPT);
    }

    /* ══════════════════ PRE-FILL ══════════════════ */
    private void tienDienMaPT(int maPT) {
        PhieuThue pt = service.getById(maPT);
        if (pt == null || pt.getSoDienThoai() == null) return;
        txtSDT.setText(pt.getSoDienThoai());
        thucHienTimKiem();
        if (searchResults == null) return;
        for (int i = 0; i < searchResults.size(); i++) {
            if (searchResults.get(i).getMaPT() == maPT) {
                tblPhieu.setRowSelectionInterval(i, i);
                break;
            }
        }
    }

    /* ══════════════════ HEADER ══════════════════ */
    private JPanel xayDungHeader() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(BG_MAIN);
        p.setBorder(new EmptyBorder(16, 22, 12, 22));

        JLabel title = new JLabel("⏰  Gia Hạn Thuê CD / Game");
        title.setFont(F_TITLE);
        title.setForeground(TEXT_PRIMARY);

        stepIndicator = xayDungStepIndicator();
        p.add(title, BorderLayout.WEST);
        p.add(stepIndicator, BorderLayout.EAST);

        // Đường kẻ dưới header
        JPanel sep = new JPanel();
        sep.setBackground(BORDER_COLOR);
        sep.setPreferredSize(new Dimension(0, 1));
        p.add(sep, BorderLayout.SOUTH);
        return p;
    }

    private JPanel xayDungStepIndicator() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        p.setBackground(BG_MAIN);
        String[] labels = {"1. Tìm kiếm", "2. Gia hạn", "3. Xác nhận"};
        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel(labels[i]);
            lbl.setName("step_" + (i + 1));
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setForeground(TEXT_MUTED);
            lbl.setBorder(new EmptyBorder(3, 9, 3, 9));
            p.add(lbl);
            if (i < labels.length - 1) {
                JLabel arr = new JLabel("›");
                arr.setForeground(TEXT_MUTED);
                p.add(arr);
            }
        }
        return p;
    }

    private void capNhatStepIndicator(int active) {
        for (Component c : stepIndicator.getComponents()) {

            if (c instanceof JLabel) {

                JLabel lbl = (JLabel) c;

                if (lbl.getName() != null && lbl.getName().startsWith("step_")) {

                    int s = Integer.parseInt(lbl.getName().split("_")[1]);

                    lbl.setForeground(
                        s == active ? ACCENT_LIGHT :
                        (s < active ? SUCCESS : TEXT_MUTED)
                    );
                }
            }
        }
        stepIndicator.repaint();
    }

    /* ══════════════════ BƯỚC 1 — TÌM KIẾM ══════════════════ */
    private JPanel xayDungBuoc1() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(BG_MAIN);
        p.setBorder(new EmptyBorder(14, 22, 4, 22));

        // ── Thanh tìm kiếm ──
        JLabel lblSDT = new JLabel("Số điện thoại khách hàng:");
        lblSDT.setFont(F_HEADER);
        lblSDT.setForeground(TEXT_SECONDARY);
        lblSDT.setBorder(new EmptyBorder(0, 0, 4, 0));

        txtSDT = new JTextField();
        taoStyleInput(txtSDT);
        txtSDT.setPreferredSize(new Dimension(0, 38));
        txtSDT.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) thucHienTimKiem();
            }
        });

        RoundBtn btnSearch = new RoundBtn("🔍  Tìm", ACCENT, TEXT_PRIMARY);
        btnSearch.setPreferredSize(new Dimension(110, 38));
        btnSearch.addActionListener(e -> thucHienTimKiem());

        JPanel rowInput = new JPanel(new BorderLayout(8, 0));
        rowInput.setBackground(BG_MAIN);
        rowInput.add(txtSDT, BorderLayout.CENTER);
        rowInput.add(btnSearch, BorderLayout.EAST);

        JPanel topBlock = new JPanel(new BorderLayout(0, 4));
        topBlock.setBackground(BG_MAIN);
        topBlock.add(lblSDT, BorderLayout.NORTH);
        topBlock.add(rowInput, BorderLayout.CENTER);
        p.add(topBlock, BorderLayout.NORTH);

        // ── Bảng kết quả ──
        String[] cols = {"Mã PT", "Ngày thuê", "Ngày trả dự kiến", "Trạng thái"};
        tblModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblPhieu = taoStyledTable(tblModel);
        tblPhieu.getColumnModel().getColumn(0).setPreferredWidth(65);
        tblPhieu.getColumnModel().getColumn(1).setPreferredWidth(130);
        tblPhieu.getColumnModel().getColumn(2).setPreferredWidth(140);
        tblPhieu.getColumnModel().getColumn(3).setPreferredWidth(100);
        tblPhieu.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblPhieu.getSelectedRow() >= 0)
                    sangBuoc2();
            }
        });
        p.add(taoStyledScroll(tblPhieu), BorderLayout.CENTER);

        JLabel hint = new JLabel("* Double-click hoặc chọn phiếu rồi nhấn \"Tiếp theo\"");
        hint.setFont(F_CAPTION);
        hint.setForeground(TEXT_MUTED);
        hint.setBorder(new EmptyBorder(4, 0, 0, 0));
        p.add(hint, BorderLayout.SOUTH);
        return p;
    }

    private void thucHienTimKiem() {
        String sdt = txtSDT.getText().trim();
        if (sdt.isEmpty()) { hienThongBao("Vui lòng nhập số điện thoại!"); return; }

        searchResults = service.getAll().stream()
            .filter(pt -> pt.getSoDienThoai() != null
                       && pt.getSoDienThoai().contains(sdt)
                       && "DangThue".equalsIgnoreCase(pt.getTrangThai()))
            .collect(Collectors.toList());

        tblModel.setRowCount(0);
        if (searchResults.isEmpty()) {
            hienThongBao("Không tìm thấy phiếu thuê đang hoạt động với SDT: " + sdt);
            return;
        }
        for (PhieuThue pt : searchResults) {
            tblModel.addRow(new Object[]{
                "PT" + pt.getMaPT(),
                pt.getNgayThue()      != null ? pt.getNgayThue().format(FMT_DATE)      : "—",
                pt.getNgayTraDuKien() != null ? pt.getNgayTraDuKien().format(FMT_DATE) : "—",
                pt.getTrangThai()
            });
        }
        if (searchResults.size() == 1) tblPhieu.setRowSelectionInterval(0, 0);
    }

    /* ══════════════════ BƯỚC 2 — NHẬP SỐ NGÀY ══════════════════ */
    private JPanel xayDungBuoc2() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(BG_MAIN);
        p.setBorder(new EmptyBorder(14, 22, 4, 22));

        // ── 3 thẻ thông tin phiếu ──
        JPanel rowCards = new JPanel(new GridLayout(1, 3, 10, 0));
        rowCards.setBackground(BG_MAIN);
        rowCards.setPreferredSize(new Dimension(0, 72));
        lblInfoNgayThue  = new JLabel("—");
        lblInfoNgayDK    = new JLabel("—");
        lblInfoTrangThai = new JLabel("—");
        rowCards.add(taoInfoCard("Ngày thuê",           lblInfoNgayThue));
        rowCards.add(taoInfoCard("Ngày trả dự kiến (cũ)", lblInfoNgayDK));
        rowCards.add(taoInfoCard("Trạng thái",           lblInfoTrangThai));
        p.add(rowCards, BorderLayout.NORTH);

        JPanel centerBlock = new JPanel(new BorderLayout(0, 10));
        centerBlock.setBackground(BG_MAIN);

        // ── Cảnh báo quá hạn ──
        pnlCanhBaoTre = taoCanhBaoBox(
            "⚠  Phiếu đang quá hạn!",
            "Phí phạt trễ sẽ được tính và thu ngay khi gia hạn.",
            new Color(69, 26, 3), WARNING, new Color(253, 230, 138)
        );
        pnlCanhBaoTre.setVisible(false);
        centerBlock.add(pnlCanhBaoTre, BorderLayout.NORTH);

        // ── Input số ngày + hiển thị ngày mới ──
        JPanel pnlInput = new JPanel(new BorderLayout(20, 0));
        pnlInput.setBackground(BG_CARD);
        pnlInput.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(14, 18, 14, 18)
        ));

        // Cột trái: spinner
        JPanel colLeft = new JPanel(new BorderLayout(0, 6));
        colLeft.setBackground(BG_CARD);
        JLabel lblNgayLbl = new JLabel("Số ngày gia hạn thêm:");
        lblNgayLbl.setFont(F_HEADER);
        lblNgayLbl.setForeground(TEXT_SECONDARY);

        SpinnerNumberModel spinModel = new SpinnerNumberModel(1, 1, 90, 1);
        spinnerSoNgay = new JSpinner(spinModel);
        spinnerSoNgay.setFont(new Font("Segoe UI", Font.BOLD, 14));
        spinnerSoNgay.setEditor(new JSpinner.NumberEditor(spinnerSoNgay, "#"));
        spinnerSoNgay.setBorder(new CompoundBorder(
            new LineBorder(ACCENT, 1, true),
            new EmptyBorder(4, 8, 4, 8)
        ));
        spinnerSoNgay.setPreferredSize(new Dimension(0, 38));
        colLeft.add(lblNgayLbl,   BorderLayout.NORTH);
        colLeft.add(spinnerSoNgay, BorderLayout.CENTER);

        // Cột phải: ngày trả mới
        JPanel colRight = new JPanel(new BorderLayout(0, 6));
        colRight.setBackground(BG_CARD);
        JLabel lblNgayMoiLabel = new JLabel("Ngày trả dự kiến mới:");
        lblNgayMoiLabel.setFont(F_HEADER);
        lblNgayMoiLabel.setForeground(TEXT_SECONDARY);
        lblNgayTraMoi = new JLabel("—");
        lblNgayTraMoi.setFont(F_BIG);
        lblNgayTraMoi.setForeground(SUCCESS);
        colRight.add(lblNgayMoiLabel, BorderLayout.NORTH);
        colRight.add(lblNgayTraMoi,   BorderLayout.CENTER);

        pnlInput.add(colLeft,  BorderLayout.CENTER);
        pnlInput.add(colRight, BorderLayout.EAST);

        // ── Bảng phí ──
        JPanel pnlPhi = new JPanel(new GridLayout(3, 2, 0, 0));
        pnlPhi.setBackground(BG_CARD);
        pnlPhi.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(0, 0, 0, 0)
        ));

        lblPhatTre  = new JLabel("0 VNĐ");
        lblPhiGiaHan = new JLabel("0 VNĐ");
        lblTongThu   = new JLabel("0 VNĐ");

        pnlPhi.add(taoPhiRow("Phạt trễ (nếu có):",     false));
        pnlPhi.add(taoPhiValue(lblPhatTre,  false));
        pnlPhi.add(taoPhiRow("Phí gia hạn thêm:",       false));
        pnlPhi.add(taoPhiValue(lblPhiGiaHan, false));
        pnlPhi.add(taoPhiRow("TỔNG THU TỪ KHÁCH:",      true));
        pnlPhi.add(taoPhiValue(lblTongThu,  true));

        lblPhatTre.setFont(F_HEADER);   lblPhatTre.setForeground(TEXT_PRIMARY);
        lblPhiGiaHan.setFont(F_HEADER); lblPhiGiaHan.setForeground(TEXT_PRIMARY);
        lblTongThu.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTongThu.setForeground(SUCCESS);

        JLabel noteCoc = new JLabel("* Tiền cọc KHÔNG thay đổi — chỉ quyết toán khi khách trả CD");
        noteCoc.setFont(F_CAPTION);
        noteCoc.setForeground(TEXT_MUTED);
        noteCoc.setBorder(new EmptyBorder(5, 0, 0, 0));

        JPanel centerSub = new JPanel(new BorderLayout(0, 8));
        centerSub.setBackground(BG_MAIN);
        centerSub.add(pnlInput, BorderLayout.NORTH);
        centerSub.add(pnlPhi,   BorderLayout.CENTER);
        centerSub.add(noteCoc,  BorderLayout.SOUTH);
        centerBlock.add(centerSub, BorderLayout.CENTER);

        p.add(centerBlock, BorderLayout.CENTER);
        spinnerSoNgay.addChangeListener(e -> tinhLaiBuoc2());
        return p;
    }

    /* Helper: tạo ô nhãn trong bảng phí */
    private JPanel taoPhiRow(String text, boolean isTotal) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(isTotal ? new Color(88, 28, 135) : BG_CARD);
        cell.setBorder(new EmptyBorder(10, 16, 10, 8));
        JLabel lbl = new JLabel(text);
        lbl.setFont(isTotal ? F_HEADER : F_BODY);
        lbl.setForeground(isTotal ? ACCENT_LIGHT : TEXT_MUTED);
        cell.add(lbl, BorderLayout.CENTER);
        return cell;
    }

    private JPanel taoPhiValue(JLabel valueLbl, boolean isTotal) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(isTotal ? new Color(88, 28, 135) : BG_CARD);
        cell.setBorder(new EmptyBorder(10, 8, 10, 16));
        cell.add(valueLbl, BorderLayout.EAST);
        return cell;
    }

    private void tinhLaiBuoc2() {
        if (selectedPhieu == null || selectedPhieu.getNgayTraDuKien() == null) return;
        int soNgay = (int) spinnerSoNgay.getValue();

        LocalDateTime ngayMoi = selectedPhieu.getNgayTraDuKien().plusDays(soNgay);
        lblNgayTraMoi.setText(ngayMoi.format(FMT_DATE));

        double phatTre   = tinhPhatTreHienTai(selectedPhieu);
        double phiGiaHan = tinhPhiGiaHan(selectedPhieu, soNgay);
        double tongThu   = phatTre + phiGiaHan;

        if (phatTre > 0) {
            lblPhatTre.setText(String.format("%,.0f VNĐ  ⚠", phatTre));
            lblPhatTre.setForeground(DANGER);
        } else {
            lblPhatTre.setText("Không có  ✓");
            lblPhatTre.setForeground(SUCCESS);
        }
        lblPhiGiaHan.setText(String.format("%,.0f VNĐ", phiGiaHan));
        lblTongThu.setText(String.format("%,.0f VNĐ", tongThu));
        contentPanel.repaint();
    }

    // ✅ SỬA THÀNH
    private double tinhPhiGiaHan(PhieuThue pt, int soNgay) {
        if (pt.getDanhSachChiTiet() == null) return 0;
        double tong = pt.getDanhSachChiTiet().stream()
            .mapToDouble(PhieuThue.CTPhieuThue::getGiaThueNgay)  // ← lấy từ SANPHAM
            .sum();
        return tong * soNgay;
    }

    /** Phạt trễ = số ngày quá hạn × 10.000 đ */
    private double tinhPhatTreHienTai(PhieuThue pt) {
        if (pt == null || pt.getNgayTraDuKien() == null) return 0;
        LocalDateTime now = LocalDate.now().atStartOfDay();
        if (!now.isAfter(pt.getNgayTraDuKien())) return 0;
        long days = java.time.temporal.ChronoUnit.DAYS.between(
            pt.getNgayTraDuKien().toLocalDate(), now.toLocalDate());
        return Math.max(days, 1) * 10_000;
    }

    private void nnapDuLieuBuoc2() {
        if (selectedPhieu == null) return;
        PhieuThue full = service.getById(selectedPhieu.getMaPT());
        if (full == null) { hienThongBao("Không tải được thông tin phiếu thuê!"); return; }
        selectedPhieu = full;

        lblInfoNgayThue.setText(selectedPhieu.getNgayThue() != null
            ? selectedPhieu.getNgayThue().format(FMT_DATE) : "—");
        lblInfoNgayDK.setText(selectedPhieu.getNgayTraDuKien() != null
            ? selectedPhieu.getNgayTraDuKien().format(FMT_DATE) : "—");
        lblInfoTrangThai.setText(nvl(selectedPhieu.getTrangThai(), "—"));

        pnlCanhBaoTre.setVisible(tinhPhatTreHienTai(selectedPhieu) > 0);
        spinnerSoNgay.setValue(1);
        tinhLaiBuoc2();
    }

    /* ══════════════════ BƯỚC 3 — XÁC NHẬN ══════════════════ */
    private JPanel xayDungBuoc3() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(BG_MAIN);
        p.setBorder(new EmptyBorder(16, 28, 10, 28));

        lblSum_PT        = new JLabel("—");
        lblSum_NgayCu    = new JLabel("—");
        lblSum_NgayMoi   = new JLabel("—");
        lblSum_PhatTre   = new JLabel("—");
        lblSum_PhiGiaHan = new JLabel("—");
        lblSum_TongThu   = new JLabel("—");
        lblSum_TienCoc   = new JLabel("—");

        // Thẻ tóm tắt
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(16, 22, 16, 22)
        ));

        card.add(taoSummaryRow("Phiếu thuê:",          lblSum_PT,      false));
        card.add(Box.createVerticalStrut(8));
        card.add(taoSummaryRow("Ngày trả cũ:",         lblSum_NgayCu,  false));
        card.add(Box.createVerticalStrut(4));
        card.add(taoSummaryRow("Ngày trả mới:",        lblSum_NgayMoi, false));
        card.add(Box.createVerticalStrut(12));
        card.add(taoDuongKe());
        card.add(Box.createVerticalStrut(12));
        card.add(taoSummaryRow("Phạt trễ (thu ngay):",     lblSum_PhatTre,   false));
        card.add(Box.createVerticalStrut(4));
        card.add(taoSummaryRow("Phí gia hạn (thu ngay):",  lblSum_PhiGiaHan, false));
        card.add(Box.createVerticalStrut(12));
        card.add(taoDuongKe());
        card.add(Box.createVerticalStrut(12));
        card.add(taoSummaryRow("TỔNG THU TỪ KHÁCH:",   lblSum_TongThu,  true));
        card.add(Box.createVerticalStrut(12));
        card.add(taoDuongKe());
        card.add(Box.createVerticalStrut(12));
        card.add(taoSummaryRow("Tiền cọc (giữ nguyên):", lblSum_TienCoc, false));

        // Ghi chú cập nhật DB
        JLabel note = new JLabel(
            "<html><center>"
          + "DB sẽ cập nhật: <b>NgayTraDuKien += số ngày</b>"
          + "&nbsp; | &nbsp;<b>TienPhat, TienCoc: KHÔNG đổi</b>"
          + "</center></html>"
        );
        note.setFont(F_SMALL);
        note.setForeground(TEXT_MUTED);
        note.setHorizontalAlignment(SwingConstants.CENTER);
        note.setBackground(new Color(38, 24, 90));
        note.setOpaque(true);
        note.setBorder(new CompoundBorder(
            new LineBorder(new Color(109, 40, 217), 1, true),
            new EmptyBorder(8, 14, 8, 14)
        ));

        p.add(card, BorderLayout.CENTER);
        p.add(note, BorderLayout.SOUTH);
        return p;
    }

    private JPanel taoSummaryRow(String label, JLabel valueLabel, boolean isTotal) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BG_CARD);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel lbl = new JLabel(label);
        lbl.setFont(isTotal ? F_HEADER : F_BODY);
        lbl.setForeground(isTotal ? ACCENT_LIGHT : TEXT_MUTED);

        valueLabel.setFont(isTotal
            ? new Font("Segoe UI", Font.BOLD, 16)
            : F_HEADER);
        valueLabel.setForeground(isTotal ? SUCCESS : TEXT_PRIMARY);
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(lbl,        BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    private JPanel taoDuongKe() {
        JPanel sep = new JPanel();
        sep.setBackground(BORDER_COLOR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setPreferredSize(new Dimension(0, 1));
        return sep;
    }

    private void capNhatTomTatBuoc3() {
        if (selectedPhieu == null) return;
        int    soNgay    = (int) spinnerSoNgay.getValue();
        double phatTre   = tinhPhatTreHienTai(selectedPhieu);
        double phiGiaHan = tinhPhiGiaHan(selectedPhieu, soNgay);
        double tongThu   = phatTre + phiGiaHan;
        double tienCoc   = selectedPhieu.getTienCoc();

        LocalDateTime ngayMoi = selectedPhieu.getNgayTraDuKien().plusDays(soNgay);

        lblSum_PT.setText("PT" + selectedPhieu.getMaPT()
            + " — " + nvl(selectedPhieu.getTenKhachHang(), ""));

        lblSum_NgayCu.setText(selectedPhieu.getNgayTraDuKien().format(FMT_DATE));
        lblSum_NgayCu.setForeground(TEXT_PRIMARY);

        lblSum_NgayMoi.setText(ngayMoi.format(FMT_DATE) + "  (+" + soNgay + " ngày)");
        lblSum_NgayMoi.setForeground(SUCCESS);

        if (phatTre > 0) {
            lblSum_PhatTre.setText(String.format("%,.0f VNĐ  ⚠", phatTre));
            lblSum_PhatTre.setForeground(DANGER);
        } else {
            lblSum_PhatTre.setText("Không có  ✓");
            lblSum_PhatTre.setForeground(SUCCESS);
        }

        lblSum_PhiGiaHan.setText(String.format("%,.0f VNĐ", phiGiaHan));
        lblSum_PhiGiaHan.setForeground(TEXT_PRIMARY);

        lblSum_TongThu.setText(String.format("%,.0f VNĐ", tongThu));

        lblSum_TienCoc.setText(String.format("%,.0f VNĐ  (không thay đổi)", tienCoc));
        lblSum_TienCoc.setForeground(TEXT_MUTED);
        lblSum_TienCoc.setFont(F_BODY);

        if (btnConfirm != null) btnConfirm.setEnabled(true);
    }

    /* ══════════════════ FOOTER ══════════════════ */
    private JPanel xayDungFooter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_MAIN);
        p.setBorder(new EmptyBorder(10, 22, 14, 22));

        JPanel sep = new JPanel();
        sep.setBackground(BORDER_COLOR);
        sep.setPreferredSize(new Dimension(0, 1));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnRow.setBackground(BG_MAIN);

        btnBack = new RoundBtn("← Quay lại", new Color(71, 85, 105), TEXT_PRIMARY);
        btnBack.setPreferredSize(new Dimension(120, 38));
        btnBack.setVisible(false);
        btnBack.addActionListener(e -> { if (currentStep > 1) hienThiBuoc(currentStep - 1); });

        btnNext = new RoundBtn("Tiếp theo →", ACCENT, TEXT_PRIMARY);
        btnNext.setPreferredSize(new Dimension(130, 38));
        btnNext.addActionListener(e -> {
            if (currentStep == 1) sangBuoc2();
            else if (currentStep == 2) sangBuoc3();
        });

        btnConfirm = new RoundBtn("✔  Xác nhận gia hạn", SUCCESS, TEXT_DARK);
        btnConfirm.setPreferredSize(new Dimension(175, 38));
        btnConfirm.setVisible(false);
        btnConfirm.addActionListener(e -> xacNhanGiaHan());

        RoundBtn btnCancel = new RoundBtn("✕  Hủy", DANGER, TEXT_PRIMARY);
        btnCancel.setPreferredSize(new Dimension(90, 38));
        btnCancel.addActionListener(e -> dispose());

        btnRow.add(btnCancel);
        btnRow.add(btnBack);
        btnRow.add(btnNext);
        btnRow.add(btnConfirm);

        p.add(sep,    BorderLayout.NORTH);
        p.add(btnRow, BorderLayout.CENTER);
        return p;
    }

    /* ══════════════════ ĐIỀU HƯỚNG ══════════════════ */
    private void hienThiBuoc(int step) {
        currentStep = step;
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, "step" + step);
        btnBack.setVisible(step > 1);
        btnNext.setVisible(step < 3);
        btnConfirm.setVisible(step == 3);
        capNhatStepIndicator(step);
    }

    private void sangBuoc2() {
        int row = tblPhieu.getSelectedRow();
        if (row < 0) { hienThongBao("Vui lòng chọn một phiếu thuê!"); return; }
        if (searchResults == null || row >= searchResults.size()) return;
        selectedPhieu = searchResults.get(row);
        nnapDuLieuBuoc2();
        hienThiBuoc(2);
    }

    private void sangBuoc3() {
        capNhatTomTatBuoc3();
        hienThiBuoc(3);
    }

    /* ══════════════════ XÁC NHẬN GIA HẠN ══════════════════ */
    /**
     * Xác nhận gia hạn:
     *   - Thu tiền mặt từ khách: tongThu = phatTre + phiGiaHan
     *   - Cập nhật DB:
     *       NgayTraDuKien += soNgay   ← CÓ cập nhật
     *       TienPhat, TienCoc         ← KHÔNG thay đổi
     */
    private void xacNhanGiaHan() {
        if (selectedPhieu == null) return;
        int    soNgay    = (int) spinnerSoNgay.getValue();
        double phatTre   = tinhPhatTreHienTai(selectedPhieu);
        double phiGiaHan = tinhPhiGiaHan(selectedPhieu, soNgay);
        double tongThu   = phatTre + phiGiaHan;
        LocalDateTime ngayMoi = selectedPhieu.getNgayTraDuKien().plusDays(soNgay);

        String msg = String.format(
            "Xác nhận gia hạn phiếu PT%d?\n\n"
          + "  Ngày trả mới           : %s  (+%d ngày)\n"
          + "  Phạt trễ (thu ngay)    : %,.0f VNĐ\n"
          + "  Phí gia hạn (thu ngay) : %,.0f VNĐ\n"
          + "  ──────────────────────────────────\n"
          + "  TỔNG THU TỪ KHÁCH     : %,.0f VNĐ\n\n"
          + "  Tiền cọc               : %,.0f VNĐ  [KHÔNG ĐỔI]\n"
          + "  (Quyết toán cọc khi khách trả CD)",
            selectedPhieu.getMaPT(),
            ngayMoi.format(FMT_DATE), soNgay,
            phatTre, phiGiaHan, tongThu,
            selectedPhieu.getTienCoc()
        );

        int xacNhan = JOptionPane.showConfirmDialog(this, msg,
            "Xác nhận gia hạn", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (xacNhan != JOptionPane.YES_OPTION) return;

        /*
         * service.extendRental cập nhật DB:
         *   SET NgayTraDuKien = NgayTraDuKien + soNgay DAYS
         *   -- TienPhat, TienCoc: KHÔNG cập nhật
         *   WHERE MaPT = ?
         *
         * Tham số phatTre và phiGiaHan được truyền vào chỉ để
         * service có thể ghi log / lịch sử nếu cần,
         * KHÔNG dùng để thay đổi TienPhat hay TienCoc.
         */
        boolean ok = service.extendRental(
            selectedPhieu.getMaPT(),
            soNgay,
            phatTre,
            phiGiaHan
        );

        if (ok) {
            JOptionPane.showMessageDialog(this,
                String.format(
                    "Gia hạn thành công!\n\n"
                  + "  Ngày trả mới      : %s\n"
                  + "  Đã thu từ khách   : %,.0f VNĐ\n"
                  + "    • Phạt trễ      : %,.0f VNĐ\n"
                  + "    • Phí gia hạn   : %,.0f VNĐ\n\n"
                  + "  Tiền cọc          : %,.0f VNĐ  (giữ nguyên)",
                    ngayMoi.format(FMT_DATE),
                    tongThu, phatTre, phiGiaHan,
                    selectedPhieu.getTienCoc()),
                "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            hienThongBao("Gia hạn thất bại! Vui lòng thử lại.");
        }
    }

    /* ══════════════════ HELPERS UI ══════════════════ */

    private JPanel taoCanhBaoBox(String tieuDe, String noiDung,
                                  Color bg, Color mauTieuDe, Color mauNoiDung) {
        JPanel box = new JPanel(new BorderLayout(10, 0));
        box.setBackground(bg);
        box.setBorder(new CompoundBorder(
            new LineBorder(mauTieuDe, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        JLabel icon = new JLabel("⚠");
        icon.setFont(new Font("Segoe UI", Font.BOLD, 18));
        icon.setForeground(mauTieuDe);

        JPanel txtPnl = new JPanel(new GridLayout(2, 1, 0, 2));
        txtPnl.setBackground(bg);
        JLabel t = new JLabel(tieuDe);   t.setFont(F_HEADER); t.setForeground(mauTieuDe);
        JLabel m = new JLabel(noiDung);  m.setFont(F_SMALL);  m.setForeground(mauNoiDung);
        txtPnl.add(t); txtPnl.add(m);

        box.add(icon,   BorderLayout.WEST);
        box.add(txtPnl, BorderLayout.CENTER);
        return box;
    }

    private JPanel taoInfoCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(F_SMALL);
        titleLbl.setForeground(TEXT_MUTED);
        valueLabel.setFont(F_HEADER);
        valueLabel.setForeground(TEXT_PRIMARY);
        card.add(titleLbl,   BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void taoStyleInput(JTextField tf) {
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_DARK);
        tf.setCaretColor(new Color(109, 40, 217));
        tf.setFont(F_BODY);
        tf.setBorder(new CompoundBorder(
            new LineBorder(ACCENT, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
    }

    private JTable taoStyledTable(DefaultTableModel model) {
        JTable t = new JTable(model) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? TBL_ROW_ODD : TBL_ROW_EVEN);
                    c.setForeground(TEXT_DARK);
                } else {
                    c.setBackground(ACCENT);
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        };
        t.setFont(F_BODY);
        t.setRowHeight(34);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(ACCENT);
        t.setSelectionForeground(Color.WHITE);
        t.setBackground(TBL_ROW_EVEN);

        JTableHeader header = t.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable tbl, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = new JLabel(v == null ? "" : v.toString());
                lbl.setFont(F_HEADER);
                lbl.setForeground(Color.WHITE);
                lbl.setBackground(TBL_HEADER);
                lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(7, 12, 7, 12));
                return lbl;
            }
        });
        header.setBackground(TBL_HEADER);
        header.setPreferredSize(new Dimension(0, 36));
        return t;
    }

    private JScrollPane taoStyledScroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        sp.getViewport().setBackground(TBL_ROW_EVEN);
        return sp;
    }

    private String nvl(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }

    private void hienThongBao(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    /* ══════════════════ NÚT TRÒN ══════════════════ */
    static class RoundBtn extends JButton {
        private final Color bg, fg;
        RoundBtn(String text, Color bg, Color fg) {
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
            Color paint = !isEnabled()
                ? bg.darker().darker()
                : (getModel().isRollover() ? bg.brighter() : bg);
            g2.setColor(paint);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}