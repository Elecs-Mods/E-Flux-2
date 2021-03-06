package elec332.eflux2.electricity.simulation;

import elec332.core.api.APIHandlerInject;
import elec332.eflux2.api.electricity.IEnergyReceiver;
import elec332.eflux2.api.electricity.IEnergySource;
import elec332.eflux2.api.electricity.component.ICircuitElementFactory;
import elec332.eflux2.electricity.simulation.optimization.ResistorOptimizer;
import elec332.eflux2.simulation.voltsource.VoltageElement;
import elec332.eflux2.simulation.voltsource.VoltageElementChecker;

/**
 * Created by Elec332 on 19-2-2019
 */
public class APIElementRegister {

    @APIHandlerInject
    private static void register(ICircuitElementFactory factory) {
        factory.registerComponentWrapper(IEnergyReceiver.class, ResistorElement.class, ResistorElement::new);
        factory.registerComponentWrapper(IEnergySource.class, VoltageElement.class, VoltageElement::new, new VoltageElementChecker());
        factory.registerCircuitOptimizer(new ResistorOptimizer());
        factory.registerSubCircuitChecker(new VoltageSourceSubCircuitChecker());
        factory.registerSubCircuitChecker(new ACSubCircuitChecker());
    }

}
