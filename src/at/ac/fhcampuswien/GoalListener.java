package at.ac.fhcampuswien;

import org.dyn4j.collision.narrowphase.Penetration;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.CollisionAdapter;

public class GoalListener extends CollisionAdapter {
    private Body b1, b2;

    public GoalListener(Body b1, Body b2) {
        this.b1 = b1;
        this.b2 = b2;
    }

    @Override
    public boolean collision(Body body1, BodyFixture fixture1, Body body2, BodyFixture fixture2, Penetration penetration) {

        Playground playground = Main.m.getPlayground();

        if ((body1 == b1 && body2 == b2) || (body1 == b2 && body2 == b1)) {
            playground.setGoalFallen();
            playground.setGoalCounter(Main.m.getPlayground().getGoalCounter()+1);
            playground.world.removeAllListeners();
            return false;
        }
        return true;
    }
}