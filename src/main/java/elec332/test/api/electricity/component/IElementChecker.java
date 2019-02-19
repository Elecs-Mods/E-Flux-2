package elec332.test.api.electricity.component;

import java.util.Collection;

/**
 * Created by Elec332 on 16-11-2017.
 */
public interface IElementChecker<T extends CircuitElement> {

    public boolean elementsValid(Collection<T> elements);

}
