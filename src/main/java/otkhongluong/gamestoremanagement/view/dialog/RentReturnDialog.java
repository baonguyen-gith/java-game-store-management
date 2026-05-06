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
 * RentReturnDialog — Wizard 3 buoc tra CD/Game
 *
 * Buoc 1 : Tim kiem phieu thue theo SDT
 * Buoc 2 : Xem chi tiet phieu + nhap chi phi hu hong
 * Buoc 3 : Xac nhan thanh toan va hoan tat
 *
 * LOGIC TINH TIEN THUE GOC:
 *   Doc thang tu CTPHIEUTHUE.DonGiaThue (da luu khi tao phieu / gia han)
 *   KHONG tinh lai tu ngay thue x don gia, tranh tinh lap phi gia han.
 *
 * LOGIC TINH PHAT TRE HAN:
 *   So ngay tre = ngayTraThucTe - ngayTraDuKien (neu > 0)
 *   Phat tre = soNgayTre x 10.000 VND/ngay
 *
 * LOGIC THANH TOAN:
 *   TongPhaiTra = TienThueGoc + PhatTre + ChiPhiHuHong
 *   KetQua      = TongPhaiTra - TienCoc
 *   > 0 : Thu them tu khach
 *   < 0 : Hoan lai cho khach
 *   = 0 : Hoa nhau
 */
public class RentReturnDialog extends JDialog {

    // =========================================================
    // PALETTE — dong nhat voi RentAddDialog
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
    private static final Font F_DIALOG_TITLE = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font F_SECTION      = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font F_LABEL        = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font F_LABEL_BOLD   = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font F_CELL         = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_TABLE_HDR    = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font F_VALUE        = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font F_MONEY        = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font F_BTN          = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font F_HINT         = new Font("Segoe UI", Font.ITALIC, 11);

    // =========================================================
    // DAO / SERVICE
    // =========================================================
    private final ThueService service = new ThueService();

    // =========================================================
    // STATE
    // =========================================================
    private int currentStep = 1;

    // Buoc 1
    private JTextField        txtSDT;
    private JTable            tblPhieu;
    private DefaultTableModel tblModel;
    private List<PhieuThue>   searchResults;

    // Buoc 2
    private PhieuThue         selectedPhieu;
    private JTable            tblChiTiet;
    private DefaultTableModel tblChiTietModel;
    private JLabel            lblNgayThue, lblNgayDK, lblTrangThai;
    private JLabel            lblPhatTre;
    private JTextField        txtChiPhiHuHong;
    private LocalDateTime     ngayTraThucTe;

    // Buoc 3 — labels
    private JLabel lblS3MaPT, lblS3KhachHang;
    private JLabel lblS3NgayThue, lblS3NgayTra;
    private JLabel lblS3TienThue, lblS3DiemTru;
    private JLabel lblS3PhatTre, lblS3HuHong;
    private JLabel lblS3TienCoc, lblS3KetQua;

    // =========================================================
    // LAYOUT
    // =========================================================
    private JPanel  contentPanel;
    private JPanel  stepIndicator;
    private JButton btnBack, btnNext, btnConfirm;

    // Ma phieu thue truyen vao de prefill (0 = khong prefill)
    private final int maPT;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public RentReturnDialog(Frame parent, int maPT) {
        super(parent, "Tra CD / Game", true);
        this.maPT = maPT;

        setSize(740, 580);
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

    // Prefill neu mo tu man hinh quan ly phieu thue
    private void prefillFromMaPT(int id) {
        PhieuThue pt = service.getById(id);
        if (pt != null && pt.getSoDienThoai() != null) {
            txtSDT.setText(pt.getSoDienThoai());
            doSearch();
            if (searchResults != null) {
                for (int i = 0; i < searchResults.size(); i++) {
                    if (searchResults.get(i).getMaPT() == id) {
                        tblPhieu.setRowSelectionInterval(i, i);
                        break;
                    }
                }
            }
        }
    }

    // =========================================================
    // HEADER
    // =========================================================
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(16, 20, 0, 20));

