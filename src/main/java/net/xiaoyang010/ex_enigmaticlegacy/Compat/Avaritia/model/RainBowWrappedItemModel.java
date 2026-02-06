package net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia.model;

import codechicken.lib.util.TransformUtils;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.data.IModelData;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class RainBowWrappedItemModel implements BakedModel {
    protected BakedModel wrapped;
    protected ModelState parentState;
    @Nullable
    protected LivingEntity entity;
    @Nullable
    protected ClientLevel world;

    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    protected ItemOverrides overrideList;

    public RainBowWrappedItemModel(BakedModel wrapped) {
        this.overrideList = new ItemOverrides() {
            @Override
            public BakedModel resolve(final @NotNull BakedModel originalModel, final @NotNull ItemStack stack,
                                      final ClientLevel world, final LivingEntity entity, final int seed) {
                RainBowWrappedItemModel.this.entity = entity;
                RainBowWrappedItemModel.this.world = world != null ? world : (entity != null ? (ClientLevel)entity.getLevel() : null);

                if (RainBowWrappedItemModel.this.isCosmic()) {
                    return RainBowWrappedItemModel.this.wrapped.getOverrides().resolve(originalModel, stack, world, entity, seed);
                }
                return originalModel;
            }
        };
        this.wrapped = wrapped;
        this.parentState = TransformUtils.stateFromItemTransforms(wrapped.getTransforms());
    }

    public static List<BakedQuad> bakeItem(final List<TextureAtlasSprite> sprites) {
        final LinkedList<BakedQuad> quads = new LinkedList<>();
        for (final TextureAtlasSprite sprite : sprites) {
            final List<BlockElement> unbaked = ITEM_MODEL_GENERATOR.processFrames(
                    sprites.indexOf(sprite),
                    "layer" + sprites.indexOf(sprite),
                    sprite
            );
            for (final BlockElement element : unbaked) {
                for (final Map.Entry<Direction, BlockElementFace> entry : element.faces.entrySet()) {
                    quads.add(FACE_BAKERY.bakeQuad(
                            element.from,
                            element.to,
                            entry.getValue(),
                            sprite,
                            entry.getKey(),
                            new SimpleModelState(ImmutableMap.of()),
                            element.rotation,
                            element.shade,
                            ExEnigmaticlegacyMod.path("dynamic")
                    ));
                }
            }
        }
        return quads;
    }

    public static <E> void checkArgument(final E argument, final Predicate<E> predicate) {
        if (predicate.test(argument)) {
            throw new RuntimeException("");
        }
    }

    public static <T> boolean isNullOrContainsNull(final T[] input) {
        if (input != null) {
            for (final T t : input) {
                if (t == null) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public boolean isCosmic() {
        return false;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(BlockState state, Direction side, @NotNull Random rand) {
        return Collections.emptyList();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return this.wrapped.getParticleIcon();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon(@NotNull IModelData data) {
        return this.wrapped.getParticleIcon(data);
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return this.overrideList;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.wrapped.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.wrapped.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return this.wrapped.usesBlockLight();
    }

    protected void renderWrapped(ItemStack stack, PoseStack pStack, MultiBufferSource buffers,
                                 int packedLight, int packedOverlay, boolean fabulous) {
        renderWrapped(stack, pStack, buffers, packedLight, packedOverlay, fabulous, Function.identity());
    }

    protected void renderWrapped(ItemStack stack, PoseStack pStack, MultiBufferSource buffers,
                                 int packedLight, int packedOverlay, boolean fabulous,
                                 Function<VertexConsumer, VertexConsumer> consOverride) {
        BakedModel model = this.wrapped.getOverrides().resolve(this.wrapped, stack, this.world, this.entity, 0);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        RenderType renderType = ItemBlockRenderTypes.getRenderType(stack, fabulous);
        VertexConsumer vertexConsumer = consOverride.apply(
                ItemRenderer.getFoilBuffer(buffers, renderType, true, stack.hasFoil())
        );

        if (model != null) {
            itemRenderer.renderModelLists(model, stack, packedLight, packedOverlay, pStack, vertexConsumer);
        }
    }
}