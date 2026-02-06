package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.over;

import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.player.Player;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;

public class GuiButtonUnlockExp extends Button {
    protected final Player player;
    protected final int expCost;

    public GuiButtonUnlockExp(int x, int y, int width, int height, Player player, int cost) {
        super(x, y, width, height, EComponent.empty(), GuiButtonUnlockExp::defaultOnPress);
        this.player = player;
        this.expCost = cost;
        updateDisplay();
    }

    protected void updateDisplay() {
        int playerExp = (int) UtilExperience.getExpTotal(player);

        if (playerExp < expCost) {
            this.active = false;
            this.setMessage(EComponent.literal(playerExp + "/" + expCost + " XP"));
        } else {
            this.active = true;
            this.setMessage(EComponent.literal(expCost + " XP"));
        }
    }

    private static void defaultOnPress(Button button) {
    }
}