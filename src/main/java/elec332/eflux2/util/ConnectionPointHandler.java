package elec332.eflux2.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import elec332.core.ElecCore;
import elec332.core.util.FMLHelper;
import elec332.core.util.NBTTypes;
import elec332.core.world.WorldHelper;
import elec332.eflux2.api.util.ConnectionPoint;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by Elec332 on 20-2-2019
 */
public class ConnectionPointHandler {

    public ConnectionPointHandler(Supplier<TileEntity> tile) {
        this.tile = tile;
        this.connections = Maps.newIdentityHashMap();
        this.listeners = l -> {
        };
        this.dataGetter = Object::toString;
        this.subIdMap = Lists.newArrayList();
    }

    private boolean locked;
    private Consumer<ConnectionPointHandler> listeners;
    private Function<Object, String> dataGetter;
    private int index;
    private final Supplier<TileEntity> tile;
    private final Map<Object, ConnectionPoint> connections;
    private final List<Integer> subIdMap;

    public void addListener(Runnable listener) {
        addListener(h -> listener.run());
    }

    public String getInfoFor(Object key) {
        getStrict(key);
        return dataGetter.apply(key);
    }

    public void setInformation(Function<Object, String> info) {
        this.dataGetter = info;
    }

    public void addListener(Consumer<ConnectionPointHandler> listener) {
        listeners = listeners.andThen(listener);
    }

    public Object makeConnectionPoint(Direction side, int sideIndex, Direction edge) {
        if (locked) {
            throw new IllegalStateException();
        }
        Object ret = new Object() {

            final int i = index;

            @Override
            public int hashCode() {
                return i;
            }

            @Override
            public String toString() {
                return hashCode() + " " + side + "@" + edge;
            }

        };
        index++;
        if (subIdMap.size() <= ret.hashCode()) {
            subIdMap.add(-1);
            subIdMap.set(ret.hashCode(), sideIndex);
        } else {
            sideIndex = subIdMap.get(ret.hashCode());
        }
        connections.put(ret, new ConnectionPoint(tile.get().getPos(), Preconditions.checkNotNull(tile.get().getWorld()), side, sideIndex, edge));
        return ret;
    }

    public void updateConnection(Object key, Direction side, int sideIndex, Direction edge) {
        getStrict(key);

        World world = Preconditions.checkNotNull(tile.get().getWorld());
        connections.put(key, new ConnectionPoint(tile.get().getPos(), world, side, sideIndex, edge));
        if (FMLHelper.getLogicalSide() == LogicalSide.SERVER) {
            subIdMap.set(key.hashCode(), sideIndex);
        }
        if (locked && WorldHelper.chunkLoaded(world, tile.get().getPos())) {
            tile.get().markDirty();
            listeners.accept(this);
        }
    }

    @Nonnull
    public ConnectionPoint getStrict(Object key) {
        if (!connections.containsKey(key)) {
            throw new IllegalArgumentException();
        }
        return Preconditions.checkNotNull(connections.get(key));
    }

    public CompoundNBT save() {
        ListNBT list = new ListNBT();
        for (Integer i : subIdMap) {
            list.add(IntNBT.valueOf(i));
        }
        CompoundNBT ret = new CompoundNBT();
        ret.put("sidm", list);
        return ret;
    }

    public void read(CompoundNBT tag) {
        ListNBT list = tag.getList("sidm", NBTTypes.INT.getID());
        subIdMap.clear();
        for (int i = 0; i < list.size(); i++) {
            subIdMap.add(list.getInt(i));
        }
        World world = tile.get().getWorld();
        if (world != null && WorldHelper.isClient(world)) {
            connections.keySet().forEach(k -> {
                ConnectionPoint old = connections.get(k);
                ConnectionPoint nw = new ConnectionPoint(tile.get().getPos(), world, old.getSide(), subIdMap.get(k.hashCode()), old.getEdge());
                connections.put(k, nw);
            });
        }
    }

    public void onLoad() {
        ElecCore.tickHandler.registerCall(this::updateData, tile.get().getWorld());
    }

    public void onBlockInfoUpdate() {
        updateData();
    }

    private void updateData() {
        connections.keySet().forEach(k -> {
            ConnectionPoint old = connections.get(k);
            World world = Preconditions.checkNotNull(tile.get().getWorld());
            ConnectionPoint nw = new ConnectionPoint(tile.get().getPos(), world, old.getOriginalSide(), old.getSideNumber(), old.getEdge());
            if (!old.equals(nw)) {
                connections.put(k, nw);
                if (locked && WorldHelper.chunkLoaded(world, tile.get().getPos())) {
                    listeners.accept(this);
                }
            }
        });
        locked = true;
    }

    @Override
    public String toString() {
        return connections.values().toString();
    }

}
