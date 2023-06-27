package elec332.eflux2.modules.wires.wire.overhead;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import elec332.core.api.annotations.StaticLoad;
import elec332.core.api.network.object.INetworkObjectHandler;
import elec332.core.api.network.object.INetworkObjectSender;
import elec332.core.api.storage.IExternalSaveHandler;
import elec332.core.util.NBTTypes;
import elec332.core.world.WorldHelper;
import elec332.core.world.posmap.DefaultMultiWorldPositionedObjectHolder;
import elec332.core.world.posmap.IMultiWorldPositionedObjectHolder;
import elec332.core.world.posmap.PositionedObjectHolder;
import elec332.eflux2.EFlux2;
import elec332.eflux2.api.util.ConnectionPoint;
import elec332.eflux2.util.EFlux2ResourceLocation;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 15-2-2019
 */
@StaticLoad
public enum OverheadWireHandler implements IExternalSaveHandler, INetworkObjectSender<OverheadWireHandler> {

    INSTANCE;

    OverheadWireHandler() {
        wireMap = DefaultMultiWorldPositionedObjectHolder.createListed();
        loadedWires = HashMultimap.create();
        MinecraftForge.EVENT_BUS.register(this);
    }

    private static final ResourceLocation name = new EFlux2ResourceLocation("overhead_wire");

    private final IMultiWorldPositionedObjectHolder<Set<OverheadWire>, OverheadWire> wireMap;
    private final SetMultimap<DimensionType, OverheadWire> loadedWires;
    private INetworkObjectHandler<?> networkhandler;

    public void add(OverheadWire wire, World world) {
        if (world.isRemote) {
            return;
        }
        if (wire.getPosts() != 2) {
            throw new IllegalArgumentException(); //...
        }
        for (int j = 0; j < 2; j++) {
            ConnectionPoint cp = wire.getConnectionPoint(j);
            wireMap.getOrCreate(cp.getWorld()).put(wire, cp.getPos());
        }
        DimensionType dim = WorldHelper.getDimID(world);
        for (int j = 0; j < 2; j++) {
            ConnectionPoint cp = wire.getConnectionPoint(j);
            if (cp.getWorld() == dim && WorldHelper.chunkLoaded(world, cp.getPos())) {
                loadedWires.put(dim, wire);
                EFlux2.electricityGridHandler.addObjectUnsafe(wire);
                break;
            }
        }
        networkhandler.sendToAll(1, wire.serialize());
    }

    public void remove(OverheadWire wire, IWorldReader world) {
        if (world.isRemote()) {
            return;
        }
        removeWire(wire);
    }

    public void remove(BlockPos pos, IWorld world) {
        if (world.isRemote()) {
            return;
        }
        Set<OverheadWire> wires = wireMap.getOrCreate(world).get(pos);
        wires.forEach(this::removeWire);
    }

    public void remove(final ConnectionPoint cp, IWorld world) {
        if (world.isRemote() || cp == null) {
            return;
        }
        if (WorldHelper.getDimID(world) != cp.getWorld()) {
            throw new IllegalArgumentException();
        }
        Set<OverheadWire> wires = wireMap.getOrCreate(world).get(cp.getPos());
        wires.stream()
                .filter(ohw -> ohw.containsPost(cp))
                .collect(Collectors.toSet()) //Collection is needed to prevent CME's
                .forEach(this::removeWire);
    }

