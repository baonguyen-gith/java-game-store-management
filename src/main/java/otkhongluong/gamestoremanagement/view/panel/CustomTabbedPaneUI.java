    package otkhongluong.gamestoremanagement.view.panel;

    import javax.swing.plaf.basic.BasicTabbedPaneUI;
    import java.awt.*;

    public class CustomTabbedPaneUI extends BasicTabbedPaneUI {

        private final Color ACTIVE   = new Color(130, 90, 230); // tím sáng
        private final Color INACTIVE = new Color(40, 25, 85);   // tím đậm
        private final Color TEXT     = Color.WHITE;

        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabAreaInsets = new Insets(10, 10, 0, 10);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement,
                                          int tabIndex, int x, int y, int w, int h, boolean isSelected) {

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(isSelected ? ACTIVE : INACTIVE);

            // bo góc
            g2.fillRoundRect(x, y, w, h, 20, 20);
        }

        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font,
                                 FontMetrics metrics, int tabIndex,
                                 String title, Rectangle textRect, boolean isSelected) {

            g.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g.setColor(TEXT);

            int x = textRect.x;
            int y = textRect.y + metrics.getAscent();

            g.drawString(title, x, y);
        }

        @Override
        protected void paintFocusIndicator(Graphics g, int tabPlacement,
                                            Rectangle[] rects, int tabIndex,
                                            Rectangle iconRect, Rectangle textRect,
                                            boolean isSelected) {
            // ❌ bỏ viền focus xấu
        }
        @Override
        protected void paintTabBorder(Graphics g, int tabPlacement,
                                      int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            // ❌ không vẽ viền tab
        }
        
        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            // ❌ không vẽ viền content
        }
        
        @Override
        protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(new Color(13, 13, 35)); // màu nền bạn muốn
            g2.fillRect(0, 0, tabPane.getWidth(), tabPane.getHeight());

            super.paintTabArea(g, tabPlacement, selectedIndex);
        }

        @Override
        protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
            return 40; // chiều cao tab
        }

        @Override
        protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
            return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + 20;
        }
    }