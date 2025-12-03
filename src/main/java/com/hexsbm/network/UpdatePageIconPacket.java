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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;

public class UpdatePageIconPacket {
    private static final Identifier ID = HexSBM.id("update_page_icon");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ID, UpdatePageIconPacket::handle);
    }

    private static void handle(
        MinecraftServer server,
        ServerPlayerEntity player,
        ServerPlayNetworkHandler handler,
        PacketByteBuf buf,
        PacketSender responseSender
    ) {
        try {
            Hand hand = buf.readEnumConstant(Hand.class);
            int pageIndex = buf.readInt();
            ItemStack iconStack = buf.readItemStack();

            // Валидация входных данных
            if (pageIndex < 1 || pageIndex > 64) return;

            server.execute(() -> {
                ItemStack handStack = player.getStackInHand(hand);
                if (handStack.isEmpty()) return;

                // Проверяем, что это именно Hexcasting Spellbook
                if (!Registries.ITEM.getId(handStack.getItem()).equals(new Identifier("hexcasting", "spellbook"))) {
                    return;
                }

                NbtCompound nbt = handStack.getOrCreateNbt();
                NbtCompound pageIcons = nbt.getCompound("page_icons");

                String pageKey = String.valueOf(pageIndex);
                if (iconStack.isEmpty()) {
                    pageIcons.remove(pageKey);
                } else {
                    NbtCompound iconNbt = new NbtCompound();
                    iconStack.writeNbt(iconNbt);
                    pageIcons.put(pageKey, iconNbt);
                }

                nbt.put("page_icons", pageIcons);
                // Изменения автоматически сохранятся, так как NBT мутабелен
            });
        } catch (Exception e) {
            HexSBM.LOGGER.warn("Failed to handle UpdatePageIconPacket", e);
        }
    }
}