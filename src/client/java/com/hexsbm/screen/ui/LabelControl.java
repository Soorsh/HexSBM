package com.hexsbm.screen.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class LabelControl implements ConfigControl {
    private final String text;
    private final int y;
    private final int color;

    public LabelControl(String text, int y) {
        this(text, y, 0xFFFFFF);
    }

    public LabelControl(String text, int y, int color) {
        this.text = text;
        this.y = y;
        this.color = color;
    }

    @Override
    public void render(DrawContext ctx, TextRenderer textRenderer, int mx, int my, int panelX, int scrollY) {
        int yScreen = y - scrollY;
        ctx.drawText(textRenderer, text, panelX + 10, yScreen, color, false);
    }

    @Override
    public boolean mouseClicked(int mx, int my, int panelX, TextRenderer textRenderer, int scrollY) { return false; }
    @Override public boolean mouseScrolled(int mx, int my, double amount, int panelX) { return false; }
    @Override public void finishEditing() {}
    @Override public boolean isEditing() { return false; }
}