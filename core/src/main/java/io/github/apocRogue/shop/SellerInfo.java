package io.github.apocRogue.shop;

import java.util.List;

public class SellerInfo {
    public String sellerID;        // "A", "B", "C"
    public String displayName;     // e.g. "Bartholomew the Bold"
    public String portraitPath;    // "ui/portraits/traderA.png"
    public int    spentGauge;      // gold spent / reputation
    public List<ShopEntry> items;  // today's rolled items
}
