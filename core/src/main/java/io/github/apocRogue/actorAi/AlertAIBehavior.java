package io.github.apocRogue.actorAi;

import com.badlogic.gdx.math.Vector2;
import io.github.apocRogue.actors.superClasses.EnemyActor;

public class AlertAIBehavior extends AIBehavior {
    private float alertMoveSpeed = 100f;
    private float alertDecayRate = 10f;

    @Override
    public void updateAI(EnemyActor enemy, float delta) {
        if (enemy.isAlerted()) {
            // read from the component
                Vector2 targetPos = enemy.getAlertPosition();
                Vector2 currentPos = new Vector2(enemy.getX(), enemy.getY());
                Vector2 dir = targetPos.cpy().sub(currentPos);
                if (dir.len() > 1f) {
                    dir.nor();
                    enemy.moveBy(dir.x * enemy.getStats().getSpeed() * delta,
                        dir.y * enemy.getStats().getSpeed() * delta);
                }
                // Decay
           //     enemy.getAlertComponent().reduceAlertLevel(delta * 10f);
            //    if (enemy.getAlertComponent().getAlertLevel() <= 0) {
            //        enemy.getAlertComponent().setAlerted(false);
        //}
            }
        }
}
