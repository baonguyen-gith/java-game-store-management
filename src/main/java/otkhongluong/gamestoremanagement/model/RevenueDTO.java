package otkhongluong.gamestoremanagement.model;

public class RevenueDTO {
    private String thoiGian;
    private int soDonHang;
    private double tongDoanhThu;

    public RevenueDTO() {}

    public RevenueDTO(String thoiGian, int soDonHang, double tongDoanhThu) {
        this.thoiGian = thoiGian;
        this.soDonHang = soDonHang;
        this.tongDoanhThu = tongDoanhThu;
    }

    public String getThoiGian() { return thoiGian; }
    public void setThoiGian(String thoiGian) { this.thoiGian = thoiGian; }
    public int getSoDonHang() { return soDonHang; }
    public void setSoDonHang(int soDonHang) { this.soDonHang = soDonHang; }
    public double getTongDoanhThu() { return tongDoanhThu; }
    public void setTongDoanhThu(double tongDoanhThu) { this.tongDoanhThu = tongDoanhThu; }

    @Override
    public String toString() {
        return "RevenueDTO{thoiGian='" + thoiGian + "', soDonHang=" + soDonHang
             + ", tongDoanhThu=" + tongDoanhThu + '}';
    }
}
