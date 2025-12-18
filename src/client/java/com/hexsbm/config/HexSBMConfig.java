package com.hexsbm.config;

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

    // === Цветовые поправки (lighten/darken) ===
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

    // === NBT-теги для иконок ===
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
        HexSBMConfig def = new HexSBMConfig();
        this.centerX = def.centerX;
        this.centerY = def.centerY;
        this.innerRingInnerRadius = def.innerRingInnerRadius;
        this.innerRingOuterRadius = def.innerRingOuterRadius;
        this.outerRingInnerRadius = def.outerRingInnerRadius;
        this.outerRingOuterRadius = def.outerRingOuterRadius;
        this.innerIconRadiusOffset = def.innerIconRadiusOffset;
        this.outerIconRadiusOffset = def.outerIconRadiusOffset;
        this.activeAlpha = def.activeAlpha;
        this.hoverAlpha = def.hoverAlpha;
        this.inactiveAlpha = def.inactiveAlpha;
        this.enableTooltips = def.enableTooltips;
        this.closeOnBackgroundClick = def.closeOnBackgroundClick;
        this.activeLighten = def.activeLighten;
        this.hoverLighten = def.hoverLighten;
        this.inactiveLighten = def.inactiveLighten;
        this.inactiveDarken = def.inactiveDarken;
        this.innerOuterActiveLighten = def.innerOuterActiveLighten;
        this.segmentResolution = def.segmentResolution;
        this.patternTooltipLineIndex = def.patternTooltipLineIndex;
        this.minTooltipLinesForPattern = def.minTooltipLinesForPattern;
        this.visualNbtTags = def.visualNbtTags;
        this.usePigmentColor = def.usePigmentColor;
        this.uiBaseColor = def.uiBaseColor;
    }
    
    public HexSBMConfig copy() {
        HexSBMConfig c = new HexSBMConfig();
        c.copyFrom(this);
        return c;
    }
}