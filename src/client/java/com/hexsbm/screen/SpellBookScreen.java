package com.hexsbm.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Круговое меню для выбора страницы в Hexcasting Spellbook.
 * Использует двухуровневую радиальную навигацию:
 * - 8 центральных секторов переключают группы (по 8 страниц),
 * - 8 внешних секторов отображают текущую группу (всего 64 страницы).
 * 
 * Поддерживает установку иконок страниц через правый клик (сохраняются в NBT книги).
 */
public class SpellBookScreen extends Screen {
    private static final Identifier SPELLBOOK_ID = new Identifier("hexcasting", "spellbook");

    // Цвета (альфа-красный-зелёный-синий в шестнадцатеричном формате)
    private static final int COLOR_CURRENT_PAGE   = 0xCC44FF44;
    private static final int COLOR_HOVERED        = 0x90FFFFFF;
    private static final int COLOR_DEFAULT_PAGE   = 0x75CCCCCC;
    private static final int COLOR_CURRENT_GROUP  = 0xCC4488FF;
    private static final int COLOR_DEFAULT_GROUP  = 0x75AAAAFF;

    private int originalPageIdx = -1;
    private Hand activeHand = null;
    private boolean selectionConfirmed = false;

    private int centralGroup = 0;
    private static final int GROUP_SIZE = 8;
    private static final int TOTAL_PAGES = 64;

    // Радиусы для секторов
    private static final int INNER_SEGMENT_START = 30;
    private static final int INNER_SEGMENT_END   = 60;
    private static final int OUTER_SEGMENT_START = 70;
    private static final int OUTER_SEGMENT_END   = 110;

    public SpellBookScreen() {
        super(Text.empty());
    }

