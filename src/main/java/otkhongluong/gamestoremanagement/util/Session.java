package otkhongluong.gamestoremanagement.util;

/**
 * Session — lưu thông tin user đang đăng nhập.
 * FIX: thêm maRole để View phân quyền mà không cần query lại DB.
 */
public class Session {

    private static int    maNV;
    private static int    maRole;
    private static String username;

    /** Gọi khi login thành công. */
    public static void login(int maNV, int maRole, String username) {
        Session.maNV     = maNV;
        Session.maRole   = maRole;
        Session.username = username;
    }

    /** Gọi khi logout. */
    public static void logout() {
        maNV     = 0;
        maRole   = 0;
        username = null;
    }

    // ── Getters ───────────────────────────────────────
    public static int    getMaNV()     { return maNV; }
    public static int    getMaRole()   { return maRole; }
    public static String getUsername() { return username; }

    /** @return true nếu user hiện tại là Admin (MaRole == 1) */
    public static boolean isAdmin() { return maRole == 1; }

    /** @return true nếu user hiện tại là Staff (MaRole == 3 hoặc 4) */
    public static boolean isStaff() { return maRole == 3 || maRole == 4; }

    // ── Compat: giữ setMaNV để không break caller cũ ──
    /** @deprecated dùng {@link #login(int, int, String)} thay thế */
    @Deprecated
    public static void setMaNV(int id) { maNV = id; }
}