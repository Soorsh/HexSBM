package com.hexsbm.screen;

import com.hexsbm.config.ConfigManager;
import com.hexsbm.config.HexSBMConfig;
import com.hexsbm.screen.nbt.SpellbookNbtManager;
import com.hexsbm.screen.pigment.PigmentColorRegistry;
import com.hexsbm.screen.ui.ColorScheme;
import com.hexsbm.screen.ui.RadialRenderer;
import com.hexsbm.screen.ui.ConfigPanel;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class SpellBookScreen extends Screen {
    private static final Identifier SPELLBOOK_ID = new Identifier("hexcasting", "spellbook");
    private static final int GROUPS = 8, GROUP_SIZE = 8, TOTAL_PAGES = 64;
    private static final int PANEL_WIDTH = 220;

    private int lastUiColor = 0;
    private boolean lastUsePigment = false;
    private int pigmentColor = 0xFFFFFFFF;
    private Hand activeHand = null;
    private int centralGroup = 0, originalPageIdx = -1;
    private HexSBMConfig liveConfig;
    private ColorScheme colorScheme;
    private ConfigPanel configPanel; // ← инициализируем позже

    public SpellBookScreen() {
        super(Text.empty());
    }

    @Override
    public void init() {
        if (client == null || client.player == null) {
            close();
            return;
        }

        this.liveConfig = ConfigManager.getSavedConfig();

        ClientPlayerEntity p = client.player;
        if (liveConfig.usePigmentColor) {
            NbtCompound cc = p.writeNbt(new NbtCompound()).getCompound("cardinal_components");
            if (cc.contains("hexcasting:favored_pigment", 10)) {
                NbtCompound pigment = cc.getCompound("hexcasting:favored_pigment");
                if (pigment.contains("pigment", 10)) {
                    String id = pigment.getCompound("pigment").getCompound("stack").getString("id");
                    pigmentColor = PigmentColorRegistry.getColor(id);
                }
            }
        } else {
            pigmentColor = liveConfig.uiBaseColor;
        }

        this.colorScheme = new ColorScheme(pigmentColor, liveConfig);

        ItemStack main = p.getMainHandStack(), off = p.getOffHandStack();
        if (!main.isEmpty() && Registries.ITEM.getId(main.getItem()).equals(SPELLBOOK_ID)) {
            activeHand = Hand.MAIN_HAND;
            originalPageIdx = SpellbookNbtManager.getPage(main);
        } else if (!off.isEmpty() && Registries.ITEM.getId(off.getItem()).equals(SPELLBOOK_ID)) {
            activeHand = Hand.OFF_HAND;
            originalPageIdx = SpellbookNbtManager.getPage(off);
        } else {
            close();
            return;
        }

        if (originalPageIdx != -1) {
            centralGroup = Math.max(0, Math.min(7, (originalPageIdx - 1) / GROUP_SIZE));
        }

        // 🔥 Инициализируем ConfigPanel здесь, когда liveConfig уже готов
        this.configPanel = new ConfigPanel(this.liveConfig);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private int panelX() {
        return width - PANEL_WIDTH;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        if (client == null || client.player == null) {
            close();
            return;
        }

        ItemStack book = getCurrentBook();
        int currentPage = book.isEmpty() ? 1 : SpellbookNbtManager.getPage(book);
        int cx = (int)(width * liveConfig.centerX);
        int cy = (int)(height * liveConfig.centerY);

        // Обновляем pigmentColor в реальном времени, если включён авто-цвет
        if (liveConfig.usePigmentColor && client != null && client.player != null) {
            ClientPlayerEntity p = client.player;
            NbtCompound cc = p.writeNbt(new NbtCompound()).getCompound("cardinal_components");
            if (cc.contains("hexcasting:favored_pigment", 10)) {
                NbtCompound pigment = cc.getCompound("hexcasting:favored_pigment");
                if (pigment.contains("pigment", 10)) {
                    String id = pigment.getCompound("pigment").getCompound("stack").getString("id");
                    this.pigmentColor = PigmentColorRegistry.getColor(id);
                }
            }
        }

        // Определяем текущий цвет
        int currentColor = liveConfig.usePigmentColor ? this.pigmentColor : liveConfig.uiBaseColor;

        // Обновляем цветовую схему, если что-то изменилось
        if (currentColor != lastUiColor || liveConfig.usePigmentColor != lastUsePigment) {
            this.lastUiColor = currentColor;
            this.lastUsePigment = liveConfig.usePigmentColor;
            this.colorScheme = new ColorScheme(currentColor, liveConfig);
        }

        // Outer ring — pages
        for (int i = 0; i < GROUPS; i++) {
            int page = centralGroup * GROUP_SIZE + i + 1;
            if (page > TOTAL_PAGES) continue;
            RadialRenderer.SectorAngles ang = new RadialRenderer.SectorAngles(i, GROUPS);
            boolean cur = page == currentPage;
            boolean hover = RadialRenderer.isPointInSegment(mx, my, cx, cy,
                liveConfig.outerRingInnerRadius, liveConfig.outerRingOuterRadius, ang.start, ang.end);

            RadialRenderer.fillSegment(ctx, cx, cy, liveConfig.outerRingInnerRadius, liveConfig.outerRingOuterRadius,
                ang.start, ang.end,
                colorScheme.getOuterInnerColor(cur, hover),
                colorScheme.getOuterOuterColor(cur, hover),
                liveConfig.segmentResolution);

            int r = Math.max(0, (liveConfig.outerRingInnerRadius + liveConfig.outerRingOuterRadius) / 2 + liveConfig.outerIconRadiusOffset);
            ctx.drawItem(SpellbookNbtManager.getPageIcon(book, page), (int)(cx + r * Math.cos(ang.mid)) - 8, (int)(cy + r * Math.sin(ang.mid)) - 8);

            if (hover && liveConfig.enableTooltips) {
                List<Text> tip = new ArrayList<>();
                String name = SpellbookNbtManager.getCustomPageName(book, page);
                tip.add(Text.literal(name != null && !name.isEmpty() ? name : "Page " + page));
                tip.addAll(SpellbookNbtManager.getPatternTooltip(book, page, client.player, liveConfig));
                ctx.drawTooltip(textRenderer, tip, mx, my);
            }
        }

        // Inner ring — groups
        for (int i = 0; i < GROUPS; i++) {
            RadialRenderer.SectorAngles ang = new RadialRenderer.SectorAngles(i, GROUPS);
            boolean cur = i == centralGroup;
            boolean hover = RadialRenderer.isPointInSegment(mx, my, cx, cy,
                liveConfig.innerRingInnerRadius, liveConfig.innerRingOuterRadius, ang.start, ang.end);

            RadialRenderer.fillSegment(ctx, cx, cy, liveConfig.innerRingInnerRadius, liveConfig.innerRingOuterRadius,
                ang.start, ang.end,
                colorScheme.getInnerInnerColor(cur, hover),
                colorScheme.getInnerOuterColor(cur, hover),
                liveConfig.segmentResolution);

            int r = Math.max(0, (liveConfig.innerRingInnerRadius + liveConfig.innerRingOuterRadius) / 2 + liveConfig.innerIconRadiusOffset);
            ctx.drawItem(SpellbookNbtManager.getGroupIcon(book, i), (int)(cx + r * Math.cos(ang.mid)) - 8, (int)(cy + r * Math.sin(ang.mid)) - 8);
        }

        // === ОТОБРАЖАЕМ ПАНЕЛЬ ВСЕГДА ===
        configPanel.render(ctx, panelX(), liveConfig, textRenderer, mx, my);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (client == null || client.player == null || activeHand == null) {
            close();
            return true;
        }

        int mx = (int) mouseX, my = (int) mouseY;
        int px = panelX();

        if (mx > px) {
            return configPanel.mouseClicked(mx, my, px, liveConfig, this.textRenderer);
        }

        int cx = (int)(width * liveConfig.centerX);
        int cy = (int)(height * liveConfig.centerY);

        if (button == 0) {
            for (int i = 0; i < GROUPS; i++) {
                RadialRenderer.SectorAngles ang = new RadialRenderer.SectorAngles(i, GROUPS);
                if (RadialRenderer.isPointInSegment(mx, my, cx, cy, liveConfig.innerRingInnerRadius, liveConfig.innerRingOuterRadius, ang.start, ang.end)) {
                    centralGroup = i;
                    return true;
                }
            }
            for (int i = 0; i < GROUPS; i++) {
                int page = centralGroup * GROUP_SIZE + i + 1;
                if (page > TOTAL_PAGES) continue;
                RadialRenderer.SectorAngles ang = new RadialRenderer.SectorAngles(i, GROUPS);
                if (RadialRenderer.isPointInSegment(mx, my, cx, cy, liveConfig.outerRingInnerRadius, liveConfig.outerRingOuterRadius, ang.start, ang.end)) {
                    com.hexsbm.HexSBMClient.sendChangeSpellbookPage(activeHand, page);
                    close();
                    return true;
                }
            }
            if (liveConfig.closeOnBackgroundClick) close();
            return true;
        }

        if (button == 1) {
            ItemStack book = getCurrentBook();
            if (book.isEmpty()) return false;

            for (int i = 0; i < GROUPS; i++) {
                RadialRenderer.SectorAngles ang = new RadialRenderer.SectorAngles(i, GROUPS);
                if (RadialRenderer.isPointInSegment(mx, my, cx, cy, liveConfig.innerRingInnerRadius, liveConfig.innerRingOuterRadius, ang.start, ang.end)) {
                    updateGroupIcon(book, i);
                    return true;
                }
            }
            for (int i = 0; i < GROUPS; i++) {
                int page = centralGroup * GROUP_SIZE + i + 1;
                if (page > TOTAL_PAGES) continue;
                RadialRenderer.SectorAngles ang = new RadialRenderer.SectorAngles(i, GROUPS);
                if (RadialRenderer.isPointInSegment(mx, my, cx, cy, liveConfig.outerRingInnerRadius, liveConfig.outerRingOuterRadius, ang.start, ang.end)) {
                    updatePageIcon(book, page);
                    return true;
                }
            }
        }
        return true;
    }

    private void updateGroupIcon(ItemStack book, int idx) {
        ItemStack src = SpellbookNbtManager.createIconFromHotbar(client.player, liveConfig);
        SpellbookNbtManager.updateIcon(book, "group_icons", idx, src);
        com.hexsbm.HexSBMClient.sendUpdateGroupIcon(activeHand, idx, src);
    }

    private void updatePageIcon(ItemStack book, int page) {
        ItemStack src = SpellbookNbtManager.createIconFromHotbar(client.player, liveConfig);
        SpellbookNbtManager.updateIcon(book, "page_icons", page, src);
        com.hexsbm.HexSBMClient.sendUpdatePageIcon(activeHand, page, src);
    }

    @Override
    public void close() {
        configPanel.close(liveConfig);
        // Сохраняем конфиг при закрытии
        ConfigManager.saveConfig(this.liveConfig);
        if (client != null) client.setScreen(null);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int mods) {
        if (configPanel.keyPressed(keyCode, scanCode, mods, liveConfig)) {
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
        int px = panelX();

        // Если мышь над панелью настроек — обрабатываем ТОЛЬКО панель
        if (mx > px) {
            return configPanel.mouseScrolled(mx, my, amount, px, liveConfig, this.height);
        }

        // Иначе — стандартное поведение (смена слота)
        if (client == null || client.player == null || activeHand == null) return false;
        if (activeHand == Hand.OFF_HAND) {
            PlayerInventory inv = client.player.getInventory();
            int current = inv.selectedSlot;
            inv.selectedSlot = amount > 0 ? (current == 0 ? 8 : current - 1) : (current == 8 ? 0 : current + 1);
            return true;
        }
        return false;
    }

    private ItemStack getCurrentBook() {
        ClientPlayerEntity p = client.player;
        ItemStack main = p.getMainHandStack(), off = p.getOffHandStack();
        if (!main.isEmpty() && Registries.ITEM.getId(main.getItem()).equals(SPELLBOOK_ID)) return main;
        if (!off.isEmpty() && Registries.ITEM.getId(off.getItem()).equals(SPELLBOOK_ID)) return off;
        return ItemStack.EMPTY;
    }
}