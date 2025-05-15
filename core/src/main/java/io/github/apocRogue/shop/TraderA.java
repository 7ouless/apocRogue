// File: TraderA.java
package io.github.apocRogue.shop;

import java.util.List;

public class TraderA extends ShopKeeper {
    public TraderA() {
        super(
                "A",                             // sellerID
                "Bartholomew the Bold",         // displayName
                "traderA",                       // portraitKey
                new String[]{ "Hello, dear friend!", "Looking for potions?" },

                new String[]{ "Thanks a ton!", "Need another?" },

                new String[]{ "You don’t have enough gold." },

                new String[]{ "You’re not trusted enough yet." },

                new String[]{ "Sorry, I’m sold out." }

        );
    }
}
