package at.ac.fhcampuswien;

import org.dyn4j.collision.narrowphase.Penetration;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.CollisionAdapter;
import org.dyn4j.dynamics.joint.MotorJoint;


import java.util.List;
import java.util.TimerTask;
import java.util.Timer;

import static at.ac.fhcampuswien.Playground.*;

public class BladeCollisionListener extends CollisionAdapter {
    private Body b1, b2, ground;


    public BladeCollisionListener(Body b1, Body b2, Body ground) {
        this.b1 = b1;
        this.b2 = b2;
        this.ground = ground;
    }

    @Override
    public boolean collision(Body body1, BodyFixture fixture1, Body body2, BodyFixture fixture2, Penetration penetration) {



        if ((body1 == b1 && body2 == b2) ||
                (body1 == b2 && body2 == b1)) {

            body2.setAsleep(true);


            List<Body> bodies = world.getBodies();

            for(Body body : bodies){
                try {
                    MotorJoint mj = new MotorJoint(body2, body);
                    mj.setCollisionAllowed(false);
                    Main.m.getPlayground().world.addJoint(mj);
                }
                catch(Exception e){
                    continue;
                }
            }

            if (Main.m.getPlayground().getLevel().getThemeSong() != null) {
                Main.m.getPlayground().getLevel().getThemeSong().stopPlaying();
            }
            Main.m.getPlayground().getLevel().getLostSound().startPlaying();


            body2.setLinearVelocity(0,0);
            body2.setAngularVelocity(0);
            body2.setLinearVelocity(0,4);

            world.removeAllListeners();

            Timer t = new Timer();
            TimerTask tt = new TimerTask() {
                @Override
                public void run() {
                    Main.m.getPlayground().initializeWorld();
                }
            };

            t.schedule(tt,(long)(Main.m.getPlayground().getLevel().getLostSound().getClipDurationInSeconds()*1000));

            Main.m.getPlayground().doNotResetLevel();



        }
        return true;
    }
}
