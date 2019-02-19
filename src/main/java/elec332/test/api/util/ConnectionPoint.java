package elec332.test.api.util;

import com.google.common.base.Preconditions;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Created by Elec332 on 12-11-2017.
 */
@Immutable
public class ConnectionPoint {

    public static ConnectionPoint readFrom(@Nonnull NBTTagCompound tag) {
        Preconditions.checkNotNull(tag);
        BlockPos bp = BlockPos.fromLong(tag.getLong("bpL"));
        EnumFacing bpf = tag.contains("bpS") ? EnumFacing.byName(tag.getString("bpS")) : null;
        int n = tag.getInt("bpN");
        DimensionType w = DimensionType.byName(new ResourceLocation(tag.getString("bpW")));
        EnumFacing bpe = tag.contains("bpE") ? EnumFacing.byName(tag.getString("bpE")) : null;
        return new ConnectionPoint(bp, w, bpf, n, bpe);
    }

    public ConnectionPoint(@Nonnull BlockPos pos, @Nonnull IWorldReaderBase world, EnumFacing side, int sideNumber) {
        this(pos, world, side, sideNumber, null);
    }

    public ConnectionPoint(@Nonnull BlockPos pos, DimensionType world, EnumFacing side, int sideNumber) {
        this(pos, world, side, sideNumber, null);
    }

    public ConnectionPoint(@Nonnull BlockPos pos, @Nonnull IWorldReaderBase world, EnumFacing side, int sideNumber, @Nullable EnumFacing edge) {
        this(pos, Preconditions.checkNotNull(world, "Cannot fetch dimID from null world.").getDimension().getType(), side, sideNumber, edge);
    }

    public ConnectionPoint(@Nonnull BlockPos pos, DimensionType world, EnumFacing side, int sideNumber, @Nullable EnumFacing edge) {
        pos = Preconditions.checkNotNull(pos.toImmutable());
        boolean n = side != null && side.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
        //Preconditions.checkNotNull(side);
        this.pos = n ? pos.offset(side) : pos;
        this.side = n ? side.getOpposite() : side;
        this.sideNumber = sideNumber;
        this.world = world;
        this.edge = edge;
    }

    public static final ConnectionPoint NULL_POINT = new ConnectionPoint(new BlockPos(-1, -1, -1), DimensionType.OVERWORLD, EnumFacing.UP, -1);

    private final BlockPos pos;
    private final EnumFacing side, edge;
    private final int sideNumber;
    private final DimensionType world;

    public BlockPos getPos() {
        return pos;
    }

    @Nullable
    public EnumFacing getSide() {
        return side;
    }

    public int getSideNumber() {
        return sideNumber;
    }

    @Nullable
    public EnumFacing getEdge() {
        return edge;
    }

    public DimensionType getWorld() {
        return world;
    }

    public ConnectionPoint copy() {
        return new ConnectionPoint(pos, world, side, sideNumber, edge);
    }

    public ConnectionPoint copy(int sideNumber) {
        return new ConnectionPoint(pos, world, side, sideNumber, edge);
    }

    public NBTTagCompound serialize() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.putLong("bpL", getPos().toLong());
        if (getSide() != null) {
            tag.putString("bpS", getSide().getName());
        }
        tag.putInt("bpN", getSideNumber());
        tag.putString("bpW", Preconditions.checkNotNull(getWorld().getRegistryName()).toString());
        if (getEdge() != null) {
            tag.putString("bpE", getEdge().getName());
        }
        return tag;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ConnectionPoint && eq((ConnectionPoint) obj);
    }

    private boolean eq(ConnectionPoint cp) {
        return pos.equals(cp.pos) && side == cp.side && sideNumber == cp.sideNumber && world == cp.world && edge == cp.edge;
    }

    @Override
    public int hashCode() {
        return pos.hashCode() * 75 + sideNumber * 31 + (side == null ? 1 : 27 * side.hashCode()) + world.hashCode() * 98 + (edge == null ? 0 : edge.hashCode() * 39);
    }

    @Override
    public String toString() {
        return "Pos: " + pos + " world: " + world + " side: " + side + " num: " + sideNumber + " edge: " + edge;
    }

}

