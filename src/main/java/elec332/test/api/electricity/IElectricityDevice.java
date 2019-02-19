package elec332.test.api.electricity;

import java.util.Set;

/**
 * Created by Elec332 on 20-1-2018.
 */
public interface IElectricityDevice {

    public Set<IEnergyObject> getInternalComponents();

}
