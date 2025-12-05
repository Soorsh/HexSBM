package com.hexsbm.network;

import com.hexsbm.HexSBM;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;

public class UpdateGroupIconPacket {
    private static final net.minecraft.util.Identifier ID = HexSBM.id("update_group_icon");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ID, UpdateGroupIconPacket::handle);
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
            int groupIndex = buf.readInt();
            ItemStack iconStack = buf.readItemStack();

            // Валидация: группы 0–7
            if (groupIndex < 0 || groupIndex > 7) return;

            server.execute(() -> {
                ItemStack handStack = player.getStackInHand(hand);
                if (handStack.isEmpty()) return;

                if (!Registries.ITEM.getId(handStack.getItem()).equals(new net.minecraft.util.Identifier("hexcasting", "spellbook"))) {
                    return;
                }

                NbtCompound nbt = handStack.getOrCreateNbt();
                NbtCompound groupIcons = nbt.getCompound("group_icons");

                String groupKey = String.valueOf(groupIndex);
                if (iconStack.isEmpty()) {
                    groupIcons.remove(groupKey);
                } else {
                    NbtCompound iconNbt = new NbtCompound();
                    iconStack.writeNbt(iconNbt);
                    groupIcons.put(groupKey, iconNbt);
                }

                nbt.put("group_icons", groupIcons);
            });
        } catch (Exception e) {
            HexSBM.LOGGER.warn("Failed to handle UpdateGroupIconPacket", e);
        }
    }
}