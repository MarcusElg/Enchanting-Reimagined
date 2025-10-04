package enchantingreimagined;

import com.mojang.serialization.MapCodec;

import enchantingreimagined.gui.EnchantingWorkstationGui;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EnchantingWorkstationBlock extends Block {

    public static final MapCodec<EnchantingWorkstationBlock> CODEC = createCodec(EnchantingWorkstationBlock::new);
    private static final Text SCREEN_TITLE = Text
            .translatable("container.enchanting_reimagined.enchanting_workstation");

    @Override
    public MapCodec<EnchantingWorkstationBlock> getCodec() {
        return CODEC;
    }

    public EnchantingWorkstationBlock(Block.Settings settings) {
        super(settings);
    }

    @Override
    protected NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory(
                (syncId, inventory, player) -> new EnchantingWorkstationGui(syncId, inventory,
                        ScreenHandlerContext.create(world, pos)),
                SCREEN_TITLE);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        } else {
            player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
            player.incrementStat(EnchantingReimagined.INTERACT_WITH_ENCHANTING_WORKSTATION);
            return ActionResult.CONSUME;
        }
    }
}
