package elec332.eflux2.modules.wires;

import com.google.common.collect.ImmutableList;
import elec332.core.ElecCore;
import elec332.core.api.module.ElecModule;
import elec332.core.api.registration.APIInjectedEvent;
import elec332.core.block.BlockSubTile;
import elec332.core.client.RenderHelper;
import elec332.core.loader.SaveHandler;
import elec332.core.tile.sub.SubTileRegistry;
import elec332.core.util.FMLHelper;
import elec332.core.util.RegistryHelper;
import elec332.eflux2.EFlux2;
import elec332.eflux2.api.EFlux2API;
import elec332.eflux2.api.electricity.IElectricityDevice;
import elec332.eflux2.api.electricity.IEnergyObject;
import elec332.eflux2.api.electricity.component.ICircuitElementFactory;
import elec332.eflux2.api.wire.IWireType;
import elec332.eflux2.modules.wires.block.BlockWire;
import elec332.eflux2.modules.wires.client.TerminalColor;
import elec332.eflux2.modules.wires.item.ItemGroundTerminal;
import elec332.eflux2.modules.wires.item.ItemGroundWire;
import elec332.eflux2.modules.wires.item.ItemOverheadWire;
import elec332.eflux2.modules.wires.wire.AbstractWire;
import elec332.eflux2.modules.wires.wire.EnumWireType;
import elec332.eflux2.modules.wires.wire.ground.GroundWire;
import elec332.eflux2.modules.wires.wire.ground.IWireContainer;
import elec332.eflux2.modules.wires.wire.ground.SubTileWire;
import elec332.eflux2.modules.wires.wire.overhead.OverHeadWireHandlerClient;
import elec332.eflux2.modules.wires.wire.overhead.OverheadWireHandler;
import elec332.eflux2.modules.wires.wire.terminal.ISubTileTerminal;
import elec332.eflux2.modules.wires.wire.terminal.SubTileTerminal;
import elec332.eflux2.simulation.IElectricityTransformer;
import elec332.eflux2.simulation.TransformerElement;
import elec332.eflux2.simulation.WireElement;
import elec332.eflux2.util.EFlux2ResourceLocation;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 4-8-2020
 */
@ElecModule(owner = EFlux2.MODID, name = "Wires", alwaysEnabled = true)
public class WiresModule {

    public static final RegistryObject<BlockSubTile> GROUND_WIRE_BLOCK = EFlux2.BLOCKS.register("ground_wire", BlockWire::new);

    public static final RegistryObject<Item> GROUND_WIRE_ITEM = EFlux2.ITEMS.register("ground_wire", () -> new ItemGroundWire(EFlux2.createStandardProperties()));
    public static final RegistryObject<Item> TERMINAL_ITEM = EFlux2.ITEMS.register("terminal", () -> new ItemGroundTerminal(EFlux2.createStandardProperties()));
    public static final RegistryObject<Item> OVERHEAD_WIRE_ITEM = EFlux2.ITEMS.register("overhead_wire", () -> new ItemOverheadWire(EFlux2.createStandardProperties()));

    public static final Block WIRE_MARKER = new Block(Block.Properties.create(Material.CACTUS));

    @CapabilityInject(IWireContainer.class)
    public static Capability<IWireContainer> WIRE_CAPABILITY;

    @CapabilityInject(ISubTileTerminal.class)
    public static Capability<ISubTileTerminal> TERMINAL_CAPABILITY;

    public static IForgeRegistry<IWireType> wireTypeRegistry;

    public WiresModule() {
        RegistryHelper.registerEmptyCapability(IWireContainer.class);
        RegistryHelper.registerEmptyCapability(ISubTileTerminal.class);
    }

    @ElecModule.EventHandler
    public void preInit(FMLCommonSetupEvent event) {

        SubTileRegistry.INSTANCE.registerSubTile(SubTileWire.class, new EFlux2ResourceLocation("wire_sub_tile"));
        SubTileRegistry.INSTANCE.registerSubTile(SubTileTerminal.class, new EFlux2ResourceLocation("terminal_sub_tile"));

        wireTypeRegistry = RegistryHelper.createRegistry(new EFlux2ResourceLocation("wire_type_registry"), IWireType.class, b -> b.setIDRange(0, Short.MAX_VALUE));
        Arrays.stream(EnumWireType.values()).forEach(wireTypeRegistry::register);

        SaveHandler.INSTANCE.registerSaveHandler(FMLHelper.getActiveModContainer(), OverheadWireHandler.INSTANCE);
        ElecCore.networkHandler.registerNetworkObject(OverHeadWireHandlerClient.INSTANCE, OverheadWireHandler.INSTANCE);
    }

    @ElecModule.EventHandler
    public void clientSetup(FMLClientSetupEvent event) {
        TerminalColor tc = new TerminalColor();
        RenderHelper.getItemColors().register(tc, TERMINAL_ITEM.get());
        RenderHelper.getBlockColors().register(tc, GROUND_WIRE_BLOCK.get());
    }

    @ElecModule.EventHandler
    public void registerElements(APIInjectedEvent<ICircuitElementFactory> circuitRegistry) {
        circuitRegistry.getInjectedAPI().registerComponentWrapper(IElectricityTransformer.class, TransformerElement.class, TransformerElement::new);
        circuitRegistry.getInjectedAPI().registerComponentWrapper(AbstractWire.class, WireElement.class, WireElement::new);
    }

    @CapabilityInject(IElectricityDevice.class)
    private static void onCapabilityRegistered(Capability<IElectricityDevice> capability) {
        SubTileRegistry.INSTANCE.registerCapabilityInstanceCombiner(EFlux2API.ELECTRICITY_CAP, devices -> {
            final Set<IEnergyObject> objects = devices.stream().map(IElectricityDevice::getInternalComponents).flatMap(Collection::stream).collect(Collectors.toSet());
            return () -> objects;
        });
        SubTileRegistry.INSTANCE.setCapabilityCacheable(WIRE_CAPABILITY);
        //My eyes...
        SubTileRegistry.INSTANCE.registerCapabilityInstanceCombiner(WIRE_CAPABILITY, stw -> new IWireContainer() {

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
