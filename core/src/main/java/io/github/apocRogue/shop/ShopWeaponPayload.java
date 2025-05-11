package io.github.apocRogue.shop;
import java.util.Map;

public class ShopWeaponPayload {
    public String itemCode;          // “ID…” string from the server
    public String typeID;            // two-char weapon type (“01”, “SW”…)
    public Map<String,Integer> stats;// damage, projectileValue, dashSpeed, …
}
