package com.hexsbm.network;

import com.hexsbm.HexSBM;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.item.ItemStack;

public class ChangeSpellbookPagePacket {
    public static final Identifier ID = HexSBM.id("change_spellbook_page");
    private static final Identifier SPELLBOOK_ID = new Identifier("hexcasting", "spellbook");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ID, ChangeSpellbookPagePacket::handle);
    }

    private static void handle(
        MinecraftServer server,
        ServerPlayerEntity player,
        ServerPlayNetworkHandler handler,
        PacketByteBuf buf,
        PacketSender sender
    ) {
        Hand hand = buf.readEnumConstant(Hand.class);
        int page = buf.readInt();

        if (page < 1 || page > 64) return;

        server.execute(() -> {
            ItemStack stack = player.getStackInHand(hand);
            if (!net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).equals(SPELLBOOK_ID)) return;

            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.putInt("page_idx", page);

            // Обновление custom name из page_names
            Text name = null;
            if (nbt.contains("page_names", NbtElement.COMPOUND_TYPE)) {
                String json = nbt.getCompound("page_names").getString(String.valueOf(page));
                if (!json.isEmpty()) {
                    try {
                        name = Text.Serializer.fromJson(json);
                    } catch (Exception ignored) {}
                }
            }

            if (name != null) {
                stack.setCustomName(name);
            } else {
                stack.removeCustomName();
            }

            player.setStackInHand(hand, stack);
        });
    }
}