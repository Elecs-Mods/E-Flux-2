package elec332.test.wire;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.EnumDyeColor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 19-1-2018.
 */
public class WireColorHelper {

    private static final List<EnumDyeColor> colorz;

    public static boolean hasWire(EnumDyeColor color, int colors) {
        return (colors & (1 << color.getId())) != 0;
    }

    public static int addWire(EnumDyeColor color, int colors) {
        colors |= 1 << color.getId();
        return colors;
    }

    public static int removeWire(EnumDyeColor color, int colors) {
        colors &= ~(1 << color.getId());
        return colors;
    }

    public static List<EnumDyeColor> getColors(int colors) {
        return colorz.stream().filter(enumDyeColor -> hasWire(enumDyeColor, colors)).collect(Collectors.toList());
    }

    public static int getSideIndex(EnumDyeColor color) {
        return 1000 + color.getId();
    }

    static {
        colorz = ImmutableList.copyOf(EnumDyeColor.values());
    }

}
