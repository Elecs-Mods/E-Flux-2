package elec332.eflux2.simulation;

import elec332.eflux2.api.electricity.IEnergyObject;

/**
 * Created by Elec332 on 5-1-2018.
 */
public interface IElectricityTransformer extends IEnergyObject {

    double getInductance();

    double getRatio();

    @Override
    default int getPosts() {
        return 4;
    }

}
