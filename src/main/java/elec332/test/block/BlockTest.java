package elec332.test.block;

import elec332.core.api.client.IIconRegistrar;
import elec332.core.api.client.model.IElecModelBakery;
import elec332.core.api.client.model.IElecQuadBakery;
import elec332.core.api.client.model.IElecTemplateBakery;
import elec332.core.client.model.loading.INoJsonBlock;
import elec332.test.TestMod;
import elec332.test.wire.ground.tile.SubTileWire;
import elec332.test.wire.terminal.tile.SubTileTerminal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.EnumBlockRenderType;

/**
 * Created by Elec332 on 1-2-2019
 */
public class BlockTest extends BlockSubTile implements INoJsonBlock {

    public BlockTest() {
        super(Properties.create(Material.CACTUS), SubTileWire.class, SubTileTerminal.class);
    }

    @Override
    public void registerTextures(IIconRegistrar iconRegistrar) {

    }

    @Override
    public void registerModels(IElecQuadBakery quadBakery, IElecModelBakery modelBakery, IElecTemplateBakery templateBakery) {

    }

    @Override
    public IBakedModel getBlockModel(IBlockState state) {
        return TestMod.model;
    }

}
