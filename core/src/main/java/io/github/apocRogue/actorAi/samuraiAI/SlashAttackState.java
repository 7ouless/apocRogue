package io.github.apocRogue.actorAi.samuraiAI;

import io.github.apocRogue.actorAi.FiniteStateMachine.State;
import io.github.apocRogue.actors.mobs.MiniSamuraiActor;

public class SlashAttackState implements State<MiniSamuraiActor> {
    private float attackDuration = 0.5f; // a quick slash
    @Override
    public void enter(MiniSamuraiActor samurai) {
        samurai.playSlashAnimation();
        if (samurai.canDash()) {
            samurai.startDash();
            samurai.resetDashCooldown();
            samurai.setAttackMode(true);
        } else {
            // skip the dash entirely
            samurai.changeState(new RoamingState());
        }
    }

    @Override
    public void update(MiniSamuraiActor samurai, float delta) {
        attackDuration -= delta;
        // Optionally update dash movement (move straight toward the player)
        samurai.dashTowardsTarget(delta);
        if (attackDuration <= 0) {
            // End the attack and return to roaming.
            samurai.endDash();
            samurai.changeState(new RoamingState());
        }
    }

    @Override
    public void exit(MiniSamuraiActor samurai) {

        samurai.setAttackMode(false);
    }
}
