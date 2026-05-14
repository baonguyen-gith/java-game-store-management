package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.view.dialog.TransactionDetailDialog;

import javax.swing.*;
import java.awt.*;

public class ButtonEditorTransaction extends DefaultCellEditor {
    private final JButton button;
    private int currentRow = -1;
    private final OnRowActionListener listener;   // callback — không biết Dialog nào

    public ButtonEditorTransaction(OnRowActionListener listener) {
        super(new JTextField());  // convention chuẩn
        this.listener = listener;

        button = new JButton("Xem");
        button.setBackground(new Color(130, 90, 230));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.addActionListener(e -> {
            int snapshot = this.currentRow;
            fireEditingStopped();
            if (snapshot >= 0) listener.onRowAction(snapshot);
        });
    }

    @Override
    public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected, int row, int col) {
        this.currentRow = row;
        return button;
    }

    @Override public Object getCellEditorValue() { return "Xem"; }
}