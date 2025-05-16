package io.github.apocRogue.shop;

import java.util.List;

public final class ShopInventory {

    private ShopInventory() {} // static-only

    public static List<ShopKeeper> loadShopkeepers() {
        return List.of(
            new TraderA(),
            new TraderB(),
            new TraderC()
        );
    }

    public static ShopKeeper byId(String sellerID) {
        return loadShopkeepers().stream()
            .filter(t -> t.getSellerID().equals(sellerID))
            .findFirst()
            .orElse(null);
    }
}
