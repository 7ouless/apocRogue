package io.github.apocRogue.globals.ids;


public final class ClassDigit {


    public static final char WEAPON = '1';
    public static final char ITEM   = '2';
    public static final char POTION = '3';

    private ClassDigit() {}



    public static char classDigit(String id) {
        return id.charAt(0);
    }


    public static String typeID(String id) {
        return id.substring(1, 3);
    }


    public static String statBits(String id) {
        return id.length() <= 3 ? "" : id.substring(3);
    }


    public static String prefix(char digit, String typeID) {
        if (typeID.length() != 2)
            throw new IllegalArgumentException("typeID must be 2 chars");
        return "" + digit + typeID;
    }
}
