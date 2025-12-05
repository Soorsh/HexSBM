package com.hexsbm.screen.pigment;

import java.util.HashMap;
import java.util.Map;

public class PigmentColorRegistry {
    private static final Map<String, Integer> PIGMENT_COLORS = new HashMap<>();

    static {
        // === Pride Pigments ===
        PIGMENT_COLORS.put("hexcasting:pride_colorizer_agender", 0xFF16A10C);
        PIGMENT_COLORS.put("hexcasting:pride_colorizer_aroace", 0xFF7210BC);
        PIGMENT_COLORS.put("hexcasting:pride_colorizer_aromantic", 0xFF16A10C);
        PIGMENT_COLORS.put("hexcasting:pride_colorizer_asexual", 0xFF333233);
        PIGMENT_COLORS.put("hexcasting:pride_colorizer_bisexual", 0xFFDB45FF);
        PIGMENT_COLORS.put("hexcasting:pride_colorizer_demiboy", 0xFF9A9FA1);
        PIGMENT_COLORS.put("hexcasting:pride_colorizer_demigirl", 0xFF9A9FA1);
        PIGMENT_COLORS.put("hexcasting:pride_colorizer_gay", 0xFFD82F3A);
        PIGMENT_COLORS.put("hexcasting:pride_colorizer_genderfluid", 0xFFFBACF9);
        PIGMENT_COLORS.put("hexcasting:pride_colorizer_genderqueer", 0xFFCA78EF);
        PIGMENT_COLORS.put("hexcasting:pride_colorizer_intersex", 0xFFEBF367);
        PIGMENT_COLORS.put("hexcasting:pride_colorizer_lesbian", 0xFFD82F3A);
        PIGMENT_COLORS.put("hexcasting:pride_colorizer_nonbinary", 0xFFEBF367);
        PIGMENT_COLORS.put("hexcasting:pride_colorizer_pansexual", 0xFFE278EF);
        PIGMENT_COLORS.put("hexcasting:pride_colorizer_plural", 0xFF30C69F);
        PIGMENT_COLORS.put("hexcasting:pride_colorizer_transgender", 0xFFEB92EA);

        // === Dye Pigments ===
        PIGMENT_COLORS.put("hexcasting:dye_colorizer_white", 0xFFFFFFFF);
        PIGMENT_COLORS.put("hexcasting:dye_colorizer_orange", 0xFFFFA700);
        PIGMENT_COLORS.put("hexcasting:dye_colorizer_magenta", 0xFFFF55FF);
        PIGMENT_COLORS.put("hexcasting:dye_colorizer_light_blue", 0xFF55FFFF);
        PIGMENT_COLORS.put("hexcasting:dye_colorizer_yellow", 0xFFFFFF55);
        PIGMENT_COLORS.put("hexcasting:dye_colorizer_lime", 0xFF55FF55);
        PIGMENT_COLORS.put("hexcasting:dye_colorizer_pink", 0xFFFFB6FF);
        PIGMENT_COLORS.put("hexcasting:dye_colorizer_gray", 0xFF555555);
        PIGMENT_COLORS.put("hexcasting:dye_colorizer_light_gray", 0xFFAAAAAA);
        PIGMENT_COLORS.put("hexcasting:dye_colorizer_cyan", 0xFF55FFFF);
        PIGMENT_COLORS.put("hexcasting:dye_colorizer_purple", 0xFFAA00AA);
        PIGMENT_COLORS.put("hexcasting:dye_colorizer_blue", 0xFF0000AA);
        PIGMENT_COLORS.put("hexcasting:dye_colorizer_brown", 0xFF884400);
        PIGMENT_COLORS.put("hexcasting:dye_colorizer_green", 0xFF00AA00);
        PIGMENT_COLORS.put("hexcasting:dye_colorizer_red", 0xFFAA0000);
        PIGMENT_COLORS.put("hexcasting:dye_colorizer_black", 0xFF323232);

        // === Special ===
        PIGMENT_COLORS.put("hexcasting:amethyst_colorizer", 0xFFAB65EB);
        PIGMENT_COLORS.put("hexcasting:amethyst_and_copper_colorizer", 0xFF54398A);
    }

    public static int getColor(String pigmentId, int defaultColor) {
        return PIGMENT_COLORS.getOrDefault(pigmentId, defaultColor);
    }

    public static int getColor(String pigmentId) {
        return getColor(pigmentId, 0xFFFFFFFF);
    }
}