package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;

public class ModTabs {
	public static CreativeModeTab TAB_EXENIGMATICLEGACY_ITEM;
	public static CreativeModeTab TAB_EXENIGMATICLEGACY_WEAPON_ARMOR;
	public static CreativeModeTab TAB_EXENIGMATICLEGACY_MINERAL;
	public static CreativeModeTab TAB_EXENIGMATICLEGACY_BLOCK;
	public static CreativeModeTab TAB_EXENIGMATICLEGACY_FOOD;
	public static CreativeModeTab TAB_EXENIGMATICLEGACY_BOTANIA;


	public static void load() {
		TAB_EXENIGMATICLEGACY_ITEM = new CreativeModeTab("ex_enigmaticlegacy") {
			@Override
			public ItemStack makeIcon() {
				return new ItemStack(ModItems.MEMORIZE.get());
			}

			@OnlyIn(Dist.CLIENT)
			public boolean hasSearchBar() {
				return false;
			}
		}/*.setBackgroundSuffix("item_search.png");*/;

		TAB_EXENIGMATICLEGACY_WEAPON_ARMOR = new CreativeModeTab("ex_enigmaticlegacy_weapon") {
			@Override
			public ItemStack makeIcon() {
				return new ItemStack(ModWeapons.SONG_OF_THE_ABYSS.get());
			}

			@OnlyIn(Dist.CLIENT)
			public boolean hasSearchBar() {
				return false;
			}
		};

		TAB_EXENIGMATICLEGACY_MINERAL = new CreativeModeTab("ex_enigmaticlegacy_mineral") {
			@Override
			public ItemStack makeIcon() {
				return new ItemStack(ModItems.FROST_ENCHANTRESS.get());
			}

			@OnlyIn(Dist.CLIENT)
			public boolean hasSearchBar() {
				return false;
			}
		};

		TAB_EXENIGMATICLEGACY_BLOCK = new CreativeModeTab("ex_enigmaticlegacy_block") {
			@Override
			public ItemStack makeIcon() {
				return new ItemStack(ModBlockss.EVILBLOCK.get());
			}

			@OnlyIn(Dist.CLIENT)
			public boolean hasSearchBar() {
				return false;
			}
		};

		TAB_EXENIGMATICLEGACY_FOOD = new CreativeModeTab("ex_enigmaticlegacy_food") {
			@Override
			public ItemStack makeIcon() {
				return new ItemStack(ModFoods.ENDLESS_CAKE.get());
			}

			@OnlyIn(Dist.CLIENT)
			public boolean hasSearchBar() {
				return false;
			}
		};

		TAB_EXENIGMATICLEGACY_BOTANIA = new CreativeModeTab("ex_enigmaticlegacy_botania") {
			@Override
			public ItemStack makeIcon() {
				return new ItemStack(ModBlockss.INFINITY_POTATO.get());
			}

			@OnlyIn(Dist.CLIENT)
			public boolean hasSearchBar() {
				return false;
			}
		}.setBackgroundImage(new ResourceLocation("ex_enigmaticlegacy", "textures/gui/container/creative_inventory/tab_botania.png"));
	}
}
