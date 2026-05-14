package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.DashboardDAO;
import otkhongluong.gamestoremanagement.model.DashboardStats;

/**
 * Service – Xử lý business logic cho Dashboard.
 * Tầng trung gian giữa Controller và DAO.
 * Hiện tại chuyển thẳng vì logic đơn giản; sau này có thể
 * thêm cache, kiểm tra quyền, tổng hợp nhiều nguồn dữ liệu, v.v.
 */
public class DashboardService {

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    /**
     * Lấy số liệu thống kê để hiển thị trên Dashboard.
     *
     * @return DashboardStats đã tổng hợp từ HoaDon + PhieuThue.
     */
    public DashboardStats getStats() {
        return dashboardDAO.getStats();
    }
}