package elec332.eflux2.block;

import com.google.common.base.Preconditions;
import elec332.core.client.model.loading.INoBlockStateJsonBlock;
import elec332.core.util.BlockProperties;
import elec332.core.world.WorldHelper;
import elec332.eflux2.tile.IActivatableTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by Elec332 on 8-2-2019
 */
public class BlockTileEntity extends AbstractBlock implements INoBlockStateJsonBlock.RotationImpl {

    public BlockTileEntity(Properties builder, Supplier<TileEntity> tileCreator) {
        super(builder);
        this.tileCreator = ibr -> tileCreator.get();
    }

    public BlockTileEntity(Properties builder, Function<IBlockReader, TileEntity> tileCreator) {
        super(builder);
        this.tileCreator = tileCreator;
    }

    private final Function<IBlockReader, TileEntity> tileCreator;

    @Override
    public boolean hasTileEntity(BlockState state) {
        return tileCreator != null;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        if (!hasTileEntity(state)) {
            return null;
        }
        return tileCreator.apply(world);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return Preconditions.checkNotNull(super.getStateForPlacement(context)).with(BlockProperties.FACING_NORMAL, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    public ActionResultType onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        TileEntity tile = WorldHelper.getTileAt(world, pos);
        return tile instanceof IActivatableTile ? ((IActivatableTile) tile).onBlockActivated(player, hand, hit) : ActionResultType.PASS;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(BlockProperties.FACING_NORMAL);
    }

}
