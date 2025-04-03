
package net.xiaoyang010.ex_enigmaticlegacy.Init;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


@OnlyIn(Dist.CLIENT)
public class ModKeyBindings {
    public static KeyMapping swordLevelKey;

    public static void init(final FMLClientSetupEvent event) {
        swordLevelKey = new KeyMapping(
                "key.ex_enigmaticlegacy.sword_level",
                InputConstants.KEY_MULTIPLY,
                "key.categories.ex_enigmaticlegacy"
        );
        ClientRegistry.registerKeyBinding(swordLevelKey);
    }

    public static boolean isSwordLevelKeyDown() {
        return swordLevelKey != null && swordLevelKey.isDown();
    }
}