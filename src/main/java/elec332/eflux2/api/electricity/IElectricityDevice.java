package elec332.eflux2.api.electricity;

import java.util.Set;

/**
 * Created by Elec332 on 20-1-2018.
 */
public interface IElectricityDevice {

    public Set<IEnergyObject> getInternalComponents();

}
