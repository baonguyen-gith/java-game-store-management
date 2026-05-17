package otkhongluong.gamestoremanagement.model;

import java.time.LocalDate;

public class Game {

    // ── GAME ──────────────────────────────────────────────
    private int    maGame;
    private String tenGame;
    private String theLoai;
    private String nenTang;
    private String ghiChu;
    private String hinhAnh;

    // ── GAME_CHITIET ──────────────────────────────────────
    private String    moTa;
    private String    rating;
    private String    genre;
    private String    deliveryMethod;
    private LocalDate releaseDate;
    private String    region;
    private String    features;
    private String    language;
    private String    currency;

    // ── SANPHAM (giá) ─────────────────────────────────────
    private Double giaCD;
    private Double giaROM;
    private Double giaThueNgay;

    // ── Constructors ──────────────────────────────────────
    public Game() {}

    public Game(int maGame, String tenGame, String theLoai,
                String nenTang, String ghiChu, String hinhAnh) {
        this.maGame   = maGame;
        this.tenGame  = tenGame;
        this.theLoai  = theLoai;
        this.nenTang  = nenTang;
        this.ghiChu   = ghiChu;
        this.hinhAnh  = hinhAnh;
    }

    // ── GAME getters/setters ───────────────────────────────
    public int    getMaGame()              { return maGame; }
    public void   setMaGame(int maGame)    { this.maGame = maGame; }

    public String getTenGame()             { return tenGame; }
    public void   setTenGame(String v)     { this.tenGame = v; }

    public String getTheLoai()             { return theLoai; }
    public void   setTheLoai(String v)     { this.theLoai = v; }

    public String getNenTang()             { return nenTang; }
    public void   setNenTang(String v)     { this.nenTang = v; }

    public String getGhiChu()             { return ghiChu; }
    public void   setGhiChu(String v)     { this.ghiChu = v; }

    public String getHinhAnh()            { return hinhAnh; }
    public void   setHinhAnh(String v)    { this.hinhAnh = v; }

    // ── GAME_CHITIET getters/setters ──────────────────────
    public String    getMoTa()                   { return moTa; }
    public void      setMoTa(String v)           { this.moTa = v; }

    public String    getRating()                 { return rating; }
    public void      setRating(String v)         { this.rating = v; }

    public String    getGenre()                  { return genre; }
    public void      setGenre(String v)          { this.genre = v; }

    public String    getDeliveryMethod()         { return deliveryMethod; }
    public void      setDeliveryMethod(String v) { this.deliveryMethod = v; }

    public LocalDate getReleaseDate()            { return releaseDate; }
    public void      setReleaseDate(LocalDate v) { this.releaseDate = v; }

    public String    getRegion()                 { return region; }
    public void      setRegion(String v)         { this.region = v; }

    public String    getFeatures()               { return features; }
    public void      setFeatures(String v)       { this.features = v; }

    public String    getLanguage()               { return language; }
    public void      setLanguage(String v)       { this.language = v; }

    public String    getCurrency()               { return currency; }
    public void      setCurrency(String v)       { this.currency = v; }

    // ── Giá getters/setters ───────────────────────────────
    public Double getGiaCD()              { return giaCD; }
    public void   setGiaCD(Double v)      { this.giaCD = v; }

    public Double getGiaROM()             { return giaROM; }
    public void   setGiaROM(Double v)     { this.giaROM = v; }

    public Double getGiaThueNgay()        { return giaThueNgay; }
    public void   setGiaThueNgay(Double v){ this.giaThueNgay = v; }

    @Override
    public String toString() {
        return "Game{maGame=" + maGame + ", tenGame='" + tenGame + "'}";
    }
}