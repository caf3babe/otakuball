package at.ac.fhcampuswien;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.World;
import org.dyn4j.dynamics.joint.MotorJoint;
import org.dyn4j.dynamics.joint.RopeJoint;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static at.ac.fhcampuswien.Main.FRAMEHEIGHT;
import static at.ac.fhcampuswien.Main.FRAMEWIDTH;

public class Level {

    private World world;
    private double scale;
    private GameBody background;
    private GameBody ball;
    private GameBody goku;
    private GameBody ground;
    private SoundPlayer themeSong;
    private SoundPlayer levelLostSound;
    private SoundPlayer wonSound;
    private Color writingColor;
    private BufferedImage explosionImage;

    private int userShots;
    private int levelNumber;
    public static int gokuCollideCount;

    //Constructor with World and Ball
    public Level(World world, double scale, GameBody ball) {
        this.world = world;
        this.scale = scale;
        this.ball = ball;
        this.wonSound = new SoundPlayer("sounds/explosion2.wav");
        this.explosionImage = new ImageGrabber("images/explosion.png").getResource();
        this.userShots = 0;
        this.writingColor = Color.black;
    }

    public void initiateLevel1() {
        levelNumber = 1;
        themeSong = new SoundPlayer("sounds/theme_lvl1.wav");
        createBackground("images/Background.png");
        //creating a rectangle to simulate the goal
        //creating a rectangle to simulate the goal

        //creating goals crossbar
        GameBody crossbar = new GameBody();
        crossbar.addFixture(Geometry.createRectangle(1, 0.1));
        crossbar.setMass(MassType.INFINITE);
        crossbar.translate((FRAMEWIDTH / 2 / scale) - 0.5, (-FRAMEHEIGHT / 2 / scale) + 0.23 + 1.5 + 0.05);
        crossbar.setColor(Color.WHITE);
        world.addBody(crossbar);

        GameBody goal = new GameBody();
        goal.addFixture(Geometry.createRectangle(0.1, 1.5));
        goal.setMass(MassType.INFINITE);
        goal.translate((FRAMEWIDTH / 2 / scale) - 1 + 0.05, (-FRAMEHEIGHT / 2 / scale) + 0.23 + 0.75);
        goal.setColor(Color.WHITE);
        world.addBody(goal);

        BufferedImage FASS = new ImageGrabber("images/fass2d.png").getResource();

        ground = new GameBody();
        ground.addFixture(Geometry.createRectangle(FRAMEWIDTH / scale, 1 / scale));
        ground.setMass(MassType.INFINITE);
        ground.translate(0.0, (-FRAMEHEIGHT / 2 / scale) + 0.23);
        //set transparent color
        ground.setColor(new Color(0, 0, 0, 0));
        world.addBody(ground);

        double obsWidth = 500 / scale / 10;
        double obsHeight = 589 / scale / 10;

        //creating obstacles
        GameBody obstacle1 = new GameBody();
        obstacle1.image = FASS;
        obstacle1.addFixture(Geometry.createRectangle(obsWidth, obsHeight), 0.1, 0.2, 0.5);
        obstacle1.setMass(MassType.NORMAL);
        obstacle1.translate(2, (-FRAMEHEIGHT / 2 / scale) + obsHeight);
        obstacle1.setAutoSleepingEnabled(false);
        world.addBody(obstacle1);

        GameBody obstacle2 = new GameBody();
        obstacle2.image = FASS;
        obstacle2.addFixture(Geometry.createRectangle(obsWidth, obsHeight), 0.1, 0.2, 0.5);
        obstacle2.setMass(MassType.NORMAL);
        obstacle2.translate(2, (-FRAMEHEIGHT / 2 / scale) + (obsHeight * 2));
        obstacle2.setAutoSleepingEnabled(false);
        world.addBody(obstacle2);

        GameBody obstacle3 = new GameBody();
        obstacle3.image = FASS;
        obstacle3.addFixture(Geometry.createRectangle(obsWidth, obsHeight), 0.1, 0.2, 0.5);
        obstacle3.setMass(MassType.NORMAL);
        obstacle3.translate(2, (-FRAMEHEIGHT / 2 / scale) + (obsHeight * 3));
        obstacle3.setAutoSleepingEnabled(false);
        world.addBody(obstacle3);

        GameBody obstacle4 = new GameBody();
        obstacle4.image = FASS;
        obstacle4.addFixture(Geometry.createRectangle(obsWidth, obsHeight), 0.1, 0.2, 0.5);
        obstacle4.setMass(MassType.NORMAL);
        obstacle4.translate(2, (-FRAMEHEIGHT / 2 / scale) + (obsHeight * 4));
        obstacle4.setAutoSleepingEnabled(false);
        world.addBody(obstacle4);

        MotorJoint control2 = new MotorJoint(background, obstacle1);
        control2.setCollisionAllowed(false);
        world.addJoint(control2);

        MotorJoint control3 = new MotorJoint(background, obstacle2);
        control3.setCollisionAllowed(false);
        world.addJoint(control3);

        MotorJoint control4 = new MotorJoint(background, obstacle3);
        control4.setCollisionAllowed(false);
        world.addJoint(control4);

        MotorJoint control5 = new MotorJoint(background, obstacle4);
        control5.setCollisionAllowed(false);
        world.addJoint(control5);
        GoalListener goalListener = new GoalListener(ball, goal);
        world.addListener(goalListener);

    }

