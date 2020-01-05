package elec332.eflux2.register;

import com.google.common.base.Preconditions;
import elec332.core.api.registration.IItemRegister;
import elec332.eflux2.EFlux2;
import elec332.eflux2.item.ItemGroundTerminal;
import elec332.eflux2.item.ItemGroundWire;
import elec332.eflux2.item.ItemOverheadWire;
import elec332.eflux2.item.ItemWireManager;
import elec332.eflux2.util.EFlux2ResourceLocation;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

import static elec332.eflux2.register.EFlux2BlockRegister.*;

/**
 * Created by Elec332 on 1-1-2020
 */
public class EFlux2ItemRegister implements IItemRegister {

    public static Item wire;
    public static Item terminal;
    public static Item overheadWire;
    public static Item wireManager;

    @Override
    public void preRegister() {
        wire = new ItemGroundWire(new Item.Properties().group(EFlux2.itemGroup)).setRegistryName(new EFlux2ResourceLocation("wire"));
        terminal = new ItemGroundTerminal(new Item.Properties().group(EFlux2.itemGroup)).setRegistryName(new EFlux2ResourceLocation("terminal"));
        overheadWire = new ItemOverheadWire(new Item.Properties().group(EFlux2.itemGroup)).setRegistryName(new EFlux2ResourceLocation("overhead_wire"));
        wireManager = new ItemWireManager(new Item.Properties().group(EFlux2.itemGroup)).setRegistryName(new EFlux2ResourceLocation("wire_manager"));
    }

    @Override
    public void register(IForgeRegistry<Item> registry) {
        registry.registerAll(wire, terminal);
        registry.registerAll(overheadWire, wireManager);

        registerItemBlock(testReceiver, registry);
        registerItemBlock(generator, registry);
        registerItemBlock(transformer, registry);
    }

    private void registerItemBlock(Block block, IForgeRegistry<Item> reg) {
        reg.register(new BlockItem(block, new Item.Properties().group(EFlux2.itemGroup)).setRegistryName(Preconditions.checkNotNull(block.getRegistryName())));
    }

}
