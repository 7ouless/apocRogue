package io.github.apocRogue.actorAi.samuraiAI;

import io.github.apocRogue.actorAi.FiniteStateMachine.State;
import io.github.apocRogue.actors.mobs.MiniSamuraiActor;

public class SlashAttackState implements State<MiniSamuraiActor> {
    private float attackDuration = 0.5f; // Duration of the quick slash

    @Override
    public void enter(MiniSamuraiActor samurai) {
        samurai.playSlashAnimation();
        samurai.startDash();
    }

    @Override
    public void update(MiniSamuraiActor samurai, float delta) {
        attackDuration -= delta;
        samurai.dashTowardsTarget(delta);
        if (attackDuration <= 0) {
            samurai.endDash();
            samurai.changeState(new SlashPauseState());
        }
    }

    @Override
    public void exit(MiniSamuraiActor samurai) {
        attackDuration = 0.5f;
    }
}