    public void initiateLevel2() {

        levelNumber = 2;

        BufferedImage sawBladeImage = new ImageGrabber("images/blade.png").getResource();
        BufferedImage fixedBrickImage = new ImageGrabber("images/fixBrick.png").getResource();
        BufferedImage goalImage = new ImageGrabber("images/lvl2_goal.png").getResource();
        BufferedImage brickImage = new ImageGrabber("images/brick.png").getResource();
        BufferedImage tunnelImage = new ImageGrabber("images/tunnel.png").getResource();

        themeSong = new SoundPlayer("sounds/lvl2_theme.wav");
        levelLostSound = new SoundPlayer("sounds/died.wav");
        wonSound = new SoundPlayer("sounds/stage2_clear.wav");
        explosionImage = null;

        createBackground("images/lvl2_background.png");

        ball.translate(0, -2);

        double brickWidth = 82 / scale / 1.5;
        double brickHeigth = 73 / scale / 1.5;

        double firstBrickPositition = -5;

        Vector2 p = new Vector2(firstBrickPositition + brickWidth * 11, brickHeigth - 1.5);
        Vector2 c = new Vector2(firstBrickPositition + brickWidth * 22, -2);


        GameBody fixedBrick = new GameBody();
        fixedBrick.image = fixedBrickImage;
        fixedBrick.addFixture(Geometry.createRectangle(brickWidth, brickHeigth));
        fixedBrick.setMass(MassType.INFINITE);
        fixedBrick.translate(c.x, c.y);
        world.addBody(fixedBrick);

        GameBody fixedBrick1 = new GameBody();
        fixedBrick1.image = fixedBrickImage;
        fixedBrick1.addFixture(Geometry.createRectangle(brickWidth, brickHeigth));
        fixedBrick1.setMass(MassType.INFINITE);
        fixedBrick1.translate(p.x, p.y);
        world.addBody(fixedBrick1);

        GameBody sawBladeL = new GameBody();
        sawBladeL.image = sawBladeImage;
        sawBladeL.addFixture(Geometry.createCircle(0.40), 5);
        sawBladeL.setMass(MassType.FIXED_ANGULAR_VELOCITY);
        sawBladeL.setAngularVelocity(9);
        sawBladeL.setLinearVelocity(-2, 2);
        sawBladeL.translate(p.x, p.y - 1.7);
        sawBladeL.setGravityScale(0);

        GameBody sawBladeR = new GameBody();
        sawBladeR.image = sawBladeImage;
        sawBladeR.addFixture(Geometry.createCircle(0.40), 5);
        sawBladeR.setMass(MassType.FIXED_ANGULAR_VELOCITY);
        sawBladeR.setAngularVelocity(9);
        sawBladeR.setLinearVelocity(2, 2);
        sawBladeR.translate(c.x, c.y - 1.2);
        sawBladeR.setGravityScale(0);
        world.addBody(sawBladeR);

        RopeJoint ropeJointL = new RopeJoint(sawBladeR, fixedBrick, sawBladeR.getWorldCenter(), new Vector2(fixedBrick.getWorldCenter().x, fixedBrick.getWorldCenter().y));
        world.addJoint(ropeJointL);

        RopeJoint ropeJointR = new RopeJoint(sawBladeL, fixedBrick1, sawBladeL.getWorldCenter(), new Vector2(fixedBrick1.getWorldCenter().x, fixedBrick1.getWorldCenter().y));
        world.addJoint(ropeJointR);

        ArrayList<Vector2> positions = new ArrayList<>();

        Vector2 a = new Vector2(firstBrickPositition, -1.5);
        Vector2 b = new Vector2(firstBrickPositition + brickWidth, -1.5);
        Vector2 cc = new Vector2(firstBrickPositition + brickWidth * 2, -1.5);
        Vector2 d = new Vector2(firstBrickPositition + brickWidth * 3, -1.5);
        Vector2 g = new Vector2(firstBrickPositition + brickWidth * 6, brickHeigth - 1.5);
        Vector2 h = new Vector2(firstBrickPositition + brickWidth * 7, brickHeigth - 1.5);
        Vector2 i = new Vector2(firstBrickPositition + brickWidth * 8, brickHeigth - 1.5);
        Vector2 j = new Vector2(firstBrickPositition + brickWidth * 9, brickHeigth - 1.5);
        Vector2 k = new Vector2(firstBrickPositition + brickWidth * 10, brickHeigth - 1.5);
        Vector2 o = new Vector2(firstBrickPositition + brickWidth * 12, brickHeigth - 1.5);
        Vector2 q = new Vector2(firstBrickPositition + brickWidth * 21, -2);
        Vector2 r = new Vector2(firstBrickPositition + brickWidth * 23, -2);

        positions.add(a);
        positions.add(b);
        positions.add(cc);
        positions.add(d);
        positions.add(g);
        positions.add(h);
        positions.add(i);
        positions.add(j);
        positions.add(k);
        positions.add(o);
        positions.add(q);
        positions.add(r);


        for (Vector2 position : positions) {
            GameBody brick = new GameBody();
            brick.image = brickImage;
            brick.addFixture(Geometry.createRectangle(brickWidth, brickHeigth));
            brick.setMass(MassType.INFINITE);
            brick.translate(position.x, position.y);
            world.addBody(brick);
            BrickCollisionListener brickCollisionListener = new BrickCollisionListener(ball, brick, background);
            world.addListener(brickCollisionListener);
            MotorJoint mj = new MotorJoint(sawBladeL, brick);
            MotorJoint mj1 = new MotorJoint(sawBladeR, brick);
            mj.setCollisionAllowed(false);
            mj1.setCollisionAllowed(false);
            world.addJoint(mj);
            world.addJoint(mj1);
        }

        world.addBody(sawBladeL);

        ground = new GameBody();
        ground.addFixture(Geometry.createRectangle(FRAMEWIDTH / scale, 1 / scale));
        ground.setMass(MassType.INFINITE);
        ground.translate(0.0, (-FRAMEHEIGHT / 2 / scale) + 0.7);
        //set transparent color
        ground.setColor(new Color(0, 0, 0, 0));

        GameBody goalAppearance = new GameBody();
        goalAppearance.image = goalImage;
        goalAppearance.setMass(MassType.INFINITE);
        goalAppearance.addFixture(Geometry.createRectangle(1, 1.5));
        goalAppearance.translate(FRAMEWIDTH / scale / 2 - 0.5, -FRAMEHEIGHT / scale / 2 + 1 + 0.45);
        world.addBody(goalAppearance);

        GameBody crossbar = new GameBody();
        crossbar.setMass(MassType.INFINITE);
        crossbar.addFixture(Geometry.createRectangle(1, 0.01));
        crossbar.setColor(new Color(0, 0, 0, 0));
        crossbar.translate(FRAMEWIDTH / 2 / scale - 0.5, (-FRAMEHEIGHT / 2 / scale) + 0.7 + 1.5);
        world.addBody(crossbar);

        GameBody realGoal = new GameBody();
        realGoal.addFixture(Geometry.createRectangle(0.1, 1.2));
        realGoal.setMass(MassType.INFINITE);
        realGoal.translate(FRAMEWIDTH / scale / 2 - 0.70, -FRAMEHEIGHT / scale / 2 + 0.60 + 0.7);
        realGoal.setColor(new Color(0, 0, 0, 0));
        world.addBody(realGoal);

        world.addBody(ground);
        world.addListener(new GoalListener(ball, realGoal));
        world.addListener(new BladeCollisionListener(ball, sawBladeL, ground));
        world.addListener(new BladeCollisionListener(ball, sawBladeR, ground));


        ArrayList<Body> firstBody = new ArrayList<Body>();
        ArrayList<Body> secondBody = new ArrayList<Body>();

        firstBody.add(background);
        secondBody.add(sawBladeL);

        firstBody.add(background);
        secondBody.add(sawBladeR);

        firstBody.add(sawBladeL);
        secondBody.add(ground);

        firstBody.add(background);
        secondBody.add(ball);

        firstBody.add(sawBladeR);
        secondBody.add(ground);

        firstBody.add(ball);
        secondBody.add(goalAppearance);

        for (int iii = 0; iii < 6; iii++) {
            MotorJoint mj = new MotorJoint(firstBody.get(iii), secondBody.get(iii));
            mj.setCollisionAllowed(false);
            world.addJoint(mj);
        }

        GameBody tunnel = new GameBody();
        tunnel.image = tunnelImage;
        tunnel.addFixture(Geometry.createRectangle(97 / scale, 98 / scale));
        tunnel.setMass(MassType.INFINITE);
        tunnel.translate(2, ground.getWorldCenter().y + 98 / 2 / scale);
        System.out.println();
        System.out.println(ground.getWorldPoint(new Vector2(0)));

        world.addBody(tunnel);


    }

