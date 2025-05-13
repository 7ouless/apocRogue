package io.github.apocRogue.globals.physics;

import io.github.apocRogue.actors.playerEntity.PlayerActor;

public class JumpProcessor {
    public static void handleJump(PlayerActor actor) {
        if (!actor.isOnGround) return;

        if (!actor.getStats().spendStamina(actor.getStats().getJumpStaminaCost())) return;

        actor.velocityY  = actor.jumpPower;
        actor.isOnGround = false;
    }
}
