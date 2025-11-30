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
    private static final boolean DEBUG = false; // ← поставь true при отладке

    public static void register() {
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

        // Валидация страницы
        if (newPage < 1 || newPage > 64) {
            HexSBM.LOGGER.warn("Отклонено: страница {} вне диапазона [1, 64]", newPage);
            return;
        }

        server.execute(() -> {
            ItemStack stack = player.getStackInHand(hand);
            Identifier itemId = net.minecraft.registry.Registries.ITEM.getId(stack.getItem());

            if (DEBUG) {
                HexSBM.LOGGER.info("Пакет: игрок={}, рука={}, страница={}, предмет={}",
                    player.getName().getString(), hand, newPage, itemId);
            }

            if (!itemId.equals(SPELLBOOK_ID)) {
                HexSBM.LOGGER.warn("Отклонено: предмет не является spellbook ({}", itemId);
                return;
            }

            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.putInt("page_idx", newPage);

            // Обновляем customName
            Text pageName = null;
            if (nbt.contains("page_names", NbtElement.COMPOUND_TYPE)) {
                NbtCompound pageNames = nbt.getCompound("page_names");
                String key = String.valueOf(newPage);
                if (pageNames.contains(key, NbtElement.STRING_TYPE)) {
                    String json = pageNames.getString(key);
                    if (json != null && !json.trim().isEmpty()) {
                        try {
                            pageName = Text.Serializer.fromJson(json);
                        } catch (Exception e) {
                            HexSBM.LOGGER.warn("Ошибка парсинга имени страницы {}: {}", newPage, e.getMessage());
                        }
                    }
                }
            }

            if (pageName != null) {
                stack.setCustomName(pageName);
            } else {
                stack.removeCustomName();
            }

            player.setStackInHand(hand, stack);
        });
    }
}