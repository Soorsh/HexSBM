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

public class UpdatePageIconPacket {
    private static final Identifier ID = HexSBM.id("update_page_icon");
    private static final Identifier SPELLBOOK_ID = new Identifier("hexcasting", "spellbook");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ID, UpdatePageIconPacket::handle);
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
            int page = buf.readInt();
            ItemStack icon = buf.readItemStack();

            if (page < 1 || page > 64) return;

            server.execute(() -> {
                ItemStack stack = player.getStackInHand(hand);
                if (stack.isEmpty() || !Registries.ITEM.getId(stack.getItem()).equals(SPELLBOOK_ID)) return;

                NbtCompound nbt = stack.getOrCreateNbt();
                NbtCompound icons = nbt.getCompound("page_icons");
                String key = String.valueOf(page);

                if (icon.isEmpty()) {
                    icons.remove(key);
                } else {
                    icons.put(key, icon.writeNbt(new NbtCompound()));
                }

                nbt.put("page_icons", icons);
            });
        } catch (Exception e) {
            HexSBM.LOGGER.warn("Bad UpdatePageIconPacket from {}", player.getName().getString());
        }
    }
}