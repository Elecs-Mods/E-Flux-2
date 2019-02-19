package elec332.test.electricity.simulation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import elec332.test.api.electricity.IEnergyObject;
import elec332.test.api.electricity.component.CircuitElement;
import elec332.test.api.electricity.component.EnumElectricityType;
import elec332.test.api.electricity.component.ICircuitPart;
import elec332.test.api.electricity.component.ISubCircuitChecker;
import elec332.test.api.util.BreakReason;
import elec332.test.api.util.IBreakableMachine;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Elec332 on 18-2-2019
 */
public class VoltageSourceSubCircuitChecker implements ISubCircuitChecker {

    @Override
    public boolean isSubCircuitValid(ICircuitPart subCircuit) {
        Multimap<EnumElectricityType, CircuitElement> types = HashMultimap.create();
        Map<EnumElectricityType, Double> pow = Maps.newEnumMap(EnumElectricityType.class);
        subCircuit.getElementPosts().forEach((ce, post) -> {
            EnumElectricityType p = ce.getProvidedPowerType(post);
            if (p != null && Math.abs(ce.getPower()) > 0) {
                types.put(p, ce);
                pow.put(p, pow.getOrDefault(p, 0d) + Math.abs(ce.getMaxProvidedPower(post)) / 2);
            }
        });
        if (pow.isEmpty() || pow.keySet().size() == 1) {
            return true;
        }
        double ac = pow.get(EnumElectricityType.AC);
        double dc = pow.get(EnumElectricityType.DC);
        Collection<CircuitElement> biem;
        EnumElectricityType t;
        if (ac * 3 >= dc) {
            biem = types.get(EnumElectricityType.DC);
            t = EnumElectricityType.AC;
        } else {
            biem = types.get(EnumElectricityType.AC);
            t = EnumElectricityType.DC;
        }
        types.values().forEach(ce -> ce.setCurrentPowerType(t));
        biem.forEach(ce -> {
            IEnergyObject eo = ce.getEnergyObject();
            if (!(eo instanceof IBreakableMachine)) {
                throw new IllegalArgumentException(eo + " isn't instanceof IBreakableMachine!");
            }
            ((IBreakableMachine) eo).breakMachine(BreakReason.OVERPOWERED_GENERATOR);
        });
        return false;
    }

}
