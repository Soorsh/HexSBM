package com.hexsbm.screen;

import com.hexsbm.screen.pigment.PigmentColorRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SpellBookScreen extends Screen {
    private static final Identifier SPELLBOOK_ID = new Identifier("hexcasting", "spellbook");
    private static final int GROUPS = 8, GROUP_SIZE = 8, TOTAL_PAGES = 64;
    private static final int R1_IN = 30, R1_OUT = 60, R2_IN = 70, R2_OUT = 110;

    private int pigmentColor = 0xFFFFFFFF;
    private Hand activeHand = null;
    private boolean selectionConfirmed = false;
    private int centralGroup = 0;
    private int originalPageIdx = -1;

    public SpellBookScreen() {
        super(Text.empty());
    }

    @Override
    public void init() {
        if (client == null || client.player == null) {
            close();
            return;
        }

        ClientPlayerEntity p = client.player;
        NbtCompound nbt = p.writeNbt(new NbtCompound());
        NbtCompound cc = nbt.getCompound("cardinal_components");
        if (cc.contains("hexcasting:favored_pigment", 10)) {
            NbtCompound pigment = cc.getCompound("hexcasting:favored_pigment");
            if (pigment.contains("pigment", 10)) {
                String id = pigment.getCompound("pigment").getCompound("stack").getString("id");
                pigmentColor = PigmentColorRegistry.getColor(id);
            }
        }

        ItemStack main = p.getMainHandStack();
        ItemStack off = p.getOffHandStack();

        if (!main.isEmpty() && Registries.ITEM.getId(main.getItem()).equals(SPELLBOOK_ID)) {
            activeHand = Hand.MAIN_HAND;
            originalPageIdx = getBookPage(main);
        } else if (!off.isEmpty() && Registries.ITEM.getId(off.getItem()).equals(SPELLBOOK_ID)) {
            activeHand = Hand.OFF_HAND;
            originalPageIdx = getBookPage(off);
        } else {
            close();
            return;
        }

        if (originalPageIdx != -1) {
            centralGroup = Math.max(0, Math.min(7, (originalPageIdx - 1) / GROUP_SIZE));
        }
    }

    private int getBookPage(ItemStack book) {
        NbtCompound nbt = book.getNbt();
        return nbt != null && nbt.contains("page_idx", NbtElement.INT_TYPE) ? nbt.getInt("page_idx") : 1;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        if (client == null || client.player == null) {
            close();
            return;
        }

        ItemStack book = getCurrentBook();
        int currentPage = book.isEmpty() ? 1 : getBookPage(book);
        int cx = width / 2, cy = height / 2;

        for (int i = 0; i < GROUPS; i++) {
            int page = centralGroup * GROUP_SIZE + i + 1;
            if (page > TOTAL_PAGES) continue;

            SectorAngles ang = new SectorAngles(i);
            boolean cur = page == currentPage;
            boolean hover = isPointInSegment(mx, my, cx, cy, R2_IN, R2_OUT, ang.start, ang.end);

            int innerCol = cur ? mkColor(0x85, lighten(pigmentColor, 0.15f)) :
                           hover ? mkColor(0x75, pigmentColor) :
                                   mkColor(0x65, darken(pigmentColor, 0.1f));
            int outerCol = cur ? mkColor(0xA0, lighten(pigmentColor, 0.25f)) :
                           hover ? mkColor(0x85, lighten(pigmentColor, 0.15f)) :
                                   mkColor(0x75, pigmentColor);

            fillSegment(ctx, cx, cy, R2_IN, R2_OUT, ang.start, ang.end, innerCol, outerCol);

            int x = (int)(cx + (R2_IN + R2_OUT) / 2.0 * Math.cos(ang.mid));
            int y = (int)(cy + (R2_IN + R2_OUT) / 2.0 * Math.sin(ang.mid));
            ItemStack icon = getPageIcon(book, page);
            if (!icon.isEmpty()) ctx.drawItem(icon, x - 8, y - 8);

            if (hover) {
                List<Text> tip = new ArrayList<>();
                String name = getCustomPageName(book, page);
                tip.add(Text.literal(name != null && !name.isEmpty() ? name : "Page " + page));
                tip.addAll(getPatternTooltip(book, page));
                ctx.drawTooltip(textRenderer, tip, mx, my);
            }
        }

        for (int i = 0; i < GROUPS; i++) {
            SectorAngles ang = new SectorAngles(i);
            boolean cur = i == centralGroup;
            boolean hover = isPointInSegment(mx, my, cx, cy, R1_IN, R1_OUT, ang.start, ang.end);

            int innerCol = cur ? mkColor(0x80, pigmentColor) :
                           hover ? mkColor(0x65, darken(pigmentColor, 0.1f)) :
                                   mkColor(0x55, darken(pigmentColor, 0.25f));
            int outerCol = cur ? mkColor(0x90, lighten(pigmentColor, 0.2f)) :
                           hover ? mkColor(0x75, pigmentColor) :
                                   mkColor(0x65, darken(pigmentColor, 0.1f));

            fillSegment(ctx, cx, cy, R1_IN, R1_OUT, ang.start, ang.end, innerCol, outerCol);

            int r = (R1_IN + R1_OUT) / 2;
            int x = (int)(cx + r * Math.cos(ang.mid));
            int y = (int)(cy + r * Math.sin(ang.mid));
            ItemStack icon = getGroupIcon(book, i);
            if (!icon.isEmpty()) ctx.drawItem(icon, x - 8, y - 8);
        }
    }

    private int mkColor(int alpha, int rgb) {
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (client == null || client.player == null || activeHand == null) {
            close();
            return true;
        }

        int cx = width / 2, cy = height / 2;
        int mx = (int) mouseX, my = (int) mouseY;

        if (button == 0) {
            for (int i = 0; i < GROUPS; i++) {
                SectorAngles ang = new SectorAngles(i);
                if (isPointInSegment(mx, my, cx, cy, R1_IN, R1_OUT, ang.start, ang.end)) {
                    centralGroup = i;
                    return true;
                }
            }
            for (int i = 0; i < GROUPS; i++) {
                int page = centralGroup * GROUP_SIZE + i + 1;
                if (page > TOTAL_PAGES) continue;
                SectorAngles ang = new SectorAngles(i);
                if (isPointInSegment(mx, my, cx, cy, R2_IN, R2_OUT, ang.start, ang.end)) {
                    selectionConfirmed = true;
                    com.hexsbm.HexSBMClient.sendChangeSpellbookPage(activeHand, page);
                    close();
                    return true;
                }
            }
            close();
            return true;
        }

        if (button == 1) {
            ItemStack book = getCurrentBook();
            if (book.isEmpty()) return false;

            for (int i = 0; i < GROUPS; i++) {
                SectorAngles ang = new SectorAngles(i);
                if (isPointInSegment(mx, my, cx, cy, R1_IN, R1_OUT, ang.start, ang.end)) {
                    updateGroupIcon(book, i);
                    return true;
                }
            }
            for (int i = 0; i < GROUPS; i++) {
                int page = centralGroup * GROUP_SIZE + i + 1;
                if (page > TOTAL_PAGES) continue;
                SectorAngles ang = new SectorAngles(i);
                if (isPointInSegment(mx, my, cx, cy, R2_IN, R2_OUT, ang.start, ang.end)) {
                    updatePageIcon(book, page);
                    return true;
                }
            }
        }

        return true;
    }

    private void updateGroupIcon(ItemStack book, int idx) {
        ClientPlayerEntity p = client.player;
        ItemStack src = makeIconOnly(p.getInventory().getStack(p.getInventory().selectedSlot));
        updateIcon(book, "group_icons", idx, src);
        com.hexsbm.HexSBMClient.sendUpdateGroupIcon(activeHand, idx, src);
    }

    private void updatePageIcon(ItemStack book, int page) {
        ClientPlayerEntity p = client.player;
        ItemStack src = makeIconOnly(p.getInventory().getStack(p.getInventory().selectedSlot));
        updateIcon(book, "page_icons", page, src);
        com.hexsbm.HexSBMClient.sendUpdatePageIcon(activeHand, page, src);
    }

    private void updateIcon(ItemStack book, String key, int idx, ItemStack icon) {
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

    @Override
    public void close() {
        if (client != null) client.setScreen(null);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int mods) {
        if (keyCode == GLFW.GLFW_KEY_V || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, mods);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (client == null || client.player == null || activeHand == null) {
            return false;
        }
        if (activeHand == Hand.OFF_HAND) {
            PlayerInventory inv = client.player.getInventory();
            int current = inv.selectedSlot;
            int newSlot = amount > 0 ? (current == 0 ? 8 : current - 1) : (current == 8 ? 0 : current + 1);
            inv.selectedSlot = newSlot;
            return true;
        }
        return false;
    }

    private ItemStack getCurrentBook() {
        ClientPlayerEntity p = client.player;
        if (!p.getMainHandStack().isEmpty() && Registries.ITEM.getId(p.getMainHandStack().getItem()).equals(SPELLBOOK_ID))
            return p.getMainHandStack();
        if (!p.getOffHandStack().isEmpty() && Registries.ITEM.getId(p.getOffHandStack().getItem()).equals(SPELLBOOK_ID))
            return p.getOffHandStack();
        return ItemStack.EMPTY;
    }

    private ItemStack getPageIcon(ItemStack book, int page) {
        return getIcon(book, "page_icons", page);
    }

    private ItemStack getGroupIcon(ItemStack book, int group) {
        return getIcon(book, "group_icons", group);
    }

    private ItemStack getIcon(ItemStack book, String key, int idx) {
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

    private static final Set<String> VISUAL_TAGS = Set.of(
        "Enchantments", "display", "CustomPotionColor", "Potion", "SkullOwner", "EntityTag",
        "hexcasting:pattern_data", "hexcasting:amulet_state", "hexcasting:op_code", "op_id"
    );

    private static ItemStack makeIconOnly(ItemStack src) {
        if (src.isEmpty()) return ItemStack.EMPTY;
        ItemStack icon = new ItemStack(src.getItem(), 1);
        NbtCompound tag = src.getNbt();
        if (tag != null) {
            NbtCompound clean = new NbtCompound();
            for (String k : VISUAL_TAGS) {
                if (tag.contains(k)) clean.put(k, tag.get(k).copy());
            }
            if (!clean.isEmpty()) icon.setNbt(clean);
        }
        return icon;
    }

    private boolean isPointInSegment(int px, int py, int cx, int cy, int rIn, int rOut, double a1, double a2) {
        double dx = px - cx, dy = py - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < rIn || dist > rOut) return false;

        double angle = Math.atan2(dy, dx);
        if (angle < 0) angle += 2 * Math.PI;
        double s = a1 < 0 ? a1 + 2 * Math.PI : a1;
        double e = a2 < 0 ? a2 + 2 * Math.PI : a2;

        return s < e ? (angle >= s && angle <= e) : (angle >= s || angle <= e);
    }

    private String getCustomPageName(ItemStack book, int page) {
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

    private List<Text> getPatternTooltip(ItemStack book, int page) {
        NbtCompound nbt = book.getNbt();
        if (nbt == null || !nbt.contains("pages", NbtElement.COMPOUND_TYPE)) return Collections.emptyList();
        NbtCompound pages = nbt.getCompound("pages");
        if (!pages.contains(String.valueOf(page), NbtElement.COMPOUND_TYPE)) return Collections.emptyList();

        ItemStack fake = book.copy();
        fake.getOrCreateNbt().putInt("page_idx", page);
        List<Text> tt = fake.getTooltip(client.player, TooltipContext.Default.BASIC);
        return tt.size() >= 3 ? List.of(tt.get(2)) : Collections.emptyList();
    }

    private void fillSegment(DrawContext ctx, int cx, int cy, int rIn, int rOut,
                             double a1, double a2, int cIn, int cOut) {
        Matrix4f m = ctx.getMatrices().peek().getPositionMatrix();
        Tessellator t = Tessellator.getInstance();
        var b = t.getBuffer();

        float ir = ((cIn >> 16) & 0xFF) / 255f, ig = ((cIn >> 8) & 0xFF) / 255f, ib = (cIn & 0xFF) / 255f, ia = ((cIn >> 24) & 0xFF) / 255f;
        float or = ((cOut >> 16) & 0xFF) / 255f, og = ((cOut >> 8) & 0xFF) / 255f, ob = (cOut & 0xFF) / 255f, oa = ((cOut >> 24) & 0xFF) / 255f;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int seg = Math.max(4, (int) ((a2 - a1) / (Math.PI / 30)));
        b.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        for (int i = 0; i <= seg; i++) {
            double a = MathHelper.lerp((double) i / seg, a1, a2);
            float cos = (float) Math.cos(a), sin = (float) Math.sin(a);
            b.vertex(m, cx + rOut * cos, cy + rOut * sin, 0).color(or, og, ob, oa).next();
            b.vertex(m, cx + rIn * cos, cy + rIn * sin, 0).color(ir, ig, ib, ia).next();
        }

        t.draw();
        RenderSystem.disableBlend();
    }

    private int lighten(int color, float f) {
        float r = Math.min(1, ((color >> 16) & 0xFF) / 255f + f);
        float g = Math.min(1, ((color >> 8) & 0xFF) / 255f + f);
        float b = Math.min(1, (color & 0xFF) / 255f + f);
        return (color & 0xFF000000) | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }

    private int darken(int color, float f) {
        float r = Math.max(0, ((color >> 16) & 0xFF) / 255f - f);
        float g = Math.max(0, ((color >> 8) & 0xFF) / 255f - f);
        float b = Math.max(0, (color & 0xFF) / 255f - f);
        return (color & 0xFF000000) | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }

    private static class SectorAngles {
        final double start, mid, end;
        SectorAngles(int i) {
            double w = 2 * Math.PI / GROUPS;
            mid = -Math.PI / 2 + w * i;
            start = mid - w / 2;
            end = mid + w / 2;
        }
    }
}