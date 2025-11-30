package com.hexsbm;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.buffer.Unpooled;

/**
 * Клиентская часть мода HexSBM.
 * Регистрирует хоткеи и отправляет сетевые пакеты на сервер.
 */
@Environment(EnvType.CLIENT)
public class HexSBMClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("HexSBM");

    private static final Identifier CHANGE_SPELLBOOK_PAGE_PACKET = new Identifier("hexsbm", "change_spellbook_page");

    @Override
    public void onInitializeClient() {
        com.hexsbm.keybinds.KeyBindManager.registerKeyBinds();
    }

    /**
     * Отправляет запрос на изменение страницы заклинаний.
     *
     * @param hand рука, в которой книга (MAIN_HAND или OFF_HAND)
     * @param newPage целевой номер страницы (1–64)
     */
    public static void sendChangeSpellbookPage(Hand hand, int newPage) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeEnumConstant(hand);
        buf.writeInt(newPage);
        ClientPlayNetworking.send(CHANGE_SPELLBOOK_PAGE_PACKET, buf);
    }
}