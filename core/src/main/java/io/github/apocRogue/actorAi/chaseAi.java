package io.github.apocRogue.actorAi;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.superClasses.EnemyActor;

public class chaseAi extends AIBehavior {

    private float lockOnTimer = 10f;
    private final float lockOnTimerMax = 10f;
    private boolean lockedOn = false;
    private SoundAlertComponent alertComponent = new SoundAlertComponent(0.2f);
    @Override
    public void updateAI(EnemyActor enemy, float delta) {
        PlayerActor player = findPlayer(enemy);
        if (player == null) return;

        // LoS check
        boolean canSeePlayer = lineOfSight.canSeeTarget(enemy, player, enemy.getStats().sightSens(), enemy.getStage());
        if (canSeePlayer) {
            // Update last known position to player's real-time position
            alertComponent.setAlertPosition(new Vector2(player.getX(), player.getY()));
            lockedOn = true;
            alertComponent.setAlerted(true);
            lockOnTimer = lockOnTimerMax;
        } else {
            lockOnTimer -= delta;
            if (lockOnTimer < 0) {
                lockedOn = false;
                alertComponent.setAlerted(false);
            }
        }

        // If locked on, move toward the stored position
        if (lockedOn) {
            Vector2 enemyPos = new Vector2(enemy.getX(), enemy.getY());
            Vector2 targetPos = enemy.getAlertPosition().cpy();
            Vector2 direction = targetPos.sub(enemyPos);

            float distance = direction.len();
            if (distance > 1f) {
                direction.nor();
                float moveSpeed = enemy.getStats().getSpeed();
                enemy.moveBy(direction.x * moveSpeed * delta, direction.y * moveSpeed * delta);
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
