package io.github.apocRogue.actorAi.samuraiAI;

import io.github.apocRogue.actorAi.FiniteStateMachine.State;
import io.github.apocRogue.actors.mobs.MiniSamuraiActor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actorAi.baseAI.lineOfSight;

public class ReadyUpState implements State<MiniSamuraiActor> {
    private float readyTime = 1.0f; // Wait for one second

    @Override
    public void enter(MiniSamuraiActor samurai) {
        samurai.playReadyAnimation();
        samurai.stopMovement(); // Lock in position.
    }

    @Override
    public void update(MiniSamuraiActor samurai, float delta) {
        readyTime -= delta;
        if (readyTime <= 0) {
            PlayerActor player = samurai.findPlayer();
            boolean inLOS = false;
            if (player != null) {
                inLOS = lineOfSight.canSeeTarget(samurai, player, samurai.getStats().sightSens(), samurai.getStage());
            }
            if (player != null && inLOS && samurai.isPlayerInCameraView()) {
                samurai.changeState(new SlashAttackState());
            } else {
                samurai.changeState(new RoamingState());
            }
        }
    }

    @Override
    public void exit(MiniSamuraiActor samurai) {
        readyTime = 1.0f;
    }
}
