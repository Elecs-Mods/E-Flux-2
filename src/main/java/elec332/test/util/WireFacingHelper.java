package elec332.test.util;

import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

/**
 * Created by Elec332 on 17-2-2018.
 */
@SuppressWarnings("all")
public final class WireFacingHelper {

    private static final EnumSet<EnumFacing> NS, EW;

    private static final EnumFacing[] indexToFacing, st1, st2;
    private static final EnumFacing[][] placementToIndex, placementToIndexReverse;
    private static final int[] horFacingToIndex;
    private static final boolean[][] checkSides;

    public static EnumFacing getSideFromHorizontalIndex(int horFacingIndex) {
        checkHorFacing(horFacingIndex);
        return indexToFacing[horFacingIndex];
    }

    public static EnumFacing getRealSide(EnumFacing placement, int horFacingIndex) {
        checkHorFacing(horFacingIndex);
        return placementToIndex[placement.ordinal()][horFacingIndex];
    }

    public static EnumFacing getHorizontalFacingFromReal(EnumFacing placement, EnumFacing realSide) {
        return placementToIndexReverse[placement.ordinal()][realSide.ordinal()];
    }

    public static EnumFacing getRealSide(EnumFacing placement, EnumFacing horFacing) {
        return getRealSide(placement, getIndexFromHorizontalFacing(horFacing));
    }

    public static int getIndexFromHorizontalFacing(EnumFacing horPaneFacing) {
        if (horPaneFacing.getAxis() == EnumFacing.Axis.Y) {
            throw new IllegalArgumentException("Facing must be in the horizontal pane!");
        }
        return horFacingToIndex[horPaneFacing.ordinal()];
    }

    private static void checkHorFacing(int index) {
        if (index > 3 || index < 0) {
            throw new IllegalArgumentException("Index must be between 0 and 3");
        }
    }

    public static boolean isCheckSide(EnumFacing placement, EnumFacing realSide) {
        return checkSides[placement.ordinal()][realSide.ordinal()];
    }

    public static boolean isStraightLine(Collection<EnumFacing> connections) {
        return connections.equals(NS) || connections.equals(EW);
    }

    static {
        EnumFacing[] vals = EnumFacing.values();
        indexToFacing = new EnumFacing[]{
                EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST
        };
        st1 = new EnumFacing[]{
                EnumFacing.UP, EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.WEST
        };
        st2 = new EnumFacing[]{
                EnumFacing.NORTH, EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.UP
        };
        placementToIndex = new EnumFacing[vals.length][];
        placementToIndexReverse = new EnumFacing[vals.length][];
        horFacingToIndex = new int[vals.length];
        Arrays.fill(horFacingToIndex, -1);
        checkSides = new boolean[vals.length][];
        for (EnumFacing placement : vals) {
            int p = placement.ordinal();
            placementToIndex[p] = new EnumFacing[4];
            placementToIndexReverse[p] = new EnumFacing[6];
            checkSides[placement.ordinal()] = new boolean[6];
            for (int i = 0; i < 4; i++) {
                EnumFacing realfacing = getFacingStuff(placement, i);
                placementToIndex[p][i] = realfacing;
                placementToIndexReverse[p][realfacing.ordinal()] = indexToFacing[i];
                if (placement == EnumFacing.NORTH) { //Random facing, this needs to run once
                    horFacingToIndex[indexToFacing[i].ordinal()] = i;
                }
                if (isCheckSide_(placement, realfacing)) {
                    checkSides[placement.ordinal()][realfacing.ordinal()] = true;
                }
            }
        }

        NS = EnumSet.of(EnumFacing.SOUTH, EnumFacing.NORTH);
        EW = EnumSet.of(EnumFacing.EAST, EnumFacing.WEST);

    }

    private static boolean isCheckSide_(EnumFacing placement, EnumFacing realSide) {
        if (placement.getAxis() == EnumFacing.Axis.Y) {
            boolean b = (placement.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE);
            return b == (realSide.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE);
        }
        boolean plNeg = (placement.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) == (realSide.getAxis() == EnumFacing.Axis.X);
        return plNeg != (realSide.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE);
    }

    @Nonnull
    private static EnumFacing getFacingStuff(EnumFacing placement, int index) {
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
