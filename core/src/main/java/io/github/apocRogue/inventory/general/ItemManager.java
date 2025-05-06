package io.github.apocRogue.inventory.general;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Array;
import java.util.HashMap;
import java.util.Map;
import io.github.apocRogue.weapons.WeaponData;

public class ItemManager {
    // Maps each 2-char typeID to its base‐stat map
    private final Map<String, Map<String,Integer>> baseStatsByTypeID = new HashMap<>();

    /**
     * Load every entry in your items.json into baseStatsByTypeID.
     * items.json must include "typeID" plus all StatKeys.ALL fields.
     */
    public void loadBaseData(String jsonPath) {
        Json json = new Json();
        // WeaponData needs a public String typeID field to pick this up :contentReference[oaicite:0]{index=0}:contentReference[oaicite:1]{index=1}
        WeaponData[] dataArray = json.fromJson(
            WeaponData[].class,
            Gdx.files.internal(jsonPath)
        );

        for (WeaponData data : dataArray) {
            Map<String,Integer> statMap = new HashMap<>();
            // Populate every slot in StatKeys.ALL :contentReference[oaicite:2]{index=2}:contentReference[oaicite:3]{index=3}
            statMap.put("damage", data.damage);
            statMap.put("projectileValue", data.projectileValue);
            statMap.put("animationSpeed", data.animationSpeed);
            statMap.put("noiseLevel", data.noiseLevel);
            // Convert float dash values into ints for hex‐packing:
            statMap.put("dashSpeed",    Math.round(data.dashSpeed));
            statMap.put("dashDuration", Math.round(data.dashDuration * 100));
            statMap.put("dashCooldown", Math.round(data.dashCooldown * 10));

            baseStatsByTypeID.put(data.typeID, statMap);
        }
    }

    /** @return a libGDX Array of every loaded typeID (e.g. "01","02",…) */
    public Array<String> getAllTypeIDs() {
        Array<String> ids = new Array<>(baseStatsByTypeID.keySet().toArray(new String[0]));
        return ids;
    }

    /**
     * @param typeID the 2-char code for a weapon (from items.json)
     * @return the map of base stats for rollAndEncode(...)
     */
    public Map<String,Integer> getBaseStats(String typeID) {
        return baseStatsByTypeID.get(typeID);
    }
}
