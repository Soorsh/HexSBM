package com.hexsbm.screen.ui;

import com.hexsbm.config.ConfigManager;
import com.hexsbm.config.HexSBMConfig;
import com.hexsbm.screen.ui.elements.Button;
import com.hexsbm.screen.ui.elements.ConfigControl;
import com.hexsbm.screen.ui.elements.CycleField;
import com.hexsbm.screen.ui.elements.DividerControl;
import com.hexsbm.screen.ui.elements.LabelControl;
import com.hexsbm.screen.ui.elements.NumberField;
import com.hexsbm.screen.ui.elements.CheckBoxField;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class ConfigPanel {
    private final List<ConfigControl> controls;
    private final HexSBMConfig config;
    private int scrollY = 0;
    private static final int CONTENT_HEIGHT = 810;

    public ConfigPanel(HexSBMConfig config) {
        this.config = config;
        this.controls = new ArrayList<>();

        int y = 20;
        
        // === Поведение ===
        controls.add(new LabelControl("Поведение", y, 0xAAAAAA)); y += 20;
        controls.add(new CheckBoxField(10, y, "Тултипы", config::isEnableTooltips, config::setEnableTooltips)); y += 20;
        controls.add(new CheckBoxField(10, y, "Закрывать по клику", config::isCloseOnBackgroundClick, config::setCloseOnBackgroundClick)); y += 20;
        controls.add(new CycleField(10, y, "Открытие меню", List.of("По зажатию", "По клику"), config::getMenuOpenMode, config::setMenuOpenMode)); y += 30;
        controls.add(new DividerControl(y, 10)); y += 10; // Divider

        // === Внешнее кольцо ===
        controls.add(new LabelControl("Внешнее кольцо", y, 0xAAAAAA)); y += 20;
        controls.add(new NumberField(100, y, "Внешний радиус", config::getOuterRingOuterRadius, config::setOuterRingOuterRadius, false)); y += 20;
        controls.add(new NumberField(100, y, "Начало", config::getOuterRingInnerRadius, config::setOuterRingInnerRadius, false)); y += 20;
        controls.add(new NumberField(100, y, "Смещение", config::getOuterIconRadiusOffset, v -> config.setOuterIconRadiusOffset(v), true)); y += 30;
        controls.add(new DividerControl(y, 10)); y += 10; // Divider

        // === Внутреннее кольцо ===
        controls.add(new LabelControl("Внутреннее кольцо", y, 0xAAAAAA)); y += 20;
        controls.add(new NumberField(100, y, "Внутр. радиус", config::getInnerRingInnerRadius, config::setInnerRingInnerRadius, false)); y += 20;
        controls.add(new NumberField(100, y, "Конец", config::getInnerRingOuterRadius, config::setInnerRingOuterRadius, false)); y += 20;
        controls.add(new NumberField(100, y, "Смещение", config::getInnerIconRadiusOffset, v -> config.setInnerIconRadiusOffset(v), true)); y += 30;
        controls.add(new DividerControl(y, 10)); y += 10; // Divider

        // === Цвет ===
        controls.add(new LabelControl("Цвет", y, 0xAAAAAA)); y += 20;
        controls.add(new CheckBoxField(10, y, "Авто-цвет", config::isUsePigmentColor, config::setUsePigmentColor)); y += 20;

        // ARGB
        controls.add(new NumberField(100, y, "A", () -> (config.uiBaseColor >> 24) & 0xFF, v -> config.uiBaseColor = ((v & 0xFF) << 24) | (config.uiBaseColor & 0x00FFFFFF), false, true)); y += 20;
        controls.add(new NumberField(100, y, "R", () -> (config.uiBaseColor >> 16) & 0xFF, v -> config.uiBaseColor = (config.uiBaseColor & 0xFF00FFFF) | ((v & 0xFF) << 16), false, true)); y += 20;
        controls.add(new NumberField(100, y, "G", () -> (config.uiBaseColor >> 8) & 0xFF, v -> config.uiBaseColor = (config.uiBaseColor & 0xFFFF00FF) | ((v & 0xFF) << 8), false, true)); y += 20;
        controls.add(new NumberField(100, y, "B", () -> config.uiBaseColor & 0xFF, v -> config.uiBaseColor = (config.uiBaseColor & 0xFFFFFF00) | (v & 0xFF), false, true)); y += 20;

        // Доп. настройки цвета
        controls.add(new CycleField(10, y, "Режим", List.of("По заклинанию", "Всегда", "Никогда"), config::getColorMode, config::setColorMode)); y += 20;
        controls.add(new CheckBoxField(10, y, "Градиент", () -> !config.isDisableGradient(), val -> config.setDisableGradient(!val))); y += 20;
        controls.add(new DividerControl(y, 10)); y += 10; // Divider

        // === Градиент: Внешнее кольцо ===
        controls.add(new LabelControl("Градиент: Внешнее", y, 0xAAAAAA)); y += 20;
        controls.add(new NumberField(100, y, "Актив. +", () -> (int)(config.outerActiveLighten * 100), v -> config.outerActiveLighten = v / 100f, false)); y += 20;
        controls.add(new NumberField(100, y, "Наведение +", () -> (int)(config.outerHoverLighten * 100), v -> config.outerHoverLighten = v / 100f, false)); y += 20;
        controls.add(new NumberField(100, y, "Неакт. +", () -> (int)(config.outerInactiveLighten * 100), v -> config.outerInactiveLighten = v / 100f, false)); y += 20;
        controls.add(new NumberField(100, y, "Неакт. –", () -> (int)(config.outerInactiveDarken * 100), v -> config.outerInactiveDarken = v / 100f, false)); y += 30;
        controls.add(new DividerControl(y, 10)); y += 10; // Divider

        // === Градиент: Внутреннее кольцо ===
        controls.add(new LabelControl("Градиент: Внутреннее", y, 0xAAAAAA)); y += 20;
        controls.add(new NumberField(100, y, "Актив. +", () -> (int)(config.innerActiveLighten * 100), v -> config.innerActiveLighten = v / 100f, false)); y += 20;
        controls.add(new NumberField(100, y, "Наведение +", () -> (int)(config.innerHoverLighten * 100), v -> config.innerHoverLighten = v / 100f, false)); y += 20;
        controls.add(new NumberField(100, y, "Неакт. +", () -> (int)(config.innerInactiveLighten * 100), v -> config.innerInactiveLighten = v / 100f, false)); y += 20;
        controls.add(new NumberField(100, y, "Неакт. –", () -> (int)(config.innerInactiveDarken * 100), v -> config.innerInactiveDarken = v / 100f, false)); y += 30;
        controls.add(new DividerControl(y, 10)); y += 10; // Divider
        
        // === Сброс ===
        controls.add(new LabelControl("Сброс", y, 0xAAAAAA)); y += 20;
        controls.add(new Button(10, y, "Сохранить", 0xFFFFFF, () -> {})); y += 20;
        controls.add(new Button(10, y, "Сбросить", 0xFFFFFF, this::reloadFromDisk)); y += 20;
        controls.add(new Button(10, y, "СБРОСИТЬ ВСЁ", 0xFFFFFF, this::resetToDefaults)); y += 20;
    }

    public void render(DrawContext ctx, int px, HexSBMConfig config, TextRenderer textRenderer, int mx, int my) {
        int panelHeight = ctx.getScaledWindowHeight();
        ctx.fill(px, 0, px + 220, panelHeight, 0xCC000000);
        ctx.fill(px, 0, px + 220, 1, 0xFFFFFFFF);
        ctx.fill(px, 0, px + 1, panelHeight, 0xFFFFFFFF);
        ctx.fill(px + 219, 0, px + 220, panelHeight, 0xFFFFFFFF);

        // Заголовок, который не скроллится
        ctx.drawText(textRenderer, "Настройки UI", px + 10, 5, 0xFFFFFFFF, false);

        // Включаем Scissor для области скролла
        ctx.enableScissor(px + 1, 20, px + 219, panelHeight);

        for (var control : controls) {
            control.render(ctx, textRenderer, mx, my, px, scrollY);
        }

        // Выключаем Scissor
        ctx.disableScissor();
    }

    public boolean mouseClicked(int mx, int my, int px, HexSBMConfig config, TextRenderer textRenderer) {
        if (mx <= px) return false;

        boolean wasEditing = controls.stream().anyMatch(ConfigControl::isEditing);
        boolean clickedOnControl = false;

        for (var control : controls) {
            if (control.mouseClicked(mx, my, px, textRenderer, scrollY)) {
                for (var c : controls) {
                    if (c != control) c.finishEditing();
                }
                clickedOnControl = true;
                break;
            }
        }

        if (clickedOnControl) return true;
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

    public boolean mouseScrolled(int mx, int my, double amount, int px, HexSBMConfig config, int windowHeight) {
        if (mx <= px) return false;

        for (var control : controls) {
            if (control instanceof NumberField field) {
                if (field.isMouseOver(mx, my, px, scrollY)) {
                    // Делегируем обработку скролла самому NumberField
                    return field.mouseScrolled(mx, my, amount, px);
                }
            }
        }

        int maxScroll = Math.max(0, CONTENT_HEIGHT - windowHeight);
        scrollY = MathHelper.clamp(scrollY - (int)(amount * 10), 0, maxScroll);
        return true;
    }

    public void close(HexSBMConfig config) {
        controls.forEach(ConfigControl::finishEditing);
    }

    private void resetToDefaults() {
        this.config.copyFrom(new HexSBMConfig());
    }

    private void reloadFromDisk() {
        this.config.copyFrom(ConfigManager.getSavedConfig());
    }
}