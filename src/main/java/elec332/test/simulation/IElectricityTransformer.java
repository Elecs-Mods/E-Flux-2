package elec332.test.simulation;

import elec332.test.api.electricity.IEnergyObject;

/**
 * Created by Elec332 on 5-1-2018.
 */
public interface IElectricityTransformer extends IEnergyObject {

    public double getInductance();

    public double getRatio();

    @Override
    default public int getPosts() {
        return 4;
    }

}
