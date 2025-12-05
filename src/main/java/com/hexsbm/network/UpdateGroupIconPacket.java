package com.hexsbm.network;

import com.hexsbm.HexSBM;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.item.ItemStack;

public class UpdateGroupIconPacket {
    private static final Identifier ID = HexSBM.id("update_group_icon");
    private static final Identifier SPELLBOOK_ID = new Identifier("hexcasting", "spellbook");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ID, UpdateGroupIconPacket::handle);
    }

    private static void handle(
        MinecraftServer server,
        ServerPlayerEntity player,
        ServerPlayNetworkHandler handler,
        PacketByteBuf buf,
        PacketSender sender
    ) {
        try {
            Hand hand = buf.readEnumConstant(Hand.class);
            int group = buf.readInt();
            ItemStack icon = buf.readItemStack();

            if (group < 0 || group > 7) return;

            server.execute(() -> {
                ItemStack stack = player.getStackInHand(hand);
                if (stack.isEmpty() || !Registries.ITEM.getId(stack.getItem()).equals(SPELLBOOK_ID)) return;

                NbtCompound nbt = stack.getOrCreateNbt();
                NbtCompound icons = nbt.getCompound("group_icons");
                String key = String.valueOf(group);

                if (icon.isEmpty()) {
                    icons.remove(key);
                } else {
                    icons.put(key, icon.writeNbt(new NbtCompound()));
                }

                nbt.put("group_icons", icons);
            });
        } catch (Exception e) {
            HexSBM.LOGGER.warn("Bad UpdateGroupIconPacket from {}", player.getName().getString());
        }
    }
}