package io.github.apocRogue.actorAi.landAI;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.apocRogue.actorAi.baseAI.AIBehavior;
import io.github.apocRogue.actorAi.baseAI.lineOfSight;
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

        //line of sight (LOS)
        boolean canSeePlayer = lineOfSight.canSeeTarget(enemy, player, enemy.getStats().sightSens(), enemy.getStage());
        if (canSeePlayer) {
            //updates the position of the enemies alert
            enemy.getAlertComponent().setAlertPosition(new Vector2(player.getX(), player.getY()));
            lockedOn = true;
            enemy.getAlertComponent().setAlerted(true);
            lockOnTimer = lockOnTimerMax;
        } else {
            lockOnTimer -= delta;
            if (lockOnTimer < 0) {
                lockedOn = false;
                enemy.getAlertComponent().setAlerted(false);
            }
        }

        //if locked, move to locked position
        if (lockedOn) {
            Vector2 enemyPos = new Vector2(enemy.getX(), enemy.getY());
            Vector2 targetPos = enemy.getAlertComponent().getAlertPosition().cpy();
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
