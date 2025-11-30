package com.hexsbm.keybinds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.registry.Registries;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import com.hexsbm.screen.SpellBookScreen;

@Environment(EnvType.CLIENT)
public class KeyBindManager {

    private static final Identifier SPELLBOOK_ID = new Identifier("hexcasting", "spellbook");

    public static void registerKeyBinds() {
        KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.hexsbm.spellbook_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.hexsbm.binds"
            )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!keyBinding.wasPressed()) return;

            PlayerEntity player = client.player;
            if (player == null) return;

            ItemStack mainHand = player.getMainHandStack();
            ItemStack offHand = player.getOffHandStack();

            boolean hasSpellbook = (!mainHand.isEmpty() && Registries.ITEM.getId(mainHand.getItem()).equals(SPELLBOOK_ID))
                                 || (!offHand.isEmpty() && Registries.ITEM.getId(offHand.getItem()).equals(SPELLBOOK_ID));

            if (hasSpellbook) {
                client.setScreen(new SpellBookScreen());
            }
        });
    }
}