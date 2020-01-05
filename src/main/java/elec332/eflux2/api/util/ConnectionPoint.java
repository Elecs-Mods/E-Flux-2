package elec332.eflux2.api.util;

import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Created by Elec332 on 12-11-2017.
 */
@Immutable
public final class ConnectionPoint {

    public static ConnectionPoint readFrom(@Nonnull CompoundNBT tag) {
        Preconditions.checkNotNull(tag);
        BlockPos bp = BlockPos.fromLong(tag.getLong("bpL"));
        Direction bpf = tag.contains("bpS") ? Direction.byName(tag.getString("bpS")) : null;
        int n = tag.getInt("bpN");
        DimensionType w = DimensionType.byName(new ResourceLocation(tag.getString("bpW")));
        Direction bpe = tag.contains("bpE") ? Direction.byName(tag.getString("bpE")) : null;
        return new ConnectionPoint(bp, w, bpf, n, bpe);
    }

    public ConnectionPoint(@Nonnull BlockPos pos, @Nonnull IWorldReader world, Direction side, int sideNumber) {
        this(pos, world, side, sideNumber, null);
    }

    public ConnectionPoint(@Nonnull BlockPos pos, DimensionType world, Direction side, int sideNumber) {
        this(pos, world, side, sideNumber, null);
    }

    public ConnectionPoint(@Nonnull BlockPos pos, @Nonnull IWorldReader world, Direction side, int sideNumber, @Nullable Direction edge) {
        this(pos, Preconditions.checkNotNull(world, "Cannot fetch dimID from null world.").getDimension().getType(), side, sideNumber, edge);
    }

    public ConnectionPoint(@Nonnull BlockPos pos, DimensionType world, Direction side, int sideNumber, @Nullable Direction edge) {
        pos = new BlockPos(pos);
        boolean n = side != null && side.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
        this.originalSide = side;
        this.pos = n ? pos.offset(side) : pos;
        this.side = n ? side.getOpposite() : side;
        this.sideNumber = sideNumber;
        this.world = world;
        this.edge = edge;
    }

    public static final ConnectionPoint NULL_POINT = new ConnectionPoint(new BlockPos(-1, -1, -1), DimensionType.OVERWORLD, Direction.UP, -1);

    private final BlockPos pos;
    private final Direction side, edge, originalSide;
    private final int sideNumber;
    private final DimensionType world;

    public BlockPos getPos() {
        return pos;
    }

    @Nullable
    public Direction getSide() {
        return side;
    }

    public int getSideNumber() {
        return sideNumber;
    }

    @Nullable
    public Direction getEdge() {
        return edge;
    }

    public DimensionType getWorld() {
        return world;
    }

    @Nullable
    public Direction getOriginalSide() {
        return originalSide;
    }

    public ConnectionPoint copy() {
        return new ConnectionPoint(pos, world, side, sideNumber, edge);
    }

    public ConnectionPoint copy(int sideNumber) {
        return new ConnectionPoint(pos, world, side, sideNumber, edge);
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.putLong("bpL", getPos().toLong());
        if (getSide() != null) {
            tag.putString("bpS", getSide().getName());
        }
        tag.putInt("bpN", getSideNumber());
        tag.putString("bpW", Preconditions.checkNotNull(DimensionType.getKey(getWorld())).toString());
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

