package elec332.eflux2.modules.core;

import elec332.core.api.module.ElecModule;
import elec332.core.inventory.window.WindowManager;
import elec332.eflux2.EFlux2;
import elec332.eflux2.modules.core.item.ItemWireManager;
import elec332.eflux2.modules.core.window.CoreModuleWindowHandler;
import elec332.eflux2.util.EFlux2ResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Created by Elec332 on 4-8-2020
 */
@ElecModule(owner = EFlux2.MODID, name = "Core", alwaysEnabled = true)
public class CoreModule {

    public static final RegistryObject<Item> WIRE_MANAGER_ITEM = EFlux2.ITEMS.register("wire_manager", () -> new ItemWireManager(EFlux2.createStandardProperties()));

    @ElecModule.EventHandler
    public void preInit(FMLCommonSetupEvent event) {
        WindowManager.INSTANCE.register(CoreModuleWindowHandler.INSTANCE, new EFlux2ResourceLocation("window_handler"));
    }

}
