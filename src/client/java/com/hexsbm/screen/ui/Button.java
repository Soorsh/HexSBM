package com.hexsbm.screen.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class Button implements ConfigControl {
    private final int x, y;
    private final String label;
    private final int color;
    private final Runnable action;

    public Button(int x, int y, String label, int color, Runnable action) {
        this.x = x;
        this.y = y;
        this.label = label;
        this.color = color;
        this.action = action;
    }

    @Override
    public void render(DrawContext ctx, TextRenderer textRenderer, int mx, int my, int panelX, int scrollY) {
        int yScreen = y - scrollY;
        ctx.drawText(textRenderer, label, panelX + x, yScreen, color, false);
    }

    @Override
    public boolean mouseClicked(int mx, int my, int panelX, TextRenderer textRenderer, int scrollY) {
        int yScreen = y - scrollY;
        int sx = panelX + x;
        int textW = textRenderer.getWidth(Text.literal(label));
        if (mx >= sx && mx <= sx + textW && my >= yScreen && my <= yScreen + 12) {
            action.run();
            return true;
        }
        return false;
    }

    @Override public boolean mouseScrolled(int mx, int my, double amount, int panelX) { return false; }
    @Override public void finishEditing() {}
    @Override public boolean isEditing() { return false; }
}