package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.tile.TileEngineerHopper;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item.Relic.BlackHalo;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.api.exboapi.IRenderHud;
import vazkii.botania.common.item.WandOfTheForestItem;

@Mod.EventBusSubscriber(modid = Exe.MODID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class HudRenderHandler {

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        ProfilerFiller profiler = mc.getProfiler();

        ItemStack equippedStack = null;
        if (mc.player != null) {
            equippedStack = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
        }

        profiler.push("ex_enigmaticlegacy-hud");

        HitResult hitResult = mc.hitResult;
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            BlockPos pos = blockHit.getBlockPos();
            BlockEntity tile = null;
            if (mc.level != null) {
                tile = mc.level.getBlockEntity(pos);
            }

            boolean canRender = false;
            if (equippedStack != null) {
                canRender = equippedStack.getItem() instanceof WandOfTheForestItem;
            }

            if (tile instanceof TileEngineerHopper &&
                    equippedStack != null &&
                    equippedStack.getItem() instanceof WandOfTheForestItem) {
                profiler.push("engineerHopper");
                GuiGraphics guiGraphics = event.getGuiGraphics();
                ((TileEngineerHopper) tile).renderHUD(guiGraphics, mc);
                profiler.pop();
            } else if (tile instanceof IRenderHud && !canRender) {
                profiler.push("renderHud");
                GuiGraphics guiGraphics = event.getGuiGraphics();
                ((IRenderHud) tile).renderHud(mc, guiGraphics,
                        mc.getWindow().getGuiScaledWidth(),
                        mc.getWindow().getGuiScaledHeight());
                profiler.pop();
            }
        }

        if (equippedStack != null && !equippedStack.isEmpty() &&
                equippedStack.getItem() instanceof BlackHalo) {
            profiler.push("blackHalo");
            GuiGraphics guiGraphics = event.getGuiGraphics();
            BlackHalo.renderHUD(mc, equippedStack, guiGraphics,
                    mc.getWindow().getGuiScaledWidth(),
                    mc.getWindow().getGuiScaledHeight());
            profiler.pop();
        }

        profiler.push("itemsRemainingAB");
        GuiGraphics guiGraphics = event.getGuiGraphics();
        ItemsRemainingRender.render(guiGraphics, event.getPartialTick());

        profiler.pop();

        profiler.pop();
    }
}