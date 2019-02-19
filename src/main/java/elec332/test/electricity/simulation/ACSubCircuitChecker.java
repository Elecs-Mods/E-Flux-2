package elec332.test.electricity.simulation;

import elec332.test.api.electricity.component.CircuitElement;
import elec332.test.api.electricity.component.EnumElectricityType;
import elec332.test.api.electricity.component.ICircuitPart;
import elec332.test.api.electricity.component.ISubCircuitChecker;
import elec332.test.api.util.BreakReason;
import elec332.test.simulation.voltsource.VoltageElement;

/**
 * Created by Elec332 on 18-2-2019
 */
public class ACSubCircuitChecker implements ISubCircuitChecker {

    @Override
    public boolean isSubCircuitValid(ICircuitPart subCircuit) {
        CircuitElement ve = null;
        for (CircuitElement ce : subCircuit.getElementPosts().keys()) {
            EnumElectricityType type = ce.getEnergyObject().getEnergyType();
            if (ce instanceof VoltageElement && type == EnumElectricityType.AC) {
                if (ve == null) {
                    ve = ce;
                } else if (ve != ce) {
                    ((VoltageElement) ce).getEnergyObject().breakMachine(BreakReason.OVERPOWERED_GENERATOR);
                    return false;
                }
            }
        }
        return true;
    }

}
