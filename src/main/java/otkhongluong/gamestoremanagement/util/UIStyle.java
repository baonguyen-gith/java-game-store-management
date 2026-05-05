package otkhongluong.gamestoremanagement.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UIStyle {

    public static Color BG_MAIN = new Color(18,18,30);
    public static Color BG_CARD = new Color(33,35,50);
    public static Color SIDEBAR = new Color(23,26,33);
    public static Color HOVER = new Color(60,90,150);

    // ===== Steam Button =====
    public static void steamButton(JButton btn){

        btn.setForeground(Color.WHITE);
        btn.setBackground(SIDEBAR);

        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);

        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12,20,12,10));

        // 🔥 TẮT hover mặc định Swing
        btn.setRolloverEnabled(false);
        btn.getModel().setRollover(false);

        // reset UI delegate
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
    }

    // ===== Steam Card Hover =====
    public static void cardHover(JPanel card){

        card.addMouseListener(new MouseAdapter(){

            public void mouseEntered(MouseEvent e){
                card.setBackground(new Color(50,60,90));
                card.setBorder(BorderFactory.createLineBorder(HOVER,1));
            }

            public void mouseExited(MouseEvent e){
                card.setBackground(BG_CARD);
                card.setBorder(null);
            }
        });
    }
}