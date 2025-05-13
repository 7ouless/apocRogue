package io.github.apocRogue.shop;

import java.util.List;

/**
 * Utility class that returns the visual ShopKeeper
 * instances used by the client.
 */
public final class ShopInventory {

    private ShopInventory() {} // static-only

    /** List of all traders in the game, in the order you want them shown. */
    public static List<ShopKeeper> loadShopkeepers() {
        return List.of(
            new TraderA(),
            new TraderB(),
            new TraderC()
        );
    }

    /** Helper if you ever need to look one up by ID. */
    public static ShopKeeper byId(String sellerID) {
        return loadShopkeepers().stream()
            .filter(t -> t.getSellerID().equals(sellerID))
            .findFirst()
            .orElse(null);
    }
}
