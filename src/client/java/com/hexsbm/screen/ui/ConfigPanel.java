package com.hexsbm.screen.ui;

import com.hexsbm.config.ConfigManager;
import com.hexsbm.config.HexSBMConfig;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import java.util.List;

public class ConfigPanel {
    private static final int FIELD_WIDTH = 80, FIELD_HEIGHT = 16;

    private boolean editing = false;
    private String editingField = null;
    private final StringBuilder editingValue = new StringBuilder();

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        if (!editing) {
            editingField = null;
        }
        this.editing = editing;
    }

    public void render(DrawContext ctx, int px, HexSBMConfig config, TextRenderer textRenderer, int mx, int my) {
        ctx.drawText(textRenderer, "Цвет UI:", px + 10, 270, 0xFFFFFF, false);
        String colorHex = String.format("%08X", config.uiBaseColor);
        ctx.drawText(textRenderer, colorHex, px + 100, 270, 0xFFAAAA, false);
        ctx.drawText(textRenderer, "Авто: " + (config.usePigmentColor ? "Да" : "Нет"), px + 10, 285, config.usePigmentColor ? 0x66FF66 : 0xFF6666, false);

        ctx.fill(px, 0, px + 220, ctx.getScaledWindowHeight(), 0x88000000);
        ctx.drawText(textRenderer, "Настройки UI", px + 10, 5, 0xFFFFFF, false);
        ctx.drawText(textRenderer, "Сбросить всё", px + 10, 22, 0xFF6666, false);
        ctx.drawText(textRenderer, "Сбросить до моего", px + 10, 52, 0x66FF66, false);

        drawNumberField(ctx, px + 100, 85, config.outerRingOuterRadius, mx, my, "Внешний радиус", "outer", textRenderer);
        drawNumberField(ctx, px + 100, 115, config.innerRingInnerRadius, mx, my, "Внутр. радиус", "inner", textRenderer);
        drawNumberField(ctx, px + 100, 145, config.outerRingInnerRadius, mx, my, "Начало внеш.", "outerInner", textRenderer);
        drawNumberField(ctx, px + 100, 175, config.innerRingOuterRadius, mx, my, "Конец внутр.", "innerOuter", textRenderer);
        drawNumberField(ctx, px + 100, 205, config.innerIconRadiusOffset, mx, my, "Смещение внутр.", "innerIconOffset", textRenderer);
        drawNumberField(ctx, px + 100, 235, config.outerIconRadiusOffset, mx, my, "Смещение внеш.", "outerIconOffset", textRenderer);
    }

    public boolean mouseClicked(int mx, int my, int px, HexSBMConfig config) {
        if (mx <= px) return false;

        setEditing(true);
        applyEditingValue(config);

        if (isFieldHovered(mx, my, px + 100, 145)) {
            startEditing("outerInner", config.outerRingInnerRadius);
            return true;
        }
        if (isFieldHovered(mx, my, px + 100, 175)) {
            startEditing("innerOuter", config.innerRingOuterRadius);
            return true;
        }
        if (isFieldHovered(mx, my, px + 100, 205)) {
            startEditing("innerIconOffset", config.innerIconRadiusOffset);
            return true;
        }
        if (isFieldHovered(mx, my, px + 100, 235)) {
            startEditing("outerIconOffset", config.outerIconRadiusOffset);
            return true;
        }

        if (my >= 20 && my <= 40) {
            // Сбросить всё
            resetToDefaults(config);
            return true;
        }
        if (my >= 50 && my <= 70) {
            // Сбросить до сохранённого
            reloadFromDisk(config);
            return true;
        }

        if (my >= 285 && my <= 300) {
            config.usePigmentColor = !config.usePigmentColor;
            return true;
        }

        editingField = null;
        return true;
    }

    public boolean keyPressed(int keyCode, int scanCode, int mods, HexSBMConfig config) {
        if (editingField == null) return false;

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && editingValue.length() > 0) {
            editingValue.setLength(editingValue.length() - 1);
            return true;
        }
        if ((keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) ||
            (keyCode >= GLFW.GLFW_KEY_KP_0 && keyCode <= GLFW.GLFW_KEY_KP_9)) {
            char c = (char) ('0' + (keyCode <= GLFW.GLFW_KEY_9 ? keyCode - GLFW.GLFW_KEY_0 : keyCode - GLFW.GLFW_KEY_KP_0));
            editingValue.append(c);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            applyEditingValue(config);
            editingField = null;
            return true;
        }
        return true;
    }

    public boolean mouseScrolled(int mx, int my, double amount, int px, HexSBMConfig config) {
        if (!editing || mx <= px) return false;

        int step = amount > 0 ? 1 : -1;
        if (isFieldHovered(mx, my, px + 100, 85)) { handleScrollField("outer", step, config); return true; }
        if (isFieldHovered(mx, my, px + 100, 115)) { handleScrollField("inner", step, config); return true; }
        if (isFieldHovered(mx, my, px + 100, 145)) { handleScrollField("outerInner", step, config); return true; }
        if (isFieldHovered(mx, my, px + 100, 175)) { handleScrollField("innerOuter", step, config); return true; }
        if (isFieldHovered(mx, my, px + 100, 205)) { handleScrollField("innerIconOffset", step, config); return true; }
        if (isFieldHovered(mx, my, px + 100, 235)) { handleScrollField("outerIconOffset", step, config); return true; }
        return true;
    }

    public void close(HexSBMConfig config) {
        applyEditingValue(config);
    }

    // --- Внутренние методы ---

    private void startEditing(String field, int value) {
        editingField = field;
        editingValue.setLength(0);
        editingValue.append(value);
    }

    private void applyEditingValue(HexSBMConfig config) {
        if (editingField == null || editingValue.length() == 0) return;
        try {
            int value = Integer.parseInt(editingValue.toString());
            switch (editingField) {
                case "outer" -> config.outerRingOuterRadius = value;
                case "inner" -> config.innerRingInnerRadius = value;
                case "outerInner" -> config.outerRingInnerRadius = value;
                case "innerOuter" -> config.innerRingOuterRadius = value;
                case "innerIconOffset" -> config.innerIconRadiusOffset = MathHelper.clamp(value, -HexSBMConfig.MAX_OFFSET, HexSBMConfig.MAX_OFFSET);
                case "outerIconOffset" -> config.outerIconRadiusOffset = MathHelper.clamp(value, -HexSBMConfig.MAX_OFFSET, HexSBMConfig.MAX_OFFSET);
            }
            if (List.of("outer", "inner", "outerInner", "innerOuter").contains(editingField)) {
                enforceRingOrder(editingField, config);
            }
        } catch (NumberFormatException ignored) {}
        editingField = null;
    }

    private void handleScrollField(String fieldName, int step, HexSBMConfig config) {
        int currentValue = getCurrentValue(fieldName, config);
        int newValue = currentValue + step;
        switch (fieldName) {
            case "outer" -> config.outerRingOuterRadius = newValue;
            case "inner" -> config.innerRingInnerRadius = newValue;
            case "outerInner" -> config.outerRingInnerRadius = newValue;
            case "innerOuter" -> config.innerRingOuterRadius = newValue;
            case "innerIconOffset" -> config.innerIconRadiusOffset = MathHelper.clamp(newValue, -HexSBMConfig.MAX_OFFSET, HexSBMConfig.MAX_OFFSET);
            case "outerIconOffset" -> config.outerIconRadiusOffset = MathHelper.clamp(newValue, -HexSBMConfig.MAX_OFFSET, HexSBMConfig.MAX_OFFSET);
        }
        if (List.of("outer", "inner", "outerInner", "innerOuter").contains(fieldName)) {
            enforceRingOrder(fieldName, config);
        }
        if (editingField != null && editingField.equals(fieldName)) {
            editingValue.setLength(0);
            editingValue.append(getCurrentValue(fieldName, config));
        }
    }

    private int getCurrentValue(String field, HexSBMConfig config) {
        return switch (field) {
            case "outer" -> config.outerRingOuterRadius;
            case "inner" -> config.innerRingInnerRadius;
            case "outerInner" -> config.outerRingInnerRadius;
            case "innerOuter" -> config.innerRingOuterRadius;
            case "innerIconOffset" -> config.innerIconRadiusOffset;
            case "outerIconOffset" -> config.outerIconRadiusOffset;
            default -> 0;
        };
    }

    private void enforceRingOrder(String editingField, HexSBMConfig config) {
        int innerIn = Math.max(0, config.innerRingInnerRadius);
        int innerOut = Math.max(0, config.innerRingOuterRadius);
        int outerIn = Math.max(0, config.outerRingInnerRadius);
        int outerOut = Math.max(0, config.outerRingOuterRadius);

        outerOut = Math.min(HexSBMConfig.MAX_RADIUS, outerOut);
        outerIn = Math.min(HexSBMConfig.MAX_RADIUS, outerIn);
        innerOut = Math.min(HexSBMConfig.MAX_RADIUS, innerOut);
        innerIn = Math.min(HexSBMConfig.MAX_RADIUS, innerIn);

        switch (editingField) {
            case "inner" -> {
                innerOut = Math.max(innerIn, innerOut);
                outerIn = Math.max(innerOut, outerIn);
                outerOut = Math.max(outerIn, outerOut);
            }
            case "innerOuter" -> {
                innerIn = Math.min(innerIn, innerOut);
                outerIn = Math.max(outerIn, innerOut);
                outerOut = Math.max(outerOut, outerIn);
            }
            case "outerInner" -> {
                outerOut = Math.max(outerOut, outerIn);
                innerOut = Math.min(innerOut, outerIn);
                innerIn = Math.min(innerIn, innerOut);
            }
            case "outer" -> {
                outerIn = Math.min(outerIn, outerOut);
                innerOut = Math.min(innerOut, outerIn);
                innerIn = Math.min(innerIn, innerOut);
            }
            default -> {
                innerIn = Math.max(0, innerIn);
                innerOut = Math.max(innerIn, innerOut);
                outerIn = Math.max(innerOut, outerIn);
                outerOut = Math.max(outerIn, outerOut);
            }
        }

        outerOut = Math.min(HexSBMConfig.MAX_RADIUS, outerOut);
        outerIn = Math.min(HexSBMConfig.MAX_RADIUS, outerIn);
        innerOut = Math.min(HexSBMConfig.MAX_RADIUS, innerOut);
        innerIn = Math.min(HexSBMConfig.MAX_RADIUS, innerIn);

        config.innerRingInnerRadius = innerIn;
        config.innerRingOuterRadius = innerOut;
        config.outerRingInnerRadius = outerIn;
        config.outerRingOuterRadius = outerOut;
    }

    private void resetToDefaults(HexSBMConfig config) {
        HexSBMConfig defaults = new HexSBMConfig();
        config.copyFrom(defaults);
    }

    private void reloadFromDisk(HexSBMConfig config) {
        HexSBMConfig saved = ConfigManager.getSavedConfig();
        config.copyFrom(saved);
    }

    private void drawNumberField(DrawContext ctx, int x, int y, int value, int mx, int my, String label, String fieldId, TextRenderer textRenderer) {
        boolean hover = isFieldHovered(mx, my, x, y);
        if (hover) ctx.fill(x, y, x + FIELD_WIDTH, y + FIELD_HEIGHT, 0x44FFFFFF);
        ctx.fill(x, y, x + FIELD_WIDTH, y + FIELD_HEIGHT, 0xFF333333);
        ctx.fill(x, y, x + FIELD_WIDTH, y + 1, 0xFF666666);
        ctx.fill(x, y, x + 1, y + FIELD_HEIGHT, 0xFF666666);

        String display = editingField != null && editingField.equals(fieldId) ? editingValue.toString() : String.valueOf(value);
        ctx.drawText(textRenderer, display, x + 3, y + 4, 0xFFFFFF, false);

        if (editingField != null && editingField.equals(fieldId)) {
            long time = System.currentTimeMillis() / 500;
            if (time % 2 == 0) {
                int textWidth = textRenderer.getWidth(Text.literal(display));
                ctx.fill(x + 3 + textWidth + 1, y + 3, x + 3 + textWidth + 2, y + 13, 0xFFFFFFFF);
            }
        }

        int labelWidth = textRenderer.getWidth(Text.literal(label + ":"));
        ctx.drawText(textRenderer, label + ":", x - labelWidth - 5, y + 4, 0xFFFFFF, false);
    }

    private boolean isFieldHovered(int mx, int my, int x, int y) {
        return mx >= x && mx < x + FIELD_WIDTH && my >= y && my < y + FIELD_HEIGHT;
    }
}