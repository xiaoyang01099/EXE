package org.xiaoyang.ex_enigmaticlegacy.Item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.xiaoyang.ex_enigmaticlegacy.Util.PlayerUtil;
import org.xiaoyang.ex_enigmaticlegacy.Util.SkillCooldownType;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigFile;
import org.xiaoyang.ex_enigmaticlegacy.Config.ExExcaliburConfig;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.BattoSlashEntity;
import org.xiaoyang.ex_enigmaticlegacy.Event.registry.ClientExcaliburChargeRegistry;
import org.xiaoyang.ex_enigmaticlegacy.Event.DimensionSlashKeyInputHandler;
import org.xiaoyang.ex_enigmaticlegacy.Network.inputPacket.BattoSlashCastC2SPacket;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModSounds;
import org.xiaoyang.ex_enigmaticlegacy.Network.NetworkHandler;

import java.util.List;
import java.util.function.Consumer;

// FlySwordPlusItem 表示可释放次元斩的真·飞剑。
public class FlySwordPlusItem extends FlySwordItem {
    public static final int EXCALIBUR_MAX_CHARGE_TICKS = 1200; // 咖喱棒最大蓄力 tick，暂定 1 分钟。
    private static final Component TOOLTIP_DIMENSION_SLASH = Component.translatable("item.akatzumatool.fly_sword.tooltip.3"); // 真·飞剑额外提示。
    private static final Component TOOLTIP_BATTO_SLASH = Component.translatable("item.akatzumatool.fly_sword.tooltip.2"); // 真·飞剑右键拔刀斩提示。
    private static final Component TOOLTIP_EXCALIBUR = Component.translatable("item.akatzumatool.fly_sword.tooltip.5"); // 真·飞剑 C 键咖喱棒提示。

    // 构造真·飞剑物品。
    public FlySwordPlusItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    // 客户端保留飞剑后处理 renderer，并在咖喱棒蓄力同步期间显示拉弓动作。
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final FlySwordHeldItemRenderer renderer = new FlySwordHeldItemRenderer(); // 真·飞剑手持后处理提交渲染器。

            @Override
            public FlySwordHeldItemRenderer getCustomRenderer() {
                return this.renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                if (!(entity instanceof Player player)) return null;
                if (!ClientExcaliburChargeRegistry.isCharging(player)) return null;
                return ClientExcaliburChargeRegistry.getHand(player) == hand ? HumanoidModel.ArmPose.BOW_AND_ARROW : null;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide()) {
            Minecraft minecraft = Minecraft.getInstance();
            int remainingTicks = DimensionSlashKeyInputHandler.getRemainingCooldownTicks(minecraft, SkillCooldownType.BATTO_SLASH);
            if (remainingTicks > 0) {
                DimensionSlashKeyInputHandler.sendCooldownMessage(minecraft, SkillCooldownType.BATTO_SLASH, remainingTicks);
                return InteractionResultHolder.fail(stack);
            }
        }
        player.startUsingItem(usedHand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) return;
        int useTicks = getUseDuration(stack) - remainingUseDuration;
        if (useTicks < getBattoSlashChargeTicks()) return;
        if (!level.isClientSide()) return;

        Minecraft minecraft = Minecraft.getInstance();
        int remainingTicks = DimensionSlashKeyInputHandler.getRemainingCooldownTicks(minecraft, SkillCooldownType.BATTO_SLASH);
        if (remainingTicks > 0) {
            DimensionSlashKeyInputHandler.sendCooldownMessage(minecraft, SkillCooldownType.BATTO_SLASH, remainingTicks);
            player.stopUsingItem();
            return;
        }

        DimensionSlashKeyInputHandler.setCooldown(minecraft, SkillCooldownType.BATTO_SLASH);
        playBattoSlashReleaseSound(minecraft, player);
        NetworkHandler.sendToServer(new BattoSlashCastC2SPacket());
        player.stopUsingItem();
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    public int getBattoSlashChargeTicks() {
        return ConfigFile.flySwordBattoSlashChargeTicks();
    }

    public static int getExcaliburFullChargeTicks() {
        return ExExcaliburConfig.fullChargeTicks();
    }

    // 客户端蓄力完成时立即播放拔刀斩释放音效，不依赖实体生成。
    public void playBattoSlashReleaseSound(Minecraft minecraft, Player player) {
        if (minecraft.level == null || player == null) return;
        minecraft.level.playLocalSound(
                player.getX(), player.getY(), player.getZ(),
                ModSounds.AAAAA.get(),
                SoundSource.PLAYERS,
                1.0F,
                1.05F,
                false
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(TOOLTIP_DIMENSION_SLASH);
        tooltip.add(TOOLTIP_BATTO_SLASH);
        tooltip.add(TOOLTIP_EXCALIBUR);
    }

    // 服务端生成拔刀斩实体。
    public static boolean trySpawnBattoSlash(Player player) {
        if (player == null) return false;
        if (player.level().isClientSide()) return false;
        if (player.isSpectator()) return false;
        if (!isHoldingFlySwordPlus(player)) return false;
        BattoSlashEntity battoSlashEntity = BattoSlashEntity.create(player);
        player.level().addFreshEntity(battoSlashEntity);
        PlayerUtil.deductFood(player, 2);
        return true;
    }
}
