package io.github.apocRogue.actorAi;

import com.badlogic.gdx.math.Vector2;
import io.github.apocRogue.actors.superClasses.EnemyActor;

/**
 * AlertAIBehavior updates enemy behavior based on alertness relative to the enemy's awareness.
 * When the enemy's alertness (increased by sound events) reaches or exceeds its awareness threshold,
 * the enemy will move toward the alert source.
 */
public class AlertAIBehavior extends AIBehavior {

    // Movement speed of the enemy when reacting to an alert.
    private float alertMoveSpeed = 100f;
    // Rate at which the alertness decays if no additional sound is detected (per second).
    private float alertDecayRate = 10f;

    @Override
    public void updateAI(EnemyActor enemy, float delta) {
        // If the enemy is currently alerted, process the alert behavior.
        if (enemy.isAlerted()) {
            // Check if the enemy's current alertness meets or exceeds its awareness threshold.
            if (enemy.getAlertness() >= enemy.getAwareness()) {
                // Calculate the direction vector toward the alert sound's origin.
                Vector2 currentPos = new Vector2(enemy.getX(), enemy.getY());
                Vector2 targetPos = enemy.getAlertPosition();
                Vector2 direction = targetPos.cpy().sub(currentPos);

                // Only move if the distance is significant (i.e. not already at the alert point).
                if (direction.len() > 1f) {
                    direction.nor();
                    enemy.moveBy(direction.x * alertMoveSpeed * delta, direction.y * alertMoveSpeed * delta);
                }
            } else {
                // If alertness is below the threshold, decay the alertness over time.
                enemy.setAlertness(enemy.getAlertness() - alertDecayRate * delta);
                // Reset the alert state if alertness falls to zero.
                if (enemy.getAlertness() <= 0) {
                    enemy.setAlertness(0);
                    enemy.setAlerted(false);
                }
            }
        } else {
            // If not alerted, execute normal AI behavior (e.g., patrolling or idling).
            // This can be implemented or delegated to another behavior.
        }
    }
}
