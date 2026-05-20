package otkhongluong.gamestoremanagement.controller;

import otkhongluong.gamestoremanagement.model.User;

/**
 * Interface điều hướng màn hình sau khi đăng nhập.
 *
 * ✅ Tách routing ra khỏi Controller — Controller không biết View cụ thể nào.
 * Implementation (ví dụ AppNavigator) mới biết AdminView, StaffView.
 */
public interface Navigator {
    void goToLogin();
    void goToAdmin(User user);
    void goToStaff(User user);
    void goToManager(User user);
}