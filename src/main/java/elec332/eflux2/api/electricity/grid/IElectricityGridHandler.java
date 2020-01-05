package elec332.eflux2.api.electricity.grid;

import elec332.eflux2.api.electricity.IEnergyObject;

/**
 * Created by Elec332 on 19-2-2019
 * <p>
 * The entire grid system was intended to stay internal,
 * but it was opened up a little bit to allow for some flexibility
 */
public interface IElectricityGridHandler {

    public void addObjectUnsafe(IEnergyObject wire);

    public void removeObjectUnsafe(IEnergyObject wire);

}
