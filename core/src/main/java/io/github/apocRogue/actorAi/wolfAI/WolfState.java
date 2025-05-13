package io.github.apocRogue.actorAi.wolfAI;

import io.github.apocRogue.actors.mobs.WolfActor;

public interface WolfState {

    void enter(WolfStateMachine fsm, WolfActor wolf);

    void update(WolfStateMachine fsm, WolfActor wolf, float delta);

    void exit(WolfStateMachine fsm, WolfActor wolf);
}
