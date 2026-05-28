package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.model.CTPhieuThue;
import otkhongluong.gamestoremanagement.model.RentalOrder;
import otkhongluong.gamestoremanagement.controller.RentController;
import otkhongluong.gamestoremanagement.controller.RentController.ActionResult;

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
 * RentReturnDialog — Wizard 3 bước trả CD/Game
 *
 * LOGIC ĐIỂM: 1 điểm = 5.000 VNĐ
 * LOGIC TÍNH PHẠT TRỄ HẠN: soNgayTre x giaThueNgay x 1.5
 * LOGIC THANH TOÁN:
 *   TongPhaiTra = (TienThueGoc - GiamDiem) + PhatTre + ChiPhiHuHong
 *   KetQua      = TongPhaiTra - TienCoc
 */
public class RentReturnDialog extends JDialog {

    private static final int    DIEM_TO_VND  = 5_000; // 1 điểm = 5.000 VNĐ

    // =========================================================
    // PALETTE
    // =========================================================
    private static final Color BG           = new Color(28, 16, 72);
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
    private static final Font F_DIALOG_TITLE = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font F_SECTION      = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font F_LABEL        = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font F_LABEL_BOLD   = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font F_CELL         = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_TABLE_HDR    = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font F_VALUE        = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font F_MONEY        = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font F_HINT         = new Font("Segoe UI", Font.ITALIC, 11);

    // =========================================================
    // Controller
    // =========================================================
    private final RentController ctrl = new RentController();

    // =========================================================
    // STATE
    // =========================================================
    private int currentStep = 1;

    // Bước 1
    private JTextField        txtSDT;
    private JTable            tblPhieu;
    private DefaultTableModel tblModel;
    private List<RentalOrder>   searchResults;

    // Bước 2
    private RentalOrder         selectedPhieu;
    private JTable            tblChiTiet;
    private DefaultTableModel tblChiTietModel;
    private JLabel            lblNgayThue, lblNgayDK, lblTrangThai;
    private JLabel            lblPhatTre;
    private JTextField        txtChiPhiHuHong;
    private LocalDateTime     ngayTraThucTe;
    private int diemDaTruTheoMaPT = 0;

    // Bước 3 — labels
    private JLabel lblS3MaPT, lblS3KhachHang;
    private JLabel lblS3NgayThue, lblS3NgayTra;
    private JLabel lblS3TienThue, lblS3DiemTru, lblS3TienThueNet;
    private JLabel lblS3PhatTre, lblS3HuHong;
    private JLabel lblS3TienCoc, lblS3KetQua;

    // =========================================================
    // LAYOUT
    // =========================================================
    private JPanel  contentPanel;
    private JPanel  stepIndicator;
    private JButton btnBack, btnNext, btnConfirm;

    private final int maPT;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public RentReturnDialog(Frame parent, int maPT) {
        super(parent, "Trả CD / Game", true);
        this.maPT = maPT;

        setSize(740, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setBackground(BG);
        getRootPane().setBorder(BorderFactory.createLineBorder(ACCENT, 2));

        add(buildHeader(),  BorderLayout.NORTH);

        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(BG);
        contentPanel.add(buildStep1(), "step1");
        contentPanel.add(buildStep2(), "step2");
        contentPanel.add(buildStep3(), "step3");
        add(contentPanel, BorderLayout.CENTER);

        add(buildFooter(), BorderLayout.SOUTH);
        showStep(1);

        if (maPT > 0) prefillFromMaPT(maPT);
    }

    private void prefillFromMaPT(int id) {
        RentalOrder pt = ctrl.getById(id);
        if (pt == null) return;

        selectedPhieu = pt;
        loadStep2Data();
        showStep(2);
    }

    // =========================================================
    // HEADER
    // =========================================================
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(16, 20, 0, 20));

        JLabel title = new JLabel("TRẢ CD / GAME");
        title.setFont(F_DIALOG_TITLE);
        title.setForeground(WHITE);

