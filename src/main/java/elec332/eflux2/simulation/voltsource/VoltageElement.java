package elec332.eflux2.simulation.voltsource;

import com.google.common.base.Preconditions;
import elec332.eflux2.api.electricity.IEnergySource;
import elec332.eflux2.api.electricity.component.CircuitElement;
import elec332.eflux2.api.electricity.component.EnumElectricityType;

/**
 * Created by Elec332 on 12-11-2017.
 */
public class VoltageElement extends CircuitElement<IEnergySource> {

    public VoltageElement(IEnergySource energySource) {
        super(energySource);
    }

    @Override
    public void stamp() {
        getCircuit().stampVoltageSource(nodes[0], nodes[1], voltSource);
        //getCircuit().stampVoltageSource(nodes[0], nodes[1], voltSource, getVoltage());
    }

    @Override
    public void doStep() {
        double maxPow = energyTile.getMaxRP() * getVoltage();
        double voltage = getVoltage();
        double currV = Math.abs(getVoltageDiff());
        double currC = Math.abs(getCurrent());
        double currentPow = currV * currC;

        currentPow *= 1; //Round to 4 digits to prevent voltage diff issues
        currentPow = Math.round(currentPow);
        currentPow /= 1;

        if (currV > 0 && currC > 0) {
            voltage = Math.min(currV * 0.75 + currV * (maxPow / currentPow * 0.25), voltage);
        }
        getCircuit().updateVoltageSource(nodes[0], nodes[1], voltSource, voltage);
    }

    private double getVoltage() {
        return energyTile.getCurrentAverageEF();
    }

    @Override
    public EnumElectricityType getProvidedPowerType(int post) {
        return Preconditions.checkNotNull(energyTile.getEnergyType());
    }

    @Override
    public double getMaxProvidedPower(int post) {
        return energyTile.getMaxRP() * getVoltage();
    }

    @Override
    public int getVoltageSourceCount() {
        return 1;
    }

    @Override
    public void apply() {
        energyTile.setData(getVoltageDiff(), getCurrent());
    }

}
