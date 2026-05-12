package net.fataled.wynnstacks.client.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fataled.wynnstacks.client.config.screen.HudConfigScreen;

public class WynnstacksModMenu implements  ModMenuApi{
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // Pass the parent screen to your custom config screen constructor
        return parent -> new HudConfigScreen(parent);
    }
}