    @Override
    public void init() {
        super.init();

        if (client == null || client.player == null) {
            close();
            return;
        }

        ClientPlayerEntity player = client.player;
        ItemStack main = player.getMainHandStack();
        ItemStack off = player.getOffHandStack();

        ItemStack currentBook;
        if (!main.isEmpty() && Registries.ITEM.getId(main.getItem()).equals(SPELLBOOK_ID)) {
            currentBook = main;
            activeHand = Hand.MAIN_HAND;
        } else if (!off.isEmpty() && Registries.ITEM.getId(off.getItem()).equals(SPELLBOOK_ID)) {
            currentBook = off;
            activeHand = Hand.OFF_HAND;
        } else {
            close();
            return;
        }

        NbtCompound nbt = currentBook.getNbt();
        this.originalPageIdx = (nbt != null && nbt.contains("page_idx", NbtElement.INT_TYPE))
            ? nbt.getInt("page_idx")
            : 1;

        this.selectionConfirmed = false;

        if (this.originalPageIdx != -1) {
            this.centralGroup = Math.max(0, Math.min(7, (this.originalPageIdx - 1) / GROUP_SIZE));
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (client == null || client.player == null) {
            close();
            return;
        }

        ItemStack currentBook = getCurrentBook();
        int currentPageIdx = 1;

        if (!currentBook.isEmpty()) {
            NbtCompound nbt = currentBook.getNbt();
            if (nbt != null && nbt.contains("page_idx", NbtElement.INT_TYPE)) {
                currentPageIdx = nbt.getInt("page_idx");
            }
        }

        renderRadialUI(context, mouseX, mouseY, currentBook, currentPageIdx);
    }

    private void renderRadialUI(DrawContext context, int mouseX, int mouseY, ItemStack currentBook, int currentPageIdx) {
        int cx = this.width / 2;
        int cy = this.height / 2;

        // Внешние секторы — страницы внутри текущей группы
        for (int i = 0; i < 8; i++) {
            int pageIndex = centralGroup * GROUP_SIZE + i + 1;
            if (pageIndex > TOTAL_PAGES) continue;

            SectorAngles angles = new SectorAngles(i);
            boolean isCurrent = (pageIndex == currentPageIdx);
            boolean isHovered = isPointInCircularSegment(mouseX, mouseY, cx, cy,
                    OUTER_SEGMENT_START, OUTER_SEGMENT_END, angles.start, angles.end);

            int color = isCurrent ? COLOR_CURRENT_PAGE :
                        isHovered ? COLOR_HOVERED :
                                    COLOR_DEFAULT_PAGE;

            fillCircularSegment(context, cx, cy, OUTER_SEGMENT_START, OUTER_SEGMENT_END,
                    angles.start, angles.end, color);

            int textX = (int)(cx + (OUTER_SEGMENT_START + OUTER_SEGMENT_END) / 2.0 * Math.cos(angles.mid));
            int textY = (int)(cy + (OUTER_SEGMENT_START + OUTER_SEGMENT_END) / 2.0 * Math.sin(angles.mid));

            // === ОТРИСОВКА ИКОНКИ ИЛИ ЦИФРЫ ===
            ItemStack icon = getPageIcon(currentBook, pageIndex);
            if (!icon.isEmpty()) {
                int size = 16;
                context.drawItem(icon, textX - size / 2, textY - size / 2);
            } else {
                String label = String.valueOf(pageIndex);
                int tw = textRenderer.getWidth(label);
                context.drawText(textRenderer, label, textX - tw / 2, textY - 4, 0xFFFFFF, false);
            }

            if (isHovered) {
                String customName = getCustomPageName(currentBook, pageIndex);
                List<Text> patterns = getPatternTooltipLines(currentBook, pageIndex);
                List<Text> tooltip = new ArrayList<>();

                if (customName != null && !customName.isEmpty()) {
                    tooltip.add(Text.literal(customName));
                } else {
                    tooltip.add(Text.literal("Page " + pageIndex));
                }
                tooltip.addAll(patterns);

                if (!tooltip.isEmpty()) {
                    context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
                }
            }
        }

        // Внутренние секторы — выбор группы
        for (int i = 0; i < 8; i++) {
            SectorAngles angles = new SectorAngles(i);
            boolean isCurrentGroup = (i == centralGroup);
            boolean isHovered = isPointInCircularSegment(mouseX, mouseY, cx, cy,
                    INNER_SEGMENT_START, INNER_SEGMENT_END, angles.start, angles.end);

            int color = isCurrentGroup ? COLOR_CURRENT_GROUP :
                        isHovered      ? COLOR_HOVERED :
                                         COLOR_DEFAULT_GROUP;

            fillCircularSegment(context, cx, cy, INNER_SEGMENT_START, INNER_SEGMENT_END,
                    angles.start, angles.end, color);

            int textX = (int)(cx + (INNER_SEGMENT_START + INNER_SEGMENT_END) / 2.0 * Math.cos(angles.mid));
            int textY = (int)(cy + (INNER_SEGMENT_START + INNER_SEGMENT_END) / 2.0 * Math.sin(angles.mid));

            String label = String.valueOf(i + 1);
            int tw = textRenderer.getWidth(label);
            context.drawText(textRenderer, label, textX - tw / 2, textY - 4, 0xFFFFFF, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (client == null || client.player == null || activeHand == null) {
            close();
            return true;
        }

        int cx = this.width / 2;
        int cy = this.height / 2;
        int mx = (int) mouseX;
        int my = (int) mouseY;

        if (button == 1) {
            return handleRightClick(mx, my, cx, cy);
        } else if (button == 0) {
            return handleLeftClick(mx, my, cx, cy);
        }

        close();
        return true;
    }

    private boolean handleRightClick(int mx, int my, int cx, int cy) {
        ItemStack currentBook = getCurrentBook();
        if (currentBook.isEmpty()) return false;

        for (int i = 0; i < 8; i++) {
            int pageIndex = centralGroup * GROUP_SIZE + i + 1;
            if (pageIndex > TOTAL_PAGES) continue;

            SectorAngles angles = new SectorAngles(i);
            if (isPointInCircularSegment(mx, my, cx, cy, OUTER_SEGMENT_START, OUTER_SEGMENT_END, angles.start, angles.end)) {
                ClientPlayerEntity player = client.player;
                int selectedSlot = player.getInventory().selectedSlot; // ← ПРАВИЛЬНО
                ItemStack rawSource = player.getInventory().getStack(selectedSlot);
                ItemStack iconSource = makeIconOnly(rawSource);

                NbtCompound bookNbt = currentBook.getOrCreateNbt();
                NbtCompound pageIcons = bookNbt.getCompound("page_icons");

                if (!iconSource.isEmpty()) {
                    NbtCompound iconNbt = new NbtCompound();
                    iconSource.writeNbt(iconNbt);
                    pageIcons.put(String.valueOf(pageIndex), iconNbt);
                } else {
                    pageIcons.remove(String.valueOf(pageIndex));
                }

                bookNbt.put("page_icons", pageIcons);
                com.hexsbm.HexSBMClient.sendUpdatePageIcon(activeHand, pageIndex, iconSource);
                return true;
            }
        }
        return false;
    }

    private boolean handleLeftClick(int mx, int my, int cx, int cy) {
        // Клик по внутренним секторам (группы)
        for (int i = 0; i < 8; i++) {
            SectorAngles angles = new SectorAngles(i);
            if (isPointInCircularSegment(mx, my, cx, cy, INNER_SEGMENT_START, INNER_SEGMENT_END, angles.start, angles.end)) {
                this.centralGroup = i;
                return true;
            }
        }

        // Клик по внешним секторам (страницы)
        for (int i = 0; i < 8; i++) {
            int pageIndex = centralGroup * GROUP_SIZE + i + 1;
            if (pageIndex > TOTAL_PAGES) continue;

            SectorAngles angles = new SectorAngles(i);
            if (isPointInCircularSegment(mx, my, cx, cy, OUTER_SEGMENT_START, OUTER_SEGMENT_END, angles.start, angles.end)) {
                selectionConfirmed = true;
                com.hexsbm.HexSBMClient.sendChangeSpellbookPage(activeHand, pageIndex);
                close();
                return true;
            }
        }

        close();
        return true;
    }

    @Override
    public void close() {
        if (!selectionConfirmed) {
            restoreOriginalPage();
        }
        if (client != null) {
            client.setScreen(null);
        }
    }

    private void restoreOriginalPage() {
        if (client == null || client.player == null || activeHand == null || originalPageIdx == -1) {
            return;
        }

        ItemStack handStack = client.player.getStackInHand(activeHand);
        if (!handStack.isEmpty() && Registries.ITEM.getId(handStack.getItem()).equals(SPELLBOOK_ID)) {
            NbtCompound nbt = handStack.getOrCreateNbt();
            nbt.putInt("page_idx", originalPageIdx);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_V || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    private ItemStack getCurrentBook() {
        if (client == null || client.player == null) {
            return ItemStack.EMPTY;
        }
        ClientPlayerEntity player = client.player;
        ItemStack main = player.getMainHandStack();
        ItemStack off = player.getOffHandStack();

        if (!main.isEmpty() && Registries.ITEM.getId(main.getItem()).equals(SPELLBOOK_ID)) {
            return main;
        }
        if (!off.isEmpty() && Registries.ITEM.getId(off.getItem()).equals(SPELLBOOK_ID)) {
            return off;
        }
        return ItemStack.EMPTY;
    }

    private ItemStack getPageIcon(ItemStack book, int pageIndex) {
        NbtCompound nbt = book.getNbt();
        if (nbt == null || !nbt.contains("page_icons", NbtElement.COMPOUND_TYPE)) {
            return ItemStack.EMPTY;
        }

        NbtCompound pageIcons = nbt.getCompound("page_icons");
        String key = String.valueOf(pageIndex);
        if (!pageIcons.contains(key, NbtElement.COMPOUND_TYPE)) {
            return ItemStack.EMPTY;
        }

        try {
            return ItemStack.fromNbt(pageIcons.getCompound(key));
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    private static final Set<String> VISUAL_NBT_TAGS = Set.of(
        "Enchantments",
        "display",
        "CustomPotionColor",
        "Potion",
        "SkullOwner",
        "EntityTag",
        // Hexcasting-специфичные:
        "hexcasting:pattern_data",
        "hexcasting:amulet_state",
        "hexcasting:op_code",
        "op_id"
    );
    private static ItemStack makeIconOnly(ItemStack original) {        
        if (original.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack icon = new ItemStack(original.getItem(), 1);
        NbtCompound originalTag = original.getNbt();

        if (originalTag != null) {
            NbtCompound cleanTag = new NbtCompound();

            for (String key : VISUAL_NBT_TAGS) {
                if (originalTag.contains(key, NbtElement.COMPOUND_TYPE) ||
                    originalTag.contains(key, NbtElement.LIST_TYPE) ||
                    originalTag.contains(key, NbtElement.STRING_TYPE) ||
                    originalTag.contains(key, NbtElement.INT_TYPE)) {
                    cleanTag.put(key, originalTag.get(key).copy());
                }
            }

            if (!cleanTag.isEmpty()) {
                icon.setNbt(cleanTag);
            }
        }

        return icon;
    }

    private boolean isPointInCircularSegment(int px, int py, int cx, int cy,
                                           int innerRadius, int outerRadius,
                                           double startAngle, double endAngle) {
        double dx = px - cx;
        double dy = py - cy;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < innerRadius || distance > outerRadius) return false;

        double angle = Math.atan2(dy, dx);
        if (angle < 0) angle += 2 * Math.PI;
        double normStart = startAngle < 0 ? startAngle + 2 * Math.PI : startAngle;
        double normEnd = endAngle < 0 ? endAngle + 2 * Math.PI : endAngle;

        if (normStart < normEnd) {
            return angle >= normStart && angle <= normEnd;
        } else {
            return angle >= normStart || angle <= normEnd;
        }
    }

    private void fillCircularSegment(DrawContext context, int cx, int cy, int innerRadius, int outerRadius,
                                double startAngle, double endAngle, int color) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        Tessellator tessellator = Tessellator.getInstance();
        var bufferBuilder = tessellator.getBuffer();

        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int segments = Math.max(4, (int) ((endAngle - startAngle) / (Math.PI / 30)));

        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        for (int i = 0; i <= segments; i++) {
            double angle = MathHelper.lerp((double) i / segments, startAngle, endAngle);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            bufferBuilder.vertex(matrix, cx + outerRadius * cos, cy + outerRadius * sin, 0)
                        .color(r, g, b, a)
                        .next();
            bufferBuilder.vertex(matrix, cx + innerRadius * cos, cy + innerRadius * sin, 0)
                        .color(r, g, b, a)
                        .next();
        }

        tessellator.draw();
        RenderSystem.disableBlend();
    }

    private String getCustomPageName(ItemStack book, int page) {
        NbtCompound nbt = book.getNbt();
        if (nbt == null || !nbt.contains("page_names", NbtElement.COMPOUND_TYPE)) {
            return null;
        }

        NbtCompound pageNames = nbt.getCompound("page_names");
        String key = String.valueOf(page);

        if (!pageNames.contains(key, NbtElement.STRING_TYPE)) {
            return null;
        }

        String json = pageNames.getString(key);
        try {
            Text text = Text.Serializer.fromJson(json);
            return text != null ? text.getString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private List<Text> getPatternTooltipLines(ItemStack book, int page) {
        NbtCompound nbt = book.getNbt();
        if (nbt == null || !nbt.contains("pages", NbtElement.COMPOUND_TYPE)) {
            return Collections.emptyList();
        }

        NbtCompound pages = nbt.getCompound("pages");
        String pageKey = String.valueOf(page);
        if (!pages.contains(pageKey, NbtElement.COMPOUND_TYPE)) {
            return Collections.emptyList();
        }

        ItemStack fakeBook = book.copy();
        fakeBook.getOrCreateNbt().putInt("page_idx", page);
        List<Text> fullTooltip = fakeBook.getTooltip(client.player, TooltipContext.Default.BASIC);

        if (fullTooltip.size() >= 3) {
            return List.of(fullTooltip.get(2));
        }

        return Collections.emptyList();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (client != null && client.player != null) {
            PlayerInventory inventory = client.player.getInventory();
            int current = inventory.selectedSlot;
            int newSlot;

            // amount > 0 → прокрутка ВВЕРХ (предыдущий слот)
            if (amount > 0) {
                newSlot = current == 0 ? 8 : current - 1;
            } else {
                newSlot = current == 8 ? 0 : current + 1;
            }

            inventory.selectedSlot = newSlot;
            return true;
        }
        return false;
    }

    private static final class SectorAngles {
        final double start, mid, end;

        SectorAngles(int index) {
            double sectorWidth = 2 * Math.PI / 8;
            this.mid = -Math.PI / 2 + sectorWidth * index;
            this.start = mid - sectorWidth / 2;
            this.end = mid + sectorWidth / 2;
        }
    }
}