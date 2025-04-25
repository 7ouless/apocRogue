package io.github.apocRogue.actorAi.samuraiAI;

import io.github.apocRogue.actorAi.FiniteStateMachine.State;
import io.github.apocRogue.actors.mobs.MiniSamuraiActor;

public class RoamingState implements State<MiniSamuraiActor> {
    @Override
    public void enter(MiniSamuraiActor samurai) {
        // Optionally set an idle animation.
    }

    @Override
    public void update(MiniSamuraiActor samurai, float delta) {
        // Do roaming logic (e.g. random movement, patrolling)
        samurai.roam(delta);

        // Check if the player is within detection range using line-of-sight or distance.
        if (samurai.detectPlayer()) {
            // Transition to the ready-up state.
            samurai.changeState(new ReadyUpState());
        }
    }

    @Override
    public void exit(MiniSamuraiActor samurai) {
        // Cleanup if needed.
    }
}
