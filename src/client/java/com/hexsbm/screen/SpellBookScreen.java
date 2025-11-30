package com.hexsbm.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.render.*;

/**
 * Круговое меню для выбора страницы в Hexcasting Spellbook.
 * Использует двухуровневую радиальную навигацию:
 * - 8 центральных секторов переключают группы (по 8 страниц),
 * - 8 внешних секторов отображают текущую группу (всего 64 страницы).
 */
public class SpellBookScreen extends Screen {
    private static final Identifier SPELLBOOK_ID = new Identifier("hexcasting", "spellbook");

    private int originalPageIdx = -1;
    private net.minecraft.util.Hand activeHand = null;
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

        ItemStack currentBook = findSpellbook();
        if (currentBook.isEmpty()) {
            close();
            return;
        }

        ClientPlayerEntity player = client.player;
        net.minecraft.util.Hand currentHand = determineHandForBook(currentBook, player);

        NbtCompound nbt = currentBook.getNbt();
        this.originalPageIdx = (nbt != null && nbt.contains("page_idx", NbtElement.INT_TYPE))
            ? nbt.getInt("page_idx")
            : 1;
        this.activeHand = currentHand;
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

        ItemStack currentBook = findSpellbook();
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

            double sectorWidth = 2 * Math.PI / 8;
            double midAngle = -Math.PI / 2 + sectorWidth * i;
            double startAngle = midAngle - sectorWidth / 2;
            double endAngle = midAngle + sectorWidth / 2;

            boolean isCurrent = (pageIndex == currentPageIdx);
            boolean isHovered = isPointInCircularSegment(mouseX, mouseY, cx, cy,
                    OUTER_SEGMENT_START, OUTER_SEGMENT_END, startAngle, endAngle);

            int color = isCurrent ? 0xFF44AA44 :
                       isHovered ? 0xFF555555 :
                                   0xFF444444;

            fillCircularSegment(context, cx, cy, OUTER_SEGMENT_START, OUTER_SEGMENT_END,
                    startAngle, endAngle, color);

            // Текст по центру сектора
            int textX = (int)(cx + (OUTER_SEGMENT_START + OUTER_SEGMENT_END) / 2.0 * Math.cos(midAngle));
            int textY = (int)(cy + (OUTER_SEGMENT_START + OUTER_SEGMENT_END) / 2.0 * Math.sin(midAngle));

            String label = getPageLabel(currentBook, pageIndex);
            int tw = textRenderer.getWidth(label);
            context.drawText(textRenderer, label, textX - tw / 2, textY - 4, 0xFFFFFF, false);

