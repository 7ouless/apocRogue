package io.github.apocRogue.shop;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import java.util.ArrayList;
import java.util.List;

public class ShopInventory {

    public static List<ShopKeeper> loadShopkeepers(Skin skin) {
        List<ShopKeeper> shopkeepers = new ArrayList<>();

        List<ShopItem> traderAItems = new ArrayList<>();
        traderAItems.add(new ShopItem("Potion", "Ah! A potion, I blieve this one cures your wounds. I'll let you have it for 100 gold.", 100));
        traderAItems.add(new ShopItem("Random Brew", "I unfortunetly lost the label on this one. Whoopsie! I'll let you have it for 50 gold.", 50));

        List<ShopItem> traderBItems = new ArrayList<>();
        traderBItems.add(new ShopItem("Stabiliser", "Seems to stabilise the affected area. I like to throw them and just sit there for a while. It's all yours for 500 gold.", 500));
        traderBItems.add(new ShopItem("Grenade", "Boom? Boom! It's all yours for 200 gold.", 200));

        List<ShopItem> traderCItems = new ArrayList<>();
        traderCItems.add(new ShopItem("Armor", "Small iron armour, should fit you fine though. Give me 300 gold and it's yours.", 300));
        traderCItems.add(new ShopItem("Axe", "Tiny Axe. You might need two hands for it though. Give me 250 gold and it's yours.", 250));


        shopkeepers.add(new ShopKeeper("Trader A", "portrait-traderA", traderAItems));
        shopkeepers.add(new ShopKeeper("Trader B", "portrait-traderB", traderBItems));
        shopkeepers.add(new ShopKeeper("Trader C", "portrait-traderC", traderCItems));


        return shopkeepers;
    }
}
