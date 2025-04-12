package io.github.apocRogue.actorAi.flierAI;

import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.apocRogue.actorAi.baseAI.AIBehavior;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.superClasses.EnemyActor;

public class FlyingAi extends AIBehavior {

    // Variables for oscillation effect
    private float elapsedTime = 0f;
    private float amplitude = 10f;  // Maximum offset in pixels
    private float frequency = 1f;   // Oscillations per second

    // Maximum distance (in game units) for chasing the player.
    private float chaseRange = 1000f;

    @Override
    public void updateAI(EnemyActor self, float delta) {
        elapsedTime += delta;

        // Find the player in the stage
        PlayerActor player = findPlayer(self);
        if (player == null) return;

        // Calculate centers for self and player
        float enemyCenterX = self.getX() + self.getWidth() / 2f;
        float enemyCenterY = self.getY() + self.getHeight() / 2f;
        float playerCenterX = player.getX() + player.getWidth() / 2f;
        float playerCenterY = player.getY() + player.getHeight() / 2f;

        // Compute the distance between the enemy and the player
        float dx = playerCenterX - enemyCenterX;
        float dy = playerCenterY - enemyCenterY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // Only chase if the player is within the chase range
        if (distance > chaseRange) {
            return; // Player is too far; enemy can idle or perform other behaviors here.
        }

        // Compute the normalized direction vector towards the player
        float dirX = dx / distance;
        float dirY = dy / distance;

        // Base movement towards the player
        float speed = self.getStats().getSpeed();
        float moveX = speed * dirX * delta;
        float moveY = speed * dirY * delta;

        // Add an oscillation effect perpendicular to the chase direction
        float perpX = -dirY;
        float perpY = dirX;
        float oscillation = (float) Math.sin(elapsedTime * frequency * 2 * Math.PI) * amplitude;
        moveX += perpX * oscillation * delta;
        moveY += perpY * oscillation * delta;

        // Update enemy's position
        self.setX(self.getX() + moveX);
        self.setY(self.getY() + moveY);


    }

    // Helper method to find the player actor from the stage's actors.
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
