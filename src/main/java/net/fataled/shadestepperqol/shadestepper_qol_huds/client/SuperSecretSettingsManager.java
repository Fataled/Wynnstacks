package net.fataled.shadestepperqol.shadestepper_qol_huds.client;

public class SuperSecretSettingsManager {
    private static boolean enabled = false;

    public static boolean toggle() {
        enabled = !enabled;
        return enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void set(boolean value) {
        enabled = value;
    }

}
