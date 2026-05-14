package otkhongluong.gamestoremanagement.view.panel;

import javax.swing.*;
import javax.swing.DefaultCellEditor;
import java.awt.*;

public class ButtonEditor extends DefaultCellEditor {

    private final JButton button;
    private int currentRow = -1;
    private final OnRowActionListener listener;

    public ButtonEditor(OnRowActionListener listener) {
        super(new JTextField()); // ✅ Fix 3: JTextField là convention chuẩn
        this.listener = listener;

        button = new JButton("Xem");
        button.setBackground(new Color(130, 90, 230));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.addActionListener(e -> handleClick());
    }

    private void handleClick() {
        int rowSnapshot = this.currentRow;  // ✅ Fix 1: snapshot trước
        fireEditingStopped();
        if (rowSnapshot >= 0)
            listener.onRowAction(rowSnapshot);
    }

    @Override
    public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected, int row, int column) {
        this.currentRow = row;
        button.setText("Xem"); // ✅ Fix 2: reset phòng reuse
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return "Xem";
    }
}