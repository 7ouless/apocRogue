package io.github.apocRogue.actorAi;

import com.badlogic.gdx.math.Vector2;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.superClasses.EnemyActor;

public class CompositeAIBehavior extends AIBehavior {

    // Underlying behaviors
    private chaseAi chaseBehavior = new chaseAi();
    private AlertAIBehavior alertBehavior = new AlertAIBehavior();

    @Override
    public void updateAI(EnemyActor enemy, float delta) {
        // Prioritize alert behavior if the enemy has been triggered by a sound.
        if (enemy.isAlerted()) {
            alertBehavior.updateAI(enemy, delta);
        } else {
            // Otherwise, perform normal chase behavior.
            chaseBehavior.updateAI(enemy, delta);
        }
    }
}
