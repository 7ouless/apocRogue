package io.github.apocRogue.globals.physics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import io.github.apocRogue.actorAi.baseAI.SoundAlertComponent;
import io.github.apocRogue.actors.superClasses.EnemyActor;

public class SoundPhysics {
    public enum SoundType {
        MELEE_NOISE, GUNSHOT_MUZZLE, PROJECTILE_IMPACT, PROJECTILE_FLIGHT, ENVIRONMENTAL
    }

    /** Holds data for one debug ring that will be drawn on screen. */
    public static class SoundDebugEvent {
        public Vector2 center;        // Where the sound was emitted
        public float maxRadius;       // The maximum radius
        public float currentRadius;   // Current radius (shrinks or grows over time)
        public float duration;        // Total lifetime for the ring
        public float timeAlive;       // How long it has existed
        public Color color;           // Color of the ring

        public SoundDebugEvent(Vector2 center, float radius, float duration, Color color) {
            this.center = center.cpy();
            this.maxRadius = radius;
            this.currentRadius = radius;
            this.duration = duration;
            this.timeAlive = 0f;
            this.color = color;
        }
    }

    // A static list to track all debug rings currently visible
    public static Array<SoundDebugEvent> debugEvents = new Array<>();

    /**
     * Emits a sound “event” into the game.
     * - Adds a debug ring for visualization
     * - Alerts nearby enemies based on distance & noise intensity
     */
    public static void emitSound(Vector2 pos, float intensity, float radius, SoundType type, Stage stage) {
        // 1) Create a debug ring so we can visualize the sound
        Color ringColor = pickColorForSoundType(type);
        float ringDuration = 2.0f;  // e.g. 2 seconds
        SoundDebugEvent evt = new SoundDebugEvent(pos, radius, ringDuration, ringColor);
        debugEvents.add(evt);

        // 2) Check each enemy in the stage to see if it’s within “radius”
        for (Actor actor : stage.getActors()) {
            if (!(actor instanceof EnemyActor)) continue;
            EnemyActor enemy = (EnemyActor) actor;

            SoundAlertComponent alertComp = enemy.getAlertComponent();
            if (alertComp == null) continue;

            float dist = pos.dst(enemy.getX(), enemy.getY());
            if (dist > radius) continue; // out of range

            // Attenuate the noise based on distance
            float effectiveIntensity = intensity * (1 - dist / radius);

            // If above the enemy’s hearing threshold, alert it
            if (effectiveIntensity >= alertComp.hearingThreshold) {
                enemy.alert(pos, effectiveIntensity);
            }
        }
    }

    /**
     * Called each frame (in GameWorld’s update) to animate or remove debug rings.
     */
    public static void updateDebugEvents(float delta) {
        for (int i = debugEvents.size - 1; i >= 0; i--) {
            SoundDebugEvent evt = debugEvents.get(i);
            evt.timeAlive += delta;

            float t = evt.timeAlive / evt.duration;

            // Example: shrink from maxRadius down to 0
            evt.currentRadius = evt.maxRadius * (1f - t);

            if (evt.timeAlive >= evt.duration) {
                debugEvents.removeIndex(i);
            }
        }
    }

    private static Color pickColorForSoundType(SoundType type) {
        switch (type) {
            case MELEE_NOISE:        return Color.YELLOW;
            case GUNSHOT_MUZZLE:     return Color.RED;
            case PROJECTILE_IMPACT:  return Color.ORANGE;
            case PROJECTILE_FLIGHT:  return Color.CYAN;
            case ENVIRONMENTAL:      return Color.GREEN;
            default:                 return Color.WHITE;
        }
    }
}
