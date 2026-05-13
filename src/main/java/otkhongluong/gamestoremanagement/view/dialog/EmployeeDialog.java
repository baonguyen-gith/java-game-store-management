package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.model.NhanVien;
import otkhongluong.gamestoremanagement.service.NhanVienService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EmployeeDialog extends JDialog {

    private JTextField txtHoTen, txtSdt, txtCccd, txtNgaySinh, txtNgayVaoLam;
    private JButton btnSave, btnCancel;

    private NhanVienService service = new NhanVienService();
    private NhanVien currentNhanVien;
    private Runnable onSuccess;

    public EmployeeDialog(Frame parent, NhanVien nv, Runnable onSuccess) {
        super(parent, nv == null ? "Thêm nhân viên mới" : "Cập nhật nhân viên", true);
        this.currentNhanVien = nv;
        this.onSuccess = onSuccess;

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
        String hoTen = txtHoTen.getText().trim();
        String sdt = txtSdt.getText().trim();
        String cccd = txtCccd.getText().trim();
        String ngaySinhStr = txtNgaySinh.getText().trim();
        String ngayVaoLamStr = txtNgayVaoLam.getText().trim();

        if (hoTen.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ tên không được để trống!");
            return;
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate ngaySinh = null;
        LocalDate ngayVaoLam = null;

        try {
            if (!ngaySinhStr.isEmpty()) {
                ngaySinh = LocalDate.parse(ngaySinhStr, dtf);
            }
            if (!ngayVaoLamStr.isEmpty()) {
                ngayVaoLam = LocalDate.parse(ngayVaoLamStr, dtf);
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Sai định dạng ngày (dd/MM/yyyy)!");
            return;
        }

        if (currentNhanVien == null) {
            NhanVien nv = new NhanVien(0, hoTen, sdt, ngaySinh, cccd, ngayVaoLam);
            if (service.addNhanVien(nv)) {
                JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!");
                if (onSuccess != null) onSuccess.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi thêm nhân viên!");
            }
        } else {
            currentNhanVien.setHoTen(hoTen);
            currentNhanVien.setSdt(sdt);
            currentNhanVien.setCccd(cccd);
            currentNhanVien.setNgaySinh(ngaySinh);
            currentNhanVien.setNgayVaoLam(ngayVaoLam);

            if (service.updateNhanVien(currentNhanVien)) {
                JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thành công!");
                if (onSuccess != null) onSuccess.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi cập nhật nhân viên!");
            }
        }
    }
}
