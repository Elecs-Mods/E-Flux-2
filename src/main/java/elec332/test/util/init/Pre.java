package elec332.test.util.init;

import elec332.core.api.discovery.AnnotationDataProcessor;
import elec332.core.api.discovery.IAnnotationDataHandler;
import elec332.core.api.discovery.IAnnotationDataProcessor;
import elec332.test.TestMod;
import net.minecraftforge.fml.ModLoadingStage;

/**
 * Created by Elec332 on 31-1-2019
 */
@AnnotationDataProcessor({ModLoadingStage.COMMON_SETUP, ModLoadingStage.ENQUEUE_IMC, ModLoadingStage.PROCESS_IMC})
public class Pre implements IAnnotationDataProcessor {

    private static TestMod mod = new TestMod();

    @Override
    public void processASMData(IAnnotationDataHandler annotationData, ModLoadingStage state) {
        if (state == ModLoadingStage.COMMON_SETUP) {
            mod.preInit();
        }
        if (state == ModLoadingStage.ENQUEUE_IMC) {
            mod.init();
        }
        if (state == ModLoadingStage.PROCESS_IMC) {
            mod.postInit();
        }
    }

}
