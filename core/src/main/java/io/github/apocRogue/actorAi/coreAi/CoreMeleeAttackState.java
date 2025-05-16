package io.github.apocRogue.actorAi.coreAi;

import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.mobs.CoreActor;

public class CoreMeleeAttackState implements CoreState {
    private static final float ATTACK_RANGE = 30f;
    private boolean done = false;

    @Override
    public void enter(CoreStateMachine fsm, CoreActor core) {
        core.setAttackMode(true);
        done = false;
    }

    @Override
    public void update(CoreStateMachine fsm, CoreActor core, float delta) {
        if (!done) {
            PlayerActor p = core.findPlayer();
            if (p != null && core.distanceToPlayer(p) <= ATTACK_RANGE) {
            }
            done = true;
        }
        fsm.changeState(new CoreRoamState(), core);
    }

    @Override public void exit(CoreStateMachine fsm, CoreActor core) { }
}
