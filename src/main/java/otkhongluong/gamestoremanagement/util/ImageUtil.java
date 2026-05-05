package otkhongluong.gamestoremanagement.util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageUtil {

    public static ImageIcon loadGameImage(String path, int w, int h){

        ImageIcon icon =
                new ImageIcon(ImageUtil.class.getResource(path));

        Image img = icon.getImage();

        BufferedImage resized = resizeAndCrop(img, w, h);

        return new ImageIcon(resized);
    }

    private static BufferedImage resizeAndCrop(Image img, int targetW, int targetH){

        int imgW = img.getWidth(null);
        int imgH = img.getHeight(null);

        double scale = Math.max(
                (double) targetW / imgW,
                (double) targetH / imgH
        );

        int newW = (int)(imgW * scale);
        int newH = (int)(imgH * scale);

        Image scaled =
                img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);

        BufferedImage buffered =
                new BufferedImage(targetW, targetH,
                        BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = buffered.createGraphics();

        int x = (targetW - newW)/2;
        int y = (targetH - newH)/2;

        g2.drawImage(scaled, x, y, null);
        g2.dispose();

        return buffered;
    }
}