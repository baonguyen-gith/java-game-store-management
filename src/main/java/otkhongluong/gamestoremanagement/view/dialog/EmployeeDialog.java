package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.model.Employee;
import otkhongluong.gamestoremanagement.controller.EmployeeController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EmployeeDialog extends JDialog {

    private JTextField txtHoTen, txtSdt, txtCccd, txtNgaySinh, txtNgayVaoLam;
    private JButton btnSave, btnCancel;

    private EmployeeController controller;
    private Employee currentNhanVien;
    private Runnable onSuccess;

    public EmployeeDialog(Frame parent, Employee nv, Runnable onSuccess) {
        super(parent, nv == null ? "Thêm nhân viên mới" : "Cập nhật nhân viên", true);
        this.currentNhanVien = nv;
        this.onSuccess = onSuccess;
        controller = new EmployeeController((JComponent) getContentPane());
        setSize(400, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

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

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnSave = new JButton("Lưu");
        btnCancel = new JButton("Hủy");

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);

        add(btnPanel, BorderLayout.SOUTH);

        if (currentNhanVien != null) {
            loadDataToForm();
        }

        btnSave.addActionListener(e -> saveNhanVien());
        btnCancel.addActionListener(e -> dispose());
    }

    private void loadDataToForm() {
        txtHoTen.setText(currentNhanVien.getHoTen());
        txtSdt.setText(currentNhanVien.getSdt());
        txtCccd.setText(currentNhanVien.getCccd());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (currentNhanVien.getNgaySinh() != null) {
            txtNgaySinh.setText(currentNhanVien.getNgaySinh().format(dtf));
        }
        if (currentNhanVien.getNgayVaoLam() != null) {
            txtNgayVaoLam.setText(currentNhanVien.getNgayVaoLam().format(dtf));
        }
    }

    private void saveNhanVien() {
        boolean ok = controller.handleSave(
                currentNhanVien,
                txtHoTen.getText(),
                txtSdt.getText(),
                txtCccd.getText(),
                txtNgaySinh.getText(),
                txtNgayVaoLam.getText()
        );
        if (ok) {
            if (onSuccess != null) onSuccess.run();
            dispose();
        }
    }
}
