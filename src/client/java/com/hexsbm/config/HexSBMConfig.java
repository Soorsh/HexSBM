package com.hexsbm.config;

import net.minecraft.util.math.MathHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HexSBMConfig {
    // === Позиция и размеры ===
    public float centerX = 0.5f;
    public float centerY = 0.5f;
    public int innerRingInnerRadius = 30;
    public int innerRingOuterRadius = 60;
    public int outerRingInnerRadius = 70;
    public int outerRingOuterRadius = 110;
    public int innerIconRadiusOffset = 0;
    public int outerIconRadiusOffset = 0;

    // === Прозрачность ===
    public int activeAlpha = 0x99;
    public int hoverAlpha = 0x90;
    public int inactiveAlpha = 0x80;

    // === Поведение ===
    public boolean enableTooltips = true;
    public boolean closeOnBackgroundClick = true;

    // === Цвет: Внешнее кольцо ===
    public float outerActiveLighten = 0.15f;
    public float outerHoverLighten = 0.25f;
    public float outerInactiveLighten = 0.10f;
    public float outerInactiveDarken = 0.10f;

    // === Цвет: Внутреннее кольцо ===
    public float innerActiveLighten = 0.15f;
    public float innerHoverLighten = 0.25f;
    public float innerInactiveLighten = 0.10f;
    public float innerInactiveDarken = 0.10f;

    // === Рендер кольца ===
    public int segmentResolution = 16;

    // === Tooltip ===
    public int patternTooltipLineIndex = 2;
    public int minTooltipLinesForPattern = 3;

    // === NBT-теги ===
    public List<String> visualNbtTags = Arrays.asList(
        "hexcasting:pattern_data",
        "hexcasting:amulet_state",
        "hexcasting:op_code",
        "CustomPotionColor",
        "patchouli:book",
        "Enchantments",
        "SkullOwner",
        "EntityTag",
        "display",
        "Potion",
        "op_id"
    );

    // === Системные ограничения ===
    public static final int MAX_RADIUS = 999;
    public static final int MAX_OFFSET = 200;

    // === Цвет интерфейса ===
    public boolean usePigmentColor = true;
    public int uiBaseColor = 0xFFFFFFFF;

    // === Режим цвета ===
    public int colorMode = 1; // 0 = по заклинанию, 1 = всегда, 2 = никогда

    // === Отключение градиента ===
    public boolean disableGradient = false;

    // === Режим открытия МЕНЮ (не конфига!) ===
    // 0 = По зажатию клавиши
    // 1 = По клику (toggle)
    public int menuOpenMode = 0;

    // =============== ГЕТТЕРЫ ===============
    public int getInnerRingInnerRadius() { return innerRingInnerRadius; }
    public int getInnerRingOuterRadius() { return innerRingOuterRadius; }
    public int getOuterRingInnerRadius() { return outerRingInnerRadius; }
    public int getOuterRingOuterRadius() { return outerRingOuterRadius; }
    public int getInnerIconRadiusOffset() { return innerIconRadiusOffset; }
    public int getOuterIconRadiusOffset() { return outerIconRadiusOffset; }
    public boolean isUsePigmentColor() { return usePigmentColor; }
    public boolean isEnableTooltips() { return enableTooltips; }
    public boolean isCloseOnBackgroundClick() { return closeOnBackgroundClick; }
    public int getColorMode() { return colorMode; }
    public int getMenuOpenMode() { return menuOpenMode; }
    public boolean isDisableGradient() { return disableGradient; }

    // =============== СЕТТЕРЫ ===============
    public void setInnerRingInnerRadius(int v) { v = MathHelper.clamp(v, 0, MAX_RADIUS); this.innerRingInnerRadius = v; if (this.innerRingOuterRadius < v) this.innerRingOuterRadius = v; }
    public void setInnerRingOuterRadius(int v) { v = MathHelper.clamp(v, 0, MAX_RADIUS); this.innerRingOuterRadius = v; if (this.innerRingInnerRadius > v) this.innerRingInnerRadius = v; }
    public void setOuterRingInnerRadius(int v) { v = MathHelper.clamp(v, 0, MAX_RADIUS); this.outerRingInnerRadius = v; if (this.outerRingOuterRadius < v) this.outerRingOuterRadius = v; }
    public void setOuterRingOuterRadius(int v) { v = MathHelper.clamp(v, 0, MAX_RADIUS); this.outerRingOuterRadius = v; if (this.outerRingInnerRadius > v) this.outerRingInnerRadius = v; }
    public void setInnerIconRadiusOffset(int v) { this.innerIconRadiusOffset = MathHelper.clamp(v, -MAX_OFFSET, MAX_OFFSET); }
    public void setOuterIconRadiusOffset(int v) { this.outerIconRadiusOffset = MathHelper.clamp(v, -MAX_OFFSET, MAX_OFFSET); }
    public void setUsePigmentColor(boolean v) { this.usePigmentColor = v; }
    public void setEnableTooltips(boolean v) { this.enableTooltips = v; }
    public void setCloseOnBackgroundClick(boolean v) { this.closeOnBackgroundClick = v; }
    public void setColorMode(int v) { this.colorMode = MathHelper.clamp(v, 0, 2); }
    public void setMenuOpenMode(int v) { this.menuOpenMode = MathHelper.clamp(v, 0, 1); }
    public void setDisableGradient(boolean v) { this.disableGradient = v; }

    private void enforceRingOrder() {
        this.innerRingInnerRadius = MathHelper.clamp(this.innerRingInnerRadius, 0, MAX_RADIUS);
        this.innerRingOuterRadius = MathHelper.clamp(this.innerRingOuterRadius, 0, MAX_RADIUS);
        this.outerRingInnerRadius = MathHelper.clamp(this.outerRingInnerRadius, 0, MAX_RADIUS);
        this.outerRingOuterRadius = MathHelper.clamp(this.outerRingOuterRadius, 0, MAX_RADIUS);

        // Внутри каждого кольца: внутренний радиус не больше внешнего
        if (this.innerRingInnerRadius > this.innerRingOuterRadius) {
            this.innerRingOuterRadius = this.innerRingInnerRadius;
        }
        if (this.outerRingInnerRadius > this.outerRingOuterRadius) {
            this.outerRingOuterRadius = this.outerRingInnerRadius;
        }
        // ← Между кольцами НЕТ связи. Совсем.
    }

    public HexSBMConfig() {}

    public void copyFrom(HexSBMConfig other) {
        this.centerX = other.centerX;
        this.centerY = other.centerY;
        this.innerRingInnerRadius = other.innerRingInnerRadius;
        this.innerRingOuterRadius = other.innerRingOuterRadius;
        this.outerRingInnerRadius = other.outerRingInnerRadius;
        this.outerRingOuterRadius = other.outerRingOuterRadius;
        this.innerIconRadiusOffset = other.innerIconRadiusOffset;
        this.outerIconRadiusOffset = other.outerIconRadiusOffset;
        this.activeAlpha = other.activeAlpha;
        this.hoverAlpha = other.hoverAlpha;
        this.inactiveAlpha = other.inactiveAlpha;
        this.enableTooltips = other.enableTooltips;
        this.closeOnBackgroundClick = other.closeOnBackgroundClick;
        this.outerActiveLighten = other.outerActiveLighten;
        this.outerHoverLighten = other.outerHoverLighten;
        this.outerInactiveLighten = other.outerInactiveLighten;
        this.outerInactiveDarken = other.outerInactiveDarken;
        this.innerActiveLighten = other.innerActiveLighten;
        this.innerHoverLighten = other.innerHoverLighten;
        this.innerInactiveLighten = other.innerInactiveLighten;
        this.innerInactiveDarken = other.innerInactiveDarken;
        this.segmentResolution = other.segmentResolution;
        this.patternTooltipLineIndex = other.patternTooltipLineIndex;
        this.minTooltipLinesForPattern = other.minTooltipLinesForPattern;
        this.visualNbtTags = new ArrayList<>(other.visualNbtTags);
        this.usePigmentColor = other.usePigmentColor;
        this.uiBaseColor = other.uiBaseColor;
        this.colorMode = other.colorMode;
        this.disableGradient = other.disableGradient;
        this.menuOpenMode = other.menuOpenMode;
    }

    public void resetToDefault() {
        copyFrom(new HexSBMConfig());
    }

    public HexSBMConfig copy() {
        HexSBMConfig c = new HexSBMConfig();
        c.copyFrom(this);
        return c;
    }
}