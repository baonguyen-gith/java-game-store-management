package otkhongluong.gamestoremanagement.model;

public class RevenueDTO {
    private String ThoiGian; 
    private int SoDonHang;
    private double TongDoanhThu;

    public RevenueDTO() {
    }

    public RevenueDTO(String ThoiGian, int SoDonHang, double TongDoanhThu) {
        this.ThoiGian = ThoiGian;
        this.SoDonHang = SoDonHang;
        this.TongDoanhThu = TongDoanhThu;
    }

    public String getThoiGian() {return ThoiGian;}
    public void setThoiGian(String ThoiGian) {this.ThoiGian = ThoiGian;}
    public int getSoDonHang() {return SoDonHang;}
    public void setSoDonHang(int SoDonHang) {this.SoDonHang = SoDonHang;}
    public double getTongDoanhThu() {return TongDoanhThu;}
    public void setTongDoanhThu(double TongDoanhThu) {this.TongDoanhThu = TongDoanhThu;}

    @Override
    public String toString() {
        return "RevenueDTO{" +
                "ThoiGian='" + ThoiGian + '\'' +
                ", SoDonHang=" + SoDonHang +
                ", TongDoanhThu=" + TongDoanhThu +
                '}';
    }
}