    public void initiateLevel3() {

        levelNumber = 3;


        //Sound playing during Level3 **********************************************************************************
        themeSong = new SoundPlayer("sounds/sega_lvl3.wav");
        wonSound = new SoundPlayer("sounds/gol_lvl3.wav");


        //Input images of all obstacles ********************************************************************************
        BufferedImage middlewall1 = new ImageGrabber("images/salah.png").getResource();
        BufferedImage middlewall2 = new ImageGrabber("images/solanke.png").getResource();
        BufferedImage middlewall3 = new ImageGrabber("images/lallana.png").getResource();
        BufferedImage rightwall = new ImageGrabber("images/mane.png").getResource();
        BufferedImage leftwall = new ImageGrabber("images/firmino.png").getResource();
        BufferedImage cameraimg = new ImageGrabber("images/camera_transparent.png").getResource();
        BufferedImage keeperimg = new ImageGrabber("images/alisson.png").getResource();

        createBackground("images/tribüne_level3goal.png");


        //OBSTACLES: attribution and rectangles, images put on rectangles **********************************************
        GameBody camera = new GameBody();
        camera.image = cameraimg;
        camera.addFixture(Geometry.createRectangle(0.7, 0.7));
        camera.setMass(MassType.INFINITE);
        camera.translate((FRAMEWIDTH / 2 / scale) - 6.3, (FRAMEHEIGHT / 2 / scale) - 2.0);
        world.addBody(camera);

        GameBody playerleft = new GameBody();
        playerleft.image = leftwall;
        playerleft.addFixture(Geometry.createRectangle(0.8, 1.8));
        playerleft.setMass(MassType.INFINITE);
        playerleft.translate((FRAMEWIDTH / 2 / scale) - 11.15, (FRAMEHEIGHT / 2 / scale) - 7.5);
        world.addBody(playerleft);

        GameBody playermiddle1 = new GameBody();
        playermiddle1.image = middlewall1;
        playermiddle1.addFixture(Geometry.createRectangle(0.8, 1.8), 1, 2, 0.965);
        playermiddle1.setMass(MassType.FIXED_ANGULAR_VELOCITY);
        playermiddle1.translate(-2.85, -1.5);
        world.addBody(playermiddle1);

        GameBody playermiddle2 = new GameBody();
        playermiddle2.image = middlewall2;
        playermiddle2.addFixture(Geometry.createRectangle(1.2, 1.8), 1, 2, 0.965);
        playermiddle2.setMass(MassType.FIXED_ANGULAR_VELOCITY);
        playermiddle2.translate(-1.85, -0.8);
        world.addBody(playermiddle2);

        GameBody playermiddle3 = new GameBody();
        playermiddle3.image = middlewall3;
        playermiddle3.addFixture(Geometry.createRectangle(1.1, 1.8), 1, 2, 0.965);
        playermiddle3.setMass(MassType.FIXED_ANGULAR_VELOCITY);
        playermiddle3.translate(-0.70, -1.5);
        world.addBody(playermiddle3);

        GameBody playerright = new GameBody();
        playerright.image = rightwall;
        playerright.addFixture(Geometry.createRectangle(0.9, 1.8));
        playerright.setMass(MassType.INFINITE);
        playerright.translate((FRAMEWIDTH / 2 / scale) - 7.0, (FRAMEHEIGHT / 2 / scale) - 7.5);
        world.addBody(playerright);

        GameBody keeper = new GameBody();
        keeper.image = keeperimg;
        keeper.addFixture(Geometry.createRectangle(1.2, 1.8));
        keeper.setMass(MassType.INFINITE);
        keeper.translate((FRAMEWIDTH / 2 / scale) - 3.6, (FRAMEHEIGHT / 2 / scale) - 7.5);
        world.addBody(keeper);


        //GOAL: goallistener and goalspot-rectangles as obstacles ******************************************************
        GameBody realGoal = new GameBody();
        realGoal.addFixture(Geometry.createRectangle(0.02, 1.3));
        realGoal.setMass(MassType.INFINITE);
        realGoal.translate((FRAMEWIDTH / 2 / scale) - 0.65, (-FRAMEHEIGHT / 2 / scale) - 0.1 + 1);
        realGoal.setColor(new Color(10, 10, 10, 10));
        world.addBody(realGoal);

        GameBody goalspot = new GameBody();
        goalspot.addFixture(Geometry.createRectangle(0.02, 1.5));
        goalspot.setMass(MassType.INFINITE);
        goalspot.translate((FRAMEWIDTH / 2 / scale) - 0.6, (-FRAMEHEIGHT / 2 / scale) - 0.1 + 1);
        goalspot.setColor(new Color(10, 10, 10, 10));
        world.addBody(goalspot);

        GameBody goalspot2 = new GameBody();
        goalspot2.addFixture(Geometry.createRectangle(1.2, 0.02));
        goalspot2.setMass(MassType.INFINITE);
        goalspot2.translate((FRAMEWIDTH / 2 / scale) - 4, (-FRAMEHEIGHT / 2 / scale) - 1.9);
        goalspot2.rotate((FRAMEWIDTH / 2 / scale) - 0.55);
        goalspot2.setColor(new Color(10, 10, 10, 10));
        world.addBody(goalspot2);


        ground = new GameBody();
        ground.addFixture(Geometry.createRectangle(FRAMEWIDTH / scale, 16 / scale));
        ground.setMass(MassType.INFINITE);
        ground.translate(0.0, (-FRAMEHEIGHT / 2 / scale) + 0.05);
        ground.setColor(new Color(10, 10, 10, 10));
        world.addBody(ground);


        //JOINT: every moveable object added as a joint and added to world *********************************************
        MotorJoint joint1 = new MotorJoint(background, playermiddle1);
        joint1.setCollisionAllowed(false);

        MotorJoint joint2 = new MotorJoint(background, playermiddle2);
        joint2.setCollisionAllowed(false);

        MotorJoint joint3 = new MotorJoint(background, playermiddle3);
        joint3.setCollisionAllowed(false);

        MotorJoint joint4 = new MotorJoint(background, ball);
        joint4.setCollisionAllowed(false);

        world.addJoint(joint2);
        world.addJoint(joint1);
        world.addJoint(joint3);
        world.addJoint(joint4);


        //goalimage, when you score a goal and goalListener, to check if there was scored a goal or not ****************
        this.explosionImage = new ImageGrabber("images/goal_popup.png").getResource();
        world.addListener(new GoalListener(ball, realGoal));

    }

