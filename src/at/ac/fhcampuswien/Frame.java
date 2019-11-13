package at.ac.fhcampuswien;

import org.dyn4j.dynamics.World;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import javax.swing.*;

import static at.ac.fhcampuswien.Main.FRAMEWIDTH;
import static at.ac.fhcampuswien.Main.FRAMEHEIGHT;

public abstract class Frame extends JFrame {

    // conversion factor from nano to base for timestamps
    public static final double NANO_TO_BASE = 1.0e9;

    protected final Canvas canvas;
    protected static World world;

    // conversion factor from pixel to metric
    protected final double scale;

    private boolean stopped;
    private boolean paused;

    // last update time stamp
    private long last;

    public Frame(String name, double scale) {
        super(name);

        // set the scale
        this.scale = scale;

        // create the world
        this.world = new World();

        // closing window closes JFrame
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // disable title menu
        this.setUndecorated(true);

        // window listener
        this.addWindowListener(new WindowAdapter() {

            //if window gets closed stop the animation
            @Override
            public void windowClosing(WindowEvent e) {
                stop();
                super.windowClosing(e);
            }
        }
        );

        // create the size of the window
        Dimension size = new Dimension(FRAMEWIDTH, FRAMEHEIGHT);

        // create a canvas to paint to
        this.canvas = new Canvas();
        this.canvas.setPreferredSize(size);
        this.canvas.setMinimumSize(size);
        this.canvas.setMaximumSize(size);

        // add the canvas to the JFrame
        this.add(this.canvas);


        // make the JFrame not resizable
        this.setResizable(false);

        // size everything
        this.pack();

        // setup the world
        this.initializeWorld();
    }

    // creates game objects and adds them to the world
    protected abstract void initializeWorld();

    // starts the simulation
    private void start() {

        // initialize the last update time
        this.last = System.nanoTime();

        // initialise an empty canvas
        this.canvas.setIgnoreRepaint(false);

        // enable double buffering
        this.canvas.createBufferStrategy(2);

        // run a separate thread to do active rendering
        Thread thread = new Thread() {
            public void run() {
                // infite loop for rendering
                while (!isStopped()) {
                    gameLoop();
                    try {
                        Thread.sleep(0);
                    } catch (InterruptedException e) {}
                }
            }
        };

        // set the game loop thread to a daemon
        thread.setDaemon(true);

        // start the game loop
        thread.start();
    }

    // where the magic happens
    private void gameLoop() {

        // get the graphics object to render to
        Graphics2D g = (Graphics2D)this.canvas.getBufferStrategy().getDrawGraphics();

        // sets (0/0) to be the center of the screen
        this.transform(g);

        // clears the view
        this.clear(g);

        // get the current time
        long time = System.nanoTime();

        // get the elapsed time from the last update
        long diff = time - this.last;

        // set the last time
        this.last = time;

        // converts from nanoseconds to seconds
        double elapsedTime = (double)diff / NANO_TO_BASE;

        // renders anything about the simulation
        this.render(g, elapsedTime);

        if (!paused) {
            // updates the World
            this.update(g, elapsedTime);
        }

        // dispose of the graphics object
        g.dispose();

        // show everything on the canvas
        BufferStrategy strategy = this.canvas.getBufferStrategy();
        if (!strategy.contentsLost()) {
            strategy.show();
        }
    }

    //sets (0/0) from left upper corner to center
    protected void transform(Graphics2D g) {
        final int w = this.canvas.getWidth();
        final int h = this.canvas.getHeight();

        AffineTransform yFlip = AffineTransform.getScaleInstance(1, -1);
        AffineTransform move = AffineTransform.getTranslateInstance(w / 2, -h / 2);
        g.transform(yFlip);
        g.transform(move);
    }

    // clears world - all white
    protected void clear(Graphics2D g) {
        final int w = this.canvas.getWidth();
        final int h = this.canvas.getHeight();

        g.setColor(Color.WHITE);
        g.fillRect(-w / 2, -h / 2, w, h);
    }

    // renders world
    protected void render(Graphics2D g, double elapsedTime) {
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // draw all the objects in the world
        for (int i = 0; i < this.world.getBodyCount(); i++) {
            // get the object
            GameBody body = (GameBody) this.world.getBody(i);
            this.render(g, elapsedTime, body);
        }
    }

    // renders body
    protected void render(Graphics2D g, double elapsedTime, GameBody body) {
        // draw the object
        body.render(g, this.scale);
    }

    // updates the world
    protected void update(Graphics2D g, double elapsedTime)
    {
        // update the world with the elapsed time
        this.world.update(elapsedTime);
    }

    // stops the simulation
    public synchronized void stop() {
        this.stopped = true;
    }

    // returns true if the simulation is stopped
    public boolean isStopped() {
        return this.stopped;
    }

    // pause the simulation
    public synchronized void pause() {
        this.paused = true;
    }

    // resumes to simulation
    public synchronized void resume() {
        this.paused = false;
    }

    // returns true if the simulation is paused
    public boolean isPaused() {
        return this.paused;
    }

    // starts the simulation
    public void run() {
        // set the look and feel to the system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // show it
        this.setVisible(true);

        // start it
        this.start();
    }
}