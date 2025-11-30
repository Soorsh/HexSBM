package com.hexsbm;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ClientModInitializer;
import com.hexsbm.keybinds.KeyBindManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Сетевые импорты — без PacketByteBufs!
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import io.netty.buffer.Unpooled; // ← это есть всегда
import net.minecraft.util.Identifier;
import net.minecraft.util.Hand;

@Environment(EnvType.CLIENT)
public class HexSBMClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("hexsbm-client");

    @Override
    public void onInitializeClient() {
        KeyBindManager.registerKeyBinds();
    }

    public static void sendChangeSpellbookPage(Hand hand, int newPage) {
        LOGGER.info("Клиент: отправка пакета — рука={}, страница={}", hand, newPage);
        
        // Создаём буфер через Netty — работает в 1.20.1
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeEnumConstant(hand);
        buf.writeInt(newPage);
        
        ClientPlayNetworking.send(new Identifier("hexsbm", "change_spellbook_page"), buf);
    }
}