package otkhongluong.gamestoremanagement.model;

public class User {
    private int    maUser;
    private String username;
    private String password;   // ⭐ lưu ý: nên hash password, không lưu plaintext
    private int    maRole;
    private int    maNV;

    public User() {}

    public User(int maUser, String username, String password, int maRole) {
        this.maUser    = maUser;
        this.username  = username;
        this.password  = password;
        this.maRole    = maRole;
    }

    public User(int maUser, String username, String password, int maRole, int maNV) {
        this(maUser, username, password, maRole);
        this.maNV = maNV;
    }

    public int    getMaUser()               { return maUser; }
    public void   setMaUser(int maUser)     { this.maUser = maUser; }
    public String getUsername()             { return username; }
    public void   setUsername(String u)     { this.username = u; }
    public String getPassword()             { return password; }
    public void   setPassword(String p)     { this.password = p; }
    public int    getMaRole()               { return maRole; }
    public void   setMaRole(int maRole)     { this.maRole = maRole; }
    public int    getMaNV()                 { return maNV; }
    public void   setMaNV(int maNV)         { this.maNV = maNV; }

    public boolean hasEmployee()            { return maNV > 0; }
}