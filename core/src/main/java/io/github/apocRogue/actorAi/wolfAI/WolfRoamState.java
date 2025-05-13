// WolfRoamState.java
package io.github.apocRogue.actorAi.wolfAI;

import com.badlogic.gdx.math.Vector2;
import io.github.apocRogue.actorAi.baseAI.lineOfSight;
import io.github.apocRogue.actors.mobs.WolfActor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;

public class WolfRoamState implements WolfState {
    private float timer = 0f;
    private static final float INTERVAL = 2f;
    private static final float RADIUS   = 150f;
    private Vector2 target = new Vector2();

    @Override
    public void enter(WolfStateMachine fsm, WolfActor wolf) {
        wolf.setAttackMode(false);
        timer = 0f;
    }

    @Override
    public void update(WolfStateMachine fsm, WolfActor wolf, float delta) {
        // see if we spot the player
        PlayerActor p = wolf.findPlayer();
        if (p != null && lineOfSight.canSeeTarget(
            wolf, p, wolf.getStats().sightSens(), wolf.getStage())) {
            fsm.changeState(new WolfChaseState(), wolf);
            return;
        }

        // pick a new random roam target every INTERVAL seconds
        timer -= delta;
        if (timer <= 0f) {
            float ang = (float)(Math.random() * Math.PI * 2);
            target.set(
                wolf.getX() + (float)Math.cos(ang) * RADIUS,
                wolf.getY()
            );
            timer = INTERVAL;
        }
        // move horizontally toward the roam target
        float dir = Math.signum(target.x - wolf.getX());
        wolf.moveBy(dir * wolf.getStats().getSpeed() * delta, 0);
    }

    @Override public void exit(WolfStateMachine fsm, WolfActor wolf) { }
}
