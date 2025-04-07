package io.github.apocRogue.actorAi;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.superClasses.EnemyActor;

public class chaseAi extends AIBehavior {

    private float lockOnTimer = 10f;
    private final float lockOnTimerMax = 10f;
    private boolean lockedOn = false;

    @Override
    public void updateAI(EnemyActor enemy, float delta) {
        PlayerActor player = findPlayer(enemy);
        if (player == null) return;

        boolean canSeePlayer = lineOfSight.canSeeTarget(enemy, player, enemy.getStats().sightSens(), enemy.getStage());

        if (canSeePlayer) {
            // Update last seen position and set alerted flag.
            enemy.setAlertPosition(new Vector2(player.getX(), player.getY()));
            lockedOn = true;
            enemy.setAlerted(true);
            lockOnTimer = lockOnTimerMax;
        } else {
            lockOnTimer -= delta;
            if (lockOnTimer < 0) {
                lockedOn = false;
                enemy.setAlerted(false);
            }
        }

        if (lockedOn) {
            Vector2 enemyPos = new Vector2(enemy.getX(), enemy.getY());
            Vector2 lastSeen = enemy.getAlertPosition().cpy();
            Vector2 toLastSeen = lastSeen.sub(enemyPos);
            float baseDistance = toLastSeen.len();

            // Incorporate aggression: higher aggression means search further.
            float aggression = enemy.getStats().getAggression(); // e.g., 0 to 10
            float searchMultiplier = 1f + aggression / 10f; // Adjust multiplier as needed

            // Calculate the search target beyond the last seen position.
            Vector2 searchTarget = enemyPos.cpy().add(toLastSeen.nor().scl(baseDistance * searchMultiplier));

            // Move enemy toward the search target.
            Vector2 moveDir = searchTarget.sub(enemyPos);
            if (moveDir.len() > 1f) {
                moveDir.nor();
                enemy.moveBy(moveDir.x * enemy.getStats().getSpeed() * delta,
                    moveDir.y * enemy.getStats().getSpeed() * delta);
            }
        }
    }



    private PlayerActor findPlayer(EnemyActor self) {
        if (self.getStage() == null) return null;
        for (Actor actor : self.getStage().getActors()) {
            if (actor instanceof PlayerActor) {
                return (PlayerActor) actor;
            }
        }
        return null;
    }
}
