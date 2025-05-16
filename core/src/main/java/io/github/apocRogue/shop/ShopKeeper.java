// File: ShopKeeper.java
package io.github.apocRogue.shop;

import java.util.List;


public class ShopKeeper {    private final String sellerID;
    private final String displayName;
    private final String portraitKey;

    private final String[] greetingLines;
    private final String[] thankYouLines;
    private final String[] soldOutLines;
    private final String[] noMoney;
    private final String[] lowLevel;
    public ShopKeeper(String id,
                      String name,
                      String portraitKey,
                      String[] greeting,
                      String[] thankYou,
                        String[] noMoney,
                      String[] lowLevel,
                      String[] soldOut
    ) {
        this.sellerID      = id;
        this.displayName   = name;
        this.portraitKey   = portraitKey;
        this.greetingLines = greeting;
        this.thankYouLines = thankYou;
        this.soldOutLines  = soldOut;
        this.noMoney = noMoney;
        this.lowLevel = lowLevel;
    }

    /* ---------- getters used by ShopUI ---------- */
    public String   getSellerID()     { return sellerID; }
    public String   getDisplayName()  { return displayName; }
    public String   getPortraitPath() { return "ui/portraits/" + portraitKey + ".png"; }

    public String   getGreeting()     { return greetingLines.length>0 ? greetingLines[0] : ""; }
    public String   getThankYou()     { return thankYouLines.length>0 ? thankYouLines[0] : "Thanks!"; }
    public String   getSoldOutLine()  { return soldOutLines.length>0 ? soldOutLines[0] : "Sold out"; }

    /* expose full arrays if UI ever wants variation */
    public String[] getGreetingLines() { return greetingLines; }
    public String[] getThankYouLines() { return thankYouLines; }
    public String[] getSoldOutLines()  { return soldOutLines; }
    public String[] getNoMoney() { return noMoney; }
    public String[] getLowLevel() { return lowLevel; }
}
