package com.hexsbm.screen;

import com.hexsbm.config.ConfigManager;
import com.hexsbm.config.HexSBMConfig;
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

    private boolean editing = false;
    private static final int PANEL_WIDTH = 220;

    private int pigmentColor = 0xFFFFFFFF;
    private Hand activeHand = null;
    private int centralGroup = 0;
    private int originalPageIdx = -1;

    private HexSBMConfig liveConfig;

    private String editingField = null; // "outer" или "inner"
    private final StringBuilder editingValue = new StringBuilder();

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

        this.liveConfig = ConfigManager.getSavedConfig();
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
        int cx = (int)(width * liveConfig.centerX);
        int cy = (int)(height * liveConfig.centerY);

        // Внешнее кольцо — страницы
        for (int i = 0; i < GROUPS; i++) {
            int page = centralGroup * GROUP_SIZE + i + 1;
            if (page > TOTAL_PAGES) continue;

            SectorAngles ang = new SectorAngles(i);
            boolean cur = page == currentPage;
            boolean hover = isPointInSegment(mx, my, cx, cy,
                liveConfig.outerRingInnerRadius,
                liveConfig.outerRingOuterRadius,
                ang.start, ang.end);

            int innerCol = cur   ? mkColor(liveConfig.activeAlpha, lighten(pigmentColor, 0.15f)) :
                           hover ? mkColor(liveConfig.hoverAlpha, lighten(pigmentColor, 0.25f)) :
                                   mkColor(liveConfig.inactiveAlpha, darken(pigmentColor, 0.10f));
            int outerCol = cur   ? mkColor(liveConfig.activeAlpha, lighten(pigmentColor, 0.15f)) :
                           hover ? mkColor(liveConfig.hoverAlpha, lighten(pigmentColor, 0.25f)) :
                                   mkColor(liveConfig.inactiveAlpha, lighten(pigmentColor, 0.10f));

            fillSegment(ctx, cx, cy,
                liveConfig.outerRingInnerRadius,
                liveConfig.outerRingOuterRadius,
                ang.start, ang.end, innerCol, outerCol);

            int r = (liveConfig.outerRingInnerRadius + liveConfig.outerRingOuterRadius) / 2;
            r += liveConfig.outerIconRadiusOffset;
            r = Math.max(0, r);
            int x = (int)(cx + r * Math.cos(ang.mid));
            int y = (int)(cy + r * Math.sin(ang.mid));
            ItemStack icon = getPageIcon(book, page);
            if (!icon.isEmpty()) ctx.drawItem(icon, x - 8, y - 8);

            if (hover && liveConfig.enableTooltips) {
                List<Text> tip = new ArrayList<>();
                String name = getCustomPageName(book, page);
                tip.add(Text.literal(name != null && !name.isEmpty() ? name : "Page " + page));
                tip.addAll(getPatternTooltip(book, page));
                ctx.drawTooltip(textRenderer, tip, mx, my);
            }
        }

        // Внутреннее кольцо — группы
        for (int i = 0; i < GROUPS; i++) {
            SectorAngles ang = new SectorAngles(i);
            boolean cur = i == centralGroup;
            boolean hover = isPointInSegment(mx, my, cx, cy,
                liveConfig.innerRingInnerRadius,
                liveConfig.innerRingOuterRadius,
                ang.start, ang.end);

            int innerCol = cur   ? mkColor(liveConfig.activeAlpha, lighten(pigmentColor, 0.15f)) :
                           hover ? mkColor(liveConfig.hoverAlpha, lighten(pigmentColor, 0.25f)) :
                                   mkColor(liveConfig.inactiveAlpha, lighten(pigmentColor, 0.15f));
            int outerCol = cur   ? mkColor(liveConfig.activeAlpha, lighten(pigmentColor, 0.20f)) :
                           hover ? mkColor(liveConfig.hoverAlpha, lighten(pigmentColor, 0.25f)) :
                                   mkColor(liveConfig.inactiveAlpha, darken(pigmentColor, 0.10f));

            fillSegment(ctx, cx, cy,
                liveConfig.innerRingInnerRadius,
                liveConfig.innerRingOuterRadius,
                ang.start, ang.end, innerCol, outerCol);

            int r = (liveConfig.innerRingInnerRadius + liveConfig.innerRingOuterRadius) / 2;
            r += liveConfig.innerIconRadiusOffset;
            r = Math.max(0, r);
            int x = (int)(cx + r * Math.cos(ang.mid));
            int y = (int)(cy + r * Math.sin(ang.mid));
            ItemStack icon = getGroupIcon(book, i);
            if (!icon.isEmpty()) ctx.drawItem(icon, x - 8, y - 8);
        }

        // Панель настроек
        if (editing) {
            int px = width - PANEL_WIDTH;
            ctx.fill(px, 0, width, height, 0x88000000);

            ctx.drawText(textRenderer, "Настройки UI", px + 10, 5, 0xFFFFFF, false);
            ctx.drawText(textRenderer, "Сбросить всё", px + 10, 22, 0xFF6666, false);
            ctx.drawText(textRenderer, "Сбросить до моего", px + 10, 52, 0x66FF66, false);

            drawNumberField(ctx, px + 100, 85, liveConfig.outerRingOuterRadius, mx, my, "Внешний радиус", "outer");
            drawNumberField(ctx, px + 100, 115, liveConfig.innerRingInnerRadius, mx, my, "Внутр. радиус", "inner");
            drawNumberField(ctx, px + 100, 145, liveConfig.outerRingInnerRadius, mx, my, "Начало внеш.", "outerInner");
            drawNumberField(ctx, px + 100, 175, liveConfig.innerRingOuterRadius, mx, my, "Конец внутр.", "innerOuter");
            drawNumberField(ctx, px + 100, 205, liveConfig.innerIconRadiusOffset, mx, my, "Смещение внутр.", "innerIconOffset");
            drawNumberField(ctx, px + 100, 235, liveConfig.outerIconRadiusOffset, mx, my, "Смещение внеш.", "outerIconOffset");
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

        int mx = (int) mouseX, my = (int) mouseY;

        if (mx > width - PANEL_WIDTH) {
            editing = true;
            int px = width - PANEL_WIDTH;

            applyEditingValue();

            if (isClickOnField(mx, my, px + 100, 145)) {
                editingField = "outerInner";
                editingValue.setLength(0);
                editingValue.append(liveConfig.outerRingInnerRadius);
                return true;
            }
            if (isClickOnField(mx, my, px + 100, 175)) {
                editingField = "innerOuter";
                editingValue.setLength(0);
                editingValue.append(liveConfig.innerRingOuterRadius);
                return true;
            }
            if (isClickOnField(mx, my, px + 100, 205)) {
                editingField = "innerIconOffset";
                editingValue.setLength(0);
                editingValue.append(liveConfig.innerIconRadiusOffset);
                return true;
            }
            if (isClickOnField(mx, my, px + 100, 235)) {
                editingField = "outerIconOffset";
                editingValue.setLength(0);
                editingValue.append(liveConfig.outerIconRadiusOffset);
                return true;
            }

            if (my >= 20 && my <= 40) {
                liveConfig = new HexSBMConfig();
                return true;
            }
            if (my >= 50 && my <= 70) {
                liveConfig = ConfigManager.getSavedConfig();
                return true;
            }

            // Клик мимо полей — выходим из редактирования
            editingField = null;
            return true;
        }

        // Обычное поведение меню
        int cx = (int)(width * liveConfig.centerX);
        int cy = (int)(height * liveConfig.centerY);

        if (button == 0) {
            for (int i = 0; i < GROUPS; i++) {
                SectorAngles ang = new SectorAngles(i);
                if (isPointInSegment(mx, my, cx, cy,
                    liveConfig.innerRingInnerRadius,
                    liveConfig.innerRingOuterRadius,
                    ang.start, ang.end)) {
                    centralGroup = i;
                    return true;
                }
            }
            for (int i = 0; i < GROUPS; i++) {
                int page = centralGroup * GROUP_SIZE + i + 1;
                if (page > TOTAL_PAGES) continue;
                SectorAngles ang = new SectorAngles(i);
                if (isPointInSegment(mx, my, cx, cy,
                    liveConfig.outerRingInnerRadius,
                    liveConfig.outerRingOuterRadius,
                    ang.start, ang.end)) {
                    com.hexsbm.HexSBMClient.sendChangeSpellbookPage(activeHand, page);
                    close();
                    return true;
                }
            }
            if (liveConfig.closeOnBackgroundClick) {
                close();
            }
            return true;
        }

        if (button == 1) {
            ItemStack book = getCurrentBook();
            if (book.isEmpty()) return false;

            for (int i = 0; i < GROUPS; i++) {
                SectorAngles ang = new SectorAngles(i);
                if (isPointInSegment(mx, my, cx, cy,
                    liveConfig.innerRingInnerRadius,
                    liveConfig.innerRingOuterRadius,
                    ang.start, ang.end)) {
                    updateGroupIcon(book, i);
                    return true;
                }
            }
            for (int i = 0; i < GROUPS; i++) {
                int page = centralGroup * GROUP_SIZE + i + 1;
                if (page > TOTAL_PAGES) continue;
                SectorAngles ang = new SectorAngles(i);
                if (isPointInSegment(mx, my, cx, cy,
                    liveConfig.outerRingInnerRadius,
                    liveConfig.outerRingOuterRadius,
                    ang.start, ang.end)) {
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
        applyEditingValue();
        if (editing) {
            ConfigManager.saveConfig(this.liveConfig);
        }
        if (client != null) client.setScreen(null);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int mods) {
        if (editingField != null) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (editingValue.length() > 0) {
                    editingValue.setLength(editingValue.length() - 1);
                }
                return true;
            }
            if ((keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) ||
                (keyCode >= GLFW.GLFW_KEY_KP_0 && keyCode <= GLFW.GLFW_KEY_KP_9)) {
                char c = (char) ('0' + (keyCode <= GLFW.GLFW_KEY_9 ? keyCode - GLFW.GLFW_KEY_0 : keyCode - GLFW.GLFW_KEY_KP_0));
                editingValue.append(c);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                applyEditingValue();
                editingField = null;
                return true;
            }
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_V || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, mods);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int mx = (int) mouseX, my = (int) mouseY;
        int px = width - PANEL_WIDTH;

        if (editing && mx > px) {
            int step = amount > 0 ? 1 : -1;

            if (isMouseOverField(mx, my, px + 100, 85)) {
                handleScrollField("outer", step);
                return true;
            }
            if (isMouseOverField(mx, my, px + 100, 115)) {
                handleScrollField("inner", step);
                return true;
            }
            if (isMouseOverField(mx, my, px + 100, 145)) {
                handleScrollField("outerInner", step);
                return true;
            }
            if (isMouseOverField(mx, my, px + 100, 175)) {
                handleScrollField("innerOuter", step);
                return true;
            }
            if (isMouseOverField(mx, my, px + 100, 205)) {
                handleScrollField("innerIconOffset", step);
                return true;
            }
            if (isMouseOverField(mx, my, px + 100, 235)) {
                handleScrollField("outerIconOffset", step);
                return true;
            }

            return true;
        }

        if (client == null || client.player == null || activeHand == null) return false;
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
        "hexcasting:pattern_data", "hexcasting:amulet_state", "hexcasting:op_code", "patchouli:book", "op_id"
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
        if (rIn < 0) rIn = 0;
        if (rOut <= rIn) rOut = rIn + 1;

        Matrix4f m = ctx.getMatrices().peek().getPositionMatrix();
        Tessellator t = Tessellator.getInstance();
        var b = t.getBuffer();

        float ir = ((cIn >> 16) & 0xFF) / 255f, ig = ((cIn >> 8) & 0xFF) / 255f, ib = (cIn & 0xFF) / 255f, ia = ((cIn >> 24) & 0xFF) / 255f;
        float or = ((cOut >> 16) & 0xFF) / 255f, og = ((cOut >> 8) & 0xFF) / 255f, ob = (cOut & 0xFF) / 255f, oa = ((cOut >> 24) & 0xFF) / 255f;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int seg = 16;

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

    private void drawNumberField(DrawContext ctx, int x, int y, int value, int mx, int my, String label, String fieldId) {
        int width = 80;
        int height = 16;

        // Подсветка при наведении
        boolean hover = mx >= x && mx < x + width && my >= y && my < y + height;
        if (hover) {
            ctx.fill(x, y, x + width, y + height, 0x44FFFFFF);
        }

        // Фон поля
        ctx.fill(x, y, x + width, y + height, 0xFF333333);
        ctx.fill(x, y, x + width, y + 1, 0xFF666666);
        ctx.fill(x, y, x + 1, y + height, 0xFF666666);

        String display = editingField != null && editingField.equals(fieldId)
            ? editingValue.toString()
            : String.valueOf(value);

        // Текст
        ctx.drawText(textRenderer, display, x + 3, y + 4, 0xFFFFFF, false);

        // Мигающий курсор
        if (editingField != null && editingField.equals(fieldId)) {
            long time = System.currentTimeMillis() / 500;
            if (time % 2 == 0) {
                int textWidth = textRenderer.getWidth(Text.literal(display));
                int cursorX = x + 3 + textWidth + 1;
                ctx.fill(cursorX, y + 3, cursorX + 1, y + 13, 0xFFFFFFFF);
            }
        }

        // Метка
        int labelWidth = textRenderer.getWidth(Text.literal(label + ":"));
        ctx.drawText(textRenderer, label + ":", x - labelWidth - 5, y + 4, 0xFFFFFF, false);
    }

    private boolean isClickOnField(int mx, int my, int x, int y) {
        return mx >= x && mx < x + 80 && my >= y && my < y + 16;
    }

    private boolean isMouseOverField(int mx, int my, int x, int y) {
        return mx >= x && mx < x + 80 && my >= y && my < y + 16;
    }

    private void applyEditingValue() {
        if (editingField == null || editingValue.length() == 0) return;
        try {
            int value = Integer.parseInt(editingValue.toString());
            switch (editingField) {
                case "outer" -> liveConfig.outerRingOuterRadius = value;
                case "inner" -> liveConfig.innerRingInnerRadius = value;
                case "outerInner" -> liveConfig.outerRingInnerRadius = value;
                case "innerOuter" -> liveConfig.innerRingOuterRadius = value;
                case "innerIconOffset" -> liveConfig.innerIconRadiusOffset = MathHelper.clamp(value, -200, 200);
                case "outerIconOffset" -> liveConfig.outerIconRadiusOffset = MathHelper.clamp(value, -200, 200);
            }
            if ("outer".equals(editingField) || "inner".equals(editingField) || 
                "outerInner".equals(editingField) || "innerOuter".equals(editingField)) {
                enforceRingOrder(editingField);
            }
        } catch (NumberFormatException ignored) {}
        editingField = null;
    }

    private void handleScrollField(String fieldName, int step) {
        int currentValue;
        if (fieldName.equals(editingField)) {
            try {
                currentValue = editingValue.length() == 0 
                    ? getCurrentValue(fieldName) 
                    : Integer.parseInt(editingValue.toString());
            } catch (NumberFormatException ignored) {
                currentValue = getCurrentValue(fieldName);
            }
        } else {
            currentValue = getCurrentValue(fieldName);
        }

        int newValue = currentValue + step;

        switch (fieldName) {
            case "outer" -> liveConfig.outerRingOuterRadius = newValue;
            case "inner" -> liveConfig.innerRingInnerRadius = newValue;
            case "outerInner" -> liveConfig.outerRingInnerRadius = newValue;
            case "innerOuter" -> liveConfig.innerRingOuterRadius = newValue;
            case "innerIconOffset" -> liveConfig.innerIconRadiusOffset = MathHelper.clamp(newValue, -200, 200);
            case "outerIconOffset" -> liveConfig.outerIconRadiusOffset = MathHelper.clamp(newValue, -200, 200);
        }

        if ("outer".equals(fieldName) || "inner".equals(fieldName) || 
            "outerInner".equals(fieldName) || "innerOuter".equals(fieldName)) {
            enforceRingOrder(fieldName);
        }

        if (fieldName.equals(editingField)) {
            editingValue.setLength(0);
            editingValue.append(getCurrentValue(fieldName));
        }
    }

    private int getCurrentValue(String field) {
        return switch (field) {
            case "outer" -> liveConfig.outerRingOuterRadius;
            case "inner" -> liveConfig.innerRingInnerRadius;
            case "outerInner" -> liveConfig.outerRingInnerRadius;
            case "innerOuter" -> liveConfig.innerRingOuterRadius;
            case "innerIconOffset" -> liveConfig.innerIconRadiusOffset;
            case "outerIconOffset" -> liveConfig.outerIconRadiusOffset;
            default -> 0;
        };
    }

    private void enforceRingOrder(String editingField) {
        // Считываем текущие значения, не допуская < 0
        int innerIn = Math.max(0, liveConfig.innerRingInnerRadius);
        int innerOut = Math.max(0, liveConfig.innerRingOuterRadius);
        int outerIn = Math.max(0, liveConfig.outerRingInnerRadius);
        int outerOut = Math.max(0, liveConfig.outerRingOuterRadius);

        // Глобальный максимум — чтобы не улетало
        outerOut = Math.min(999, outerOut);
        outerIn = Math.min(999, outerIn);
        innerOut = Math.min(999, innerOut);
        innerIn = Math.min(999, innerIn);

        if ("inner".equals(editingField)) {
            // innerIn — главный: двигаем всё вправо от него
            innerOut = Math.max(innerIn, innerOut);
            outerIn = Math.max(innerOut, outerIn);
            outerOut = Math.max(outerIn, outerOut);
        }
        else if ("innerOuter".equals(editingField)) {
            // innerOut — главный
            innerIn = Math.min(innerIn, innerOut);
            outerIn = Math.max(outerIn, innerOut);
            outerOut = Math.max(outerOut, outerIn);
        }
        else if ("outerInner".equals(editingField)) {
            // outerIn — главный
            outerOut = Math.max(outerOut, outerIn);
            innerOut = Math.min(innerOut, outerIn);
            innerIn = Math.min(innerIn, innerOut);
        }
        else if ("outer".equals(editingField)) {
            // outerOut — главный
            outerIn = Math.min(outerIn, outerOut);
            innerOut = Math.min(innerOut, outerIn);
            innerIn = Math.min(innerIn, innerOut);
        }
        else {
            // Никто не редактирует — просто сортируем
            innerIn = Math.max(0, innerIn);
            innerOut = Math.max(innerIn, innerOut);
            outerIn = Math.max(innerOut, outerIn);
            outerOut = Math.max(outerIn, outerOut);
        }

        // Финальный clamp по убыванию, чтобы сохранить порядок при max=999
        outerOut = Math.min(999, outerOut);
        outerIn = Math.min(999, outerIn);
        innerOut = Math.min(999, innerOut);
        innerIn = Math.min(999, innerIn);

        // Применяем
        liveConfig.innerRingInnerRadius = innerIn;
        liveConfig.innerRingOuterRadius = innerOut;
        liveConfig.outerRingInnerRadius = outerIn;
        liveConfig.outerRingOuterRadius = outerOut;
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