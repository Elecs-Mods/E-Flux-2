package elec332.eflux2.electricity.grid;

import com.google.common.collect.*;
import elec332.core.ElecCore;
import elec332.core.grid.AbstractGridHandler;
import elec332.core.world.DimensionCoordinate;
import elec332.core.world.WorldHelper;
import elec332.core.world.posmap.DefaultMultiWorldPositionedObjectHolder;
import elec332.core.world.posmap.IMultiWorldPositionedObjectHolder;
import elec332.core.world.posmap.PositionedObjectHolder;
import elec332.eflux2.api.EFlux2API;
import elec332.eflux2.api.electricity.IElectricityDevice;
import elec332.eflux2.api.electricity.IEnergyObject;
import elec332.eflux2.api.electricity.component.CircuitElement;
import elec332.eflux2.api.electricity.component.ICircuit;
import elec332.eflux2.api.electricity.grid.IElectricityGridHandler;
import elec332.eflux2.api.util.ConnectionPoint;
import elec332.eflux2.electricity.simulation.CircuitElementFactory;
import elec332.eflux2.electricity.simulation.engine.Circuit;
import elec332.eflux2.electricity.simulation.engine.SimulationEngine;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Elec332 on 13-11-2017.
 */
public final class ElectricityGridHandler extends AbstractGridHandler<ETileEntityLink> implements IElectricityGridHandler {

    public ElectricityGridHandler() {
        MinecraftForge.EVENT_BUS.register(this);
        this.cache = DefaultMultiWorldPositionedObjectHolder.create();
        this.circuits = Maps.newHashMap();
        this.toAddNextTick = Sets.newHashSet();
        this.map = HashMultimap.create();
        this.wires = Sets.newHashSet();
    }

    private final Multimap<Object, CircuitElement<?>> map;
    private final IMultiWorldPositionedObjectHolder<CPPosObj, CPPosObj> cache;
    private final Map<UUID, Circuit> circuits;
    private final Set<CircuitElement<?>> toAddNextTick;
    private final Set<IEnergyObject> wires;

    @Override
    public void addObjectUnsafe(IEnergyObject wire) {
        if (wires.add(wire)) {
            Collection<CircuitElement<?>> elm = CircuitElementFactory.INSTANCE.wrapComponent(wire);
            elm.forEach(element -> {
                map.put(wire, element);
                addElmImpl(element);
            });
        }
    }

    @Override
    public void removeObjectUnsafe(IEnergyObject wire) {
        Collection<CircuitElement<?>> elm = map.get(wire);
        if (elm != null) {
            elm.forEach(this::removeElmImpl);
            map.removeAll(wire);
            wires.remove(wire);
        }
    }

    public void clear() {
        map.clear();
        cache.clear();
        circuits.values().forEach(Circuit::clear);
        circuits.clear();
        wires.clear();
        toAddNextTick.clear();
    }

    @Override
    protected void onObjectRemoved(ETileEntityLink o, Set<DimensionCoordinate> allUpdates) {
        Collection<CircuitElement<?>> elm = map.get(o);
        if (elm != null) {
            elm.forEach(this::removeElmImpl);
            map.removeAll(o);
        }
    }

    @Override
    protected void internalAdd(ETileEntityLink o) {
        checkPendingAdds();
        TileEntity tile = o.getPosition().getTileEntity();
        if (tile == null) {
            return;
        }
        Collection<CircuitElement<?>> elm = CircuitElementFactory.INSTANCE.wrapComponent(tile.getCapability(EFlux2API.ELECTRICITY_CAP));
        elm.forEach(element -> {
            map.put(o, element);
            addElmImpl(element);
        });
    }

    private void checkPendingAdds() {
        if (!toAddNextTick.isEmpty()) {
            toAddNextTick.forEach(this::addElmImpl);
            toAddNextTick.clear();
        }
    }

