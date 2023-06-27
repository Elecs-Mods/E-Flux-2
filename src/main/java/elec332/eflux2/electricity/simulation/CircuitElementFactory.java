package elec332.eflux2.electricity.simulation;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import elec332.core.api.APIHandlerInject;
import elec332.core.api.IAPIHandler;
import elec332.core.api.annotations.StaticLoad;
import elec332.eflux2.api.electricity.IElectricityDevice;
import elec332.eflux2.api.electricity.IEnergyObject;
import elec332.eflux2.api.electricity.component.CircuitElement;
import elec332.eflux2.api.electricity.component.ICircuitElementFactory;
import elec332.eflux2.api.electricity.component.IElementChecker;
import elec332.eflux2.api.electricity.component.ISubCircuitChecker;
import elec332.eflux2.api.electricity.component.optimization.ICircuitCompressor;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Created by Elec332 on 12-11-2017.
 */
@StaticLoad
public enum CircuitElementFactory implements ICircuitElementFactory {

    INSTANCE;

    CircuitElementFactory() {
        this.cache = Maps.newHashMap();
        this.elementCheckers = Maps.newHashMap();
        this.optimizers = Sets.newHashSet();
        this.optimizers_ = Collections.unmodifiableSet(optimizers);
        this.circuitCheckers = Sets.newHashSet();
        this.circuitCheckers_ = Collections.unmodifiableSet(circuitCheckers);
    }

    private final Map<Class<?>, BiConsumer<IEnergyObject, Collection<CircuitElement<?>>>> cache;
    private final Set<ICircuitCompressor> optimizers, optimizers_;
    private final Map<Integer, Pair<Class<?>, IElementChecker<?>>> elementCheckers;
    private final Set<ISubCircuitChecker> circuitCheckers, circuitCheckers_;
    private int hc = 1000;

    @Nonnull
    @Override
    public Collection<CircuitElement<?>> wrapComponent(IElectricityDevice component) {
        if (component == null) {
            return Collections.emptySet();
        }
        Set<IEnergyObject> objects = component instanceof IEnergyObject ? Sets.newHashSet((IEnergyObject) component) : component.getInternalComponents();
        Set<CircuitElement<?>> ret = Sets.newHashSet();
        objects.stream().filter(eo -> !eo.isPassiveConnector()).forEach(object -> ret.addAll(wrapComponent(object)));
        return ret;
    }

    @Nonnull
    @Override
    public Set<CircuitElement<?>> wrapComponent(IEnergyObject component) {
        List<BiConsumer<IEnergyObject, Collection<CircuitElement<?>>>> wrappers = Lists.newArrayList();
        cache.forEach((type, wrapper) -> {
            if (type.isAssignableFrom(component.getClass())) {
                wrappers.add(wrapper);
            }
        });
        int i = wrappers.size();
        if (i == 1) {
            Set<CircuitElement<?>> ret = Sets.newHashSet();
            wrappers.get(0).accept(component, ret);
            Preconditions.checkArgument(!ret.isEmpty());
            return ret;
        } else if (i == 0) {
            throw new IllegalArgumentException(component.toString());
        } else {
            throw new IllegalStateException(component.toString());
        }
    }

    @Override
    public boolean isPassiveConnector(IElectricityDevice device) {
        for (IEnergyObject obj : device.getInternalComponents()) {
            if (!obj.isPassiveConnector()) {
                return false;
            }
        }
        return true;
    }

    @Override
    @SuppressWarnings("all")
    public <O extends IEnergyObject, E extends CircuitElement<?>> void registerComponentWrapper(Class<O> clazz, BiConsumer<O, Collection<CircuitElement<?>>> wrapper) {
        cache.put(clazz, (BiConsumer<IEnergyObject, Collection<CircuitElement<?>>>) wrapper);
    }

    @Override
    @SuppressWarnings("all")
    public <O extends IEnergyObject, E extends CircuitElement<O>> void registerComponentWrapper(Class<O> clazz, Class<E> eClass, Function<O, E> wrapper, IElementChecker<E> checker) {
        cache.put(clazz, (energyObject, circuitElements) -> circuitElements.add(wrapper.apply((O) energyObject)));
        elementCheckers.put(hc++, Pair.of(eClass, checker));
    }

    @Override
    public void registerCircuitOptimizer(ICircuitCompressor optimizer) {
        optimizers.add(optimizer);
    }

    @Override
    public void registerSubCircuitChecker(ISubCircuitChecker circuitChecker) {
        circuitCheckers.add(circuitChecker);
    }

    @Nonnull
    @Override
    public Collection<ICircuitCompressor> getCircuitOptimizers() {
        return optimizers_;
    }

    @Nonnull
    @Override
    public Collection<Pair<Class<?>, IElementChecker<?>>> getElementCheckers() {
        return elementCheckers.values();
    }

    @Nonnull
    @Override
    public Collection<ISubCircuitChecker> getCircuitCheckers() {
        return circuitCheckers_;
    }

    @APIHandlerInject
    private void inject(IAPIHandler apiHandler) {
        apiHandler.inject(this, ICircuitElementFactory.class);
    }

}
