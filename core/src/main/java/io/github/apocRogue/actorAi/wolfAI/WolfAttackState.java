package io.github.apocRogue.actorAi.wolfAI;

import io.github.apocRogue.actors.mobs.WolfActor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;

public class WolfAttackState implements WolfState {
    private boolean done = false;
    private static final float ATTACK_RANGE = 30f;

    @Override
    public void enter(WolfStateMachine fsm, WolfActor wolf) {
        wolf.setAttackMode(true);
    }

    @Override
    public void update(WolfStateMachine fsm, WolfActor wolf, float delta) {
        if (!done) {
            PlayerActor p = wolf.findPlayer();
            // only attack if player still in range
            if (p != null && wolf.distanceToPlayer(p) <= ATTACK_RANGE) {

                    }
            done = true;
        }
        fsm.changeState(new WolfRoamState(), wolf);
    }

    @Override
    public void exit(WolfStateMachine fsm, WolfActor wolf) { }
}
