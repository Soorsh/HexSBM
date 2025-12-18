package com.hexsbm.screen.ui;

import com.hexsbm.config.HexSBMConfig;

public class ColorScheme {

    private final int pigmentColor;
    private final HexSBMConfig config;

    public ColorScheme(int pigmentColor, HexSBMConfig config) {
        this.pigmentColor = pigmentColor;
        this.config = config;
    }

    public int getOuterInnerColor(boolean cur, boolean hover) {
        int alpha = cur ? config.activeAlpha : hover ? config.hoverAlpha : config.inactiveAlpha;
        int rgb = cur || hover
            ? lighten(pigmentColor, config.activeLighten)
            : darken(pigmentColor, config.inactiveDarken);
        return mkColor(alpha, rgb);
    }

    public int getOuterOuterColor(boolean cur, boolean hover) {
        int alpha = cur ? config.activeAlpha : hover ? config.hoverAlpha : config.inactiveAlpha;
        int rgb = cur || hover
            ? lighten(pigmentColor, config.activeLighten)
            : lighten(pigmentColor, config.inactiveLighten);
        return mkColor(alpha, rgb);
    }

    public int getInnerInnerColor(boolean cur, boolean hover) {
        int alpha = cur ? config.activeAlpha : hover ? config.hoverAlpha : config.inactiveAlpha;
        return mkColor(alpha, lighten(pigmentColor, config.inactiveLighten));
    }

    public int getInnerOuterColor(boolean cur, boolean hover) {
        int alpha = cur ? config.activeAlpha : hover ? config.hoverAlpha : config.inactiveAlpha;
        if (cur) {
            return mkColor(alpha, lighten(pigmentColor, config.innerOuterActiveLighten));
        } else if (hover) {
            return mkColor(alpha, lighten(pigmentColor, config.hoverLighten));
        } else {
            return mkColor(alpha, darken(pigmentColor, config.inactiveDarken));
        }
    }

    // --- Вспомогательные методы ---

    private int mkColor(int alpha, int rgb) {
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }

    private int lighten(int color, float f) {
        float r = Math.min(1, ((color >> 16) & 0xFF) / 255f + f);
        float g = Math.min(1, ((color >> 8) & 0xFF) / 255f + f);
        float b = Math.min(1, (color & 0xFF) / 255f + f);
        return (color & 0xFF000000) | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }

    private int darken(int color, float f) {
        float r = Math.max(0, ((color >> 16) & 0xFF) / 255f - f);
        float g = Math.max(0, ((color >> 8) & 0xFF) / 255f - f);
        float b = Math.max(0, (color & 0xFF) / 255f - f);
        return (color & 0xFF000000) | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }
}