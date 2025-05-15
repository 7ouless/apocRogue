package io.github.apocRogue.actorAi.samuraiAI;

import io.github.apocRogue.actorAi.FiniteStateMachine.State;
import io.github.apocRogue.actorAi.samuraiAI.ReadyUpState;
import io.github.apocRogue.actorAi.samuraiAI.RoamingState;
import io.github.apocRogue.actors.mobs.MiniSamuraiActor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;

public class MoveToViewState implements State<MiniSamuraiActor> {
    @Override
    public void enter(MiniSamuraiActor samurai) {
        samurai.stopRoaming();
    }

    @Override
    public void update(MiniSamuraiActor samurai, float delta) {
        PlayerActor player = samurai.findPlayer();
        if (player != null) {
            float playerCenterX = player.getX() + player.getWidth() / 2;
            float myCenterX = samurai.getX() + samurai.getWidth() / 2;
            if (playerCenterX < myCenterX) {
                samurai.moveBy(-samurai.getStats().getSpeed() * delta, 0);
            } else {
                samurai.moveBy(samurai.getStats().getSpeed() * delta, 0);
            }
            if (samurai.isPlayerInCameraView()) {
                samurai.changeState(new ReadyUpState());
            }
        } else {
            samurai.changeState(new RoamingState());
        }
    }

    @Override
    public void exit(MiniSamuraiActor samurai) { }
}
