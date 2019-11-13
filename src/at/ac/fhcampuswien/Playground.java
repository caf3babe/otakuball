package at.ac.fhcampuswien;

import org.dyn4j.dynamics.*;
import org.dyn4j.dynamics.joint.MotorJoint;
import org.dyn4j.geometry.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import static at.ac.fhcampuswien.CustomMouseAdapter.*;
import static at.ac.fhcampuswien.Main.*;

public class Playground extends Frame {

    public int goalCounter;
    private static int userShots = 4;  //decremented by initialization
    private static boolean goalFallen;
    private int activeLevel;
    private GameBody backButton;
    private GameBody ball;
    private GameBody floor;
    private Level level;
    private ManageHighscore highscoreManager;
    private boolean resetLevel;
    private boolean drawExplosionImage;
    private Vector2 drawImageVector;
    private double timer;
    private List<SoundPlayer> ballBounceSounds;
    private SoundPlayer ballKickSound;
    private SoundPlayer ballfBounceSound;
    private SoundPlayer ballsBounceSound;


    public Playground() {
        super("Otakuball", 128.0); //needed for calculation dyn4j coordinates
        // setup the mouse listening
        MouseAdapter ml = new CustomMouseAdapter();
        this.canvas.addMouseMotionListener(ml);
        this.canvas.addMouseListener(ml);
        this.goalCounter = 0;
        this.activeLevel = 0;
        this.goalFallen = false;
        this.highscoreManager = new ManageHighscore();
        this.drawExplosionImage = false;
    }

    public static void setGoalFallen() {
        goalFallen = true;
    }

    public void initializeWorld() {
        setLocationRelativeTo(null);
        world = new World();

        GameBody ceiling = new GameBody();
        ceiling.addFixture(Geometry.createRectangle(FRAMEWIDTH / scale, 1 / scale));
        ceiling.setMass(MassType.INFINITE);
        ceiling.translate(0, FRAMEHEIGHT / scale / 2);
        world.addBody(ceiling);

        floor = new GameBody();
        floor.addFixture(Geometry.createRectangle(FRAMEWIDTH / scale, 1 / scale));
        floor.setMass(MassType.INFINITE);
        floor.translate(0, -FRAMEHEIGHT / scale / 2);
        world.addBody(floor);

        GameBody rightBorder = new GameBody();
        rightBorder.addFixture(Geometry.createRectangle(1 / scale, FRAMEHEIGHT / scale));
        rightBorder.setMass(MassType.INFINITE);
        rightBorder.translate(FRAMEWIDTH / scale / 2, 0);
        world.addBody(rightBorder);

        GameBody leftBorder = new GameBody();
        leftBorder.addFixture(Geometry.createRectangle(1 / scale, FRAMEHEIGHT / scale));
        leftBorder.setMass(MassType.INFINITE);
        leftBorder.translate(-FRAMEWIDTH / scale / 2, 0);
        world.addBody(leftBorder);

        ball = new GameBody();
        ball.image = new ImageGrabber("images/SoccerBall.png").getResource();
        //density --> masse
        //friction --> reibung
        //restitution --> sprungkraft
        ball.addFixture(Geometry.createCircle(0.14), 0.115, 1, 0.5);
        ball.setMass(MassType.NORMAL);
        ball.setAutoSleepingEnabled(false);
        ball.translate(-6.5, 0);
        ball.setLinearDamping(1);

        BufferedImage buttonImage = new ImageGrabber("images/back_transparent.png").getResource();
        backButton = new GameBody();
        backButton.image = buttonImage;
        backButton.setColor(new Color(50, 50, 50, 50));
        backButton.addFixture(Geometry.createRectangle(2, 0.5));
        backButton.translate((FRAMEWIDTH / 2 / scale) - 1.25, (FRAMEHEIGHT / 2 / scale) - 0.5);
        backButton.setMass(MassType.INFINITE);

        level = new Level(world, scale, ball);
        switch (goalCounter) {
            case 0:
                level.initiateLevel1();
                break;
            case 1:
                level.initiateLevel2();
                break;
            case 2:
                level.initiateLevel3();
                break;
            case 3:
                level.initiateLevel4();
                break;
            case 4:
                level.initiateLevel5();
                break;
            case 5:
                level.initiateLevel6();
                break;
        }
        activeLevel = level.getLevelNumber();

        if (level.getThemeSong() != null) {
            level.getThemeSong().startPlaying();
        }

        world.addBody(ball);
        world.addBody(backButton);
        MotorJoint control = new MotorJoint(level.getLevelBackground(), ball);
        control.setCollisionAllowed(false);
        world.addJoint(control);
        System.out.println("Level " + level.getLevelNumber() + " initiated");

        dragPoint = null;
        pressPoint = null;
        releasePoint = null;

        this.ballBounceSounds = new LinkedList<>();
        this.ballfBounceSound = new SoundPlayer("sounds/first_ball_bounce.wav");
        this.ballsBounceSound = new SoundPlayer("sounds/second_ball_bounce.wav");
        ballBounceSounds.add(ballfBounceSound);
        ballBounceSounds.add(ballsBounceSound);

    }

