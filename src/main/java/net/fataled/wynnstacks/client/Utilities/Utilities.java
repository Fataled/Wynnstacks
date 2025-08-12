package net.fataled.wynnstacks.client.Utilities;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Utilities {

    private static final Identifier PUA_FONT = Identifier.of("wynnstacks","stat_icons");

    private static boolean isPUA(int cp) { return cp >= 0xE000 && cp <= 0xF8FF; }

    public static Text stylePUAOnly(String s) {
        MutableText out = Text.empty();
        int i = 0, n = s.length();
        while (i < n) {
            int j = i;
            boolean pua = isPUA(s.codePointAt(i));
            while (j < n) {
                int cp = s.codePointAt(j);
                if (isPUA(cp) != pua) break;
                j += Character.charCount(cp);
            }
            MutableText seg = Text.literal(s.substring(i, j));
            if (pua) seg = seg.styled(st -> st.withFont(PUA_FONT)); // <- force your sheet
            out.append(seg);
            i = j;
        }
        return out;
    }

}