    public void initiateLevel4() {

        levelNumber = 4;

        //for birds view effect
        world.setGravity(World.ZERO_GRAVITY);

        createBackground("images/Level4.png");

        //deactivate collisions between ball and background
        MotorJoint control2 = new MotorJoint(background, ball);
        control2.setCollisionAllowed(false);
        world.addJoint(control2);

        //new startpoint
        ball.translate(+4.5, -1.5);

        GameBody goal = new GameBody();
        goal.addFixture(Geometry.createRectangle(0.5, 1.3));
        goal.setMass(MassType.INFINITE);
        goal.translate((FRAMEWIDTH / 2 / scale) - 1.2, (-FRAMEHEIGHT / 2 / scale) + 6.5);
        goal.setColor(new Color(1, 1, 1, 1));
        //goal.setColor(Color.RED);
        world.addBody(goal);

        //invisible borders
        ground = new GameBody();
        ground.addFixture(Geometry.createRectangle(FRAMEWIDTH / scale, 16 / scale));
        ground.setMass(MassType.INFINITE);
        ground.translate((-FRAMEHEIGHT / 2 / scale) + 7.0, (-FRAMEHEIGHT / 2 / scale) + 2.3);
        //set transparent color
        ground.setColor(new Color(1, 1, 1, 1));
        //ground.setColor(Color.RED);
        world.addBody(ground);

        GameBody wallbar = new GameBody();
        wallbar.addFixture(Geometry.createRectangle(5, 16 / scale));
        wallbar.setMass(MassType.INFINITE);
        wallbar.translate((-FRAMEHEIGHT / 2 / scale) + 2.6, (-FRAMEHEIGHT / 2 / scale) + 3.3);
        //set transparent color
        wallbar.setColor(new Color(1, 1, 1, 1));
        //wallbar.setColor(Color.RED);
        world.addBody(wallbar);

        GameBody walltoys = new GameBody();
        walltoys.addFixture(Geometry.createRectangle(5, 16 / scale));
        walltoys.setMass(MassType.INFINITE);
        walltoys.translate((-FRAMEHEIGHT / 2 / scale) + 8.4, (-FRAMEHEIGHT / 2 / scale) + 6.8);
        //set transparent color
        walltoys.setColor(new Color(1, 1, 1, 1));
        //walltoys.setColor(Color.RED);
        world.addBody(walltoys);

        GameBody walltain = new GameBody();
        walltain.addFixture(Geometry.createRectangle(5, 16 / scale));
        walltain.setMass(MassType.INFINITE);
        walltain.translate((-FRAMEHEIGHT / 2 / scale) + 6.8, (-FRAMEHEIGHT / 2 / scale) + 1.8);
        //set transparent color
        walltain.setColor(new Color(1, 1, 1, 1));
        //walltain.setColor(Color.RED);
        walltain.rotate((FRAMEWIDTH / 2 / scale) - 0.65);
        world.addBody(walltain);

        GameBody wallhosp = new GameBody();
        wallhosp.addFixture(Geometry.createRectangle(4, 16 / scale));
        wallhosp.setMass(MassType.INFINITE);
        wallhosp.translate((-FRAMEHEIGHT / 2 / scale) + 6.2, (-FRAMEHEIGHT / 2 / scale) + 3.1);
        //set transparent color
        wallhosp.setColor(new Color(1, 1, 1, 1));
        //wallhosp.setColor(Color.RED);
        wallhosp.rotate((FRAMEWIDTH / 2 / scale) - 0.45);
        world.addBody(wallhosp);

        GameBody wallbank = new GameBody();
        wallbank.addFixture(Geometry.createRectangle(0.2, 3));
        wallbank.setMass(MassType.INFINITE);
        wallbank.translate((-FRAMEHEIGHT / 2 / scale) + 9, (-FRAMEHEIGHT / 2 / scale) + 4.5);
        //set transparent color
        wallbank.setColor(new Color(1, 1, 1, 1));
        //wallbank.setColor(Color.RED);
        world.addBody(wallbank);

        GameBody goalground = new GameBody();
        goalground.addFixture(Geometry.createRectangle(2, 16 / scale));
        goalground.setMass(MassType.INFINITE);
        goalground.translate((-FRAMEHEIGHT / 2 / scale) + 10, (-FRAMEHEIGHT / 2 / scale) + 6);
        //set transparent color
        goalground.setColor(new Color(1, 1, 1, 1));
        //goalground.setColor(Color.RED);
        world.addBody(goalground);

        GameBody walllast = new GameBody();
        walllast.addFixture(Geometry.createRectangle(0.2, 3));
        walllast.setMass(MassType.INFINITE);
        walllast.translate((-FRAMEHEIGHT / 2 / scale) + 7.7, (-FRAMEHEIGHT / 2 / scale) + 7.5);
        //set transparent color
        walllast.setColor(new Color(1, 1, 1, 1));
        //walllast.setColor(Color.RED);
        world.addBody(walllast);

        world.addListener(new GoalListener(ball, goal));

    }

