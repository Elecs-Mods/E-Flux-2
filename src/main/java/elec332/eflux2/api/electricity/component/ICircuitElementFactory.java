package elec332.eflux2.api.electricity.component;

import elec332.eflux2.api.electricity.IElectricityDevice;
import elec332.eflux2.api.electricity.IEnergyObject;
import elec332.eflux2.api.electricity.component.optimization.ICircuitCompressor;
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
    @SuppressWarnings("ConstantConditions")
    default Collection<CircuitElement<?>> wrapComponent(LazyOptional<IElectricityDevice> component) {
        if (!component.isPresent()) {
            return Collections.emptySet();
        }
        return wrapComponent(component.orElse(null));
    }

    @Nonnull
    Collection<CircuitElement<?>> wrapComponent(IElectricityDevice component);

    @Nonnull
    Set<CircuitElement<?>> wrapComponent(IEnergyObject component);

    boolean isPassiveConnector(IElectricityDevice device);

    <O extends IEnergyObject, E extends CircuitElement<?>> void registerComponentWrapper(Class<O> clazz, BiConsumer<O, Collection<CircuitElement<?>>> wrapper);

    <O extends IEnergyObject, E extends CircuitElement<O>> void registerComponentWrapper(Class<O> clazz, Class<E> eClass, Function<O, E> wrapper, IElementChecker<E> checker);

    default <O extends IEnergyObject, E extends CircuitElement<O>> void registerComponentWrapper(Class<O> clazz, Class<E> eClass, Function<O, E> wrapper) {
        registerComponentWrapper(clazz, eClass, wrapper, elements -> true);
    }

    void registerCircuitOptimizer(ICircuitCompressor optimizer);

    void registerSubCircuitChecker(ISubCircuitChecker circuitChecker);

    @Nonnull
    Collection<ICircuitCompressor> getCircuitOptimizers();

    @Nonnull
    Collection<Pair<Class<?>, IElementChecker<?>>> getElementCheckers();

    @Nonnull
    Collection<ISubCircuitChecker> getCircuitCheckers();

}
