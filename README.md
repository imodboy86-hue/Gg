# HELPER CLIENT 0.4.1

Minecraft Java 1.21.11 / Fabric client-side mod.

## Included / working in this source
- Dark, paged module menu and persistent ON/OFF settings
- HUD Editor preferences: scale and opacity values are saved
- Fullbright (gamma override, restored when disabled)
- Toggle Sprint
- Zoom (hold C)
- FPS, XYZ, CPS, Keystrokes and Armor Status HUD
- Custom Crosshair overlay

## Requested modules listed in the menu
Mini Items, Low Fire, TierTagger, visual Hitbox, Potion Effects, HurtCam, Freelook, Combo Counter, Damage Tint, Sound Hit, Block Overlay and Performance Mode are present as settings entries, but their actual rendering/behavior is not implemented in this build yet. They require extra mixins, render hooks or server-specific tier data. A visible toggle does not mean the feature is active. Hitbox is intended as a visual-only display; no hitbox enlargement or automated attacks are included.

## Build
Use JDK 21 and Gradle: `gradle build`. The jar should appear in `build/libs/`. Install it in the `mods` folder alongside compatible Fabric API for Minecraft 1.21.11.

This source has not been compiled or tested in a live Minecraft or Mojo Launcher session.

## Branding update 0.4.1
The custom in-game Helper Client screen now uses its own top navigation and
HELPER CLIENT title. It does not alter Lunar Client's proprietary interface;
it provides an original Helper Client menu instead.
