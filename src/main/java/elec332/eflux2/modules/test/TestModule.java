package elec332.eflux2.modules.test;

import elec332.core.api.module.ElecModule;
import elec332.eflux2.EFlux2;
import elec332.eflux2.block.BlockTileEntity;
import elec332.eflux2.modules.test.block.BlockGenerator;
import elec332.eflux2.modules.test.tile.TileTestReceiver;
import elec332.eflux2.modules.test.tile.TileTestTransformer;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;

/**
 * Created by Elec332 on 4-8-2020
 */
@ElecModule(owner = EFlux2.MODID, name = "Test", alwaysEnabled = true)
public class TestModule {

    public static final RegistryObject<Block> RECEIVER_BLOCK = EFlux2.BLOCKS.register("receiver_test", () -> new BlockTileEntity(Block.Properties.create(Material.CACTUS), TileTestReceiver::new));
    public static final RegistryObject<Block> GENERATOR_BLOCK = EFlux2.BLOCKS.register("generator_test", () -> new BlockGenerator(Block.Properties.create(Material.CACTUS)));
    public static final RegistryObject<Block> TRANSFORMER_BLOCK = EFlux2.BLOCKS.register("transformer_test", () -> new BlockTileEntity(Block.Properties.create(Material.ANVIL), TileTestTransformer::new));

    public static final RegistryObject<Item> RECEIVER_ITEM = EFlux2.fromBlock(RECEIVER_BLOCK);
    public static final RegistryObject<Item> GENERATOR_ITEM = EFlux2.fromBlock(GENERATOR_BLOCK);
    public static final RegistryObject<Item> TRANSFORMER_ITEM = EFlux2.fromBlock(TRANSFORMER_BLOCK);

}
