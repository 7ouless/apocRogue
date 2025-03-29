package io.github.apocRogue.actorAi;

import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.superClasses.EnemyActor;

public class FlyingAi extends AIBehavior {

    // Variables for oscillation effect
    private float elapsedTime = 0f;
    private float amplitude = 10f;  // Maximum offset in pixels
    private float frequency = 1f;   // Oscillations per second

    @Override
    public void updateAI(EnemyActor self, float delta) {
        elapsedTime += delta;

        // Find the player in the stage
        PlayerActor player = findPlayer(self);
        if (player == null) return;

        // Calculate the center positions for self and player
        float enemyCenterX = self.getX() + self.getWidth() / 2f;
        float enemyCenterY = self.getY() + self.getHeight() / 2f;
        float playerCenterX = player.getX() + player.getWidth() / 2f;
        float playerCenterY = player.getY() + player.getHeight() / 2f;

        // Compute the normalized direction vector towards the player
        float dx = playerCenterX - enemyCenterX;
        float dy = playerCenterY - enemyCenterY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance == 0) return; // Avoid division by zero

        float dirX = dx / distance;
        float dirY = dy / distance;

        // Base movement towards the player
        float speed = self.getStats().getSpeed();
        float moveX = speed * dirX * delta;
        float moveY = speed * dirY * delta;

        // Add an oscillation effect perpendicular to the chase direction
        // The perpendicular vector to (dirX, dirY) is (-dirY, dirX)
        float perpX = -dirY;
        float perpY = dirX;
        // Calculate the oscillation offset using a sine wave
        float oscillation = (float) Math.sin(elapsedTime * frequency * 2 * Math.PI) * amplitude;
        moveX += perpX * oscillation * delta;
        moveY += perpY * oscillation * delta;

        // Update enemy's position in both axes
        self.setX(self.getX() + moveX);
        self.setY(self.getY() + moveY);

        // Clamp the enemy within the stage boundaries (optional)
        if (self.getStage() != null) {
            float stageWidth = self.getStage().getWidth();
            float stageHeight = self.getStage().getHeight();
            if (self.getX() < 0) self.setX(0);
            if (self.getY() < 0) self.setY(0);
            if (self.getX() + self.getWidth() > stageWidth)
                self.setX(stageWidth - self.getWidth());
            if (self.getY() + self.getHeight() > stageHeight)
                self.setY(stageHeight - self.getHeight());
        }
    }

    // Helper method to find the player actor from the stage's actors
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

