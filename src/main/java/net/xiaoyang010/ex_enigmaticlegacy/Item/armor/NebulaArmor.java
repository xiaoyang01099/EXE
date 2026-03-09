package net.xiaoyang010.ex_enigmaticlegacy.Item.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.ModelArmorNebula;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.SpecialMiscellaneousModels;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModArmors;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.api.EXEAPI;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.item.IManaProficiencyArmor;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.gui.TooltipHandler;
import vazkii.botania.common.item.equipment.armor.manasteel.ItemManasteelArmor;

import java.util.*;
import java.util.function.Consumer;

public class NebulaArmor extends ItemManasteelArmor implements IManaItem, IManaProficiencyArmor {
    private static final String TAG_MANA = "mana";
    private static final int MAX_MANA = 250000;
    protected static final float MAX_SPEED = 0.275F;
    public static final List<String> playersWithStepup = new ArrayList<>();
    public static final List<String> playersWithFeet = new ArrayList<>();
    public static final String NBT_FALL = ExEnigmaticlegacyMod.MODID + ":nebula_armor";
    private static final ThreadLocal<ItemStack> CURRENT_STACK = new ThreadLocal<>();

    public static TextureAtlasSprite nebulaEyes;
    private static final UUID CHEST_UUID = UUID.fromString("6d88f904-e22f-7cfa-8c66-c0bee4e40289");
    private static final UUID HEAD_UUID = UUID.fromString("cfb111e4-9caa-12bf-6a67-01bccaabe34d");
    private static final UUID HEAD_REVEAL_UUID = UUID.fromString("584424ee-c473-d5b7-85b9-aa4081577bd7");

