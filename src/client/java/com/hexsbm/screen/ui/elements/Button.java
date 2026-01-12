package com.hexsbm.screen.ui.elements;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class Button implements ConfigControl {
    private final int x, y;
    private final Text label;
    private final int color;
    private final Runnable action;

    private boolean isPressed = false;

    private static final int BUTTON_WIDTH = 90;
    private static final int BUTTON_HEIGHT = 16;
    private static final int BACKGROUND_COLOR = 0xFF333333;
    private static final int HOVER_BACKGROUND_COLOR = 0xFF555555;
    private static final int PRESSED_BACKGROUND_COLOR = 0xFF111111;
    private static final int BORDER_COLOR = 0xFF666666;

    public Button(int x, int y, Text label, int color, Runnable action) {
        this.x = x;
        this.y = y;
        this.label = label;
        this.color = color;
        this.action = action;
    }

    @Override
    public void render(DrawContext ctx, TextRenderer textRenderer, int mx, int my, int panelX, int scrollY) {
        int yScreen = y - scrollY;
        int sx = panelX + x;

        boolean isHovered = mx >= sx && mx <= sx + BUTTON_WIDTH && my >= yScreen && my <= yScreen + BUTTON_HEIGHT;

        int currentBackgroundColor = BACKGROUND_COLOR;
        if (isPressed) {
            currentBackgroundColor = PRESSED_BACKGROUND_COLOR;
            isPressed = false; // Reset after one frame
        } else if (isHovered) {
            currentBackgroundColor = HOVER_BACKGROUND_COLOR;
        }

        // Draw background
        ctx.fill(sx, yScreen, sx + BUTTON_WIDTH, yScreen + BUTTON_HEIGHT, currentBackgroundColor);
        // Draw border
        ctx.drawBorder(sx, yScreen, BUTTON_WIDTH, BUTTON_HEIGHT, BORDER_COLOR);

        // Draw text centered
        int textX = sx + (BUTTON_WIDTH - textRenderer.getWidth(label)) / 2;
        int textY = yScreen + (BUTTON_HEIGHT - textRenderer.fontHeight) / 2 + 1;
        ctx.drawText(textRenderer, label, textX, textY, color, false);
    }

    @Override
    public boolean mouseClicked(int mx, int my, int panelX, TextRenderer textRenderer, int scrollY) {
        int yScreen = y - scrollY;
        int sx = panelX + x;
        if (mx >= sx && mx <= sx + BUTTON_WIDTH && my >= yScreen && my <= yScreen + BUTTON_HEIGHT) {
            isPressed = true;
            action.run();
            return true;
        }
        return false;
    }

    @Override public boolean mouseScrolled(int mx, int my, double amount, int panelX) { return false; }
    @Override public void finishEditing() {}
    @Override public boolean isEditing() { return false; }
}