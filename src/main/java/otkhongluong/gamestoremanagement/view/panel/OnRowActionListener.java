package otkhongluong.gamestoremanagement.view.panel;

/**
 * Callback interface — View dùng để thông báo sự kiện "người dùng bấm Xem ở hàng N".
 *
 * MVC rule: ButtonEditor chỉ biết interface này, không biết Controller hay Panel cụ thể.
 * Ai muốn xử lý sự kiện thì implement interface này và truyền vào ButtonEditor.
 */
public interface OnRowActionListener {

    /**
     * Được gọi khi người dùng bấm nút "Xem" tại một hàng.
     *
     * @param rowIndex chỉ số hàng trong model (0-based)
     */
    void onRowAction(int rowIndex);
}