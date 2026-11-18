package com.helperclient;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

/** Main client entrypoint for Helper Client. */
public final class HelperClient implements ClientModInitializer {
    private static final int PANEL = 0xB8101726;
    private static final int KEY_BG = 0xCC1C2A3D;
    private static final int CYAN = 0xFF55D6FF;
    private static final int WHITE = 0xFFF2F6FF;
    private static final int MUTED = 0xFF9BAAC0;
    private static final int ACTIVE = 0xFF63E6A5;
    private static final int ZOOM_FOV = 30;

    private static final Deque<Long> LEFT_CLICKS = new ArrayDeque<>();
    private static final Deque<Long> RIGHT_CLICKS = new ArrayDeque<>();
    private static boolean wasAttackDown;
    private static boolean wasUseDown;
    private static boolean zooming;
    static boolean showInfoHud = true;
    static boolean showCpsHud = true;
    static boolean showKeystrokesHud = true;
    static boolean showArmorHud = true;
    private static int previousFov = -1;
    private static double originalGamma = -1.0;

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath("helper-client", "general")
    );
    private static final KeyMapping MENU_KEY = KeyBindingHelper.registerKeyBinding(
        new KeyMapping("key.helper-client.menu", InputConstants.Type.KEYSYM,
            InputConstants.KEY_RIGHT_SHIFT, CATEGORY)
    );
    private static final KeyMapping ZOOM_KEY = KeyBindingHelper.registerKeyBinding(
        new KeyMapping("key.helper-client.zoom", InputConstants.Type.KEYSYM,
            InputConstants.KEY_C, CATEGORY)
    );

    @Override
    public void onInitializeClient() {
        ClientConfig.load();
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick(client));
        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath("helper-client", "main_hud"),
            (graphics, deltaTracker) -> renderHud(graphics)
        );
    }

    private static void tick(Minecraft mc) {
        while (MENU_KEY.consumeClick()) {
            mc.setScreen(new HelperClientScreen());
        }
        if (mc.player == null) {
            restoreFov(mc);
            restoreGamma(mc);
            wasAttackDown = false;
            wasUseDown = false;
            return;
        }

        long now = System.currentTimeMillis();
        boolean attackDown = mc.options.keyAttack.isDown();
        boolean useDown = mc.options.keyUse.isDown();
        if (attackDown && !wasAttackDown) LEFT_CLICKS.addLast(now);
        if (useDown && !wasUseDown) RIGHT_CLICKS.addLast(now);
        wasAttackDown = attackDown;
        wasUseDown = useDown;
        trimClicks(LEFT_CLICKS, now);
        trimClicks(RIGHT_CLICKS, now);

        if (ClientConfig.enabled(ClientConfig.Feature.FULLBRIGHT)) {
            if (originalGamma < 0) originalGamma = mc.options.gamma().get();
            mc.options.gamma().set(16.0);
        } else restoreGamma(mc);
        if (ClientConfig.enabled(ClientConfig.Feature.TOGGLE_SPRINT) && mc.options.keyUp.isDown() && !mc.player.isCrouching()) mc.player.setSprinting(true);
        if (ZOOM_KEY.isDown() && ClientConfig.enabled(ClientConfig.Feature.ZOOM)) {
            if (!zooming) {
                previousFov = mc.options.fov().get();
                zooming = true;
            }
            mc.options.fov().set(ZOOM_FOV);
        } else {
            restoreFov(mc);
        }
    }

    private static void restoreGamma(Minecraft mc) {
        if (originalGamma >= 0) mc.options.gamma().set(originalGamma);
        originalGamma = -1.0;
    }

    private static void restoreFov(Minecraft mc) {
        if (zooming && previousFov >= 0) {
            mc.options.fov().set(previousFov);
        }
        zooming = false;
        previousFov = -1;
    }

    private static void trimClicks(Deque<Long> clicks, long now) {
        while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000L) {
            clicks.removeFirst();
        }
    }

    private static void renderHud(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        int x = 8;
        int y = 8;
        if (showInfoHud) {
            graphics.fill(x, y, x + 154, y + 55, PANEL);
            graphics.fill(x, y, x + 154, y + 2, CYAN);
            graphics.drawString(mc.font, "HELPER CLIENT", x + 7, y + 7, CYAN);
            if (ClientConfig.enabled(ClientConfig.Feature.FPS_DISPLAY)) graphics.drawString(mc.font, "FPS: " + mc.getFps(), x + 7, y + 21, WHITE);
            if (ClientConfig.enabled(ClientConfig.Feature.COORDINATES)) {
                String coords = String.format(Locale.ROOT, "XYZ: %.1f / %.1f / %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
                graphics.drawString(mc.font, coords, x + 7, y + 36, MUTED);
            }
        }

        if (showCpsHud && ClientConfig.enabled(ClientConfig.Feature.CPS_COUNTER)) {
            int cpsX = Math.max(8, graphics.guiWidth() - 94);
            graphics.fill(cpsX, 8, cpsX + 86, 43, PANEL);
            graphics.fill(cpsX, 8, cpsX + 86, 10, CYAN);
            graphics.drawString(mc.font, "LMB: " + LEFT_CLICKS.size(), cpsX + 7, 17, WHITE);
            graphics.drawString(mc.font, "RMB: " + RIGHT_CLICKS.size(), cpsX + 7, 29, MUTED);
        }

        if (showKeystrokesHud && ClientConfig.enabled(ClientConfig.Feature.KEYSTROKES)) {
            int kx = 170;
            int ky = 8;
            drawKey(graphics, mc, kx + 18, ky, "W", mc.options.keyUp.isDown());
            drawKey(graphics, mc, kx, ky + 18, "A", mc.options.keyLeft.isDown());
            drawKey(graphics, mc, kx + 18, ky + 18, "S", mc.options.keyDown.isDown());
            drawKey(graphics, mc, kx + 36, ky + 18, "D", mc.options.keyRight.isDown());
            drawKey(graphics, mc, kx, ky + 36, "SPACE", mc.options.keyJump.isDown());
        }

        if (showArmorHud && ClientConfig.enabled(ClientConfig.Feature.ARMOR_STATUS)) {
            int armorY = graphics.guiHeight() - 48;
            int armorIndex = 0;
            for (ItemStack stack : mc.player.getArmorSlots()) {
                if (!stack.isEmpty()) {
                    int remaining = stack.getMaxDamage() > 0
                        ? Math.max(0, stack.getMaxDamage() - stack.getDamageValue()) : 100;
                    int percent = stack.getMaxDamage() > 0
                        ? (int) (remaining * 100.0 / stack.getMaxDamage()) : 100;
                    int color = percent <= 25 ? 0xFFFF7777 : (percent <= 50 ? 0xFFFFD166 : ACTIVE);
                    graphics.fill(8, armorY + armorIndex * 11, 92, armorY + armorIndex * 11 + 10, PANEL);
                    graphics.drawString(mc.font, shortName(stack) + " " + percent + "%",
                        12, armorY + armorIndex * 11 + 1, color);
                }
                armorIndex++;
            }
        }
        if (ClientConfig.enabled(ClientConfig.Feature.CUSTOM_CROSSHAIR)) {
            int cx = graphics.guiWidth() / 2, cy = graphics.guiHeight() / 2;
            graphics.fill(cx - 5, cy, cx - 2, cy + 1, CYAN);
            graphics.fill(cx + 2, cy, cx + 5, cy + 1, CYAN);
            graphics.fill(cx, cy - 5, cx + 1, cy - 2, CYAN);
            graphics.fill(cx, cy + 2, cx + 1, cy + 5, CYAN);
        }
    }

    private static String shortName(ItemStack stack) {
        String name = stack.getHoverName().getString();
        return name.length() > 10 ? name.substring(0, 10) : name;
    }

    private static void drawKey(GuiGraphics graphics, Minecraft mc, int x, int y,
                                String label, boolean pressed) {
        int width = label.equals("SPACE") ? 54 : 16;
        int color = pressed ? 0xCC174B43 : KEY_BG;
        graphics.fill(x, y, x + width, y + 16, color);
        graphics.drawString(mc.font, label, x + 3, y + 4, pressed ? ACTIVE : WHITE);
    }
}