    private void removeWire(OverheadWire wire) {
        if (wire.getPosts() != 2) {
            throw new IllegalArgumentException(); //...
        }
        for (int j = 0; j < 2; j++) {
            ConnectionPoint cp = wire.getConnectionPoint(j);
            wireMap.getOrCreate(cp.getWorld()).remove(cp.getPos(), wire);
            loadedWires.get(cp.getWorld()).remove(wire);
        }
        EFlux2.electricityGridHandler.removeObjectUnsafe(wire);
        networkhandler.sendToAll(2, wire.serialize());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void chunkLoad(ChunkEvent.Load event) {
        if (event.getWorld() != null && !event.getWorld().isRemote()) {
            if (!(event.getChunk() instanceof Chunk)) {
                throw new RuntimeException();
            }
            World world = ((Chunk) event.getChunk()).getWorld();
            PositionedObjectHolder<Set<OverheadWire>, OverheadWire> poh = wireMap.get(world);
            if (poh == null) {
                return;
            }
            ChunkPos pos = event.getChunk().getPos();
            Map<BlockPos, Set<OverheadWire>> cd = poh.getObjectsInChunk(pos);
            if (cd.isEmpty()) {
                return;
            }
            Set<OverheadWire> loaded = loadedWires.get(WorldHelper.getDimID(world));
            cd.values().stream()
                    .flatMap(Collection::stream)
                    .forEach(ohw -> {
                        if (!loaded.contains(ohw)) {
                            loaded.add(ohw);
                            EFlux2.electricityGridHandler.addObjectUnsafe(ohw);
                        }
                    });
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void chunkUnLoad(ChunkEvent.Unload event) {
        if (!event.getWorld().isRemote()) {
            if (!(event.getChunk() instanceof Chunk)) {
                throw new RuntimeException();
            }
            World world = ((Chunk) event.getChunk()).getWorld();
            PositionedObjectHolder<Set<OverheadWire>, OverheadWire> poh = wireMap.get(world);
            if (poh == null) {
                return;
            }
            ChunkPos pos = event.getChunk().getPos();
            Map<BlockPos, Set<OverheadWire>> cd = poh.getObjectsInChunk(pos);
            if (cd.isEmpty()) {
                return;
            }
            Set<OverheadWire> loaded = loadedWires.get(WorldHelper.getDimID(world));
            cd.values().stream()
                    .flatMap(Collection::stream)
                    .forEach(ohw -> {
                        loaded.remove(ohw);
                        EFlux2.electricityGridHandler.removeObjectUnsafe(ohw);
                    });
        }
    }

    @SubscribeEvent
    public void onPlayerConnected(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getPlayer().world.isRemote()) {
            ListNBT data = new ListNBT();
            wireMap.getValues().stream().flatMap(PositionedObjectHolder::streamValues).forEach(ohw -> data.add(ohw.serialize()));
            CompoundNBT tag = new CompoundNBT();
            tag.put("wires", data);
            networkhandler.sendTo(0, tag, (ServerPlayerEntity) event.getPlayer());
        }
    }

    @Override
    public String getName() {
        return name.toString();
    }

    @Override
    public void load(SaveHandler saveHandler, WorldInfo info, CompoundNBT tag) {
        ListNBT list = tag.getList("wireMap", NBTTypes.COMPOUND.getID());
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT data = list.getCompound(i);
            OverheadWire wire = OverheadWire.read(data);
            if (wire.getPosts() != 2) {
                throw new IllegalArgumentException(); //...
            }
            for (int j = 0; j < 2; j++) {
                ConnectionPoint cp = wire.getConnectionPoint(j);
                wireMap.getOrCreate(cp.getWorld()).put(wire, cp.getPos());
            }
        }
    }

    @Override
    public CompoundNBT save(SaveHandler saveHandler, WorldInfo info) {
        CompoundNBT ret = new CompoundNBT();
        ListNBT list = new ListNBT();
        wireMap.getValues().stream()
                .flatMap(PositionedObjectHolder::streamValues)
                .collect(Collectors.toSet())
                .forEach(w -> list.add(w.serialize()));
        ret.put("wireMap", list);
        return ret;
    }

    @Override
    public void nullifyData() {
        wireMap.clear();
    }

    @Override
    public void setNetworkObjectHandler(INetworkObjectHandler<OverheadWireHandler> handler) {
        this.networkhandler = handler;
    }

}
