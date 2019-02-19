package elec332.test.block;

import elec332.core.client.model.loading.INoBlockStateJsonBlock;
import elec332.core.util.BlockProperties;
import elec332.core.world.WorldHelper;
import elec332.test.tile.IActivatableTile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by Elec332 on 8-2-2019
 */
public class BlockTile extends AbstractBlock implements INoBlockStateJsonBlock.RotationImpl {

    public BlockTile(Properties builder, Supplier<TileEntity> tileCreator) {
        super(builder);
        this.tileCreator = ibr -> tileCreator.get();
    }

    public BlockTile(Properties builder, Function<IBlockReader, TileEntity> tileCreator) {
        super(builder);
        this.tileCreator = tileCreator;
    }

    private final Function<IBlockReader, TileEntity> tileCreator;

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return tileCreator != null;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(IBlockState state, IBlockReader world) {
        if (!hasTileEntity(state)) {
            return null;
        }
        return tileCreator.apply(world);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, RayTraceResult hit) {
        TileEntity tile = WorldHelper.getTileAt(world, pos);
        return tile instanceof IActivatableTile && ((IActivatableTile) tile).onBlockActivated(player, hand, hit);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(BlockProperties.FACING_NORMAL);
    }

    @Override
    public IBlockState getRenderState(ItemStack s) {
        return getDefaultState();
    }

}
