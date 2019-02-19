package elec332.test.api.electricity;

import elec332.test.api.util.IBreakableMachine;

/**
 * Created by Elec332 on 16-4-2015.
 */
public interface IEnergySource extends IEnergyObject, IBreakableMachine {

    public float getVariance();

    //Volts
    public int getCurrentAverageEF();

    //Amps
    public float getMaxRP();

    default public void setData(double ef, double rp) {
    }

}
