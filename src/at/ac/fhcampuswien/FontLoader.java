package at.ac.fhcampuswien;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class FontLoader {

    public FontLoader() {
        InputStream font = this.getClass().getClassLoader().getResourceAsStream("fonts/mangat.ttf");
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, font));
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }
}
