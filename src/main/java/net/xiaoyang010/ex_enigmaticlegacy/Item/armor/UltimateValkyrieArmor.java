//package net.xiaoyang010.ex_enigmaticlegacy.Item.armor;
//
//import net.minecraft.network.chat.Component;
//import net.minecraft.sounds.SoundEvent;
//import net.minecraft.sounds.SoundEvents;
//import net.minecraft.world.effect.MobEffectInstance;
//import net.minecraft.world.effect.MobEffects;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.EquipmentSlot;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.*;
//import net.minecraft.world.item.crafting.Ingredient;
//import net.minecraft.world.level.Level;
//import net.minecraftforge.client.event.RenderPlayerEvent;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.event.entity.living.LivingDeathEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.xiaoyang010.ex_enigmaticlegacy.Client.renderer.layer.WitherArmorLayer;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.List;
//import java.util.UUID;
//
//public abstract class UltimateValkyrieArmor extends ArmorItem {
//    //贴图建议深红色
//    public UltimateValkyrieArmor(EquipmentSlot pSlot, Properties pProperties) {
//        super(new ValkyrieMaterial(), pSlot, pProperties);
//    }
//
//    @Override
//    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
//        pTooltipComponents.add(Component.nullToEmpty("§7》§e全套-能量护甲:"));
//        pTooltipComponents.add(Component.nullToEmpty("§7可以抵御25次致命伤害，冷却20秒。"));
//        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
//    }
//
//    //武神材质
//    public static class ValkyrieMaterial implements ArmorMaterial{
//
//        @Override
//        public int getDurabilityForSlot(EquipmentSlot equipmentSlot) {
//            return 1024;
//        }
//
//        //防御力
//        @Override
//        public int getDefenseForSlot(EquipmentSlot equipmentSlot) {
//            return 1024;
//        }
//
//        //附魔能力，越高附魔的品质越好
//        @Override
//        public int getEnchantmentValue() {
//            return 1024;
//        }
//
//        //声音
//        @Override
//        public SoundEvent getEquipSound() {
//            return SoundEvents.ARMOR_EQUIP_NETHERITE;
//        }
//
//        //修复材料，因为没写武神锭所以先用下界合金代替
//        @Override
//        public Ingredient getRepairIngredient() {
//            return Ingredient.of(Items.NETHERITE_INGOT);
//        }
//
//        @Override
//        public String getName() {
//            return "ex_enigmaticlegacy:ultimatevalkyrie";
//
//        }
//
//        //盔甲韧性
//        @Override
//        public float getToughness() {
//            return 0;
//        }
//
//        //击退抗性
//        @Override
//        public float getKnockbackResistance() {
//            return 1024;
//        }
//    }
//    //武神战盔
//    public static class UltimateValkyrieHelmet extends UltimateValkyrieArmor{
//
//        UUID uuid = UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150");
//
//        public UltimateValkyrieHelmet(Properties pProperties) {
//            super(EquipmentSlot.HEAD, pProperties.fireResistant().stacksTo(1));
//        }
//
//        @Override
//        public void onArmorTick(ItemStack stack, Level level, Player player) {
//            player.addEffect(new MobEffectInstance(MobEffects.HEAL,2,5));
//            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,2,2));
//        }
//
//        @Override
//        public Component getName(ItemStack pStack) {
//            return Component.nullToEmpty("§c武神战盔");
//        }
//        @Override
//        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
//            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
//            pTooltipComponents.add(Component.nullToEmpty("§7》§c武神意志："));
//            pTooltipComponents.add(Component.nullToEmpty("§7穿戴者将获得永久生命恢复IV，以及极高的防御。"));
//        }
//
//        /*@Override
//        public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pEquipmentSlot) {
//            Multimap<Attribute, AttributeModifier> multimap = super.getDefaultAttributeModifiers(pEquipmentSlot);
//            multimap.put(Attributes.MAX_HEALTH, new AttributeModifier(uuid, "Armor modifier", (double)20, AttributeModifier.Operation.ADDITION));
//            return multimap;
//        }*/
//    }
//    //武神重甲
//    public static class UltimateValkyrieChestplate extends UltimateValkyrieArmor{
//
//        UUID uuid =UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E");
//        public UltimateValkyrieChestplate(Properties pProperties) {
//            super(EquipmentSlot.CHEST, pProperties.fireResistant().stacksTo(1));
//            MinecraftForge.EVENT_BUS.register(this);
//
//        }
//
//        @Override
//        public void onArmorTick(ItemStack stack, Level level, Player player) {
//            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,2,3));
//            super.onArmorTick(stack, level, player);
//        }
//
//        /*@Override
//        public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pEquipmentSlot) {
//            Multimap<Attribute, AttributeModifier> multimap = super.getDefaultAttributeModifiers(pEquipmentSlot);
//            multimap.put(Attributes.MAX_HEALTH, new AttributeModifier(uuid, "Armor modifier", (double)80, AttributeModifier.Operation.ADDITION));
//            return multimap;
//        }*/
//        @Override
//        public Component getName(ItemStack pStack) {
//            return Component.nullToEmpty("§c武神重甲");
//        }
//
//        @Override
//        public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
//            super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
//            pTooltipComponents.add(Component.nullToEmpty("§7》§6屹立不倒："));
//            pTooltipComponents.add(Component.nullToEmpty("§7将大多数伤害最小化，且血量越少防御越高。"));
//            pTooltipComponents.add(Component.nullToEmpty("§7》§c越战越勇："));
//            pTooltipComponents.add(Component.nullToEmpty("§7每击杀一个敌人都会提高伤害。"));
//            }
//
//        @SubscribeEvent
//        public void killEmeny(LivingDeathEvent event){
//            if (event != null) {
//                Entity entity = event.getSource().getEntity();
//                if (entity != null) {
//                    if (entity instanceof Player player && player.getInventory().contains(getDefaultInstance())) {
//                        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 120, (int) ((entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(MobEffects.DAMAGE_BOOST) ? _livEnt.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() : 0) + 1)));
//                    }
//                }
//            }
//        }
//        @SubscribeEvent
//        public void DeathDefense(LivingDeathEvent event){
//            //event.getEntity()
//        }
//        public static boolean isAdd = false;
//
//        @SubscribeEvent
//        public void WitherArmorLayerRenderer(RenderPlayerEvent event){
//            if (event.getEntity() instanceof Player player && player.getInventory().contains(getDefaultInstance())) {
//                if (!isAdd) {
//                    event.getRenderer().addLayer(new WitherArmorLayer<>(event.getRenderer(), event.getRenderer().getModel()));
//                    isAdd=true;
//                }
//            }
//        }
//    }
//    public static boolean isFullSuit(Player player){
//        return //player.getInventory().armor.get(0).getItem() instanceof UltimateValkyrieArmor
//                //&& player.getInventory().armor.get(1).getItem() instanceof UltimateValkyrieArmor
//                 player.getInventory().armor.get(2).getItem() instanceof UltimateValkyrieArmor
//                && player.getInventory().armor.get(3).getItem() instanceof UltimateValkyrieArmor;
//    }
//}
