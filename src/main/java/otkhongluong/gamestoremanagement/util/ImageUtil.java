package otkhongluong.gamestoremanagement.util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

public class ImageUtil {

    public static ImageIcon loadGameImage(String path, int w, int h) {
        if (path == null || path.isBlank()) return null;

        Image img = null;

        // 1. Thử load từ file tuyệt đối trên máy (C:\..., /home/...)
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            img = new ImageIcon(file.getAbsolutePath()).getImage();
        }

        // 2. Thử load từ classpath / resource trong JAR
        if (img == null) {
            URL url = ImageUtil.class.getResource(path);
            if (url != null) {
                img = new ImageIcon(url).getImage();
            }
        }

        if (img == null) return null;

        return new ImageIcon(resizeAndCrop(img, w, h));
    }

    private static BufferedImage resizeAndCrop(Image img, int targetW, int targetH) {
        int imgW = img.getWidth(null);
        int imgH = img.getHeight(null);
        if (imgW <= 0 || imgH <= 0) return new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);

        double scale = Math.max(
                (double) targetW / imgW,
                (double) targetH / imgH
        );
        int newW = (int)(imgW * scale);
        int newH = (int)(imgH * scale);

        Image scaled = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage buffered = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = buffered.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int x = (targetW - newW) / 2;
        int y = (targetH - newH) / 2;
        g2.drawImage(scaled, x, y, null);
        g2.dispose();
        return buffered;
    }
}