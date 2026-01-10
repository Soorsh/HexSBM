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

    private static final int TRACK_WIDTH = 24;
    private static final int TRACK_HEIGHT = 12;
    private static final int KNOB_WIDTH = 10;
    private static final int KNOB_HEIGHT = 10;

    private static final int TRACK_COLOR_OFF = 0xFF333333; // Темно-серый для выключенного состояния
    private static final int TRACK_COLOR_ON = 0xFF555555;  // Светло-серый для включенного состояния
    private static final int TRACK_COLOR_HOVER = 0xFF777777; // Цвет фона при наведении
    private static final int KNOB_COLOR_ON = 0xFFFFFFFF;    // Белый для ручки (включено)
    private static final int KNOB_COLOR_OFF = 0xFFBBBBBB;   // Светло-серый для ручки (выключено)

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
        int sx = panelX + x;

        // Draw label
        ctx.drawText(textRenderer, label + ":", sx, yScreen + (TRACK_HEIGHT - textRenderer.fontHeight) / 2, 0xFFFFFF, false);

        int switchX = sx + textRenderer.getWidth(Text.literal(label + ": ")) + 5; // Position the switch after the label

        boolean isHovered = mx >= switchX && mx <= switchX + TRACK_WIDTH && my >= yScreen && my <= yScreen + TRACK_HEIGHT;

        // Draw track
        int trackColor;
        if (isHovered) {
            trackColor = TRACK_COLOR_HOVER;
        } else {
            trackColor = value ? TRACK_COLOR_ON : TRACK_COLOR_OFF;
        }
        ctx.fill(switchX, yScreen, switchX + TRACK_WIDTH, yScreen + TRACK_HEIGHT, trackColor);
        ctx.drawBorder(switchX, yScreen, TRACK_WIDTH, TRACK_HEIGHT, 0xFF666666); // Gray border for the track

        // Calculate knob position
        int knobX = switchX + (value ? TRACK_WIDTH - KNOB_WIDTH - 1 : 1);
        int knobY = yScreen + (TRACK_HEIGHT - KNOB_HEIGHT) / 2;

        // Draw knob
        int knobColor = value ? KNOB_COLOR_ON : KNOB_COLOR_OFF;
        ctx.fill(knobX, knobY, knobX + KNOB_WIDTH, knobY + KNOB_HEIGHT, knobColor);
    }

    @Override
    public boolean mouseClicked(int mx, int my, int panelX, TextRenderer textRenderer, int scrollY) {
        int yScreen = y - scrollY;
        int sx = panelX + x;

        int switchX = sx + textRenderer.getWidth(Text.literal(label + ": ")) + 5; // Position the switch after the label

        if (mx >= switchX && mx <= switchX + TRACK_WIDTH && my >= yScreen && my <= yScreen + TRACK_HEIGHT) {
            setter.accept(!getter.getAsBoolean());
            return true;
        }
        return false;
    }

    @Override public boolean mouseScrolled(int mx, int my, double amount, int panelX) { return false; }
    @Override public void finishEditing() {}
    @Override public boolean isEditing() { return false; }
}