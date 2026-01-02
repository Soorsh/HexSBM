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
        int rgb = getRgbForState(cur, hover, false, false);
        return mkColor(alpha, rgb);
    }

    public int getOuterOuterColor(boolean cur, boolean hover) {
        int alpha = cur ? config.activeAlpha : hover ? config.hoverAlpha : config.inactiveAlpha;
        int rgb = getRgbForState(cur, hover, false, true);
        return mkColor(alpha, rgb);
    }

    public int getInnerInnerColor(boolean cur, boolean hover) {
        int alpha = cur ? config.activeAlpha : hover ? config.hoverAlpha : config.inactiveAlpha;
        int rgb = getRgbForState(cur, hover, true, false);
        return mkColor(alpha, rgb);
    }

    public int getInnerOuterColor(boolean cur, boolean hover) {
        int alpha = cur ? config.activeAlpha : hover ? config.hoverAlpha : config.inactiveAlpha;
        int rgb = getRgbForState(cur, hover, true, true);
        return mkColor(alpha, rgb);
    }

    private int getRgbForState(boolean cur, boolean hover, boolean isInner, boolean isOuterEdge) {
        if (config.disableGradient) {
            // Единый цвет для всех — выбираем логику, близкую к outer-outer
            if (cur) {
                return lighten(pigmentColor, config.activeLighten);
            } else if (hover) {
                return lighten(pigmentColor, config.hoverLighten);
            } else {
                // Для неактивного — используем inactiveLighten (мягче, чем darken)
                return lighten(pigmentColor, config.inactiveLighten);
            }
        } else {
            // Оригинальная логика
            if (isInner) {
                if (isOuterEdge) { // innerOuter
                    if (cur) {
                        return lighten(pigmentColor, config.innerOuterActiveLighten);
                    } else if (hover) {
                        return lighten(pigmentColor, config.hoverLighten);
                    } else {
                        return darken(pigmentColor, config.inactiveDarken);
                    }
                } else { // innerInner
                    return lighten(pigmentColor, config.inactiveLighten);
                }
            } else {
                if (isOuterEdge) { // outerOuter
                    return cur || hover
                        ? lighten(pigmentColor, config.activeLighten)
                        : lighten(pigmentColor, config.inactiveLighten);
                } else { // outerInner
                    return cur || hover
                        ? lighten(pigmentColor, config.activeLighten)
                        : darken(pigmentColor, config.inactiveDarken);
                }
            }
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