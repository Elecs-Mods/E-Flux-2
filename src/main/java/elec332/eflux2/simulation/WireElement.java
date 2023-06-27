package elec332.eflux2.simulation;

import elec332.eflux2.api.electricity.IEnergyReceiver;
import elec332.eflux2.api.electricity.component.AbstractResistorElement;
import elec332.eflux2.modules.wires.wire.AbstractWire;

import javax.annotation.Nullable;

/**
 * Created by Elec332 on 13-11-2017.
 */
public class WireElement extends AbstractResistorElement<AbstractWire> {

    public WireElement(AbstractWire energyTile) {
        super(energyTile);
        resistance = energyTile.getResistance();
    }

    private final double resistance;

    @Override
    public double getResistance() {
        return resistance;
    }

    @Nullable
    @Override
    protected IEnergyReceiver getReceiver() {
        return null;
    }

    @Override
    public void apply() {
        energyTile.setPowerTest(getVoltageDiff(), getCurrent());
    }

    @Override
    public boolean isWire() {
        return super.isWire();
    }

	/*   Tests, don't remove

	@Override
	public void stamp() {
		getCircuit().stampVoltageSource(nodes[0], nodes[1], voltSource, 0);
		getCircuit().stampResistor(nodes[0], nodes[1], 1);
	}

	@Override
	public int getVoltageSourceCount() {
		return 1;
	}

	@Override
	protected double getPower() {
		return 0;
	}

	@Override
	protected double getVoltageDiff() {
		return volts[0];
	}

	@Override
	public boolean isWire() {
		return true;
	}
	*/

}
