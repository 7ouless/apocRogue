package io.github.apocRogue.shop;

import java.util.ArrayList;
import java.util.List;

public class TraderA extends ShopKeeper {
    public TraderA() {
        super("Trader A", "portrait-traderA", createInventory());
    }

    private static List<ShopItem> createInventory() {
        List<ShopItem> items = new ArrayList<>();
        // Always unlocked items:
        items.add(new ShopItem("Potion", "Ah! A potion, I believe this one cures your wounds. I'll let you have it for 100 gold.",
            100,
            1,
            10));
        items.add(new ShopItem("Random Brew", "I unfortunately lost the label on this one. Whoopsie! I'll let you have it for 50 gold.",
            50,
            1,
            5));

        // Level 2 items
        items.add(new ShopItem("Speed Potion",
            "Good choice friend! This one is will help you get to where you want to be quicker. I'll let you have it for 250 gold.",
            250,
            2,
            10)
        );
        items.add(new ShopItem("Anti-Gravity Potion",
            "I don't quite understand how this one works, but it seems to inverse the gravitational pull you body experiences. I'll let you have it for 1000 gold.",
            1000,
            2,
            5)
        );

        return items;
    }

    // Personality lines
    @Override
    public String getGreeting() {
        return "Hello, dear friend. Looking for potions or maybe something... rarer?";
    }

    @Override
    public String getThankYouLine() {
        return "Much appreciated! Would you like another?";
    }

    @Override
    public String getCannotAffordLine() {
        return "Sorry, but you don't have the gold for that.";
    }

    @Override
    public String getLockedItemLine() {
        return "Shhh, you haven't earned my trust enough to see that.";
    }

    @Override
    public String getSoldOutLine() {
        return "I'm very sorry, It seems I am sold out on that item.";
    }
}