            // Тултип при наведении
            if (isHovered) {
                String customName = getCustomPageName(currentBook, pageIndex);
                List<Text> patterns = getPatternTooltipLines(currentBook, pageIndex);
                List<Text> tooltip = new java.util.ArrayList<>();

                if (customName != null && !customName.isEmpty()) {
                    tooltip.add(Text.literal(customName));
                } else {
                    tooltip.add(Text.literal("Страница " + pageIndex));
                }
                tooltip.addAll(patterns);

                if (!tooltip.isEmpty()) {
                    context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
                }
            }
        }

        // Внутренние секторы — выбор группы
        for (int i = 0; i < 8; i++) {
            double sectorWidth = 2 * Math.PI / 8;
            double midAngle = -Math.PI / 2 + sectorWidth * i;
            double startAngle = midAngle - sectorWidth / 2;
            double endAngle = midAngle + sectorWidth / 2;

            boolean isCurrentGroup = (i == centralGroup);
            boolean isHovered = isPointInCircularSegment(mouseX, mouseY, cx, cy,
                    INNER_SEGMENT_START, INNER_SEGMENT_END, startAngle, endAngle);

            int color = isCurrentGroup ? 0xFF3388FF :
                       isHovered ? 0xFF666666 :
                                   0xFF333333;

            fillCircularSegment(context, cx, cy, INNER_SEGMENT_START, INNER_SEGMENT_END,
                    startAngle, endAngle, color);

            // Текст по центру сектора
            int textX = (int)(cx + (INNER_SEGMENT_START + INNER_SEGMENT_END) / 2.0 * Math.cos(midAngle));
            int textY = (int)(cy + (INNER_SEGMENT_START + INNER_SEGMENT_END) / 2.0 * Math.sin(midAngle));

            String label = String.valueOf(i + 1);
            int tw = textRenderer.getWidth(label);
            context.drawText(textRenderer, label, textX - tw / 2, textY - 4, 0xFFFFFF, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || client == null || client.player == null || activeHand == null) {
            close();
            return true;
        }

        int cx = this.width / 2;
        int cy = this.height / 2;
        int mx = (int) mouseX;
        int my = (int) mouseY;

        // Клик по внутренним секторам (группы)
        for (int i = 0; i < 8; i++) {
            double sectorWidth = 2 * Math.PI / 8;
            double midAngle = -Math.PI / 2 + sectorWidth * i;
            double startAngle = midAngle - sectorWidth / 2;
            double endAngle = midAngle + sectorWidth / 2;
            if (isPointInCircularSegment(mx, my, cx, cy, INNER_SEGMENT_START, INNER_SEGMENT_END, startAngle, endAngle)) {
                this.centralGroup = i;
                return true;
            }
        }

        // Клик по внешним секторам (страницы)
        for (int i = 0; i < 8; i++) {
            int pageIndex = centralGroup * GROUP_SIZE + i + 1;
            if (pageIndex > TOTAL_PAGES) continue;

            double sectorWidth = 2 * Math.PI / 8;
            double midAngle = -Math.PI / 2 + sectorWidth * i;
            double startAngle = midAngle - sectorWidth / 2;
            double endAngle = midAngle + sectorWidth / 2;
            if (isPointInCircularSegment(mx, my, cx, cy, OUTER_SEGMENT_START, OUTER_SEGMENT_END, startAngle, endAngle)) {
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
            NbtCompound nbt = handStack.getOrCreateNbt().copy();
            nbt.putInt("page_idx", originalPageIdx);
            ItemStack restored = new ItemStack(handStack.getItem(), handStack.getCount());
            restored.setNbt(nbt);
            client.player.setStackInHand(activeHand, restored);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_V) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    private ItemStack findSpellbook() {
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

    private net.minecraft.util.Hand determineHandForBook(ItemStack book, ClientPlayerEntity player) {
        if (!player.getMainHandStack().isEmpty() && Registries.ITEM.getId(player.getMainHandStack().getItem()).equals(SPELLBOOK_ID)) {
            return net.minecraft.util.Hand.MAIN_HAND;
        }
        return net.minecraft.util.Hand.OFF_HAND;
    }

    private boolean isPointInCircularSegment(int px, int py, int cx, int cy,
                                           int innerRadius, int outerRadius,
                                           double startAngle, double endAngle) {
        double dx = px - cx;
        double dy = py - cy;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < innerRadius || distance > outerRadius) return false;

        double angle = Math.atan2(dy, dx);
        // Нормализуем все углы в [0, 2π)
        if (angle < 0) angle += 2 * Math.PI;
        double normStart = startAngle < 0 ? startAngle + 2 * Math.PI : startAngle;
        double normEnd = endAngle < 0 ? endAngle + 2 * Math.PI : endAngle;

        if (normStart < normEnd) {
            return angle >= normStart && angle <= normEnd;
        } else {
            // Сектор пересекает 0° (например, 350° → 10°)
            return angle >= normStart || angle <= normEnd;
        }
    }

    private void fillCircularSegment(DrawContext context, int cx, int cy, int innerRadius, int outerRadius,
                                double startAngle, double endAngle, int color) {
        MatrixStack matrices = context.getMatrices();
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

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

    private String getPageLabel(ItemStack book, int page) {
        String name = getCustomPageName(book, page);
        if (name != null && !name.isEmpty()) {
            return name.length() > 3 ? name.substring(0, 3) : name;
        }
        return String.valueOf(page);
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

        if (fullTooltip.size() <= 1) {
            return Collections.emptyList();
        }

        return fullTooltip.subList(1, fullTooltip.size());
    }
}