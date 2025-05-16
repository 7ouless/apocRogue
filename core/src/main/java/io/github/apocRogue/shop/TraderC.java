package io.github.apocRogue.shop;

import java.util.List;

public class TraderC extends ShopKeeper {
    public TraderC() {
        super(
                "C",
                "Igor",
                "traderC",
                new String[]{ "Hello, I am Igor. I am Shop. Thank you."},
                new String[]{"This makes me happy, tiny person! Do you want another?"},

                new String[]{"It seems your wallet is as small as you, give more money."},

                new String[]{"Ha Ha Ha, that is for grown ups, little boy." },
                new String[]{"Hmmm, this as much as I have on me. Sorry little man."}

        );
    }
}