    private static final Properties NEBULA_ARMOR = new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR).durability(1000).rarity(EXEAPI.rarityNebula);

    public NebulaArmor(EquipmentSlot slot) {
        super(slot, ArmorMaterials.NETHERITE, NEBULA_ARMOR);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @OnlyIn(Dist.CLIENT)
    public static void initTextures() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS) != null) {
            try {
                nebulaEyes = mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(SpecialMiscellaneousModels.INSTANCE.nebula_eyes.texture());
                ExEnigmaticlegacyMod.LOGGER.info("Nebula eyes texture loaded successfully");
            } catch (Exception e) {
                ExEnigmaticlegacyMod.LOGGER.error("Failed to load nebula eyes texture", e);
                nebulaEyes = mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(MissingTextureAtlasSprite.getLocation());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static TextureAtlasSprite getNebulaEyesTexture() {
        if (nebulaEyes == null) {
            initTextures();
        }

        if (nebulaEyes == null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS) != null) {
                nebulaEyes = mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(SpecialMiscellaneousModels.INSTANCE.nebula_eyes.texture());
            }
        }

        return nebulaEyes;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> list, TooltipFlag flags) {
        TooltipHandler.addOnShift(list, () -> {
            this.addInformationAfterShift(stack, world, list, flags);
        });
        list.add(new TranslatableComponent("item.info.mana",
                getManaInternal(stack),
                getMaxMana())
                .withStyle(ChatFormatting.AQUA));

        list.add(new TranslatableComponent("item.info.durability",
                stack.getMaxDamage() - stack.getDamageValue(),
                stack.getMaxDamage())
                .withStyle(ChatFormatting.GREEN));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attributes = getDefaultAttributeModifiers(slot);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();

        builder.putAll(attributes);
        if (slot == EquipmentSlot.CHEST) {
            builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(CHEST_UUID, "Nebula Chest modifier",
                    1.0F - (float) getDamage(stack) / 1000.0F, AttributeModifier.Operation.ADDITION));
        }else if (slot == EquipmentSlot.HEAD && stack.getItem() == ModArmors.NEBULA_HELMET_REVEAL.get()) {
            builder.put(Attributes.MAX_HEALTH, new AttributeModifier(HEAD_REVEAL_UUID, "Nebula Helm Reveal modifier",
                    20.0F * (1.0F - (float)getDamage(stack) / 1000.0F), AttributeModifier.Operation.ADDITION));
        }else if (slot == EquipmentSlot.HEAD && stack.getItem() == ModArmors.NEBULA_HELMET.get()) {
            builder.put(Attributes.MAX_HEALTH, new AttributeModifier(HEAD_UUID, "Nebula Helm modifier",
                    20.0F * (1.0F - (float)getDamage(stack) / 1000.0F), AttributeModifier.Operation.ADDITION));
        }

        return builder.build();
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> stacks) {
        if (this.allowdedIn(tab)){
            ItemStack stack = new ItemStack(this);
            stack.getOrCreateTag().putBoolean("Unbreakable",true);
            stacks.add(stack);
        }
    }

    @Override
    public String getArmorTextureAfterInk(ItemStack stack, EquipmentSlot slot) {
        return ExEnigmaticlegacyMod.MODID + ":textures/models/armor/nebula_armor.png";
    }

    public float getEfficiency(ItemStack stack, Player player) {
        if (this.slot == EquipmentSlot.HEAD) {
            return 0.3f;
        }
        return 0;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (world.isClientSide() || world.getGameTime() % 5 != 0) return;

        CURRENT_STACK.set(stack);
        try {
            BlockPos pos = entity.blockPosition();
            for (BlockPos checkPos : BlockPos.betweenClosed(
                    pos.offset(-2, -2, -2),
                    pos.offset(2, 2, 2))) {
                BlockEntity be = world.getBlockEntity(checkPos);
                if (be instanceof IManaPool pool) {
                    int space = getMaxMana() - getMana();
                    if (space > 0 && pool.getCurrentMana() > 0) {
                        int manaToTransfer = Math.min(25000, Math.min(space, pool.getCurrentMana()));
                        pool.receiveMana(-manaToTransfer);
                        addMana(manaToTransfer);
                        break;
                    }
                }
            }
        } finally {
            CURRENT_STACK.remove();
        }
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        CURRENT_STACK.set(stack);
        try {
            if (!level.isClientSide() && getMana() != getMaxMana() &&
                    ManaItemHandler.instance().requestManaExactForTool(stack, player, 1000, true)) {
                addMana(1000);
            }

            ItemStack itemBySlot = player.getItemBySlot(this.slot);

            if (!level.isClientSide || itemBySlot.isEmpty()) return;

            if (this.slot == EquipmentSlot.FEET && itemBySlot.getItem() == ModArmors.NEBULA_BOOTS.get() && player.isSprinting()) {
                float r = 0.6F + (float)Math.random() * 0.4f;
                float g = 0.6F + (float)Math.random() * 0.4f;
                float b = 0.6F + (float)Math.random() * 0.4f;

                for(int i = 0; i < 2; ++i) {
                    BotaniaAPI.instance().sparkleFX(level, player.getX() + (Math.random() - 0.5F), player.getY() - 1.25F + (Math.random() / 4.0F - 0.125F), player.getZ() + (Math.random() - 0.5F),
                            r, g, b, 0.7F + (float)Math.random() / 2.0F, 25);
                }
            }else if (this.slot == EquipmentSlot.HEAD && itemBySlot.getItem() == ModArmors.NEBULA_HELMET.get()) {
                NebulaArmorHelper.foodToHeal(player);
                NebulaArmorHelper.dispatchManaExact(stack, player, 2, true);
            }else if (this.slot == EquipmentSlot.HEAD && itemBySlot.getItem() == ModArmors.NEBULA_HELMET_REVEAL.get()) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 4, false, false));
                if (player.hasEffect(MobEffects.WITHER)) {
                    player.removeEffect(MobEffects.WITHER);
                }
                NebulaArmorHelper.foodToHeal(player);
                NebulaArmorHelper.dispatchManaExact(stack, player, 2, true);
            }
        } finally {
            CURRENT_STACK.remove();
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && this.slot == EquipmentSlot.HEAD) {
            return InteractionResultHolder.success(stack);
        }

        return super.use(level, player, hand);
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        consumer.accept(new IItemRenderProperties() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public HumanoidModel<?> getArmorModel(LivingEntity entity,
                                                                             ItemStack stack, EquipmentSlot slot, HumanoidModel<?> defaultModel) {
                ModelPart modelPart = Minecraft.getInstance().getEntityModels()
                        .bakeLayer(ModelArmorNebula.LAYER_LOCATION);

                HumanoidModel<?> armorModel;

                if (slot == EquipmentSlot.FEET) {
                    armorModel = new HumanoidModel<>(new ModelPart(Collections.emptyList(),
                            Map.of("right_leg", new ModelArmorNebula<>(modelPart, slot).rightBoot,
                                    "left_leg", new ModelArmorNebula<>(modelPart, slot).leftBoot,
                                    "head", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                    "hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                    "body", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                    "right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                    "left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
                } else {
                    armorModel = new ModelArmorNebula<>(modelPart, slot);
                }

                if (armorModel instanceof ModelArmorNebula<?> nebulaModel) {
                    nebulaModel.setCurrentEntity(entity);
                }
                armorModel.crouching = entity.isShiftKeyDown();
                armorModel.riding = defaultModel.riding;
                armorModel.young = entity.isBaby();
                return armorModel;
            }
        });
    }

    public boolean hasArmorSetItem(Player player, EquipmentSlot slot) {
        if (player == null) return false;
        return NebulaArmorHelper.hasNebulaArmorPiece(player, slot);
    }

    @Override
    public int getMana() {
        ItemStack stack = CURRENT_STACK.get();
        if (stack != null) {
            return getManaInternal(stack);
        }
        return 0;
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public void addMana(int mana) {
        ItemStack stack = CURRENT_STACK.get();
        if (stack != null) {
            int currentMana = getManaInternal(stack);
            int newMana = Math.min(currentMana + mana, getMaxMana());
            setManaInternal(stack, newMana);
        }
    }

    @Override
    public boolean canReceiveManaFromPool(BlockEntity pool) {
        return true;
    }

    @Override
    public boolean canReceiveManaFromItem(ItemStack otherStack) {
        return true;
    }

    @Override
    public boolean canExportManaToPool(BlockEntity pool) {
        return false;
    }

    @Override
    public boolean canExportManaToItem(ItemStack otherStack) {
        return false;
    }

    @Override
    public boolean isNoExport() {
        return true;
    }

    public static int getManaInternal(ItemStack stack) {
        return stack.getOrCreateTag().getInt(TAG_MANA);
    }

    public static void setManaInternal(ItemStack stack, int mana) {
        stack.getOrCreateTag().putInt(TAG_MANA, Math.min(mana, MAX_MANA));
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        int manaInternal = getManaInternal(stack);
        if (manaInternal > 100){
            int i = manaInternal % 100;
            amount -= i;
            setManaInternal(stack, manaInternal - i * 100);
            return super.damageItem(stack, amount, entity, onBroken);
        }
        return super.damageItem(stack, amount, entity, onBroken);
    }

    @SubscribeEvent
    public void updatePlayerStepStatus(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntity() instanceof Player player) {
            String playerStr = player.getGameProfile().getName() + ":" + player.level.isClientSide;

            ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
            boolean isFeet = feet.getItem() == ModArmors.NEBULA_BOOTS.get();
            if (playersWithFeet.contains(playerStr)){
                if (isFeet){
                    if (player.getAbilities().getFlyingSpeed() != .25f) {
                        player.getAbilities().setFlyingSpeed(.25f);
                        player.onUpdateAbilities();
                    }
                }else {
                    if (player.getAbilities().getFlyingSpeed() != 0.05f) {
                        player.getAbilities().setFlyingSpeed(0.05f);
                        player.onUpdateAbilities();
                    }
                    playersWithFeet.remove(playerStr);
                }
            }else if (isFeet){
                playersWithFeet.add(playerStr);
            }

            if (playersWithStepup.contains(playerStr)) {
                if (NebulaArmorHelper.shouldPlayerHaveStepup(player)) {
                    if (!player.level.isClientSide)
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 5, 3));
                    player.maxUpStep = player.isCrouching() ? 1.0F : 1.5F;
                } else {
                    if (player.maxUpStep > 1.0F)
                        player.maxUpStep = 1.0F;
                    playersWithStepup.remove(playerStr);
                }
            } else if (NebulaArmorHelper.shouldPlayerHaveStepup(player)) {
                playersWithStepup.add(playerStr);
                player.maxUpStep = 1.5F;
            }

        }
    }

    @SubscribeEvent
    public void onPlayerJump(LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
            if (!legs.isEmpty() && legs.getItem() == ModArmors.NEBULA_LEGGINGS.get()) {
                player.setDeltaMovement(player.getDeltaMovement().add(
                        0, getJump(legs), 0
                ));
                player.fallDistance = -getFallBuffer(legs);
                player.getPersistentData().putBoolean(NBT_FALL, true);
            }
        }
    }

    private float getJump(ItemStack stack) {
        return 0.2F * (1.0F - (float)getDamage(stack) / 1000.0F);
    }

    private float getFallBuffer(ItemStack stack) {
        return 12.0F * (1.0F - (float)getDamage(stack) / 1000.0F);
    }
}