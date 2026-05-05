package otkhongluong.gamestoremanagement.view.dialog;

import otkhongluong.gamestoremanagement.model.HoaDon;
import otkhongluong.gamestoremanagement.service.HoaDonService;
import javax.swing.*;
import java.awt.*;

public class TransactionDetailDialog {

    public static void open(Frame parent,String type,int id){

        type = type.toLowerCase();

        if(type.contains("hóa") || type.contains("hoa")){

            HoaDonService service = new HoaDonService();
            HoaDon hd = service.getHoaDonById(id);

            if(hd != null){
                new BillDetailDialog(parent, id).setVisible(true);
            }else{
                JOptionPane.showMessageDialog(parent,"Không tìm thấy hóa đơn!");
            }
        }
        else if(type.contains("thuê") || type.contains("thue")){
            new RentDetailDialog(parent,id).setVisible(true);
        }
    }
}