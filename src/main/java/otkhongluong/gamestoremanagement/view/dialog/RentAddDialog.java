package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.dao.DiscDAO;
import otkhongluong.gamestoremanagement.dao.CustomerDAO;
import otkhongluong.gamestoremanagement.model.Customer;
import otkhongluong.gamestoremanagement.model.RentalOrder;
import otkhongluong.gamestoremanagement.service.RentalService;
import otkhongluong.gamestoremanagement.util.Session;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * RentAddDialog — Wizard 3 bước thuê CD/Game
 *
 * Bước 1 : Chọn game (danh sách CD sẵn sàng)
 * Bước 2 : Thông tin khách hàng + thời gian + điểm + cọc
 * Bước 3 : Xác nhận & tạo phiếu thuê
 *
 * LOGIC:
 *   CTPHIEUTHUE.DonGiaThue = tổng thuê gốc = GiaThueNgay x SoNgay
 *   Điểm dùng giảm lưu vào TinhTrang = "OK|diem=N"
 *   1 điểm = 5.000 VNĐ, không được giảm quá số tiền thuê
 */
public class RentAddDialog extends JDialog {

    // =========================================================
    // PALETTE
    // =========================================================
    private static final Color BG            = new Color(28, 16, 72);
    private static final Color BG_PANEL      = new Color(38, 24, 90);
    private static final Color BG_CARD       = new Color(52, 34, 118);
    private static final Color ACCENT        = new Color(124, 92, 220);
    private static final Color ACCENT_LIGHT  = new Color(168, 144, 255);
    private static final Color DIVIDER       = new Color(72, 52, 148);
    private static final Color WHITE         = Color.WHITE;
    private static final Color TEXT_MAIN     = new Color(235, 230, 255);
    private static final Color TEXT_MUTED    = new Color(148, 132, 196);
    private static final Color TEXT_DARK     = new Color(28, 16, 72);
    private static final Color INPUT_BG      = new Color(245, 242, 255);
    private static final Color SUCCESS       = new Color(72, 199, 142);
    private static final Color DANGER        = new Color(240, 80, 80);
    private static final Color GOLD          = new Color(255, 196, 64);
    private static final Color BTN_GREEN     = new Color(72, 199, 142);
    private static final Color BTN_RED       = new Color(240, 80, 80);

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
    // CONSTANTS
    // =========================================================
    private static final double COC_MIN      = 250_000;
    private static final double COC_MULTIPLY = 2.0;
    private static final int    DIEM_TO_VND  = 5_000; // 1 điểm = 5.000 VNĐ

    // =========================================================
    // DAO / SERVICE
    // =========================================================
    private final DiscDAO        cdDAO   = new DiscDAO();
    private final CustomerDAO khDAO   = new CustomerDAO();
    private final RentalService  service = new RentalService();

    // =========================================================
    // STATE
    // =========================================================
    private int currentStep = 1;

    // Bước 1
    JTable            tblCD;
    private DefaultTableModel tblCDModel;
    List<Object[]>    cdList;

    private int    selectedMaCD    = -1;
    private String selectedTenGame = "";
    private double selectedGia     = 0;   // GiaThueNgay (đơn giá/ngày)

    // Bước 2
    private JTextField txtSDT;
    private JLabel     lblTenKH, lblDiemHienCo;
    private JTextField txtSoNgay, txtDiemSuDung;
    private JLabel     lblInfoTongThueGoc, lblInfoGiamDiem, lblInfoTongPhaiTra, lblInfoTienCoc;

    private Customer currentKH  = null;
    private int       maKH       = -1;
    private int       diemHienCo = 0;

    // Bước 3 — info cards
    private JLabel cardMaCD, cardGame, cardGiaNgay;

    // Bước 3 — summary labels
    private JLabel lblS3CD, lblS3Game, lblS3KH;
    private JLabel lblS3NgayThue, lblS3NgayTra, lblS3SoNgay;
    private JLabel lblS3TongGoc, lblS3DiemTru, lblS3TongSauGiam, lblS3Coc;

    // =========================================================
    // LAYOUT
    // =========================================================
    private JPanel  contentPanel;
    private JPanel  stepIndicator;
    private JButton btnBack, btnNext, btnConfirm;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public RentAddDialog(Frame parent) {
        super(parent, "Thuê CD / Game", true);
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
    }

