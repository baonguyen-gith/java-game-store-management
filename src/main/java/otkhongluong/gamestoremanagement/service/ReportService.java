package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.ReportDAO;
import otkhongluong.gamestoremanagement.model.RevenueDTO;
import java.util.List;

public class ReportService {
    private final ReportDAO reportDAO;

    public ReportService() {
        this.reportDAO = new ReportDAO();
    }

    public List<RevenueDTO> getDailyRevenue() {
        return reportDAO.getDoanhThuTheoNgay();
    }

    public List<RevenueDTO> getMonthlyRevenue() {
        return reportDAO.getDoanhThuTheoThang();
    }

    public List<RevenueDTO> getYearlyRevenue() {
        return reportDAO.getDoanhThuTheoNam();
    }

    /**
     * API báo cáo: Tổng hợp dữ liệu thành chuỗi thông tin tóm tắt
     */
    public String generateSummary() {
        List<RevenueDTO> daily = getDailyRevenue();
        if (daily.isEmpty()) return "Không có dữ liệu doanh thu.";
        
        double total = 0;
        int orders = 0;
        for (RevenueDTO r : daily) {
            total += r.getTongDoanhThu();
            orders += r.getSoDonHang();
        }
        return String.format("Tổng số đơn hàng: %d | Tổng doanh thu: %,.0f VND", orders, total);
    }
}
