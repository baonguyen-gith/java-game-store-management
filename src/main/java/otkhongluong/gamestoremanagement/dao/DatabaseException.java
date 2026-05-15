package otkhongluong.gamestoremanagement.dao;

/**
 * Runtime exception bọc mọi lỗi SQL từ tầng DAO.
 *
 * Dùng RuntimeException để:
 *  - Caller không bắt buộc try-catch (unchecked)
 *  - Vẫn giữ nguyên stack trace gốc qua cause
 *  - Service/Controller có thể bắt nếu cần xử lý riêng
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(String message) {
        super(message);
    }
}