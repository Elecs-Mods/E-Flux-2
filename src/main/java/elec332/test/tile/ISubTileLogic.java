package elec332.test.tile;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Elec332 on 20-2-2018
 */
public interface ISubTileLogic extends ICapabilityProvider {

    public void readFromNBT(NBTTagCompound compound);

    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound);

    @Nullable
    public World getWorld();

    public BlockPos getPos();

    public void markDirty();

    public boolean hasWorld();

    public default void onRemoved() {
    }

    public default void neighborChanged(BlockPos neighborPos, Block changedBlock, boolean observer) {
    }

    public default boolean removedByPlayer(@Nonnull EntityPlayer player, boolean willHarvest, @Nonnull RayTraceResult hit) {
        return false;
    }

    public default boolean canBeRemoved() {
        return true;
    }

    public default boolean onBlockActivated(EntityPlayer player, EnumHand hand, RayTraceResult hit) {
        return false;
    }

    public default VoxelShape getShape(int data) {
        return VoxelShapes.fullCube();
    }

    default public VoxelShape getSelectionBox(@Nonnull RayTraceResult hit, EntityPlayer player) {
        return getShape(hit.subHit);
    }

    default public RayTraceResult getRayTraceResult(Vec3d start, Vec3d end, int data) {
        return getShape(data).func_212433_a(start, end, getPos());
    }

    @Nullable
    public default ItemStack getStack(@Nonnull RayTraceResult hit, EntityPlayer player) {
        return null;
    }

    public default void invalidate() {
    }

    public default void onLoad() {
    }

    public default void sendInitialLoadPackets() {
    }

    public void sendPacket(int ID, NBTTagCompound data);

    public default void onDataPacket(int id, NBTTagCompound tag) {
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side);

}
