package otkhongluong.gamestoremanagement.service;

import java.util.regex.Pattern;

public class ValidationService {

    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " không được để trống.");
        }
    }

    public static void validateEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        if (email != null && !email.isEmpty() && !pattern.matcher(email).matches()) {
            throw new IllegalArgumentException("Email không hợp lệ.");
        }
    }

    public static void validatePhone(String phone) {
        String phoneRegex = "^[0-9]{10,11}$";
        Pattern pattern = Pattern.compile(phoneRegex);
        if (phone == null || !pattern.matcher(phone).matches()) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ (phải từ 10-11 chữ số).");
        }
    }

    public static void validateDate(String date) {
        // Có thể mở rộng để check định dạng ngày
        if (date == null || date.trim().isEmpty()) {
            throw new IllegalArgumentException("Ngày không được để trống.");
        }
    }
}
