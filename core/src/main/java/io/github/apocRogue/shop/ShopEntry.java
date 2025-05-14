package io.github.apocRogue.shop;

import java.util.LinkedHashMap;
import java.util.Map;

public class ShopEntry {
    public String itemCode;
    public String typeID;
    public String name;
    public String texturePath;
    public int    price;
    public int    remaining;

    /** rolled or base stats sent by the server, e.g. {"damage":15,"cooldown":96} */
    public LinkedHashMap<String,Integer> stats;   // concrete type — Json can build it
}
