package com.hexsbm.screen.ui.elements;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import com.hexsbm.config.HexSBMConfig;

public class NumberField implements ConfigControl {
    public final int x, y;
    public final String label;
    public final boolean isOffset;
    public final boolean isColor;

    public final java.util.function.IntSupplier getter;
    public final java.util.function.IntConsumer setter;

    private boolean editing = false;
    public final StringBuilder buffer = new StringBuilder();

    public static final int WIDTH = 40; // Новая ширина поля ввода
    private static final int HIGHLIGHT_COLOR = 0xFFFFFFFF; // Цвет подсветки при редактировании
    private static final int BACKGROUND_COLOR = 0xFF333333;
    private static final int HOVER_BACKGROUND_COLOR = 0xFF555555;

    public NumberField(int x, int y, String label, java.util.function.IntSupplier getter, java.util.function.IntConsumer setter, boolean isOffset, boolean isColor) {
        this.x = x;
        this.y = y;
        this.label = label;
        this.getter = getter;
        this.setter = setter;
        this.isOffset = isOffset;
        this.isColor = isColor;
    }

    public NumberField(int x, int y, String label, java.util.function.IntSupplier getter, java.util.function.IntConsumer setter, boolean isOffset) {
        this(x, y, label, getter, setter, isOffset, false);
    }

    @Override
    public void render(DrawContext ctx, TextRenderer textRenderer, int mx, int my, int panelX, int scrollY) {
        int yScreen = y - scrollY;
        int sx = panelX + x;
        boolean hovered = mx >= sx && mx < sx + WIDTH && my >= yScreen && my < yScreen + 16;

        int currentBackgroundColor = hovered ? HOVER_BACKGROUND_COLOR : BACKGROUND_COLOR;
        ctx.fill(sx, yScreen, sx + WIDTH, yScreen + 16, currentBackgroundColor);
        
        ctx.fill(sx, yScreen, sx + WIDTH, yScreen + 1, 0xFF666666);
        ctx.fill(sx, yScreen, sx + 1, yScreen + 16, 0xFF666666);

        if (editing) {
            ctx.fill(sx - 1, yScreen - 1, sx + WIDTH + 1, yScreen, HIGHLIGHT_COLOR); // Top border
            ctx.fill(sx - 1, yScreen + 16, sx + WIDTH + 1, yScreen + 17, HIGHLIGHT_COLOR); // Bottom border
            ctx.fill(sx - 1, yScreen, sx, yScreen + 16, HIGHLIGHT_COLOR); // Left border
            ctx.fill(sx + WIDTH, yScreen, sx + WIDTH + 1, yScreen + 16, HIGHLIGHT_COLOR); // Right border
        }

        String display = editing ? buffer.toString() : String.valueOf(getter.getAsInt());
        int availableWidth = WIDTH - 6; // 3px padding on each side

        String truncatedDisplay;
        if (editing) {
            truncatedDisplay = display;
            while (textRenderer.getWidth(truncatedDisplay) > availableWidth && truncatedDisplay.length() > 0) {
                truncatedDisplay = truncatedDisplay.substring(1);
            }
        } else {
            truncatedDisplay = textRenderer.trimToWidth(display, availableWidth);
        }

        ctx.drawText(textRenderer, truncatedDisplay, sx + 3, yScreen + 4, 0xFFFFFF, false);

        if (editing) {
            long blink = System.currentTimeMillis() / 500;
            if (blink % 2 == 0) {
                int w = textRenderer.getWidth(Text.literal(truncatedDisplay));
                ctx.fill(sx + 3 + w + 1, yScreen + 3, sx + 3 + w + 2, yScreen + 13, 0xFFFFFFFF);
            }
        }

        int lw = textRenderer.getWidth(Text.literal(label + ":"));
        ctx.drawText(textRenderer, label + ":", sx - lw - 5, yScreen + 4, 0xFFFFFF, false);
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
        // Этот метод вызывается ТОЛЬКО если isMouseOver уже вернул true,
        // но на всякий случай проверим ещё раз с учётом scrollY
        // → но scrollY недоступен здесь, поэтому...
        // Лучше: не использовать этот метод напрямую, а вызывать через ConfigPanel
        // Но для простоты — сейчас он вызывается только когда поле под мышкой
        // → значит, просто примени скролл

        int step = amount > 0 ? 1 : -1;
        int newValue = getter.getAsInt() + step;

        if (isColor) {
            newValue = MathHelper.clamp(newValue, 0, 255);
        } else if (isOffset) {
            newValue = MathHelper.clamp(newValue, -HexSBMConfig.MAX_OFFSET, HexSBMConfig.MAX_OFFSET);
        } else {
            newValue = MathHelper.clamp(newValue, 0, HexSBMConfig.MAX_RADIUS);
        }

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
            if (isColor) {
                v = MathHelper.clamp(v, 0, 255);
            } else if (isOffset) {
                v = MathHelper.clamp(v, -com.hexsbm.config.HexSBMConfig.MAX_OFFSET, com.hexsbm.config.HexSBMConfig.MAX_OFFSET);
            } else {
                v = MathHelper.clamp(v, 0, com.hexsbm.config.HexSBMConfig.MAX_RADIUS);
            }
            setter.accept(v);
        } catch (NumberFormatException ignored) {}
    }

    public boolean isMouseOver(int mx, int my, int panelX, int scrollY) {
        int yScreen = this.y - scrollY;
        int sx = panelX + x;
        return mx >= sx && mx < sx + WIDTH && my >= yScreen && my < yScreen + 16;
    }

    public boolean isEditing() { return editing; }

    public void finishEditing() {
        if (editing) {
            apply();
            editing = false;
        }
    }
}