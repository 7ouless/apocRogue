package io.github.apocRogue.inventory.general;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Json;
import java.util.ArrayList;
import java.util.List;


public class InventoryPreferences {
    private static final String PREF_NAME = "inventory_prefs";
    private static final String KEY_ITEMS  = "items_json";

    public static void save(List<String> names) {
        Preferences prefs = Gdx.app.getPreferences(PREF_NAME);
        String json = new Json().toJson(names);
        prefs.putString(KEY_ITEMS, json);
        prefs.flush();
    }

    @SuppressWarnings("unchecked")
    public static List<String> load() {
        Preferences prefs = Gdx.app.getPreferences(PREF_NAME);
        String str = prefs.getString(KEY_ITEMS, null);
        if (str == null) return new ArrayList<>();
        return new Json().fromJson(ArrayList.class, String.class, str);
    }


    public static void add(String name) {
        List<String> list = load();
        list.add(name);
        save(list);
    }
}
