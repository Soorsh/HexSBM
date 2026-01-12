package com.hexsbm.screen.ui.elements;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import com.hexsbm.config.HexSBMConfig;

public class NumberField implements ConfigControl {
    public final int x, y;
    public final Text label;
    public final boolean isOffset;
    public final boolean isColor;

    public final java.util.function.IntSupplier getter;
    public final java.util.function.IntConsumer setter;

    private boolean editing = false;
    public final StringBuilder buffer = new StringBuilder();

    public static final int WIDTH = 40;
    private static final int HIGHLIGHT_COLOR = 0xFFFFFFFF;
    private static final int BACKGROUND_COLOR = 0xFF333333;
    private static final int HOVER_BACKGROUND_COLOR = 0xFF555555;
    private static final int FIELD_HEIGHT = 16;
    private static final int BORDER_COLOR_LIGHT = 0xFF666666;
    private static final int BORDER_OFFSET = 1;
    private static final int TEXT_HORIZONTAL_MARGIN = 6;
    private static final int TEXT_X_OFFSET = 3;
    private static final int TEXT_Y_OFFSET = 4;
    private static final int BLINK_RATE_MS = 500;
    private static final int CURSOR_HEIGHT_OFFSET = 13;
    private static final int LABEL_SPACING = 5;
    private static final int LINE_WIDTH = 1;
    private static final int WHITE_COLOR = 0xFFFFFF;

    public NumberField(int x, int y, Text label, java.util.function.IntSupplier getter, java.util.function.IntConsumer setter, boolean isOffset, boolean isColor) {
        this.x = x;
        this.y = y;
        this.label = label;
        this.getter = getter;
        this.setter = setter;
        this.isOffset = isOffset;
        this.isColor = isColor;
    }

    public NumberField(int x, int y, Text label, java.util.function.IntSupplier getter, java.util.function.IntConsumer setter, boolean isOffset) {
        this(x, y, label, getter, setter, isOffset, false);
    }

    public NumberField(int x, int y, String label, java.util.function.IntSupplier getter, java.util.function.IntConsumer setter, boolean isOffset, boolean isColor) {
        this(x, y, Text.literal(label), getter, setter, isOffset, isColor);
    }

    public NumberField(int x, int y, String label, java.util.function.IntSupplier getter, java.util.function.IntConsumer setter, boolean isOffset) {
        this(x, y, Text.literal(label), getter, setter, isOffset, false);
    }

