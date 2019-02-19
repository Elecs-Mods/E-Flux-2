package elec332.test.api.electricity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Set;

/**
 * Created by Elec332 on 20-1-2018.
 */
public class DefaultElectricityDevice implements IElectricityDevice {

    public DefaultElectricityDevice(IEnergyObject... others) {
        Preconditions.checkState(others.length > 0 && others[0] != null);
        this.components = ImmutableSet.copyOf(Lists.newArrayList(others));
    }

    public DefaultElectricityDevice(Collection<IEnergyObject> objects) {
        Preconditions.checkState(!objects.isEmpty());
        this.components = ImmutableSet.copyOf(objects);
    }

    private final Set<IEnergyObject> components;

    @Override
    public Set<IEnergyObject> getInternalComponents() {
        return components;
    }

}