        stepIndicator = buildStepIndicator();

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG);
        top.add(title, BorderLayout.WEST);
        top.add(stepIndicator, BorderLayout.EAST);

        JSeparator sep = new JSeparator();
        sep.setForeground(DIVIDER);
        sep.setBackground(DIVIDER);

        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setBackground(BG);
        wrapper.add(top, BorderLayout.CENTER);
        wrapper.add(sep, BorderLayout.SOUTH);

        p.add(wrapper);
        return p;
    }

    private JPanel buildStepIndicator() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        p.setBackground(BG);
        String[] labels = {"1. Tìm kiếm", "2. Chi tiết", "3. Xác nhận"};
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
                    lbl.setForeground(
                        s == active ? ACCENT_LIGHT :
                        (s < active ? SUCCESS : TEXT_MUTED)
                    );
                }
            }
        }
        stepIndicator.repaint();
    }

    // =========================================================
    // BƯỚC 1 — Tìm kiếm phiếu thuê
    // =========================================================
    private JPanel buildStep1() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(14, 20, 0, 20));

        JPanel topBar = new JPanel(new BorderLayout(0, 6));
        topBar.setBackground(BG);

        JLabel lbl = new JLabel("Số điện thoại khách hàng:");
        lbl.setFont(F_SECTION);
        lbl.setForeground(TEXT_MAIN);

        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setBackground(BG);

        txtSDT = makeInput();
        txtSDT.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doSearch();
            }
        });

        PillButton btnSearch = new PillButton("Tìm kiếm", ACCENT, WHITE);
        btnSearch.setPreferredSize(new Dimension(120, 36));
        btnSearch.addActionListener(e -> doSearch());

        inputRow.add(txtSDT,    BorderLayout.CENTER);
        inputRow.add(btnSearch, BorderLayout.EAST);

        topBar.add(lbl,      BorderLayout.NORTH);
        topBar.add(inputRow, BorderLayout.CENTER);
        p.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Mã PT", "Họ tên khách", "Ngày thuê", "Ngày trả dự kiến", "Trạng thái"};
        tblModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblPhieu = makeTable(tblModel);
        tblPhieu.getColumnModel().getColumn(0).setPreferredWidth(65);
        tblPhieu.getColumnModel().getColumn(1).setPreferredWidth(160);
        tblPhieu.getColumnModel().getColumn(2).setPreferredWidth(120);
        tblPhieu.getColumnModel().getColumn(3).setPreferredWidth(130);
        tblPhieu.getColumnModel().getColumn(4).setPreferredWidth(95);
        tblPhieu.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblPhieu.getSelectedRow() >= 0) goToStep2();
            }
        });

        p.add(wrapScroll(tblPhieu), BorderLayout.CENTER);

        JLabel hint = new JLabel("Chọn phiếu thuê rồi nhấn \"Tiếp theo\", hoặc double-click để sang bước tiếp.");
        hint.setFont(F_HINT);
        hint.setForeground(TEXT_MUTED);
        p.add(hint, BorderLayout.SOUTH);

        return p;
    }

    private void doSearch() {
        String sdt = txtSDT.getText().trim();
        if (sdt.isEmpty()) { showMsg("Vui lòng nhập số điện thoại!"); return; }

        searchResults = ctrl.searchActiveRentalsBySdt(sdt);

        tblModel.setRowCount(0);
        if (searchResults.isEmpty()) {
            showMsg("Không tìm thấy phiếu thuê đang hoạt động với SĐT: " + sdt);
            return;
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (RentalOrder pt : searchResults) {
            tblModel.addRow(new Object[]{
                "PT" + pt.getMaPT(),
                pt.getTenKhachHang() != null ? pt.getTenKhachHang() : "-",
                pt.getNgayThue()      != null ? pt.getNgayThue().format(fmt)      : "-",
                pt.getNgayTraDuKien() != null ? pt.getNgayTraDuKien().format(fmt) : "-",
                pt.getTrangThai()
            });
        }
        if (searchResults.size() == 1) tblPhieu.setRowSelectionInterval(0, 0);
    }

    // =========================================================
    // BƯỚC 2 — Chi tiết phiếu + nhập chi phí
    // =========================================================
    private JPanel buildStep2() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(14, 20, 0, 20));

        lblNgayThue  = new JLabel("-");
        lblNgayDK    = new JLabel("-");
        lblTrangThai = new JLabel("-");

        JPanel cards = new JPanel(new GridLayout(1, 3, 10, 0));
        cards.setBackground(BG);
        cards.setPreferredSize(new Dimension(0, 68));
        cards.add(infoCard("Ngày thuê",        lblNgayThue));
        cards.add(infoCard("Ngày trả dự kiến", lblNgayDK));
        cards.add(infoCard("Trạng thái",        lblTrangThai));
        p.add(cards, BorderLayout.NORTH);

        String[] cols = {"Mã CD", "Tên Game", "Tiền thuê gốc", "Điểm đã dùng", "Tình trạng"};
        tblChiTietModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblChiTiet = makeTable(tblChiTietModel);
        tblChiTiet.getColumnModel().getColumn(0).setPreferredWidth(60);
        tblChiTiet.getColumnModel().getColumn(1).setPreferredWidth(180);
        tblChiTiet.getColumnModel().getColumn(2).setPreferredWidth(120);
        tblChiTiet.getColumnModel().getColumn(3).setPreferredWidth(100);
        tblChiTiet.getColumnModel().getColumn(4).setPreferredWidth(90);

        tblChiTiet.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                String tt = tblChiTietModel.getValueAt(r, 4) != null
                    ? tblChiTietModel.getValueAt(r, 4).toString() : "";
                boolean isHong = tt.toUpperCase().startsWith("HONG");
                if (isHong) {
                    comp.setBackground(new Color(80, 20, 30));
                    comp.setForeground(DANGER);
                } else if (sel) {
                    comp.setBackground(ACCENT);
                    comp.setForeground(WHITE);
                } else {
                    comp.setBackground(r % 2 == 0 ? new Color(248, 246, 255) : WHITE);
                    comp.setForeground(TEXT_DARK);
                }
                return comp;
            }
        });

        p.add(wrapScroll(tblChiTiet), BorderLayout.CENTER);

        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 16, 0));
        bottomRow.setBackground(BG);
        bottomRow.setPreferredSize(new Dimension(0, 76));

        JPanel trePanel = new JPanel(new BorderLayout(0, 6));
        trePanel.setBackground(BG);
        JLabel lblTreLbl = new JLabel("Nợ gia hạn + phạt trễ mới (nếu có):");
        lblTreLbl.setFont(F_LABEL);
        lblTreLbl.setForeground(TEXT_MUTED);
        lblPhatTre = new JLabel("0 VNĐ");
        lblPhatTre.setFont(F_MONEY);
        lblPhatTre.setForeground(SUCCESS);
        trePanel.add(lblTreLbl,  BorderLayout.NORTH);
        trePanel.add(lblPhatTre, BorderLayout.CENTER);

        JPanel huHongPanel = new JPanel(new BorderLayout(0, 6));
        huHongPanel.setBackground(BG);
        JLabel lblHuHongLbl = new JLabel("Chi phí hư hỏng / sửa chữa (VNĐ):");
        lblHuHongLbl.setFont(F_LABEL);
        lblHuHongLbl.setForeground(TEXT_MUTED);
        txtChiPhiHuHong = makeInput();
        txtChiPhiHuHong.setText("0");
        huHongPanel.add(lblHuHongLbl,    BorderLayout.NORTH);
        huHongPanel.add(txtChiPhiHuHong, BorderLayout.CENTER);

        bottomRow.add(trePanel);
        bottomRow.add(huHongPanel);
        p.add(bottomRow, BorderLayout.SOUTH);

        return p;
    }

    private void loadStep2Data() {
        if (selectedPhieu == null) return;

        RentalOrder full = ctrl.getById(selectedPhieu.getMaPT());
        if (full == null) { showMsg("Không tải được thông tin phiếu thuê!"); return; }
        selectedPhieu = full;

        ngayTraThucTe = LocalDate.now().atStartOfDay();

        // ✅ Load điểm đã trừ từ DIEM_LICHSU thay vì parse TinhTrang
        diemDaTruTheoMaPT = ctrl.loadDiemDaTru(selectedPhieu.getMaPT());

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblNgayThue.setText(selectedPhieu.getNgayThue() != null
            ? selectedPhieu.getNgayThue().format(fmt) : "-");
        lblNgayDK.setText(selectedPhieu.getNgayTraDuKien() != null
            ? selectedPhieu.getNgayTraDuKien().format(fmt) : "-");
        lblTrangThai.setText(selectedPhieu.getTrangThai() != null
            ? selectedPhieu.getTrangThai() : "-");

        tblChiTietModel.setRowCount(0);
        int soCD = selectedPhieu.getDanhSachChiTiet() != null
                   ? selectedPhieu.getDanhSachChiTiet().size() : 1;

        // Phân bổ điểm đều cho các CD (hiển thị tổng ở dòng đầu, còn lại "—")
        boolean diemHienThi = false;
        if (selectedPhieu.getDanhSachChiTiet() != null) {
            for (int idx = 0; idx < selectedPhieu.getDanhSachChiTiet().size(); idx++) {
                CTPhieuThue ct = selectedPhieu.getDanhSachChiTiet().get(idx);
                String raw = ct.getTinhTrang() != null ? ct.getTinhTrang() : "";
                String tinhTrangHienThi = raw.split("\\|")[0];

                String diemCol;
                if (!diemHienThi && diemDaTruTheoMaPT > 0) {
                    // Hiển thị tổng điểm đã trừ ở dòng đầu tiên
                    diemCol = String.format("%d điểm  (~%,.0f VNĐ)",
                        diemDaTruTheoMaPT,
                        (double) diemDaTruTheoMaPT * DIEM_TO_VND);
                    diemHienThi = true;
                } else if (diemHienThi) {
                    diemCol = "—";
                } else {
                    diemCol = "Không dùng";
                }

                tblChiTietModel.addRow(new Object[]{
                    "CD" + ct.getMaCD(),
                    ct.getTenGame(),
                    String.format("%,.0f VNĐ", ct.getDonGiaThue()),
                    diemCol,
                    tinhTrangHienThi
                });
            }
        }

        double phatTre = tinhPhatTreHan(selectedPhieu, ngayTraThucTe);
        double noGiaHan = selectedPhieu.getTienPhat();           // nợ từ các lần gia hạn
        double phatMoi  = Math.max(0, phatTre - noGiaHan);       // phạt trễ phát sinh mới

        if (noGiaHan > 0 && phatMoi > 0) {
            lblPhatTre.setText(String.format("%,.0f VNĐ  (nợ GH: %,.0f  +  trễ mới: %,.0f)",
                phatTre, noGiaHan, phatMoi));
            lblPhatTre.setForeground(DANGER);
        } else if (noGiaHan > 0) {
            lblPhatTre.setText(String.format("%,.0f VNĐ  (nợ từ gia hạn)", noGiaHan));
            lblPhatTre.setForeground(GOLD);
        } else if (phatMoi > 0) {
            lblPhatTre.setText(String.format("%,.0f VNĐ  (phạt trễ)", phatMoi));
            lblPhatTre.setForeground(DANGER);
        } else {
            lblPhatTre.setText("Không có");
            lblPhatTre.setForeground(SUCCESS);
        }

        txtChiPhiHuHong.setText("0");
    }

    private double tinhPhatTreHan(RentalOrder pt, LocalDateTime ngayTra) {
        if (pt == null || ngayTra == null || pt.getNgayTraDuKien() == null) return 0;
        LocalDateTime ngayDK = pt.getNgayTraDuKien();

        // Phạt trễ mới: tính từ NgayTraDuKien hiện tại (đã cộng ngày gia hạn nếu có)
        double phatMoi = 0;
        if (ngayTra.isAfter(ngayDK)) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(
                ngayDK.toLocalDate(), ngayTra.toLocalDate());
            if (days <= 0) days = 1;
            double giaThueNgay = pt.getDanhSachChiTiet() != null
                ? pt.getDanhSachChiTiet().stream().mapToDouble(CTPhieuThue::getGiaThueNgay).sum()
                : 0;
            phatMoi = days * giaThueNgay * 1.5;
        }

        // FIX: cộng thêm TienPhat đang có trong DB.
        // Sau khi gia hạn, DAO lưu phatTre (lúc gia hạn) vào TienPhat.
        // NgayTraDuKien đã được đẩy ra sau → phatMoi tính từ ngày mới = 0 nếu trả đúng hạn.
        // Nếu không cộng pt.getTienPhat(), khoản phạt đã thu lúc gia hạn bị mất
        // khỏi màn hình quyết toán — returnCDFull() vẫn ghi đúng vào DB nhưng
        // delta (thu thêm / hoàn lại) hiển thị sai cho nhân viên.
        double phatCuDaLuu = pt.getTienPhat(); // 0 nếu chưa từng gia hạn với phạt
        return phatMoi + phatCuDaLuu;
    }

    // =========================================================
    // BƯỚC 3 — Xác nhận thanh toán
    // =========================================================
    private JPanel buildStep3() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(16, 20, 10, 20));

        lblS3MaPT        = sumValue(TEXT_MAIN);
        lblS3KhachHang   = sumValue(TEXT_MAIN);
        lblS3NgayThue    = sumValue(TEXT_MAIN);
        lblS3NgayTra     = sumValue(TEXT_MAIN);
        lblS3TienThue    = sumValue(TEXT_MAIN);
        lblS3DiemTru     = sumValue(SUCCESS);
        lblS3TienThueNet = sumValue(ACCENT_LIGHT);
        lblS3PhatTre     = sumValue(SUCCESS);
        lblS3HuHong      = sumValue(DANGER);
        lblS3TienCoc     = sumValue(GOLD);
        lblS3KetQua      = sumValue(TEXT_MAIN);
        lblS3KetQua.setFont(F_MONEY);

        // 11 dòng
        JPanel card = new JPanel(new GridLayout(11, 2, 8, 10));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(DIVIDER, 1, true),
            new EmptyBorder(16, 20, 16, 20)
        ));

        card.add(sumKey("Mã phiếu thuê:"));         card.add(lblS3MaPT);
        card.add(sumKey("Khách hàng:"));             card.add(lblS3KhachHang);
        card.add(sumKey("Ngày thuê:"));              card.add(lblS3NgayThue);
        card.add(sumKey("Ngày trả thực tế:"));       card.add(lblS3NgayTra);
        card.add(sumKey("Tiền thuê gốc:"));          card.add(lblS3TienThue);
        card.add(sumKey("Giảm từ điểm:"));           card.add(lblS3DiemTru);
        card.add(sumKey("Tiền thuê sau giảm:"));     card.add(lblS3TienThueNet);
        card.add(sumKey("Nợ gia hạn + phạt trễ:"));  card.add(lblS3PhatTre);
        card.add(sumKey("Chi phí hư hỏng:"));        card.add(lblS3HuHong);
        card.add(sumKey("Tiền cọc đã đặt:"));        card.add(lblS3TienCoc);
        card.add(sumKey("Kết quả thanh toán:"));     card.add(lblS3KetQua);

        JLabel notice = new JLabel(
            "<html><center>"
          + "Tổng phải trả = (Thuê gốc − Giảm điểm) + Phạt trễ + Hư hỏng  |  Kết quả = Tổng phải trả − Cọc<br>"
          + "Sau xác nhận: Phiếu thuê → Đã Trả  |  CD → Sẵn sàng  |  Cộng điểm tích lũy"
          + "</center></html>"
        );
        notice.setFont(F_HINT);
        notice.setForeground(TEXT_MUTED);
        notice.setHorizontalAlignment(SwingConstants.CENTER);
        notice.setBorder(new EmptyBorder(4, 0, 0, 0));

        p.add(card,   BorderLayout.CENTER);
        p.add(notice, BorderLayout.SOUTH);
        return p;
    }

    private JLabel sumValue(Color color) {
        JLabel l = new JLabel("-");
        l.setFont(F_VALUE);
        l.setForeground(color);
        return l;
    }

    private JLabel sumKey(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_LABEL);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    private void updateStep3Summary() {
        if (selectedPhieu == null) return;

        try {
            double tienThueGoc = 0;
            if (selectedPhieu.getDanhSachChiTiet() != null)
                for (CTPhieuThue ct : selectedPhieu.getDanhSachChiTiet())
                    tienThueGoc += ct.getDonGiaThue();

            int diemDaGiam = diemDaTruTheoMaPT; // ✅ lấy từ DIEM_LICHSU

            // ✅ 1 điểm = 5.000 VNĐ
            double giamDiem    = diemDaGiam * (double) DIEM_TO_VND;
            double tienThueNet = Math.max(0, tienThueGoc - giamDiem);
            double phatTre     = tinhPhatTreHan(selectedPhieu, ngayTraThucTe);
            double chiPhiHuHong = 0;
            try {
                chiPhiHuHong = Double.parseDouble(
                    txtChiPhiHuHong.getText().trim().replace(",", ""));
            } catch (NumberFormatException ignored) {}

            double tongPhaiTra = tienThueNet + phatTre + chiPhiHuHong;
            double coc         = selectedPhieu.getTienCoc();
            double delta       = tongPhaiTra - coc;

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            lblS3MaPT.setText("PT" + selectedPhieu.getMaPT());
            lblS3KhachHang.setText(selectedPhieu.getTenKhachHang() != null
                ? selectedPhieu.getTenKhachHang() : "-");
            lblS3NgayThue.setText(selectedPhieu.getNgayThue() != null
                ? selectedPhieu.getNgayThue().format(fmt) : "-");
            lblS3NgayTra.setText(ngayTraThucTe.format(fmt));

            lblS3TienThue.setText(String.format("%,.0f VNĐ", tienThueGoc));
            lblS3TienThue.setForeground(TEXT_MAIN);

            if (diemDaGiam > 0) {
                lblS3DiemTru.setText(String.format("-%,.0f VNĐ  (%d điểm x %,.0f VNĐ/điểm)",
                    giamDiem, diemDaGiam, (double) DIEM_TO_VND));
                lblS3DiemTru.setForeground(SUCCESS);
            } else {
                lblS3DiemTru.setText("Không sử dụng điểm");
                lblS3DiemTru.setForeground(TEXT_MUTED);
            }

            lblS3TienThueNet.setText(String.format("%,.0f VNĐ", tienThueNet));
            lblS3TienThueNet.setForeground(ACCENT_LIGHT);

            double noGiaHan = selectedPhieu.getTienPhat();
            double phatMoi  = Math.max(0, phatTre - noGiaHan);
            if (noGiaHan > 0 && phatMoi > 0) {
                lblS3PhatTre.setText(String.format("%,.0f VNĐ  (nợ GH: %,.0f  +  trễ mới: %,.0f)",
                    phatTre, noGiaHan, phatMoi));
                lblS3PhatTre.setForeground(DANGER);
            } else if (noGiaHan > 0) {
                lblS3PhatTre.setText(String.format("%,.0f VNĐ  (nợ từ gia hạn)", noGiaHan));
                lblS3PhatTre.setForeground(GOLD);
            } else if (phatMoi > 0) {
                lblS3PhatTre.setText(String.format("%,.0f VNĐ  (phạt trễ)", phatMoi));
                lblS3PhatTre.setForeground(DANGER);
            } else {
                lblS3PhatTre.setText("Không có");
                lblS3PhatTre.setForeground(SUCCESS);
            }

            if (chiPhiHuHong > 0) {
                lblS3HuHong.setText(String.format("%,.0f VNĐ", chiPhiHuHong));
                lblS3HuHong.setForeground(DANGER);
            } else {
                lblS3HuHong.setText("Không có");
                lblS3HuHong.setForeground(TEXT_MUTED);
            }

            lblS3TienCoc.setText(String.format("%,.0f VNĐ", coc));
            lblS3TienCoc.setForeground(GOLD);

            if (delta > 0) {
                lblS3KetQua.setText(String.format("Thu thêm từ khách:  %,.0f VNĐ", delta));
                lblS3KetQua.setForeground(DANGER);
            } else if (delta < 0) {
                lblS3KetQua.setText(String.format("Hoàn lại cho khách:  %,.0f VNĐ", -delta));
                lblS3KetQua.setForeground(SUCCESS);
            } else {
                lblS3KetQua.setText("Hòa nhau");
                lblS3KetQua.setForeground(TEXT_MUTED);
            }

        } catch (Exception ex) {
            showMsg("Lỗi tính toán: " + ex.getMessage());
        }
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

        btnNext = new PillButton("Tiếp theo", ACCENT, WHITE);
        btnNext.setPreferredSize(new Dimension(120, 36));
        btnNext.addActionListener(e -> {
            if (currentStep == 1) goToStep2();
            else if (currentStep == 2) goToStep3();
        });

        btnConfirm = new PillButton("Xác nhận trả", BTN_GREEN, TEXT_DARK);
        btnConfirm.setPreferredSize(new Dimension(150, 36));
        btnConfirm.setVisible(false);
        btnConfirm.addActionListener(e -> doConfirmReturn());

        PillButton btnCancel = new PillButton("Hủy", BTN_RED, WHITE);
        btnCancel.setPreferredSize(new Dimension(80, 36));
        btnCancel.addActionListener(e -> dispose());

        btnRow.add(btnCancel);
        btnRow.add(btnBack);
        btnRow.add(btnNext);
        btnRow.add(btnConfirm);

        p.add(sep,    BorderLayout.NORTH);
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
        btnNext.setVisible(step < 3);
        btnConfirm.setVisible(step == 3);
        updateStepIndicator(step);
    }

    private void goToStep2() {
        int row = tblPhieu.getSelectedRow();
        if (row < 0) { showMsg("Vui lòng chọn một phiếu thuê!"); return; }
        if (searchResults == null || row >= searchResults.size()) return;
        selectedPhieu = searchResults.get(row);
        loadStep2Data();
        showStep(2);
    }

    private void goToStep3() {
        try {
            double val = Double.parseDouble(
                txtChiPhiHuHong.getText().trim().replace(",", ""));
            if (val < 0) { showMsg("Chi phí hư hỏng không được âm!"); return; }
        } catch (NumberFormatException e) {
            showMsg("Chi phí hư hỏng phải là số!"); return;
        }
        updateStep3Summary();
        showStep(3);
    }

    // =========================================================
    // XÁC NHẬN TRẢ CD
    // =========================================================
    private void doConfirmReturn() {
        try {
            double tienThueGoc = 0;
            if (selectedPhieu.getDanhSachChiTiet() != null)
                for (CTPhieuThue ct : selectedPhieu.getDanhSachChiTiet())
                    tienThueGoc += ct.getDonGiaThue();

            int diemDaGiam = diemDaTruTheoMaPT; // ✅ lấy từ DIEM_LICHSU

            // ✅ 1 điểm = 5.000 VNĐ
            double giamDiem    = diemDaGiam * (double) DIEM_TO_VND;
            double tienThueNet = Math.max(0, tienThueGoc - giamDiem);
            double phatTre     = tinhPhatTreHan(selectedPhieu, ngayTraThucTe);

            double chiPhiHuHong = 0;
            try {
                chiPhiHuHong = Double.parseDouble(
                    txtChiPhiHuHong.getText().trim().replace(",", ""));
            } catch (NumberFormatException ignored) {}

            double tongPhaiTra = tienThueNet + phatTre + chiPhiHuHong;
            double coc         = selectedPhieu.getTienCoc();
            double delta       = tongPhaiTra - coc;

            String ketQua;
            if      (delta > 0) ketQua = String.format("Thu thêm từ khách:  %,.0f VNĐ", delta);
            else if (delta < 0) ketQua = String.format("Hoàn lại cho khách:  %,.0f VNĐ", -delta);
            else                ketQua = "Hòa nhau";

            // Tính điểm tích lũy: 100.000 VNĐ tiền thuê gốc = 1 điểm
            int diemTichLuy = (int) (tienThueGoc / 100_000);

            String msg = String.format(
                "Xác nhận thanh toán và hoàn tất trả?\n\n"
              + "  Tiền thuê gốc      : %,.0f VNĐ\n"
              + "  Giảm từ điểm       : -%,.0f VNĐ  (%d điểm x %,d VNĐ)\n"
              + "  Tiền thuê sau giảm : %,.0f VNĐ\n"
              + "  Nợ GH + phạt trễ   : %,.0f VNĐ\n"
              + "  Chi phí hư hỏng    : %,.0f VNĐ\n"
              + "  Tổng phải trả      : %,.0f VNĐ\n"
              + "  Tiền cọc           : %,.0f VNĐ\n"
              + "  --------------------------------\n"
              + "  %s\n\n"
              + "  Điểm tích lũy sẽ cộng: +%d điểm",
                tienThueGoc, giamDiem, diemDaGiam, DIEM_TO_VND,
                tienThueNet, phatTre, chiPhiHuHong,
                tongPhaiTra, coc, ketQua, diemTichLuy);

            int confirm = JOptionPane.showConfirmDialog(this, msg,
                "Xác nhận thanh toán", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;


            ActionResult ar = ctrl.returnCD(
                selectedPhieu.getMaPT(),
                ngayTraThucTe,
                chiPhiHuHong,
                selectedPhieu.getMaKH(),
                diemTichLuy
            );

            if (ar.success) {
                JOptionPane.showMessageDialog(this,
                    String.format(
                        "Trả CD thành công!\n\n"
                      + "  Phiếu thuê → Đã Trả\n"
                      + "  CD → Sẵn sàng\n"
                      + "  Điểm tích lũy cộng: +%d điểm ✓\n\n"
                      + "  %s",
                        diemTichLuy, ketQua),
                    "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                showMsg(ar.message);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showMsg("Lỗi xử lý: " + ex.getMessage());
        }
    }

    // =========================================================
    // HELPERS
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

    private JPanel infoCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 3));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(DIVIDER, 1, true),
            new EmptyBorder(7, 10, 7, 10)
        ));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        t.setForeground(TEXT_MUTED);
        valueLabel.setFont(F_LABEL_BOLD);
        valueLabel.setForeground(TEXT_MAIN);
        card.add(t, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
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
        t.setRowHeight(34);
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
                lbl.setFont(F_TABLE_HDR);
                lbl.setForeground(WHITE);
                lbl.setBackground(ACCENT);
                lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(6, 10, 6, 10));
                return lbl;
            }
        });
        h.setBackground(ACCENT);
        h.setPreferredSize(new Dimension(0, 36));
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
}