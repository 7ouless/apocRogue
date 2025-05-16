package io.github.apocRogue.actorAi.flierAI;

import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.apocRogue.actorAi.baseAI.AIBehavior;
import io.github.apocRogue.actorAi.baseAI.AlertAIBehavior;
import io.github.apocRogue.actorAi.landAI.chaseAi;
import io.github.apocRogue.actorAi.baseAI.lineOfSight;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.superClasses.EnemyActor;

public class CompositeAIBehavior extends AIBehavior {

    //follow/chase
    private chaseAi chaseBehavior = new chaseAi();
    //'investigating' sounds
    private AlertAIBehavior alertBehavior = new AlertAIBehavior();

    @Override
    public void updateAI(EnemyActor enemy, float delta) {
        PlayerActor player = findPlayer(enemy);
        boolean hasLoS = false;
        if (player != null) {
            hasLoS = lineOfSight.canSeeTarget(enemy, player, enemy.getStats().sightSens(), enemy.getStage());
        }

        if (hasLoS) {
            chaseBehavior.updateAI(enemy, delta);
            return;
        }

        if (enemy.getAlertComponent().isAlerted()) {
            //move to alert position
            alertBehavior.updateAI(enemy, delta);
        } else {
            //idle
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
