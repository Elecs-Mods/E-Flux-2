package elec332.eflux2.util;

import com.google.common.collect.Sets;
import elec332.core.util.math.IndexedBlockPos;
import elec332.core.world.WorldHelper;
import elec332.eflux2.wire.ground.GroundWire;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;

/**
 * Created by Elec332 on 3-2-2019
 */
public enum EnumConnectionPlace {

    CORNER_UP { // Cornering up, within the same block

        @Override
        public Pair<BlockPos, Direction> modify(World world, BlockPos myPos, GroundWire wire, Direction to) {
            if (EnumConnectionPlace.isValid(world, myPos, wire, EnumConnectionPlace.getShape(wire), to, this)) {
                return Pair.of(myPos, to);
            }
            return null;
        }

    },
    NORMAL { //Straight forward

        @Override
        public Pair<BlockPos, Direction> modify(World world, BlockPos myPos, GroundWire wire, Direction to) {
            if (EnumConnectionPlace.isValid(world, myPos, wire, EnumConnectionPlace.getShape(wire), to, this)) {
                return Pair.of(myPos.offset(to), wire.getPlacement());
            }
            return null;
        }

    },
    CORNER_DOWN { //Cornering down, other block

        @Override
        @SuppressWarnings("all")
        public Pair<BlockPos, Direction> modify(World world, BlockPos myPos, GroundWire wire, Direction to) {
            VoxelShape currentWire = EnumConnectionPlace.getShape(wire);
            BlockPos pos = myPos.offset(to);
            Vec3i off = to.getOpposite().getDirectionVec();
            VoxelShape block = WorldHelper.getBlockState(world, pos).getShape(world, pos);
            if (!block.isEmpty()) {
                Direction f = WireFacingHelper.getHorizontalFacingFromReal(wire.getPlacement(), to);
                VoxelShape newWire = wire.getBaseShape(Sets.newHashSet(f), false, wire.getColorBits(), ef -> this == CORNER_DOWN, ef -> this == CORNER_UP, false).withOffset(off.getX(), off.getY(), off.getZ());
                if (VoxelShapes.compare(block, newWire, IBooleanFunction.AND)) {
                    return null;
                }
            }
            if (EnumConnectionPlace.isValid(world, myPos, wire, currentWire, to, this)) {
                return Pair.of(pos.offset(wire.getPlacement()), to.getOpposite());
            }
            return null;
        }

    };

    @Nullable
    public abstract Pair<BlockPos, Direction> modify(World world, BlockPos myPos, GroundWire wire, Direction to);

    private static VoxelShape getShape(GroundWire wire) {
        return wire.getShape(true);//VoxelShapes.create(wire.getBaseShape(wire.getHorizontalConnections(), true).getBoundingBox().expand(0.002, 0.002, 0.002));
    }

    private static boolean isValid(World world, BlockPos pos, GroundWire wire, VoxelShape remFromBlock, Direction to, EnumConnectionPlace caller) {
        pos = new IndexedBlockPos(pos, 1);
        VoxelShape block = VoxelShapes.combineAndSimplify(WorldHelper.getBlockState(world, pos).getShape(world, pos), remFromBlock, IBooleanFunction.ONLY_FIRST);
        if (block.isEmpty()) {
            return true;
        }
        Direction f = WireFacingHelper.getHorizontalFacingFromReal(wire.getPlacement(), to);
        VoxelShape newWire = wire.getBaseShape(Sets.newHashSet(f), false, wire.getColorBits(), ef -> caller == CORNER_DOWN, ef -> caller == CORNER_UP, false);
        return !VoxelShapes.compare(block, newWire, IBooleanFunction.AND);
    }

}