// File: io/github/apocRogue/globals/physics/JumpProcessor.java
package io.github.apocRogue.globals.physics;

import io.github.apocRogue.actors.playerEntity.PlayerActor;

public class JumpProcessor {
    public static void handleJump(PlayerActor actor) {
        if (actor.isOnGround) {
            actor.velocityY   = actor.jumpPower;
            actor.isOnGround  = false;
        }
    }
}
