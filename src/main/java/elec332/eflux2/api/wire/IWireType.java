package elec332.eflux2.api.wire;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;

/**
 * Created by Elec332 on 6-11-2017.
 */
public interface IWireType extends IForgeRegistryEntry<IWireType> {

    public abstract double getResistivity();

    public abstract double getMassM3();

    @Nonnull
    @Override
    public ResourceLocation getRegistryName();

    @Override
    default public IWireType setRegistryName(ResourceLocation name) {
        throw new UnsupportedOperationException();
    }

    @Override
    default public Class<IWireType> getRegistryType() {
        return IWireType.class;
    }

}
