package io.github.apocRogue.shop;

import io.github.apocRogue.globals.ids.ClassDigit;
import java.util.Map;


public class ShopWeaponPayload {


    public String id;

    public Map<String,Integer> stats;


    public char   getClassDigit() { return ClassDigit.classDigit(id); }
    public String getTypeID()     { return ClassDigit.typeID(id); }
    public String getStatBits()   { return ClassDigit.statBits(id); }
}
