package elec332.eflux2.modules.wires.wire.overhead;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import elec332.core.api.network.ElecByteBuf;
import elec332.core.api.network.object.INetworkObjectReceiver;
import elec332.core.util.NBTTypes;
import elec332.core.world.posmap.DefaultMultiWorldPositionedObjectHolder;
import elec332.core.world.posmap.IMultiWorldPositionedObjectHolder;
import elec332.core.world.posmap.PositionedObjectHolder;
import elec332.eflux2.api.util.ConnectionPoint;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by Elec332 on 16-2-2019
 */
@OnlyIn(Dist.CLIENT)
public enum OverHeadWireHandlerClient implements INetworkObjectReceiver<OverheadWireHandler> {

    INSTANCE;

    OverHeadWireHandlerClient() {
        loadedWires = DefaultMultiWorldPositionedObjectHolder.createListed();
    }

    private static final Function<ChunkPos, Stream<OverheadWire>> NULL_RENDER = pos -> ImmutableSet.<OverheadWire>of().stream();
    private final IMultiWorldPositionedObjectHolder<Set<OverheadWire>, OverheadWire> loadedWires;

    @Nonnull
    public Function<ChunkPos, Stream<OverheadWire>> getWires(DimensionType world) {
        PositionedObjectHolder<Set<OverheadWire>, OverheadWire> r = loadedWires.get(world);
        if (r == null) {
            return NULL_RENDER;
        }
        return pos -> r.getObjectsInChunk(pos).values().stream().flatMap(Collection::stream);
    }

    @Override
    public void onPacket(int id, ElecByteBuf data) {
        switch (id) {
            case 0:
                loadedWires.clear();
                ListNBT list = Preconditions.checkNotNull(data.readCompoundTag()).getList("wires", NBTTypes.COMPOUND.getID());
                for (int i = 0; i < list.size(); i++) {
                    addWire(OverheadWire.read(list.getCompound(i)));
                }
                break;
            case 1:
                addWire(OverheadWire.read(data.readCompoundTag()));
                break;
            case 2:
                removeWire(OverheadWire.read(data.readCompoundTag()));
                break;
            case 3:
                loadedWires.clear();
        }
    }

    private void addWire(OverheadWire wire) {
        for (int j = 0; j < 2; j++) {
            ConnectionPoint cp = wire.getConnectionPoint(j);
            loadedWires.getOrCreate(cp.getWorld()).put(wire, cp.getPos());
        }
    }

    private void removeWire(final OverheadWire wire_) {
        ConnectionPoint cp = wire_.getConnectionPoint(0);
        OverheadWire wire = Preconditions.checkNotNull(loadedWires.get(cp.getWorld())).get(cp.getPos()).stream().filter(w -> w.equals(wire_)).findFirst().orElse(wire_);
        for (int j = 0; j < 2; j++) {
            cp = wire.getConnectionPoint(j);
            loadedWires.getOrCreate(cp.getWorld()).remove(cp.getPos(), wire);
        }
    }

}
