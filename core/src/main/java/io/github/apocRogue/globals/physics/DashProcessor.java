// File: io/github/apocRogue/globals/physics/DashProcessor.java
package io.github.apocRogue.globals.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import io.github.apocRogue.actors.playerEntity.PlayerActor;

public class DashProcessor {
    public static void handleDash(PlayerActor actor, float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            startDash(actor, -actor.dashSpeed);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            startDash(actor, actor.dashSpeed);
        }

        if (actor.isDashing) {
            actor.dashTimer -= delta;
            if (actor.dashTimer <= 0f) {
                endDash(actor);
            }
        }
    }

    public static void startDash(PlayerActor actor, float dashVel) {
        // new guard:
        if (!actor.getStats().spendStamina(actor.getStats().getDashStaminaCost())) return;

        actor.isDashing  = true;
        actor.dashTimer  = actor.dashDuration;
        actor.velocityX  = dashVel;
    }

    public static void endDash(PlayerActor actor) {
        actor.isDashing = false;
        actor.dashTimer = 0f;
    }
}
