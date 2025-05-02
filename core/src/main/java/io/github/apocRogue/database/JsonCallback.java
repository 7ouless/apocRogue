package io.github.apocRogue.database;

import com.badlogic.gdx.utils.JsonValue;

public interface JsonCallback {
    /** invoked when the call succeeds; you get the raw JSON payload */
    void onSuccess(String json);

    void onSuccess(JsonValue data);

    /** invoked on network or parsing errors */
    void onError(Throwable t);
}
