package io.github.apocRogue.shop;

import java.util.ArrayList;
import java.util.List;

public class TraderB extends ShopKeeper {
    public TraderB() {
        super("Trader B", "portrait-traderB", createInventory());
    }

    private static List<ShopItem> createInventory() {
        List<ShopItem> items = new ArrayList<>();
        //Always unlocked items
        items.add(new ShopItem("Stabiliser", "Seems to stabilise the affected area. I like to throw them and just sit there for a while. It's all yours for 500 gold.", 500));
        items.add(new ShopItem("Grenade", "Boom? Boom! It's all yours for 200 gold.", 200));

        // Level 2 items
        items.add(new ShopItem("Dynamite",
            "So remember that Boom? It is nothing compared to this Boom! It's all yours for 400 gold.",
            400,
            2)
        );
        items.add(new ShopItem("Confusion Bubble",
            "It seems to make some entities fight one another, found that out the hard way. I miss Mark. It's all yours for 1000 gold.",
            1000,
            2)
        );

        return items;
    }
    // Personality lines
    @Override
    public String getGreeting() {
        return "Trader B: What's up man! Take a look, anything you see you can buy. Not me though, I won't fall for that again,";
    }

    @Override
    public String getThankYouLine() {
        return "Trader B: Thank you! I'll be sure to spend this... responsibly. Want one more?";
    }

    @Override
    public String getCannotAffordLine() {
        return "Trader B: You don't have enough money man. Are you trying to scam me?";
    }

    @Override
    public String getLockedItemLine() {
        return "Trader B: Wow wow wow. That's reserved for my brothers in ummm... arm?."; //maybe only 1 arm from testing his own products... explosives
    }
}
