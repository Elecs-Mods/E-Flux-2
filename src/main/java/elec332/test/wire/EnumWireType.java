package elec332.test.wire;

import elec332.test.api.wire.IWireType;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Created by Elec332 on 23-11-2017.
 */
public enum EnumWireType implements IWireType {

    TEST(1e-8, 1e10);

    EnumWireType(double resistivity, double massM3) {
        this.resistivity = resistivity;
        this.mass = massM3;
    }

    private final double resistivity, mass;

    @Override
    public double getResistivity() {
        return this.resistivity;
    }

    @Override
    public double getMassM3() {
        return this.mass;
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation("eleccore", getName());
    }

    public String getName() {
        return toString().toLowerCase();
    }

}
