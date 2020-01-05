package elec332.eflux2;

import elec332.core.ElecCore;
import elec332.core.api.mod.IElecCoreMod;
import elec332.core.api.mod.SidedProxy;
import elec332.core.api.network.ModNetworkHandler;
import elec332.core.api.registration.IObjectRegister;
import elec332.core.api.registration.IWorldGenRegister;
import elec332.core.config.ConfigWrapper;
import elec332.core.data.SaveHandler;
import elec332.core.grid.IStructureWorldEventHandler;
import elec332.core.handler.ElecCoreRegistrar;
import elec332.core.inventory.window.WindowManager;
import elec332.core.network.IElecNetworkHandler;
import elec332.core.util.FMLHelper;
import elec332.core.util.LoadTimer;
import elec332.core.util.RegistryHelper;
import elec332.eflux2.api.electricity.IElectricityDevice;
import elec332.eflux2.api.electricity.grid.IElectricityGridHandler;
import elec332.eflux2.api.wire.IWireType;
import elec332.eflux2.electricity.grid.ElectricityGridHandler;
import elec332.eflux2.inventory.window.EFluxWindowHandler;
import elec332.eflux2.proxies.CommonProxy;
import elec332.eflux2.register.EFlux2BlockRegister;
import elec332.eflux2.register.EFlux2ItemRegister;
import elec332.eflux2.register.EFlux2TileRegister;
import elec332.eflux2.tile.TileTestTransformer;
import elec332.eflux2.util.EFlux2ResourceLocation;
import elec332.eflux2.wire.EnumWireType;
import elec332.eflux2.wire.ground.tile.IWireContainer;
import elec332.eflux2.wire.overhead.OverHeadWireHandlerClient;
import elec332.eflux2.wire.overhead.OverheadWireHandler;
import elec332.eflux2.wire.terminal.tile.ISubTileTerminal;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Created by Elec332 on 31-12-2019
 */
@Mod(EFlux2.MODID)
public class EFlux2 implements IElecCoreMod {

    public EFlux2() {
        if (instance != null) {
            throw new RuntimeException();
        }
        instance = this;
        logger = LogManager.getLogger(MODNAME);

        IEventBus eventBus = FMLHelper.getActiveModEventBus();
        eventBus.addListener(this::preInit);
        eventBus.addListener(this::init);
        eventBus.addListener(this::postInit);
        eventBus.register(this);
        eventBus = MinecraftForge.EVENT_BUS;
        //eventBus.addListener(this::serverStarted);
        eventBus.register(this);
    }

    public static final String MODID = "eflux2";
    public static final String MODNAME = FMLHelper.getModNameEarly(MODID);

    @SidedProxy(clientSide = "elec332.eflux2.proxies.ClientProxy", serverSide = "elec332.eflux2.proxies.CommonProxy")
    public static CommonProxy proxy;

    public static final Block WIRE_MARKER = new Block(Block.Properties.create(Material.CACTUS));

    public static EFlux2 instance;
    @ModNetworkHandler
    public static IElecNetworkHandler networkHandler;
    public static Logger logger;
    private ConfigWrapper config;
    private LoadTimer loadTimer;

    public static ItemGroup itemGroup = new ItemGroup("test") {

        @Override
        public ItemStack createIcon() {
            return new ItemStack(Items.APPLE);
        }

    };
    public static IForgeRegistry<IWireType> wireTypeRegistry;
    public static IElectricityGridHandler electricityGridHandler;

    @CapabilityInject(IWireContainer.class)
    public static Capability<IWireContainer> WIRE_CAPABILITY;

    @CapabilityInject(ISubTileTerminal.class)
    public static Capability<ISubTileTerminal> TERMINAL_CAPABILITY;

    private void preInit(FMLCommonSetupEvent event) {
        this.loadTimer = new LoadTimer(logger, MODNAME);
        this.loadTimer.startPhase(event);
        this.config = new ConfigWrapper(this);
        RegistryHelper.registerEmptyCapability(IWireContainer.class);
        RegistryHelper.registerEmptyCapability(IElectricityDevice.class);
        RegistryHelper.registerEmptyCapability(ISubTileTerminal.class);
        ElecCoreRegistrar.GRIDHANDLERS.register((IStructureWorldEventHandler) (electricityGridHandler = new ElectricityGridHandler()));
        wireTypeRegistry = RegistryHelper.createRegistry(new EFlux2ResourceLocation("wire_type_registry"), IWireType.class, b -> b.setIDRange(0, Short.MAX_VALUE));
        loadTimer.endPhase(event);
    }

    public void init(InterModEnqueueEvent event) {
        this.loadTimer.startPhase(event);
        SaveHandler.INSTANCE.registerSaveHandler(FMLHelper.getActiveModContainer(), OverheadWireHandler.INSTANCE);
        ElecCore.networkHandler.registerNetworkObject(OverHeadWireHandlerClient.INSTANCE, OverheadWireHandler.INSTANCE);
        Arrays.stream(EnumWireType.values()).forEach(wireTypeRegistry::register);
        WindowManager.INSTANCE.register(EFluxWindowHandler.INSTANCE, new EFlux2ResourceLocation("window_handler"));
        proxy.registerColors();
        loadTimer.endPhase(event);
    }

    private void postInit(InterModProcessEvent event) {
        this.loadTimer.startPhase(event);
        //Mod compat stuff
        loadTimer.endPhase(event);
    }

    @Override
    public void registerRegisters(Consumer<IObjectRegister<?>> objectHandler, Consumer<IWorldGenRegister> worldHandler) {
        objectHandler.accept(new EFlux2ItemRegister());
        objectHandler.accept(new EFlux2BlockRegister());
        objectHandler.accept(new EFlux2TileRegister());
    }

}
