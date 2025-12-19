// com/hexsbm/screen/ui/ConfigPanel.java
package com.hexsbm.screen.ui;

import com.hexsbm.config.ConfigManager;
import com.hexsbm.config.HexSBMConfig;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import java.util.List;

public class ConfigPanel {
    private final List<NumberField> fields;
    private HexSBMConfig configRef; // для enforceRingOrder

    public ConfigPanel(HexSBMConfig config) {
        this.configRef = config;
        this.fields = List.of(
            new NumberField(100, 85, "Внешний радиус", 
                () -> config.outerRingOuterRadius,
                v -> {
                    config.outerRingOuterRadius = v;
                    enforceRingOrder("outer", config);
                },
                false),

            new NumberField(100, 115, "Внутр. радиус",
                () -> config.innerRingInnerRadius,
                v -> {
                    config.innerRingInnerRadius = v;
                    enforceRingOrder("inner", config);
                },
                false),

            new NumberField(100, 145, "Начало внеш.",
                () -> config.outerRingInnerRadius,
                v -> {
                    config.outerRingInnerRadius = v;
                    enforceRingOrder("outerInner", config);
                },
                false),

            new NumberField(100, 175, "Конец внутр.",
                () -> config.innerRingOuterRadius,
                v -> {
                    config.innerRingOuterRadius = v;
                    enforceRingOrder("innerOuter", config);
                },
                false),

            new NumberField(100, 205, "Смещение внутр.",
                () -> config.innerIconRadiusOffset,
                v -> config.innerIconRadiusOffset = clampOffset(v),
                true),

            new NumberField(100, 235, "Смещение внеш.",
                () -> config.outerIconRadiusOffset,
                v -> config.outerIconRadiusOffset = clampOffset(v),
                true)
        );
    }

    private int clampOffset(int v) {
        return net.minecraft.util.math.MathHelper.clamp(v, -HexSBMConfig.MAX_OFFSET, HexSBMConfig.MAX_OFFSET);
    }

    public void render(DrawContext ctx, int px, HexSBMConfig config, TextRenderer textRenderer, int mx, int my) {
        // Фон панели
        ctx.fill(px, 0, px + 220, ctx.getScaledWindowHeight(), 0x88000000);

        // Заголовки
        ctx.drawText(textRenderer, "Настройки UI", px + 10, 5, 0xFFFFFF, false);
        ctx.drawText(textRenderer, "Сбросить всё", px + 10, 22, 0xFF6666, false);
        ctx.drawText(textRenderer, "Сбросить до моего", px + 10, 52, 0x66FF66, false);

        // Цвет и автовыбор
        ctx.drawText(textRenderer, "Цвет UI:", px + 10, 270, 0xFFFFFF, false);
        String colorHex = String.format("%08X", config.uiBaseColor);
        ctx.drawText(textRenderer, colorHex, px + 100, 270, 0xFFAAAA, false);
        ctx.drawText(textRenderer, "Авто: " + (config.usePigmentColor ? "Да" : "Нет"), px + 10, 285,
            config.usePigmentColor ? 0x66FF66 : 0xFF6666, false);

        // Числовые поля
        for (var field : fields) {
            field.render(ctx, textRenderer, mx, my, px);
        }
    }

    public boolean mouseClicked(int mx, int my, int px, HexSBMConfig config) {
        if (mx <= px) return false;

        // Сначала — числовые поля
        for (var field : fields) {
            if (field.mouseClicked(mx, my, px)) {
                // Снимаем фокус с других
                for (var f : fields) {
                    if (f != field) f.finishEditing();
                }
                return true;
            }
        }

        // Клик мимо полей — завершить редактирование
        boolean wasEditing = fields.stream().anyMatch(NumberField::isEditing);
        if (wasEditing) {
            fields.forEach(NumberField::finishEditing);
            return true;
        }

        // Кнопки
        if (my >= 20 && my <= 40) {
            resetToDefaults(config);
            return true;
        }
        if (my >= 50 && my <= 70) {
            reloadFromDisk(config);
            return true;
        }
        if (my >= 285 && my <= 300) {
            config.usePigmentColor = !config.usePigmentColor;
            return true;
        }

        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int mods, HexSBMConfig config) {
        for (var field : fields) {
            if (field.isEditing() && field.keyPressed(keyCode, scanCode)) {
                return true;
            }
        }
        return false;
    }

    public boolean mouseScrolled(int mx, int my, double amount, int px, HexSBMConfig config) {
        if (mx <= px) return false;
        for (var field : fields) {
            if (field.mouseScrolled(mx, my, amount, px)) {
                return true;
            }
        }
        return false;
    }

    public void close(HexSBMConfig config) {
        fields.forEach(NumberField::finishEditing);
    }

    // === Сохранённая логика из твоего кода ===

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

        config.innerRingInnerRadius = Math.min(HexSBMConfig.MAX_RADIUS, innerIn);
        config.innerRingOuterRadius = Math.min(HexSBMConfig.MAX_RADIUS, innerOut);
        config.outerRingInnerRadius = Math.min(HexSBMConfig.MAX_RADIUS, outerIn);
        config.outerRingOuterRadius = Math.min(HexSBMConfig.MAX_RADIUS, outerOut);
    }

    private void resetToDefaults(HexSBMConfig config) {
        config.copyFrom(new HexSBMConfig());
    }

    private void reloadFromDisk(HexSBMConfig config) {
        config.copyFrom(ConfigManager.getSavedConfig());
    }
}