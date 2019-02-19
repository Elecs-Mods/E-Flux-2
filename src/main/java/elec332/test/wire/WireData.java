package elec332.test.wire;

import com.google.common.base.Preconditions;
import elec332.core.util.NBTBuilder;
import elec332.test.TestMod;
import elec332.test.api.wire.EnumWireThickness;
import elec332.test.api.wire.IWireData;
import elec332.test.api.wire.IWireType;
import elec332.test.api.wire.WireConnectionMethod;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Created by Elec332 on 19-11-2017.
 */
@Immutable
public class WireData implements IWireData {

    public static WireData read(NBTTagCompound tag) {
        NBTBuilder data = NBTBuilder.from(tag);
        IWireType wireType = data.getRegistryObject("wireT", TestMod.wireTypeRegistry);
        return new WireData(wireType, data.getEnum("tkns", EnumWireThickness.class), data.getEnum("wcm", WireConnectionMethod.class), data.getColor("clr"));
    }

    public WireData(IWireType wireType, EnumWireThickness thickness, WireConnectionMethod connectionMethod) {
        this(wireType, thickness, connectionMethod, null);
    }

    public WireData(IWireType wireType, EnumWireThickness thickness, WireConnectionMethod connectionMethod, EnumDyeColor color) {
        this.wireType = Preconditions.checkNotNull(wireType);
        this.thickness = Preconditions.checkNotNull(thickness);
        this.connectionMethod = Preconditions.checkNotNull(connectionMethod);
        this.color = color;
        this.resistivityBase = wireType.getResistivity() / (thickness.surfaceAreaR * 0.001 * 0.001);
        if (TestMod.wireTypeRegistry.getValue(wireType.getRegistryName()) != wireType) {
            throw new IllegalArgumentException();
        }
    }

    private final IWireType wireType;
    private final WireConnectionMethod connectionMethod;
    private final EnumWireThickness thickness;
    @Nullable
    private final EnumDyeColor color;
    private final double resistivityBase;

    @Override
    public double getResistivity(double length) {
        Preconditions.checkArgument(length > 0);
        return length * resistivityBase;
    }

    @Nonnull
    @Override
    public IWireType getWireType() {
        return wireType;
    }

    @Nonnull
    @Override
    public WireConnectionMethod getConnectionMethod() {
        return connectionMethod;
    }

    @Nullable
    @Override
    public EnumDyeColor getColor() {
        return color;
    }

    public WireData copy(EnumDyeColor color) {
        if (color == this.color) {
            return this;
        }
        return new WireData(wireType, thickness, connectionMethod, color);
    }

    public NBTTagCompound serialize() {
        NBTBuilder data = new NBTBuilder();
        data.setRegistryObject("wireT", wireType);
        data.setEnum("tkns", thickness);
        data.setEnum("wcm", connectionMethod);
        data.setColor("clr", color);
        return data.get();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WireData && equals_((WireData) obj);
    }

    private boolean equals_(WireData wireData) {
        return wireType == wireData.wireType && connectionMethod == wireData.connectionMethod && thickness == wireData.thickness && color == wireData.color;
    }

}
