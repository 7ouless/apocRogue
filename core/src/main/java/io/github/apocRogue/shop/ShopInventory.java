package io.github.apocRogue.shop;

import java.util.ArrayList;
import java.util.List;

public class ShopInventory {

    public static List<ShopKeeper> loadShopkeepers() {
        List<ShopKeeper> shopkeepers = new ArrayList<>();
        shopkeepers.add(new TraderA());
        shopkeepers.add(new TraderB());
        shopkeepers.add(new TraderC());
        return shopkeepers;
    }
}
