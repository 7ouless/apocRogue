package io.github.apocRogue.actorAi.wolfAI;

import io.github.apocRogue.actorAi.baseAI.AIBehavior;
import io.github.apocRogue.actors.superClasses.EnemyActor;
import io.github.apocRogue.actors.mobs.WolfActor;

public class WolfStateMachine extends AIBehavior {
    private WolfState current;

    public WolfStateMachine() {
        this.current = new WolfRoamState();  // initial state
    }

    @Override
    public void updateAI(EnemyActor e, float delta) {
        WolfActor wolf = (WolfActor)e;
        current.update(this, wolf, delta);
    }

    public void changeState(WolfState next, WolfActor wolf) {
        current.exit(this, wolf);
        current = next;
        current.enter(this, wolf);
    }
}
