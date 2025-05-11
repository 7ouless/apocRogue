package io.github.apocRogue.inventory.general;

public class InventoryItem {
    public final String itemCode;
    public final String typeID;
    public final String displayName;
    public final String iconPath;

    public InventoryItem(String itemCode,
                         String typeID,
                         String displayName,
                         String iconPath) {
        this.itemCode    = itemCode;
        this.typeID      = typeID;
        this.displayName = displayName;
        this.iconPath    = iconPath;
    }
}
