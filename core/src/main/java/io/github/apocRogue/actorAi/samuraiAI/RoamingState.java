package io.github.apocRogue.actorAi.samuraiAI;

import io.github.apocRogue.actorAi.FiniteStateMachine.State;
import io.github.apocRogue.actors.mobs.MiniSamurRoamingState implements State<MiniSamuraiActor> {
    @Override
    public void enter(MiniSamuraiActor samurai) {
    }

    @Override
    public void update(MiniSamuraiActor samurai, float delta) {
        samurai.roam(delta);

        //is player in LOS
        if (samurai.detectPlayer()) {
            // Transition to the ready-up state.
            samurai.changeState(new ReadyUpState());
        }
    }

    @Override
    public void exit(MiniSamuraiActor samurai) {

    }
}
