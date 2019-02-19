package elec332.test;

import com.google.common.collect.Sets;
import elec332.core.ElecCore;
import elec332.core.client.RenderHelper;
import elec332.core.data.SaveHandler;
import elec332.core.grid.IStructureWorldEventHandler;
import elec332.core.handler.ElecCoreRegistrar;
import elec332.core.util.FMLHelper;
import elec332.core.util.RegistryHelper;
import elec332.test.api.TestModAPI;
import elec332.test.api.electricity.IElectricityDevice;
import elec332.test.api.electricity.IEnergyObject;
import elec332.test.api.electricity.grid.IElectricityGridHandler;
import elec332.test.api.wire.IWireType;
import elec332.test.block.BlockGenerator;
import elec332.test.block.BlockTest;
import elec332.test.block.BlockTile;
import elec332.test.client.TerminalColor;
import elec332.test.client.wire.TerminalItemModelCache;
import elec332.test.client.wire.WireModelCache;
import elec332.test.electricity.grid.ElectricityGridHandler;
import elec332.test.item.ItemGroundTerminal;
import elec332.test.item.ItemGroundWire;
import elec332.test.item.ItemOverheadWire;
import elec332.test.tile.TileTestReceiver;
import elec332.test.tile.TileTestTransformer;
import elec332.test.util.SubTileRegistry;
import elec332.test.util.TestModResourceLocation;
import elec332.test.wire.EnumWireType;
import elec332.test.wire.ground.tile.ISubTileWire;
import elec332.test.wire.ground.tile.SubTileWire;
import elec332.test.wire.overhead.OverHeadWireHandlerClient;
import elec332.test.wire.overhead.OverheadWireHandler;
import elec332.test.wire.terminal.tile.ISubTileTerminal;
import elec332.test.wire.terminal.tile.SubTileTerminal;
import mcp.mobius.waila.api.impl.WailaRegistrar;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Arrays;
import java.util.Set;

/**
 * Created by Elec332 on 31-1-2019
 */
public class TestMod {

    public static BlockTest block = (BlockTest) new BlockTest().setRegistryName(new TestModResourceLocation("testblock"));
    public static ItemGroup test = new ItemGroup("test") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Items.APPLE);
        }
    };
    public static Item wire = new ItemGroundWire(new Item.Properties().group(TestMod.test)).setRegistryName(block.getRegistryName());
    public static Item terminal = new ItemGroundTerminal(new Item.Properties().group(test)).setRegistryName(new TestModResourceLocation("terminaltest"));

    public static WireModelCache model = new WireModelCache();
    public static TerminalItemModelCache terminalModel = new TerminalItemModelCache();

    public static Block b1, b2, b3, b4;

    public static final Block WIRE_MARKER = new Block(Block.Properties.create(Material.CACTUS));

    public static IForgeRegistry<IWireType> wireTypeRegistry;

    public TestMod() {
        ((FMLModContainer) FMLHelper.getModContainer(ElecCore.instance)).getEventBus().register(new Object() {

            @SubscribeEvent
            public void registerBlocks(RegistryEvent.Register<Block> blockRegister) {
                blockRegister.getRegistry().register(block);
                blockRegister.getRegistry().register(b1 = new BlockTile(Block.Properties.create(Material.CACTUS), TileTestReceiver::new).setRegistryName(new TestModResourceLocation("testreceiver")));
                blockRegister.getRegistry().register(b2 = new BlockGenerator(Block.Properties.create(Material.CACTUS)).setRegistryName(new TestModResourceLocation("testpowertgen")));
                blockRegister.getRegistry().register(b3 = new BlockTile(Block.Properties.create(Material.ANVIL), TileTestTransformer::new).setRegistryName(new TestModResourceLocation("testtransformer")));
                System.out.println("regblock");
            }

            @SubscribeEvent
            public void registerItems(RegistryEvent.Register<Item> itemRegister) {
                itemRegister.getRegistry().register(wire);
                itemRegister.getRegistry().register(terminal);
                itemRegister.getRegistry().register(new ItemOverheadWire(new Item.Properties().group(test)).setRegistryName(new TestModResourceLocation("overhead_wire")));
                refor(b1, itemRegister.getRegistry());
                refor(b2, itemRegister.getRegistry());
                refor(b3, itemRegister.getRegistry());
                System.out.println("regitem");
                WailaRegistrar.INSTANCE.toString();
            }

            private void refor(Block block, IForgeRegistry<Item> reg) {
                reg.register(new ItemBlock(block, new Item.Properties().group(test)).setRegistryName(block.getRegistryName()));
            }

        });
    }

    public static IElectricityGridHandler electricityGridHandler;

    @CapabilityInject(ISubTileWire.class)
    public static Capability<ISubTileWire> WIRE_CAPABILITY;

    @CapabilityInject(ISubTileTerminal.class)
    public static Capability<ISubTileTerminal> TERMINAL_CAPABILITY;

    public void preInit() {
        System.out.println("preinit");
        RegistryHelper.registerEmptyCapability(ISubTileWire.class);
        RegistryHelper.registerEmptyCapability(IElectricityDevice.class);
        RegistryHelper.registerEmptyCapability(ISubTileTerminal.class);
        ElecCoreRegistrar.GRIDHANDLERS.register((IStructureWorldEventHandler) (electricityGridHandler = new ElectricityGridHandler()));
        wireTypeRegistry = RegistryHelper.createRegistry(new TestModResourceLocation("wire_typr_registry"), IWireType.class, b -> b.setIDRange(0, Short.MAX_VALUE));
    }

    public void init() {
        System.out.println("init");
        SubTileRegistry.INSTANCE.registerSubTile(SubTileWire.class, new TestModResourceLocation("wiretestsub"));
        SubTileRegistry.INSTANCE.registerSubTile(SubTileTerminal.class, new TestModResourceLocation("terminaltestsub"));
        TerminalColor tc = new TerminalColor();
        RenderHelper.getItemColors().register(tc, terminal);
        RenderHelper.getBlockColors().register(tc, block);
        SaveHandler.INSTANCE.registerSaveHandler(FMLHelper.getActiveModContainer(), OverheadWireHandler.INSTANCE);
        ElecCore.networkHandler.registerNetworkObject(OverHeadWireHandlerClient.INSTANCE, OverheadWireHandler.INSTANCE);
        Arrays.stream(EnumWireType.values()).forEach(wireTypeRegistry::register);
    }

    public void postInit() {
        System.out.println("postinit");
        SubTileRegistry.INSTANCE.registerCapabilityCombiner(TestModAPI.ELECTRICITY_CAP, lazyOptionals -> {
            Set<IEnergyObject> objects = Sets.newHashSet();
            lazyOptionals.forEach(c -> {
                if (!c.isPresent()) {
                    throw new RuntimeException();
                }
                objects.addAll(c.orElseThrow(NullPointerException::new).getInternalComponents());
            });
            LazyOptional<IElectricityDevice> ret = LazyOptional.of(() -> (IElectricityDevice) () -> objects);
            lazyOptionals.forEach(lo -> lo.addListener(lo2 -> ret.invalidate()));
            return ret;
        });
    }

}
