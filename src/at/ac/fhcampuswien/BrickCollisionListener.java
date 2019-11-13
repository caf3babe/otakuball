package at.ac.fhcampuswien;

import org.dyn4j.collision.narrowphase.Penetration;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.CollisionAdapter;
import org.dyn4j.dynamics.joint.MotorJoint;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;

import java.awt.image.BufferedImage;

import static at.ac.fhcampuswien.Playground.world;

public class BrickCollisionListener extends CollisionAdapter {
    private Body b1, b2,background;


    public BrickCollisionListener(Body b1, Body b2, GameBody background) {
        this.b1 = b1;
        this.b2 = b2;
        this.background = background;
    }

    @Override
    public boolean collision(Body body1, BodyFixture fixture1, Body body2, BodyFixture fixture2, Penetration penetration) {

        if ((body1 == b1 && body2 == b2) ||
                (body1 == b2 && body2 == b1)) {

            body2.getLinearVelocity().zero();
            body2.setAngularVelocity(0.0);

            BufferedImage destroyedBrickImage = new ImageGrabber("images/brick_destroyed.png").getResource();

            Rectangle r = new Rectangle(0.15,0.15);

            GameBody destroyedBrick1 = new GameBody();
            GameBody destroyedBrick2 = new GameBody();
            GameBody destroyedBrick3 = new GameBody();
            GameBody destroyedBrick4 = new GameBody();

            destroyedBrick1.image = destroyedBrickImage;
            destroyedBrick2.image = destroyedBrickImage;
            destroyedBrick3.image = destroyedBrickImage;
            destroyedBrick4.image = destroyedBrickImage;

            destroyedBrick1.addFixture(r,0.1,0.1,0.1);
            destroyedBrick2.addFixture(r,0.1,0.1,0.1);
            destroyedBrick3.addFixture(r,0.1,0.1,0.1);
            destroyedBrick4.addFixture(r,0.1,0.1,0.1);

            destroyedBrick1.translate(body1.getWorldCenter());
            destroyedBrick2.translate(body1.getWorldCenter());
            destroyedBrick3.translate(body1.getWorldCenter());
            destroyedBrick4.translate(body1.getWorldCenter());

            destroyedBrick1.setLinearVelocity(2,2);
            destroyedBrick2.setLinearVelocity(-2,2);
            destroyedBrick3.setLinearVelocity(2,1);
            destroyedBrick4.setLinearVelocity(-2,1);

            destroyedBrick1.setMass(MassType.NORMAL);
            destroyedBrick2.setMass(MassType.NORMAL);
            destroyedBrick3.setMass(MassType.NORMAL);
            destroyedBrick4.setMass(MassType.NORMAL);

            world.removeBody(body1);
            world.addBody(destroyedBrick1);
            world.addBody(destroyedBrick2);
            world.addBody(destroyedBrick3);
            world.addBody(destroyedBrick4);

            MotorJoint control = new MotorJoint(destroyedBrick1,background);
            MotorJoint contro2 = new MotorJoint(destroyedBrick2,background);
            MotorJoint contro3 = new MotorJoint(destroyedBrick3,background);
            MotorJoint contro4 = new MotorJoint(destroyedBrick4,background);
            control.setCollisionAllowed(false);
            contro2.setCollisionAllowed(false);
            contro3.setCollisionAllowed(false);
            contro4.setCollisionAllowed(false);

            world.addJoint(control);
            world.addJoint(contro2);
            world.addJoint(contro3);
            world.addJoint(contro4);

            SoundPlayer soundPlayer = new SoundPlayer("sounds/breakbrick.wav");
            soundPlayer.startPlaying();

            return false;
        }
        return true;
    }
}
