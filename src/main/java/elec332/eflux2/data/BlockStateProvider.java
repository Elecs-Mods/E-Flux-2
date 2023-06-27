package elec332.eflux2.data;

import elec332.core.data.AbstractBlockStateProvider;
import elec332.eflux2.EFlux2;
import elec332.eflux2.modules.test.TestModule;
import elec332.eflux2.util.EFlux2ResourceLocation;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

/**
 * Created by Elec332 on 6-8-2020
 */
public class BlockStateProvider extends AbstractBlockStateProvider {

    public BlockStateProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, EFlux2.MODID, exFileHelper);
    }

    private static final ResourceLocation DEFAULT_TOP = new EFlux2ResourceLocation("block/normal");
    private static final ResourceLocation DEFAULT_BOTTOM = new EFlux2ResourceLocation("block/normal");
    private static final ResourceLocation DEFAULT_SIDE = new EFlux2ResourceLocation("block/default_back");

    @Override
    protected void registerBlockStatesAndModels() {
        simpleFront(TestModule.RECEIVER_BLOCK, new EFlux2ResourceLocation("block/black"));
        simpleFront(TestModule.TRANSFORMER_BLOCK, new EFlux2ResourceLocation("block/black"));
        simpleFront(TestModule.GENERATOR_BLOCK, new EFlux2ResourceLocation("block/coalgeneratorfront"));
    }

    @Override
    protected ResourceLocation getDefaultTopLocation() {
        return DEFAULT_TOP;
    }

    @Override
    protected ResourceLocation getDefaultBottomLocation() {
        return DEFAULT_BOTTOM;
    }

    @Override
    protected ResourceLocation getDefaultSideLocation() {
        return DEFAULT_SIDE;
    }

}
