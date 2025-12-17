package com.hexsbm.config;

public class HexSBMConfig {
    public float centerX = 0.5f;
    public float centerY = 0.5f;
    public int innerRingInnerRadius = 30;
    public int innerRingOuterRadius = 60;
    public int outerRingInnerRadius = 70;
    public int outerRingOuterRadius = 110;
    public int activeAlpha = 0x99;
    public int hoverAlpha = 0x90;
    public int inactiveAlpha = 0x80;
    public boolean enableTooltips = true;
    public boolean closeOnBackgroundClick = true;
    public int innerIconRadiusOffset = 0;
    public int outerIconRadiusOffset = 0;

    public HexSBMConfig copy() {
        HexSBMConfig c = new HexSBMConfig();
        c.centerX = this.centerX;
        c.centerY = this.centerY;
        c.innerRingInnerRadius = this.innerRingInnerRadius;
        c.innerRingOuterRadius = this.innerRingOuterRadius;
        c.outerRingInnerRadius = this.outerRingInnerRadius;
        c.outerRingOuterRadius = this.outerRingOuterRadius;
        c.activeAlpha = this.activeAlpha;
        c.hoverAlpha = this.hoverAlpha;
        c.inactiveAlpha = this.inactiveAlpha;
        c.enableTooltips = this.enableTooltips;
        c.closeOnBackgroundClick = this.closeOnBackgroundClick;
        c.innerIconRadiusOffset = this.innerIconRadiusOffset;
        c.outerIconRadiusOffset = this.outerIconRadiusOffset;
        return c;
    }

    public void resetToDefault() {
        centerX = 0.5f;
        centerY = 0.5f;
        innerRingInnerRadius = 30;
        innerRingOuterRadius = 60;
        outerRingInnerRadius = 70;
        outerRingOuterRadius = 110;
        activeAlpha = 0x99;
        hoverAlpha = 0x90;
        inactiveAlpha = 0x80;
        enableTooltips = true;
        closeOnBackgroundClick = true;
        innerIconRadiusOffset = 0;
        outerIconRadiusOffset = 0;
    }
}