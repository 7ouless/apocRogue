package io.github.apocRogue.actorAi.samuraiAI;

import io.github.apocRogue.actorAi.FiniteStateMachine.State;
import io.github.apocRogue.actorAi.samuraiAI.SamuraiState;
import io.github.apocRogue.actors.mobs.MiniSamuraiActor;

public class ReadyUpState implements State<MiniSamuraiActor> {
    private float readyTime = 1.0f; //ready for one second
    @Override
    public void enter(MiniSamuraiActor samurai) {
        samurai.playReadyAnimation();
        samurai.stopMovement();
    }

    @Override
    public void update(MiniSamuraiActor samurai, float delta) {
        readyTime -= delta;
        if (readyTime <= 0) {
            //switch to attack state
            samurai.changeState(new SlashAttackState());
        }
    }

    @Override
    public void exit(MiniSamuraiActor samurai) {
    }
}
