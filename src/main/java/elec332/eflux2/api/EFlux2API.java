package elec332.eflux2.api;

import elec332.eflux2.api.electricity.IElectricityDevice;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/**
 * Created by Elec332 on 6-2-2019
 */
public class EFlux2API {

    @CapabilityInject(IElectricityDevice.class)
    public static Capability<IElectricityDevice> ELECTRICITY_CAP;

}
