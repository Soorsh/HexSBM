package com.hexsbm.screen.ui;

import com.hexsbm.config.ConfigManager;
import com.hexsbm.config.HexSBMConfig;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class ConfigPanel {
    private final List<ConfigControl> controls;
    private final HexSBMConfig config;

    public ConfigPanel(HexSBMConfig config) {
        this.config = config;
        this.controls = new ArrayList<>();

        // === Кольца ===
        controls.add(new NumberField(100, 85, "Внешний радиус",
            config::getOuterRingOuterRadius,
            config::setOuterRingOuterRadius,
            false));

        controls.add(new NumberField(100, 115, "Внутр. радиус",
            config::getInnerRingInnerRadius,
            config::setInnerRingInnerRadius,
            false));

        controls.add(new NumberField(100, 145, "Начало внеш.",
            config::getOuterRingInnerRadius,
            config::setOuterRingInnerRadius,
            false));

        controls.add(new NumberField(100, 175, "Конец внутр.",
            config::getInnerRingOuterRadius,
            config::setInnerRingOuterRadius,
            false));

        // === Смещения ===
        controls.add(new NumberField(100, 205, "Смещение внутр.",
            config::getInnerIconRadiusOffset,
            v -> config.setInnerIconRadiusOffset(v),
            true));

        controls.add(new NumberField(100, 235, "Смещение внеш.",
            config::getOuterIconRadiusOffset,
            v -> config.setOuterIconRadiusOffset(v),
            true));

        // === Кнопки ===
        controls.add(new Button(10, 22, "Сбросить всё", 0xFF6666, () -> resetToDefaults()));
        controls.add(new Button(10, 52, "Сбросить до моего", 0x66FF66, () -> reloadFromDisk()));

        // === Переключатель цвета ===
        controls.add(new ToggleField(10, 285, "Авто-цвет", config::isUsePigmentColor, config::setUsePigmentColor));
    }

    public void render(DrawContext ctx, int px, HexSBMConfig config, TextRenderer textRenderer, int mx, int my) {
        // Фон панели
        ctx.fill(px, 0, px + 220, ctx.getScaledWindowHeight(), 0x88000000);

        // Заголовок
        ctx.drawText(textRenderer, "Настройки UI", px + 10, 5, 0xFFFFFF, false);

        // Цвет UI (пока просто отображение)
        ctx.drawText(textRenderer, "Цвет UI:", px + 10, 270, 0xFFFFFF, false);
        String colorHex = String.format("%08X", config.uiBaseColor);
        ctx.drawText(textRenderer, colorHex, px + 100, 270, 0xFFAAAA, false);

        // Рендер всех контролов
        for (var control : controls) {
            control.render(ctx, textRenderer, mx, my, px);
        }
    }

    public boolean mouseClicked(int mx, int my, int px, HexSBMConfig config, TextRenderer textRenderer) {
        if (mx <= px) return false;

        // Если какое-то поле в редактировании — снимаем фокус при клике мимо
        boolean wasEditing = controls.stream().anyMatch(ConfigControl::isEditing);
        boolean clickedOnControl = false;

        for (var control : controls) {
            if (control.mouseClicked(mx, my, px, textRenderer)) {
                // Снимаем фокус с других
                for (var c : controls) {
                    if (c != control) c.finishEditing();
                }
                clickedOnControl = true;
                break;
            }
        }

        if (clickedOnControl) {
            return true;
        }

        if (wasEditing) {
            controls.forEach(ConfigControl::finishEditing);
            return true;
        }

        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int mods, HexSBMConfig config) {
        for (var control : controls) {
            if (control.isEditing() && control instanceof NumberField field) {
                if (field.keyPressed(keyCode, scanCode)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mouseScrolled(int mx, int my, double amount, int px, HexSBMConfig config) {
        if (mx <= px) return false;
        for (var control : controls) {
            if (control.mouseScrolled(mx, my, amount, px)) {
                return true;
            }
        }
        return false;
    }

    public void close(HexSBMConfig config) {
        controls.forEach(ConfigControl::finishEditing);
    }

    // === Действия ===
    private void resetToDefaults() {
        this.config.copyFrom(new HexSBMConfig());
    }

    private void reloadFromDisk() {
        this.config.copyFrom(ConfigManager.getSavedConfig());
    }
}