package elec332.test.api.wire;

import net.minecraft.item.EnumDyeColor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Elec332 on 19-2-2019
 */
public interface IWireData {

    @Nonnull
    public WireConnectionMethod getConnectionMethod();

    @Nullable
    public EnumDyeColor getColor();

    public double getResistivity(double length);

    @Nonnull
    public IWireType getWireType();

}
