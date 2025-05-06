package io.github.apocRogue.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads and provides lookup for your static metadata
 * (name, texturePath, ammoTexture, projectileType).
 */
public class WeaponTypeRegistry {
    private final Map<String, WeaponTypeInfo> infoByTypeID = new HashMap<>();

    /**
     * Call once at startup:
     *    registry.load("ui/weapon_types.json");
     */
    public void load(String jsonPath) {
        FileHandle file = Gdx.files.internal(jsonPath);
        JsonReader reader = new JsonReader();
        JsonValue root = reader.parse(file);

        Json json = new Json();
        for (JsonValue entry = root.child; entry != null; entry = entry.next) {
            // entry.name() is the "01", "02", etc.
            WeaponTypeInfo info = json.readValue(WeaponTypeInfo.class, entry);
            infoByTypeID.put(entry.name(), info);
        }
    }

    /**
     * @param typeID two-char code, e.g. "02"
     * @return the parsed WeaponTypeInfo (or null if not found)
     */
    public WeaponTypeInfo get(String typeID) {
        return infoByTypeID.get(typeID);
    }
}
