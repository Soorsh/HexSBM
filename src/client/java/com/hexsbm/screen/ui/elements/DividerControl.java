package com.hexsbm.screen.ui.elements;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class DividerControl implements ConfigControl {
    private final int y;
    private final int height;

    public DividerControl(int y, int height) {
        this.y = y;
        this.height = height;
    }

    @Override
    public void render(DrawContext ctx, TextRenderer textRenderer, int mx, int my, int panelX, int scrollY) {
        int yScreen = y - scrollY;
        // Draw a horizontal line across the panel
        ctx.fill(panelX + 5, yScreen + height / 2, panelX + 215, yScreen + height / 2 + 1, 0xFF888888);
    }

    @Override
    public boolean mouseClicked(int mx, int my, int panelX, TextRenderer textRenderer, int scrollY) {
        return false;
    }

    @Override public boolean mouseScrolled(int mx, int my, double amount, int panelX) { return false; }
    @Override public boolean isEditing() { return false; }
    @Override public void finishEditing() {}
}
