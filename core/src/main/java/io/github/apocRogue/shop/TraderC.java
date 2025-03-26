package io.github.apocRogue.shop;

import java.util.ArrayList;
import java.util.List;

public class TraderC extends ShopKeeper {
    public TraderC() {
        super("Trader C", "portrait-traderC", createInventory());
    }

    private static List<ShopItem> createInventory() {
        List<ShopItem> items = new ArrayList<>();
        //Always unlocked items
        items.add(new ShopItem("Armor", "Small iron armour, should fit you fine though. Give me 300 gold and it's yours.", 300));
        items.add(new ShopItem("Axe", "Tiny Axe. You might need two hands for it though. Give me 250 gold and it's yours.", 250));

        // Level 2 items
        items.add(new ShopItem("Big Axe",
            "A normal Axe! Maybe you can use it as roof... or SeeSaw. You're small man. Give me 500 gold and it's yours.",
            1000,
            2)
        );
        items.add(new ShopItem("Bow",
            "Toothpick shooter! Maybe you can be dentist from far away. HA HA HA! Me funny! Give me 700 gold and it's yours. ",
            700,
            2)
        );

        return items;
    }
    // Personality lines
    @Override
    public String getGreeting() {
        return "Trader C: Hello, I am Igor. I am Shop. Thank you.";
    }

    @Override
    public String getThankYouLine() {
        return "Trader C: This makes me happy, tiny person! Do you want another?";
    }

    @Override
    public String getCannotAffordLine() {
        return "Trader C: It seems your wallet is as small as you, give more money.";
    }

    @Override
    public String getLockedItemLine() {
        return "Trader C: Ha Ha Ha, that is for grown ups little boy.";
    }
}
