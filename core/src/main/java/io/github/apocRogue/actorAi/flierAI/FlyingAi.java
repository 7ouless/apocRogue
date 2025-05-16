package io.github.apocRogue.actorAi.flierAI;

import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.apocRogue.actorAi.baseAI.AIBehavior;
import io.github.apocRogue.actors.playerEntity.PlayerActor;
import io.github.apocRogue.actors.superClasses.EnemyActor;

public class FlyingAi extends AIBehavior {

    //oscilliation control
    private float elapsedTime = 0f;
    private float amplitude = 10f;  //max offset in px
    private float frequency = 1f;   //oscilliations per second

    // max distance fir chasing player
    private float chaseRange = 1000f;

    @Override
    public void updateAI(EnemyActor self, float delta) {
        elapsedTime += delta;

        PlayerActor player = findPlayer(self);
        if (player == null) return;

        float enemyCenterX = self.getX() + self.getWidth() / 2f;
        float enemyCenterY = self.getY() + self.getHeight() / 2f;
        float playerCenterX = player.getX() + player.getWidth() / 2f;
        float playerCenterY = player.getY() + player.getHeight() / 2f;

        //distance between player + enemy
        float dx = playerCenterX - enemyCenterX;
        float dy = playerCenterY - enemyCenterY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > chaseRange) {
            return; //player is too far away
        }

        //calculating vector to player
        float dirX = dx / distance;
        float dirY = dy / distance;

        //movement
        float speed = self.getStats().getSpeed();
        float moveX = speed * dirX * delta;
        float moveY = speed * dirY * delta;

        //oscilliation perpendicular to the chase direction
        float perpX = -dirY;
        float perpY = dirX;
        float oscillation = (float) Math.sin(elapsedTime * frequency * 2 * Math.PI) * amplitude;
        moveX += perpX * oscillation * delta;
        moveY += perpY * oscillation * delta;

        //move enemy
        self.setX(self.getX() + moveX);
        self.setY(self.getY() + moveY);


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
