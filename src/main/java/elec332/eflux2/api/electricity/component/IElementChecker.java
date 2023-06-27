package elec332.eflux2.api.electricity.component;

import java.util.Collection;

/**
 * Created by Elec332 on 16-11-2017.
 */
public interface IElementChecker<T extends CircuitElement<?>> {

    boolean elementsValid(Collection<T> elements);

}
