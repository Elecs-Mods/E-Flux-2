package elec332.test.simulation;

import elec332.core.api.APIHandlerInject;
import elec332.test.api.electricity.component.ICircuitElementFactory;
import elec332.test.wire.AbstractWire;

/**
 * Created by Elec332 on 19-2-2019
 */
public class ElementRegister {

    @APIHandlerInject
    private static void register(ICircuitElementFactory factory) {
        factory.registerComponentWrapper(IElectricityTransformer.class, TransformerElement.class, TransformerElement::new);
        factory.registerComponentWrapper(AbstractWire.class, WireElement.class, WireElement::new);
    }

}
