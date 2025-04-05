package io.github.apocRogue.actorAi;

import io.github.apocRogue.actors.superClasses.EnemyActor;

public class CompositeFlyingAIBehavior extends AIBehavior {

    // The normal flying behavior for bats.
    private FlyingAi flyingBehavior = new FlyingAi();
    // The alert behavior for when a sound triggers the bat.
    private AlertAIBehavior alertBehavior = new AlertAIBehavior();

    @Override
    public void updateAI(EnemyActor enemy, float delta) {
        // Check if the enemy (bat) is in an alert state.
        if (enemy.isAlerted()) {
            // When alerted, move toward the sound's source.
            alertBehavior.updateAI(enemy, delta);
        } else {
            // Otherwise, use the normal flying behavior.
            flyingBehavior.updateAI(enemy, delta);
        }
    }
}