        JLabel title = new JLabel("TRA CD / GAME");
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
        String[] labels = {"1. Tim kiem", "2. Chi tiet", "3. Xac nhan"};
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
    // BUOC 1 — Tim kiem phieu thue
    // =========================================================
    private JPanel buildStep1() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(14, 20, 0, 20));

        // Thanh tim kiem
        JPanel topBar = new JPanel(new BorderLayout(0, 6));
        topBar.setBackground(BG);

        JLabel lbl = new JLabel("So dien thoai khach hang:");
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

        PillButton btnSearch = new PillButton("Tim kiem", ACCENT, WHITE);
        btnSearch.setPreferredSize(new Dimension(120, 36));
        btnSearch.addActionListener(e -> doSearch());

        inputRow.add(txtSDT,     BorderLayout.CENTER);
        inputRow.add(btnSearch,  BorderLayout.EAST);

        topBar.add(lbl,      BorderLayout.NORTH);
        topBar.add(inputRow, BorderLayout.CENTER);
        p.add(topBar, BorderLayout.NORTH);

        // Bang danh sach phieu thue
        String[] cols = {"Ma PT", "Ho ten khach", "Ngay thue", "Ngay tra du kien", "Trang thai"};
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

        JLabel hint = new JLabel("Chon phieu thue roi nhan \"Tiep theo\", hoac double-click de sang buoc tiep.");
        hint.setFont(F_HINT);
        hint.setForeground(TEXT_MUTED);
        p.add(hint, BorderLayout.SOUTH);

        return p;
    }

    private void doSearch() {
        String sdt = txtSDT.getText().trim();
        if (sdt.isEmpty()) { showMsg("Vui long nhap so dien thoai!"); return; }

        List<PhieuThue> all = service.getAll();
        searchResults = all.stream()
            .filter(pt -> pt.getSoDienThoai() != null
                       && pt.getSoDienThoai().contains(sdt)
                       && "DangThue".equalsIgnoreCase(pt.getTrangThai()))
            .collect(Collectors.toList());

        tblModel.setRowCount(0);
        if (searchResults.isEmpty()) {
            showMsg("Khong tim thay phieu thue dang hoat dong voi SDT: " + sdt);
            return;
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (PhieuThue pt : searchResults) {
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
    // BUOC 2 — Chi tiet phieu + nhap chi phi
    // =========================================================
    private JPanel buildStep2() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(14, 20, 0, 20));

        // --- Info cards ---
        lblNgayThue  = new JLabel("-");
        lblNgayDK    = new JLabel("-");
        lblTrangThai = new JLabel("-");

        JPanel cards = new JPanel(new GridLayout(1, 3, 10, 0));
        cards.setBackground(BG);
        cards.setPreferredSize(new Dimension(0, 68));
        cards.add(infoCard("Ngay thue",       lblNgayThue));
        cards.add(infoCard("Ngay tra du kien", lblNgayDK));
        cards.add(infoCard("Trang thai",       lblTrangThai));
        p.add(cards, BorderLayout.NORTH);

        // --- Bang chi tiet CD ---
        String[] cols = {"Ma CD", "Ten Game", "Tien thue goc", "Tinh trang"};
        tblChiTietModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblChiTiet = makeTable(tblChiTietModel);
        tblChiTiet.getColumnModel().getColumn(0).setPreferredWidth(70);
        tblChiTiet.getColumnModel().getColumn(1).setPreferredWidth(200);
        tblChiTiet.getColumnModel().getColumn(2).setPreferredWidth(140);
        tblChiTiet.getColumnModel().getColumn(3).setPreferredWidth(110);

        // To mau hang bi hong
        tblChiTiet.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                String tt = tblChiTietModel.getValueAt(r, 3) != null
                    ? tblChiTietModel.getValueAt(r, 3).toString() : "";
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

        // --- Phat tre + chi phi hu hong ---
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 16, 0));
        bottomRow.setBackground(BG);
        bottomRow.setPreferredSize(new Dimension(0, 76));

        // Tien phat tre (tu dong tinh)
        JPanel trePanel = new JPanel(new BorderLayout(0, 6));
        trePanel.setBackground(BG);
        JLabel lblTreLbl = new JLabel("Tien phat tre han (tu tinh):");
        lblTreLbl.setFont(F_LABEL);
        lblTreLbl.setForeground(TEXT_MUTED);
        lblPhatTre = new JLabel("0 VND");
        lblPhatTre.setFont(F_MONEY);
        lblPhatTre.setForeground(SUCCESS);
        trePanel.add(lblTreLbl,  BorderLayout.NORTH);
        trePanel.add(lblPhatTre, BorderLayout.CENTER);

        // Chi phi hu hong (nhap tay)
        JPanel huHongPanel = new JPanel(new BorderLayout(0, 6));
        huHongPanel.setBackground(BG);
        JLabel lblHuHongLbl = new JLabel("Chi phi hu hong / sua chua (VND):");
        lblHuHongLbl.setFont(F_LABEL);
        lblHuHongLbl.setForeground(TEXT_MUTED);
        txtChiPhiHuHong = makeInput();
        txtChiPhiHuHong.setText("0");
        huHongPanel.add(lblHuHongLbl,   BorderLayout.NORTH);
        huHongPanel.add(txtChiPhiHuHong, BorderLayout.CENTER);

        bottomRow.add(trePanel);
        bottomRow.add(huHongPanel);
        p.add(bottomRow, BorderLayout.SOUTH);

        return p;
    }

    private void loadStep2Data() {
        if (selectedPhieu == null) return;

        PhieuThue full = service.getById(selectedPhieu.getMaPT());
        if (full == null) { showMsg("Khong tai duoc thong tin phieu thue!"); return; }
        selectedPhieu = full;

        ngayTraThucTe = LocalDate.now().atStartOfDay();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblNgayThue.setText(selectedPhieu.getNgayThue() != null
            ? selectedPhieu.getNgayThue().format(fmt) : "-");
        lblNgayDK.setText(selectedPhieu.getNgayTraDuKien() != null
            ? selectedPhieu.getNgayTraDuKien().format(fmt) : "-");
        lblTrangThai.setText(selectedPhieu.getTrangThai() != null
            ? selectedPhieu.getTrangThai() : "-");

        // Hien thi chi tiet CD — doc DonGiaThue tu DB (tong thue goc da luu)
        tblChiTietModel.setRowCount(0);
        if (selectedPhieu.getDanhSachChiTiet() != null) {
            for (PhieuThue.CTPhieuThue ct : selectedPhieu.getDanhSachChiTiet()) {
                String tinhTrangHienThi = ct.getTinhTrang() != null
                    ? ct.getTinhTrang().split("\\|")[0] : "";
                tblChiTietModel.addRow(new Object[]{
                    "CD" + ct.getMaCD(),
                    ct.getTenGame(),
                    // DonGiaThue = tong thue goc da luu (khong tinh lai tu ngay)
                    String.format("%,.0f VND", ct.getDonGiaThue()),
                    tinhTrangHienThi
                });
            }
        }

        // Phat tre tinh tu ngay tra thuc te so voi ngay du kien
        double phatTre = tinhPhatTreHan(selectedPhieu, ngayTraThucTe);
        lblPhatTre.setForeground(phatTre > 0 ? DANGER : SUCCESS);
        lblPhatTre.setText(phatTre > 0
            ? String.format("%,.0f VND", phatTre)
            : "Dung han");

        txtChiPhiHuHong.setText("0");
    }

    /**
     * Tinh phat tre han:
     * Phat = soNgayTre x 10.000 VND/ngay
     * (Chi tinh neu ngayTra > ngayDuKien)
     */
    private double tinhPhatTreHan(PhieuThue pt, LocalDateTime ngayTra) {
        if (pt == null || ngayTra == null || pt.getNgayTraDuKien() == null) return 0;
        LocalDateTime ngayDK = pt.getNgayTraDuKien();
        if (!ngayTra.isAfter(ngayDK)) return 0;
        long days = java.time.temporal.ChronoUnit.DAYS.between(
            ngayDK.toLocalDate(), ngayTra.toLocalDate());
        if (days <= 0) days = 1;
        return days * 10_000;
    }

    // =========================================================
    // BUOC 3 — Xac nhan thanh toan
    // =========================================================
    private JPanel buildStep3() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(16, 20, 10, 20));

        lblS3MaPT      = sumValue(TEXT_MAIN);
        lblS3KhachHang = sumValue(TEXT_MAIN);
        lblS3NgayThue  = sumValue(TEXT_MAIN);
        lblS3NgayTra   = sumValue(TEXT_MAIN);
        lblS3TienThue  = sumValue(TEXT_MAIN);
        lblS3DiemTru   = sumValue(SUCCESS);
        lblS3PhatTre   = sumValue(SUCCESS);
        lblS3HuHong    = sumValue(DANGER);
        lblS3TienCoc   = sumValue(GOLD);
        lblS3KetQua    = sumValue(TEXT_MAIN);
        lblS3KetQua.setFont(F_MONEY);

        JPanel card = new JPanel(new GridLayout(10, 2, 8, 10));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(DIVIDER, 1, true),
            new EmptyBorder(16, 20, 16, 20)
        ));

        card.add(sumKey("Ma phieu thue:"));       card.add(lblS3MaPT);
        card.add(sumKey("Khach hang:"));          card.add(lblS3KhachHang);
        card.add(sumKey("Ngay thue:"));           card.add(lblS3NgayThue);
        card.add(sumKey("Ngay tra thuc te:"));    card.add(lblS3NgayTra);
        card.add(sumKey("Tien thue goc:"));       card.add(lblS3TienThue);
        card.add(sumKey("Giam tu diem:"));        card.add(lblS3DiemTru);
        card.add(sumKey("Phat tre han:"));        card.add(lblS3PhatTre);
        card.add(sumKey("Chi phi hu hong:"));     card.add(lblS3HuHong);
        card.add(sumKey("Tien coc da dat:"));     card.add(lblS3TienCoc);
        card.add(sumKey("Ket qua thanh toan:")); card.add(lblS3KetQua);

        JLabel notice = new JLabel(
            "<html><center>"
          + "Cong thuc: Tong phai tra = Thue goc + Phat tre + Hu hong  |  "
          + "Ket qua = Tong phai tra - Coc<br>"
          + "Sau xac nhan: Phieu thue chuyen sang Da Tra  |  CD chuyen sang San sang  |  Cong diem tich luy"
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

    /**
     * Cap nhat man hinh xac nhan (buoc 3).
     *
     * LOGIC TINH TIEN THUE:
     *   - Doc DonGiaThue tu tung CTPhieuThue (tong thue goc da luu, khong tinh lai)
     *   - Tru diem da su dung (luu trong TinhTrang "OK|diem=N")
     *   - Tong sau giam = tienThueGoc - giamDiem
     */
    private void updateStep3Summary() {
        if (selectedPhieu == null) return;

        try {
            // Tong tien thue goc tu DonGiaThue (da luu khi tao / gia han phieu)
            double tienThueGoc = 0;
            int    diemDaGiam  = 0;
            if (selectedPhieu.getDanhSachChiTiet() != null) {
                for (PhieuThue.CTPhieuThue ct : selectedPhieu.getDanhSachChiTiet()) {
                    tienThueGoc += ct.getDonGiaThue(); // Doc thang, khong tinh lai
                    String tt = ct.getTinhTrang();
                    if (tt != null && tt.contains("|diem=")) {
                        try {
                            diemDaGiam += Integer.parseInt(tt.split("\\|diem=")[1].trim());
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            double giamDiem    = diemDaGiam * 1_000;
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

            if (diemDaGiam > 0) {
                lblS3TienThue.setText(String.format(
                    "%,.0f VND  (goc %,.0f  -  giam %,.0f tu %d diem)",
                    tienThueNet, tienThueGoc, giamDiem, diemDaGiam));
            } else {
                lblS3TienThue.setText(String.format("%,.0f VND", tienThueGoc));
            }

            if (diemDaGiam > 0) {
                lblS3DiemTru.setText(String.format("-%,.0f VND  (%d diem)", giamDiem, diemDaGiam));
                lblS3DiemTru.setForeground(SUCCESS);
            } else {
                lblS3DiemTru.setText("Khong su dung diem");
                lblS3DiemTru.setForeground(TEXT_MUTED);
            }

            if (phatTre > 0) {
                lblS3PhatTre.setText(String.format("%,.0f VND", phatTre));
                lblS3PhatTre.setForeground(DANGER);
            } else {
                lblS3PhatTre.setText("Khong co  (dung han)");
                lblS3PhatTre.setForeground(SUCCESS);
            }

            if (chiPhiHuHong > 0) {
                lblS3HuHong.setText(String.format("%,.0f VND", chiPhiHuHong));
                lblS3HuHong.setForeground(DANGER);
            } else {
                lblS3HuHong.setText("Khong co");
                lblS3HuHong.setForeground(TEXT_MUTED);
            }

            lblS3TienCoc.setText(String.format("%,.0f VND", coc));
            lblS3TienCoc.setForeground(GOLD);

            if (delta > 0) {
                lblS3KetQua.setText(String.format("Thu them tu khach:  %,.0f VND", delta));
                lblS3KetQua.setForeground(DANGER);
            } else if (delta < 0) {
                lblS3KetQua.setText(String.format("Hoan lai cho khach:  %,.0f VND", -delta));
                lblS3KetQua.setForeground(SUCCESS);
            } else {
                lblS3KetQua.setText("Hoa nhau");
                lblS3KetQua.setForeground(TEXT_MUTED);
            }

        } catch (Exception ex) {
            showMsg("Loi tinh toan: " + ex.getMessage());
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

        btnBack = new PillButton("Quay lai", INPUT_BG, TEXT_DARK);
        btnBack.setPreferredSize(new Dimension(110, 36));
        btnBack.setVisible(false);
        btnBack.addActionListener(e -> { if (currentStep > 1) showStep(currentStep - 1); });

        btnNext = new PillButton("Tiep theo", ACCENT, WHITE);
        btnNext.setPreferredSize(new Dimension(120, 36));
        btnNext.addActionListener(e -> {
            if (currentStep == 1) goToStep2();
            else if (currentStep == 2) goToStep3();
        });

        btnConfirm = new PillButton("Xac nhan tra", BTN_GREEN, TEXT_DARK);
        btnConfirm.setPreferredSize(new Dimension(150, 36));
        btnConfirm.setVisible(false);
        btnConfirm.addActionListener(e -> doConfirmReturn());

        PillButton btnCancel = new PillButton("Huy", BTN_RED, WHITE);
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
        if (row < 0) { showMsg("Vui long chon mot phieu thue!"); return; }
        if (searchResults == null || row >= searchResults.size()) return;
        selectedPhieu = searchResults.get(row);
        loadStep2Data();
        showStep(2);
    }

    private void goToStep3() {
        try {
            double val = Double.parseDouble(
                txtChiPhiHuHong.getText().trim().replace(",", ""));
            if (val < 0) { showMsg("Chi phi hu hong khong duoc am!"); return; }
        } catch (NumberFormatException e) {
            showMsg("Chi phi hu hong phai la so!"); return;
        }
        updateStep3Summary();
        showStep(3);
    }

    // =========================================================
    // XAC NHAN TRA CD
    //
    // LOGIC TINH TIEN:
    //   - Doc DonGiaThue tu CTPhieuThue (khong tinh lai tu ngay)
    //   - chiPhiHuHong lay tu input
    //   - Service.returnCD(maPT, ngayTra, chiPhiHuHong):
    //       TienPhat = phatTreHan(tu DB) + chiPhiHuHong
    // =========================================================
    private void doConfirmReturn() {
        try {
            // Tinh tong thue goc tu DonGiaThue (da luu san, khong tinh lai)
            double tienThueGoc = 0;
            int    diemDaGiam  = 0;
            if (selectedPhieu.getDanhSachChiTiet() != null) {
                for (PhieuThue.CTPhieuThue ct : selectedPhieu.getDanhSachChiTiet()) {
                    tienThueGoc += ct.getDonGiaThue();
                    String tt = ct.getTinhTrang();
                    if (tt != null && tt.contains("|diem=")) {
                        try {
                            diemDaGiam += Integer.parseInt(tt.split("\\|diem=")[1].trim());
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            double giamDiem    = diemDaGiam * 1_000;
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
            if      (delta > 0) ketQua = String.format("Thu them tu khach:  %,.0f VND", delta);
            else if (delta < 0) ketQua = String.format("Hoan lai cho khach:  %,.0f VND", -delta);
            else                ketQua = "Hoa nhau";

            String msg = String.format(
                "Xac nhan thanh toan va hoan tat tra?\n\n"
              + "  Tien thue goc  : %,.0f VND\n"
              + "  Giam tu diem   : %,.0f VND\n"
              + "  Phat tre han   : %,.0f VND\n"
              + "  Chi phi hu hong: %,.0f VND\n"
              + "  Tong phai tra  : %,.0f VND\n"
              + "  Tien coc       : %,.0f VND\n"
              + "  ---------------------------\n"
              + "  %s",
                tienThueGoc, giamDiem, phatTre, chiPhiHuHong,
                tongPhaiTra, coc, ketQua);

            int confirm = JOptionPane.showConfirmDialog(this, msg,
                "Xac nhan thanh toan", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            // Ghi DB: service tu tinh lai phatTreHan + cong chiPhiHuHong → luu TienPhat
            boolean ok = service.returnCD(selectedPhieu.getMaPT(), ngayTraThucTe, chiPhiHuHong);

            if (ok) {
                // Cong diem tich luy: 10.000 VND = 1 diem (tinh tren tienThueNet)
                int diemTichLuy = (int) (tienThueNet / 10_000);
                if (selectedPhieu.getMaKH() > 0 && diemTichLuy > 0) {
                    new otkhongluong.gamestoremanagement.dao.KhachHangDAO()
                        .updatePoint(selectedPhieu.getMaKH(), diemTichLuy);
                }

                JOptionPane.showMessageDialog(this,
                    String.format(
                        "Tra CD thanh cong!\n\n"
                      + "  Phieu thue -> Da Tra\n"
                      + "  CD -> San sang\n"
                      + "  Diem tich luy cong: +%d diem\n\n"
                      + "  %s",
                        diemTichLuy, ketQua),
                    "Hoan tat", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                showMsg("Tra that bai! Vui long thu lai hoac lien he quan tri.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showMsg("Loi xu ly: " + ex.getMessage());
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================
    private void showMsg(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thong bao", JOptionPane.INFORMATION_MESSAGE);
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
    // PILL BUTTON — dong nhat voi RentAddDialog
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