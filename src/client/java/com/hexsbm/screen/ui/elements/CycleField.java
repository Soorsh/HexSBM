// com/hexsbm/screen/ui/CycleField.java
package com.hexsbm.screen.ui.elements;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.IntConsumer;

public class CycleField implements ConfigControl {
    private final int x, y;
    private final String label;
    private final List<String> options;
    private final IntSupplier getter;
    private final IntConsumer setter;

    private static final int FIELD_WIDTH = 80; // Ширина поля
    private static final int FIELD_HEIGHT = 16; // Высота поля
    private static final int BACKGROUND_COLOR = 0xFF333333; // Цвет фона
    private static final int HOVER_BACKGROUND_COLOR = 0xFF555555; // Цвет фона при наведении
    private static final int BORDER_COLOR = 0xFF666666;    // Цвет границы

    public CycleField(int x, int y, String label, List<String> options, IntSupplier getter, IntConsumer setter) {
        this.x = x;
        this.y = y;
        this.label = label;
        this.options = options;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void render(DrawContext ctx, TextRenderer textRenderer, int mx, int my, int panelX, int scrollY) {
        int yScreen = y - scrollY;
        int value = getter.getAsInt();
        if (value < 0 || value >= options.size()) value = 0;
        String currentOptionText = options.get(value);
        int sx = panelX + x;

        // Draw label
        ctx.drawText(textRenderer, label + ":", sx, yScreen + (FIELD_HEIGHT - textRenderer.fontHeight) / 2, 0xFFFFFF, false);

        int fieldX = sx + textRenderer.getWidth(Text.literal(label + ": ")) + 5; // Position the field after the label

        boolean isHovered = mx >= fieldX && mx <= fieldX + FIELD_WIDTH && my >= yScreen && my <= yScreen + FIELD_HEIGHT;

        // Draw background box for the current option
        int currentBackgroundColor = isHovered ? HOVER_BACKGROUND_COLOR : BACKGROUND_COLOR;
        ctx.fill(fieldX, yScreen, fieldX + FIELD_WIDTH, yScreen + FIELD_HEIGHT, currentBackgroundColor);
        // Draw border for the background box
        ctx.drawBorder(fieldX, yScreen, FIELD_WIDTH, FIELD_HEIGHT, BORDER_COLOR);

        // Display current option text centered within the box
        int textX = fieldX + (FIELD_WIDTH - textRenderer.getWidth(Text.literal(currentOptionText))) / 2;
        int textY = yScreen + (FIELD_HEIGHT - textRenderer.fontHeight) / 2;
        ctx.drawText(textRenderer, currentOptionText, textX, textY, 0xFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(int mx, int my, int panelX, TextRenderer textRenderer, int scrollY) {
        int yScreen = y - scrollY;
        int value = getter.getAsInt();
        if (value < 0 || value >= options.size()) value = 0;
        String currentText = options.get(value);
        int sx = panelX + x;
        
        int fieldX = sx + textRenderer.getWidth(Text.literal(label + ": ")) + 5; // Position the field after the label

        // Check if click is within the new visual box
        if (mx >= fieldX && mx <= fieldX + FIELD_WIDTH && my >= yScreen && my <= yScreen + FIELD_HEIGHT) {
            int next = (value + 1) % options.size();
            setter.accept(next);
            return true;
        }
        return false;
    }

    @Override public boolean mouseScrolled(int mx, int my, double amount, int panelX) { return false; }
    @Override public void finishEditing() {}
    @Override public boolean isEditing() { return false; }
}