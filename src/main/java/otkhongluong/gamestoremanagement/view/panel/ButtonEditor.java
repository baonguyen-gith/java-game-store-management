package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.view.dialog.BillDetailDialog;
import otkhongluong.gamestoremanagement.view.dialog.RentDetailDialog;
import otkhongluong.gamestoremanagement.view.dialog.TransactionDetailDialog;

import javax.swing.*;
import javax.swing.DefaultCellEditor;
import java.awt.*;

public class ButtonEditor extends DefaultCellEditor {

    private JButton button;
    private JTable table;
    private String type;
    private int row;

    public ButtonEditor(JCheckBox checkBox,
                        JTable table,
                        String type) {

        super(checkBox);

        this.table = table;
        this.type = type;

        button = new JButton("Xem");
        button.setFocusPainted(false);

        button.addActionListener(e -> openDetail());
    }

    /* ================= OPEN POPUP ================= */
    private void openDetail() {

        Frame frame = (Frame) SwingUtilities.getWindowAncestor(table);

        try {
            // 👉 Lấy ID trực tiếp từ cột 0
            String raw = table.getValueAt(row, 0).toString();
            int id = Integer.parseInt(raw.replaceAll("\\D", ""));

            if (type.equals("HOADON")) {

                new BillDetailDialog(frame, id).setVisible(true);

            } else if (type.equals("PHIEUTHUE")) {

                new RentDetailDialog(frame, id).setVisible(true);

            } else if (type.equals("GIAODICH")) {

                TransactionDetailDialog.open(frame, type, id);
            }

        } catch (Exception ex) {

            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    table,
                    "Không mở được chi tiết!");
        }

        fireEditingStopped();
    }

    /* ===== LẤY ROW ===== */
    @Override
    public Component getTableCellEditorComponent(
            JTable table,
            Object value,
            boolean isSelected,
            int row,
            int column) {

        this.row = row;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return "Xem";
    }
}