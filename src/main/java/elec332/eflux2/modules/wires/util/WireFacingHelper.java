package elec332.eflux2.modules.wires.util;

import net.minecraft.util.Direction;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

/**
 * Created by Elec332 on 17-2-2018.
 */
@SuppressWarnings("all")
public final class WireFacingHelper {

    private static final EnumSet<Direction> NS, EW;

    private static final Direction[] indexToFacing, st1, st2;
    private static final Direction[][] placementToIndex, placementToIndexReverse;
    private static final int[] horFacingToIndex;
    private static final boolean[][] checkSides;

    public static Direction getSideFromHorizontalIndex(int horFacingIndex) {
        checkHorFacing(horFacingIndex);
        return indexToFacing[horFacingIndex];
    }

    public static Direction getRealSide(Direction placement, int horFacingIndex) {
        checkHorFacing(horFacingIndex);
        return placementToIndex[placement.ordinal()][horFacingIndex];
    }

    public static Direction getHorizontalFacingFromReal(Direction placement, Direction realSide) {
        return placementToIndexReverse[placement.ordinal()][realSide.ordinal()];
    }

    public static Direction getRealSide(Direction placement, Direction horFacing) {
        return getRealSide(placement, getIndexFromHorizontalFacing(horFacing));
    }

    public static int getIndexFromHorizontalFacing(Direction horPaneFacing) {
        if (horPaneFacing.getAxis() == Direction.Axis.Y) {
            throw new IllegalArgumentException("Facing must be in the horizontal pane!");
        }
        return horFacingToIndex[horPaneFacing.ordinal()];
    }

    private static void checkHorFacing(int index) {
        if (index > 3 || index < 0) {
            throw new IllegalArgumentException("Index must be between 0 and 3");
        }
    }

    public static boolean isCheckSide(Direction placement, Direction realSide) {
        return checkSides[placement.ordinal()][realSide.ordinal()];
    }

    public static boolean isStraightLine(Collection<Direction> connections) {
        return connections.equals(NS) || connections.equals(EW);
    }

    static {
        Direction[] vals = Direction.values();
        indexToFacing = new Direction[]{
                Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
        };
        st1 = new Direction[]{
                Direction.UP, Direction.EAST, Direction.DOWN, Direction.WEST
        };
        st2 = new Direction[]{
                Direction.NORTH, Direction.DOWN, Direction.SOUTH, Direction.UP
        };
        placementToIndex = new Direction[vals.length][];
        placementToIndexReverse = new Direction[vals.length][];
        horFacingToIndex = new int[vals.length];
        Arrays.fill(horFacingToIndex, -1);
        checkSides = new boolean[vals.length][];
        for (Direction placement : vals) {
            int p = placement.ordinal();
            placementToIndex[p] = new Direction[4];
            placementToIndexReverse[p] = new Direction[6];
            checkSides[placement.ordinal()] = new boolean[6];
            for (int i = 0; i < 4; i++) {
                Direction realfacing = getFacingStuff(placement, i);
                placementToIndex[p][i] = realfacing;
                placementToIndexReverse[p][realfacing.ordinal()] = indexToFacing[i];
                if (placement == Direction.NORTH) { //Random facing, this needs to run once
                    horFacingToIndex[indexToFacing[i].ordinal()] = i;
                }
                if (isCheckSide_(placement, realfacing)) {
                    checkSides[placement.ordinal()][realfacing.ordinal()] = true;
                }
            }
        }

        NS = EnumSet.of(Direction.SOUTH, Direction.NORTH);
        EW = EnumSet.of(Direction.EAST, Direction.WEST);

    }

    private static boolean isCheckSide_(Direction placement, Direction realSide) {
        if (placement.getAxis() == Direction.Axis.Y) {
            boolean b = (placement.getAxisDirection() == Direction.AxisDirection.POSITIVE);
            return b == (realSide.getAxisDirection() == Direction.AxisDirection.NEGATIVE);
        }
        boolean plNeg = (placement.getAxisDirection() == Direction.AxisDirection.NEGATIVE) == (realSide.getAxis() == Direction.Axis.X);
        return plNeg != (realSide.getAxisDirection() == Direction.AxisDirection.NEGATIVE);
    }

    @Nonnull
    private static Direction getFacingStuff(Direction placement, int index) {
        if (index > 3 || index < 0) {
            throw new IllegalArgumentException();
        }
        switch (placement) {
            case UP:
                if (index % 2 == 0) {
                    return indexToFacing[index].getOpposite();
                }
            case DOWN:
                return indexToFacing[index];
            case SOUTH:
                if (index % 2 == 0) {
                    return st1[index].getOpposite();
                }
            case NORTH:
                return st1[index];
            case EAST:
                if (index % 2 == 1) {
                    return st2[index].getOpposite();
                }
            case WEST:
                return st2[index];
            default:
                throw new IllegalArgumentException();
        }
    }

}
