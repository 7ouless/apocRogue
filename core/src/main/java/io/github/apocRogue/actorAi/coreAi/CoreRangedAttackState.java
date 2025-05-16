package io.github.apocRogue.actorAi.coreAi;

import io.github.apocRogue.actorAi.landAI.chaseAi;
import io.github.apocRogue.actors.mobs.CoreActor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;

public class CoreRangedAttackState implements CoreState {
    private static final float MELEE_RANGE  = 30f;
    private static final float SHOOT_RANGE  = 400f;

    private final chaseAi chase = new chaseAi();

    @Override
    public void enter(CoreStateMachine fsm, CoreActor core) {
        core.setAttackMode(true);
        core.resetShootCooldown();
    }

    @Override
    public void update(CoreStateMachine fsm, CoreActor core, float delta) {
        core.act(delta); // reduces cooldown internally

        PlayerActor p = core.findPlayer();
        if (p == null || !core.canSeePlayer(p)) {
            fsm.changeState(new CoreRoamState(), core);
            return;
        }

        float dist = core.distanceToPlayer(p);

        if (dist <= MELEE_RANGE) {
            fsm.changeState(new CoreMeleeAttackState(), core);
        }
        else if (dist <= SHOOT_RANGE) {
            // kite + shoot
            chase.updateAI(core, delta);
            core.shootCoreSting(p.getX(), p.getY());
        }
        else {
            // too far—just chase
            chase.updateAI(core, delta);
        }
    }

    @Override public void exit(CoreStateMachine fsm, CoreActor core) { }
}
