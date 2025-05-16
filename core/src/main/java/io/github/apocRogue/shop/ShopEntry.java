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

    public LinkedHashMap<String,Integer> stats;
}
