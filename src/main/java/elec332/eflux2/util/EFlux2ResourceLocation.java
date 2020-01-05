package elec332.eflux2.util;

import elec332.eflux2.EFlux2;
import net.minecraft.util.ResourceLocation;

/**
 * Created by Elec332 on 6-2-2019
 */
public class EFlux2ResourceLocation extends ResourceLocation {

    public EFlux2ResourceLocation(String pathIn) {
        super(EFlux2.MODID, pathIn);
    }

}
