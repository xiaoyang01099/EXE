package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.ModList;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.ManaContainerBlock;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.tile.ManaContainerTile;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.tile.NidavellirForgeTile;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.tile.PolychromeCollapsePrismTile;
import org.xiaoyang.ex_enigmaticlegacy.Tile.RainbowTableTile;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.common.block.block_entity.TerrestrialAgglomerationPlateBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity;
import vazkii.botania.common.block.mana.ManaPoolBlock;

import java.lang.reflect.Method;
import java.util.Objects;

public class ManaReader extends Item {

    public ManaReader(Properties rarity) {
        super(new Properties()
                .stacksTo(1));
    }

    private boolean handleMythicBotanyInfuser(BlockEntity te, Player player) {
        if (!ModList.get().isLoaded("mythicbotany")) {
            return false;
        }

        try {
            Class<?> infuserClass = Class.forName("mythicbotany.infuser.TileManaInfuser");
            if (!infuserClass.isInstance(te)) {
                return false;
            }

            Method getCurrentManaMethod = infuserClass.getMethod("getCurrentMana");
            Method getProgressMethod = infuserClass.getMethod("getProgress");

            int currentMana = (Integer) getCurrentManaMethod.invoke(te);
            double progress = (Double) getProgressMethod.invoke(te);

            StringBuilder message = new StringBuilder();

            if (progress >= 0 && progress <= 1.0) {

                if (progress > 0) {
                    int maxMana = (int) Math.round(currentMana / progress);
                    message.append(String.format("%,d", currentMana))
                            .append("/")
                            .append(String.format("%,d", maxMana));
                    message.append(String.format(" (%.1f%%)", progress * 100));
                } else {
                    message.append(String.format("%,d", currentMana))
                            .append(" mana");
                    message.append(" ").append(Component.translatable("mana_reader.status.starting").getString());
                }

                message.append(" [").append(Component.translatable("mana_reader.device.mana_infuser").getString()).append("]");

                if (progress >= 1.0) {
                    message.append(" [").append(Component.translatable("mana_reader.status.complete").getString()).append("]");
                } else if (progress > 0) {
                    message.append(" [").append(Component.translatable("mana_reader.status.infusing").getString()).append("]");
                } else {
                    message.append(" [").append(Component.translatable("mana_reader.status.waiting_mana").getString()).append("]");
                }
            } else {
                message.append(String.format("%,d", currentMana))
                        .append(" mana");
                message.append(" [").append(Component.translatable("mana_reader.device.mana_infuser").getString()).append("]");

                if (currentMana > 0) {
                    message.append(" [").append(Component.translatable("mana_reader.status.no_recipe_has_mana").getString()).append("]");
                } else {
                    message.append(" [").append(Component.translatable("mana_reader.status.no_recipe").getString()).append("]");
                }
            }

            player.sendSystemMessage(Component.translatable(message.toString())
                );
            return true;

        } catch (Exception e) {

            return false;
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        BlockEntity te = context.getLevel().getBlockEntity(context.getClickedPos());

        if (player == null) return InteractionResult.FAIL;

        if (te instanceof ManaPoolBlockEntity pool) {
            if (!context.getLevel().isClientSide) {
                int mana = pool.getCurrentMana();

                int maxMana = pool.getMaxMana();
                if (maxMana == -1) {
                    if (pool.getBlockState().getBlock() instanceof ManaPoolBlock block) {
                        maxMana = block.variant == ManaPoolBlock.Variant.DILUTED ?
                                10000 : 1000000;
                    } else {
                        maxMana = 1000000;
                    }
                }

                StringBuilder message = new StringBuilder();
                message.append(String.format("%,d", mana)).append("/").append(String.format("%,d", maxMana));

                if (pool.getBlockState().getBlock() instanceof ManaPoolBlock block) {
                    String variant = Component.translatable("mana_pool.variant." + block.variant.name().toLowerCase()).getString();
                    message.append(" (").append(variant).append(")");
                }

                if (pool.isOutputtingPower()) {
                    message.append(" [").append(Component.translatable("mana_reader.status.outputting").getString()).append("]");
                } else {
                    message.append(" [").append(Component.translatable("mana_reader.status.receiving").getString()).append("]");
                }

                if (maxMana > 0) {
                    double percentage = (double) mana / maxMana * 100;
                    message.append(String.format(" %.1f%%", percentage));
                }

                player.sendSystemMessage(Component.translatable(message.toString()));
            }
        } else if (te instanceof ManaContainerTile container) {
            if (!context.getLevel().isClientSide) {
                int mana = container.getCurrentMana();

                int maxMana = container.manaCap;
                if (maxMana == -1) {
                    if (container.getBlockState().getBlock() instanceof ManaContainerBlock block) {
                        maxMana = block.variant == ManaContainerBlock.Variant.DEFAULT ?
                                ManaContainerTile.MAX_MANA : ManaContainerTile.MAX_MANA_DILLUTED;
                    } else {
                        maxMana = ManaContainerTile.MAX_MANA;
                    }
                }

                StringBuilder message = new StringBuilder();
                message.append(String.format("%,d", mana)).append("/").append(String.format("%,d", maxMana));

                if (container.getBlockState().getBlock() instanceof ManaContainerBlock block) {
                    String variant = Component.translatable("mana_reader.variant." + block.variant.name().toLowerCase()).getString();
                    message.append(" (").append(variant).append(")");
                }

                if (container.isOutputtingPower()) {
                    message.append(" [").append(Component.translatable("mana_reader.status.outputting").getString()).append("]");
                } else {
                    message.append(" [").append(Component.translatable("mana_reader.status.receiving").getString()).append("]");
                }

                if (maxMana > 0) {
                    double percentage = (double) mana / maxMana * 100;
                    message.append(String.format(" %.1f%%", percentage));
                }

                player.sendSystemMessage(Component.translatable(message.toString())
                );
            }
        } else if (te instanceof GeneratingFlowerBlockEntity gen) {
            int mana = gen.getMana();
            if (!context.getLevel().isClientSide) {
                player.sendSystemMessage(Component.translatable("mana_reader.generating_flower", mana, gen.getMaxMana())
                );
            }
        } else if (te instanceof FunctionalFlowerBlockEntity func) {
            int mana = func.getMana();
            if (!context.getLevel().isClientSide) {
                player.sendSystemMessage(Component.translatable("mana_reader.functional_flower", mana, func.getMaxMana())
                );
            }
        } else if (te instanceof ManaSpreaderBlockEntity spreader) {
            int mana = spreader.getCurrentMana();
            if (!context.getLevel().isClientSide) {
                player.sendSystemMessage(Component.translatable("mana_reader.spreader", mana, spreader.getMaxMana())
                );
            }
        } else if (te instanceof TerrestrialAgglomerationPlateBlockEntity terra) {
            int mana = terra.getCurrentMana();
            if (!context.getLevel().isClientSide) {
                if (terra.canReceiveManaFromBursts()) {
                    float completion = terra.getCompletion();
                    int maxMana = (int) (mana / Math.max(completion, 0.00001f));
                    player.sendSystemMessage(Component.translatable("mana_reader.terra_plate.active", mana, maxMana, (int) (completion * 100))
                    );
                } else {
                    player.sendSystemMessage(Component.translatable("mana_reader.terra_plate.no_recipe")
                    );
                }
            }
        } else if (te instanceof RainbowTableTile rainbowTable) {
            if (!context.getLevel().isClientSide) {
                int mana = rainbowTable.getCurrentMana();
                int maxMana = RainbowTableTile.MAX_MANA;

                StringBuilder message = new StringBuilder();
                message.append(String.format("%,d", mana)).append("/").append(String.format("%,d", maxMana));

                message.append(" [").append(Component.translatable("mana_reader.status.receiver").getString()).append("]");

                if (maxMana > 0) {
                    double percentage = (double) mana / maxMana * 100;
                    message.append(String.format(" %.1f%%", percentage));
                }

                boolean canOperate = mana >= 10000;
                if (!canOperate) {
                    message.append(" [").append(Component.translatable("mana_reader.status.insufficient_mana").getString()).append("]");
                } else {
                    message.append(" [").append(Component.translatable("mana_reader.status.operational").getString()).append("]");
                }

                player.sendSystemMessage(Component.translatable(message.toString())
                );
            }
        } else if (te instanceof NidavellirForgeTile forge) {
            if (!context.getLevel().isClientSide) {
                int currentMana = forge.getCurrentMana();
                int requiredMana = forge.manaToGet;

                StringBuilder message = new StringBuilder();

                if (requiredMana > 0) {
                    message.append(String.format("%,d", currentMana)).append("/").append(String.format("%,d", requiredMana));

                    double percentage = (double) currentMana / requiredMana * 100;
                    message.append(String.format(" (%.1f%%)", Math.min(percentage, 100.0)));

                    message.append(" [").append(Component.translatable("mana_reader.device.nidavellir_forge").getString()).append("]");

                    if (currentMana >= requiredMana) {
                        message.append(" [").append(Component.translatable("mana_reader.status.ready_to_craft").getString()).append("]");
                    } else {
                        message.append(" [").append(Component.translatable("mana_reader.status.charging").getString()).append("]");
                    }

                    if (forge.getAttachedSpark() != null) {
                        message.append(" [").append(Component.translatable("mana_reader.status.spark_connected").getString()).append("]");
                    } else {
                        message.append(" [").append(Component.translatable("mana_reader.status.no_spark").getString()).append("]");
                    }
                } else {
                    message.append(String.format("%,d", currentMana)).append(" mana");
                    message.append(" [").append(Component.translatable("mana_reader.device.nidavellir_forge").getString()).append("]");

                    if (forge.getAttachedSpark() != null) {
                        message.append(" [").append(Component.translatable("mana_reader.status.spark_connected").getString()).append("]");
                    } else {
                        message.append(" [").append(Component.translatable("mana_reader.status.no_spark").getString()).append("]");
                    }

                    boolean hasInputItems = false;
                    for (int i = NidavellirForgeTile.FIRST_INPUT_SLOT; i < forge.getInventory().getSlots(); i++) {
                        if (!forge.getInventory().getStackInSlot(i).isEmpty()) {
                            hasInputItems = true;
                            break;
                        }
                    }

                    if (hasInputItems) {
                        message.append(" [").append(Component.translatable("mana_reader.status.no_recipe_has_items").getString()).append("]");
                    } else {
                        message.append(" [").append(Component.translatable("mana_reader.status.no_recipe").getString()).append("]");
                    }
                }

                player.sendSystemMessage(Component.translatable(message.toString())
                );
            }
        }else if (te instanceof PolychromeCollapsePrismTile prism) {
            if (!context.getLevel().isClientSide) {
                int currentMana = prism.getCurrentMana();

                StringBuilder message = new StringBuilder();

                if (prism.getCurrentRecipe(prism.getInventory()) != null) {
                    int requiredMana = Objects.requireNonNull(prism.getCurrentRecipe(prism.getInventory())).getManaUsage();
                    message.append(String.format("%,d", currentMana)).append("/").append(String.format("%,d", requiredMana));

                    float completion = prism.getCompletion();
                    message.append(String.format(" (%.1f%%)", completion * 100));

                    message.append(" [").append(Component.translatable("mana_reader.device.polychrome_prism").getString()).append("]");

                    if (prism.hasValidPlatform()) {
                        message.append(" [").append(Component.translatable("mana_reader.status.platform_valid").getString()).append("]");
                    } else {
                        message.append(" [").append(Component.translatable("mana_reader.status.platform_invalid").getString()).append("]");
                    }

                    if (currentMana >= requiredMana) {
                        message.append(" [").append(Component.translatable("mana_reader.status.ready_to_craft").getString()).append("]");
                    } else {
                        message.append(" [").append(Component.translatable("mana_reader.status.charging").getString()).append("]");
                    }

                } else {
                    message.append(String.format("%,d", currentMana)).append(" mana");
                    message.append(" [").append(Component.translatable("mana_reader.device.polychrome_prism").getString()).append("]");
                    if (prism.hasValidPlatform()) {
                        message.append(" [").append(Component.translatable("mana_reader.status.platform_valid").getString()).append("]");
                    } else {
                        message.append(" [").append(Component.translatable("mana_reader.status.platform_invalid").getString()).append("]");
                    }
                    message.append(" [").append(Component.translatable("mana_reader.status.no_recipe").getString()).append("]");
                }
                player.sendSystemMessage(Component.translatable(message.toString())
                );
            }
        }else {
            if (!context.getLevel().isClientSide) {
                handleMythicBotanyInfuser(te, player);
            }
        }
        return InteractionResult.PASS;
    }
}