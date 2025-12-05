package com.hexsbm;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.buffer.Unpooled;

public class HexSBMClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("HexSBM");

    private static final Identifier
        CHANGE_PAGE = new Identifier("hexsbm", "change_spellbook_page"),
        UPDATE_PAGE_ICON = new Identifier("hexsbm", "update_page_icon"),
        UPDATE_GROUP_ICON = new Identifier("hexsbm", "update_group_icon");

    @Override
    public void onInitializeClient() {
        com.hexsbm.keybinds.KeyBindManager.registerKeyBinds();
    }

    public static void sendChangeSpellbookPage(Hand hand, int page) {
        send(CHANGE_PAGE, buf -> {
            buf.writeEnumConstant(hand);
            buf.writeInt(page);
        });
    }

    public static void sendUpdatePageIcon(Hand hand, int page, ItemStack icon) {
        send(UPDATE_PAGE_ICON, buf -> {
            buf.writeEnumConstant(hand);
            buf.writeInt(page);
            buf.writeItemStack(icon);
        });
    }

    public static void sendUpdateGroupIcon(Hand hand, int group, ItemStack icon) {
        send(UPDATE_GROUP_ICON, buf -> {
            buf.writeEnumConstant(hand);
            buf.writeInt(group);
            buf.writeItemStack(icon);
        });
    }

    private static void send(Identifier id, java.util.function.Consumer<PacketByteBuf> writer) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        writer.accept(buf);
        ClientPlayNetworking.send(id, buf);
    }
}