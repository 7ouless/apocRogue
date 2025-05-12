package io.github.apocRogue.potion;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import io.github.apocRogue.globals.ids.ClassDigit;
import io.github.apocRogue.potion.PotionTypeInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PotionTypeRegistry {
    private final Map<String, PotionTypeInfo> infoByTypeID = new HashMap<>();


    public void load() {
        FileHandle fh = Gdx.files.internal("data/potion_types.json");
        JsonValue root = new JsonReader().parse(fh);
        Json json = new Json();
        for (JsonValue e = root.child; e != null; e = e.next) {
            PotionTypeInfo info = json.readValue(PotionTypeInfo.class, e);
            infoByTypeID.put(e.name(), info);
        }
    }


    public PotionTypeInfo getByGlobalID(String globalID) {
        if (ClassDigit.classDigit(globalID) != ClassDigit.POTION) return null;
        return infoByTypeID.get(ClassDigit.typeID(globalID));
    }


    public Set<String> getAllGlobalIDs() {
        Set<String> out = new HashSet<>();
        for (String local : infoByTypeID.keySet()) {
            out.add(ClassDigit.prefix(ClassDigit.POTION, local));
        }
        return out;
    }
}
