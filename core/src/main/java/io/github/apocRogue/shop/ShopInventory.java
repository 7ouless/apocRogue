package io.github.apocRogue.shop;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import java.util.ArrayList;
import java.util.List;

public class ShopInventory {

    public static List<ShopKeeper> loadShopkeepers(Skin skin) {
        List<ShopKeeper> shopkeepers = new ArrayList<>();

        List<ShopItem> traderAItems = new ArrayList<>();
        traderAItems.add(new ShopItem("Potion", "Restores Health", 100));
        traderAItems.add(new ShopItem("Random Brew", "Provides a random effect", 150));

        List<ShopItem> traderBItems = new ArrayList<>();
        traderBItems.add(new ShopItem("Stabiliser", "Seems to stabilise the affected area", 50));
        traderBItems.add(new ShopItem("Grenade", "Boom?", 200));

        List<ShopItem> traderCItems = new ArrayList<>();
        traderCItems.add(new ShopItem("Armor", "Defensive wear", 300));
        traderCItems.add(new ShopItem("Axe", "Heavy weapon", 250));


        shopkeepers.add(new ShopKeeper("Trader A", "portrait-traderA", traderAItems));
        shopkeepers.add(new ShopKeeper("Trader B", "portrait-traderB", traderBItems));
        shopkeepers.add(new ShopKeeper("Trader C", "portrait-traderC", traderCItems));


        return shopkeepers;
    }
}
