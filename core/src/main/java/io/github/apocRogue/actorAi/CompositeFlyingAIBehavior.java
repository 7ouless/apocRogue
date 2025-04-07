package io.github.apocRogue.actorAi;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.superClasses.EnemyActor;

public class CompositeFlyingAIBehavior extends AIBehavior {

    private FlyingAi flyingBehavior = new FlyingAi();
    private AlertAIBehavior alertBehavior = new AlertAIBehavior();

    @Override
    public void updateAI(EnemyActor enemy, float delta) {
        // 1) Check LoS to player
        PlayerActor player = findPlayer(enemy);
        boolean hasLoS = false;
        if (player != null) {
            hasLoS = lineOfSight.canSeeTarget(enemy, player, enemy.getStats().sightSens(), enemy.getStage());
        }

        // 2) If LoS is valid, do normal “flying chase”
        if (hasLoS) {
            flyingBehavior.updateAI(enemy, delta);
            return;
        }

        // 3) Otherwise, if alerted by sound, investigate
        if (enemy.getAlertComponent().isAlerted()) {
            alertBehavior.updateAI(enemy, delta);
        } else {
            // 4) No LoS, no sound => idle/wander in flyingAi or do nothing
            // For now, just do the normal flyingAi but not chasing
            // Possibly the same flyingAi handles idle as well.
            flyingBehavior.updateAI(enemy, delta);
        }
    }

    private PlayerActor findPlayer(EnemyActor enemy) {
        if (enemy.getStage() == null) return null;
        for (Actor actor : enemy.getStage().getActors()) {
            if (actor instanceof PlayerActor) {
                return (PlayerActor) actor;
            }
        }
        return null;
    }
}
