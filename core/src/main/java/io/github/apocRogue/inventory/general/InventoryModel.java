package io.github.apocRogue.inventory.general;

import io.github.apocRogue.weapons.Weapon;
import java.util.ArrayList;
import java.util.List;

/** Pure data model for “stash” items. */
public class InventoryModel {
    private final List<Weapon> items = new ArrayList<>();

    /** Add a weapon to the stash */
    public boolean add(Weapon w) {
        return items.add(w);
    }

    /** Remove a weapon from the stash */
    public boolean remove(Weapon w) {
        return items.remove(w);
    }

    /** Get a snapshot of all stash items */
    public List<Weapon> getAll() {
        return new ArrayList<>(items);
    }
}
