package com.hexsbm.screen.ui;

import com.hexsbm.config.ConfigManager;
import com.hexsbm.config.HexSBMConfig;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class ConfigPanel {
    private final List<ConfigControl> controls;
    private final HexSBMConfig config;
    private int scrollY = 0;
    private static final int CONTENT_HEIGHT = 560; // чуть увеличено

    public ConfigPanel(HexSBMConfig config) {
        this.config = config;
        this.controls = new ArrayList<>();

        // === Внешнее кольцо ===
        controls.add(new LabelControl("Внешнее кольцо", 60, 0xAAAAAA));
        controls.add(new NumberField(100, 80, "Внешний радиус",
            config::getOuterRingOuterRadius,
            config::setOuterRingOuterRadius,
            false));
        controls.add(new NumberField(100, 100, "Начало",
            config::getOuterRingInnerRadius,
            config::setOuterRingInnerRadius,
            false));
        controls.add(new NumberField(100, 120, "Смещение",
            config::getOuterIconRadiusOffset,
            v -> config.setOuterIconRadiusOffset(v),
            true));

        // === Внутреннее кольцо ===
        controls.add(new LabelControl("Внутреннее кольцо", 150, 0xAAAAAA));
        controls.add(new NumberField(100, 170, "Внутр. радиус",
            config::getInnerRingInnerRadius,
            config::setInnerRingInnerRadius,
            false));
        controls.add(new NumberField(100, 190, "Конец",
            config::getInnerRingOuterRadius,
            config::setInnerRingOuterRadius,
            false));
        controls.add(new NumberField(100, 210, "Смещение",
            config::getInnerIconRadiusOffset,
            v -> config.setInnerIconRadiusOffset(v),
            true));

        // === Цвет ===
        controls.add(new LabelControl("Цвет", 240, 0xAAAAAA));
        controls.add(new ToggleField(10, 260, "Авто-цвет", config::isUsePigmentColor, config::setUsePigmentColor));

        // ARGB-компоненты
        controls.add(new NumberField(100, 280, "A",
            () -> (config.uiBaseColor >> 24) & 0xFF,
            v -> config.uiBaseColor = ((v & 0xFF) << 24) | (config.uiBaseColor & 0x00FFFFFF),
            false));
        controls.add(new NumberField(100, 300, "R",
            () -> (config.uiBaseColor >> 16) & 0xFF,
            v -> config.uiBaseColor = (config.uiBaseColor & 0xFF00FFFF) | ((v & 0xFF) << 16),
            false));
        controls.add(new NumberField(100, 320, "G",
            () -> (config.uiBaseColor >> 8) & 0xFF,
            v -> config.uiBaseColor = (config.uiBaseColor & 0xFFFF00FF) | ((v & 0xFF) << 8),
            false));
        controls.add(new NumberField(100, 340, "B",
            () -> config.uiBaseColor & 0xFF,
            v -> config.uiBaseColor = (config.uiBaseColor & 0xFFFFFF00) | (v & 0xFF),
            false));

        // Доп. настройки цвета
        controls.add(new CycleField(10, 360, "Режим",
            List.of("По заклинанию", "Всегда", "Никогда"),
            config::getColorMode,
            config::setColorMode));
        controls.add(new ToggleField(10, 380, "Без градиента", () -> false, v -> {})); // TODO

        // === Поведение ===
        controls.add(new LabelControl("Поведение", 410, 0xAAAAAA));
        controls.add(new ToggleField(10, 430, "Тултипы", config::isEnableTooltips, config::setEnableTooltips));
        controls.add(new ToggleField(10, 450, "Закрывать по клику", config::isCloseOnBackgroundClick, config::setCloseOnBackgroundClick));
        controls.add(new CycleField(10, 470, "Открытие меню",
            List.of("По зажатию", "По клику"),
            config::getMenuOpenMode,
            config::setMenuOpenMode));

        // === Сброс ===
        controls.add(new LabelControl("Сброс", 500, 0xAAAAAA));
        controls.add(new Button(10, 520, "Сбросить всё", 0xFF6666, this::resetToDefaults));
        controls.add(new Button(10, 540, "Сбросить до моего", 0x66FF66, this::reloadFromDisk));
    }

    public void render(DrawContext ctx, int px, HexSBMConfig config, TextRenderer textRenderer, int mx, int my) {
        int panelHeight = ctx.getScaledWindowHeight();
        ctx.fill(px, 0, px + 220, panelHeight, 0xCC000000);
        ctx.fill(px, 0, px + 220, 1, 0xFFFFFFFF);
        ctx.fill(px, 0, px + 1, panelHeight, 0xFFFFFFFF);
        ctx.fill(px + 219, 0, px + 220, panelHeight, 0xFFFFFFFF);

        ctx.drawText(textRenderer, "Настройки UI", px + 10, 5 - scrollY, 0xFFFFFF, false);

        for (var control : controls) {
            control.render(ctx, textRenderer, mx, my, px, scrollY);
        }
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
                    int step = amount > 0 ? 1 : -1;
                    int newValue = field.getter.getAsInt() + step;

                    if (field.isOffset) {
                        newValue = MathHelper.clamp(newValue, -HexSBMConfig.MAX_OFFSET, HexSBMConfig.MAX_OFFSET);
                    } else {
                        newValue = MathHelper.clamp(newValue, 0, HexSBMConfig.MAX_RADIUS);
                    }

                    field.setter.accept(newValue);
                    if (field.isEditing()) {
                        field.buffer.setLength(0);
                        field.buffer.append(newValue);
                    }
                    return true;
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