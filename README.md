# WynnStacks

A client-side Fabric mod for Minecraft **1.21.11** that adds a configurable
HUD for the [Wynncraft](https://wynncraft.com) server. It surfaces relevant
mob debuff stacks while you're fighting and tracks the Satsujin
(Shadestepper aspect) cooldown so you don't have to count it in your head.

- **Version:** 2.0
- **Minecraft:** 1.21.11
- **Side:** client only
- **License:** MIT
- **Author:** Fataled

## What it does

### Debuff HUD
Whenever you look at a priority mob (raid bosses, notable named enemies,
training dummies, etc.), WynnStacks reads the Wynncraft text-display labels
floating above the mob, strips out unrelated lines like damage popups, and
draws a clean stack of the active debuff symbols + counts in the corner of
your screen. The HUD lingers briefly after you look away so quick target
swaps don't make the readout flicker.

You can pick exactly which stat symbols to track (✜, ⚙, ☠, ⚔, and a set of
Wynncraft's private-use glyphs for things like Marked, Poison, Tricks,
Drained, Enkindled, Confusion, Contamination, Weakened, Twilight…), and
style each one independently — fill color, outline color/thickness, scale,
and alignment are all per-profile.

### Satsujin cooldown timer
A separate HUD element that starts a 10s or 15s countdown when you cast
Satsujin as a Shadestepper. Detection is sound-based: the mod listens for
the `end_portal.spawn` + `enchantment_table.use` pair that Wynncraft plays
on cast and confirms by looking at the targeted mob's debuff stacks. When
the timer ends it can optionally play a finish sound (trident return) at a
volume you configure.

Toggle "Aspect Lv2" in the config if you have the upgraded aspect (shortens
the cooldown from 15s to 10s).

### Other niceties
- Player IGNs are filtered out of label scans automatically (refreshed
  from the tab list once a second).
- Config is JSON-backed and autosaved every ~5 minutes plus on demand.
- ModMenu integration — opens the config screen from the mods list.
- Help and Demo client commands (see below).

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.11.
2. Install [Fabric API](https://modrinth.com/mod/fabric-api) matching
   1.21.11.
3. (Recommended) Install [ModMenu](https://modrinth.com/mod/modmenu) so you
   can open the config from the in-game mods list.
4. Drop the WynnStacks `.jar` into your `mods/` folder.

## Build from source

This project uses the Gradle wrapper. From the repo root:

```sh
./gradlew build
```

The output jar lands in `build/libs/`. To launch a dev client with the mod
loaded:

```sh
./gradlew runClient
```

## Controls and commands

| Key / command         | Default | What it does                       |
| --------------------- | ------- | ---------------------------------- |
| Open HUD Config       | `H`     | Opens the WynnStacks config screen |
| Toggle debug mode     | `F10`   | Toggles verbose debug logging      |
| `/Wynnstacks Help`    | —       | In-chat help text                  |
| `/Wynnstacks Demo`    | —       | Pushes a fake HUD readout for ~10s |

Both keybinds can be rebound from Minecraft's standard Controls menu (the
WynnStacks bindings show up under their own categories).

## Config

The screen at `H` exposes:

- **Debuff HUD** — show/hide, X/Y position, scale, alignment, fill +
  outline color, outline thickness, target distance, cone angle (how
  forgiving the look-at raycast is), and an "Ignore players" toggle.
- **Symbol picker** — checkboxes for every supported stat glyph; disabled
  ones are stripped from the HUD output.
- **Satsujin** — show/hide, X/Y, scale, color, Aspect Lv2 toggle, end-of-
  countdown sound toggle, volume slider, audio range.

Settings are written to `config/wynnstacks-config.json` in your game
directory.

## Project layout

```
src/main/java/net/fataled/wynnstacks/client
├── WynnstacksClient.java          ClientModInitializer entry point
├── commands/Commands.java         /Wynnstacks Help and Demo
├── config/
│   ├── HudConfig.java             Config schema (singleton)
│   ├── HudConfigManager.java      JSON load/save
│   └── screen/HudConfigScreen.java In-game config UI
├── input/KeybindManager.java      Keybindings
├── integration/WynnstacksModMenu.java ModMenu hook
├── label/
│   ├── MobLabelUtils.java         Read + clean Wynncraft text-display labels
│   └── IgnPattern.java            Cached regex of online player IGNs
├── rendering/
│   ├── HudRender.java             Per-frame HUD draw callback
│   └── PuaStyler.java             Styles private-use-area glyphs per profile
├── sound/SoundListener.java       Satsujin cast detection + countdown
└── util/
    ├── RaycastUtils.java          Cone-based look-at entity selection
    ├── ClientUtils.java
    └── LoggerUtils.java
```

## License

MIT. See `LICENSE.txt`.

## Disclaimer

WynnStacks is not affiliated with or endorsed by Wynncraft or Mojang. It
only reads information the server already sends to your client and draws
it in a more readable form — no automation, no packet injection, no
gameplay automation.
