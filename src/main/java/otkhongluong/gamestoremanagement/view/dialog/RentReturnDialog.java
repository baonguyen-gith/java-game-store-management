package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.model.PhieuThue;
import otkhongluong.gamestoremanagement.service.ThueService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RentReturnDialog extends JDialog {

    private final ThueService service = new ThueService();
    private final int maPT;

    private JTextField txtNgayTra;
    private JLabel lblTienPhat;

    private PhieuThue phieuThue;

    public RentReturnDialog(Frame parent, int maPT) {
        super(parent, "TRẢ CD - PHIẾU THUÊ " + maPT, true);
        this.maPT = maPT;

        setSize(520, 420);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        phieuThue = service.getById(maPT);

        add(buildContent(), BorderLayout.CENTER);
        add(buildButton(), BorderLayout.SOUTH);

        setVisible(true);
    }

    // ================= UI =================
    private JPanel buildContent() {

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Ngày trả
        txtNgayTra = new JTextField();
        txtNgayTra.setText(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        txtNgayTra.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                updatePenaltyRealtime();
            }
        });

        // Tiền phạt
        lblTienPhat = new JLabel("0 VNĐ");
        lblTienPhat.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTienPhat.setForeground(Color.RED);

        panel.add(new JLabel("Ngày trả thực tế (dd/MM/yyyy):"));
        panel.add(txtNgayTra);

        panel.add(new JLabel("Ngày trả dự kiến:"));
        panel.add(new JLabel(String.valueOf(phieuThue.getNgayTraDuKien())));

        panel.add(new JLabel("Tiền phạt:"));
        panel.add(lblTienPhat);

        return panel;
    }

    // ================= BUTTON =================
    private JPanel buildButton() {

        JPanel panel = new JPanel();

        JButton btnXacNhan = new JButton("XÁC NHẬN TRẢ");
        btnXacNhan.setBackground(new Color(255, 140, 0));
        btnXacNhan.setForeground(Color.WHITE);

        btnXacNhan.addActionListener(e -> {

            try {
                String text = txtNgayTra.getText().trim();

                LocalDateTime ngayTra = LocalDateTime.parse(
                        text,
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                );

                if(phieuThue.getNgayTraDuKien() != null &&
                   ngayTra.isBefore(phieuThue.getNgayThue())){

                    JOptionPane.showMessageDialog(this,
                            "Ngày trả không được nhỏ hơn ngày thuê!");
                    return;
                }

                boolean ok = service.returnCD(maPT, ngayTra);

                if (ok) {
                    JOptionPane.showMessageDialog(this, "Trả CD thành công!");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Trả thất bại!");
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Sai định dạng ngày!");
            }
        });

        panel.add(btnXacNhan);

        return panel;
    }

    // ================= AUTO CALCULATE =================
    private void updatePenaltyRealtime() {

        try {
            if(phieuThue == null) return;

            String text = txtNgayTra.getText().trim();
            if(text.isEmpty()) {
                lblTienPhat.setText("0 VNĐ");
                return;
            }

            LocalDateTime ngayTra = LocalDateTime.parse(
                    text,
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            );

            double tienPhat = service.tinhTienPhat(
                    phieuThue,
                    ngayTra,
                    phieuThue.getDanhSachChiTiet()
            );

            lblTienPhat.setText(String.format("%.0f VNĐ", tienPhat));

        } catch (Exception ex) {
            lblTienPhat.setText("Sai định dạng ngày");
        }
    }
}