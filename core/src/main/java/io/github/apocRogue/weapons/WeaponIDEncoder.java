package io.github.apocRogue.weapons;

import java.util.Map;

/** Builds IDs like "ID" + typeID(2) + skull(1)+sub(1) + 2-hex-chars per stat. */
public class WeaponIDEncoder {
    private static final String[] HEX = {
        "0","1","2","3","4","5","6","7","8","9",
        "A","B","C","D","E","F"
    };

    public static String encode(
        String typeID,
        int skullLevel, int skullSub,
        Map<String,Integer> stats
    ) {
        StringBuilder sb = new StringBuilder("ID");
        sb.append(typeID)
            .append(HEX[skullLevel]).append(HEX[skullSub]);

        for (String key : StatKeys.ALL) {
            int v = stats.getOrDefault(key, 0);
            // clamp 0–255
            v = Math.max(0, Math.min(v, 255));
            sb.append( HEX[(v>>4)&0xF] )
                .append( HEX[v & 0xF] );
        }
        return sb.toString();
    }
}
