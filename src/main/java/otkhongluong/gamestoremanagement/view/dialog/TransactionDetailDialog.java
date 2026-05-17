package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.controller.InvoiceController;
import otkhongluong.gamestoremanagement.controller.RentController;

import javax.swing.*;
import java.awt.*;

/**
 * MVC: không import Service, DAO, hay DBConnection.
 * Dùng InvoiceController để kiểm tra hóa đơn tồn tại,
 * dùng RentController để kiểm tra phiếu thuê tồn tại.
 */
public class TransactionDetailDialog {

    public static void open(Frame parent, String type, int id) {
        type = type.toLowerCase();

        if (type.contains("hóa") || type.contains("hoa")) {
            InvoiceController ctrl = new InvoiceController();
            if (ctrl.getHoaDonById(id) != null) {
                new InvoiceDetailDialog(parent, id).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(parent, "Không tìm thấy hóa đơn!");
            }
        } else if (type.contains("thuê") || type.contains("thue")) {
            RentController ctrl = new RentController();
            if (ctrl.getById(id) != null) {
                new RentDetailDialog(parent, id).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(parent, "Không tìm thấy phiếu thuê!");
            }
        }
    }
}