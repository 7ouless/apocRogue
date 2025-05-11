// InventoryPreferences.java
package io.github.apocRogue.inventory.general;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Json;
import java.util.*;

/**
 * Persists the player’s hotbar as an ordered list of encoded item IDs.
 */
public class InventoryPreferences {
    private static final String PREF_NAME   = "inventory_prefs";
    private static final String KEY_HOTBAR  = "hotbar_item_codes";

    private static Preferences prefs() {
        return Gdx.app.getPreferences(PREF_NAME);
    }

    /** Overwrites the saved hotbar order. */
    public static void saveHotbar(List<String> itemCodes) {
        String json = new Json().toJson(itemCodes);
        prefs().putString(KEY_HOTBAR, json).flush();
    }

    /** Returns the saved hotbar order (empty list if none). */
    @SuppressWarnings("unchecked")
    public static List<String> loadHotbar() {
        String json = prefs().getString(KEY_HOTBAR, "[]");
        try {
            return new Json().fromJson(ArrayList.class, String.class, json);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /** Adds the code to the end of the hotbar (if not already present). */
    public static void addToHotbar(String itemCode) {
        List<String> list = loadHotbar();
        if (!list.contains(itemCode)) {
            list.add(itemCode);
            saveHotbar(list);
        }
    }

    /** Removes the code from the hotbar (if present). */
    public static void removeFromHotbar(String itemCode) {
        List<String> list = loadHotbar();
        if (list.remove(itemCode)) {
            saveHotbar(list);
        }
    }

    /** Clears out all saved hotbar slots. */
    public static void clearHotbar() {
        saveHotbar(Collections.emptyList());
    }
}
