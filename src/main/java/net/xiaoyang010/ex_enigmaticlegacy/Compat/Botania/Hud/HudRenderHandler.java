package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.api.IRenderHud;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile.TileEngineerHopper;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic.BlackHalo;
import vazkii.botania.common.item.ItemTwigWand;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class HudRenderHandler {

    @SubscribeEvent
    public static void onDrawScreenPost(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
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
                canRender = equippedStack.getItem() instanceof ItemTwigWand;
            }

            if (tile instanceof TileEngineerHopper &&
                    equippedStack != null &&
                    equippedStack.getItem() instanceof ItemTwigWand) {
                profiler.push("engineerHopper");
                PoseStack poseStack = event.getMatrixStack();
                ((TileEngineerHopper) tile).renderHUD(poseStack, mc);
                profiler.pop();
            }
            else if (tile instanceof IRenderHud && !canRender) {
                profiler.push("renderHud");
                PoseStack poseStack = event.getMatrixStack();
                ((IRenderHud) tile).renderHud(mc, poseStack,
                        mc.getWindow().getGuiScaledWidth(),
                        mc.getWindow().getGuiScaledHeight());
                profiler.pop();
            }
        }

        if (equippedStack != null && !equippedStack.isEmpty() &&
                equippedStack.getItem() instanceof BlackHalo) {
            profiler.push("blackHalo");
            PoseStack poseStack = event.getMatrixStack();
            BlackHalo.renderHUD(mc, equippedStack, poseStack,
                    mc.getWindow().getGuiScaledWidth(),
                    mc.getWindow().getGuiScaledHeight());
            profiler.pop();
        }

        profiler.popPush("itemsRemainingAB");
        PoseStack poseStack = event.getMatrixStack();
        ItemsRemainingRender.render(poseStack, event.getPartialTicks());

        profiler.pop();
    }
}
