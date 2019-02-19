package elec332.test.electricity.grid;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import elec332.test.api.electricity.component.CircuitElement;
import elec332.test.api.util.ConnectionPoint;

/**
 * Created by Elec332 on 18-11-2017.
 */
class CPPosObj {

    Multimap<ConnectionPoint, CircuitElement> connections = HashMultimap.create();

}