    private void addElmImpl(CircuitElement<?> elm) {
        if (elm == null) {
            return;
        }
        Set<CircuitElement<?>> ceL = Sets.newHashSet(elm);
        //if (elm.getCircuit() != null){
        //    System.out.println(((Circuit)elm.getCircuit()).getId());
        //    throw new RuntimeException();
        //}
        Circuit myCircuit = (Circuit) elm.getCircuit();
        if (myCircuit != null) {
            ceL = null;
        }
        for (ConnectionPoint cp : elm.getConnectionPoints()) {
            PositionedObjectHolder<CPPosObj, CPPosObj> woj = cache.getOrCreate(cp.getWorld());
            CPPosObj bla = woj.get(cp.getPos());
            if (bla == null) {
                woj.put(bla = new CPPosObj(), cp.getPos());
            }
            for (CircuitElement<?> otherElmsAtPos : bla.connections.get(cp)) {
                ICircuit c = otherElmsAtPos.getCircuit();
                if (c == null) {
                    if (myCircuit == null) {
                        ceL.add(otherElmsAtPos);
                    } else {
                        myCircuit.addElement(otherElmsAtPos);
                    }
                } else {
                    Circuit circuit = (Circuit) c;
                    if (myCircuit == null) {
                        ceL.forEach(circuit::addElement);
                        ceL = null;
                        myCircuit = circuit;
                    } else {
                        if (circuit != myCircuit) {
                            myCircuit.consumeCircuit(circuit);
                            circuit.clear();
                            circuits.remove(circuit.getId());
                        }
                    }
                }
            }
            bla.connections.put(cp, elm);
        }
        if (ceL != null && ceL.size() > 1) {
            Circuit newCircuit = new Circuit();
            ceL.forEach(newCircuit::addElement);
            circuits.put(newCircuit.getId(), newCircuit);
        }
    }

    private void removeElmImpl(CircuitElement<?> elm) {
        if (elm == null) {
            return;
        }
        ICircuit c = elm.getCircuit();
        for (ConnectionPoint cp : elm.getConnectionPoints()) {
            PositionedObjectHolder<CPPosObj, CPPosObj> woj = cache.getOrCreate(cp.getWorld());
            CPPosObj bla = woj.get(cp.getPos());
            if (bla == null) {
                throw new RuntimeException();
            }
            bla.connections.remove(cp, elm);
        }
        elm.destroy();
        toAddNextTick.remove(elm);
        if (c == null) {
            return;
        }
        Circuit circuit = (Circuit) c;
        if (circuit.removeElement(Lists.newArrayList(elm), toAddNextTick)) {
            circuits.remove(circuit.getId());
            circuit.clear();
        }
    }

    @Override
    public void tick() {
        checkPendingAdds();
        if (SimulationEngine.debug()) {
            System.out.println("-------------------------------");
            System.out.println("Siz: " + circuits.keySet().size());
        }
        //SimEngine.INSTANCE.preTick(circuit);
        circuits.values().forEach(SimulationEngine.INSTANCE::preTick);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean isValidObject(TileEntity tile) {
        LazyOptional<IElectricityDevice> cap = tile.getCapability(EFlux2API.ELECTRICITY_CAP);
        if (!cap.isPresent()) {
            return false;
        }
        System.out.println(tile.getPos() + "  " + tile + " valid");
        IElectricityDevice eObj = cap.orElse(null);
        return eObj != null && !CircuitElementFactory.INSTANCE.isPassiveConnector(eObj);
    }

    @Override
    protected ETileEntityLink createNewObject(TileEntity tile) {
        return new ETileEntityLink(tile);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void serverTick(TickEvent.ServerTickEvent event) {
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END) {
            if (SimulationEngine.debug()) {
                System.out.println("TickLow: " + ElecCore.proxy.getServer().getWorld(DimensionType.OVERWORLD).getGameTime());
            }

            circuits.values().forEach(SimulationEngine.INSTANCE::tick);
            //SimEngine.INSTANCE.tick(circuit);
            if (SimulationEngine.debug()) {
                System.out.println("-------------------------");
            }
        }
    }

    @SubscribeEvent
    public void worldUnload(WorldEvent.Unload event) {
        if (!event.getWorld().isRemote() && WorldHelper.getDimID(event.getWorld()) == DimensionType.OVERWORLD) {
            this.clear();
        }
    }

}
