package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.model.Employee;
import otkhongluong.gamestoremanagement.controller.EmployeeController;
import otkhongluong.gamestoremanagement.controller.EmployeeController.SaveResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class EmployeeDialog extends JDialog {

    private JTextField txtHoTen, txtSdt, txtCccd, txtNgaySinh, txtNgayVaoLam;
    private JButton btnSave, btnCancel;

    // ✅ Nhận controller từ bên ngoài, không tự tạo
    private final EmployeeController controller;
    private final Employee currentNhanVien;
    private final Runnable onSuccess;

    // ✅ Constructor mới: nhận thêm controller
    public EmployeeDialog(Frame parent, Employee nv, EmployeeController controller, Runnable onSuccess) {
        super(parent, nv == null ? "Thêm nhân viên mới" : "Cập nhật nhân viên", true);
        this.currentNhanVien = nv;
        this.controller      = controller; // ✅ dùng lại controller từ Panel, không new thêm
        this.onSuccess       = onSuccess;

        setSize(400, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // --- Form ---
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 20));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        formPanel.add(new JLabel("Họ tên:"));
        txtHoTen = new JTextField();
        formPanel.add(txtHoTen);

        formPanel.add(new JLabel("Số điện thoại:"));
        txtSdt = new JTextField();
        formPanel.add(txtSdt);

        formPanel.add(new JLabel("CCCD:"));
        txtCccd = new JTextField();
        formPanel.add(txtCccd);

        formPanel.add(new JLabel("Ngày sinh (dd/MM/yyyy):"));
        txtNgaySinh = new JTextField();
        formPanel.add(txtNgaySinh);

        formPanel.add(new JLabel("Ngày vào làm (dd/MM/yyyy):"));
        txtNgayVaoLam = new JTextField();
        formPanel.add(txtNgayVaoLam);

        add(formPanel, BorderLayout.CENTER);

        // --- Buttons ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnSave   = new JButton("Lưu");
        btnCancel = new JButton("Hủy");
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        add(btnPanel, BorderLayout.SOUTH);

        // --- Load dữ liệu nếu đang sửa ---
        if (currentNhanVien != null) loadDataToForm();

        // --- Events ---
        btnSave.addActionListener(e -> saveNhanVien());
        btnCancel.addActionListener(e -> dispose());
    }

    private void loadDataToForm() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        txtHoTen.setText(currentNhanVien.getHoTen());
        txtSdt.setText(currentNhanVien.getSdt()   != null ? currentNhanVien.getSdt()  : "");
        txtCccd.setText(currentNhanVien.getCccd() != null ? currentNhanVien.getCccd() : "");
        if (currentNhanVien.getNgaySinh()   != null) txtNgaySinh.setText(currentNhanVien.getNgaySinh().format(dtf));
        if (currentNhanVien.getNgayVaoLam() != null) txtNgayVaoLam.setText(currentNhanVien.getNgayVaoLam().format(dtf));
    }

    private void saveNhanVien() {
        // ✅ Dùng SaveResult thay vì boolean — View tự hiển thị thông báo
        SaveResult result = controller.handleSave(
            currentNhanVien,
            txtHoTen.getText(),
            txtSdt.getText(),
            txtCccd.getText(),
            txtNgaySinh.getText(),
            txtNgayVaoLam.getText()
        );

        // ✅ View hiện thông báo, không phải Controller
        JOptionPane.showMessageDialog(this, result.message);

        if (result.success) {
            if (onSuccess != null) onSuccess.run();
            dispose();
        }
    }
}