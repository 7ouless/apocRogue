package io.github.apocRogue.actorAi.coreAi;

import com.badlogic.gdx.math.Vector2;
import io.github.apocRogue.actors.mobs.CoreActor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;

public class CoreRoamState implements CoreState {
    private static final float INTERVAL = 2f;
    private static final float RADIUS   = 150f;

    private float timer = 0f;
    private final Vector2 target = new Vector2();

    @Override
    public void enter(CoreStateMachine fsm, CoreActor core) {
        core.setAttackMode(false);
        timer = 0f;
    }

    @Override
    public void update(CoreStateMachine fsm, CoreActor core, float delta) {
        // spot player?
        PlayerActor p = core.findPlayer();
        if (p != null && core.canSeePlayer(p)) {
            fsm.changeState(new CoreChaseState(), core);
            return;
        }

        // pick new roam target
        timer -= delta;
        if (timer <= 0f) {
            float ang = (float)(Math.random() * Math.PI * 2);
            target.set(
                core.getX() + (float)Math.cos(ang) * RADIUS,
                core.getY()
            );
            timer = INTERVAL;
        }
        float dir = Math.signum(target.x - core.getX());
        core.moveBy(dir * core.getStats().getSpeed() * delta, 0);
    }

    @Override public void exit(CoreStateMachine fsm, CoreActor core) { }
}
