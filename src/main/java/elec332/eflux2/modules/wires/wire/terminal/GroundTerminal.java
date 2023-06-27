package elec332.eflux2.modules.wires.wire.terminal;

import elec332.core.util.math.IndexedVoxelShape;
import elec332.core.util.math.VectorHelper;
import elec332.core.world.WorldHelper;
import elec332.eflux2.api.EFlux2API;
import elec332.eflux2.api.electricity.IElectricityDevice;
import elec332.eflux2.api.electricity.IEnergyObject;
import elec332.eflux2.api.util.ConnectionPoint;
import elec332.eflux2.modules.wires.item.ItemGroundTerminal;
import elec332.eflux2.modules.wires.wire.overhead.OverheadWireHandler;
import net.minecraft.block.Block;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

/**
 * Created by Elec332 on 20-1-2018.
 */
public class GroundTerminal {

    @Nullable
    public static GroundTerminal makeTerminal(Direction placement, int size, Vec3d hitVec, DyeColor color) {
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

    public static GroundTerminal read(CompoundNBT tag) {
        Vec3d pos = new Vec3d(tag.getDouble("xp"), tag.getDouble("yp"), tag.getDouble("zp"));
        DyeColor color = tag.contains("color") ? DyeColor.values()[tag.getInt("color")] : null;
        Direction side = Direction.values()[tag.getByte("side")];
        int size = tag.getInt("size");
        return new GroundTerminal(side, size, pos, color);
    }

    public GroundTerminal(Direction side, int size, Vec3d pos, DyeColor color) {
        this(side, size, pos, color, getShape(pos, size, side));
        if (!isWithinBounds(aabb)) {
            throw new IllegalArgumentException();
        }
    }

    private GroundTerminal(Direction side, int size, Vec3d pos, DyeColor color, VoxelShape shape) {
        if (!pos.equals(lockToPixels(pos))) {
            throw new IllegalArgumentException();
        }
        this.color = color;
        this.side = side;
        this.aabb = new IndexedVoxelShape(shape, 20 * side.ordinal(), this);
        this.pos = pos;
        this.size = size;
        this.cp = null;
    }

    @Nullable
    private final DyeColor color;
    private final Direction side;
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

    public Direction getSide() {
        return side;
    }

    @Nullable
    public DyeColor getColor() {
        return color;
    }

    public int getSize() {
        return size;
    }

    public ConnectionPoint getConnectionPoint() {
        return cp;
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
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

    public void checkConnectionPoint(IWorld world, BlockPos myPos) {
        if (world == null || world.isRemote()) {
            return;
        }
        if (getColor() == null) {
            TileEntity tile = WorldHelper.getTileAt(world, myPos.offset(getSide()));
            if (tile != null) {
                LazyOptional<IElectricityDevice> ied = tile.getCapability(EFlux2API.ELECTRICITY_CAP);
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

    private void setNewCP(ConnectionPoint newCP, IWorld world) {
        if (this.cp != null && !this.cp.equals(newCP)) {
            OverheadWireHandler.INSTANCE.remove(this.cp, world);
        }
        this.cp = newCP;
    }

    //Helpers

    public static boolean canTerminalStay(World world, BlockPos pos, Direction placement) {
        return Block.hasSolidSide(WorldHelper.getBlockState(world, pos.offset(placement)), world, pos.offset(placement), placement.getOpposite());
    }

    public static Vec3d getPlacementLocationFromHitVec(Vec3d hitVec, Direction.Axis axis) {
        return new Vec3d(axis == Direction.Axis.X ? 1 - hitVec.x : hitVec.x, axis == Direction.Axis.Y ? 1 - hitVec.y : hitVec.y, axis == Direction.Axis.Z ? 1 - hitVec.z : hitVec.z);
    }

    public static boolean isPositionValid(Vec3d pos, Direction placement) {
        if (placement.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
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

    public static boolean isWithinBounds(Vec3d location, int size, Direction placement) {
        return isWithinBounds(getShape(location, size, placement));
    }

    public static boolean isWithinBounds(VoxelShape shape) {
        return VoxelShapes.combineAndSimplify(shape, VoxelShapes.fullCube(), IBooleanFunction.ONLY_FIRST).isEmpty();
    }

    public static VoxelShape getShape(Vec3d location, int size, Direction placement) {
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
