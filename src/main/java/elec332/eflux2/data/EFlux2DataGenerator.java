package elec332.eflux2.data;

import elec332.core.api.registration.DataGenerator;
import elec332.core.data.AbstractDataGenerator;

/**
 * Created by Elec332 on 6-8-2020
 */
@DataGenerator
public class EFlux2DataGenerator extends AbstractDataGenerator {

    @Override
    public void registerDataProviders(DataRegistry registry) {
        registry.registerChecked(ItemModelProvider::new);
        registry.registerChecked(BlockStateProvider::new);
    }

}