    public void initiateLevel5() {

        levelNumber = 5;

        //world.setGravity(new Vector2 (0.2,0.2));
        writingColor = Color.white;

        explosionImage = null;

        themeSong = new SoundPlayer("sounds/asteroids.wav");
        wonSound = new SoundPlayer("sounds/wormhole.wav");

        createBackground("images/lvl5_background.png");

        BufferedImage asteroidimg = new ImageGrabber("images/asteroid.png").getResource();

        ground = new GameBody();
        ground.addFixture(Geometry.createRectangle(FRAMEWIDTH / scale, 1 / scale));
        ground.setMass(MassType.INFINITE);
        ground.translate(0.0, (-FRAMEHEIGHT / 2 / scale) + 0.3);
        //set transparent color
        ground.setColor(new Color(0, 0, 0, 0));
        world.addBody(ground);

        GameBody flagPickle = new GameBody();
        flagPickle.addFixture(Geometry.createRectangle(20 / scale, 324 / scale));
        flagPickle.setMass(MassType.INFINITE);
        flagPickle.setColor(new Color(0, 0, 0, 0));
        flagPickle.translate(190 / scale, -(FRAMEHEIGHT / 2 / scale) + 0.3 + (340 / scale / 2));
        world.addBody(flagPickle);

        GameBody flag = new GameBody();
        flag.addFixture(Geometry.createRectangle(270 / scale, 128 / scale));
        flag.setMass(MassType.INFINITE);
        flag.setColor(new Color(0, 0, 0, 0));
        flag.translate(190 / scale + 10 / scale + 135 / scale, -(FRAMEHEIGHT / 2 / scale) + 0.3 + (340 / scale) - (128 / 2 / scale));
        world.addBody(flag);

        GameBody tor = new GameBody();
        tor.addFixture(Geometry.createEllipse(70 / scale, 220 / scale));
        tor.setMass(MassType.INFINITE);
        tor.setColor(new Color(0, 0, 0, 0));
        tor.translate((960 / scale) - 0.4, -(FRAMEHEIGHT / 2 / scale) + 1.6);
        world.addBody(tor);

        GameBody ufo = new GameBody();
        ufo.addFixture(Geometry.createEllipse(380 / scale, 380 / scale));
        ufo.setMass(MassType.INFINITE);
        ufo.setColor(new Color(0, 0, 0, 0));
        ufo.translate(-(960 / scale) + 2.95, +2);
        world.addBody(ufo);

        GameBody ufo1 = new GameBody();
        ufo1.addFixture(Geometry.createEllipse(525 / scale, 160 / scale));
        ufo1.setMass(MassType.INFINITE);
        ufo1.setColor(new Color(0, 0, 0, 0));
        ufo1.translate(-(960 / scale) + 2.95, +1.9);
        world.addBody(ufo1);

        GameBody asteroid1 = new GameBody();
        asteroid1.image = asteroidimg;
        asteroid1.addFixture(Geometry.createCircle(100 / scale), 0.5, 0, 1);
        asteroid1.setMass(MassType.NORMAL);
        asteroid1.setGravityScale(0.03);
        asteroid1.setLinearDamping(0);
        asteroid1.setLinearVelocity(3, 2);
        //asteroid1.translate();
        world.addBody(asteroid1);

        GameBody asteroid2 = new GameBody();
        asteroid2.image = asteroidimg;
        asteroid2.addFixture(Geometry.createCircle(100 / scale), 0.5, 0, 1);
        asteroid2.setMass(MassType.NORMAL);
        asteroid2.setGravityScale(0.03);
        asteroid2.translate(-600 / scale, 0);
        asteroid2.setLinearVelocity(3, 4);
        asteroid2.setLinearDamping(0);
        world.addBody(asteroid2);

        GameBody asteroid3 = new GameBody();
        asteroid3.image = asteroidimg;
        asteroid3.addFixture(Geometry.createCircle(100 / scale), 0.5, 0, 1);
        asteroid3.setMass(MassType.NORMAL);
        asteroid3.setGravityScale(0.03);
        asteroid3.translate(450 / scale, 0);
        asteroid3.setLinearVelocity(2, 0);
        asteroid3.setLinearDamping(0);
        world.addBody(asteroid3);

        MotorJoint contrl = new MotorJoint(getLevelBackground(), asteroid3);
        contrl.setCollisionAllowed(false);
        world.addJoint(contrl);

        MotorJoint control0 = new MotorJoint(getLevelBackground(), asteroid1);
        control0.setCollisionAllowed(false);
        world.addJoint(control0);

        MotorJoint control = new MotorJoint(getLevelBackground(), asteroid2);
        control.setCollisionAllowed(false);
        world.addJoint(control);

        MotorJoint control2 = new MotorJoint(tor, asteroid1);
        control2.setCollisionAllowed(true);
        world.addJoint(control2);

        MotorJoint control3 = new MotorJoint(tor, asteroid2);
        control3.setCollisionAllowed(true);
        world.addJoint(control3);

        MotorJoint control4 = new MotorJoint(tor, asteroid3);
        control4.setCollisionAllowed(true);
        world.addJoint(control4);

        world.addListener(new GoalListener(ball, tor));
    }

