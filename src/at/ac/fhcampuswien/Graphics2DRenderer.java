package at.ac.fhcampuswien;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Polygon;;
import org.dyn4j.geometry.Shape;
import org.dyn4j.geometry.Vector2;

public final class Graphics2DRenderer {

    public static final void render(Graphics2D g, Shape shape, double scale, Color color) {
        // do nothing if no shape is included
        if (shape == null) return;
        if (shape instanceof Circle) {
            Graphics2DRenderer.render(g, (Circle) shape, scale, color);
        } else if (shape instanceof Polygon) {
            Graphics2DRenderer.render(g, (Polygon) shape, scale, color);
        } else {
            // unknown shape
        }
    }

    public static final void render(Graphics2D g, Circle circle, double scale, Color color) {
        double radius = circle.getRadius();
        Vector2 center = circle.getCenter();

        double radius2 = 2.0 * radius;
        Ellipse2D.Double c = new Ellipse2D.Double(
                (center.x - radius) * scale,
                (center.y - radius) * scale,
                radius2 * scale,
                radius2 * scale);

        // fill the shape
        g.setColor(color);
        g.fill(c);
        // draw the outline
        g.setColor(getOutlineColor(color));
        g.draw(c);
    }

    public static final void render(Graphics2D g, Polygon polygon, double scale, Color color) {
        Vector2[] vertices = polygon.getVertices();
        int l = vertices.length;

        // create the awt polygon
        Path2D.Double p = new Path2D.Double();
        p.moveTo(vertices[0].x * scale, vertices[0].y * scale);
        for (int i = 1; i < l; i++) {
            p.lineTo(vertices[i].x * scale, vertices[i].y * scale);
        }
        p.closePath();

        // fill the shape
        g.setColor(color);
        g.fill(p);
        // draw the outline
        g.setColor(getOutlineColor(color));
        g.draw(p);
    }

    private static final Color getOutlineColor(Color color) {
        Color oc = color.darker();
        return new Color(oc.getRed(), oc.getGreen(), oc.getBlue(), color.getAlpha());
    }

    public static final Color getRandomColor() {
        return new Color(
                (float) Math.random() * 0.5f + 0.5f,
                (float) Math.random() * 0.5f + 0.5f,
                (float) Math.random() * 0.5f + 0.5f);
    }
}
