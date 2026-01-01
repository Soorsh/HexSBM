package com.hexsbm.config;

import net.minecraft.util.math.MathHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HexSBMConfig {
    // === Позиция и размеры (остаются public для ConfigLib) ===
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

    // === Цветовые поправки ===
    public float activeLighten = 0.15f;
    public float hoverLighten = 0.25f;
    public float inactiveLighten = 0.10f;
    public float inactiveDarken = 0.10f;
    public float innerOuterActiveLighten = 0.20f;

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

    // =============== СЕТТЕРЫ С ВАЛИДАЦИЕЙ ===============
    public void setInnerRingInnerRadius(int v) { this.innerRingInnerRadius = MathHelper.clamp(v, 0, MAX_RADIUS); enforceRingOrder(); }
    public void setInnerRingOuterRadius(int v) { this.innerRingOuterRadius = MathHelper.clamp(v, 0, MAX_RADIUS); enforceRingOrder(); }
    public void setOuterRingInnerRadius(int v) { this.outerRingInnerRadius = MathHelper.clamp(v, 0, MAX_RADIUS); enforceRingOrder(); }
    public void setOuterRingOuterRadius(int v) { this.outerRingOuterRadius = MathHelper.clamp(v, 0, MAX_RADIUS); enforceRingOrder(); }
    public void setInnerIconRadiusOffset(int v) { this.innerIconRadiusOffset = MathHelper.clamp(v, -MAX_OFFSET, MAX_OFFSET); }
    public void setOuterIconRadiusOffset(int v) { this.outerIconRadiusOffset = MathHelper.clamp(v, -MAX_OFFSET, MAX_OFFSET); }
    public void setUsePigmentColor(boolean v) { this.usePigmentColor = v; }
    public void setEnableTooltips(boolean v) { this.enableTooltips = v; }
    public void setCloseOnBackgroundClick(boolean v) { this.closeOnBackgroundClick = v; }
    public void setColorMode(int v) { this.colorMode = MathHelper.clamp(v, 0, 2); }

    // =============== АВТО-СОГЛАСОВАНИЕ КОЛЕЦ ===============
    private void enforceRingOrder() {
        // Шаг 1: убедимся, что все ≥ 0 и ≤ MAX_RADIUS (уже сделано в сеттерах, но на всякий)
        innerRingInnerRadius = MathHelper.clamp(innerRingInnerRadius, 0, MAX_RADIUS);
        innerRingOuterRadius = MathHelper.clamp(innerRingOuterRadius, 0, MAX_RADIUS);
        outerRingInnerRadius = MathHelper.clamp(outerRingInnerRadius, 0, MAX_RADIUS);
        outerRingOuterRadius = MathHelper.clamp(outerRingOuterRadius, 0, MAX_RADIUS);

        // Шаг 2: установим логический порядок
        // Внутреннее кольцо: innerIn ≤ innerOut
        innerRingOuterRadius = Math.max(innerRingInnerRadius, innerRingOuterRadius);
        // Внешнее кольцо начинается не раньше конца внутреннего
        outerRingInnerRadius = Math.max(innerRingOuterRadius, outerRingInnerRadius);
        // Внешнее кольцо: outerIn ≤ outerOut
        outerRingOuterRadius = Math.max(outerRingInnerRadius, outerRingOuterRadius);
    }

    // =============== ОСТАЛЬНОЕ ===============
    public HexSBMConfig() {}

    public void copyFrom(HexSBMConfig other) {
        // ... (оставь как есть — он копирует напрямую, что OK при загрузке)
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
        this.activeLighten = other.activeLighten;
        this.hoverLighten = other.hoverLighten;
        this.inactiveLighten = other.inactiveLighten;
        this.inactiveDarken = other.inactiveDarken;
        this.innerOuterActiveLighten = other.innerOuterActiveLighten;
        this.segmentResolution = other.segmentResolution;
        this.patternTooltipLineIndex = other.patternTooltipLineIndex;
        this.minTooltipLinesForPattern = other.minTooltipLinesForPattern;
        this.visualNbtTags = new ArrayList<>(other.visualNbtTags);
        this.usePigmentColor = other.usePigmentColor;
        this.uiBaseColor = other.uiBaseColor;
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