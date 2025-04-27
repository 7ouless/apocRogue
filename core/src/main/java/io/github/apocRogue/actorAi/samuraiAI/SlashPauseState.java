package io.github.apocRogue.actorAi.samuraiAI;

import io.github.apocRogue.actorAi.FiniteStateMachine.State;
import io.github.apocRogue.actors.mobs.MiniSamuraiActor;

public class SlashPauseState implements State<MiniSamuraiActor> {
    private float pauseDuration = 1.0f; // Wait for one second

    @Override
    public void enter(MiniSamuraiActor samurai) {
        System.out.println("Samurai: Pause after slash.");
        samurai.stopMovement();
    }

    @Override
    public void update(MiniSamuraiActor samurai, float delta) {
        pauseDuration -= delta;
        if (pauseDuration <= 0) {
            samurai.changeState(new RoamingState());
        }
    }

    @Override
    public void exit(MiniSamuraiActor samurai) {
        pauseDuration = 1.0f;
    }
}
