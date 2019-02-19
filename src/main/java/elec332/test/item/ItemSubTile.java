package elec332.test.item;

import com.google.common.base.Preconditions;
import elec332.core.item.AbstractItemBlock;
import elec332.core.util.RayTraceHelper;
import elec332.core.world.WorldHelper;
import elec332.test.block.BlockSubTile;
import elec332.test.tile.TileMultiObject;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;

/**
 * Created by Elec332 on 20-2-2018
 */
public class ItemSubTile extends AbstractItemBlock {

    public ItemSubTile(BlockSubTile block, Properties builder) {
        super(block, builder);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public boolean canBePlaced(@Nonnull BlockItemUseContext context, @Nonnull IBlockState state) {
        return true;
    }

    public void afterBlockPlaced(@Nonnull BlockItemUseContext context, @Nonnull IBlockState state, TileEntity tile) {
    }

    @Override
    protected boolean placeBlock(BlockItemUseContext context, @Nonnull IBlockState state) {
        Preconditions.checkNotNull(context);
        boolean ret = canBePlaced(context, state) && super.placeBlock(context, state);
        if (ret) {
            TileEntity tile = WorldHelper.getTileAt(context.getWorld(), context.getPos());
            if (tile != null) {
                afterBlockPlaced(context, state, tile);
            }
        }
        return ret;
    }

    public void onExistingObjectClicked(TileEntity tile, @Nonnull RayTraceResult hit, EntityPlayer player, ItemStack stack, IBlockState state) {
    }

    public void onEmptySolidSideClicked(TileEntity tile, @Nonnull EnumFacing hit, EntityPlayer player, ItemStack stack, IBlockState state, Vec3d hitVec) {
    }

    @SubscribeEvent //Using onRightClick doesn't work if there's a block directly above the wire
    @SuppressWarnings("all")
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().getItem() != this) {
            return;
        }
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = player.getHeldItem(event.getHand());
        if (WorldHelper.chunkLoaded(world, pos)) { //You never know...
            TileEntity tile = WorldHelper.getTileAt(world, pos);
            EnumFacing face = event.getFace();
            IBlockState state = WorldHelper.getBlockState(world, pos);
            if (state.getBlock() == getBlock() && tile instanceof TileMultiObject) { //attempt to add wire
                event.setUseItem(Event.Result.DENY);
                event.setUseBlock(Event.Result.DENY);
                event.setCanceled(true);
                if (!world.isRemote) { //All logic on the server side
                    Pair<Vec3d, Vec3d> vec = RayTraceHelper.getRayTraceVectors(player);
                    RayTraceResult hit = Block.collisionRayTrace(WorldHelper.getBlockState(world, pos), world, pos, vec.getLeft(), vec.getRight());
                    if (hit != null) { //Can be null
                        onExistingObjectClicked(tile, hit, player, stack, state);
                    }
                }
                player.swingArm(event.getHand());
            } else if (face != null) { //attempt to place at face
                if (state.getBlockFaceShape(world, pos, face) == BlockFaceShape.SOLID) {
                    tile = WorldHelper.getTileAt(world, pos.offset(face));
                    state = WorldHelper.getBlockState(world, pos.offset(face));
                    if (state.getBlock() == getBlock() && tile instanceof TileMultiObject) {
                        event.setUseItem(Event.Result.DENY);
                        event.setUseBlock(Event.Result.DENY);
                        event.setCanceled(true);
                        if (!world.isRemote) { //All logic on the server side
                            EnumFacing rf = face.getOpposite();
                            onEmptySolidSideClicked(tile, rf, player, stack, state, event.getHitVec());
                        }
                        player.swingArm(event.getHand());
                    }
                }
            }
        }

    }

}
