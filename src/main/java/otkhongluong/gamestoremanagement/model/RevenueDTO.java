package otkhongluong.gamestoremanagement.model;

import java.math.BigDecimal;

public class RevenueDTO {
    private String thoiGian;   
    private int tongDonHang;   
    private BigDecimal tongDoanhThu; 

    public RevenueDTO() {}

    public RevenueDTO(String thoiGian, int tongDonHang, BigDecimal tongDoanhThu) {
        this.thoiGian = thoiGian;
        this.tongDonHang = tongDonHang;
        this.tongDoanhThu = tongDoanhThu;
    }

    // Getters and Setters
    public String getThoiGian() { return thoiGian; }
    public void setThoiGian(String thoiGian) { this.thoiGian = thoiGian; }

    public int getTongDonHang() { return tongDonHang; }
    public void setTongDonHang(int tongDonHang) { this.tongDonHang = tongDonHang; }

    public BigDecimal getTongDoanhThu() { return tongDoanhThu; }
    public void setTongDoanhThu(BigDecimal tongDoanhThu) { this.tongDoanhThu = tongDoanhThu; }
}
