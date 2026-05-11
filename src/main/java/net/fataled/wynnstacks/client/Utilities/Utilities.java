package net.fataled.wynnstacks.client.Utilities;

import net.minecraft.text.*;
import net.minecraft.util.Identifier;


public final class Utilities {

    private Utilities() {}

    private static final Identifier PUA_FONT = Identifier.of("wynnstacks", "default");
    public static final StyleSpriteSource.Font Pua_Font = new StyleSpriteSource.Font(PUA_FONT);

    private static boolean isPUA(int cp) {
        return cp >= 0xE000 && cp <= 0xF8FF;
    }

    public static Text stylePUAOnly(String s) {
        boolean anyPua = false;
        int i = 0;
        while (i < s.length()){
            int codePoint = s.codePointAt(i);
            if(isPUA(codePoint)) {anyPua = true; break;}
            i += Character.charCount(codePoint);
        }

        if (!anyPua) return Text.literal(s);

        MutableText out = Text.empty();
        i = 0;
        int n = s.length();
        while (i < n) {
            int j = i;
            boolean pua = isPUA(s.codePointAt(i));
            while (j < n) {
                int cp = s.codePointAt(j);
                if (isPUA(cp) != pua) break;
                j += Character.charCount(cp);
            }
            MutableText seg = Text.literal(s.substring(i, j));
            if (pua) seg = seg.styled(st -> st.withFont(Pua_Font)); // <- force your sheet
            out.append(seg);
            i = j;
        }
        return out;
    }
}



