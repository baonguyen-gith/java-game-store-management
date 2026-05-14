package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.model.DashboardStats;
import otkhongluong.gamestoremanagement.model.Employee;
import otkhongluong.gamestoremanagement.service.DashboardService;
import otkhongluong.gamestoremanagement.service.EmployeeService;
import otkhongluong.gamestoremanagement.view.panel.EmployeeDashboardPanel;

import javax.swing.*;

/**
 * Controller – Điều phối giữa View (EmployeeDashboardPanel)
 * và các Service (EmployeeService, DashboardService).
 *
 * Tuân thủ MVC:
 *   - View KHÔNG gọi DAO / Service trực tiếp.
 *   - Controller lấy data rồi đẩy vào View qua các setter / loadData().
 */
public class EmployeeDashboardController {

    // ─── Dependencies ──────────────────────────────────────────────────────
    private final EmployeeDashboardPanel view;
    private final EmployeeService        employeeService  = new EmployeeService();
    private final DashboardService       dashboardService = new DashboardService();

    // ─── State ────────────────────────────────────────────────────────────
    /** Nhân viên đang đăng nhập (được truyền vào từ màn hình login). */
    private final int currentMaNV;

    // ─── Constructor ──────────────────────────────────────────────────────

    /**
     * @param view       Panel UI đã được khởi tạo.
     * @param currentMaNV Mã nhân viên của người đang đăng nhập.
     */
    public EmployeeDashboardController(EmployeeDashboardPanel view, int currentMaNV) {
        this.view        = view;
        this.currentMaNV = currentMaNV;
    }

    // ─── Public API ───────────────────────────────────────────────────────

    /**
     * Gọi sau khi khởi tạo Controller để tải dữ liệu lần đầu.
     * Chạy trên SwingWorker để không block EDT (Event Dispatch Thread).
     */
    public void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {

            // Chạy trên background thread – an toàn để query DB
            Employee        employee;
            DashboardStats  stats;

            @Override
            protected Void doInBackground() {
                employee = employeeService.getNhanVienById(currentMaNV);
                stats    = dashboardService.getStats();
                return null;
            }

            // Chạy trên EDT – an toàn để cập nhật Swing UI
            @Override
            protected void done() {
                if (employee != null) {
                    view.setEmployeeInfo(employee);
                } else {
                    view.showError("Không tìm thấy thông tin nhân viên (mã: " + currentMaNV + ")");
                }
                view.setStats(stats);
            }
        };

        worker.execute();
    }

    /**
     * Làm mới số liệu dashboard (có thể gắn vào nút Refresh hoặc Timer).
     */
    public void refreshStats() {
        SwingWorker<DashboardStats, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardStats doInBackground() {
                return dashboardService.getStats();
            }

            @Override
            protected void done() {
                try {
                    view.setStats(get());
                } catch (Exception e) {
                    view.showError("Lỗi làm mới dữ liệu: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
}