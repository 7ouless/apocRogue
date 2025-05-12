package io.github.apocRogue.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import io.github.apocRogue.globals.ids.ClassDigit;   // ← ADD
import java.util.*;

public class ItemTypeRegistry {
    private final Map<String, ItemTypeInfo> infoByTypeID = new HashMap<>();


    public void load() {
        FileHandle fh = Gdx.files.internal("data/item_types.json");
        JsonValue root = new JsonReader().parse(fh);
        Json json = new Json();
        for (JsonValue e = root.child; e != null; e = e.next) {
            infoByTypeID.put(e.name(), json.readValue(ItemTypeInfo.class, e));
        }
    }


    public ItemTypeInfo getByGlobalID(String globalID) {
        if (ClassDigit.classDigit(globalID) != ClassDigit.ITEM) return null;
        return infoByTypeID.get(ClassDigit.typeID(globalID));   // was localID / substring
    }


    public Set<String> getAllGlobalIDs() {
        Set<String> out = new HashSet<>();
        for (String local : infoByTypeID.keySet())
            out.add(ClassDigit.prefix   (ClassDigit.ITEM,  local));
        return out;
    }
}
