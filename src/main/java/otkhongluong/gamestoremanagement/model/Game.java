package otkhongluong.gamestoremanagement.model;

public class Game {

    private int maGame;
    private String tenGame;
    private String theLoai;
    private String nenTang;
    private String ghiChu;
    private String hinhAnh;

    // ======================
    // FROM SANPHAM + CD + ROM
    // ======================
    private Double giaCD;
    private Double giaROM;

    public Game() {}

    public Game(int maGame, String tenGame, String theLoai,
                String nenTang, String ghiChu, String hinhAnh) {

        this.maGame = maGame;
        this.tenGame = tenGame;
        this.theLoai = theLoai;
        this.nenTang = nenTang;
        this.ghiChu = ghiChu;
        this.hinhAnh = hinhAnh;
    }

    // ===== GETTER / SETTER =====

    public int getMaGame() {
        return maGame;
    }

    public void setMaGame(int maGame) {
        this.maGame = maGame;
    }

    public String getTenGame() {
        return tenGame;
    }

    public void setTenGame(String tenGame) {
        this.tenGame = tenGame;
    }

    public String getTheLoai() {
        return theLoai;
    }

    public void setTheLoai(String theLoai) {
        this.theLoai = theLoai;
    }

    public String getNenTang() {
        return nenTang;
    }

    public void setNenTang(String nenTang) {
        this.nenTang = nenTang;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    // ======================
    // CD PRICE
    // ======================
    public Double getGiaCD() {
        return giaCD;
    }

    public void setGiaCD(Double giaCD) {
        this.giaCD = giaCD;
    }

    // ======================
    // ROM PRICE
    // ======================
    public Double getGiaROM() {
        return giaROM;
    }

    public void setGiaROM(Double giaROM) {
        this.giaROM = giaROM;
    }
    
    public String getGiaCDText() {
        return giaCD == null ? "" : String.format("%,.0f đ", giaCD);
    }

    public String getGiaROMText() {
        return giaROM == null ? "" : String.format("%,.0f đ", giaROM);
    }

    @Override
    public String toString() {
        return "Game{" +
                "maGame=" + maGame +
                ", tenGame='" + tenGame + '\'' +
                '}';
    }
}