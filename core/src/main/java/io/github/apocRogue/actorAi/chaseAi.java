package io.github.apocRogue.actorAi;

import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.superClasses.EnemyActor;

public class chaseAi extends AIBehavior {

    private float lockOnTimer = 10f;
    private final float lockOnTimerMax = 10f;
    private boolean lockedOn = false;

    @Override
    public void updateAI(EnemyActor self, float delta) {
        // e.g. chase logic
        PlayerActor player = findPlayer(self);
        if (player == null) return;

        // If can't see the player, do nothing
        // Use the parent's stats for sight range: self.stats.sightSens()
        if (lineOfSight.canSeeTarget(self, player, self.getStats().sightSens(), self.getStage())) {
            lockedOn = true;
        } else {
            lockOnTimer -= delta;
            if (lockOnTimer < 0) {
                lockedOn = false;
                lockOnTimer = lockOnTimerMax;
            }
        }

        if (lockedOn) {
            float enemyCenterX  = self.getX() + self.getWidth() / 2f;
            float playerCenterX = player.getX() + player.getWidth() / 2f;
            float dx = playerCenterX - enemyCenterX;
            float direction = Math.signum(dx);

            // Possibly jump if on ground
            if (self.isOnGround && self.getJumpTimer() <= 0f) {
                self.jump(); // uses parent's jump() method
            }
            // Move horizontally
            float moveAmount = self.getStats().getSpeed() * direction * delta;
            self.setX(self.getX() + moveAmount);

            // clamp X if desired
            if (self.getStage() != null) {
                float stageW = self.getStage().getWidth();
                if (self.getX() < 0) {
                    self.setX(0);
                } else if (self.getX() + self.getWidth() > stageW) {
                    self.setX(stageW - self.getWidth());
                }
            }
        }
    }

    private PlayerActor findPlayer(EnemyActor self) {
        if (self.getStage() == null) return null;
        for (Actor actor : self.getStage().getActors()) {
            if (actor instanceof PlayerActor) {
                return (PlayerActor) actor;
            }
        }
        return null;
    }
}