    public void initiateLevel6() {
        levelNumber = 6;

        createBackground("images/kamehame.jpg");

        BufferedImage gokuimg = new ImageGrabber("images/goku.png").getResource();

        goku = new GameBody();
        goku.image = gokuimg;
        goku.addFixture(Geometry.createCircle(100 / scale), 0.5, 0, 1);
        goku.setMass(MassType.NORMAL);
        goku.setGravityScale(0.03);
        goku.translate(450 / scale, 0);

        goku.setLinearDamping(0);


        //  world.addJoint(control);

        world.addBody(goku);

        ground = new GameBody();
        ground.addFixture(Geometry.createRectangle(FRAMEWIDTH / scale, 1 / scale));
        ground.setMass(MassType.INFINITE);
        ground.translate(0.0, (-FRAMEHEIGHT / 2 / scale) + 0.3);
        //set transparent color
        ground.setColor(new Color(0, 0, 0, 0));
        world.addBody(ground);

        ball.image = new ImageGrabber("images/dragonball.png").getResource();
        themeSong = new SoundPlayer("sounds/chala.wav");
        GokuCollisionListener gokuCollisionListener = new GokuCollisionListener(ball, goku);
        world.addListener(gokuCollisionListener);

    }

    //some getter and setter for this class

    public Color getFontColor() {
        return this.writingColor;
    }

