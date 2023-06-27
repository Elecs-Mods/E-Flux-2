package elec332.eflux2;

import com.google.common.base.Preconditions;
import elec332.core.api.mod.IElecCoreMod;
import elec332.core.config.ConfigWrapper;
import elec332.core.grid.IStructureWorldEventHandler;
import elec332.core.handler.ElecCoreRegistrar;
import elec332.core.util.FMLHelper;
import elec332.core.util.LoadTimer;
import elec332.core.util.RegistryHelper;
import elec332.eflux2.api.electricity.IElectricityDevice;
import elec332.eflux2.api.electricity.grid.IElectricityGridHandler;
import elec332.eflux2.electricity.grid.ElectricityGridHandler;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        config = new ConfigWrapper(FMLHelper.getActiveModContainer());
        config = new ConfigWrapper(FMLHelper.getActiveModContainer(), ModConfig.Type.CLIENT);

        IEventBus eventBus = FMLHelper.getActiveModEventBus();
        eventBus.addListener(this::preInit);
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }

    public static final String MODID = "eflux2";
    public static final String MODNAME = FMLHelper.getModNameEarly(MODID);

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);

    public static boolean noCircuitCompression = false;

    public static EFlux2 instance;
    public static Logger logger;
    public static ConfigWrapper config;
    public static ConfigWrapper clientConfig;

    public static ItemGroup itemGroup = new ItemGroup("test") {

        @Override
        public ItemStack createIcon() {
            return new ItemStack(Items.APPLE);
        }

    };
    public static IElectricityGridHandler electricityGridHandler;

    private void preInit(FMLCommonSetupEvent event) {
        LoadTimer loadTimer = new LoadTimer(logger, MODNAME);
        loadTimer.startPhase(event);
        RegistryHelper.registerEmptyCapability(IElectricityDevice.class);
        ElecCoreRegistrar.GRIDHANDLERS.register((IStructureWorldEventHandler) (electricityGridHandler = new ElectricityGridHandler()));
        loadTimer.endPhase(event);
    }

    public static <B extends Block> RegistryObject<Item> fromBlock(RegistryObject<B> block) {
        Preconditions.checkNotNull(block.getId().getPath());
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(Preconditions.checkNotNull(block.get()), createStandardProperties()));
    }

    public static Item.Properties createStandardProperties() {
        return new Item.Properties().group(itemGroup);
    }

}
