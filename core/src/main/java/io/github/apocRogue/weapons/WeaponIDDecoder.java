package io.github.apocRogue.weapons;

import java.util.LinkedHashMap;
import java.util.Map;

public class WeaponIDDecoder {
    public static class Decoded {
        public final String typeID;
        public final int skullLevel, skullSub;
        public final Map<String,Integer> stats;
        public Decoded(String t, int sl, int ss, Map<String,Integer> s){
            typeID=t; skullLevel=sl; skullSub=ss; stats=s;
        }
    }

    public static Decoded decode(String id) {
        if (!id.startsWith("ID") || id.length() != 2 + 2 + StatKeys.ALL.length*2 + 2)
            throw new IllegalArgumentException("Invalid ID length");

        String typeID = id.substring(2,4);
        int skullLevel = fromHex(id.charAt(4));
        int skullSub   = fromHex(id.charAt(5));

        Map<String,Integer> stats = new LinkedHashMap<>();
        int pos = 6;
        for (String key : StatKeys.ALL) {
            int hi = fromHex(id.charAt(pos++));
            int lo = fromHex(id.charAt(pos++));
            stats.put(key, (hi<<4) + lo);
        }
        return new Decoded(typeID, skullLevel, skullSub, stats);
    }

    private static int fromHex(char c) {
        if (c>='0' && c<='9') return c - '0';
        return 10 + (c - 'A');
    }
}
