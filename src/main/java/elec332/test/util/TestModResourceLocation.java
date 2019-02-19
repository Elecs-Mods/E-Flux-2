package elec332.test.util;

import elec332.core.ElecCore;
import net.minecraft.util.ResourceLocation;

/**
 * Created by Elec332 on 6-2-2019
 */
public class TestModResourceLocation extends ResourceLocation {

    public TestModResourceLocation(String pathIn) {
        super(ElecCore.MODID, pathIn);
    }

}
