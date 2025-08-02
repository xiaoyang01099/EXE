package net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;
import net.xiaoyang010.ex_enigmaticlegacy.api.WanionApi.IF.*;
import org.apache.commons.lang3.tuple.Pair;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ControlWButton<C extends IStateProvider<C, S>, S extends IState<S>> extends WButton<ControlWButton<C, S>> {
    protected final C stateProvider;
    protected int lineWidth = 0;

    public ControlWButton(@NotNull final C stateProvider, @NotNull final WGuiContainer<?> wGuiContainer, final int x, final int y) {
        this(stateProvider, wGuiContainer, x, y, 18, 18);
    }

    public ControlWButton(@NotNull final C stateProvider, @NotNull final WGuiContainer<?> wGuiContainer, final int x, final int y, final int width, final int height) {
        super(wGuiContainer, x, y, width, height);
        this.stateProvider = stateProvider;
        setTooltipSupplier(new ControlWButtonTooltipSupplier());
    }

    @Override
    public void draw(@NotNull final WInteraction wInteraction) {
        final S state = stateProvider.getState();
        final ResourceLocation textureResourceLocation = state.getTextureResourceLocation();
        final Pair<Integer, Integer> texturePos = state.getTexturePos(wInteraction.isHovering(this));

        if (textureResourceLocation == null || texturePos == null) {
            return;
        }

        RenderSystem.setShaderTexture(0, textureResourceLocation);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();

        PoseStack poseStack = wInteraction.getPoseStack();

        GuiComponent.blit(poseStack, getUsableX(), getUsableY(),
                texturePos.getLeft(), texturePos.getRight(),
                width, height, 128, 128);
    }

    @Override
    public void interact(@NotNull final WMouseInteraction mouseInteraction) {
        final S state = stateProvider.getState();
        final CompoundTag compoundTag = new CompoundTag();
        final ListTag controlTagList = new ListTag();
        final CompoundTag controlNBT = new CompoundTag();
        stateProvider.writeToNBT(controlNBT, mouseInteraction.getMouseButton() == 0 ? state.getNextState() : state.getPreviousState());
        controlTagList.add(controlNBT);
        compoundTag.put("control", controlTagList);

        NetworkHandler.CHANNEL.sendToServer(new SmartNBTMessage(getWindowID(), compoundTag));
        playPressSound();
    }

    private class ControlWButtonTooltipSupplier implements ITooltipSupplier {
        @Override
        public List<Component> getTooltip(@NotNull final WInteraction interaction, @NotNull final Supplier<ItemStack> stackSupplier) {
            final List<Component> description = new ArrayList<>();
            final S state = stateProvider.getState();
            if (stateProvider instanceof IControlNameable) {
                if (state instanceof IStateNameable) {
                    Component controlName = EComponent.translatable(((IControlNameable) stateProvider).getControlName()).withStyle(ChatFormatting.RED);
                    Component stateName = EComponent.translatable(((IStateNameable) state).getStateName()).withStyle(ChatFormatting.WHITE);
                    description.add(controlName.copy().append(": ").append(stateName));
                } else {
                    description.add(EComponent.translatable(((IControlNameable) stateProvider).getControlName()).withStyle(ChatFormatting.RED));
                }
            }
            if (state instanceof IStateNameable) {
                final String desc = ((IStateNameable) state).getStateDescription();
                if (desc != null)
                    description.add(EComponent.translatable(desc));
            }
            lineWidth = 0;
            for (final Component line : description) {
                final int localLineWidth = getFontRenderer().width(line);
                if (localLineWidth > lineWidth)
                    lineWidth = localLineWidth;
            }
            return description;
        }
    }
}