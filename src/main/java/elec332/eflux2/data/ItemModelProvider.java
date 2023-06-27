package elec332.eflux2.data;

import elec332.core.data.AbstractItemModelProvider;
import elec332.eflux2.EFlux2;
import elec332.eflux2.modules.test.TestModule;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

/**
 * Created by Elec332 on 6-8-2020
 */
public class ItemModelProvider extends AbstractItemModelProvider {

    public ItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, EFlux2.MODID, existingFileHelper);
    }

    @Override
    protected void registerItemModels() {
        parentedModel(TestModule.TRANSFORMER_BLOCK);
        parentedModel(TestModule.GENERATOR_BLOCK);
        parentedModel(TestModule.RECEIVER_BLOCK);
    }

}
