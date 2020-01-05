package elec332.eflux2.register;

import elec332.core.api.registration.IBlockRegister;
import elec332.core.block.BlockSubTile;
import elec332.eflux2.block.BlockGenerator;
import elec332.eflux2.block.BlockTileEntity;
import elec332.eflux2.block.BlockWire;
import elec332.eflux2.tile.TileTestReceiver;
import elec332.eflux2.tile.TileTestTransformer;
import elec332.eflux2.util.EFlux2ResourceLocation;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Created by Elec332 on 1-1-2020
 */
public class EFlux2BlockRegister implements IBlockRegister {

    public static BlockSubTile wire;
    public static Block testReceiver, generator, transformer;

    @Override
    public void preRegister() {
        wire = (BlockSubTile) new BlockWire().setRegistryName(new EFlux2ResourceLocation("wire"));
        testReceiver = new BlockTileEntity(Block.Properties.create(Material.CACTUS), TileTestReceiver::new).setRegistryName(new EFlux2ResourceLocation("testreceiver"));
        generator = new BlockGenerator(Block.Properties.create(Material.CACTUS)).setRegistryName(new EFlux2ResourceLocation("testpowertgen"));
        transformer = new BlockTileEntity(Block.Properties.create(Material.ANVIL), TileTestTransformer::new).setRegistryName(new EFlux2ResourceLocation("testtransformer"));
    }

    @Override
    public void register(IForgeRegistry<Block> registry) {
        registry.register(wire);
        registry.registerAll(testReceiver, generator, transformer);
    }

}
