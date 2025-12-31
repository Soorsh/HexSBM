package com.hexsbm.screen.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public interface ConfigControl {
    void render(DrawContext ctx, TextRenderer textRenderer, int mx, int my, int panelX);
    boolean mouseClicked(int mx, int my, int panelX, TextRenderer textRenderer); // ← добавлен textRenderer
    boolean mouseScrolled(int mx, int my, double amount, int panelX);
    void finishEditing();
    boolean isEditing();
}