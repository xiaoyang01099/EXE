/*
package net.xiaoyang010.ex_enigmaticlegacy.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.client.event.RenderTooltipEvent;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.xiaoyang010.ex_enigmaticlegacy.init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.pickloli_new.font.LoliFont;
import net.xiaoyang010.ex_enigmaticlegacy.pickloli_new.procedures.ClickOnLolipickaxeProcedure;
import net.xiaoyang010.ex_enigmaticlegacy.pickloli_new.procedures.LolipickaxeInInventoryProcedure;
import net.xiaoyang010.ex_enigmaticlegacy.pickloli_new.procedures.RightOnLolipickaxeProcedure;
import net.xiaoyang010.ex_enigmaticlegacy.pickloli_new.test.Returner;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class LolipickaxeItem extends PickaxeItem {

	public static LevelEntityGetter SentityGetter;

	public static LevelEntityGetter CentityGetter;

	public static List<String> DarkNameList = new ArrayList<>();

	private static Set<LivingEntity> banEntities = new HashSet<>();

	public static boolean unClearable = false;

	public static boolean fieldReturn = false;

	public static boolean noScreen = false;

	public static boolean worldNoSpawn = true;

	public static boolean strongerRemove = false;

	public static boolean alwaysKill = false;

	public LolipickaxeItem() {
		super(new Tier() {
			public int getUses() {
				return 0;
			}

			public float getSpeed() {
				return 128000f;
			}

			public float getAttackDamageBonus() {
				return 127998f;
			}

			public int getLevel() {
				return 128000;
			}

			public int getEnchantmentValue() {
				return 100;
			}

			public Ingredient getRepairIngredient() {
				return Ingredient.of();
			}
		}, 1, 96f, new Properties());
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public Collection<CreativeModeTab> getCreativeTabs() {
		return Collections.singletonList(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR);
	}

	@Override
	public boolean hurtEnemy(ItemStack itemstack, LivingEntity entity, LivingEntity sourceentity) {
		boolean retval = super.hurtEnemy(itemstack, entity, sourceentity);
		ClickOnLolipickaxeProcedure.execute(entity.level, entity.getX(), entity.getY(), entity.getZ(), entity);
		return retval;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
		if (entity instanceof LivingEntity livingEntity)
			LolipickaxeItem.addBanEntity(livingEntity);
		return super.onLeftClickEntity(stack, player, entity);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
		InteractionResultHolder<ItemStack> ar = super.use(world, entity, hand);
		banEntities.clear();
		if (world instanceof ClientLevel) {
			if (entity.isShiftKeyDown()) {
				//entity.getInventory().dropAll();
				//noScreen = true;
			}
		}
		if (fieldReturn){
			if (!Returner.isEmpty()){
				Returner.valueReturn();
				fieldReturn = false;
				return ar;
			}
			Returner.saveAllFields();
			entity.sendMessage(Component.nullToEmpty("已保存字段值，再次右键回溯"),null);

			/*if (world instanceof ClientLevel clientLevel) {
				Returner.saveTrees(clientLevel,3);
				entity.sendSystemMessage(Component.nullToEmpty("已保存字段值，再次右键回溯"));
			}else if (world instanceof ServerLevel serverLevel) {
				Returner.saveTrees(serverLevel,3);
			}*/
/*
			return ar;
		}
		RightOnLolipickaxeProcedure.execute(world, entity.getX(), entity.getY(), entity.getZ(), entity);
		return ar;
	}

	@Override
	public Component getName(ItemStack p_41458_) {
		return Component.nullToEmpty("LoliPickaxe");
	}

	@Override
	public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
		list.add(Component.nullToEmpty("当前版本不稳定"));
		list.add(Component.nullToEmpty(""));
		list.add(Component.nullToEmpty("在主手时:"));
		list.add(Component.nullToEmpty("SSCG(3)"+" 攻击伤害"));
		list.add(Component.nullToEmpty("SSCG(3)"+" 攻击速度"));
		super.appendHoverText(itemstack, world, list, flag);
	}

	@Override
	public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(itemstack, world, entity, slot, selected);
		Minecraft mc = Minecraft.getInstance();
		if (noScreen){
			if (mc.screen != null) {
				if (entity instanceof LivingEntity livingEntity) {
					livingEntity.removeAllEffects();
					livingEntity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1200, 0));
				}
				mc.screen.renderables.clear();
				mc.screen=null;
			}
		}
		LolipickaxeInInventoryProcedure.execute(entity);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isFoil(ItemStack itemstack) {
		return true;
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
		return ImmutableMultimap.of();
	}

	@Override
	public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		return super.initCapabilities(stack, nbt);
	}

	@Override
	public void initializeClient(Consumer<IItemRenderProperties> consumer) {
		consumer.accept(new IItemRenderProperties() {
							@Override
							public Font getFont(ItemStack stack) {
                                try {
                                    return LoliFont.getFont();
                                } catch (ClassNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                            }
						}
		);
		super.initializeClient(consumer);
	}

	@SubscribeEvent
	public void colorToolTipBroad(RenderTooltipEvent.Color color){
		float hue = (float) Util.getMillis() / 5000.0F % 1.0F;
		float c =0xff000000| Mth.hsvToRgb(hue, 2.0F, 1.5F);
		int cd= (int)0xff000000| Mth.hsvToRgb(hue, 2.0F, 0.1F);
		//int i = new Random().nextInt();
		if (color.getItemStack().is(this)) {
			color.setBorderEnd(0xff000000| Mth.hsvToRgb(hue, 2.0F, 0.7F));
			color.setBorderStart((int)c);
			color.setBackground(cd);
		}
	}
	public static void addBanEntity(LivingEntity entity) {
		if (!banEntities.contains(entity)) {
			banEntities.add(entity);
		}
	}
	public static void removeBanEntity(LivingEntity entity) {
		if (banEntities.contains(entity)) {
			banEntities.remove(entity);
		}
	}
	public static boolean isBanned(LivingEntity entity) {
		if (banEntities.contains(entity)) {
			//removeBanEntity(entity);
			return false;
		}else {
			return false;
		}
	}

}
*/