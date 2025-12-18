package com.hexsbm.screen.nbt;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.client.item.TooltipContext;
import com.hexsbm.config.HexSBMConfig;
import java.util.*;

public final class SpellbookNbtManager {

    private SpellbookNbtManager() {}

    public static int getPage(ItemStack book) {
        NbtCompound nbt = book.getNbt();
        return nbt != null && nbt.contains("page_idx", NbtElement.INT_TYPE) ? nbt.getInt("page_idx") : 1;
    }

    public static ItemStack getPageIcon(ItemStack book, int page) {
        return getIcon(book, "page_icons", page);
    }

    public static ItemStack getGroupIcon(ItemStack book, int group) {
        return getIcon(book, "group_icons", group);
    }

    public static ItemStack getIcon(ItemStack book, String key, int idx) {
        NbtCompound nbt = book.getNbt();
        if (nbt == null || !nbt.contains(key, NbtElement.COMPOUND_TYPE)) return ItemStack.EMPTY;
        NbtCompound map = nbt.getCompound(key);
        String s = String.valueOf(idx);
        if (!map.contains(s, NbtElement.COMPOUND_TYPE)) return ItemStack.EMPTY;
        try {
            return ItemStack.fromNbt(map.getCompound(s));
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    public static void updateIcon(ItemStack book, String key, int idx, ItemStack icon) {
        NbtCompound nbt = book.getOrCreateNbt();
        NbtCompound map = nbt.getCompound(key);
        String s = String.valueOf(idx);
        if (!icon.isEmpty()) {
            map.put(s, icon.writeNbt(new NbtCompound()));
        } else {
            map.remove(s);
        }
        nbt.put(key, map);
    }

    public static ItemStack makeIconOnly(ItemStack src, HexSBMConfig config) {
        if (src.isEmpty()) return ItemStack.EMPTY;
        ItemStack icon = new ItemStack(src.getItem(), 1);
        NbtCompound tag = src.getNbt();
        if (tag != null) {
            NbtCompound clean = new NbtCompound();
            for (String k : config.visualNbtTags) {
                if (tag.contains(k)) {
                    clean.put(k, tag.get(k).copy());
                }
            }
            if (!clean.isEmpty()) {
                icon.setNbt(clean);
            }
        }
        return icon;
    }

    public static ItemStack createIconFromHotbar(ClientPlayerEntity player, HexSBMConfig config) {
        PlayerInventory inv = player.getInventory();
        ItemStack src = inv.getStack(inv.selectedSlot);
        return makeIconOnly(src, config);
    }

    public static String getCustomPageName(ItemStack book, int page) {
        NbtCompound nbt = book.getNbt();
        if (nbt == null || !nbt.contains("page_names", NbtElement.COMPOUND_TYPE)) return null;
        String json = nbt.getCompound("page_names").getString(String.valueOf(page));
        try {
            Text t = Text.Serializer.fromJson(json);
            return t != null ? t.getString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static List<Text> getPatternTooltip(ItemStack book, int page, ClientPlayerEntity player, HexSBMConfig config) {
        NbtCompound nbt = book.getNbt();
        if (nbt == null || !nbt.contains("pages", NbtElement.COMPOUND_TYPE)) return Collections.emptyList();
        NbtCompound pages = nbt.getCompound("pages");
        if (!pages.contains(String.valueOf(page), NbtElement.COMPOUND_TYPE)) return Collections.emptyList();

        ItemStack fake = book.copy();
        fake.getOrCreateNbt().putInt("page_idx", page);
        List<Text> tt = fake.getTooltip(player, TooltipContext.Default.BASIC);
        return tt.size() >= config.minTooltipLinesForPattern
            ? List.of(tt.get(config.patternTooltipLineIndex))
            : Collections.emptyList();
    }
}