package net.fataled.wynnstacks.client.Utilities;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fataled.wynnstacks.client.HudConfig.HudConfig;
import net.fataled.wynnstacks.client.HudConfig.HudconfigScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;



public class KeybindManager {

    public static KeyBinding openHudConfig;
    public static KeyBinding toggleDebug;


    public static void register() {
        openHudConfig = registerKey("Key.hud.open_config", GLFW.GLFW_KEY_H);
        toggleDebug = registerKey("key.shadestepperqol.toggleDebug", GLFW.GLFW_KEY_F7);


        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (openHudConfig.wasPressed()) {
                client.setScreen(new HudconfigScreen(client.currentScreen));
            }

            while (toggleDebug.wasPressed()) {
                HudConfig.INSTANCE.debug = !HudConfig.INSTANCE.debug;
            }

        });

    }

        private static KeyBinding registerKey (String name,int keyCode){
            KeyBinding key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    name,
                    InputUtil.Type.KEYSYM,
                    keyCode,
                    "category.hud"
            ));
            return key;
        }

}