    @Override
    public void render(DrawContext ctx, TextRenderer textRenderer, int mx, int my, int panelX, int scrollY) {
        int yScreen = y - scrollY;
        int sx = panelX + x;
        boolean hovered = mx >= sx && mx < sx + WIDTH && my >= yScreen && my < yScreen + FIELD_HEIGHT;

        int currentBackgroundColor = hovered ? HOVER_BACKGROUND_COLOR : BACKGROUND_COLOR;
        ctx.fill(sx, yScreen, sx + WIDTH, yScreen + FIELD_HEIGHT, currentBackgroundColor);
        
        ctx.fill(sx, yScreen, sx + WIDTH, yScreen + LINE_WIDTH, BORDER_COLOR_LIGHT);
        ctx.fill(sx, yScreen, sx + LINE_WIDTH, yScreen + FIELD_HEIGHT, BORDER_COLOR_LIGHT);

        if (editing) {
            ctx.fill(sx - BORDER_OFFSET, yScreen - BORDER_OFFSET, sx + WIDTH + BORDER_OFFSET, yScreen, HIGHLIGHT_COLOR);
            ctx.fill(sx - BORDER_OFFSET, yScreen + FIELD_HEIGHT, sx + WIDTH + BORDER_OFFSET, yScreen + FIELD_HEIGHT + BORDER_OFFSET, HIGHLIGHT_COLOR);
            ctx.fill(sx - BORDER_OFFSET, yScreen, sx, yScreen + FIELD_HEIGHT, HIGHLIGHT_COLOR);
            ctx.fill(sx + WIDTH, yScreen, sx + WIDTH + BORDER_OFFSET, yScreen + FIELD_HEIGHT, HIGHLIGHT_COLOR);
        }

        String display = editing ? buffer.toString() : String.valueOf(getter.getAsInt());
        int availableWidth = WIDTH - TEXT_HORIZONTAL_MARGIN; // 3px padding on each side

        String truncatedDisplay;
        if (editing) {
            truncatedDisplay = display;
            while (textRenderer.getWidth(truncatedDisplay) > availableWidth && truncatedDisplay.length() > 0) {
                truncatedDisplay = truncatedDisplay.substring(1);
            }
        } else {
            truncatedDisplay = textRenderer.trimToWidth(display, availableWidth);
        }

        ctx.drawText(textRenderer, truncatedDisplay, sx + TEXT_X_OFFSET, yScreen + TEXT_Y_OFFSET, WHITE_COLOR, false);

        if (editing) {
            long blink = System.currentTimeMillis() / BLINK_RATE_MS;
            if (blink % 2 == 0) {
                int w = textRenderer.getWidth(Text.literal(truncatedDisplay));
                ctx.fill(sx + TEXT_X_OFFSET + w + BORDER_OFFSET, yScreen + TEXT_X_OFFSET, sx + TEXT_X_OFFSET + w + BORDER_OFFSET + BORDER_OFFSET, yScreen + CURSOR_HEIGHT_OFFSET, HIGHLIGHT_COLOR);
            }
        }

        Text fullLabel = Text.empty().append(label).append(":");
        int lw = textRenderer.getWidth(fullLabel);
        ctx.drawText(textRenderer, fullLabel, sx - lw - LABEL_SPACING, yScreen + TEXT_Y_OFFSET, WHITE_COLOR, false);
    }

    @Override
    public boolean mouseClicked(int mx, int my, int panelX, TextRenderer textRenderer, int scrollY) {
        int yScreen = y - scrollY;
        int sx = panelX + x;
        if (mx >= sx && mx < sx + WIDTH && my >= yScreen && my < yScreen + 16) {
            editing = true;
            buffer.setLength(0);
            buffer.append(getter.getAsInt());
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode) {
        if (!editing) return false;

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            apply();
            editing = false;
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && buffer.length() > 0) {
            buffer.setLength(buffer.length() - 1);
            return true;
        }

        if ((keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) ||
            (keyCode >= GLFW.GLFW_KEY_KP_0 && keyCode <= GLFW.GLFW_KEY_KP_9)) {
            char c = (char) ('0' + (keyCode <= GLFW.GLFW_KEY_9 ? keyCode - GLFW.GLFW_KEY_0 : keyCode - GLFW.GLFW_KEY_KP_0));
            buffer.append(c);
            return true;
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(int mx, int my, double amount, int panelX) {
        // Метод вызывается только когда поле под мышкой
        int step = amount > 0 ? 1 : -1;
        int newValue = getter.getAsInt() + step;

        newValue = clampValue(newValue);

        setter.accept(newValue);
        if (editing) {
            buffer.setLength(0);
            buffer.append(newValue);
        }
        return true;
    }

    public void apply() {
        if (buffer.length() == 0) return;
        try {
            int v = Integer.parseInt(buffer.toString());
            v = clampValue(v);
            setter.accept(v);
        } catch (NumberFormatException ignored) {}
    }

    private int clampValue(int value) {
        if (isColor) {
            return MathHelper.clamp(value, 0, 255);
        } else if (isOffset) {
            return MathHelper.clamp(value, -HexSBMConfig.MAX_OFFSET, HexSBMConfig.MAX_OFFSET);
        } else {
            return MathHelper.clamp(value, 0, HexSBMConfig.MAX_RADIUS);
        }
    }

    public boolean isMouseOver(int mx, int my, int panelX, int scrollY) {
        int yScreen = this.y - scrollY;
        int sx = panelX + x;
        return mx >= sx && mx < sx + WIDTH && my >= yScreen && my < yScreen + FIELD_HEIGHT;
    }

    public boolean isEditing() { return editing; }

    public void finishEditing() {
        if (editing) {
            apply();
            editing = false;
        }
    }
}