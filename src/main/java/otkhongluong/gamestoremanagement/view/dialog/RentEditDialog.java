package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.dao.CustomerDAO;
import otkhongluong.gamestoremanagement.dao.EmployeeDAO;
import otkhongluong.gamestoremanagement.dao.RentalOrderDAO;
import otkhongluong.gamestoremanagement.model.Customer;
import otkhongluong.gamestoremanagement.model.Employee;
import otkhongluong.gamestoremanagement.model.RentalOrder;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class RentEditDialog extends JDialog {

    /* ── Palette ── */
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

    /* ── Fonts ── */
    private static final Font F_TITLE  = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font F_LABEL  = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font F_INPUT  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_HINT   = new Font("Segoe UI", Font.ITALIC, 11);
    private static final Font F_RESULT = new Font("Segoe UI", Font.BOLD, 13);

    // Chỉ parse/format phần ngày từ input người dùng
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /* ── State ── */
    private final RentalOrder    pt;
    private final RentalOrderDAO ptDAO = new RentalOrderDAO();
    private final CustomerDAO khDAO = new CustomerDAO();
    private final EmployeeDAO  nvDAO = new EmployeeDAO();

    private Customer     foundKH    = null;
    private Employee      foundNV    = null;
    private LocalDateTime newNgayTra = null; // LocalDateTime để khớp model + DAO

    /* ── Components ── */
    private JTextField txtSdt;
    private JTextField txtMaNV;
    private JTextField txtNgayTra;
    private JLabel     lblKHResult;
    private JLabel     lblNVResult;
    private JLabel     lblNgayTraResult;
    private JButton    btnSave;

    // ═══════════════════════════════════════════════════════════
    public RentEditDialog(Frame parent, RentalOrder pt) {
        super(parent, "Sửa Phiếu Thuê", true);
        this.pt = pt;

        setSize(500, 630);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);

        prefillCurrentValues();
    }

    // ── HEADER ──────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(20, 24, 0, 24));

        JLabel title = new JLabel("✏  Sửa Phiếu Thuê  —  PT" + pt.getMaPT());
        title.setFont(F_TITLE);
        title.setForeground(WHITE);
        p.add(title, BorderLayout.WEST);

        boolean dangThue = "DangThue".equalsIgnoreCase(pt.getTrangThai());
        JLabel badge = new JLabel(dangThue ? "⏳ Đang thuê" : "✔ Đã trả");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(dangThue ? YELLOW : GREEN);
        p.add(badge, BorderLayout.EAST);

        JPanel sep = new JPanel();
        sep.setBackground(ACCENT_LIGHT);
        sep.setPreferredSize(new Dimension(0, 1));
        p.add(sep, BorderLayout.SOUTH);
        return p;
    }

    // ── BODY ────────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(16, 24, 8, 24));

        body.add(buildInfoCards());
        body.add(Box.createVerticalStrut(20));
        body.add(buildDivider("Chỉnh sửa thông tin nhập nhầm"));
        body.add(Box.createVerticalStrut(16));
        body.add(buildSdtSection());
        body.add(Box.createVerticalStrut(16));
        body.add(buildMaNVSection());
        body.add(Box.createVerticalStrut(16));
        body.add(buildNgayTraSection());

        return body;
    }

    private JPanel buildInfoCards() {
        JPanel row = new JPanel(new GridLayout(1, 2, 10, 0));
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        // LocalDateTime.format(FMT "dd/MM/yyyy") — hợp lệ, chỉ in phần ngày
        String ngayThue = pt.getNgayThue()      != null ? pt.getNgayThue().format(FMT)      : "—";
        String ngayTra  = pt.getNgayTraDuKien()  != null ? pt.getNgayTraDuKien().format(FMT) : "—";

        row.add(infoCard("Ngày thuê",        ngayThue));
        row.add(infoCard("Ngày trả dự kiến", ngayTra));
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
        lbl.setFont(F_LABEL);
        lbl.setForeground(MUTED);
        JLabel val = new JLabel(value);
        val.setFont(F_RESULT);
        val.setForeground(WHITE);
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
        lbl.setFont(F_HINT);
        lbl.setForeground(ACCENT_LIGHT);
        p.add(l1,  BorderLayout.WEST);
        p.add(lbl, BorderLayout.CENTER);
        p.add(l2,  BorderLayout.EAST);
        return p;
    }

    private JPanel buildSdtSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(BG);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        section.add(sectionLabel("Khách hàng  —  nhập số điện thoại"));
        section.add(Box.createVerticalStrut(6));

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        txtSdt = styledInput("Số điện thoại khách hàng...");
        RoundBtn btnFind = new RoundBtn("Tìm", ACCENT, WHITE);
        btnFind.setPreferredSize(new Dimension(70, 40));
        btnFind.addActionListener(e -> lookupKhachHang());
        txtSdt.addActionListener(e -> lookupKhachHang());

        row.add(txtSdt,   BorderLayout.CENTER);
        row.add(btnFind,  BorderLayout.EAST);
        section.add(row);
        section.add(Box.createVerticalStrut(6));

        lblKHResult = new JLabel("  ");
        lblKHResult.setFont(F_RESULT);
        lblKHResult.setForeground(MUTED);
        section.add(lblKHResult);
        return section;
    }

    private JPanel buildMaNVSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(BG);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        section.add(sectionLabel("Nhân viên  —  nhập mã nhân viên"));
        section.add(Box.createVerticalStrut(6));

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        txtMaNV = styledInput("Mã nhân viên (VD: NV001)...");
        RoundBtn btnFind = new RoundBtn("Tìm", ACCENT, WHITE);
        btnFind.setPreferredSize(new Dimension(70, 40));
        btnFind.addActionListener(e -> lookupNhanVien());
        txtMaNV.addActionListener(e -> lookupNhanVien());

        row.add(txtMaNV,  BorderLayout.CENTER);
        row.add(btnFind,  BorderLayout.EAST);
        section.add(row);
        section.add(Box.createVerticalStrut(6));

        lblNVResult = new JLabel("  ");
        lblNVResult.setFont(F_RESULT);
        lblNVResult.setForeground(MUTED);
        section.add(lblNVResult);
        return section;
    }

    private JPanel buildNgayTraSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(BG);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        section.add(sectionLabel("Ngày trả dự kiến  —  định dạng dd/MM/yyyy"));
        section.add(Box.createVerticalStrut(6));

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        txtNgayTra = styledInput("dd/MM/yyyy — VD: 25/06/2025");
        RoundBtn btnVerify = new RoundBtn("Xác nhận", ACCENT, WHITE);
        btnVerify.setPreferredSize(new Dimension(90, 40));
        btnVerify.addActionListener(e -> validateNgayTra());
        txtNgayTra.addActionListener(e -> validateNgayTra());

        row.add(txtNgayTra, BorderLayout.CENTER);
        row.add(btnVerify,  BorderLayout.EAST);
        section.add(row);
        section.add(Box.createVerticalStrut(6));

        lblNgayTraResult = new JLabel("  ");
        lblNgayTraResult.setFont(F_RESULT);
        lblNgayTraResult.setForeground(MUTED);
        section.add(lblNgayTraResult);
        return section;
    }

    // ── FOOTER ──────────────────────────────────────────────────
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

        btnSave = new RoundBtn("✔  Lưu", GREEN, TEXT_DARK);
        btnSave.setPreferredSize(new Dimension(110, 40));
        btnSave.addActionListener(e -> doSave());

        btnRow.add(btnCancel);
        btnRow.add(btnSave);

        p.add(sep,    BorderLayout.NORTH);
        p.add(btnRow, BorderLayout.CENTER);
        return p;
    }

    // ── LOGIC ───────────────────────────────────────────────────

    private void prefillCurrentValues() {
        if (pt.getSoDienThoai() != null && !pt.getSoDienThoai().isEmpty()) {
            txtSdt.setText(pt.getSoDienThoai());
            txtSdt.setForeground(TEXT_DARK);
            lookupKhachHang();
        }
        if (pt.getMaNV() > 0) {
            txtMaNV.setText(String.valueOf(pt.getMaNV()));
            txtMaNV.setForeground(TEXT_DARK);
            lookupNhanVien();
        }
        if (pt.getNgayTraDuKien() != null) {
            txtNgayTra.setText(pt.getNgayTraDuKien().format(FMT));
            txtNgayTra.setForeground(TEXT_DARK);
            setResult(lblNgayTraResult,
                "Ngày hiện tại: " + pt.getNgayTraDuKien().format(FMT)
                + "  (" + soNgay(pt.getNgayThue(), pt.getNgayTraDuKien()) + " ngày)", MUTED);
        }
    }

    private void lookupKhachHang() {
        String sdt = txtSdt.getText().trim();
        if (sdt.isEmpty()) {
            setResult(lblKHResult, "⚠  Nhập số điện thoại trước!", MUTED);
            foundKH = null;
            return;
        }
        Customer kh = khDAO.findBySDT(sdt);
        if (kh == null) {
            setResult(lblKHResult, "✗  Không tìm thấy khách hàng với SĐT: " + sdt, RED);
            foundKH = null;
        } else {
            setResult(lblKHResult,
                "✓  " + kh.getHoTen() + "  (Mã KH: KH" + String.format("%03d", kh.getMaKH()) + ")", GREEN);
            foundKH = kh;
        }
    }

    private void lookupNhanVien() {
        String raw = txtMaNV.getText().trim().replaceAll("(?i)nv", "");
        if (raw.isEmpty()) {
            setResult(lblNVResult, "⚠  Nhập mã nhân viên trước!", MUTED);
            foundNV = null;
            return;
        }
        int maNV;
        try {
            maNV = Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            setResult(lblNVResult, "✗  Mã NV phải là số (VD: NV001 hoặc 1)", RED);
            foundNV = null;
            return;
        }
        Employee nv = nvDAO.findById(maNV);
        if (nv == null) {
            setResult(lblNVResult, "✗  Không tìm thấy nhân viên với mã: " + maNV, RED);
            foundNV = null;
        } else {
            setResult(lblNVResult,
                "✓  " + nv.getHoTen() + "  (NV" + String.format("%03d", nv.getMaNV()) + ")", GREEN);
            foundNV = nv;
        }
    }

    private void validateNgayTra() {
        String raw = txtNgayTra.getText().trim();
        if (raw.isEmpty()) {
            setResult(lblNgayTraResult, "⚠  Nhập ngày trả dự kiến trước!", MUTED);
            newNgayTra = null;
            return;
        }

        // Dùng LocalDate để parse input dd/MM/yyyy (không có giờ)
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(raw, FMT);
        } catch (DateTimeParseException ex) {
            setResult(lblNgayTraResult, "✗  Định dạng không hợp lệ. Vui lòng nhập dd/MM/yyyy", RED);
            newNgayTra = null;
            return;
        }

        // NgayThue là LocalDateTime → lấy toLocalDate() để so sánh ngày
        LocalDate ngayThueDateOnly = pt.getNgayThue() != null
            ? pt.getNgayThue().toLocalDate()
            : null;

        if (ngayThueDateOnly != null && !parsedDate.isAfter(ngayThueDateOnly)) {
            setResult(lblNgayTraResult,
                "✗  Ngày trả dự kiến phải sau ngày thuê (" + ngayThueDateOnly.format(FMT) + ")", RED);
            newNgayTra = null;
            return;
        }

        long soNgay = ngayThueDateOnly != null
            ? ChronoUnit.DAYS.between(ngayThueDateOnly, parsedDate)
            : 0;

        // NgayTraDuKien là LocalDateTime → lấy toLocalDate() để so sánh
        LocalDate ngayTraCuDateOnly = pt.getNgayTraDuKien() != null
            ? pt.getNgayTraDuKien().toLocalDate()
            : null;

        if (parsedDate.equals(ngayTraCuDateOnly)) {
            setResult(lblNgayTraResult,
                "ℹ  Ngày không đổi (" + soNgay + " ngày). DonGiaThue không tính lại.", MUTED);
            newNgayTra = null;
            return;
        }

        // Chuyển LocalDate → LocalDateTime (atStartOfDay) để truyền vào DAO
        newNgayTra = parsedDate.atStartOfDay();
        setResult(lblNgayTraResult,
            "✓  " + parsedDate.format(FMT) + "  (" + soNgay + " ngày)  — DonGiaThue sẽ được tính lại", GREEN);
    }

    private void doSave() {
        boolean khChanged   = foundKH    != null && foundKH.getMaKH() != pt.getMaKH();
        boolean nvChanged   = foundNV    != null && foundNV.getMaNV() != pt.getMaNV();
        boolean ngayChanged = newNgayTra != null;

        if (!khChanged && !nvChanged && !ngayChanged) {
            if (foundKH == null && !txtSdt.getText().trim().equals(pt.getSoDienThoai())) {
                JOptionPane.showMessageDialog(this,
                    "Vui lòng nhấn 'Tìm' để xác nhận khách hàng trước khi lưu.",
                    "Chưa xác nhận", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (foundNV == null && !txtMaNV.getText().trim().replaceAll("(?i)nv", "")
                    .equals(String.valueOf(pt.getMaNV()))) {
                JOptionPane.showMessageDialog(this,
                    "Vui lòng nhấn 'Tìm' để xác nhận nhân viên trước khi lưu.",
                    "Chưa xác nhận", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String txtNgay    = txtNgayTra.getText().trim();
            String ngayHienTai = pt.getNgayTraDuKien() != null ? pt.getNgayTraDuKien().format(FMT) : "";
            if (!txtNgay.isEmpty() && !txtNgay.equals(ngayHienTai)) {
                JOptionPane.showMessageDialog(this,
                    "Vui lòng nhấn 'Xác nhận' để kiểm tra ngày trả trước khi lưu.",
                    "Chưa xác nhận", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this,
                "Không có thay đổi nào để lưu.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Confirm
        StringBuilder sb = new StringBuilder("Xác nhận cập nhật phiếu PT")
            .append(pt.getMaPT()).append("?\n\n");
        if (khChanged)
            sb.append("• Khách hàng : ").append(pt.getTenKhachHang())
              .append("  →  ").append(foundKH.getHoTen()).append("\n");
        if (nvChanged)
            sb.append("• Nhân viên  : ").append(pt.getTenNhanVien())
              .append("  →  ").append(foundNV.getHoTen()).append("\n");
        if (ngayChanged)
            sb.append("• Ngày trả DK: ")
              .append(pt.getNgayTraDuKien() != null ? pt.getNgayTraDuKien().format(FMT) : "—")
              .append("  →  ").append(newNgayTra.format(FMT)).append("\n")
              .append("  ↳ DonGiaThue sẽ được tính lại tự động.\n");

        int confirm = JOptionPane.showConfirmDialog(this, sb.toString(),
            "Xác nhận sửa", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean anyFail = false;

        // 1. Cập nhật KH / NV
        if (khChanged || nvChanged) {
            int newMaKH = khChanged ? foundKH.getMaKH() : pt.getMaKH();
            int newMaNV = nvChanged ? foundNV.getMaNV() : pt.getMaNV();
            boolean ok  = ptDAO.updateKhachHangVaNhanVien(pt.getMaPT(), newMaKH, newMaNV);
            if (ok && khChanged) {
                int diem = ptDAO.tinhDiemPhieu(pt.getMaPT());
                if (diem > 0) {
                    khDAO.truDiem(pt.getMaKH(), diem);
                    khDAO.congDiem(foundKH.getMaKH(), diem);
                }
            }
            if (!ok) {
                anyFail = true;
                JOptionPane.showMessageDialog(this,
                    "Cập nhật Khách hàng / Nhân viên thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }

        // 2. Cập nhật Ngày trả + tính lại DonGiaThue
        // newNgayTra là LocalDateTime → khớp đúng kiểu DAO
        if (ngayChanged) {
            boolean ok = ptDAO.updateNgayTraVaDonGia(pt.getMaPT(), newNgayTra);
            if (!ok) {
                anyFail = true;
                JOptionPane.showMessageDialog(this,
                    "Cập nhật Ngày trả / DonGiaThue thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }

        if (!anyFail) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!", "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }

    // ── Helpers ─────────────────────────────────────────────────

    /** Tính số ngày giữa 2 LocalDateTime — chỉ so phần ngày */
    private long soNgay(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) return 0;
        return ChronoUnit.DAYS.between(from.toLocalDate(), to.toLocalDate());
    }

    private void setResult(JLabel lbl, String text, Color color) {
        lbl.setText(text);
        lbl.setForeground(color);
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(F_LABEL);
        lbl.setForeground(ACCENT_LIGHT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
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
        tf.setBackground(INPUT_BG);
        tf.setForeground(TEXT_DARK);
        tf.setCaretColor(ACCENT);
        tf.setFont(F_INPUT);
        tf.setBorder(new CompoundBorder(
            new LineBorder(ACCENT, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        tf.setPreferredSize(new Dimension(0, 40));
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { tf.repaint(); }
        });
        return tf;
    }

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
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}