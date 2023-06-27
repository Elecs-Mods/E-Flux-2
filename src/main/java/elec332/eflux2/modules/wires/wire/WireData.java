package elec332.eflux2.modules.wires.wire;

import com.google.common.base.Preconditions;
import elec332.core.util.NBTBuilder;
import elec332.eflux2.api.wire.EnumWireThickness;
import elec332.eflux2.api.wire.IWireData;
import elec332.eflux2.api.wire.IWireType;
import elec332.eflux2.api.wire.WireConnectionMethod;
import elec332.eflux2.modules.wires.WiresModule;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Created by Elec332 on 19-11-2017.
 */
@Immutable
public class WireData implements IWireData {

    public static WireData read(CompoundNBT tag) {
        NBTBuilder data = NBTBuilder.from(tag);
        IWireType wireType = data.getRegistryObject("wireT", WiresModule.wireTypeRegistry);
        return new WireData(wireType, data.getEnum("tkns", EnumWireThickness.class), data.getEnum("wcm", WireConnectionMethod.class), data.getColor("clr"));
    }

    public WireData(IWireType wireType, EnumWireThickness thickness, WireConnectionMethod connectionMethod) {
        this(wireType, thickness, connectionMethod, null);
    }

    public WireData(IWireType wireType, EnumWireThickness thickness, WireConnectionMethod connectionMethod, DyeColor color) {
        this.wireType = Preconditions.checkNotNull(wireType);
        this.thickness = Preconditions.checkNotNull(thickness);
        this.connectionMethod = Preconditions.checkNotNull(connectionMethod);
        this.color = color;
        this.resistivityBase = wireType.getResistivity() / (thickness.surfaceAreaR * 0.001 * 0.001);
        if (WiresModule.wireTypeRegistry.getValue(wireType.getRegistryName()) != wireType) {
            throw new IllegalArgumentException();
        }
    }

    private final IWireType wireType;
    private final WireConnectionMethod connectionMethod;
    private final EnumWireThickness thickness;
    @Nullable
    private final DyeColor color;
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
    public DyeColor getColor() {
        return color;
    }

    public WireData copy(DyeColor color) {
        if (color == this.color) {
            return this;
        }
        return new WireData(wireType, thickness, connectionMethod, color);
    }

    public CompoundNBT serialize() {
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
