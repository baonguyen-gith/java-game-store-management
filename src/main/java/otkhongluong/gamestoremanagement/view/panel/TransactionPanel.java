package otkhongluong.gamestoremanagement.view.panel;

import otkhongluong.gamestoremanagement.dao.TransactionDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TransactionPanel extends JPanel {

    public TransactionPanel(){

        setLayout(new BorderLayout());

        JLabel title =
                new JLabel("LỊCH SỬ GIAO DỊCH",JLabel.CENTER);
        title.setFont(new Font("Arial",Font.BOLD,20));

        add(title,BorderLayout.NORTH);

        DefaultTableModel model =
                new DefaultTableModel(
                        new String[]{"ID","Loại","Ngày","Tiền","Chi tiết"},0);

        JTable table = new JTable(model);

        add(new JScrollPane(table),BorderLayout.CENTER);

        TransactionDAO dao = new TransactionDAO();

        for(Object[] row : dao.findAll())
            model.addRow(row);

        table.getColumn("Chi tiết")
                .setCellEditor(
                        new ButtonEditorTransaction(new JCheckBox(),table));
    }
}