package at.ac.fhcampuswien;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class CustomMouseAdapter extends MouseAdapter {

    public static Point pressPoint;
    public static Point releasePoint;
    public static Point dragPoint;
    public static Point clickPoint;

    @Override
    public void mousePressed(MouseEvent e) {
        //create a point and store it for future need
        pressPoint = new Point(e.getX(), e.getY());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        dragPoint = new Point(e.getX(), e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        releasePoint = new Point(e.getX(), e.getY());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        clickPoint = new Point(e.getX(), e.getY());
    }
}