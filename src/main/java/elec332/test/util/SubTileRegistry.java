package elec332.test.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import elec332.test.tile.ISubTileLogic;
import elec332.test.tile.SubTileLogicBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 20-2-2018
 */
public enum SubTileRegistry {

    INSTANCE;

    private final Map<ResourceLocation, Class<? extends SubTileLogicBase>> registry = Maps.newHashMap();
    private final Map<Class<? extends SubTileLogicBase>, ResourceLocation> registryInverse = Maps.newHashMap();
    private final Map<ResourceLocation, Function<SubTileLogicBase.Data, SubTileLogicBase>> constructors = Maps.newHashMap();
    private final Map<Capability, Function<List<?>, ?>> capCombiners = Maps.newHashMap();
    private final Map<Class<? extends SubTileLogicBase>, List<IUnlistedProperty>> properties = Maps.newHashMap();

    @SuppressWarnings("all")
    public void registerSubTile(@Nonnull Class<? extends ISubTileLogic> clazz, @Nonnull ResourceLocation name, IUnlistedProperty... properties) {
        if (!SubTileLogicBase.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException();
        }
        Preconditions.checkNotNull(name);
        if (registry.get(name) != null) {
            throw new UnsupportedOperationException();
        }
        try {
            MethodHandle handle = MethodHandles.lookup().unreflectConstructor(clazz.getConstructor(SubTileLogicBase.Data.class));
            constructors.put(name, data -> {
                try {
                    return (SubTileLogicBase) handle.invoke(data);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            });
            registry.put(name, (Class<? extends SubTileLogicBase>) clazz);
            registryInverse.put((Class<? extends SubTileLogicBase>) clazz, name);
            this.properties.put((Class<? extends SubTileLogicBase>) clazz, Lists.newArrayList(properties));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    @SuppressWarnings("all")
    public SubTileLogicBase invoke(@Nonnull Class<? extends ISubTileLogic> clazz, @Nonnull SubTileLogicBase.Data data) {
        if (!registryInverse.containsKey(clazz)) {
            throw new IllegalArgumentException();
        }
        return constructors.get(registryInverse.get(clazz)).apply(data);
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public SubTileLogicBase invoke(@Nonnull ResourceLocation name, @Nonnull SubTileLogicBase.Data data) {
        if (!constructors.containsKey(name)) {
            throw new IllegalArgumentException();
        }
        return constructors.get(name).apply(data);
    }

    @Nonnull
    @SuppressWarnings("all")
    public ResourceLocation getRegistryName(@Nonnull Class<? extends ISubTileLogic> clazz) {
        if (!registryInverse.containsKey(clazz)) {
            throw new IllegalArgumentException();
        }
        return Preconditions.checkNotNull(registryInverse.get(clazz));
    }

    @Nonnull
    @SuppressWarnings("all")
    public Set<IUnlistedProperty> getPropertiesFor(Class<? extends ISubTileLogic>... tiles) {
        return Arrays.stream(tiles)
                .map(properties::get)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    public <T> void registerCapabilityCombiner(@Nonnull Capability<T> capability, @Nonnull Function<List<LazyOptional<T>>, LazyOptional<T>> combiner) {
        Preconditions.checkNotNull(capability, "Capability cannot be null!");
        if (capCombiners.containsKey(capability)) {
            throw new IllegalArgumentException();
        }
        capCombiners.put(capability, (Function) combiner);
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public <T> LazyOptional<T> getCombined(@Nonnull Capability<T> capability, @Nonnull List<LazyOptional<T>> list) {
        list = list.stream()
                .filter(Objects::nonNull)
                .filter(LazyOptional::isPresent)
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            return LazyOptional.empty();
        }
        if (list.size() == 1) {
            return list.get(0);
        }
        Function<List<?>, ?> func = capCombiners.get(capability);
        if (func == null) {
            throw new IllegalArgumentException("No combiner registered for capability: " + capability.getName());
        }
        return (LazyOptional<T>) func.apply(list);
    }

}
