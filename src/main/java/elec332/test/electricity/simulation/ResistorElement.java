package elec332.test.electricity.simulation;

import elec332.test.api.electricity.IEnergyReceiver;
import elec332.test.api.electricity.component.AbstractResistorElement;

import javax.annotation.Nullable;

/**
 * Created by Elec332 on 14-11-2017.
 */
public class ResistorElement extends AbstractResistorElement<IEnergyReceiver> {

    private double resistance;

    public ResistorElement(IEnergyReceiver receiver) {
        super(receiver);
        this.resistance = receiver.getResistance();
    }

    @Override
    public boolean isPolarityAgnostic() {
        return false;
    }

    @Override
    public double getResistance() {
        return resistance;
    }

    @Nullable
    @Override
    protected IEnergyReceiver getReceiver() {
        return energyTile;
    }

    @Override
    public void apply() {
        energyTile.receivePower(getVoltageDiff(), current);
    }

}
