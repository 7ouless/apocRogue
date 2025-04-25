package io.github.apocRogue.userSettings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Input;

public class userSettingsManager {
    private static final String PREFS_NAME = "MyGameSettings";
    private Preferences prefs;
    private static userSettingsManager instance;

    // Default keys for custom controls (hotbar keys remain fixed)
    private static final int DEFAULT_MOVE_LEFT    = Input.Keys.A;
    private static final int DEFAULT_MOVE_RIGHT   = Input.Keys.D;
    private static final int DEFAULT_JUMP         = Input.Keys.SPACE;
    private static final int DEFAULT_DASH_LEFT    = Input.Keys.Q;
    private static final int DEFAULT_DASH_RIGHT   = Input.Keys.E;
    private static final int DEFAULT_INTERACT     = Input.Keys.R;
    private static final int DEFAULT_JUMP_KEY = com.badlogic.gdx.Input.Keys.SPACE;
    private static final float DEFAULT_MASTER_VOLUME = 1.0f;
    private userSettingsManager() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
    }

    public static userSettingsManager getInstance() {
        if (instance == null) {
            instance = new userSettingsManager();
        }
        return instance;
    }

    public int getMoveLeft() {
        return prefs.getInteger("moveLeft", DEFAULT_MOVE_LEFT);
    }
    public void setMoveLeft(int key) {
        prefs.putInteger("moveLeft", key);
        prefs.flush();
    }

    public int getMoveRight() {
        return prefs.getInteger("moveRight", DEFAULT_MOVE_RIGHT);
    }
    public void setMoveRight(int key) {
        prefs.putInteger("moveRight", key);
        prefs.flush();
    }

    public int getJump() {
        return prefs.getInteger("jump", DEFAULT_JUMP);
    }
    public void setJump(int key) {
        prefs.putInteger("jump", key);
        prefs.flush();
    }

    public int getDashLeft() {
        return prefs.getInteger("dashLeft", DEFAULT_DASH_LEFT);
    }
    public void setDashLeft(int key) {
        prefs.putInteger("dashLeft", key);
        prefs.flush();
    }

    public int getDashRight() {
        return prefs.getInteger("dashRight", DEFAULT_DASH_RIGHT);
    }
    public void setDashRight(int key) {
        prefs.putInteger("dashRight", key);
        prefs.flush();
    }

    public int getInteract() {
        return prefs.getInteger("interact", DEFAULT_INTERACT);
    }
    public void setInteract(int key) {
        prefs.putInteger("interact", key);
        prefs.flush();
    }
    public float getMasterVolume() {
        return prefs.getFloat("masterVolume", DEFAULT_MASTER_VOLUME);
    }

    public void setMasterVolume(float newVolume) {
        prefs.putFloat("masterVolume", newVolume);
        prefs.flush();
    }
}
