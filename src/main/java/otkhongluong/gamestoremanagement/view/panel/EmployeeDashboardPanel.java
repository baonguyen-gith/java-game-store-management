package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.model.DashboardStats;
import otkhongluong.gamestoremanagement.model.Employee;

import javax.swing.*;
import java.awt.*;

/**
 * View – Hiển thị Dashboard của nhân viên.
 *
 * NGUYÊN TẮC MVC:
 *   - Panel KHÔNG biết đến DAO / Service / DB.
 *   - Dữ liệu được đẩy vào từ bên ngoài qua setEmployeeInfo() và setStats().
 *   - Controller gọi các phương thức này sau khi lấy data từ Service.
 */
public class EmployeeDashboardPanel extends JPanel {

    // ─── UI Components (cần giữ reference để cập nhật sau) ────────────────
    private final JLabel lblBadge;
    private final JLabel lblName;
    private final JLabel lblMaNV;
    private final JLabel lblNgaySinh;
    private final JLabel lblCccd;
    private final JLabel lblSdt;

    // Stats cards
    private final JLabel lblDoanhThuHomNay;
    private final JLabel lblDoanhThuTuan;
    private final JLabel lblSoHoaDon;
    private final JLabel lblSoPhieuThue;

    // ─── Constructor ──────────────────────────────────────────────────────

    public EmployeeDashboardPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // ===== MAIN CARD =====
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(200, 190, 220));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(card, BorderLayout.CENTER);

        // ================= TOP – BADGE =================
        lblBadge = new JLabel("—");
        lblBadge.setOpaque(true);
        lblBadge.setBackground(new Color(160, 120, 255));
        lblBadge.setForeground(Color.BLACK);
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblBadge.setBorder(BorderFactory.createEmptyBorder(5, 25, 5, 25));

        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(lblBadge);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(badgeWrapper, BorderLayout.CENTER);
        card.add(top);
        card.add(Box.createVerticalStrut(15));

        // ================= AVATAR =================
        JLabel avatar = new JLabel("👩", SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(avatar);
        card.add(Box.createVerticalStrut(10));

        // ================= NAME =================
        lblName = new JLabel("Đang tải...");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblName);
        card.add(Box.createVerticalStrut(20));

        // ================= INFO ROWS =================
        lblMaNV      = new JLabel("—");
        lblNgaySinh  = new JLabel("—");
        lblCccd      = new JLabel("—");
        lblSdt       = new JLabel("—");

        card.add(createInfoRow("Mã nhân viên:", lblMaNV));
        card.add(createInfoRow("Ngày sinh:",    lblNgaySinh));
        card.add(createInfoRow("CCCD:",         lblCccd));
        card.add(createInfoRow("SĐT:",          lblSdt));
        card.add(Box.createVerticalStrut(20));

        // ================= STATS CARDS =================
        lblDoanhThuHomNay = new JLabel("—");
        lblDoanhThuTuan   = new JLabel("—");
        lblSoHoaDon       = new JLabel("—");
        lblSoPhieuThue    = new JLabel("—");

        card.add(createStatCard("Doanh thu hôm nay",  lblDoanhThuHomNay));
        card.add(Box.createVerticalStrut(10));
        card.add(createStatCard("Doanh thu tuần",     lblDoanhThuTuan));
        card.add(Box.createVerticalStrut(15));

        JPanel statsRow = new JPanel(new GridLayout(1, 2, 10, 0));
        statsRow.setOpaque(false);
        statsRow.add(createStatCard("Hóa đơn hôm nay",    lblSoHoaDon));
        statsRow.add(createStatCard("Phiếu thuê hôm nay", lblSoPhieuThue));
        card.add(statsRow);
    }

    // ─── Public setters – gọi từ Controller ───────────────────────────────

    /**
     * Điền thông tin nhân viên đang đăng nhập vào UI.
     * Phải gọi trên Event Dispatch Thread.
     */
    public void setEmployeeInfo(Employee emp) {
        lblName.setText(emp.getHoTen());
        lblMaNV.setText(emp.getMaNVFormatted());
        lblNgaySinh.setText(emp.getNgaySinh() != null
                ? formatDate(emp.getNgaySinh().getDayOfMonth(),
                             emp.getNgaySinh().getMonthValue(),
                             emp.getNgaySinh().getYear())
                : "—");
        lblCccd.setText(emp.getCccd() != null ? emp.getCccd() : "—");
        lblSdt.setText(emp.getSdt()   != null ? emp.getSdt()  : "—");
        // Badge hiện chưa có role trong model – để trống hoặc tuỳ chỉnh
        lblBadge.setText("NHÂN VIÊN");
    }

    /**
     * Cập nhật các ô thống kê (doanh thu, số đơn).
     * Phải gọi trên Event Dispatch Thread.
     */
    public void setStats(DashboardStats stats) {
        if (stats == null) return;
        lblDoanhThuHomNay.setText(stats.getDoanhThuHomNayFormatted());
        lblDoanhThuTuan.setText(stats.getDoanhThuTuanFormatted());
        lblSoHoaDon.setText(String.valueOf(stats.getSoHoaDonHomNay()));
        lblSoPhieuThue.setText(String.valueOf(stats.getSoPhieuThueHomNay()));
    }

    /**
     * Hiển thị thông báo lỗi đơn giản (dùng khi load data thất bại).
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // ─── Private UI builders ──────────────────────────────────────────────

    /** Tạo một hàng thông tin gồm nhãn tiêu đề + nhãn giá trị. */
    private JPanel createInfoRow(String title, JLabel valueLabel) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        row.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        row.add(lblTitle);
        row.add(valueLabel);
        return row;
    }

    /** Tạo card thống kê với tiêu đề và nhãn giá trị được quản lý bên ngoài. */
    private JPanel createStatCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(90, 95, 140));
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(Color.LIGHT_GRAY);

        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        card.add(lblTitle,  BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // ─── Utility ──────────────────────────────────────────────────────────

    private String formatDate(int day, int month, int year) {
        return day + "/" + month + "/" + year;
    }
}