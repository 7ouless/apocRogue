package io.github.apocRogue.actorAi.coreAi;

import io.github.apocRogue.actors.mobs.CoreActor;

public interface CoreState {
    void enter(CoreStateMachine fsm, CoreActor core);
    void update(CoreStateMachine fsm, CoreActor core, float delta);
    void exit(CoreStateMachine fsm, CoreActor core);
}
