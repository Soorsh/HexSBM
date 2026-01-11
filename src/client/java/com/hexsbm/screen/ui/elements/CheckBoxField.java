package com.hexsbm.screen.ui.elements;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class CheckBoxField implements ConfigControl {
    private final int x, y;
    private final String label;
    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;

    private static final int BOX_SIZE = 10;
    private static final int BOX_Y_OFFSET = 1;

    private static final int BACKGROUND_COLOR = 0xFF333333; // Темно-серый для фона
    private static final int HOVER_BACKGROUND_COLOR = 0xFF555555;  // Светло-серый при наведении
    private static final int BORDER_COLOR = 0xFF666666;    // Цвет рамки
    private static final int CHECKMARK_COLOR = 0xFFFFFFFF; // Белый цвет для галочки

    public CheckBoxField(int x, int y, String label, BooleanSupplier getter, Consumer<Boolean> setter) {
        this.x = x;
        this.y = y;
        this.label = label;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public void render(DrawContext ctx, TextRenderer textRenderer, int mx, int my, int panelX, int scrollY) {
        int yScreen = y - scrollY;
        boolean value = getter.getAsBoolean();
        int sx = panelX + x;

        // Draw label
        ctx.drawText(textRenderer, label + ":", sx, yScreen + BOX_Y_OFFSET + (BOX_SIZE - textRenderer.fontHeight) / 2, 0xFFFFFF, false);

        int checkboxX = sx + textRenderer.getWidth(Text.literal(label + ": ")) + 5; // Position the checkbox after the label
        int checkboxY = yScreen + BOX_Y_OFFSET;

        boolean isHovered = mx >= checkboxX && mx <= checkboxX + BOX_SIZE && my >= checkboxY && my <= checkboxY + BOX_SIZE;

        // Draw box
        int bgColor = isHovered ? HOVER_BACKGROUND_COLOR : BACKGROUND_COLOR;
        ctx.fill(checkboxX, checkboxY, checkboxX + BOX_SIZE, checkboxY + BOX_SIZE, bgColor);
        ctx.drawBorder(checkboxX, checkboxY, BOX_SIZE, BOX_SIZE, BORDER_COLOR);

        // Draw checkmark if toggled on
        if (value) {
            // Draw a filled square inside the box
            ctx.fill(checkboxX + 2, checkboxY + 2, checkboxX + BOX_SIZE - 2, checkboxY + BOX_SIZE - 2, CHECKMARK_COLOR);
        }
    }

    @Override
    public boolean mouseClicked(int mx, int my, int panelX, TextRenderer textRenderer, int scrollY) {
        int yScreen = y - scrollY;
        int sx = panelX + x;

        int checkboxX = sx + textRenderer.getWidth(Text.literal(label + ": ")) + 5; // Position the checkbox after the label
        int checkboxY = yScreen + BOX_Y_OFFSET;

        if (mx >= checkboxX && mx <= checkboxX + BOX_SIZE && my >= checkboxY && my <= checkboxY + BOX_SIZE) {
            setter.accept(!getter.getAsBoolean());
            return true;
        }
        return false;
    }

    @Override public boolean mouseScrolled(int mx, int my, double amount, int panelX) { return false; }
    @Override public void finishEditing() {}
    @Override public boolean isEditing() { return false; }
}