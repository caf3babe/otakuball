package at.ac.fhcampuswien;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;

//custom Gamebody to add drawing functionality
public class GameBody extends Body {

    public BufferedImage image;
    protected Color color;
    public GameBody() {
        this.color = Graphics2DRenderer.getRandomColor();
    }

    //render body, just for polygons and circles
    public void render(Graphics2D g, double scale) {
        this.render(g, scale, this.color);
    }

    //alternative method to render with color
    public void render(Graphics2D g, double scale, Color color) {

        // save the original transform
        AffineTransform ot = g.getTransform();

        // transform the coordinate system from world coordinates to local coordinates
        AffineTransform lt = new AffineTransform();
        lt.translate(this.transform.getTranslationX() * scale, this.transform.getTranslationY() * scale);
        lt.rotate(this.transform.getRotation());

        // apply the transform
        g.transform(lt);

        // loop over all the body fixtures for this body
        for (BodyFixture fixture : this.fixtures) {
            this.renderFixture(g, scale, fixture, color);
        }

        // set the original transform
        g.setTransform(ot);
    }

    //we also need to render the fixutre
    protected void renderFixture(Graphics2D g, double scale, BodyFixture fixture, Color color) {
        // get the shape on the fixture
        Convex convex = fixture.getShape();

        if (this.image != null) {
            // check the shape type
            if (convex instanceof org.dyn4j.geometry.Rectangle) {
                org.dyn4j.geometry.Rectangle r = (Rectangle)convex;
                Vector2 c = r.getCenter();
                double w = r.getWidth();
                double h = r.getHeight();

                g.drawImage(image,
                        (int)Math.ceil((c.x - w / 2.0) * scale),
                        (int)Math.ceil((c.y - h / 2.0) * scale),
                        (int)Math.ceil(w * scale),
                        (int)Math.ceil(h * scale),
                        null);
            } else if (convex instanceof Circle) {
                // cast the shape to get the radius
                Circle c = (Circle) convex;
                double r = c.getRadius();
                Vector2 cc = c.getCenter();
                int x = (int)Math.ceil((cc.x - r) * scale);
                int y = (int)Math.ceil((cc.y - r) * scale);
                int w = (int)Math.ceil(r * 2 * scale);
                // lets us an image instead
                g.drawImage(image, x, y, w, w, null);
            }
        } else {

            // render the fixture
            Graphics2DRenderer.render(g, convex, scale, color);
        }
    }

    public void setColor(Color color) {
        this.color = color;
    }
}