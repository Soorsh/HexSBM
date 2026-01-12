package com.hexsbm.screen.ui.elements;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class LabelControl implements ConfigControl {
    private final Text text;
    private final int y;
    private final int color;

    public LabelControl(Text text, int y) {
        this(text, y, 0xFFFFFF);
    }

    public LabelControl(Text text, int y, int color) {
        this.text = text;
        this.y = y;
        this.color = color;
    }

    public LabelControl(String text, int y) {
        this(Text.literal(text), y, 0xFFFFFF);
    }

    public LabelControl(String text, int y, int color) {
        this(Text.literal(text), y, color);
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