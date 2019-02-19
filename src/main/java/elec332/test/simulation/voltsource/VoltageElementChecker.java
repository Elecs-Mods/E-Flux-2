package elec332.test.simulation.voltsource;

import elec332.test.api.electricity.IEnergySource;
import elec332.test.api.electricity.component.IElementChecker;

import java.util.Collection;

/**
 * Created by Elec332 on 18-2-2019
 */
public class VoltageElementChecker implements IElementChecker<VoltageElement> {

    @Override
    public boolean elementsValid(Collection<VoltageElement> elements) {
        for (VoltageElement ve : elements) {
            IEnergySource s = ve.getEnergyObject();
            //if (s.getMaxRP() * s.getCurrentAverageEF() < Math.abs(ve.getPower())) {
            //if (Math.abs(ve.getCurrent()) > s.getMaxRP() * 2 || Math.abs(ve.getVoltageDiff()) < (Math.abs(s.getCurrentAverageEF()) / 2d)){
            if (s.getMaxRP() * s.getCurrentAverageEF() < Math.abs(ve.getPower()) && (Math.abs(ve.getCurrent()) > s.getMaxRP() * 2 || Math.abs(ve.getVoltageDiff()) < (Math.abs(s.getCurrentAverageEF()) / 2d))) {
                //System.out.println(" VEC: " +ve.getPower() + ve);
                //System.out.println(" VEC: " +ve.getVoltageDiff());
                //System.out.println(" VEC: " +ve.getCurrent());
                //s.breakMachine(BreakReason.OVERPOWERED_GENERATOR);
                //return false;
            }
        }
        return true;
    }

}
