package io.github.apocRogue.actorAi.samuraiAI;

import io.github.apocRogue.actorAi.FiniteStateMachine.State;
import io.github.apocRogue.actorAi.samuraiAI.SamuraiState;
import io.github.apocRogue.actors.mobs.MiniSamuraiActor;

public class ReadyUpState implements State<MiniSamuraiActor> {
    private float readyTime = 1.5f; // ready for one second
    @Override
    public void enter(MiniSamuraiActor samurai) {
        // Play a ready-up animation.
        samurai.setAttackMode(true);
        samurai.playReadyAnimation();
        samurai.stopMovement();
        samurai.stopRoaming();
    }

    @Override
    public void update(MiniSamuraiActor samurai, float delta) {
        readyTime -= delta;
        if (readyTime <= 0) {
            // After readying up, switch to the slash attack state.
            samurai.changeState(new SlashAttackState());
        }
    }

    @Override
    public void exit(MiniSamuraiActor samurai) {
        samurai.setAttackMode(false);
    }
}