    // =========================================================
    // HEADER
    // =========================================================
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(16, 20, 0, 20));

        JLabel title = new JLabel("THUÊ CD / GAME");
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
        String[] labels = {"1. Chọn game", "2. Chi tiết", "3. Xác nhận"};
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
    // BƯỚC 1 — Danh sách CD sẵn sàng
    // =========================================================
    private JPanel buildStep1() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(14, 20, 0, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG);

        JLabel lbl = new JLabel("Danh sách CD sẵn sàng cho thuê:");
        lbl.setFont(F_SECTION);
        lbl.setForeground(TEXT_MAIN);

        PillButton btnRefresh = new PillButton("Làm mới", ACCENT, WHITE);
        btnRefresh.setPreferredSize(new Dimension(100, 32));
        btnRefresh.addActionListener(e -> loadCDTable());

        topBar.add(lbl, BorderLayout.WEST);
        topBar.add(btnRefresh, BorderLayout.EAST);
        p.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Mã CD", "Tên Game", "Giá thuê / ngày"};
        tblCDModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblCD = makeTable(tblCDModel);
        tblCD.getColumnModel().getColumn(0).setPreferredWidth(70);
        tblCD.getColumnModel().getColumn(1).setPreferredWidth(340);
        tblCD.getColumnModel().getColumn(2).setPreferredWidth(160);
        tblCD.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblCD.getSelectedRow() >= 0) goToStep2();
            }
        });

        loadCDTable();
        p.add(wrapScroll(tblCD), BorderLayout.CENTER);

        JLabel hint = new JLabel("Chọn game rồi nhấn \"Tiếp theo\", hoặc double-click để sang bước tiếp.");
        hint.setFont(F_HINT);
        hint.setForeground(TEXT_MUTED);
        p.add(hint, BorderLayout.SOUTH);

        return p;
    }

    private void loadCDTable() {
        tblCDModel.setRowCount(0);
        cdList = cdDAO.getAllAvailableCD();
        for (Object[] row : cdList) {
            int    maCD = (int)    row[0];
            String ten  = (String) row[1];
            double gia  = (double) row[2];
            tblCDModel.addRow(new Object[]{
                "CD" + maCD,
                ten,
                String.format("%,.0f VNĐ / ngày", gia)
            });
        }
    }

    // =========================================================
    // BƯỚC 2 — Thông tin thuê
    // =========================================================
    private JPanel buildStep2() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(14, 20, 0, 20));

        // --- Info cards game đã chọn ---
        cardMaCD    = new JLabel("-");
        cardGame    = new JLabel("-");
        cardGiaNgay = new JLabel("-");

        JPanel cards = new JPanel(new GridLayout(1, 3, 10, 0));
        cards.setBackground(BG);
        cards.setPreferredSize(new Dimension(0, 68));
        cards.add(infoCard("Mã CD",            cardMaCD));
        cards.add(infoCard("Tên game",         cardGame));
        cards.add(infoCard("Giá thuê / ngày",  cardGiaNgay));
        p.add(cards, BorderLayout.NORTH);

        // --- Form ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 0, 5, 10);

        int row = 0;

        // SĐT + nút tìm + nút tạo
        txtSDT = makeInput();
        PillButton btnTim = new PillButton("Tìm", ACCENT, WHITE);
        btnTim.setPreferredSize(new Dimension(80, 34));
        btnTim.addActionListener(e -> doFindKH());

        PillButton btnTao = new PillButton("Tạo mới", SUCCESS, TEXT_DARK);
        btnTao.setPreferredSize(new Dimension(100, 34));
        btnTao.addActionListener(e -> doCreateKH());

        txtSDT.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { doFindKH(); }
            public void keyPressed(KeyEvent e)  { if (e.getKeyCode() == KeyEvent.VK_ENTER) doFindKH(); }
        });

        addLabel(form, gc, "Số điện thoại KH:", row, 0, 130);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 1;
        form.add(txtSDT, gc);
        gc.gridx = 2; gc.weightx = 0;
        form.add(btnTim, gc);
        gc.gridx = 3;
        form.add(btnTao, gc);
        gc.gridwidth = 1;

        // Tên KH
        row++;
        lblTenKH = new JLabel("---");
        lblTenKH.setFont(F_VALUE);
        lblTenKH.setForeground(TEXT_MAIN);
        addLabel(form, gc, "Tên khách hàng:", row, 0, 130);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 3;
        form.add(lblTenKH, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        // Điểm hiện có
        row++;
        lblDiemHienCo = new JLabel("---");
        lblDiemHienCo.setFont(F_VALUE);
        lblDiemHienCo.setForeground(GOLD);
        addLabel(form, gc, "Điểm tích lũy:", row, 0, 130);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 3;
        form.add(lblDiemHienCo, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        // Đường kẻ
        row++;
        addDivider(form, gc, row, 4);

        // Số ngày thuê
        row++;
        txtSoNgay = makeInput();
        txtSoNgay.setText("1");
        txtSoNgay.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { recalc(); }
        });
        addLabel(form, gc, "Số ngày thuê:", row, 0, 130);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 3;
        form.add(txtSoNgay, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        // Điểm sử dụng
        row++;
        txtDiemSuDung = makeInput();
        txtDiemSuDung.setText("0");
        txtDiemSuDung.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { recalc(); }
        });
        addLabel(form, gc, "Điểm muốn dùng:", row, 0, 130);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 3;
        form.add(txtDiemSuDung, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        // Đường kẻ
        row++;
        addDivider(form, gc, row, 4);

        // --- Kết quả tính (4 dòng) ---
        row++;
        lblInfoTongThueGoc = resultLabel("---", TEXT_MAIN);
        addLabel(form, gc, "Tổng thuê gốc:", row, 0, 130);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 3;
        form.add(lblInfoTongThueGoc, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        row++;
        lblInfoGiamDiem = resultLabel("---", SUCCESS);
        addLabel(form, gc, "Giảm từ điểm:", row, 0, 130);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 3;
        form.add(lblInfoGiamDiem, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        row++;
        lblInfoTongPhaiTra = resultLabel("---", ACCENT_LIGHT);
        addLabel(form, gc, "Tổng tiền thuê phải trả:", row, 0, 130);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 3;
        form.add(lblInfoTongPhaiTra, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        row++;
        lblInfoTienCoc = resultLabel("---", GOLD);
        addLabel(form, gc, "Tiền cọc thu ngay:", row, 0, 130);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 3;
        form.add(lblInfoTienCoc, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        // Filler
        row++;
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 4; gc.weighty = 1.0;
        gc.fill = GridBagConstraints.BOTH;
        form.add(Box.createVerticalGlue(), gc);
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weighty = 0;

        p.add(form, BorderLayout.CENTER);
        return p;
    }

    private JLabel resultLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(F_VALUE);
        l.setForeground(color);
        return l;
    }

    private void addLabel(JPanel form, GridBagConstraints gc, String text, int row, int col, int w) {
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

    /** Load dữ liệu game đã chọn vào step 2 */
    private void loadStep2Data() {
        cardMaCD.setText("CD" + selectedMaCD);
        cardGame.setText(selectedTenGame);
        cardGiaNgay.setText(String.format("%,.0f VNĐ", selectedGia));

        txtSDT.setText("");
        txtSoNgay.setText("1");
        txtDiemSuDung.setText("0");
        lblTenKH.setText("Chưa tìm kiếm");
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
            // Kiểm tra CCCD
            if (kh.getCccd() == null || kh.getCccd().trim().isEmpty()) {
                lblTenKH.setForeground(GOLD);
                lblTenKH.setText(kh.getHoTen() + "  ⚠ Chưa có CCCD");
            } else {
                lblTenKH.setForeground(SUCCESS);
            }
            lblDiemHienCo.setText(diemHienCo + " điểm  ~  "
                + String.format("%,.0f VNĐ", (double) diemHienCo * DIEM_TO_VND));
        } else {
            currentKH = null; maKH = -1; diemHienCo = 0;
            lblTenKH.setText("Không tìm thấy  —  Nhấn \"Tạo mới\"");
            lblTenKH.setForeground(DANGER);
            lblDiemHienCo.setText("0 điểm");
        }
        recalc();
    }

    /**
     * Xử lý tạo mới hoặc cập nhật CCCD cho khách hàng đã tồn tại nhưng chưa có CCCD.
     * Nếu KH chưa tồn tại: tạo mới hoàn toàn.
     * Nếu KH tồn tại nhưng chưa có CCCD: yêu cầu nhập CCCD.
     */
    private void doCreateKH() {
        String sdt = txtSDT.getText().trim();
        if (sdt.isEmpty()) { showMsg("Vui lòng nhập số điện thoại trước!"); return; }

        Customer existing = khDAO.findBySDT(sdt);

        // Trường hợp KH đã tồn tại nhưng chưa có CCCD → yêu cầu cập nhật CCCD
        if (existing != null && (existing.getCccd() == null || existing.getCccd().trim().isEmpty())) {
            JTextField fldTen  = new JTextField(existing.getHoTen(), 20);
            JTextField fldCCCD = new JTextField(20);

            JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
            panel.setBorder(new EmptyBorder(8, 8, 8, 8));
            panel.add(new JLabel("Số điện thoại:")); panel.add(new JLabel(sdt));
            panel.add(new JLabel("Họ và tên (*):"));  panel.add(fldTen);
            panel.add(new JLabel("CCCD (*):"));       panel.add(fldCCCD);
            panel.add(new JLabel(""));
            panel.add(new JLabel("<html><i style='color:gray'>9 hoặc 12 chữ số</i></html>"));

            int result = JOptionPane.showConfirmDialog(this, panel,
                "Cập nhật thông tin khách hàng", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) return;

            String hoTen = fldTen.getText().trim();
            String cccd  = fldCCCD.getText().trim();
            if (hoTen.isEmpty()) { showMsg("Họ và tên không được để trống!"); return; }
            if (!cccd.matches("\\d{9}|\\d{12}")) {
                showMsg("CCCD phải có 9 hoặc 12 chữ số!"); return;
            }

            existing.setHoTen(hoTen);
            existing.setCccd(cccd);
            if (khDAO.update(existing)) {
                currentKH = khDAO.findBySDT(sdt);
                maKH = currentKH.getMaKH();
                diemHienCo = currentKH.getDiemTichLuy();
                lblTenKH.setText(currentKH.getHoTen());
                lblTenKH.setForeground(SUCCESS);
                lblDiemHienCo.setText(diemHienCo + " điểm  ~  "
                    + String.format("%,.0f VNĐ", (double) diemHienCo * DIEM_TO_VND));
                showMsg("Cập nhật tên và CCCD thành công!");
            } else {
                showMsg("Cập nhật CCCD thất bại!");
            }
            recalc();
            return;
        }

        // Trường hợp KH đã tồn tại và đã có CCCD
        if (existing != null) {
            showMsg("Khách hàng với SĐT " + sdt + " đã tồn tại!");
            currentKH = existing; maKH = existing.getMaKH(); diemHienCo = existing.getDiemTichLuy();
            lblTenKH.setText(existing.getHoTen()); lblTenKH.setForeground(SUCCESS);
            lblDiemHienCo.setText(diemHienCo + " điểm  ~  "
                + String.format("%,.0f VNĐ", (double) diemHienCo * DIEM_TO_VND));
            recalc(); return;
        }

        // Trường hợp KH chưa tồn tại → tạo mới
        JTextField fldTen  = new JTextField(20);
        JTextField fldCCCD = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(new JLabel("Số điện thoại:"));  panel.add(new JLabel(sdt));
        panel.add(new JLabel("Họ và tên (*):"));  panel.add(fldTen);
        panel.add(new JLabel("CCCD (*):"));       panel.add(fldCCCD);
        panel.add(new JLabel(""));
        panel.add(new JLabel("<html><i style='color:gray'>9 hoặc 12 chữ số</i></html>"));

        int result = JOptionPane.showConfirmDialog(this, panel, "Tạo khách hàng mới",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String hoTen = fldTen.getText().trim();
        String cccd  = fldCCCD.getText().trim();
        if (hoTen.isEmpty()) { showMsg("Họ và tên không được để trống!"); return; }
        if (!cccd.matches("\\d{9}|\\d{12}")) { showMsg("CCCD phải có 9 hoặc 12 chữ số!"); return; }

        Customer kh = new Customer();
        kh.setHoTen(hoTen); kh.setSdt(sdt); kh.setCccd(cccd); kh.setDiemTichLuy(0);
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

    /** Tính lại các giá trị hiển thị (mỗi khi thay đổi số ngày / điểm) */
    private void recalc() {
        try {
            int soNgay = Math.max(1, Integer.parseInt(txtSoNgay.getText().trim()));

            int diemDung = 0;
            try { diemDung = Math.max(0, Integer.parseInt(txtDiemSuDung.getText().trim())); }
            catch (NumberFormatException ignored) {}
            if (diemDung > diemHienCo) {
                diemDung = diemHienCo;
                txtDiemSuDung.setText(String.valueOf(diemDung));
            }

            // Tổng thuê gốc = GiaThueNgay x SoNgay
            double tongThueGoc = selectedGia * soNgay;

            // Giảm điểm không được vượt quá tiền thuê gốc
            double giamDiem = Math.min((double) diemDung * DIEM_TO_VND, tongThueGoc);

            // Nếu điểm nhập vượt quá, tính lại số điểm thực sự dùng
            int diemThucDung = (int) Math.floor(giamDiem / DIEM_TO_VND);
            giamDiem = diemThucDung * DIEM_TO_VND;

            double tongPhaiTra = Math.max(0, tongThueGoc - giamDiem);
            double tienCoc = Math.max(COC_MIN, tongThueGoc * COC_MULTIPLY);

            lblInfoTongThueGoc.setText(String.format("%,.0f VNĐ  (%,.0f x %d ngày)", tongThueGoc, selectedGia, soNgay));
            lblInfoTongThueGoc.setForeground(TEXT_MAIN);

            if (diemThucDung > 0) {
                lblInfoGiamDiem.setText(String.format("-%,.0f VNĐ  (%d điểm)", giamDiem, diemThucDung));
                lblInfoGiamDiem.setForeground(SUCCESS);
            } else {
                lblInfoGiamDiem.setText("Không sử dụng điểm");
                lblInfoGiamDiem.setForeground(TEXT_MUTED);
            }

            lblInfoTongPhaiTra.setText(String.format("%,.0f VNĐ", tongPhaiTra));
            lblInfoTongPhaiTra.setForeground(ACCENT_LIGHT);

            lblInfoTienCoc.setText(String.format("%,.0f VNĐ  (thu ngay bây giờ)", tienCoc));
            lblInfoTienCoc.setForeground(GOLD);

        } catch (NumberFormatException ex) {
            lblInfoTongThueGoc.setText("Nhập số hợp lệ");
            lblInfoTienCoc.setText("---");
        }
    }

    // =========================================================
    // BƯỚC 3 — Xác nhận
    // =========================================================
    private JPanel buildStep3() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(16, 20, 10, 20));

        lblS3CD      = sumValue();
        lblS3Game    = sumValue();
        lblS3KH      = sumValue();
        lblS3NgayThue= sumValue();
        lblS3NgayTra = sumValue();
        lblS3SoNgay  = sumValue();
        lblS3TongGoc = sumValue();
        lblS3DiemTru = sumValue();
        lblS3TongSauGiam = sumValue();
        lblS3TongSauGiam.setForeground(ACCENT_LIGHT);
        lblS3Coc     = sumValue();
        lblS3Coc.setFont(F_MONEY);
        lblS3Coc.setForeground(GOLD);

        // 10 dòng
        JPanel card = new JPanel(new GridLayout(10, 2, 8, 10));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(DIVIDER, 1, true),
            new EmptyBorder(16, 20, 16, 20)
        ));

        card.add(sumKey("Mã CD:"));              card.add(lblS3CD);
        card.add(sumKey("Tên game:"));           card.add(lblS3Game);
        card.add(sumKey("Khách hàng:"));         card.add(lblS3KH);
        card.add(sumKey("Ngày thuê:"));          card.add(lblS3NgayThue);
        card.add(sumKey("Ngày trả dự kiến:"));   card.add(lblS3NgayTra);
        card.add(sumKey("Số ngày / Giá:"));      card.add(lblS3SoNgay);
        card.add(sumKey("Tổng thuê gốc:"));      card.add(lblS3TongGoc);
        card.add(sumKey("Giảm từ điểm:"));       card.add(lblS3DiemTru);
        card.add(sumKey("Tiền thuê sau giảm:"));  card.add(lblS3TongSauGiam);
        card.add(sumKey("Tiền cọc thu ngay:"));  card.add(lblS3Coc);

        JLabel notice = new JLabel(
            "<html><center>Sau khi xác nhận:<br>"
          + "Phiếu thuê mới → Đang thuê  |  CD → Đang thuê  |  Trừ điểm (nếu có)</center></html>"
        );
        notice.setFont(F_HINT);
        notice.setForeground(TEXT_MUTED);
        notice.setHorizontalAlignment(SwingConstants.CENTER);
        notice.setBorder(new EmptyBorder(4, 0, 0, 0));

        p.add(card, BorderLayout.CENTER);
        p.add(notice, BorderLayout.SOUTH);
        return p;
    }

    private JLabel sumValue() {
        JLabel l = new JLabel("-");
        l.setFont(F_VALUE);
        l.setForeground(TEXT_MAIN);
        return l;
    }

    private JLabel sumKey(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_LABEL);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    private void updateStep3Summary() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDateTime now = LocalDateTime.now();

        int soNgay = 1;
        try { soNgay = Math.max(1, Integer.parseInt(txtSoNgay.getText().trim())); }
        catch (NumberFormatException ignored) {}

        int diemDung = 0;
        try { diemDung = Math.max(0, Integer.parseInt(txtDiemSuDung.getText().trim())); }
        catch (NumberFormatException ignored) {}
        if (diemDung > diemHienCo) diemDung = diemHienCo;

        double tongThueGoc = selectedGia * soNgay;
        double giamDiem    = Math.min((double) diemDung * DIEM_TO_VND, tongThueGoc);
        int    diemThucDung = (int) Math.floor(giamDiem / DIEM_TO_VND);
        giamDiem = diemThucDung * DIEM_TO_VND;
        double tienCoc     = Math.max(COC_MIN, tongThueGoc * COC_MULTIPLY);

        lblS3CD.setText("CD" + selectedMaCD);
        lblS3Game.setText(selectedTenGame);
        lblS3KH.setText(currentKH != null ? currentKH.getHoTen() + "  (KH" + maKH + ")" : "Khách vãng lai");
        lblS3NgayThue.setText(now.format(fmt));
        lblS3NgayTra.setText(now.plusDays(soNgay).format(fmt));
        lblS3SoNgay.setText(soNgay + " ngày  x  " + String.format("%,.0f VNĐ", selectedGia) + " / ngày");
        lblS3TongGoc.setText(String.format("%,.0f VNĐ  (lưu vào phiếu thuê)", tongThueGoc));

        if (diemThucDung > 0) {
            lblS3DiemTru.setText(String.format("-%,.0f VNĐ  (%d điểm)", giamDiem, diemThucDung));
            lblS3DiemTru.setForeground(SUCCESS);
        } else {
            lblS3DiemTru.setText("Không sử dụng điểm");
            lblS3DiemTru.setForeground(TEXT_MUTED);
        }

        double tongSauGiam = Math.max(0, tongThueGoc - giamDiem);
        lblS3TongSauGiam.setText(String.format("%,.0f VNĐ", tongSauGiam));
        lblS3TongSauGiam.setForeground(ACCENT_LIGHT);

        lblS3Coc.setText(String.format("%,.0f VNĐ  (thu ngay bây giờ)", tienCoc));
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

        btnConfirm = new PillButton("Xác nhận thuê", BTN_GREEN, TEXT_DARK);
        btnConfirm.setPreferredSize(new Dimension(150, 36));
        btnConfirm.setVisible(false);
        btnConfirm.addActionListener(e -> doConfirmRent());

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
        btnNext.setVisible(step < 3);
        btnConfirm.setVisible(step == 3);
        updateStepIndicator(step);
    }

    private void goToStep2() {
        int row = tblCD.getSelectedRow();
        if (row < 0 || cdList == null || row >= cdList.size()) {
            showMsg("Vui lòng chọn một game!"); return;
        }
        Object[] cd = cdList.get(row);
        selectedMaCD    = (int)    cd[0];
        selectedTenGame = (String) cd[1];
        selectedGia     = (double) cd[2];
        if (selectedGia <= 0) {
            showMsg("CD này không có thông tin giá thuê, vui lòng chọn CD khác!"); return;
        }
        loadStep2Data();
        showStep(2);
    }

    private void goToStep3() {
        int soNgay;
        try {
            soNgay = Integer.parseInt(txtSoNgay.getText().trim());
            if (soNgay <= 0) { showMsg("Số ngày thuê phải lớn hơn 0!"); return; }
        } catch (NumberFormatException e) {
            showMsg("Số ngày thuê phải là số nguyên!"); return;
        }

        int diemDung;
        try {
            diemDung = Integer.parseInt(txtDiemSuDung.getText().trim());
            if (diemDung < 0) { showMsg("Điểm sử dụng không được âm!"); return; }
        } catch (NumberFormatException e) {
            showMsg("Điểm sử dụng phải là số nguyên!"); return;
        }

        if (diemDung > diemHienCo) {
            showMsg("Không đủ điểm! Hiện có: " + diemHienCo + ", muốn dùng: " + diemDung);
            txtDiemSuDung.setText(String.valueOf(diemHienCo));
            recalc(); return;
        }

        // ✅ Kiểm tra CCCD trước khi sang bước 3
        if (currentKH != null && (currentKH.getCccd() == null || currentKH.getCccd().trim().isEmpty())) {
            showMsg("Khách hàng \"" + currentKH.getHoTen() + "\" chưa có CCCD!\n"
                  + "Vui lòng nhấn \"Tạo mới\" để cập nhật CCCD trước khi tiếp tục.");
            return;
        }

        updateStep3Summary();
        showStep(3);
    }

    // =========================================================
    // XÁC NHẬN — Tạo phiếu thuê
    // =========================================================
    private void doConfirmRent() {
        try {
            int soNgay = Integer.parseInt(txtSoNgay.getText().trim());
            int diemDung = 0;
            try { diemDung = Math.max(0, Integer.parseInt(txtDiemSuDung.getText().trim())); }
            catch (NumberFormatException ignored) {}
            if (diemDung > diemHienCo) diemDung = diemHienCo;

            double tongThueGoc = selectedGia * soNgay;

            // Giảm điểm không vượt quá tiền thuê gốc
            double giamDiem    = Math.min((double) diemDung * DIEM_TO_VND, tongThueGoc);
            int    diemThucDung = (int) Math.floor(giamDiem / DIEM_TO_VND);
            giamDiem = diemThucDung * DIEM_TO_VND;

            double tienCoc = Math.max(COC_MIN, tongThueGoc * COC_MULTIPLY);

            // Xử lý khách vãng lai
            if (maKH == -1) {
                int yn = JOptionPane.showConfirmDialog(this,
                    "Khách hàng chưa xác định.\n\n"
                  + "[Có]    Tạo tự động Khách vãng lai (không tích điểm)\n"
                  + "[Không] Quay lại nhập SĐT / tạo KH",
                    "Khách vãng lai?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (yn != JOptionPane.YES_OPTION) { showStep(2); return; }

                Customer kh = new Customer();
                kh.setHoTen("Khách vãng lai");
                kh.setSdt(null);
                kh.setDiemTichLuy(0);
                if (!khDAO.insert(kh)) { showMsg("Không thể tạo khách vãng lai!"); return; }
                Customer newKH = khDAO.findBySDT(kh.getSdt());
                if (newKH == null) { showMsg("Lỗi tạo khách!"); return; }
                maKH = newKH.getMaKH();
                diemThucDung = 0;
                giamDiem     = 0;
            }

            // Popup xác nhận
            double tongPhaiTra = Math.max(0, tongThueGoc - giamDiem);
            String msg = String.format(
                "Xác nhận thu cọc và tạo phiếu thuê?\n\n"
              + "  CD            : CD%d  -  %s\n"
              + "  Khách hàng    : %s\n"
              + "  Thời gian     : %d ngày\n"
              + "  Tổng thuê gốc : %,.0f VNĐ  (lưu vào phiếu thuê)\n"
              + "  Giảm điểm     : %,.0f VNĐ  (%d điểm)\n"
              + "  Tổng tiền thuê phải trả: %,.0f VNĐ\n\n"
              + ">>> Thu cọc ngay: %,.0f VNĐ <<<",
                selectedMaCD, selectedTenGame,
                currentKH != null ? currentKH.getHoTen() : "Khách vãng lai",
                soNgay,
                tongThueGoc, giamDiem, diemThucDung, tongPhaiTra,
                tienCoc);

            int confirm = JOptionPane.showConfirmDialog(this, msg,
                "Xác nhận thu cọc", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            LocalDateTime now = LocalDateTime.now();

            RentalOrder pt = new RentalOrder();
            pt.setMaKH(maKH);
            pt.setMaNV(Session.getMaNV());
            pt.setNgayThue(now);
            pt.setNgayTraDuKien(now.plusDays(soNgay));
            pt.setTienCoc(tienCoc);
            pt.setTrangThai("DangThue");

            // TinhTrang lưu điểm đã sử dụng để RentReturnDialog đọc lại
            String tinhTrang = diemThucDung > 0 ? ("OK|diem=" + diemThucDung) : "OK";

            RentalOrder.CTPhieuThue ct =
                new RentalOrder.CTPhieuThue(selectedMaCD, selectedTenGame, tongThueGoc, tinhTrang);
            ct.setMaNV(Session.getMaNV());
            pt.getDanhSachChiTiet().add(ct);

            boolean ok = service.createPhieuThue(pt);
            if (ok) {
                if (maKH > 0 && diemThucDung > 0) {
                    khDAO.updatePoint(maKH, -diemThucDung);

                    // Ghi lịch sử trừ điểm vào DIEM_LICHSU
                    try (java.sql.Connection con =
                             otkhongluong.gamestoremanagement.util.DBConnection.getConnection();
                         java.sql.PreparedStatement psLog = con.prepareStatement(
                             "INSERT INTO DIEM_LICHSU (MaKH, Loai, SoDiem, GhiChu, MaPT) VALUES (?, 'TRU', ?, ?, ?)")) {
                        psLog.setInt(1, maKH);
                        psLog.setInt(2, diemThucDung);
                        psLog.setString(3, "Thuê CD - CD" + selectedMaCD + " (" + selectedTenGame + ")");
                        psLog.setInt(4, pt.getMaPT());
                        psLog.executeUpdate();
                    } catch (Exception exLog) {
                        exLog.printStackTrace();
                    }
                }
                JOptionPane.showMessageDialog(this,
                    String.format(
                        "Tạo phiếu thuê thành công!\n\n"
                      + "  Đã thu cọc: %,.0f VNĐ\n"
                      + "  CD%d → Đang thuê\n\n"
                      + "Vui lòng giao CD cho khách hàng.",
                        tienCoc, selectedMaCD),
                    "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                showMsg("Tạo phiếu thuê thất bại!\n"
                      + "CD có thể đã được thuê bởi giao dịch khác.\n"
                      + "Vui lòng làm mới danh sách và thử lại.");
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
    
    public static void openAndPreselectCD(Frame parent, int maCD) {
        RentAddDialog dlg = new RentAddDialog(parent);
 
        SwingUtilities.invokeLater(() -> {
            if (dlg.cdList != null) {
                for (int i = 0; i < dlg.cdList.size(); i++) {
                    if ((int) dlg.cdList.get(i)[0] == maCD) {
                        dlg.tblCD.setRowSelectionInterval(i, i);
                        dlg.tblCD.scrollRectToVisible(dlg.tblCD.getCellRect(i, 0, true));
                        break;
                    }
                }
            }
        });
 
        dlg.setVisible(true);
    }
 
    /**
     * Mở RentAddDialog và tự động chọn sẵn CD đầu tiên khớp tên game.
     * Dùng khi chỉ biết tên game, không biết maCD cụ thể.
     *
     * @param parent  Frame cha
     * @param tenGame Tên game (so khớp một phần, không phân biệt hoa thường)
     */
    public static void openAndPreselectByGameName(Frame parent, String tenGame) {
        RentAddDialog dlg = new RentAddDialog(parent);
 
        SwingUtilities.invokeLater(() -> {
            if (dlg.cdList != null && tenGame != null) {
                String keyword = tenGame.trim().toLowerCase();
                for (int i = 0; i < dlg.cdList.size(); i++) {
                    String rowName = ((String) dlg.cdList.get(i)[1]).trim().toLowerCase();
                    if (rowName.contains(keyword) || keyword.contains(rowName)) {
                        dlg.tblCD.setRowSelectionInterval(i, i);
                        dlg.tblCD.scrollRectToVisible(dlg.tblCD.getCellRect(i, 0, true));
                        break;
                    }
                }
            }
        });
 
        dlg.setVisible(true);
    }
}