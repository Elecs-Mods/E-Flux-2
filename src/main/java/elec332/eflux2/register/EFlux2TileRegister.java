package elec332.eflux2.register;

import com.google.common.collect.ImmutableList;
import elec332.core.api.registration.ITileRegister;
import elec332.core.tile.sub.SubTileRegistry;
import elec332.eflux2.EFlux2;
import elec332.eflux2.api.EFlux2API;
import elec332.eflux2.api.electricity.IElectricityDevice;
import elec332.eflux2.api.electricity.IEnergyObject;
import elec332.eflux2.util.EFlux2ResourceLocation;
import elec332.eflux2.wire.ground.GroundWire;
import elec332.eflux2.wire.ground.tile.IWireContainer;
import elec332.eflux2.wire.ground.tile.SubTileWire;
import elec332.eflux2.wire.terminal.tile.SubTileTerminal;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 1-1-2020
 */
public class EFlux2TileRegister implements ITileRegister {

    @Override
    public void preRegister() {
        SubTileRegistry.INSTANCE.registerSubTile(SubTileWire.class, new EFlux2ResourceLocation("wire_sub_tile"));
        SubTileRegistry.INSTANCE.registerSubTile(SubTileTerminal.class, new EFlux2ResourceLocation("terminal_sub_tile"));

    }

    @Override
    public void register(IForgeRegistry<TileEntityType<?>> registry) {
    }

    @CapabilityInject(IElectricityDevice.class)
    private static void onCapabilityRegistered(Capability<IElectricityDevice> capability) {
        SubTileRegistry.INSTANCE.registerCapabilityInstanceCombiner(EFlux2API.ELECTRICITY_CAP, devices -> {
            final Set<IEnergyObject> objects = devices.stream().map(IElectricityDevice::getInternalComponents).flatMap(Collection::stream).collect(Collectors.toSet());
            return () -> objects;
        });
        SubTileRegistry.INSTANCE.setCapabilityCacheable(EFlux2.WIRE_CAPABILITY);
        //My eyes...
        SubTileRegistry.INSTANCE.registerCapabilityInstanceCombiner(EFlux2.WIRE_CAPABILITY, stw -> new IWireContainer() {

            {
                wires = ImmutableList.copyOf(stw);
                List<IWireContainer> l = stw.stream().filter(IWireContainer::isRealWireContainer).collect(Collectors.toList());
                if (l.size() > 1) {
                    throw new UnsupportedOperationException();
                }
                main = l.isEmpty() ? null : l.get(0);
            }

            private final List<IWireContainer> wires;
            private final IWireContainer main;

            @Override
            public boolean addWire(GroundWire wire) {
                for (IWireContainer wc : wires) {
                    if (wc.addWire(wire)) {
                        return true;
                    }
                }
                return false;
            }

            @Nullable
            @Override
            public GroundWire getWire(Direction facing) {
                return main == null ? null : main.getWire(facing);
            }

            @Nonnull
            @Override
            public List<GroundWire> getWireView() {
                return main == null ? ImmutableList.of() : main.getWireView();
            }


        });
    }

}
