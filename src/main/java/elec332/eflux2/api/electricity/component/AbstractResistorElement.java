package elec332.eflux2.api.electricity.component;

import elec332.eflux2.api.electricity.IEnergyObject;
import elec332.eflux2.api.electricity.IEnergyReceiver;

import javax.annotation.Nullable;

/**
 * Created by Elec332 on 12-11-2017.
 */
public abstract class AbstractResistorElement<T extends IEnergyObject> extends CircuitElement<T> {

    public AbstractResistorElement(T receiver) {
        super(receiver);
    }

    public boolean combineData;

    public abstract double getResistance();

    public boolean isPolarityAgnostic() {
        return true;
    }

    @Nullable
    protected abstract IEnergyReceiver getReceiver();

    @Override
    protected void calculateCurrent() {
        current = getVoltageDiff() / getResistance();
    }

    @Override
    public void stamp() {
        getCircuit().stampResistor(nodes[0], nodes[1], getResistance());
    }

    @Override
    public abstract void apply();

    @Override
    public double getVoltageDiff() {
        double ret = super.getVoltageDiff();
        if (isPolarityAgnostic()) {
            return Math.abs(ret);
        }
        return ret;
    }

    @Override
    public String toString() {
        return "ResistorElement: R=" + getResistance();
    }

}
