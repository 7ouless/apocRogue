package io.github.apocRogue.actorAi;

import io.github.apocRogue.actors.superClasses.EnemyActor;

public abstract class AIBehavior {
    public abstract void updateAI(EnemyActor self, float delta);
}