    //override method in Frame - this adds the ballshot over mouselistener
    public void update(Graphics2D g, double elapsedTime) {

        //In every gameloop we are gonna check if a goal fell and act accordingly


        if (goalFallen) {
            drawExplosionImage = true;
            resetLevel = false;
            drawImageVector = new Vector2(ball.getWorldCenter().x * scale - 100, ball.getWorldCenter().y * scale - 100);
            level.stopAllSounds();
            level.getWonSound().startPlaying();
            ball.setActive(false);
            goalFallen = false;
        }

        if (drawExplosionImage) {
            g.drawImage(level.getExplosionImage(), (int) drawImageVector.x, (int) drawImageVector.y, 200, 200, this);
            timer += elapsedTime;
            if (timer > level.getWonSound().getClipDurationInSeconds()) {
                timer = 0;
                drawExplosionImage = false;

                if (goalCounter == Main.getTotalLevelNumber()) {
                    this.stop();
                    this.dispose();
                    Main.m.removeResumeButton();
                    Main.m.setVisible(true);
                } else {
                    this.initializeWorld();
                }
                highscoreManager.handleHighscore(userShots);
                userShots = 4;
            }
        }

        //if there is a press and releaspoint calculate the distance and set the ball linear velocity
        if (pressPoint != null && level.getUserShots() < 3 && !goalFallen) {
            if (releasePoint != null) {
                double xPress = pressPoint.getX();
                double yPress = pressPoint.getY();
                double xRelease = releasePoint.getX();
                double yRelease = releasePoint.getY();

                double xVelocity = (xPress - xRelease) / scale * 8;
                double yVelocity = (yPress - yRelease) / scale * -8;

                if (xVelocity > 0 && yVelocity > 0) {
                    if (xVelocity > 15) {
                        xVelocity = 15;
                    }
                    if (yVelocity > 15) {
                        yVelocity = 15;
                    }
                    this.ball.setLinearVelocity(xVelocity, yVelocity);
                    this.ballKickSound = new SoundPlayer("sounds/ball_kick.wav");
                    this.ballKickSound.startPlaying();

                    this.ballfBounceSound = new SoundPlayer("sounds/first_ball_bounce.wav");
                    this.ballsBounceSound = new SoundPlayer("sounds/second_ball_bounce.wav");
                    ballBounceSounds.add(ballfBounceSound);
                    ballBounceSounds.add(ballsBounceSound);

                    this.ball.setAutoSleepingEnabled(false);

                    level.riseUserShots();

                    resetLevel = true;

                    userShots--;
                } else {
                    double dPx = (releasePoint.getX() - (FRAMEWIDTH / 2)) / scale;
                    double dPy = -(releasePoint.getY() - (FRAMEHEIGHT / 2)) / scale;
                    double buPx = this.backButton.getWorldCenter().x - 1;
                    double buPy = this.backButton.getWorldCenter().y + 0.25;
                    double bbPx = this.backButton.getWorldCenter().x + 1;
                    double bbPy = this.backButton.getWorldCenter().y - 0.25;

                    if (!(dPx > buPx && dPx < bbPx && dPy < buPy && dPy > bbPy)) {
                        Point currLocation = this.getLocationOnScreen();
                        Point position1 = new Point(currLocation.x + 5, currLocation.y + (-10));
                        Point position2 = new Point(currLocation.x - 5, currLocation.y - (-10));
                        for (int i = 0; i < 40; i++) {
                            this.setLocation(position1);
                            this.setLocation(position2);
                        }
                        this.setLocation(currLocation);
                    }
                }
                pressPoint = null;
                releasePoint = null;
                dragPoint = null;

            }
            if (dragPoint != null) {

                double x1 = pressPoint.getX() - (FRAMEWIDTH / 2);
                double y1 = -(pressPoint.getY() - (FRAMEHEIGHT / 2));
                double x2 = dragPoint.getX() - (FRAMEWIDTH / 2);
                double y2 = -(dragPoint.getY() - (FRAMEHEIGHT / 2));


                Line2D l = new Line2D.Double(x1, y1, x2, y2);
                g.setColor(Color.RED);
                g.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
                g.draw(l);
            }
        }

        //That is kind of a workaround, cause we cannot directly use J-Items in World.
        if (clickPoint != null) {
            double cPx = (clickPoint.getX() - (FRAMEWIDTH / 2)) / scale;
            double cPy = -(clickPoint.getY() - (FRAMEHEIGHT / 2)) / scale;
            double buPx = this.backButton.getWorldCenter().x - 1;
            double buPy = this.backButton.getWorldCenter().y + 0.25;
            double bbPx = this.backButton.getWorldCenter().x + 1;
            double bbPy = this.backButton.getWorldCenter().y - 0.25;

            if (cPx > buPx && cPx < bbPx && cPy < buPy && cPy > bbPy) {
                pause();
                dispose();
                Main.m.createResumeButton();
                Main.m.setVisible(true);
                level.stopAllSounds();

            }
            CustomMouseAdapter.clickPoint = null;
        }

        //Write them shots on upper left corner
        if (activeLevel==6 && level.getUserShots()==3) {
            writeUpperLeftString("It's useless, Goku just can't be beaten",Color.RED,g);

        } else {
            writeUpperLeftString("Used " + level.getUserShots() + ((level.getUserShots() == 1) ? " Shot" : " Shots") + " of 3",((level.getUserShots() == 3) ? Color.RED : level.getFontColor()),g);
        }

        //Reset the ball after 3 tries
        if (level.getLevelGround() instanceof GameBody) {
            if (level.getUserShots() == 3 && resetLevel && Math.abs(ball.getLinearVelocity().x) <= 0.02 && Math.abs(ball.getLinearVelocity().y) <= 0.02 && Math.abs(ball.getAngularVelocity()) <= 0.05 && ball.getForce().x < 0.02 && ball.getForce().y < 0.02 && ball.isInContact(level.getLevelGround())) {
                level.stopAllSounds();
                //but we will reinitilize current level
                this.initializeWorld();
            }
        }

        super.update(g, elapsedTime);

        //reset level without ground -- workaround
        if (activeLevel == 4 && goalCounter == 3 && level.getUserShots() == 3) {


            timer += elapsedTime;
            if (timer > 6) {
                goalCounter = 3;
                timer = 0;
                initializeWorld();
            }
        }

        if (ballBounceSounds != null && !ballBounceSounds.isEmpty()) {
            if (this.ball.isInContact(level.getLevelGround())) {
                ballBounceSounds.get(0).startPlaying();
                ballBounceSounds.remove(0);

            }
        }

        double pos = ball.getTransform().getTranslationY();
        if (goalCounter == 5) {
            if (level.getGoku() != null) {
                // reset the transform of the controller body
                Transform tx = new Transform();
                tx.translate(0, pos);
                level.getGoku().setTransform(tx);
            }
        }
    }

    public int getGoalCounter() {
        return goalCounter;
    }

    public void setGoalCounter(int goalCounter) {
        this.goalCounter = goalCounter;
    }

    public void doNotResetLevel() {
        this.resetLevel = false;
    }

    public void writeUpperLeftString(String s,Color c,Graphics2D g){
        g.setColor(c);
        g.setFont(font);
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
        String drawableString = s;
        g.scale(1.0, -1.0);
        g.drawString(drawableString, (int) (-FRAMEWIDTH / 2 + (0.25 * scale)), (int) (-FRAMEHEIGHT / 2 + (0.50 * scale)));
    }

    public boolean isDrawExplosionImageTrue(){
        return drawExplosionImage;
    }

    public Level getLevel() {
        return this.level;
    }
}