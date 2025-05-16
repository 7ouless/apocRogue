package io.github.apocRogue.actorAi.wolfAI;

import io.github.apocRogue.actorAi.landAI.chaseAi;
import io.github.apocRogue.actors.mobs.WolfActor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;

public class RadiatedWolfAttackState implements WolfState {
    private static final float MELEE_RANGE = 30f;
    private static final float SHOOT_RANGE = 400f;
    private final chaseAi chase = new chaseAi();

    @Override
    public void enter(WolfStateMachine fsm, WolfActor wolf) {
        wolf.setAttackMode(true);
        wolf.resetShootCooldown();  //ready to fire immediately
    }

    @Override
    public void update(WolfStateMachine fsm, WolfActor wolf, float delta) {
        //cooldown tick
        wolf.updateRadiationTimer(delta);

        PlayerActor p = wolf.findPlayer();
        boolean canSee = p != null && wolf.canSeePlayer(p);
        if (!canSee) {
            fsm.changeState(new WolfRoamState(), wolf);
            return;
        }

        float dist = wolf.distanceToPlayer(p);

        if (dist <= MELEE_RANGE) {
            fsm.changeState(new WolfAttackState(), wolf);
            return;
        }
        if (dist <= SHOOT_RANGE) {
            //chase to keep kiting
            chase.updateAI(wolf, delta);

            //shoot if ready
            wolf.shootTailSting(p.getX(), p.getY());
            return;
        }
            //long-range: chase like a normal wolf
            chase.updateAI(wolf, delta);
        }
    @Override public void exit(WolfStateMachine fsm, WolfActor wolf) { }
    }



