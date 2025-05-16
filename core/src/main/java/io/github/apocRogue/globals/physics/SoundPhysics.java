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

    public static class SoundDebugEvent {
        public Vector2 center;
        public float maxRadius;
        public float currentRadius;
        public float duration;
        public float timeAlive;
        public Color color;

        public SoundDebugEvent(Vector2 center, float radius, float duration, Color color) {
            this.center = center.cpy();
            this.maxRadius = radius;
            this.currentRadius = radius;
            this.duration = duration;
            this.timeAlive = 0f;
            this.color = color;
        }
    }

    //stores debug rings
    public static Array<SoundDebugEvent> debugEvents = new Array<>();

    //emits an in-game sound event
    public static void emitSound(Vector2 pos, float intensity, float radius, SoundType type, Stage stage) {
        Color ringColor = pickColorForSoundType(type);
        float ringDuration = 2.0f;
        SoundDebugEvent evt = new SoundDebugEvent(pos, radius, ringDuration, ringColor);
        debugEvents.add(evt);

        //is enemy in radius
        for (Actor actor : stage.getActors()) {
            if (!(actor instanceof EnemyActor)) continue;
            EnemyActor enemy = (EnemyActor) actor;

            SoundAlertComponent alertComp = enemy.getAlertComponent();
            if (alertComp == null) continue;

            float dist = pos.dst(enemy.getX(), enemy.getY());
            if (dist > radius) continue; // out of range

            //basing the precision on noise level
            float effectiveIntensity = intensity * (1 - dist / radius);

            //alert the enemy if the noise level is exceeded
            if (effectiveIntensity >= alertComp.hearingThreshold) {
                enemy.alert(pos, effectiveIntensity);
            }
        }
    }

    public static void updateDebugEvents(float delta) {
        for (int i = debugEvents.size - 1; i >= 0; i--) {
            SoundDebugEvent evt = debugEvents.get(i);
            evt.timeAlive += delta;

            float t = evt.timeAlive / evt.duration;

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
