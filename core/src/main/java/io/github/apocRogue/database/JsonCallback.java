package io.github.apocRogue.database;

import com.badlogic.gdx.utils.JsonValue;

public interface JsonCallback {
    void onSuccess(String json);

    void onSuccess(JsonValue data);

    void onError(Throwable t);
}
