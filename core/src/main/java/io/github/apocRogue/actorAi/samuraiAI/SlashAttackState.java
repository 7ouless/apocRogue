package io.github.apocRogue.actorAi.samuraiAI;

import io.github.apocRogue.actorAi.FiniteStateMachine.State;
import io.github.apocRogue.actors.mobs.MiniSamuraiActor;

public class SlashAttackState implements State<MiniSamuraiActor> {
    private float attackDuration = 0.5f; // a quick slash
    @Override
    public void enter(MiniSamuraiActor samurai) {
        //dash
        samurai.playSlashAnimation();
        samurai.startDash();
    }

    @Override
    public void update(MiniSamuraiActor samurai, float delta) {
        attackDuration -= delta;
        //dash towards target
        samurai.dashTowardsTarget(delta);
        if (attackDuration <= 0) {
            //back to 'roaming'
            samurai.endDash();
            samurai.changeState(new RoamingState());
        }
    }

    @Override
    public void exit(MiniSamuraiActor samurai) {
    }
}
