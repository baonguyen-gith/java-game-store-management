package otkhongluong.gamestoremanagement.model;

/**
 * Model – Chứa các số liệu thống kê hiển thị trên Dashboard.
 * Không ánh xạ trực tiếp một bảng DB mà là kết quả tổng hợp từ nhiều bảng.
 */
public class DashboardStats {

    private long doanhThuHomNay;     // Tổng tiền hóa đơn bán hôm nay
    private long doanhThuTuan;       // Tổng tiền hóa đơn bán trong tuần
    private int  soHoaDonHomNay;     // Số hóa đơn bán hôm nay
    private int  soPhieuThueHomNay;  // Số phiếu thuê hôm nay

    // ─── Constructors ──────────────────────────────────────────────────────

    public DashboardStats() {}

    public DashboardStats(long doanhThuHomNay, long doanhThuTuan,
                          int soHoaDonHomNay, int soPhieuThueHomNay) {
        this.doanhThuHomNay    = doanhThuHomNay;
        this.doanhThuTuan      = doanhThuTuan;
        this.soHoaDonHomNay    = soHoaDonHomNay;
        this.soPhieuThueHomNay = soPhieuThueHomNay;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────

    public long getDoanhThuHomNay()               { return doanhThuHomNay; }
    public void setDoanhThuHomNay(long v)          { this.doanhThuHomNay = v; }

    public long getDoanhThuTuan()                  { return doanhThuTuan; }
    public void setDoanhThuTuan(long v)            { this.doanhThuTuan = v; }

    public int getSoHoaDonHomNay()                 { return soHoaDonHomNay; }
    public void setSoHoaDonHomNay(int v)           { this.soHoaDonHomNay = v; }

    public int getSoPhieuThueHomNay()              { return soPhieuThueHomNay; }
    public void setSoPhieuThueHomNay(int v)        { this.soPhieuThueHomNay = v; }
}