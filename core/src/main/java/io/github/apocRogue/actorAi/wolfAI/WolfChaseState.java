package io.github.apocRogue.actorAi.wolfAI;

import io.github.apocRogue.actorAi.landAI.chaseAi;
import io.github.apocRogue.actors.mobs.WolfActor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;

public class WolfChaseState implements WolfState {
    private static final float ATTACK_RANGE = 30f;
    private final chaseAi chase = new chaseAi();

    @Override
    public void enter(WolfStateMachine fsm, WolfActor wolf) {
        wolf.setAttackMode(true);
    }


    @Override
    public void update(WolfStateMachine fsm, WolfActor wolf, float delta) {
        // perform the chase
        chase.updateAI(wolf, delta);

        PlayerActor p = wolf.findPlayer();
        boolean canSee = p != null && wolf.canSeePlayer(p);
        float dist = wolf.distanceToPlayer(p);

        if (!canSee) {
            fsm.changeState(new WolfRoamState(), wolf);
        } else if (wolf.isRadiated()) {
            fsm.changeState(new RadiatedWolfAttackState(), wolf);
        } else if (dist <= ATTACK_RANGE) {
            fsm.changeState(new WolfAttackState(), wolf);
        }
    }

    @Override public void exit(WolfStateMachine fsm, WolfActor wolf) { }
}
