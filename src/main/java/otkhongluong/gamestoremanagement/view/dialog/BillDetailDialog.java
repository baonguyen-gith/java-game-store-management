package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.model.HoaDon;
import otkhongluong.gamestoremanagement.service.HoaDonService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class BillDetailDialog extends JDialog {

    private JTable table;
    private JLabel lblInfo;

    public BillDetailDialog(Frame parent, int maHD) {

        super(parent, "Chi tiết hóa đơn #" + maHD, true);

        setSize(600,400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        lblInfo = new JLabel();
        add(lblInfo, BorderLayout.NORTH);

        table = new JTable();
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 👉 LOAD FULL DATA từ DB
        HoaDon hd = new HoaDonService().getHoaDonById(maHD);

        loadData(hd);
    }

    private void loadData(HoaDon hd){

        if(hd == null){
            JOptionPane.showMessageDialog(this,"Không có dữ liệu!");
            dispose();
            return;
        }

        lblInfo.setText(
            "Khách hàng: " + hd.getTenKhachHang()
            + " | Tổng tiền: " + String.format("%,.0f đ", hd.getTongTien())
            + " | Trạng thái: " + hd.getTrangThai()
        );

        String[] cols = {"Tên Game","Loại SP","SL","Đơn giá"};

        DefaultTableModel model = new DefaultTableModel(cols,0);

        if(hd.getDanhSachChiTiet()!=null){
            for(HoaDon.ChiTietHoaDon ct : hd.getDanhSachChiTiet()){
                model.addRow(new Object[]{
                    ct.getTenGame(),
                    ct.getLoaiSanPham(),
                    ct.getSoLuong(),
                    String.format("%,.0f đ", ct.getDonGia())
                });
            }
        }

        table.setModel(model);
    }
}