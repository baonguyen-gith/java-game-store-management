package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.dao.CDDAO;
import otkhongluong.gamestoremanagement.dao.KhachHangDAO;
import otkhongluong.gamestoremanagement.model.KhachHang;
import otkhongluong.gamestoremanagement.model.PhieuThue;
import otkhongluong.gamestoremanagement.service.ThueService;
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
 * RentAddDialog — Wizard 3 buoc thue CD/Game
 *
 * Buoc 1 : Chon game (danh sach CD san sang)
 * Buoc 2 : Thong tin khach hang + thoi gian + diem + coc
 * Buoc 3 : Xac nhan & tao phieu thue
 *
 * LOGIC MOI:
 *   CTPHIEUTHUE.DonGiaThue = tong thue goc = GiaThueNgay x SoNgay
 *   (chua tru diem, thu khi tra CD)
 *   => Khi gia han, chi can cong them vao DonGiaThue, khong bi tinh lai.
 *   Diem dung giam luu vao TinhTrang = "OK|diem=N"
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
    private static final int    DIEM_TO_VND  = 1_000;

    // =========================================================
    // DAO / SERVICE
    // =========================================================
    private final CDDAO        cdDAO   = new CDDAO();
    private final KhachHangDAO khDAO   = new KhachHangDAO();
    private final ThueService  service = new ThueService();

    // =========================================================
    // STATE
    // =========================================================
    private int currentStep = 1;

    // Buoc 1
    private JTable            tblCD;
    private DefaultTableModel tblCDModel;
    private List<Object[]>    cdList;

    private int    selectedMaCD    = -1;
    private String selectedTenGame = "";
    private double selectedGia     = 0;   // GiaThueNgay (don gia/ngay)

    // Buoc 2
    private JTextField txtSDT;
    private JLabel     lblTenKH, lblDiemHienCo;
    private JTextField txtSoNgay, txtDiemSuDung;
    private JLabel     lblInfoTongThueGoc, lblInfoGiamDiem, lblInfoTongSauGiam, lblInfoTienCoc;

    private KhachHang currentKH  = null;
    private int       maKH       = -1;
    private int       diemHienCo = 0;

    // Buoc 3 — info cards
    private JLabel cardMaCD, cardGame, cardGiaNgay;

    // Buoc 3 — summary labels
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
        super(parent, "Thue CD / Game", true);
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

        JLabel title = new JLabel("THUE CD / GAME");
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
        String[] labels = {"1. Chon game", "2. Chi tiet", "3. Xac nhan"};
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
    // STEP 1 — Danh sach CD san sang
    // =========================================================
    private JPanel buildStep1() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(14, 20, 0, 20));

        // Thanh tren: tieu de + nut lam moi
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG);

        JLabel lbl = new JLabel("Danh sach CD san sang cho thue:");
        lbl.setFont(F_SECTION);
        lbl.setForeground(TEXT_MAIN);

        PillButton btnRefresh = new PillButton("Lam moi", ACCENT, WHITE);
        btnRefresh.setPreferredSize(new Dimension(100, 32));
        btnRefresh.addActionListener(e -> loadCDTable());

        topBar.add(lbl, BorderLayout.WEST);
        topBar.add(btnRefresh, BorderLayout.EAST);
        p.add(topBar, BorderLayout.NORTH);

        // Bang CD
        String[] cols = {"Ma CD", "Ten Game", "Gia thue / ngay"};
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

        JLabel hint = new JLabel("Chon game roi nhan \"Tiep theo\", hoac double-click de sang buoc tiep.");
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
                String.format("%,.0f VND / ngay", gia)
            });
        }
    }

    // =========================================================
    // STEP 2 — Thong tin thue
    // =========================================================
    private JPanel buildStep2() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(14, 20, 0, 20));

        // --- Info cards game da chon ---
        cardMaCD  = new JLabel("-");
        cardGame  = new JLabel("-");
        cardGiaNgay = new JLabel("-");

        JPanel cards = new JPanel(new GridLayout(1, 3, 10, 0));
        cards.setBackground(BG);
        cards.setPreferredSize(new Dimension(0, 68));
        cards.add(infoCard("Ma CD",          cardMaCD));
        cards.add(infoCard("Ten game",       cardGame));
        cards.add(infoCard("Gia thue / ngay", cardGiaNgay));
        p.add(cards, BorderLayout.NORTH);

        // --- Form ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 0, 5, 10);

        int row = 0;

        // SĐT + nut tim + nut tao
        txtSDT = makeInput();
        PillButton btnTim = new PillButton("Tim", ACCENT, WHITE);
        btnTim.setPreferredSize(new Dimension(80, 34));
        btnTim.addActionListener(e -> doFindKH());

        PillButton btnTao = new PillButton("Tao moi", SUCCESS, TEXT_DARK);
        btnTao.setPreferredSize(new Dimension(100, 34));
        btnTao.addActionListener(e -> doCreateKH());

        txtSDT.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { doFindKH(); }
            public void keyPressed(KeyEvent e)  { if (e.getKeyCode() == KeyEvent.VK_ENTER) doFindKH(); }
        });

        addLabel(form, gc, "So dien thoai KH:", row, 0, 120);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 1;
        form.add(txtSDT, gc);
        gc.gridx = 2; gc.weightx = 0;
        form.add(btnTim, gc);
        gc.gridx = 3;
        form.add(btnTao, gc);
        gc.gridwidth = 1;

        // Ten KH
        row++;
        lblTenKH = new JLabel("---");
        lblTenKH.setFont(F_VALUE);
        lblTenKH.setForeground(TEXT_MAIN);
        addLabel(form, gc, "Ten khach hang:", row, 0, 120);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 3;
        form.add(lblTenKH, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        // Diem hien co
        row++;
        lblDiemHienCo = new JLabel("---");
        lblDiemHienCo.setFont(F_VALUE);
        lblDiemHienCo.setForeground(GOLD);
        addLabel(form, gc, "Diem tich luy:", row, 0, 120);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 3;
        form.add(lblDiemHienCo, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        // Duong ke
        row++;
        addDivider(form, gc, row, 4);

        // So ngay thue
        row++;
        txtSoNgay = makeInput();
        txtSoNgay.setText("1");
        txtSoNgay.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { recalc(); }
        });
        addLabel(form, gc, "So ngay thue:", row, 0, 120);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 3;
        form.add(txtSoNgay, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        // Diem su dung
        row++;
        txtDiemSuDung = makeInput();
        txtDiemSuDung.setText("0");
        txtDiemSuDung.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { recalc(); }
        });
        addLabel(form, gc, "Diem muon dung:", row, 0, 120);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 3;
        form.add(txtDiemSuDung, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        // Duong ke
        row++;
        addDivider(form, gc, row, 4);

        // --- Ket qua tinh (4 dong, 2 cot moi dong) ---
        row++;
        lblInfoTongThueGoc = resultLabel("---", TEXT_MAIN);
        addLabel(form, gc, "Tong thue goc:", row, 0, 120);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 3;
        form.add(lblInfoTongThueGoc, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        row++;
        lblInfoGiamDiem = resultLabel("---", SUCCESS);
        addLabel(form, gc, "Giam tu diem:", row, 0, 120);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 3;
        form.add(lblInfoGiamDiem, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        row++;
        lblInfoTongSauGiam = resultLabel("---", ACCENT_LIGHT);
        addLabel(form, gc, "Du kien thu khi tra:", row, 0, 120);
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1.0; gc.gridwidth = 3;
        form.add(lblInfoTongSauGiam, gc);
        gc.gridwidth = 1; gc.weightx = 0;

        row++;
        lblInfoTienCoc = resultLabel("---", GOLD);
        addLabel(form, gc, "Tien coc thu ngay:", row, 0, 120);
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

    /** Load du lieu game da chon vao step 2 */
    private void loadStep2Data() {
        cardMaCD.setText("CD" + selectedMaCD);
        cardGame.setText(selectedTenGame);
        cardGiaNgay.setText(String.format("%,.0f VND", selectedGia));

        txtSDT.setText("");
        txtSoNgay.setText("1");
        txtDiemSuDung.setText("0");
        lblTenKH.setText("Chua tim kiem");
        lblTenKH.setForeground(TEXT_MUTED);
        lblDiemHienCo.setText("---");
        currentKH = null; maKH = -1; diemHienCo = 0;
        recalc();
    }

    private void doFindKH() {
        String sdt = txtSDT.getText().trim();
        if (sdt.isEmpty()) {
            currentKH = null; maKH = -1; diemHienCo = 0;
            lblTenKH.setText("Chua nhap so dien thoai");
            lblTenKH.setForeground(TEXT_MUTED);
            lblDiemHienCo.setText("---");
            recalc(); return;
        }
        KhachHang kh = khDAO.findBySDT(sdt);
        if (kh != null) {
            currentKH = kh; maKH = kh.getMaKH(); diemHienCo = kh.getDiemTichLuy();
            lblTenKH.setText(kh.getHoTen());
            lblTenKH.setForeground(SUCCESS);
            lblDiemHienCo.setText(diemHienCo + " diem  ~  "
                + String.format("%,.0f VND", (double) diemHienCo * DIEM_TO_VND));
        } else {
            currentKH = null; maKH = -1; diemHienCo = 0;
            lblTenKH.setText("Khong tim thay  —  Nhan \"Tao moi\"");
            lblTenKH.setForeground(DANGER);
            lblDiemHienCo.setText("0 diem");
        }
        recalc();
    }

    private void doCreateKH() {
        String sdt = txtSDT.getText().trim();
        if (sdt.isEmpty()) { showMsg("Vui long nhap so dien thoai truoc khi tao!"); return; }

        KhachHang existing = khDAO.findBySDT(sdt);
        if (existing != null) {
            showMsg("Khach hang voi SDT " + sdt + " da ton tai!");
            currentKH = existing; maKH = existing.getMaKH(); diemHienCo = existing.getDiemTichLuy();
            lblTenKH.setText(existing.getHoTen()); lblTenKH.setForeground(SUCCESS);
            lblDiemHienCo.setText(diemHienCo + " diem");
            recalc(); return;
        }

        JTextField fldTen  = new JTextField(20);
        JTextField fldCCCD = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(new JLabel("So dien thoai:"));  panel.add(new JLabel(sdt));
        panel.add(new JLabel("Ho va ten (*):"));  panel.add(fldTen);
        panel.add(new JLabel("CCCD (*):"));       panel.add(fldCCCD);
        panel.add(new JLabel(""));
        panel.add(new JLabel("<html><i style='color:gray'>9 hoac 12 chu so</i></html>"));

        int result = JOptionPane.showConfirmDialog(this, panel, "Tao khach hang moi",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String hoTen = fldTen.getText().trim();
        String cccd  = fldCCCD.getText().trim();
        if (hoTen.isEmpty()) { showMsg("Ho va ten khong duoc de trong!"); return; }
        if (!cccd.matches("\\d{9}|\\d{12}")) { showMsg("CCCD phai co 9 hoac 12 chu so!"); return; }

        KhachHang kh = new KhachHang();
        kh.setHoTen(hoTen); kh.setSdt(sdt); kh.setCccd(cccd); kh.setDiemTichLuy(0);
        if (khDAO.insert(kh)) {
            currentKH = khDAO.findBySDT(sdt); maKH = currentKH.getMaKH(); diemHienCo = 0;
            lblTenKH.setText(currentKH.getHoTen() + "  (moi tao)");
            lblTenKH.setForeground(SUCCESS);
            lblDiemHienCo.setText("0 diem");
            showMsg("Tao khach hang thanh cong!");
        } else {
            showMsg("Tao khach hang that bai!");
        }
        recalc();
    }

    /** Tinh lai cac gia tri hien thi (moi khi thay doi so ngay / diem) */
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

            // Tong thue goc = GiaThueNgay x SoNgay (day la gia tri se luu vao DonGiaThue)
            double tongThueGoc = selectedGia * soNgay;
            double giamDiem    = diemDung * DIEM_TO_VND;
            double tongSauGiam = Math.max(0, tongThueGoc - giamDiem);
            // Coc tinh tren gia goc (chua tru diem)
            double tienCoc = Math.max(COC_MIN, tongThueGoc * COC_MULTIPLY);

            lblInfoTongThueGoc.setText(String.format("%,.0f VND  (%,.0f x %d ngay)", tongThueGoc, selectedGia, soNgay));
            lblInfoTongThueGoc.setForeground(TEXT_MAIN);

            lblInfoGiamDiem.setText(diemDung > 0
                ? String.format("-%,.0f VND  (%d diem)", giamDiem, diemDung)
                : "Khong su dung diem");
            lblInfoGiamDiem.setForeground(diemDung > 0 ? SUCCESS : TEXT_MUTED);

            lblInfoTongSauGiam.setText(String.format("%,.0f VND  (thu khi tra CD)", tongSauGiam));
            lblInfoTongSauGiam.setForeground(ACCENT_LIGHT);

            lblInfoTienCoc.setText(String.format("%,.0f VND  (thu ngay bay gio)", tienCoc));
            lblInfoTienCoc.setForeground(GOLD);

        } catch (NumberFormatException ex) {
            lblInfoTongThueGoc.setText("Nhap so hop le");
            lblInfoTienCoc.setText("---");
        }
    }

    // =========================================================
    // STEP 3 — Xac nhan
    // =========================================================
    private JPanel buildStep3() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(16, 20, 10, 20));

        lblS3CD         = sumValue();
        lblS3Game       = sumValue();
        lblS3KH         = sumValue();
        lblS3NgayThue   = sumValue();
        lblS3NgayTra    = sumValue();
        lblS3SoNgay     = sumValue();
        lblS3TongGoc    = sumValue();
        lblS3DiemTru    = sumValue();
        lblS3TongSauGiam= sumValue();
        lblS3Coc        = sumValue();
        lblS3Coc.setFont(F_MONEY);
        lblS3Coc.setForeground(GOLD);

        JPanel card = new JPanel(new GridLayout(10, 2, 8, 10));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(DIVIDER, 1, true),
            new EmptyBorder(16, 20, 16, 20)
        ));

        card.add(sumKey("Ma CD:"            )); card.add(lblS3CD);
        card.add(sumKey("Ten game:"         )); card.add(lblS3Game);
        card.add(sumKey("Khach hang:"       )); card.add(lblS3KH);
        card.add(sumKey("Ngay thue:"        )); card.add(lblS3NgayThue);
        card.add(sumKey("Ngay tra du kien:" )); card.add(lblS3NgayTra);
        card.add(sumKey("So ngay / Gia:"    )); card.add(lblS3SoNgay);
        card.add(sumKey("Tong thue goc:"    )); card.add(lblS3TongGoc);
        card.add(sumKey("Giam tu diem:"     )); card.add(lblS3DiemTru);
        card.add(sumKey("Du kien thu khi tra:")); card.add(lblS3TongSauGiam);
        card.add(sumKey("Tien coc thu ngay:")); card.add(lblS3Coc);

        JLabel notice = new JLabel(
            "<html><center>Sau khi xac nhan:<br>"
          + "Phieu thue moi &rarr; Dang thue  |  CD &rarr; Dang thue  |  Tru diem (neu co)</center></html>"
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

        double tongThueGoc = selectedGia * soNgay;   // GiaThueNgay x SoNgay => se luu vao DonGiaThue
        double giamDiem    = diemDung * DIEM_TO_VND;
        double tongSauGiam = Math.max(0, tongThueGoc - giamDiem);
        double tienCoc     = Math.max(COC_MIN, tongThueGoc * COC_MULTIPLY);

        lblS3CD.setText("CD" + selectedMaCD);
        lblS3Game.setText(selectedTenGame);
        lblS3KH.setText(currentKH != null ? currentKH.getHoTen() + "  (KH" + maKH + ")" : "Khach vang lai");
        lblS3NgayThue.setText(now.format(fmt));
        lblS3NgayTra.setText(now.plusDays(soNgay).format(fmt));
        lblS3SoNgay.setText(soNgay + " ngay  x  " + String.format("%,.0f VND", selectedGia) + " / ngay");
        lblS3TongGoc.setText(String.format("%,.0f VND  (se luu vao phieu thue)", tongThueGoc));
        lblS3DiemTru.setText(diemDung > 0
            ? String.format("-%,.0f VND  (%d diem)", giamDiem, diemDung)
            : "Khong su dung diem");
        lblS3DiemTru.setForeground(diemDung > 0 ? SUCCESS : TEXT_MUTED);
        lblS3TongSauGiam.setText(String.format("%,.0f VND", tongSauGiam));
        lblS3TongSauGiam.setForeground(ACCENT_LIGHT);
        lblS3Coc.setText(String.format("%,.0f VND  (thu ngay bay gio)", tienCoc));
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

        btnConfirm = new PillButton("Xac nhan thue", BTN_GREEN, TEXT_DARK);
        btnConfirm.setPreferredSize(new Dimension(150, 36));
        btnConfirm.setVisible(false);
        btnConfirm.addActionListener(e -> doConfirmRent());

        PillButton btnCancel = new PillButton("Huy", BTN_RED, WHITE);
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
            showMsg("Vui long chon mot game!"); return;
        }
        Object[] cd = cdList.get(row);
        selectedMaCD    = (int)    cd[0];
        selectedTenGame = (String) cd[1];
        selectedGia     = (double) cd[2];
        if (selectedGia <= 0) {
            showMsg("CD nay khong co thong tin gia thue, vui long chon CD khac!"); return;
        }
        loadStep2Data();
        showStep(2);
    }

    private void goToStep3() {
        int soNgay;
        try {
            soNgay = Integer.parseInt(txtSoNgay.getText().trim());
            if (soNgay <= 0) { showMsg("So ngay thue phai lon hon 0!"); return; }
        } catch (NumberFormatException e) {
            showMsg("So ngay thue phai la so nguyen!"); return;
        }

        int diemDung;
        try {
            diemDung = Integer.parseInt(txtDiemSuDung.getText().trim());
            if (diemDung < 0) { showMsg("Diem su dung khong duoc am!"); return; }
        } catch (NumberFormatException e) {
            showMsg("Diem su dung phai la so nguyen!"); return;
        }

        if (diemDung > diemHienCo) {
            showMsg("Khong du diem! Hien co: " + diemHienCo + ", muon dung: " + diemDung);
            txtDiemSuDung.setText(String.valueOf(diemHienCo));
            recalc(); return;
        }

        updateStep3Summary();
        showStep(3);
    }

    // =========================================================
    // CONFIRM — Tao phieu thue
    // LOGIC MOI: DonGiaThue = tongThueGoc = GiaThueNgay x SoNgay
    // =========================================================
    private void doConfirmRent() {
        try {
            int soNgay = Integer.parseInt(txtSoNgay.getText().trim());
            int diemDung = 0;
            try { diemDung = Math.max(0, Integer.parseInt(txtDiemSuDung.getText().trim())); }
            catch (NumberFormatException ignored) {}
            if (diemDung > diemHienCo) diemDung = diemHienCo;

            // Tong thue goc — day la gia tri luu vao CTPHIEUTHUE.DonGiaThue
            double tongThueGoc = selectedGia * soNgay;
            double tienCoc     = Math.max(COC_MIN, tongThueGoc * COC_MULTIPLY);

            // Xu ly khach vang lai
            if (maKH == -1) {
                int yn = JOptionPane.showConfirmDialog(this,
                    "Khach hang chua xac dinh.\n\n"
                  + "[Co]    Tao tu dong Khach vang lai (khong tich diem)\n"
                  + "[Khong] Quay lai nhap SDT / tao KH",
                    "Khach vang lai?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (yn != JOptionPane.YES_OPTION) { showStep(2); return; }

                KhachHang kh = new KhachHang();
                kh.setHoTen("Khach vang lai");
                kh.setSdt("VL_" + System.currentTimeMillis());
                kh.setDiemTichLuy(0);
                if (!khDAO.insert(kh)) { showMsg("Khong the tao khach vang lai!"); return; }
                KhachHang newKH = khDAO.findBySDT(kh.getSdt());
                if (newKH == null) { showMsg("Loi tao khach!"); return; }
                maKH = newKH.getMaKH();
                diemDung = 0;
            }

            // Popup xac nhan
            double giamDiem    = diemDung * DIEM_TO_VND;
            double tongSauGiam = Math.max(0, tongThueGoc - giamDiem);
            String msg = String.format(
                "Xac nhan thu coc va tao phieu thue?\n\n"
              + "  CD            : CD%d  -  %s\n"
              + "  Khach hang    : %s\n"
              + "  Thoi gian     : %d ngay\n"
              + "  Tong thue goc : %,.0f VND  (luu vao phieu thue)\n"
              + "  Giam diem     : %,.0f VND  (%d diem, ap dung khi tra)\n"
              + "  Du kien thu khi tra: %,.0f VND\n\n"
              + ">>> Thu coc ngay: %,.0f VND <<<",
                selectedMaCD, selectedTenGame,
                currentKH != null ? currentKH.getHoTen() : "Khach vang lai",
                soNgay,
                tongThueGoc, giamDiem, diemDung, tongSauGiam,
                tienCoc);

            int confirm = JOptionPane.showConfirmDialog(this, msg,
                "Xac nhan thu coc", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            LocalDateTime now = LocalDateTime.now();

            PhieuThue pt = new PhieuThue();
            pt.setMaKH(maKH);
            pt.setMaNV(Session.getMaNV());
            pt.setNgayThue(now);
            pt.setNgayTraDuKien(now.plusDays(soNgay));
            pt.setTienCoc(tienCoc);
            pt.setTrangThai("DangThue");

            // TinhTrang luu diem da su dung de RentReturnDialog doc lai
            String tinhTrang = diemDung > 0 ? ("OK|diem=" + diemDung) : "OK";

            // === LOGIC MOI ===
            // DonGiaThue = tongThueGoc = GiaThueNgay x SoNgay
            // RentReturnDialog chi can doc DonGiaThue tu DB, khong tinh lai tu ngay
            PhieuThue.CTPhieuThue ct =
                new PhieuThue.CTPhieuThue(selectedMaCD, selectedTenGame, tongThueGoc, tinhTrang);
            ct.setMaNV(Session.getMaNV());
            pt.getDanhSachChiTiet().add(ct);

            boolean ok = service.createPhieuThue(pt);
            if (ok) {
                if (maKH > 0 && diemDung > 0) {
                    khDAO.updatePoint(maKH, -diemDung);
                }
                JOptionPane.showMessageDialog(this,
                    String.format(
                        "Tao phieu thue thanh cong!\n\n"
                      + "  Da thu coc: %,.0f VND\n"
                      + "  CD%d -> Dang thue\n\n"
                      + "Vui long giao CD cho khach hang.",
                        tienCoc, selectedMaCD),
                    "Hoan tat", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                showMsg("Tao phieu thue that bai!\n"
                      + "CD co the da duoc thue boi giao dich khac.\n"
                      + "Vui long lam moi danh sach va thu lai.");
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