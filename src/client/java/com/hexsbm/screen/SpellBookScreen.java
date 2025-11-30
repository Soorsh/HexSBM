package com.hexsbm.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.lwjgl.glfw.GLFW;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import java.util.Collections;
import java.util.List;

/**
 * Круговое меню для выбора страницы в Hexcasting Spellbook.
 * Использует двухуровневую радиальную навигацию:
 * - 8 центральных кнопок переключают группы (по 8 страниц),
 * - 8 внешних кнопок отображают текущую группу (всего 64 страницы).
 */
public class SpellBookScreen extends Screen {
    private static final Identifier SPELLBOOK_ID = new Identifier("hexcasting", "spellbook");

    private int originalPageIdx = -1;
    private net.minecraft.util.Hand activeHand = null;
    private boolean selectionConfirmed = false;

    private int centralGroup = 0;
    private static final int GROUP_SIZE = 8;
    private static final int TOTAL_PAGES = 64;

    private static final int BUTTON_RADIUS = 16;
    private static final int INNER_RADIUS = 40;
    private static final int OUTER_RADIUS = 100;

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

        // Внешние кнопки — выбор страницы внутри группы
        for (int i = 0; i < 8; i++) {
            int pageIndex = centralGroup * GROUP_SIZE + i + 1;
            if (pageIndex > TOTAL_PAGES) continue;

            double angle = Math.toRadians(360.0 / 8 * i - 90);
            int x = (int)(cx + OUTER_RADIUS * Math.cos(angle));
            int y = (int)(cy + OUTER_RADIUS * Math.sin(angle));

            boolean isCurrent = (pageIndex == currentPageIdx);
            boolean isHovered = isPointInCircle(mouseX, mouseY, x, y, BUTTON_RADIUS);

            int color = isCurrent ? 0xFF44AA44 :
                       isHovered ? 0xFF555555 :
                                   0xFF444444;
            fillCircle(context, x, y, BUTTON_RADIUS, color);

            String label = getPageLabel(currentBook, pageIndex);
            int tw = textRenderer.getWidth(label);
            context.drawText(textRenderer, label, x - tw / 2, y - 4, 0xFFFFFF, false);

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

        // Центральные кнопки — выбор группы
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(360.0 / 8 * i - 90);
            int x = (int)(cx + INNER_RADIUS * Math.cos(angle));
            int y = (int)(cy + INNER_RADIUS * Math.sin(angle));

            boolean isCurrentGroup = (i == centralGroup);
            boolean isHovered = isPointInCircle(mouseX, mouseY, x, y, BUTTON_RADIUS - 2);

            int color = isCurrentGroup ? 0xFF3388FF :
                       isHovered ? 0xFF666666 :
                                   0xFF333333;
            fillCircle(context, x, y, BUTTON_RADIUS - 2, color);

            String label = String.valueOf(i + 1);
            int tw = textRenderer.getWidth(label);
            context.drawText(textRenderer, label, x - tw / 2, y - 4, 0xFFFFFF, false);
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

        // Клик по центральным кнопкам (группы)
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(360.0 / 8 * i - 90);
            int x = (int)(cx + INNER_RADIUS * Math.cos(angle));
            int y = (int)(cy + INNER_RADIUS * Math.sin(angle));
            if (isPointInCircle((int)mouseX, (int)mouseY, x, y, BUTTON_RADIUS - 2)) {
                this.centralGroup = i;
                return true;
            }
        }

        // Клик по внешним кнопкам (страницы)
        for (int i = 0; i < 8; i++) {
            int pageIndex = centralGroup * GROUP_SIZE + i + 1;
            if (pageIndex > TOTAL_PAGES) continue;

            double angle = Math.toRadians(360.0 / 8 * i - 90);
            int x = (int)(cx + OUTER_RADIUS * Math.cos(angle));
            int y = (int)(cy + OUTER_RADIUS * Math.sin(angle));
            if (isPointInCircle((int)mouseX, (int)mouseY, x, y, BUTTON_RADIUS)) {
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

    private boolean isPointInCircle(int px, int py, int cx, int cy, int radius) {
        return (px - cx) * (px - cx) + (py - cy) * (py - cy) <= radius * radius;
    }

    private void fillCircle(DrawContext context, int cx, int cy, int r, int color) {
        context.fill(cx - r, cy - r, cx + r, cy + r, color);
        context.drawHorizontalLine(cx - r, cx + r, cy - r, 0xFFFFFFFF);
        context.drawHorizontalLine(cx - r, cx + r, cy + r - 1, 0xFFFFFFFF);
        context.drawVerticalLine(cx - r, cy - r, cy + r, 0xFFFFFFFF);
        context.drawVerticalLine(cx + r - 1, cy - r, cy + r, 0xFFFFFFFF);
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