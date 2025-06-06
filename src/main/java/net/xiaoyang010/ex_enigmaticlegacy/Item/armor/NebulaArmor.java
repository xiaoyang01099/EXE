package net.xiaoyang010.ex_enigmaticlegacy.Item.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
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
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.ModelArmorWildHunt;
import net.xiaoyang010.ex_enigmaticlegacy.Client.model.NebulaArmorModel;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModArmors;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.api.AdvancedBotanyAPI;
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

    private static final UUID CHEST_UUID = UUID.fromString("6d88f904-e22f-7cfa-8c66-c0bee4e40289");
    private static final UUID HEAD_UUID = UUID.fromString("cfb111e4-9caa-12bf-6a67-01bccaabe34d");
    private static final UUID HEAD_REVEAL_UUID = UUID.fromString("584424ee-c473-d5b7-85b9-aa4081577bd7");

    private static final Properties NEBULA_ARMOR = new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR).durability(1000).rarity(AdvancedBotanyAPI.rarityNebula);

    public NebulaArmor(EquipmentSlot slot) {
        super(slot, ArmorMaterials.NETHERITE, NEBULA_ARMOR);
        MinecraftForge.EVENT_BUS.register(this);
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
        BlockPos pos = entity.blockPosition();
        for (BlockPos checkPos : BlockPos.betweenClosed(
                pos.offset(-2, -2, -2),
                pos.offset(2, 2, 2))) {
            BlockEntity be = world.getBlockEntity(checkPos);
            if (be instanceof IManaPool pool) {
                int space = getMaxMana() - getManaInternal(stack);
                if (space > 0 && pool.getCurrentMana() > 0) {
                    int manaToTransfer = Math.min(25000, Math.min(space, pool.getCurrentMana()));
                    pool.receiveMana(-manaToTransfer);
                    addMana(manaToTransfer);
                    setManaInternal(stack, getManaInternal(stack) + manaToTransfer);
                    break;
                }
            }
        }
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
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
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            NebulaArmorHelper.setCosmicFace(stack, !NebulaArmorHelper.enableCosmicFace(stack));
            return InteractionResultHolder.success(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        consumer.accept(new IItemRenderProperties() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public net.minecraft.client.model.HumanoidModel<?> getArmorModel(LivingEntity entity,
                                                                             ItemStack stack, EquipmentSlot slot, net.minecraft.client.model.HumanoidModel<?> defaultModel) {
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

                armorModel.crouching = entity.isShiftKeyDown();
                armorModel.riding = defaultModel.riding;
                armorModel.young = entity.isBaby();
                return armorModel;
            }
        });
    }

    public boolean hasArmorSetItem(Player player, EquipmentSlot slot) {
        if (player == null) return false;
        ItemStack stack = player.getItemBySlot(slot);
        if (stack.isEmpty()) {
            return false;
        }

        return switch (slot) {
            case HEAD -> stack.getItem() == ModArmors.NEBULA_HELMET.get() ||
                    stack.getItem() == ModArmors.NEBULA_HELMET_REVEAL.get();
            case CHEST -> stack.getItem() == ModArmors.NEBULA_CHESTPLATE.get();
            case LEGS -> stack.getItem() == ModArmors.NEBULA_LEGGINGS.get();
            case FEET -> stack.getItem() == ModArmors.NEBULA_BOOTS.get();
            default -> false;
        };
    }

    @Override
    public int getMana() {
        return getManaInternal(new ItemStack(this));
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public void addMana(int mana) {
        setManaInternal(new ItemStack(this), getMana() + mana);
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
            int i = manaInternal % 100; //可以抵扣的耐久
            amount -= i;
            setManaInternal(stack, manaInternal - i * 100); //消耗100魔力 抵1点耐久
            return super.damageItem(stack, amount, entity, onBroken);
        }
        return super.damageItem(stack, amount, entity, onBroken);
    }

    @Override
    public boolean canReceiveManaFromPool(BlockEntity pool) {
        return true;  // 允许从魔力池接收魔力
    }

    @Override
    public boolean canReceiveManaFromItem(ItemStack stack) {
        return true;  // 允许从物品接收魔力
    }

    @Override
    public boolean canExportManaToPool(BlockEntity pool) {
        return false;  // 不允许导出魔力到魔力池
    }

    @Override
    public boolean canExportManaToItem(ItemStack stack) {
        return false;  // 不允许导出魔力到物品
    }

    @Override
    public boolean isNoExport() {
        return true;  // 标记为不可导出魔力
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
                    player.maxUpStep = player.isCrouching() ? 1.0F : 1.5F; //上坡高度
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
        if (event.getEntity() instanceof Player player) {  // 使用新的模式匹配语法
            ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
            if (!legs.isEmpty() && legs.getItem() == ModArmors.NEBULA_LEGGINGS.get()) {
                // 更新为新的运动属性访问方式
                player.setDeltaMovement(player.getDeltaMovement().add(
                        0, getJump(legs), 0
                ));
                player.fallDistance = -getFallBuffer(legs);  // 更新为新的摔落伤害计算
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