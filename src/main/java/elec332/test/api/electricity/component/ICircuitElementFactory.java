package elec332.test.api.electricity.component;

import elec332.test.api.electricity.IElectricityDevice;
import elec332.test.api.electricity.IEnergyObject;
import elec332.test.api.electricity.component.optimization.ICircuitCompressor;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Created by Elec332 on 19-2-2019
 */
public interface ICircuitElementFactory {

    @Nonnull
    @SuppressWarnings("all")
    default public Collection<CircuitElement<?>> wrapComponent(LazyOptional<IElectricityDevice> component) {
        if (!component.isPresent()) {
            return Collections.emptySet();
        }
        return wrapComponent(component.orElse(null));
    }

    @Nonnull
    public Collection<CircuitElement<?>> wrapComponent(IElectricityDevice component);

    @Nonnull
    public Set<CircuitElement<?>> wrapComponent(IEnergyObject component);

    public boolean isPassiveConnector(IElectricityDevice device);

    public <O extends IEnergyObject, E extends CircuitElement<?>> void registerComponentWrapper(Class<O> clazz, BiConsumer<O, Collection<CircuitElement<?>>> wrapper);

    public <O extends IEnergyObject, E extends CircuitElement<O>> void registerComponentWrapper(Class<O> clazz, Class<E> eClass, Function<O, E> wrapper, IElementChecker<E> checker);

    default public <O extends IEnergyObject, E extends CircuitElement<O>> void registerComponentWrapper(Class<O> clazz, Class<E> eClass, Function<O, E> wrapper) {
        registerComponentWrapper(clazz, eClass, wrapper, elements -> true);
    }

    public void registerCircuitOptimizer(ICircuitCompressor optimizer);

    public void registerSubCircuitChecker(ISubCircuitChecker circuitChecker);

    @Nonnull
    public Collection<ICircuitCompressor> getCircuitOptimizers();

    @Nonnull
    public Collection<Pair<Class, IElementChecker>> getElementCheckers();

    @Nonnull
    public Collection<ISubCircuitChecker> getCircuitCheckers();

}
