package elec332.eflux2.api.electricity.component.optimization;

import com.google.common.collect.Multimap;
import elec332.eflux2.api.electricity.component.CircuitElement;
import elec332.eflux2.api.util.ConnectionPoint;

import java.util.List;

/**
 * Created by Elec332 on 14-11-2017.
 */
public interface ICircuitCompressor {

    Multimap<CompressedCircuitElement<?>, CircuitElement<?>> compress(List<CircuitElement<?>> elements, Multimap<ConnectionPoint, CircuitElement<?>> map2);

}
