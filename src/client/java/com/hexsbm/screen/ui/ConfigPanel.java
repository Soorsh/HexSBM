package com.hexsbm.screen.ui;

import com.hexsbm.config.ConfigManager;
import com.hexsbm.config.HexSBMConfig;
import com.hexsbm.screen.ui.elements.*;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class ConfigPanel {
    private final List<ConfigControl> controls;
    private final HexSBMConfig config;
    private int scrollY = 0;
    private static final int CONTENT_HEIGHT = 860;

    private record RingControlSet(Text radius1Label, java.util.function.IntSupplier radius1Getter, java.util.function.IntConsumer radius1Setter,
                                  Text radius2Label, java.util.function.IntSupplier radius2Getter, java.util.function.IntConsumer radius2Setter,
                                  java.util.function.IntSupplier iconOffsetGetter, java.util.function.IntConsumer iconOffsetSetter) {}

    private record GradientControlSet(java.util.function.IntSupplier activeLightenGetter, java.util.function.IntConsumer activeLightenSetter,
                                      java.util.function.IntSupplier hoverLightenGetter, java.util.function.IntConsumer hoverLightenSetter,
                                      java.util.function.IntSupplier inactiveLightenGetter, java.util.function.IntConsumer inactiveLightenSetter,
                                      java.util.function.IntSupplier inactiveDarkenGetter, java.util.function.IntConsumer inactiveDarkenSetter) {}

    public ConfigPanel(HexSBMConfig config) {
        this.config = config;
        this.controls = new ArrayList<>();

        int y = 20;

        // === Сброс (Верх) ===
        y = addResetSection(y);
        controls.add(new DividerControl(y, 10)); y += 10;

        // === Поведение ===
        controls.add(new LabelControl(Text.translatable("hexsbm.ui.behavior"), y, 0xAAAAAA)); y += 20;
        controls.add(new CheckBoxField(10, y, Text.translatable("hexsbm.ui.tooltips"), config::isEnableTooltips, config::setEnableTooltips)); y += 20;
        controls.add(new CheckBoxField(10, y, Text.translatable("hexsbm.ui.close_on_miss_click"), config::isCloseOnBackgroundClick, config::setCloseOnBackgroundClick)); y += 20;
        controls.add(new CycleField(10, y, Text.translatable("hexsbm.ui.open_menu"), List.of(Text.translatable("hexsbm.ui.hold"), Text.translatable("hexsbm.ui.press")), config::getMenuOpenMode, config::setMenuOpenMode)); y += 30;
        controls.add(new DividerControl(y, 10)); y += 10; // Divider

        // === Кольца ===
        y = addRingControls(y, Text.translatable("hexsbm.ui.spell_ring"),
                new RingControlSet(
                        Text.translatable("hexsbm.ui.outer_radius"), config::getOuterRingOuterRadius, config::setOuterRingOuterRadius,
                        Text.translatable("hexsbm.ui.inner_radius"), config::getOuterRingInnerRadius, config::setOuterRingInnerRadius,
                        config::getOuterIconRadiusOffset, v -> config.setOuterIconRadiusOffset(v)
                ));

        y = addRingControls(y, Text.translatable("hexsbm.ui.group_ring"),
                new RingControlSet(
                        Text.translatable("hexsbm.ui.inner_radius"), config::getInnerRingInnerRadius, config::setInnerRingInnerRadius,
                        Text.translatable("hexsbm.ui.outer_radius"), config::getInnerRingOuterRadius, config::setInnerRingOuterRadius,
                        config::getInnerIconRadiusOffset, v -> config.setInnerIconRadiusOffset(v)
                ));

        // === Цвет ===
        controls.add(new LabelControl(Text.translatable("hexsbm.ui.color"), y, 0xAAAAAA)); y += 20;
        controls.add(new CheckBoxField(10, y, Text.translatable("hexsbm.ui.auto_color"), config::isUsePigmentColor, config::setUsePigmentColor)); y += 20;

        controls.add(new NumberField(100, y, Text.translatable("hexsbm.ui.a"), () -> (config.uiBaseColor >> 24) & 0xFF, v -> config.uiBaseColor = ((v & 0xFF) << 24) | (config.uiBaseColor & 0x00FFFFFF), false, true)); y += 20;
        controls.add(new NumberField(100, y, Text.translatable("hexsbm.ui.r"), () -> (config.uiBaseColor >> 16) & 0xFF, v -> config.uiBaseColor = (config.uiBaseColor & 0xFF00FFFF) | ((v & 0xFF) << 16), false, true)); y += 20;
        controls.add(new NumberField(100, y, Text.translatable("hexsbm.ui.g"), () -> (config.uiBaseColor >> 8) & 0xFF, v -> config.uiBaseColor = (config.uiBaseColor & 0xFFFF00FF) | ((v & 0xFF) << 8), false, true)); y += 20;
        controls.add(new NumberField(100, y, Text.translatable("hexsbm.ui.b"), () -> config.uiBaseColor & 0xFF, v -> config.uiBaseColor = (config.uiBaseColor & 0xFFFFFF00) | (v & 0xFF), false, true)); y += 20;

        controls.add(new CycleField(10, y, Text.translatable("hexsbm.ui.mode"), List.of(Text.translatable("hexsbm.ui.by_spell"), Text.translatable("hexsbm.ui.always"), Text.translatable("hexsbm.ui.never")), config::getColorMode, config::setColorMode)); y += 20;
        controls.add(new CheckBoxField(10, y, Text.translatable("hexsbm.ui.gradient"), () -> !config.isDisableGradient(), val -> config.setDisableGradient(!val))); y += 20;
        controls.add(new DividerControl(y, 10)); y += 10;

        // === Градиенты ===
        y = addGradientControls(y, Text.translatable("hexsbm.ui.gradient_outer"),
                new GradientControlSet(
                        () -> (int)(config.outerActiveLighten * 100), v -> config.outerActiveLighten = v / 100f,
                        () -> (int)(config.outerHoverLighten * 100), v -> config.outerHoverLighten = v / 100f,
                        () -> (int)(config.outerInactiveLighten * 100), v -> config.outerInactiveLighten = v / 100f,
                        () -> (int)(config.outerInactiveDarken * 100), v -> config.outerInactiveDarken = v / 100f
                ));

        y = addGradientControls(y, Text.translatable("hexsbm.ui.gradient_inner"),
                new GradientControlSet(
                        () -> (int)(config.innerActiveLighten * 100), v -> config.innerActiveLighten = v / 100f,
                        () -> (int)(config.innerHoverLighten * 100), v -> config.innerHoverLighten = v / 100f,
                        () -> (int)(config.innerInactiveLighten * 100), v -> config.innerInactiveLighten = v / 100f,
                        () -> (int)(config.innerInactiveDarken * 100), v -> config.innerInactiveDarken = v / 100f
                ));
        
        // === Сброс (Низ) ===
        y = addResetSection(y);
    }

    private int addRingControls(int y, Text title, RingControlSet rcs) {
        controls.add(new LabelControl(title, y, 0xAAAAAA)); y += 20;
        controls.add(new NumberField(100, y, rcs.radius1Label(), rcs.radius1Getter(), rcs.radius1Setter(), false)); y += 20;
        controls.add(new NumberField(100, y, rcs.radius2Label(), rcs.radius2Getter(), rcs.radius2Setter(), false)); y += 20;
        controls.add(new NumberField(100, y, Text.translatable("hexsbm.ui.icon_offset"), rcs.iconOffsetGetter(), rcs.iconOffsetSetter(), true)); y += 30;
        controls.add(new DividerControl(y, 10)); y += 10;
        return y;
    }

    private int addGradientControls(int y, Text title, GradientControlSet gcs) {
        controls.add(new LabelControl(title, y, 0xAAAAAA)); y += 20;
        controls.add(new NumberField(100, y, Text.translatable("hexsbm.ui.gradient_active_lighten"), gcs.activeLightenGetter(), gcs.activeLightenSetter(), false)); y += 20;
        controls.add(new NumberField(100, y, Text.translatable("hexsbm.ui.gradient_hover_lighten"), gcs.hoverLightenGetter(), gcs.hoverLightenSetter(), false)); y += 20;
        controls.add(new NumberField(100, y, Text.translatable("hexsbm.ui.gradient_inactive_lighten"), gcs.inactiveLightenGetter(), gcs.inactiveLightenSetter(), false)); y += 20;
        controls.add(new NumberField(100, y, Text.translatable("hexsbm.ui.gradient_inactive_darken"), gcs.inactiveDarkenGetter(), gcs.inactiveDarkenSetter(), false)); y += 30;
        controls.add(new DividerControl(y, 10)); y += 10;
        return y;
    }

    private int addResetSection(int y) {
        controls.add(new LabelControl(Text.translatable("hexsbm.ui.reset"), y, 0xAAAAAA)); y += 20;
        controls.add(new Button(15, y, Text.translatable("hexsbm.ui.save"), 0xFFFFFF, this::saveConfig));
        controls.add(new Button(115, y, Text.translatable("hexsbm.ui.reset_changes"), 0xFFFFFF, this::resetChanges)); y += 20;
        controls.add(new Button(115, y, Text.translatable("hexsbm.ui.reset_all"), 0xFFFF0000, this::resetToDefaults)); y += 20;
        return y;
    }

    public void render(DrawContext ctx, int px, TextRenderer textRenderer, int mx, int my) {
        int panelHeight = ctx.getScaledWindowHeight();
        ctx.fill(px, 0, px + 220, panelHeight, 0xCC000000);
        ctx.fill(px, 0, px + 220, 1, 0xFFFFFFFF);
        ctx.fill(px, 0, px + 1, panelHeight, 0xFFFFFFFF);
        ctx.fill(px + 219, 0, px + 220, panelHeight, 0xFFFFFFFF);

        ctx.drawText(textRenderer, Text.translatable("hexsbm.ui.title"), px + 10, 5, 0xFFFFFFFF, false);

        ctx.enableScissor(px + 1, 20, px + 219, panelHeight);

        for (var control : controls) {
            control.render(ctx, textRenderer, mx, my, px, scrollY);
        }

        ctx.disableScissor();
    }

    public boolean mouseClicked(int mx, int my, int px, TextRenderer textRenderer) {
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

    public boolean keyPressed(int keyCode, int scanCode, int mods) {
        for (var control : controls) {
            if (control.isEditing() && control instanceof NumberField field) {
                if (field.keyPressed(keyCode, scanCode)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mouseScrolled(int mx, int my, double amount, int px, int windowHeight) {
        if (mx <= px) return false;

        for (var control : controls) {
            if (control instanceof NumberField field) {
                if (field.isMouseOver(mx, my, px, scrollY)) {
                    return field.mouseScrolled(mx, my, amount, px);
                }
            }
        }

        int maxScroll = Math.max(0, CONTENT_HEIGHT - windowHeight);
        scrollY = MathHelper.clamp(scrollY - (int)(amount * 10), 0, maxScroll);
        return true;
    }

    public void close() {
        controls.forEach(ConfigControl::finishEditing);
    }

    private void saveConfig() {
        ConfigManager.saveConfig(this.config);
    }

    private void resetToDefaults() {
        this.config.copyFrom(new HexSBMConfig());
    }

    private void resetChanges() {
        this.config.copyFrom(ConfigManager.getSavedConfig());
    }
}