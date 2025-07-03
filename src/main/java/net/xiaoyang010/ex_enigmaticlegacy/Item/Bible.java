package net.xiaoyang010.ex_enigmaticlegacy.Item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import vazkii.patchouli.api.PatchouliAPI;
import vazkii.patchouli.common.base.PatchouliSounds;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.book.BookRegistry;
import vazkii.patchouli.common.item.ItemModBook;

import javax.annotation.Nullable;
import java.util.List;

public class Bible extends ItemModBook {
    private static final ResourceLocation BIBLE_ID = new ResourceLocation("ex_enigmaticlegacy", "bible");

    public Bible() {
        super();
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (tab == ModTabs.TAB_EXENIGMATICLEGACY_ITEM) {
            items.add(new ItemStack(this));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        Book book = getBibleBook();
        if (book != null) {
            return new TranslatableComponent(book.name);
        }
        return new TranslatableComponent("item.ex_enigmaticlegacy.bible");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        Book book = getBibleBook();

        if (book != null && !book.getContents().isErrored()) {
            // 添加副标题
            tooltip.add(book.getSubtitle().withStyle(ChatFormatting.GRAY));

            // 显示进度
            float completion = getBibleCompletion(stack);
            if (completion > 0) {
                int percentage = (int) (completion * 100);
                tooltip.add(new TranslatableComponent("ex_enigmaticlegacy.gui.lexicon.completion", percentage)
                        .withStyle(ChatFormatting.GREEN));
            }

            tooltip.add(new TranslatableComponent("ex_enigmaticlegacy.bible.tooltip.usage")
                    .withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(new TranslatableComponent("ex_enigmaticlegacy.bible.tooltip.error")
                    .withStyle(ChatFormatting.DARK_RED));
        }

        if (flagIn.isAdvanced()) {
            tooltip.add(new TextComponent("Book ID: " + BIBLE_ID).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(new TextComponent("Loaded: " + (book != null)).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);

        if (worldIn.isClientSide) {
            // 客户端直接打开GUI
            openBibleGUI();
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }

        Book book = getBibleBook();
        if (book == null) {
            playerIn.sendMessage(
                    new TranslatableComponent("ex_enigmaticlegacy.bible.message.not_found")
                            .withStyle(ChatFormatting.RED),
                    playerIn.getUUID()
            );
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    private void openBibleGUI() {
        Book book = getBibleBook();
        if (book != null) {
            PatchouliAPI.get().openBookGUI(BIBLE_ID);

            SoundEvent sfx = PatchouliSounds.getSound(book.openSound, PatchouliSounds.BOOK_OPEN);
        }
    }

    public float getBibleCompletion(ItemStack stack) {
        Book book = getBibleBook();
        if (book == null) {
            return 0F;
        }

        int totalEntries = 0;
        int unlockedEntries = 0;

        for (var entry : book.getContents().entries.values()) {
            if (!entry.isSecret()) {
                totalEntries++;
                if (!entry.isLocked()) {
                    unlockedEntries++;
                }
            }
        }

        return totalEntries > 0 ? ((float) unlockedEntries) / (float) totalEntries : 0F;
    }

    private Book getBibleBook() {
        return BookRegistry.INSTANCE.books.get(BIBLE_ID);
    }

    public boolean isBibleLoaded() {
        return getBibleBook() != null;
    }
}