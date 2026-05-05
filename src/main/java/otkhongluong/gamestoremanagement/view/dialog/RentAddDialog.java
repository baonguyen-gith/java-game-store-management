package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.dao.CDDAO;
import otkhongluong.gamestoremanagement.dao.KhachHangDAO;
import otkhongluong.gamestoremanagement.dao.PhieuThueDAO;
import otkhongluong.gamestoremanagement.model.PhieuThue;
import otkhongluong.gamestoremanagement.service.ThueService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

public class RentAddDialog extends JDialog {

    private JTable tblCD;
    private DefaultTableModel model;

    private JTextField txtSDT;
    private JTextField txtSoNgay;
    private JTextField txtDiemSuDung;

    private JLabel lblTenKH;
    private JLabel lblDiem;
    private JLabel lblTongTien;

    private final CDDAO cdDAO = new CDDAO();
    private final KhachHangDAO khDAO = new KhachHangDAO();
    private final PhieuThueDAO ptDAO = new PhieuThueDAO();
    private final ThueService service = new ThueService();

    private double giaThue = 0;
    private int selectedMaCD = -1;
    private int maKH = -1;

    public RentAddDialog(Frame parent) {
        super(parent, "Thuê Game", true);

        setSize(900, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        add(buildLeft(), BorderLayout.WEST);
        add(buildRight(), BorderLayout.CENTER);
    }

    // ================= LEFT: CD LIST =================
    private JPanel buildLeft() {

        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(450, 0));

        model = new DefaultTableModel(new Object[]{"Mã CD", "Game", "Giá"}, 0);
        tblCD = new JTable(model);

        loadCD();

        tblCD.getSelectionModel().addListSelectionListener(e -> {
            int row = tblCD.getSelectedRow();
            if (row >= 0) {
                selectedMaCD = Integer.parseInt(model.getValueAt(row, 0).toString());
                giaThue = Double.parseDouble(model.getValueAt(row, 2).toString());
                updateTotal();
            }
        });

        p.add(new JScrollPane(tblCD), BorderLayout.CENTER);
        return p;
    }

    private void loadCD() {
        model.setRowCount(0);
        List<Object[]> list = cdDAO.getAllAvailableCD();

        for (Object[] o : list) {
            model.addRow(o);
        }
    }

    // ================= RIGHT: FORM =================
    private JPanel buildRight() {

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        txtSDT = new JTextField();
        txtSoNgay = new JTextField("1");
        txtDiemSuDung = new JTextField("0");

        lblTenKH = new JLabel("-");
        lblDiem = new JLabel("-");
        lblTongTien = new JLabel("0");

        JButton btnCheckKH = new JButton("Check KH");
        btnCheckKH.addActionListener(e -> loadKH());

        JButton btnPay = new JButton("Thanh toán");
        btnPay.addActionListener(e -> thanhToan());

        txtSoNgay.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                updateTotal();
            }
        });

        txtDiemSuDung.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                updateTotal();
            }
        });

        p.add(new JLabel("SĐT KH"));
        p.add(txtSDT);
        p.add(btnCheckKH);

        p.add(new JLabel("Tên KH"));
        p.add(lblTenKH);

        p.add(new JLabel("Điểm hiện có"));
        p.add(lblDiem);

        p.add(new JLabel("Số ngày thuê"));
        p.add(txtSoNgay);

        p.add(new JLabel("Điểm sử dụng"));
        p.add(txtDiemSuDung);

        p.add(new JLabel("Tổng tiền"));
        p.add(lblTongTien);

        p.add(btnPay);

        return p;
    }

    // ================= KH =================
    private void loadKH() {

        String sdt = txtSDT.getText().trim();

        var kh = khDAO.findBySDT(sdt);

        if (kh == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy KH");
            return;
        }

        maKH = kh.getMaKH();
        lblTenKH.setText(kh.getHoTen());
        lblDiem.setText(String.valueOf(kh.getDiemTichLuy()));

        updateTotal();
    }

    // ================= CALC =================
    private void updateTotal() {

        try {
            int days = Integer.parseInt(txtSoNgay.getText().trim());
            int point = Integer.parseInt(txtDiemSuDung.getText().trim());

            double total = giaThue * days;

            total -= point * 1000; // quy đổi điểm

            if (total < 0) total = 0;

            lblTongTien.setText(String.format("%,.0f", total));

        } catch (Exception e) {
            lblTongTien.setText("0");
        }
    }

    // ================= PAYMENT =================
    private void thanhToan() {

        if (selectedMaCD == -1) {
            JOptionPane.showMessageDialog(this, "Chọn CD!");
            return;
        }

        if (maKH == -1) {
            JOptionPane.showMessageDialog(this, "Chọn KH!");
            return;
        }

        try {
            int days = Integer.parseInt(txtSoNgay.getText());
            int pointUse = Integer.parseInt(txtDiemSuDung.getText());

            LocalDateTime now = LocalDateTime.now();

            PhieuThue pt = new PhieuThue();
            pt.setMaKH(maKH);
            pt.setNgayThue(now);
            pt.setNgayTraDuKien(now.plusDays(days));
            pt.setTienCoc(0);
            pt.setTrangThai("DangThue");

            // detail
            PhieuThue.CTPhieuThue ct =
                    new PhieuThue.CTPhieuThue(selectedMaCD, "", giaThue, "OK");

            pt.getDanhSachChiTiet().add(ct);

            boolean ok = service.createPhieuThue(pt);

            if (ok) {

                // update CD
                new CDDAO().updateTrangThai(selectedMaCD, "DangThue");

                // update point
                khDAO.updatePoint(maKH, -pointUse);

                JOptionPane.showMessageDialog(this, "Thuê thành công!");
                dispose();

            } else {
                JOptionPane.showMessageDialog(this, "Thất bại!");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi dữ liệu!");
            e.printStackTrace();
        }
    }
}