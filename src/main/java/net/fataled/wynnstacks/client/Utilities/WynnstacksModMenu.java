package net.fataled.wynnstacks.client.Utilities;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fataled.wynnstacks.client.HudConfig.HudconfigScreen;

public class WynnstacksModMenu implements  ModMenuApi{
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // Pass the parent screen to your custom config screen constructor
        return parent -> new HudconfigScreen(parent);
    }
}