    public GameBody getLevelGround() {
        return this.ground;
    }

    public int getUserShots() {
        return userShots;
    }

    public SoundPlayer getThemeSong() {
        return this.themeSong;
    }


    public int getLevelNumber() {
        return levelNumber;
    }

    public SoundPlayer getWonSound() {
        return this.wonSound;
    }

    public void riseUserShots() {
        userShots++;
    }

    public SoundPlayer getLostSound() {
        return this.levelLostSound;
    }

    public Body getLevelBackground() {
        return this.background;
    }

    public void createBackground(String pathOnClasspath) {
        BufferedImage BACKGROUND = new ImageGrabber(pathOnClasspath).getResource();
        background = new GameBody();
        background.image = BACKGROUND;
        background.addFixture(Geometry.createRectangle(FRAMEWIDTH / scale, FRAMEHEIGHT / scale));
        background.setMass(MassType.INFINITE);
        background.setAutoSleepingEnabled(false);
        world.addBody(background);
    }

    public BufferedImage getExplosionImage() {
        return this.explosionImage;
    }

    public GameBody getGoku() {
        return goku;
    }

    public void stopAllSounds(){
        if (getThemeSong() != null) {
            getThemeSong().stopPlaying();
        }
        if (getLostSound() != null) {
            getLostSound().stopPlaying();
        }
        if (getWonSound() != null) {
            getWonSound().stopPlaying();
        }
    }

}