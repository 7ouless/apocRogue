package io.github.apocRogue.actorAi;

import com.badlogic.gdx.math.Vector2;

public class SoundAlertComponent {
    private Vector2 alertPosition = new Vector2();
    private float alertLevel = 0f;
    private boolean alerted = false;
    private float lockOnTimer = 0f;
    private final float LOCK_ON_TIMER_MAX = 10f;

    // Called externally by the sound system when a sound occurs.
    public void triggerAlert(Vector2 soundPosition, float noiseLevel) {
        alertPosition.set(soundPosition);
        alertLevel += noiseLevel;
        alerted = true;
        lockOnTimer = LOCK_ON_TIMER_MAX;
    }

    // Update the alert state each frame.
    public void update(float delta) {
        if (alerted) {
            lockOnTimer -= delta;
            if (lockOnTimer <= 0) {
                alerted = false;
                alertLevel = 0;
            }
        }
    }

    public Vector2 getAlertPosition() {
        return alertPosition;
    }

    public boolean isAlerted() {
        return alerted;
    }
}
