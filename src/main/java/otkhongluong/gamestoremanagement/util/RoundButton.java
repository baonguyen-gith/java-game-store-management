package otkhongluong.gamestoremanagement.util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Nút bo góc dùng chung toàn app.
 * Trước đây bị duplicate trong ~6 panel — giờ dùng class này.
 */
public class RoundButton extends JButton {

    private Color bg;
    private final Color fg;

    public RoundButton(String text, Color bg, Color fg) {
        super(text);
        this.bg = bg;
        this.fg = fg;
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setForeground(fg);
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Cho phép đổi màu nền lúc runtime (dùng cho nút Lọc toggle). */
    @Override
    public void setBackground(Color bg) {
        this.bg = bg;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(!isEnabled()
            ? bg.darker()
            : getModel().isRollover() ? bg.brighter() : bg);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
        super.paintComponent(g2);
        g2.dispose();
    }
}