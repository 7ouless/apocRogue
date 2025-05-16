package io.github.apocRogue.shop;

import java.util.List;

public class TraderB extends ShopKeeper {
    public TraderB() {
        super(
                "B",
                "Destaros",
                "traderB",
                new String[]{ "\"What's up man! Take a look, anything you see you can buy. Not me though, I won't fall for that again\"" },

                new String[]{ "Thank you! I'll be sure to spend this... responsibly. Want one more?"},

                new String[]{ "You don't have enough money man. Are you trying to scam me?"},

                new String[]{"Woah woah woah. That's reserved for my brothers in ummm... arm?."}, //maybe only 1 arm from testing his own products... explosives

                new String[]{"Ah sorry man, that's all I've got for that item."}

        );
    }
}
