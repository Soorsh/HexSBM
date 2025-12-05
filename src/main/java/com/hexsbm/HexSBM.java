package com.hexsbm;

import com.hexsbm.network.ChangeSpellbookPagePacket;
import com.hexsbm.network.UpdateGroupIconPacket;
import com.hexsbm.network.UpdatePageIconPacket;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Основной класс мода HexSBM.
 * Регистрирует сетевые пакеты и предоставляет общие утилиты.
 */
public class HexSBM implements ModInitializer {
    public static final String MOD_ID = "hexsbm";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * Создаёт {@link Identifier} с пространством имён {@value MOD_ID}.
     */
    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        UpdatePageIconPacket.register();
        UpdateGroupIconPacket.register();
        ChangeSpellbookPagePacket.register();
    }
}