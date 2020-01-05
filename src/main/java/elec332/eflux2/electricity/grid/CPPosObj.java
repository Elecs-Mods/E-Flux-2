package elec332.eflux2.electricity.grid;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import elec332.eflux2.api.electricity.component.CircuitElement;
import elec332.eflux2.api.util.ConnectionPoint;

/**
 * Created by Elec332 on 18-11-2017.
 */
class CPPosObj {

    Multimap<ConnectionPoint, CircuitElement> connections = HashMultimap.create();

}
