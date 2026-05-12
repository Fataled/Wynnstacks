package net.fataled.wynnstacks.client.rendering;

import java.util.Map;

import net.fataled.wynnstacks.client.config.HudConfig;
import net.fataled.wynnstacks.client.util.LoggerUtils;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;

public final class PuaStyler {

    private PuaStyler() {
    }

    private static final Identifier PUA_FONT = Identifier.of("wynnstacks", "default");
    public static final StyleSpriteSource.Font Pua_Font = new StyleSpriteSource.Font(PUA_FONT);

    private static Map<Integer, String> profiles = Map.of(
            0x271C, HudConfig.MARKED,
            0x2699, HudConfig.DISCOMBOBULATE,
            0x2620, HudConfig.POISON,
            0xE03A, HudConfig.TRICKS,
            0xE03F, HudConfig.DRAINED,
            0xE03D, HudConfig.ENKINDLED,
            0xE03C, HudConfig.CONFUSION,
            0xE043, HudConfig.CONTAMINATION,
            0x2694, HudConfig.WEAKENED);

    private static HudConfig.Profile getProfile(int codePoint) {
        String key = profiles.get(codePoint);
        return key == null ? null : HudConfig.INSTANCE.obtain(key);

    }

    private static boolean isPUA(int cp) {
        return cp >= 0xE000 && cp <= 0xF8FF;
    }

    public static Text stylePUAOnly(String s) {
        MutableText out = Text.empty();
        HudConfig.Profile profile = null;
        LoggerUtils.info("Trying to render: {}", s);
        int i = 0, n = s.length();
        int lastStyled = -1;
        while (i < n) {
            int cp = s.codePointAt(i);
            int cc = Character.charCount(cp);
            boolean styled = isPUA(cp) || profiles.containsKey(cp);
            MutableText seg = Text.literal(s.substring(i, i + cc));
            if (styled) {
                lastStyled = cp;
                LoggerUtils.info("lastStyled was:", lastStyled);
            }
            profile = getProfile(cp);
            if (profile == null)
                profile = getProfile(lastStyled);
            if (profile == null)
                profile = HudConfig.INSTANCE.obtain(HudConfig.DEBUFF);
            final boolean usePuaFont = isPUA(cp);
            final int segColor = profile.solidRgb;
            seg = seg.styled(st -> usePuaFont ? st.withFont(Pua_Font).withColor(segColor) : st.withColor(segColor));
            out.append(seg);
            i += cc;
        }
        return out;
    }
}
