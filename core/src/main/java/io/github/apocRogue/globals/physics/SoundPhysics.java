package io.github.apocRogue.globals.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import io.github.apocRogue.actors.superClasses.EnemyActor;

public class SoundPhysics {

    // The base distance (in game units) within which enemies can hear sounds.
    public static final float BASE_HEARING_RANGE = 300f;

    // Multiplier for player-originated sounds, making them more likely to alert enemies.
    public static final float PLAYER_SOUND_MULTIPLIER = 1.5f;

    /**
     * Emits a sound event at the given position with the specified noise level.
     * Enemies within the hearing range will have a chance to be alerted.
     *
     * @param soundPosition the location of the sound event (e.g., collision point)
     * @param noiseLevel the intensity of the sound (weapon noise)
     * @param stage the stage containing enemy actors
     * @param isPlayerSound true if the sound originates from the player (applies a multiplier)
     */
    public static void emitSound(Vector2 soundPosition, float noiseLevel, Stage stage, boolean isPlayerSound) {
        // Apply the player sound multiplier if applicable.
        float effectiveNoise = isPlayerSound ? noiseLevel * PLAYER_SOUND_MULTIPLIER : noiseLevel;

        // Get all actors from the stage.
        Array<Actor> actors = stage.getActors();
        for (Actor actor : actors) {
            // Check if the actor is an enemy (you should have an EnemyActor class).
            if (actor instanceof EnemyActor) {
                EnemyActor enemy = (EnemyActor) actor;

                // Calculate the distance from the sound event to the enemy's center.
                float enemyCenterX = enemy.getX() + enemy.getWidth() / 2f;
                float enemyCenterY = enemy.getY() + enemy.getHeight() / 2f;
                float distance = soundPosition.dst(enemyCenterX, enemyCenterY);

                // If the enemy is within hearing range, calculate an alert chance.
                if (distance <= BASE_HEARING_RANGE) {
                    // The chance of alerting increases with effective noise and decreases with distance.
                    // Here we use a simple formula: chance = effectiveNoise / (distance + 1).
                    float chance = effectiveNoise / (distance + 1f);
                    if (chance > 1f) {
                        chance = 1f;
                    }

                    // Use randomness to determine if the enemy is alerted.
                    if (Math.random() < chance) {
                        enemy.alert(soundPosition, effectiveNoise);
                    }
                }
            }
        }
    }
}
