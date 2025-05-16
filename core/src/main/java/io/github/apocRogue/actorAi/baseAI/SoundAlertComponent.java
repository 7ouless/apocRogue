package io.github.apocRogue.actorAi.baseAI;

import com.badlogic.gdx.math.Vector2;

public class SoundAlertComponent {
    private Vector2 alertPosition = new Vector2();
    private boolean alerted = false;
    private float alertLevel = 0f;
    private float lockOnTimer = 0f;
    private final float LOCK_ON_TIMER_MAX = 10f;
    public float hearingThreshold;

    public void setAlertPosition(Vector2 pos) {
        alertPosition.set(pos);
    }

    public Vector2 getAlertPosition() {
        return alertPosition;
    }

    public void setAlerted(boolean val) {
        alerted = val;
    }

    public boolean isAlerted() {
        return alerted;
    }

    public void triggerAlert(Vector2 soundPos, float noise) {
        if (noise >= hearingThreshold) {
            // Immediately trigger alert if the sound is intense enough.
            alertPosition.set(soundPos);
            alerted = true;

            lockOnTimer = LOCK_ON_TIMER_MAX;
            alertLevel = 1.0f;
        }
    }

    public void update(float delta) {
        if (alerted) {
            lockOnTimer -= delta;
            if (lockOnTimer <= 0) {
                alerted = false;
                alertLevel = 0;
            }
        }
    }

    public SoundAlertComponent(float threshold) {
        this.hearingThreshold = threshold;
    }
    public void reduceAlertLevel(float amount) {
        alertLevel = Math.max(0f, alertLevel - amount);
    }

    public float getAlertLevel() {
        return alertLevel;
    }


}
