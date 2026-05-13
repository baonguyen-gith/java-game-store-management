package otkhongluong.gamestoremanagement.util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class IconUtils {

    public static Icon getAddIcon(int size, Color color) {
        return new CustomIcon(size, color) {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                setupGraphics(g2);
                g2.translate(x, y);
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int pad = size / 4;
                g2.drawLine(size / 2, pad, size / 2, size - pad);
                g2.drawLine(pad, size / 2, size - pad, size / 2);
                g2.dispose();
            }
        };
    }

    public static Icon getEditIcon(int size, Color color) {
        return new CustomIcon(size, color) {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                setupGraphics(g2);
                g2.translate(x, y);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                int p = size / 5;
                Path2D path = new Path2D.Double();
                path.moveTo(size - p, p * 1.5);
                path.lineTo(size - p * 1.5, p);
                path.lineTo(p, size - p * 2);
                path.lineTo(p, size - p);
                path.lineTo(p * 2, size - p);
                path.closePath();
                g2.draw(path);
                g2.drawLine(size - p * 2, (int)(p * 1.5), (int)(size - p * 1.5), p * 2);
                
                g2.dispose();
            }
        };
    }

    public static Icon getDeleteIcon(int size, Color color) {
        return new CustomIcon(size, color) {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                setupGraphics(g2);
                g2.translate(x, y);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                int p = size / 5;
                // lid
                g2.drawLine(p, (int)(p * 1.5), size - p, (int)(p * 1.5));
                g2.drawLine(p * 2, (int)(p * 1.5), p * 2, p);
                g2.drawLine(size - p * 2, (int)(p * 1.5), size - p * 2, p);
                g2.drawLine(p * 2, p, size - p * 2, p);
                
                // bin
                g2.drawRect((int)(p * 1.5), (int)(p * 1.5), size - p * 3, size - (int)(p * 2.5));
                g2.drawLine(p * 2, p * 2, p * 2, size - p * 2);
                g2.drawLine(size - p * 2, p * 2, size - p * 2, size - p * 2);
                g2.drawLine(size / 2, p * 2, size / 2, size - p * 2);

                g2.dispose();
            }
        };
    }

    public static Icon getRefreshIcon(int size, Color color) {
        return new CustomIcon(size, color) {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                setupGraphics(g2);
                g2.translate(x, y);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                int pad = size / 5;
                g2.drawArc(pad, pad, size - pad * 2, size - pad * 2, 45, 270);
                
                // Arrow head
                g2.drawLine(size - pad, pad * 2, size - pad, size / 2);
                g2.drawLine(size - pad * 2, pad, size - pad, pad * 2);

                g2.dispose();
            }
        };
    }

    public static Icon getUserIcon(int size, Color color) {
        return new CustomIcon(size, color) {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                setupGraphics(g2);
                g2.translate(x, y);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                int pad = size / 5;
                g2.drawOval(size / 2 - pad, pad, pad * 2, pad * 2);
                g2.drawArc(pad, size / 2, size - pad * 2, size - pad, 0, 180);

                g2.dispose();
            }
        };
    }

    public static Icon getSearchIcon(int size, Color color) {
        return new CustomIcon(size, color) {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                setupGraphics(g2);
                g2.translate(x, y);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int radius = size / 2;
                g2.drawOval(0, 0, radius, radius);
                g2.drawLine(radius - 2, radius - 2, size - 2, size - 2);

                g2.dispose();
            }
        };
    }

    public static Icon getCalendarIcon(int size, Color color) {
        return new CustomIcon(size, color) {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                setupGraphics(g2);
                g2.translate(x, y);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int pad = size / 6;
                // calendar body
                g2.drawRoundRect(pad, pad * 2, size - pad * 2, size - pad * 3, 4, 4);
                // header line
                g2.drawLine(pad, pad * 3 + 2, size - pad, pad * 3 + 2);
                // rings
                g2.drawLine(pad * 2, pad, pad * 2, pad * 2 + 2);
                g2.drawLine(size - pad * 2, pad, size - pad * 2, pad * 2 + 2);

                g2.dispose();
            }
        };
    }

    public static Icon getReturnIcon(int size, Color color) {
        return new CustomIcon(size, color) {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                setupGraphics(g2);
                g2.translate(x, y);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int pad = size / 5;
                // U-turn arrow
                g2.drawArc(pad, pad, size - pad * 2, size - pad * 2, -90, 180);
                g2.drawLine(pad, size / 2, pad, size - pad);
                // arrow head
                g2.drawLine(pad, size / 2, pad * 2, size / 2 + pad);
                g2.drawLine(pad, size / 2, 0, size / 2 + pad);

                g2.dispose();
            }
        };
    }

    private static abstract class CustomIcon implements Icon {
        protected int size;
        protected Color color;

        public CustomIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        protected void setupGraphics(Graphics2D g2) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}
