package com.hexsbm.network;

import com.hexsbm.HexSBM;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.item.ItemStack;

public class ChangeSpellbookPagePacket {
    public static final Identifier ID = HexSBM.id("change_spellbook_page");

    public static void register() {
        HexSBM.LOGGER.info("Регистрация пакета change_spellbook_page");
        ServerPlayNetworking.registerGlobalReceiver(ID, ChangeSpellbookPagePacket::handle);
    }

    private static void handle(
        MinecraftServer server,
        ServerPlayerEntity player,
        ServerPlayNetworkHandler handler,
        PacketByteBuf buf,
        PacketSender responseSender
    ) {
        Hand hand = buf.readEnumConstant(Hand.class);
        int newPage = buf.readInt();

        server.execute(() -> {
            ItemStack stack = player.getStackInHand(hand);
            Identifier itemId = net.minecraft.registry.Registries.ITEM.getId(stack.getItem());
            HexSBM.LOGGER.info("Получен пакет: игрок={}, рука={}, страница={}, предмет={}", 
                player.getName().getString(), hand, newPage, itemId);

            if (!itemId.getNamespace().equals("hexcasting") || !itemId.getPath().equals("spellbook")) {
                HexSBM.LOGGER.warn("Отклонено: предмет не является spellbook");
                return;
            }

            stack.getOrCreateNbt().putInt("page_idx", newPage);
            player.setStackInHand(hand, stack);
            HexSBM.LOGGER.info("Страница успешно обновлена!");
        });
    }
}