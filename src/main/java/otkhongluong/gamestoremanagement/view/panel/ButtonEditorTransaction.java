package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.view.dialog.TransactionDetailDialog;

import javax.swing.*;
import java.awt.*;

public class ButtonEditorTransaction extends DefaultCellEditor {

    private JButton button;
    private JTable table;
    private int row; // ⭐ LƯU ROW CLICK

    public ButtonEditorTransaction(JCheckBox checkBox, JTable table) {

        super(checkBox);

        this.table = table;

        button = new JButton("Xem");

        button.addActionListener(e -> {
            fireEditingStopped();   // bắt buộc
            openDetail();
        });
    }

    // ================= OPEN DETAIL =================
    private void openDetail() {

        try {

            // ⭐ LẤY ID
            String raw =
                    table.getValueAt(row,0).toString();

            int id =
                    Integer.parseInt(raw.replaceAll("\\D",""));

            // ⭐ LẤY TYPE
            String type =
                    table.getValueAt(row,1).toString();

            Frame frame =
                    (Frame) SwingUtilities.getWindowAncestor(table);

            TransactionDetailDialog.open(frame,type,id);

        } catch(Exception ex){

            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    table,
                    "Không mở được chi tiết!");
        }
    }

    // ⭐ QUAN TRỌNG NHẤT
    @Override
    public Component getTableCellEditorComponent(
            JTable table,Object value,
            boolean isSelected,int row,int column){

        this.row = row; // lưu row đúng

        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return "Xem";
    }
}