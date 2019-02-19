package elec332.test.api.electricity.component;

import com.google.common.collect.Multimap;

/**
 * Created by Elec332 on 17-2-2019
 */
public interface ICircuitPart {

    public Multimap<CircuitElement, Integer> getElementPosts();

}
