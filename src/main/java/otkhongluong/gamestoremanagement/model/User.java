package otkhongluong.gamestoremanagement.model;

public class User {
    private int    MaUser;
    private String Username;
    private String Password;
    private int    MaRole;
    private int    MaNV;   // ✅ THÊM — link sang bảng NHANVIEN (0 nếu chưa gắn NV)

    public User() {}

    // ✅ Constructor cũ giữ nguyên để không break code cũ
    public User(int MaUser, String Username, String Password, int MaRole) {
        this.MaUser   = MaUser;
        this.Username = Username;
        this.Password = Password;
        this.MaRole   = MaRole;
    }

    // ✅ Constructor mới có MaNV
    public User(int MaUser, String Username, String Password, int MaRole, int MaNV) {
        this.MaUser   = MaUser;
        this.Username = Username;
        this.Password = Password;
        this.MaRole   = MaRole;
        this.MaNV     = MaNV;
    }

    public int    getMaUser()                { return MaUser;   }
    public void   setMaUser(int MaUser)      { this.MaUser = MaUser; }

    public String getUsername()              { return Username; }
    public void   setUsername(String u)      { this.Username = u; }

    public String getPassword()              { return Password; }
    public void   setPassword(String p)      { this.Password = p; }

    public int    getMaRole()                { return MaRole;   }
    public void   setMaRole(int MaRole)      { this.MaRole = MaRole; }

    // ✅ MaNV — 0 nghĩa là user chưa được gắn với nhân viên nào (VD: tài khoản system)
    public int    getMaNV()                  { return MaNV; }
    public void   setMaNV(int MaNV)          { this.MaNV = MaNV; }

    /** Tiện ích: kiểm tra user có gắn với nhân viên không */
    public boolean hasEmployee()             { return MaNV > 0; }
}