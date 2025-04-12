package io.github.apocRogue.actorAi.flierAI;

import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.apocRogue.actorAi.baseAI.AIBehavior;
import io.github.apocRogue.actorAi.baseAI.AlertAIBehavior;
import io.github.apocRogue.actorAi.landAI.chaseAi;
import io.github.apocRogue.actorAi.baseAI.lineOfSight;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.superClasses.EnemyActor;

public class CompositeAIBehavior extends AIBehavior {

    // “Normal” chase logic when LoS is established
    private chaseAi chaseBehavior = new chaseAi();
    // Investigate sound if not in LoS
    private AlertAIBehavior alertBehavior = new AlertAIBehavior();

    @Override
    public void updateAI(EnemyActor enemy, float delta) {
        // 1) Check if the player is visible
        PlayerActor player = findPlayer(enemy);
        boolean hasLoS = false;
        if (player != null) {
            hasLoS = lineOfSight.canSeeTarget(enemy, player, enemy.getStats().sightSens(), enemy.getStage());
        }

        // 2) If we see the player, chase the player (highest priority).
        if (hasLoS) {
            chaseBehavior.updateAI(enemy, delta);
            return;
        }

        // 3) Otherwise, if we heard a sound, investigate alert
        if (enemy.getAlertComponent().isAlerted()) {
            // The AlertAIBehavior will move enemy toward alert position
            alertBehavior.updateAI(enemy, delta);
        } else {
            // 4) No LoS, no sound: (Optional) idle/patrol or do nothing
            // By default, do nothing
        }
    }

    // Helper: find the player actor
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
