package elec332.test.wire.terminal;

import elec332.core.util.VectorHelper;
import elec332.core.world.WorldHelper;
import elec332.test.api.TestModAPI;
import elec332.test.api.electricity.IElectricityDevice;
import elec332.test.api.electricity.IEnergyObject;
import elec332.test.api.util.ConnectionPoint;
import elec332.test.item.ItemGroundTerminal;
import elec332.test.wire.overhead.OverheadWireHandler;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

/**
 * Created by Elec332 on 20-1-2018.
 */
public class GroundTerminal {

    @Nullable
    public static GroundTerminal makeTerminal(EnumFacing placement, int size, Vec3d hitVec, EnumDyeColor color) {
        Vec3d pos = getPlacementLocationFromHitVec(lockToPixels(hitVec), placement.getAxis());
        if (!isPositionValid(pos, placement)) {
            return null;
        }
        VoxelShape shape = getShape(pos, size, placement);
        if (isWithinBounds(shape)) {
            return new GroundTerminal(placement, size, pos, color, shape);
        }
        return null;
    }

    public static GroundTerminal read(NBTTagCompound tag) {
        Vec3d pos = new Vec3d(tag.getDouble("xp"), tag.getDouble("yp"), tag.getDouble("zp"));
        EnumDyeColor color = tag.contains("color") ? EnumDyeColor.values()[tag.getInt("color")] : null;
        EnumFacing side = EnumFacing.values()[tag.getByte("side")];
        int size = tag.getInt("size");
        return new GroundTerminal(side, size, pos, color);
    }

    public GroundTerminal(EnumFacing side, int size, Vec3d pos, EnumDyeColor color) {
        this(side, size, pos, color, getShape(pos, size, side));
        if (!isWithinBounds(aabb)) {
            throw new IllegalArgumentException();
        }
    }

    private GroundTerminal(EnumFacing side, int size, Vec3d pos, EnumDyeColor color, VoxelShape shape) {
        if (!pos.equals(lockToPixels(pos))) {
            throw new IllegalArgumentException();
        }
        this.color = color;
        this.side = side;
        this.aabb = shape;
        this.pos = pos;
        this.size = size;
        this.cp = null;
    }

    @Nullable
    private final EnumDyeColor color;
    private final EnumFacing side;
    private final VoxelShape aabb;
    private final int size;
    private final Vec3d pos;
    private ConnectionPoint cp;

    public VoxelShape getShape() {
        return aabb;
    }

    public Vec3d getLocation() {
        return pos;
    }

    public EnumFacing getSide() {
        return side;
    }

    @Nullable
    public EnumDyeColor getColor() {
        return color;
    }

    public int getSize() {
        return size;
    }

    public ConnectionPoint getConnectionPoint() {
        return cp;
    }

    public NBTTagCompound serialize() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.putInt("size", getSize());
        if (color != null) {
            tag.putInt("color", color.getId());
        }
        tag.putDouble("xp", pos.x);
        tag.putDouble("yp", pos.y);
        tag.putDouble("zp", pos.z);
        tag.putByte("side", (byte) getSide().ordinal());
        return tag;
    }

    public ItemStack getStack() {
        return ItemGroundTerminal.withColor(getColor(), getSize());
    }

    public boolean canStay(World world, BlockPos pos) {
        return canTerminalStay(world, pos, getSide());
    }

    public void checkConnectionPoint(IWorldReaderBase world, BlockPos myPos) {
        if (world == null || world.isRemote()) {
            return;
        }
        if (getColor() == null) {
            TileEntity tile = WorldHelper.getTileAt(world, myPos.offset(getSide()));
            if (tile != null) {
                LazyOptional<IElectricityDevice> ied = tile.getCapability(TestModAPI.ELECTRICITY_CAP);
                if (ied.isPresent()) {
                    Vec3d vec = VectorHelper.subtractFrom(getLocation(), getSide().getDirectionVec());
                    for (IEnergyObject eo : ied.orElseThrow(NullPointerException::new).getInternalComponents()) {
                        ConnectionPoint cp = eo.getConnectionPoint(getSide().getOpposite(), vec);
                        if (cp != null) {
                            setNewCP(cp, world);
                            return;
                        }
                    }
                }
            }
        } else {
            setNewCP(new ConnectionPoint(myPos, world, null, getColor().getId(), getSide()), world);
        }
    }

    private void setNewCP(ConnectionPoint newCP, IWorldReaderBase world) {
        if (this.cp != null && !this.cp.equals(newCP)) {
            OverheadWireHandler.INSTANCE.remove(this.cp, world);
        }
        this.cp = newCP;
    }

    //Helpers

    public static boolean canTerminalStay(World world, BlockPos pos, EnumFacing placement) {
        return WorldHelper.getBlockState(world, pos.offset(placement)).getBlockFaceShape(world, pos.offset(placement), placement.getOpposite()) != BlockFaceShape.UNDEFINED;
    }

    public static Vec3d getPlacementLocationFromHitVec(Vec3d hitVec, EnumFacing.Axis axis) {
        return new Vec3d(axis == EnumFacing.Axis.X ? 1 - hitVec.x : hitVec.x, axis == EnumFacing.Axis.Y ? 1 - hitVec.y : hitVec.y, axis == EnumFacing.Axis.Z ? 1 - hitVec.z : hitVec.z);
    }

    public static boolean isPositionValid(Vec3d pos, EnumFacing placement) {
        if (placement.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
            return pos.equals(VectorHelper.multiplyVectors(pos, VectorHelper.zeroOnAxis(placement)));
        } else {
            Vec3d oneOnAxis = VectorHelper.oneOnAxis(placement);
            return (pos.x * oneOnAxis.x + pos.y * oneOnAxis.y + pos.z * oneOnAxis.z) == 1;
        }
    }

    public static Vec3d lockToPixels(Vec3d hitVec) {
        float mul = 16f;
        hitVec = new Vec3d((int) (hitVec.x * mul), (int) (hitVec.y * mul), (int) (hitVec.z * mul));
        return hitVec.scale(1 / 16f);
    }

    public static boolean isWithinBounds(Vec3d location, int size, EnumFacing placement) {
        return isWithinBounds(getShape(location, size, placement));
    }

    public static boolean isWithinBounds(VoxelShape shape) {
        return VoxelShapes.combineAndSimplify(shape, VoxelShapes.fullCube(), IBooleanFunction.ONLY_FIRST).isEmpty();
    }

    public static VoxelShape getShape(Vec3d location, int size, EnumFacing placement) {
        float sizeScalar = (size + 2.6f) / 4f;
        float width = sizeScalar * 6 / 16f;
        float offset = width / 2;
        float height = 8 * sizeScalar / 16f;

        Vec3d oneOnAxis = VectorHelper.oneOnAxis(placement);
        Vec3d zeroOnAxis = VectorHelper.zeroOnAxis(placement);
        Vec3d translation = oneOnAxis.scale(placement.getAxisDirection().getOffset());
        translation = translation.add(zeroOnAxis);
        translation = translation.scale(-1);

        Vec3d left = zeroOnAxis.scale(offset);
        Vec3d right = left.subtract(oneOnAxis.scale(height));
        left = location.subtract(left);
        right = location.subtract(VectorHelper.multiplyVectors(right, translation));

        return VoxelShapes.create(left.x, left.y, left.z, right.x, right.y, right.z);
    }

}
