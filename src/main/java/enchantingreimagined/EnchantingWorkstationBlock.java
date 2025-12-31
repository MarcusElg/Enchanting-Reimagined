package enchantingreimagined;

import com.mojang.serialization.MapCodec;

import enchantingreimagined.gui.EnchantingWorkstationGui;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class EnchantingWorkstationBlock extends Block {

    public static final MapCodec<EnchantingWorkstationBlock> CODEC = simpleCodec(EnchantingWorkstationBlock::new);
    private static final Component SCREEN_TITLE = Component
            .translatable("container.enchanting_reimagined.enchanting_workstation");

    @Override
    public MapCodec<EnchantingWorkstationBlock> codec() {
        return CODEC;
    }

    public EnchantingWorkstationBlock(Block.Properties settings) {
        super(settings);
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        return new SimpleMenuProvider(
                (syncId, inventory, player) -> new EnchantingWorkstationGui(syncId, inventory,
                        ContainerLevelAccess.create(world, pos)),
                SCREEN_TITLE);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else {
            player.openMenu(state.getMenuProvider(world, pos));
            player.awardStat(EnchantingReimagined.INTERACT_WITH_ENCHANTING_WORKSTATION);
            return InteractionResult.CONSUME;
        }
    }
}
