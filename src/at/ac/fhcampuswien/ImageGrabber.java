package at.ac.fhcampuswien;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ImageGrabber {

    private BufferedImage btr;

    public ImageGrabber(String pathOnClasspath) {
        try {
            BufferedImage br = ImageIO.read(Main.class.getClassLoader().getResource(pathOnClasspath));
            AffineTransform at = new AffineTransform();
            at.concatenate(AffineTransform.getScaleInstance(1, -1));
            at.concatenate(AffineTransform.getTranslateInstance(0, -br.getHeight()));
            btr = new BufferedImage(br.getWidth(), br.getHeight(), BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = btr.createGraphics();
            g.transform(at);
            g.drawImage(br, 0, 0, null);
            g.dispose();
        } catch (IOException e) {
        }
    }

    public BufferedImage getResource() {
        return this.btr;
    }
}
