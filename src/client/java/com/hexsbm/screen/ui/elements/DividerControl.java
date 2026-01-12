package com.hexsbm.screen.ui.elements;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class DividerControl implements ConfigControl {
    private final int y;
    private final int height;

    private static final int DIVIDER_START_X = 5;
    private static final int DIVIDER_END_X = 215;
    private static final int DIVIDER_COLOR = 0xFF888888;

    public DividerControl(int y, int height) {
        this.y = y;
        this.height = height;
    }

    @Override
    public void render(DrawContext ctx, TextRenderer textRenderer, int mx, int my, int panelX, int scrollY) {
        int yScreen = y - scrollY;
        ctx.fill(panelX + DIVIDER_START_X, yScreen + height / 2, panelX + DIVIDER_END_X, yScreen + height / 2 + 1, DIVIDER_COLOR);
    }

    @Override
    public boolean mouseClicked(int mx, int my, int panelX, TextRenderer textRenderer, int scrollY) {
        return false;
    }

    @Override public boolean mouseScrolled(int mx, int my, double amount, int panelX) { return false; }
    @Override public boolean isEditing() { return false; }
    @Override public void finishEditing() {}
}
