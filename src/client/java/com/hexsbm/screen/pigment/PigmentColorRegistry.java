package com.hexsbm.screen.pigment;

import java.util.Map;
import java.util.HashMap;

public class PigmentColorRegistry {
    private static final Map<String, Integer> COLORS = new HashMap<>();

    static {
        // Pride
        COLORS.put("hexcasting:pride_colorizer_agender", 0xFF16A10C);
        COLORS.put("hexcasting:pride_colorizer_aroace", 0xFF7210BC);
        COLORS.put("hexcasting:pride_colorizer_aromantic", 0xFF16A10C);
        COLORS.put("hexcasting:pride_colorizer_asexual", 0xFF333233);
        COLORS.put("hexcasting:pride_colorizer_bisexual", 0xFFDB45FF);
        COLORS.put("hexcasting:pride_colorizer_demiboy", 0xFF9A9FA1);
        COLORS.put("hexcasting:pride_colorizer_demigirl", 0xFF9A9FA1);
        COLORS.put("hexcasting:pride_colorizer_gay", 0xFFD82F3A);
        COLORS.put("hexcasting:pride_colorizer_genderfluid", 0xFFFBACF9);
        COLORS.put("hexcasting:pride_colorizer_genderqueer", 0xFFCA78EF);
        COLORS.put("hexcasting:pride_colorizer_intersex", 0xFFEBF367);
        COLORS.put("hexcasting:pride_colorizer_lesbian", 0xFFD82F3A);
        COLORS.put("hexcasting:pride_colorizer_nonbinary", 0xFFEBF367);
        COLORS.put("hexcasting:pride_colorizer_pansexual", 0xFFE278EF);
        COLORS.put("hexcasting:pride_colorizer_plural", 0xFF30C69F);
        COLORS.put("hexcasting:pride_colorizer_transgender", 0xFFEB92EA);

        // Dyes
        COLORS.put("hexcasting:dye_colorizer_white", 0xFFFFFFFF);
        COLORS.put("hexcasting:dye_colorizer_orange", 0xFFFFA700);
        COLORS.put("hexcasting:dye_colorizer_magenta", 0xFFFF55FF);
        COLORS.put("hexcasting:dye_colorizer_light_blue", 0xFF55FFFF);
        COLORS.put("hexcasting:dye_colorizer_yellow", 0xFFFFFF55);
        COLORS.put("hexcasting:dye_colorizer_lime", 0xFF55FF55);
        COLORS.put("hexcasting:dye_colorizer_pink", 0xFFFFB6FF);
        COLORS.put("hexcasting:dye_colorizer_gray", 0xFF555555);
        COLORS.put("hexcasting:dye_colorizer_light_gray", 0xFFAAAAAA);
        COLORS.put("hexcasting:dye_colorizer_cyan", 0xFF55FFFF);
        COLORS.put("hexcasting:dye_colorizer_purple", 0xFFAA00AA);
        COLORS.put("hexcasting:dye_colorizer_blue", 0xFF0000AA);
        COLORS.put("hexcasting:dye_colorizer_brown", 0xFF884400);
        COLORS.put("hexcasting:dye_colorizer_green", 0xFF00AA00);
        COLORS.put("hexcasting:dye_colorizer_red", 0xFFAA0000);
        COLORS.put("hexcasting:dye_colorizer_black", 0xFF323232);

        // Special
        COLORS.put("hexcasting:amethyst_colorizer", 0xFFAB65EB);
        COLORS.put("hexcasting:amethyst_and_copper_colorizer", 0xFF54398A);
    }

    public static int getColor(String id) {
        return COLORS.getOrDefault(id, 0xFFFFFFFF);
    }
}