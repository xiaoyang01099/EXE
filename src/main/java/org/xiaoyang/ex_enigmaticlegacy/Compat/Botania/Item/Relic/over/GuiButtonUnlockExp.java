package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class GuiButtonUnlockExp extends Button {
    protected final Player player;
    protected final int expCost;

    protected GuiButtonUnlockExp(int x, int y, int width, int height, Player player, int cost, Button.OnPress onPress) {
        super(Button.builder(Component.empty(), onPress).pos(x, y).size(width, height));
        this.player = player;
        this.expCost = cost;
        updateDisplay();
    }

    public GuiButtonUnlockExp(int x, int y, int width, int height, Player player, int cost) {
        this(x, y, width, height, player, cost, btn -> {});
    }

    protected void updateDisplay() {
        int playerExp = (int) UtilExperience.getExpTotal(player);
        if (playerExp < expCost) {
            this.active = false;
            this.setMessage(Component.literal(playerExp + "/" + expCost + " XP"));
        } else {
            this.active = true;
            this.setMessage(Component.literal(expCost + " XP"));
        }
    }
}