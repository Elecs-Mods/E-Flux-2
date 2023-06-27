package elec332.eflux2.block;

import com.google.common.base.Preconditions;
import elec332.core.client.model.loading.INoBlockStateJsonBlock;
import elec332.core.util.BlockProperties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by Elec332 on 8-2-2019
 */
public class BlockTileEntity extends AbstractBlock {

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
    public BlockState getStateForPlacement(@Nonnull BlockItemUseContext context) {
        return Preconditions.checkNotNull(super.getStateForPlacement(context)).with(BlockProperties.FACING_HORIZONTAL, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    protected void fillStateContainer(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(BlockProperties.FACING_HORIZONTAL);
    }

}
