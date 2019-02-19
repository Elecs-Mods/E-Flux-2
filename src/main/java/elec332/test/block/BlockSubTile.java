package elec332.test.block;

import elec332.core.block.AbstractBlock;
import elec332.core.block.ISelectionBoxOverride;
import elec332.test.tile.ISubTileLogic;
import elec332.test.tile.TileMultiObject;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Elec332 on 20-2-2018
 */
public class BlockSubTile extends AbstractBlock implements ISelectionBoxOverride {

    @SuppressWarnings("all")
    public BlockSubTile(Properties builder, Class<? extends ISubTileLogic>... subtiles) {
        super(builder);
        this.subtiles = subtiles;
    }

    private final Class<? extends ISubTileLogic>[] subtiles;

    @Nullable
    @Override
    public TileEntity createTileEntity(IBlockState state, IBlockReader world) {
        return new TileMultiObject(subtiles);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nonnull
    @Override
    public VoxelShape getShape(@Nonnull IBlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        TileMultiObject tile = getTileEntity(world, pos, TileMultiObject.class);
        if (tile == null) { //tile is null when checking placement
            return VoxelShapes.fullCube();
        }
        return tile.getShape(state, pos);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public VoxelShape getSelectionBox(IBlockState state, IWorld world, BlockPos pos, EntityPlayer player, RayTraceResult hit) {
        TileMultiObject tile = getTileEntity(world, pos, TileMultiObject.class);
        return tile.getSelectionBox(player, hit);
    }

    @Override
    @SuppressWarnings("deprecation") //Don't obscure player vision when in block
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public BlockFaceShape getBlockFaceShape(IBlockReader worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public EnumPushReaction getPushReaction(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @Nullable
    @Override
    public RayTraceResult getRayTraceResult(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end, RayTraceResult original) {
        TileMultiObject tile = getTileEntity(world, pos, TileMultiObject.class);
        if (original == null || tile == null) { //tile is null when checking placement
            return original;
        }
        return tile.getRayTraceResult(start, end, original, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReplaced(@Nonnull IBlockState me, @Nonnull World world, @Nonnull BlockPos pos, IBlockState newState, boolean isMoving) {
        TileMultiObject tile = getTileEntity(world, pos, TileMultiObject.class);
        tile.onRemoved();
        super.onReplaced(me, world, pos, newState, isMoving);
    }

    @Override
    public boolean removedByPlayer(@Nonnull IBlockState state, World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest, IFluidState fluid) {
        TileMultiObject tile = getTileEntity(world, pos, TileMultiObject.class);
        return tile.removedByPlayer(player, willHarvest, fluid, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos p_189540_5_) {
        neighborChanged(world, pos, p_189540_5_, false, neighbor);
    }

    @Override
    public void observedNeighborChange(IBlockState observerState, World world, BlockPos observerPos, Block changedBlock, BlockPos changedBlockPos) {
        neighborChanged(world, observerPos, changedBlockPos, true, changedBlock);
    }

    private void neighborChanged(World world, BlockPos pos, BlockPos neighborPos, boolean observer, Block changedBlock) {
        if (!world.isRemote) {
            TileMultiObject tile = getTileEntity(world, pos, TileMultiObject.class);
            if (!tile.shouldRefresh(world.getGameTime(), neighborPos) && observer) {
                return;
            }
            tile.neighborChanged(neighborPos, observer, world.getFluidState(pos), changedBlock);
        }
    }

    @Override
    public void getDrops(IBlockState state, NonNullList<ItemStack> drops, World world, BlockPos pos, int fortune) {
    }

    @Override
    public boolean onBlockActivated(IBlockState state, World world, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileMultiObject tile = getTileEntity(world, pos, TileMultiObject.class);
        return tile.onBlockActivated(player, hand, state, facing, hitX, hitY, hitZ);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, EntityPlayer player) {
        TileMultiObject tile = getTileEntity(world, pos, TileMultiObject.class);
        return tile.getStack(target, player);
    }

}
