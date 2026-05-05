package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.model.PhieuThue;
import otkhongluong.gamestoremanagement.service.ThueService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RentEditDialog extends JDialog {

    private final ThueService service = new ThueService();
    private final PhieuThue pt;

    private JTextField txtTenKH;
    private JTextField txtTenNV;

    private JTextField txtNgayThue;
    private JTextField txtNgayTraDK;
    private JTextField txtNgayTraTT;
    private JTextField txtTienCoc;

    private JLabel lblTienPhat;

    public RentEditDialog(Frame parent, PhieuThue pt) {
        super(parent, "Sửa Phiếu Thuê", true);
        this.pt = pt;

        setSize(520, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        add(buildForm(), BorderLayout.CENTER);
        add(buildButton(), BorderLayout.SOUTH);

        loadData();
    }

    // ================= FORM =================
    private JPanel buildForm() {
        JPanel p = new JPanel(new GridLayout(7, 2, 10, 10));

        txtTenKH = new JTextField();
        txtTenNV = new JTextField();

        txtNgayThue = new JTextField();
        txtNgayTraDK = new JTextField();
        txtNgayTraTT = new JTextField();
        txtTienCoc = new JTextField();
        lblTienPhat = new JLabel("0");

        // AUTOCOMPLETE
        setupAutoComplete(txtTenKH, service.getAllKhachHangNames());
        setupAutoComplete(txtTenNV, service.getAllNhanVienNames());

        txtNgayTraTT.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                updatePenalty();
            }
        });

        p.add(new JLabel("Khách hàng"));
        p.add(txtTenKH);

        p.add(new JLabel("Nhân viên"));
        p.add(txtTenNV);

        p.add(new JLabel("Ngày thuê"));
        p.add(txtNgayThue);

        p.add(new JLabel("Ngày trả dự kiến"));
        p.add(txtNgayTraDK);

        p.add(new JLabel("Ngày trả thực tế"));
        p.add(txtNgayTraTT);

        p.add(new JLabel("Tiền cọc"));
        p.add(txtTienCoc);

        p.add(new JLabel("Tiền phạt"));
        p.add(lblTienPhat);

        return p;
    }

    // ================= AUTOCOMPLETE SIMPLE =================
    private void setupAutoComplete(JTextField field, List<String> data) {

        JPopupMenu popup = new JPopupMenu();
        JList<String> list = new JList<>();

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        list.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                field.setText(list.getSelectedValue());
                popup.setVisible(false);
            }
        });

        field.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {

                String text = field.getText().toLowerCase();

                DefaultListModel<String> model = new DefaultListModel<>();

                for (String item : data) {
                    if (item.toLowerCase().contains(text)) {
                        model.addElement(item);
                    }
                }

                if (model.isEmpty()) {
                    popup.setVisible(false);
                    return;
                }

                list.setModel(model);
                popup.removeAll();
                popup.add(new JScrollPane(list));

                popup.show(field, 0, field.getHeight());
            }
        });
    }

    // ================= LOAD DATA =================
    private void loadData() {

        txtTenKH.setText(pt.getTenKhachHang());
        txtTenNV.setText(pt.getTenNhanVien());

        txtTienCoc.setText(String.valueOf(pt.getTienCoc()));

        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (pt.getNgayThue() != null)
            txtNgayThue.setText(pt.getNgayThue().format(f));

        if (pt.getNgayTraDuKien() != null)
            txtNgayTraDK.setText(pt.getNgayTraDuKien().format(f));

        if (pt.getNgayTraThucTe() != null)
            txtNgayTraTT.setText(pt.getNgayTraThucTe().format(f));
    }

    // ================= PENALTY =================
    private void updatePenalty() {
        try {
            if (txtNgayTraTT.getText().isEmpty()) return;

            LocalDateTime ngayTraTT =
                    LocalDateTime.parse(txtNgayTraTT.getText().replace(" ", "T"));

            double phat = service.tinhTienPhat(
                    pt,
                    ngayTraTT,
                    pt.getDanhSachChiTiet()
            );

            lblTienPhat.setText(String.valueOf(phat));

        } catch (Exception e) {
            lblTienPhat.setText("0");
        }
    }

    // ================= SAVE =================
    private JPanel buildButton() {

        JPanel p = new JPanel();

        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");

        btnSave.addActionListener(e -> {

            pt.setTenKhachHang(txtTenKH.getText());
            pt.setTenNhanVien(txtTenNV.getText());
            pt.setTienCoc(Double.parseDouble(txtTienCoc.getText()));

            try {
                DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                pt.setNgayTraDuKien(
                        LocalDateTime.parse(txtNgayTraDK.getText().replace(" ", "T"))
                );

                if (!txtNgayTraTT.getText().isEmpty()) {
                    pt.setNgayTraThucTe(
                            LocalDateTime.parse(txtNgayTraTT.getText().replace(" ", "T"))
                    );
                }

                boolean ok = service.updatePhieuThue(pt);

                if (ok) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                    dispose();
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Sai dữ liệu!");
            }
        });

        btnCancel.addActionListener(e -> dispose());

        p.add(btnSave);
        p.add(btnCancel);

        return p;
    }
}