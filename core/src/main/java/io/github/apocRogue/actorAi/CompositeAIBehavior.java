package io.github.apocRogue.actorAi;

import com.badlogic.gdx.math.Vector2;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.superClasses.EnemyActor;

public class CompositeAIBehavior extends AIBehavior {

    // The normal flying behavior for bats.
    private FlyingAi flyingBehavior = new FlyingAi();
    // The alert behavior for when a sound triggers the bat.
    private AlertAIBehavior alertBehavior = new AlertAIBehavior();

    @Override
    public void updateAI(EnemyActor enemy, float delta) {
        // Use the SoundAlertComponent instead of enemy.isAlerted()
        if (enemy.getAlertComponent().isAlerted()) {
            alertBehavior.updateAI(enemy, delta);
        } else {
            flyingBehavior.updateAI(enemy, delta);
        }
    }
}
