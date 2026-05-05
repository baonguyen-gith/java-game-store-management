package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.util.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class RentDetailDialog extends JDialog {

    private DefaultTableModel model;
    private JTable table;

    public RentDetailDialog(Frame parent, int maPT) {

        super(parent, "Chi tiết phiếu thuê #" + maPT, true);

        setSize(720, 420);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10,10));

        /* ================= TITLE ================= */
        JLabel title = new JLabel(
                "CHI TIẾT PHIẾU THUÊ #" + maPT,
                JLabel.CENTER
        );
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setBorder(new EmptyBorder(10,10,10,10));
        add(title, BorderLayout.NORTH);

        /* ================= TABLE ================= */
        String[] cols = {
                "Mã CD",
                "Tên Game",
                "Tình trạng",
                "Đơn giá thuê"
        };

        model = new DefaultTableModel(cols, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);

        /* ================= LOAD ================= */
        loadData(maPT);
    }

    // ================= LOAD DATABASE =================
    private void loadData(int maPT) {

        String sql =
                "SELECT " +
                "cd.MaCD, " +   // ⚠️ FIX: bỏ CONCAT để format bên Java
                "g.TenGame, " +
                "cd.TinhTrang, " +
                "ct.DonGiaThue " +
                "FROM CTPHIEUTHUE ct " +
                "JOIN CD cd ON ct.MaCD = cd.MaCD " +
                "JOIN SANPHAM sp ON cd.MaSP = sp.MaSP " +
                "JOIN GAME g ON sp.MaGame = g.MaGame " +
                "WHERE ct.MaPT = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, maPT);
            ResultSet rs = ps.executeQuery();

            model.setRowCount(0); // 🔥 tránh trùng dữ liệu

            while (rs.next()) {

                String maCD = "CD" + rs.getInt("MaCD"); // format tại Java

                model.addRow(new Object[]{
                        maCD,
                        rs.getString("TenGame"),
                        rs.getString("TinhTrang"),
                        String.format("%,.0f đ", rs.getDouble("DonGiaThue")) // đẹp hơn
                });
            }

            // Nếu không có dữ liệu
            if(model.getRowCount() == 0){
                JOptionPane.showMessageDialog(
                        this,
                        "Phiếu thuê không có dữ liệu!"
                );
            }

        } catch (Exception e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi tải dữ liệu!"
            );
        }
    }
}