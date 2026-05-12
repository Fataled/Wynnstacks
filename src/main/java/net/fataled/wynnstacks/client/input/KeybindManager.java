package net.fataled.wynnstacks.client.input;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fataled.wynnstacks.client.config.screen.HudConfigScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;



public class KeybindManager {

    private static KeyBinding openHudConfig;

    public static void register() {
        openHudConfig = registerKey("Key.hud.open_config", GLFW.GLFW_KEY_H, "wynnstacks.hud");
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (openHudConfig.wasPressed()) {
                client.setScreen(new HudConfigScreen(client.currentScreen));
            }

        });

    }
        private static KeyBinding registerKey (String name,int keyCode, String id){

            return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    name,
                    keyCode,
                    KeyBinding.Category.create(Identifier.of(id))
            ));
        }

}
