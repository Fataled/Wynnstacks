package net.fataled.shadestepperqol.shadestepper_qol_huds.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ShadestepperModMenu implements  ModMenuApi{
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // Pass the parent screen to your custom config screen constructor
        return parent -> new HudconfigScreen(parent);
    }
}