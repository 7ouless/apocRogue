package io.github.apocRogue.actorAi.coreAi;

import io.github.apocRogue.actorAi.baseAI.AIBehavior;
import io.github.apocRogue.actors.mobs.CoreActor;
import io.github.apocRogue.actors.superClasses.EnemyActor;

public class CoreStateMachine extends AIBehavior {
    private CoreState current;

    public CoreStateMachine() {
        this.current = new CoreRoamState();
    }

    @Override
    public void updateAI(EnemyActor e, float delta) {
        CoreActor core = (CoreActor)e;
        current.update(this, core, delta);
    }

    public void changeState(CoreState next, CoreActor core) {
        current.exit(this, core);
        current = next;
        current.enter(this, core);
    }
}
