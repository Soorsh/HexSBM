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
        String text = label + ": " + options.get(value);
        ctx.drawText(textRenderer, text, panelX + x, yScreen, 0xFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(int mx, int my, int panelX, TextRenderer textRenderer, int scrollY) {
        int yScreen = y - scrollY;
        int value = getter.getAsInt();
        if (value < 0 || value >= options.size()) value = 0;
        String currentText = label + ": " + options.get(value);
        int sx = panelX + x;
        int textW = textRenderer.getWidth(Text.literal(currentText));
        if (mx >= sx && mx <= sx + textW && my >= yScreen && my <= yScreen + 12) {
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