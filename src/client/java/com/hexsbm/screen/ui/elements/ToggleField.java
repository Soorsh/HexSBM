package com.hexsbm.screen.ui.elements;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ToggleField implements ConfigControl {
    private final int x, y;
    private final String label;
    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;

    public ToggleField(int x, int y, String label, BooleanSupplier getter, Consumer<Boolean> setter) {
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
        int color = value ? 0x66FF66 : 0xFF6666;
        String text = label + ": " + (value ? "Да" : "Нет");
        ctx.drawText(textRenderer, text, panelX + x, yScreen, color, false);
    }

    @Override
    public boolean mouseClicked(int mx, int my, int panelX, TextRenderer textRenderer, int scrollY) {
        int yScreen = y - scrollY;
        boolean value = getter.getAsBoolean();
        String text = label + ": " + (value ? "Да" : "Нет");
        int sx = panelX + x;
        int textW = textRenderer.getWidth(Text.literal(text));
        if (mx >= sx && mx <= sx + textW && my >= yScreen && my <= yScreen + 12) {
            setter.accept(!value);
            return true;
        }
        return false;
    }

    @Override public boolean mouseScrolled(int mx, int my, double amount, int panelX) { return false; }
    @Override public void finishEditing() {}
    @Override public boolean isEditing() { return false; }
}