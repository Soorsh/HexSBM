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
            // Единый цвет — используем outer как базовый
            if (cur) {
                return lighten(pigmentColor, config.outerActiveLighten);
            } else if (hover) {
                return lighten(pigmentColor, config.outerHoverLighten);
            } else {
                return lighten(pigmentColor, config.outerInactiveLighten);
            }
        } else {
            float lightenAmount, darkenAmount;
            if (isInner) {
                lightenAmount = isOuterEdge ? config.innerActiveLighten : config.innerInactiveLighten;
                darkenAmount = config.innerInactiveDarken;
            } else {
                lightenAmount = config.outerActiveLighten;
                darkenAmount = config.outerInactiveDarken;
            }

            if (cur) {
                return lighten(pigmentColor, isInner && isOuterEdge ? config.innerActiveLighten : (isInner ? config.innerActiveLighten : config.outerActiveLighten));
            } else if (hover) {
                return lighten(pigmentColor, isInner ? config.innerHoverLighten : config.outerHoverLighten);
            } else {
                // Для неактивного: innerInner/outerOuter → lighten, innerOuter/outerInner → darken
                if ((isInner && !isOuterEdge) || (!isInner && isOuterEdge)) {
                    return lighten(pigmentColor, isInner ? config.innerInactiveLighten : config.outerInactiveLighten);
                } else {
                    return darken(pigmentColor, isInner ? config.innerInactiveDarken : config.outerInactiveDarken);
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