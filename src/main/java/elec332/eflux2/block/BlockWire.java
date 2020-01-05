package elec332.eflux2.block;

import elec332.core.api.client.IIconRegistrar;
import elec332.core.api.client.model.IElecModelBakery;
import elec332.core.api.client.model.IElecQuadBakery;
import elec332.core.api.client.model.IElecTemplateBakery;
import elec332.core.block.BlockSubTile;
import elec332.core.client.model.loading.INoJsonBlock;
import elec332.eflux2.client.wire.WireModelCache;
import elec332.eflux2.wire.ground.tile.SubTileWire;
import elec332.eflux2.wire.terminal.tile.SubTileTerminal;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Created by Elec332 on 1-2-2019
 */
public class BlockWire extends BlockSubTile implements INoJsonBlock {

    @SuppressWarnings("all")
    public BlockWire() {
        super(Properties.create(Material.CACTUS), SubTileWire.class, SubTileTerminal.class);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerTextures(IIconRegistrar iconRegistrar) {
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerModels(IElecQuadBakery quadBakery, IElecModelBakery modelBakery, IElecTemplateBakery templateBakery) {
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IBakedModel getBlockModel(BlockState state) {
        return WireModelCache.instance;
    }

}
