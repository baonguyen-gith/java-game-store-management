package otkhongluong.gamestoremanagement.service;

import otkhongluong.gamestoremanagement.dao.PhieuThueDAO;
import otkhongluong.gamestoremanagement.dao.SanPhamDAO;
import otkhongluong.gamestoremanagement.model.PhieuThue;
import java.time.LocalDateTime;
import java.time.Duration;
import java.sql.Timestamp;
import java.util.List;

public class ThueService {
    private final PhieuThueDAO phieuThueDAO;
    private final SanPhamDAO sanPhamDAO;

    public ThueService() {
        this.phieuThueDAO = new PhieuThueDAO();
        this.sanPhamDAO = new SanPhamDAO();
    }

    public boolean createPhieuThue(PhieuThue pt) {
        // Business logic: Khi thuê CD, đổi trạng thái CD sang "Đang thuê"
        boolean success = phieuThueDAO.insert(pt);
        if (success) {
            for (PhieuThue.CTPhieuThue ct : pt.getDanhSachChiTiet()) {
                sanPhamDAO.updateStatus(ct.getMaSP(), "Đang thuê");
            }
        }
        return success;
    }

    public List<PhieuThue> getAllPhieuThue() {
        return phieuThueDAO.findAll();
    }

    public boolean returnCD(int maPT, LocalDateTime ngayTraThucTe) {
        PhieuThue pt = phieuThueDAO.findById(maPT);
        if (pt == null) return false;

        // Tính tiền phạt: Giả sử 10,000 VND / ngày quá hạn
        double tienPhat = 0;
        if (ngayTraThucTe.isAfter(pt.getNgayTraDuKien())) {
            long daysLate = Duration.between(pt.getNgayTraDuKien(), ngayTraThucTe).toDays();
            if (daysLate == 0) daysLate = 1; // Tính ít nhất 1 ngày nếu quá giờ
            tienPhat = daysLate * 10000.0;
        }

        // Cập nhật ngày trả và tiền phạt
        boolean success = phieuThueDAO.updateNgayTra(maPT, Timestamp.valueOf(ngayTraThucTe));
        // Giả sử có method updateTienPhat hoặc gộp chung
        // Ở đây tôi gọi updateNgayTra trước, sau đó có thể cần update thêm tiền phạt nếu DB hỗ trợ
        
        // Đổi trạng thái CD về "Sẵn sàng"
        // Cần lấy danh sách chi tiết của phiếu thuê này (PhieuThueDAO.findById nên load kèm chi tiết)
        // Nếu DAO chưa load chi tiết, ta có thể cần bổ sung
        
        return success;
    }
}
