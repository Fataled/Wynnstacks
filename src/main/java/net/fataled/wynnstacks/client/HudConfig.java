package net.fataled.wynnstacks.client;

import com.google.gson.annotations.SerializedName;


public class HudConfig {
    public static HudConfig INSTANCE = new HudConfig();
  @SerializedName("x")  public  int x = 10;
  @SerializedName("y")  public  int y = 10;
  @SerializedName("color")  public  int color = 0xFFFFFF;
  @SerializedName("scale")  public  float scale = 1.0f;
  @SerializedName("Target Distance") public float maxTargetDistance = 72.0f;
  @SerializedName("debug") public boolean debug = false;
  @SerializedName("range") public double range = 72.0;   // blocks
  @SerializedName("coneAngleDeg") public int coneAngleDeg = 30;
  @SerializedName("ignorePlayers") public boolean ignorePlayers = true;
  @SerializedName("showHud") public boolean showHud = true;
  @SerializedName("AspectLvl2") public boolean AspectLvl2 = false;
  @SerializedName("Satx")  public  int Satx = 0;
  @SerializedName("Saty")  public  int Saty = 40;
  @SerializedName("useEndSounds") public boolean useEndSounds = true;
  @SerializedName("Volume") public float Volume = 10f;
  @SerializedName("Satsujin Scale") public float sScale = 1.0f;
  @SerializedName("rcX") public int rcX = 0;
  @SerializedName("rcY") public int rcY = 0;
  @SerializedName("rcS") public float rcS = 1.0f;
  @SerializedName("lineGap") public int lineGap = 10;
  @SerializedName("showRaidCounter") public boolean showRaidCounter = true;

  public static final String CONFIG_FILE = "shadestepper_hud_config.json";
}
