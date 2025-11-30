package com.hexsbm.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.lwjgl.glfw.GLFW;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import java.util.List;

public class SpellBookScreen extends Screen {
    private static final Identifier SPELLBOOK_ID = new Identifier("hexcasting", "spellbook");

    private int originalPageIdx = -1;
    private net.minecraft.util.Hand activeHand = null;
    private boolean selectionConfirmed = false;

    private static final int BUTTON_WIDTH = 30;
    private static final int BUTTON_HEIGHT = 20;
    private static final int COLUMNS = 8;
    private static final int START_X = 50;
    private static final int START_Y = 50;

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

        var player = client.player;
        var main = player.getMainHandStack();
        var off = player.getOffHandStack();

        ItemStack currentBook = ItemStack.EMPTY;
        net.minecraft.util.Hand currentHand = null;

        if (!main.isEmpty() && Registries.ITEM.getId(main.getItem()).equals(SPELLBOOK_ID)) {
            currentBook = main;
            currentHand = net.minecraft.util.Hand.MAIN_HAND;
        } else if (!off.isEmpty() && Registries.ITEM.getId(off.getItem()).equals(SPELLBOOK_ID)) {
            currentBook = off;
            currentHand = net.minecraft.util.Hand.OFF_HAND;
        }

        if (currentBook.isEmpty()) {
            close();
            return;
        }

        NbtCompound nbt = currentBook.getNbt();
        this.originalPageIdx = (nbt != null && nbt.contains("page_idx", NbtElement.INT_TYPE))
            ? nbt.getInt("page_idx")
            : 1;
        this.activeHand = currentHand;
        this.selectionConfirmed = false;
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

        var player = client.player;
        var main = player.getMainHandStack();
        var off = player.getOffHandStack();
        ItemStack currentBook = ItemStack.EMPTY;

        if (!main.isEmpty() && Registries.ITEM.getId(main.getItem()).equals(SPELLBOOK_ID)) {
            currentBook = main;
        } else if (!off.isEmpty() && Registries.ITEM.getId(off.getItem()).equals(SPELLBOOK_ID)) {
            currentBook = off;
        }

        if (currentBook.isEmpty()) {
            renderButtons(context, mouseX, mouseY, 1);
            return;
        }

        int currentPageIdx = 1;
        NbtCompound nbt = currentBook.getNbt();
        if (nbt != null && nbt.contains("page_idx", NbtElement.INT_TYPE)) {
            currentPageIdx = nbt.getInt("page_idx");
        }

        int hoveredIndex = getButtonAt(mouseX, mouseY);
        if (hoveredIndex != -1) {
            int targetPage = hoveredIndex + 1;

            // Получаем кастомное имя страницы
            String customName = getCustomPageName(currentBook, targetPage);

            // Получаем паттерны для этой страницы
            List<Text> patternsText = getPatternTooltipLines(currentBook, targetPage);

            List<Text> finalTooltip = new java.util.ArrayList<>();

            if (customName != null && !customName.isEmpty()) {
                finalTooltip.add(Text.literal(customName));
            } else if (!patternsText.isEmpty()) {
                // Если имени нет, но есть паттерны — можно показать что-то вроде "Страница 5"
                finalTooltip.add(Text.literal("Страница " + targetPage));
            }
            // Если имени нет и паттернов нет — ничего не добавляем (пустой тултип)

            finalTooltip.addAll(patternsText);

            if (!finalTooltip.isEmpty()) {
                context.drawTooltip(textRenderer, finalTooltip, mouseX, mouseY);
            }
        }

        renderButtons(context, mouseX, mouseY, currentPageIdx);
    }

    private void renderButtons(DrawContext context, int mouseX, int mouseY, int currentPageIdx) {
        for (int i = 0; i < 64; i++) {
            int row = i / COLUMNS;
            int col = i % COLUMNS;
            int x = START_X + col * (BUTTON_WIDTH + 2);
            int y = START_Y + row * (BUTTON_HEIGHT + 2);

            boolean isCurrent = (i + 1 == currentPageIdx);
            boolean isHovered = (mouseX >= x && mouseX < x + BUTTON_WIDTH && mouseY >= y && mouseY < y + BUTTON_HEIGHT);

            int color = isCurrent ? 0xFF44AA44 :
                       isHovered ? 0xFF555555 :
                                   0xFF444444;

            context.fill(x, y, x + BUTTON_WIDTH, y + BUTTON_HEIGHT, color);
            context.drawHorizontalLine(x, x + BUTTON_WIDTH, y, 0xFFFFFFFF);
            context.drawHorizontalLine(x, x + BUTTON_WIDTH, y + BUTTON_HEIGHT - 1, 0xFFFFFFFF);
            context.drawVerticalLine(x, y, y + BUTTON_HEIGHT, 0xFFFFFFFF);
            context.drawVerticalLine(x + BUTTON_WIDTH - 1, y, y + BUTTON_HEIGHT, 0xFFFFFFFF);

            String label = String.valueOf(i + 1);
            int textX = x + (BUTTON_WIDTH - textRenderer.getWidth(label)) / 2;
            int textY = y + (BUTTON_HEIGHT - 9) / 2;
            context.drawText(textRenderer, label, textX, textY, 0xFFFFFF, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || client == null || client.player == null || activeHand == null) {
            close();
            return true;
        }

        int clickedIndex = getButtonAt((int) mouseX, (int) mouseY);
        if (clickedIndex == -1) {
            close();
            return true;
        }

        selectionConfirmed = true;
        com.hexsbm.HexSBMClient.sendChangeSpellbookPage(activeHand, clickedIndex + 1);
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
        if (originalPageIdx == -1 || activeHand == null || client == null || client.player == null) {
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

    private int getButtonAt(int mouseX, int mouseY) {
        for (int i = 0; i < 64; i++) {
            int row = i / COLUMNS;
            int col = i % COLUMNS;
            int x = START_X + col * (BUTTON_WIDTH + 2);
            int y = START_Y + row * (BUTTON_HEIGHT + 2);

            if (mouseX >= x && mouseX < x + BUTTON_WIDTH &&
                mouseY >= y && mouseY < y + BUTTON_HEIGHT) {
                return i;
            }
        }
        return -1;
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
            return java.util.Collections.emptyList();
        }

        NbtCompound pages = nbt.getCompound("pages");
        String pageKey = String.valueOf(page);

        if (!pages.contains(pageKey, NbtElement.COMPOUND_TYPE)) {
            return java.util.Collections.emptyList(); // пустая страница
        }

        // Чтобы не писать парсер вручную, используем fakeBook + getTooltip, но БЕЗ первой строки
        ItemStack fakeBook = book.copy();
        fakeBook.getOrCreateNbt().putInt("page_idx", page);
        List<Text> fullTooltip = fakeBook.getTooltip(client.player, TooltipContext.Default.BASIC);

        if (fullTooltip.size() <= 1) {
            return java.util.Collections.emptyList();
        }

        // Пропускаем первую строку (название книги/страницы) — она нам НЕ нужна
        return fullTooltip.subList(1, fullTooltip.size());
    }
}