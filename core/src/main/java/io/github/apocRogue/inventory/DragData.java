package io.github.apocRogue.inventory;

import io.github.apocRogue.weapons.Weapon;

public class DragData {
    public InventorySlot sourceSlot;
    public Weapon weapon;

    public DragData(InventorySlot sourceSlot, Weapon weapon) {
        this.sourceSlot = sourceSlot;
        this.weapon = weapon;
    }
}
