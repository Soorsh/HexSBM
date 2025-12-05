package com.hexsbm;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.item.ItemStack;
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

    private static final Identifier UPDATE_PAGE_ICON_PACKET = new Identifier("hexsbm", "update_page_icon");

    /**
     * Отправляет запрос на обновление иконки страницы.
     *
     * @param hand рука, в которой книга
     * @param pageIndex номер страницы (1–64)
     * @param iconStack предмет-иконка (может быть EMPTY для удаления)
     */
    public static void sendUpdatePageIcon(Hand hand, int pageIndex, ItemStack iconStack) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeEnumConstant(hand);
        buf.writeInt(pageIndex);
        buf.writeItemStack(iconStack); // ← Fabric умеет это
        ClientPlayNetworking.send(UPDATE_PAGE_ICON_PACKET, buf);
    }

    private static final Identifier UPDATE_GROUP_ICON_PACKET = new Identifier("hexsbm", "update_group_icon");

    /**
     * Отправляет запрос на обновление иконки группы.
     *
     * @param hand рука, в которой книга
     * @param groupIndex индекс группы (0–7)
     * @param iconStack предмет-иконка (может быть EMPTY для удаления)
     */
    public static void sendUpdateGroupIcon(Hand hand, int groupIndex, ItemStack iconStack) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeEnumConstant(hand);
        buf.writeInt(groupIndex);
        buf.writeItemStack(iconStack);
        ClientPlayNetworking.send(UPDATE_GROUP_ICON_PACKET, buf);
    }
}