package otkhongluong.gamestoremanagement.util;


public class Session {
    private static int maNV;

    public static void setMaNV(int id) {
        maNV = id;
    }

    public static int getMaNV() {
        return maNV;
    }
}