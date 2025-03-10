package io.github.apocRogue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Array;
import io.github.apocRogue.Weapon;

public class ItemManager {
    private Array<Weapon> loadedWeapons;

    public ItemManager() {
        loadedWeapons = new Array<>();
    }

    public void loadFromFile(String jsonFilePath) {
        Json json = new Json();
        // Read the file into a WeaponData[] array
        WeaponData[] dataArray = json.fromJson(WeaponData[].class, Gdx.files.internal("ui/items.json"));

        // Convert each WeaponData to a real Weapon object
        for (WeaponData data : dataArray) {
            // Create a Weapon using the data
            Weapon w = new Weapon(
                data.name,
                data.damage,
                new Texture(Gdx.files.internal(data.texturePath)),
                data.projectileType
            );
            loadedWeapons.add(w);
        }
    }

    public Array<Weapon> getLoadedWeapons() {
        return loadedWeapons;
    }
}
