package io.github.apocRogue.actorAi.samuraiAI;

import io.github.apocRogue.actorAi.FiniteStateMachine.State;
import io.github.apocRogue.actors.mobs.MiniSamuraiActor;

public class RoamingState implements State<MiniSamuraiActor> {
    @Override
    public void enter(MiniSamuraiActor samurai) {
        samurai.startRoaming();
    }

    @Override
    public void update(MiniSamuraiActor samurai, float delta) {
        samurai.roam(delta);
        if (samurai.detectPlayer()) {
            if (samurai.isPlayerInCameraView()) {
                samurai.changeState(new ReadyUpState());
            } else {
                samurai.changeState(new MoveToViewState());
            }
        }
    }

    @Override
    public void exit(MiniSamuraiActor samurai) { }
}
