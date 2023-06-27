package elec332.eflux2.modules.wires.wire;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.DyeColor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 19-1-2018.
 */
public class WireColorHelper {

    private static final List<DyeColor> colorz;

    public static boolean hasWire(DyeColor color, int colors) {
        return (colors & (1 << color.getId())) != 0;
    }

    public static int addWire(DyeColor color, int colors) {
        colors |= 1 << color.getId();
        return colors;
    }

    public static int removeWire(DyeColor color, int colors) {
        colors &= ~(1 << color.getId());
        return colors;
    }

    public static List<DyeColor> getColors(int colors) {
        return colorz.stream().filter(DyeColor -> hasWire(DyeColor, colors)).collect(Collectors.toList());
    }

    public static int getSideIndex(DyeColor color) {
        return 1000 + color.getId();
    }

    static {
        colorz = ImmutableList.copyOf(DyeColor.values());
    }

}
