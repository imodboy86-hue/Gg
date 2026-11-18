package com.helperclient;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Original Helper Client interface with its own branding and module browser. */
public final class HelperClientScreen extends Screen {
    private static final int BG = 0xE9080D15;
    private static final int BAR = 0xFF111820;
    private static final int CARD = 0xFF1B222B;
    private static final int CARD_EDGE = 0xFF303B48;
    private static final int CYAN = 0xFF55D6FF;
    private static final int MUTED = 0xFF9BAAC0;

    private enum Page { MODS, SETTINGS, HUD }
    private final Page page;
    private final int modulePage;

    public HelperClientScreen() { this(Page.MODS, 0); }
    private HelperClientScreen(Page page, int modulePage) {
        super(Component.literal("Helper Client"));
        this.page = page;
        this.modulePage = modulePage;
    }

    @Override protected void init() {
        int left = Math.max(8, width / 2 - 235);
        addNav(left + 8, 12, "MODS", Page.MODS);
        addNav(left + 92, 12, "SETTINGS", Page.SETTINGS);
        addNav(left + 190, 12, "HUD EDITOR", Page.HUD);
        int top = Math.max(75, height / 2 - 80);
        if (page == Page.MODS) {
            ClientConfig.Feature[] fs = ClientConfig.Feature.values();
            int start = modulePage * 6;
            int cardW = Math.min(145, Math.max(105, (width - 48) / 3));
            int gap = 8, totalW = cardW * 3 + gap * 2, x0 = (width - totalW) / 2;
            for (int i = 0; i < 6 && start + i < fs.length; i++) {
                ClientConfig.Feature f = fs[start + i];
                int x = x0 + (i % 3) * (cardW + gap), y = top + (i / 3) * 82;
                Button toggle = Button.builder(Component.literal(ClientConfig.enabled(f) ? "ENABLED" : "DISABLED"), b -> {
                    boolean next = !ClientConfig.enabled(f);
                    ClientConfig.setEnabled(f, next);
                    b.setMessage(Component.literal(next ? "ENABLED" : "DISABLED"));
                }).bounds(x + 4, y + 54, cardW - 8, 20).build();
                addRenderableWidget(toggle);
            }
            int navY = top + 174;
            addButton(width / 2 - 100, navY, 92, "PREVIOUS", () -> open(Page.MODS, Math.max(0, modulePage - 1)));
            addButton(width / 2 + 8, navY, 92, "NEXT", () -> open(Page.MODS, Math.min((fs.length - 1) / 6, modulePage + 1)));
        } else if (page == Page.HUD) {
            int cx = width / 2;
            addButton(cx - 105, top, 210, "HUD SCALE  -", () -> { ClientConfig.scale(ClientConfig.scale() - .1f); open(Page.HUD, 0); });
            addButton(cx - 105, top + 27, 210, "HUD SCALE  +", () -> { ClientConfig.scale(ClientConfig.scale() + .1f); open(Page.HUD, 0); });
            addButton(cx - 105, top + 54, 210, "HUD OPACITY  -", () -> { ClientConfig.opacity(ClientConfig.opacity() - .1f); open(Page.HUD, 0); });
            addButton(cx - 105, top + 81, 210, "HUD OPACITY  +", () -> { ClientConfig.opacity(ClientConfig.opacity() + .1f); open(Page.HUD, 0); });
        } else {
            int cx = width / 2;
            addButton(cx - 105, top, 210, "PERFORMANCE MODE: " + (ClientConfig.enabled(ClientConfig.Feature.OPTIMIZATION) ? "ON" : "OFF"), () -> {
                ClientConfig.setEnabled(ClientConfig.Feature.OPTIMIZATION, !ClientConfig.enabled(ClientConfig.Feature.OPTIMIZATION)); open(Page.SETTINGS, 0);
            });
            addButton(cx - 105, top + 27, 210, "RESET INTERFACE", () -> { ClientConfig.scale(1f); ClientConfig.opacity(.82f); open(Page.SETTINGS, 0); });
        }
    }

    private void addNav(int x, int y, String text, Page target) {
        addRenderableWidget(Button.builder(Component.literal(text), b -> open(target, 0))
            .bounds(x, y, text.equals("SETTINGS") ? 86 : 76, 20).build());
    }
    private void addButton(int x, int y, int w, String text, Runnable action) {
        addRenderableWidget(Button.builder(Component.literal(text), b -> action.run()).bounds(x, y, w, 20).build());
    }
    private void open(Page target, int number) { minecraft.setScreen(new HelperClientScreen(target, number)); }

    @Override public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, BG);
        g.fill(0, 0, width, 44, BAR);
        g.fill(0, 43, width, 45, CYAN);
        g.drawString(font, "◆ HELPER CLIENT", 12, 18, CYAN);
        g.drawString(font, "1.21.11  •  FABRIC", 14, 32, MUTED);
        String heading = switch (page) {
            case MODS -> "MODULES  •  PAGE " + (modulePage + 1);
            case SETTINGS -> "SETTINGS";
            case HUD -> "HUD EDITOR";
        };
        g.drawCenteredString(font, heading, width / 2, 52, 0xFFFFFFFF);
        if (page == Page.MODS) {
            ClientConfig.Feature[] fs = ClientConfig.Feature.values();
            int start = modulePage * 6;
            int cardW = Math.min(145, Math.max(105, (width - 48) / 3));
            int gap = 8, totalW = cardW * 3 + gap * 2, x0 = (width - totalW) / 2;
            int top = Math.max(75, height / 2 - 80);
            for (int i = 0; i < 6 && start + i < fs.length; i++) {
                ClientConfig.Feature f = fs[start + i];
                int x = x0 + (i % 3) * (cardW + gap), y = top + (i / 3) * 82;
                boolean on = ClientConfig.enabled(f);
                g.fill(x, y, x + cardW, y + 78, CARD);
                g.fill(x, y, x + cardW, y + 1, CARD_EDGE);
                g.drawCenteredString(font, f.label, x + cardW / 2, y + 15, 0xFFFFFFFF);
                g.drawCenteredString(font, on ? "● ACTIVE" : "○ INACTIVE", x + cardW / 2, y + 37, on ? 0xFF67E6A5 : MUTED);
            }
        } else if (page == Page.HUD) {
            g.drawCenteredString(font, "SCALE: " + String.format("%.1f", ClientConfig.scale()), width / 2, Math.max(55, height / 2 - 105), 0xFFFFFFFF);
            g.drawCenteredString(font, "OPACITY: " + (int)(ClientConfig.opacity() * 100) + "%", width / 2, Math.max(55, height / 2 + 18), MUTED);
        } else {
            g.drawCenteredString(font, "Performance and interface preferences", width / 2, Math.max(55, height / 2 - 105), MUTED);
        }
        super.render(g, mx, my, pt);
        g.drawCenteredString(font, "HELPER CLIENT  •  RIGHT SHIFT TO OPEN", width / 2, height - 14, MUTED);
    }
    @Override public boolean isPauseScreen() { return false; }
}
