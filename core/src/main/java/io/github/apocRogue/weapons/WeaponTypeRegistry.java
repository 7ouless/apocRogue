package io.github.apocRogue.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import io.github.apocRogue.globals.ids.ClassDigit;

import java.util.HashMap;
import java.util.Map;


public class WeaponTypeRegistry {
    private final Map<String, WeaponTypeInfo> infoByTypeID = new HashMap<>();


    public java.util.Set<String> getAllTypeIDs() {
        return infoByTypeID.keySet();
    }

    public void load(String jsonPath) {
        FileHandle file = Gdx.files.internal(jsonPath);
        JsonReader reader = new JsonReader();
        JsonValue root = reader.parse(file);

        Json json = new Json();
        for (JsonValue entry = root.child; entry != null; entry = entry.next) {
            // entry.name() is the "01", "02"... essentially the weapon type id
            WeaponTypeInfo info = json.readValue(WeaponTypeInfo.class, entry);
            infoByTypeID.put(entry.name(), info);
        }
    }

    public WeaponTypeInfo getByGlobalID(String globalID) {
        if (ClassDigit.classDigit(globalID) != ClassDigit.WEAPON) return null;
        return infoByTypeID.get(ClassDigit.typeID(globalID));   // was substring(1)
    }

}
