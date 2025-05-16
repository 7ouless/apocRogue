package io.github.apocRogue.actorAi.samuraiAI;

import io.github.apocRogue.actorAi.FiniteStateMachine.State;
import io.github.apocRogue.actors.mobs.MiniSamuraiActor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;

public class SlashAttackState implements State<MiniSamuraiActor> {
    private static final float INITIAL_ATTACK_DURATION = 0.5f;
    private float attackDuration;

    @Override
    public void enter(MiniSamuraiActor samurai) {
        // reset timer
        this.attackDuration = INITIAL_ATTACK_DURATION;

        // stop everything
        samurai.stopRoaming();

        // make sure we're facing the player
        PlayerActor player = samurai.findPlayer();
        if (player != null) {
            float myCenterX     = samurai.getX() + samurai.getWidth() / 2f;
            float playerCenterX = player.getX()   + player.getWidth() / 2f;
            samurai.setFacingRight(playerCenterX > myCenterX);
        }

        // play the slash
        samurai.playSlashAnimation();

        // only dash if cooled down
        if (samurai.canDash()) {
            samurai.startDash();
            samurai.resetDashCooldown();
            samurai.setAttackMode(true);
        } else {
            samurai.changeState(new RoamingState());
        }
    }

    @Override
    public void update(MiniSamuraiActor samurai, float delta) {
        attackDuration -= delta;
        samurai.dashTowardsTarget(delta);
        if (attackDuration <= 0) {
            samurai.endDash();
            samurai.changeState(new RoamingState());
        }
    }

    @Override
    public void exit(MiniSamuraiActor samurai) {
        samurai.setAttackMode(false);
    }
}
