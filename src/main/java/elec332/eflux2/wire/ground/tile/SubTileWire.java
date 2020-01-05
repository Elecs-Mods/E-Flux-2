package elec332.eflux2.wire.ground.tile;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import elec332.core.ElecCore;
import elec332.core.tile.sub.ISubTileLogic;
import elec332.core.tile.sub.SubTileLogicBase;
import elec332.core.util.EnumBitSet;
import elec332.core.util.NBTTypes;
import elec332.core.util.PlayerHelper;
import elec332.core.util.math.HitboxHelper;
import elec332.core.world.WorldHelper;
import elec332.eflux2.EFlux2;
import elec332.eflux2.api.EFlux2API;
import elec332.eflux2.api.electricity.IElectricityDevice;
import elec332.eflux2.api.electricity.IEnergyObject;
import elec332.eflux2.register.EFlux2BlockRegister;
import elec332.eflux2.wire.WireColorHelper;
import elec332.eflux2.wire.ground.GroundWire;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.minecraft.block.Block.spawnAsEntity;

/**
 * Created by Elec332 on 1-2-2019
 */
public class SubTileWire extends SubTileLogicBase implements ISubTileLogic, IWireContainer, IElectricityDevice {

    public SubTileWire(Data data) {
        super(data);
        wires = Lists.newArrayList();
        wireView = Collections.unmodifiableList(wires);
        shapeMemory = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.SECONDS).build();
    }

    private final List<GroundWire> wires;
    private final List<GroundWire> wireView;
    private Cache<List<VoxelShape>, VoxelShape> shapeMemory;
    private boolean checkingWires = false;
    private boolean sendPacket = false;
    private boolean hadFirstLoad = false; //Packet saving measure
    private boolean blockPackets = false; //Packet saving measure
    private boolean noWires = false; // Connection checking reduction
    private Map<BlockPos, LazyOptional<IWireContainer>> otherWires = Maps.newHashMap();

    @Override
    public boolean canBeRemoved() {
        return wireView.isEmpty();
    }

    @Override
    public boolean addWire(GroundWire wire) {
        return wire != null && addWireInternal(wire, true);
    }

    private boolean addWireInternal(@Nonnull GroundWire wire, boolean notify) {
        if (wires.isEmpty() && notify && !hadFirstLoad) {
            blockPackets = true;
        }
        boolean added = false;
        if (wireView.isEmpty() || getWire(wire.getPlacement()) == null) {
            if (!wire.isTerminalPart() && notify && VoxelShapes.compare(WorldHelper.getBlockState(getWorld(), getPos()).getShape(getWorld(), getPos()), wire.getShape(), IBooleanFunction.AND)) {
                return false;
            }
            wires.add(wire);
            wire.wire = this;
            added = true;
            resetWireCapability();
            if (hasWorld() && !getWorld().isRemote) {
                checkConnections(true);
                if (notify) {
                    notifyNeighborsOfChangeExtensively();
                    markDirty();
                }
            }
        }
        return added;
    }

    @Nullable
    @Override
    public GroundWire getWire(Direction facing) {
        return wireView.stream().filter(wp -> wp.getPlacement() == facing).findFirst().orElse(null);
    }

    @Nonnull
    @Override
    public List<GroundWire> getWireView() {
        return wireView;
    }

    public void notifyNeighborsOfChangeExtensively() {
        BlockPos start = getPos().add(-1, -1, -1);
        //Block me = WorldHelper.getBlockAt(getWorld(), getPos());
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    if (i == 1 || j == 1 || k == 1) {
                        BlockPos poz = start.add(i, k, j);
                        if (!getPos().equals(poz)) {
                            getWorld().neighborChanged(poz, EFlux2.WIRE_MARKER, getPos());
                        }
                    }
                }
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, int data) {
        try {
            final List<VoxelShape> shapes = wireView.stream().map(wp -> wp.getShape(data == 1)).collect(Collectors.toList());
            return shapeMemory.get(shapes, () -> HitboxHelper.combineShapes(shapes));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public VoxelShape getSelectionBox(BlockState state, @Nonnull RayTraceResult hit, PlayerEntity player) {
        if (hit.hitInfo instanceof GroundWire) {
            return ((GroundWire) hit.hitInfo).getShape();
        }
        return null;
    }

    @Override
    public boolean removedByPlayer(@Nonnull PlayerEntity player, boolean willHarvest, @Nonnull RayTraceResult hit) {
        if (!getWorld().isRemote && hit.hitInfo instanceof GroundWire) {
            GroundWire wirePart = (GroundWire) hit.hitInfo;
            if (wirePart != null) {
                remove(wirePart);
                ItemStack stack = wirePart.getDropStack();
                Block.spawnAsEntity(getWorld(), getPos(), stack);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onBlockActivated(PlayerEntity player, Hand hand, RayTraceResult hit) {
        if (hand != Hand.MAIN_HAND) {
            return false;
        }
        if (hit.hitInfo instanceof GroundWire) {
            GroundWire wirePart = (GroundWire) hit.hitInfo;
            if (wirePart != null) {
                if (!getWorld().isRemote) {
                    DecimalFormat df = new DecimalFormat("0.00");
                    String s = "";
                    for (DyeColor c : WireColorHelper.getColors(wirePart.getColorBits())) {
                        s += c + " V:" + df.format(wirePart.v[c.getId()]) + " A:" + df.format(wirePart.a[c.getId()]) + " ";
                    }
                    PlayerHelper.sendMessageToPlayer(player, s);
                }

                /*String str = "";
                for (Direction f : wirePart.getHorizontalConnections()) {
                    str += " " + f + "|" + wirePart.getClientColors(f);
                }
                PlayerHelper.sendMessageToPlayer(player, (!player.world.isRemote) + " " + WireColorHelper.getColors(wirePart.getColorBits()) + wirePart.getColorBits() + str);
                PlayerHelper.sendMessageToPlayer(player, wirePart.getPlacement() + "  "+wirePart.getHorizontalConnections().stream().map(f -> WireFacingHelper.getRealSide(wirePart.getPlacement(), f)).collect(Collectors.toList()));
                */
            }
        }
        return false;
    }

    @Override
    public void neighborChanged(BlockPos neighborPos, Block changedBlock, boolean observer) {
        List<GroundWire> wp = Lists.newArrayList();
        for (GroundWire p : getWireView()) {
            if (!p.canStay(getWorld(), getPos())) {
                spawnAsEntity(getWorld(), getPos(), p.getDropStack());
                wp.add(p);
            }
        }
        removeAll(wp);
        if (wp.isEmpty()) {
            checkConnections(false, changedBlock != EFlux2.WIRE_MARKER);
        }
    }

    public void removeAll(Collection<GroundWire> wires) {
        if (wires.isEmpty()) {
            return;
        }
        this.wires.removeAll(wires);
        onWiresRemoved();
    }

    public void remove(GroundWire wire) {
        wires.remove(wire);
        onWiresRemoved();
    }

    private void onWiresRemoved() {
        if (getWorld().isRemote) {
            return;
        }
        markDirty();
        noWires = wireView.isEmpty();
        resetWireCapability();
        checkingWires = blockPackets = true;
        if (!wireView.isEmpty()) {
            checkConnections();
        }
        checkingWires = blockPackets = sendPacket = false;
        syncWireData();
        notifyNeighborsOfChangeExtensively();
    }

    @Nullable
    @Override
    public ItemStack getStack(@Nonnull RayTraceResult hit, PlayerEntity player) {
        if (hit.hitInfo instanceof GroundWire) {
            GroundWire wire = (GroundWire) hit.hitInfo;
            if (wire != null) {
                return wire.getDropStack();
            }
        }
        return null;
    }

    public void syncWireData() {
        if (checkingWires) {
            sendPacket = true;
            return;
        }
        sendPacket(3, writeWiresToNBT(new CompoundNBT(), true));
    }

    public void checkConnections() {
        checkConnections(false);
    }

    public void checkConnections(boolean forcePacket) {
        checkConnections(forcePacket, false);
    }

    private void checkConnections(boolean forcePacket, boolean forceCheck) {
        if (getWorld().isRemote) {
            return;
        }
        checkingWires = true;

        Function<BlockPos, Pair<Boolean, IWireContainer>> func = new Function<BlockPos, Pair<Boolean, IWireContainer>>() {

            private Map<BlockPos, Boolean> prevData = Maps.newHashMap();

            //If multiple calls are made for the same location, but the old cap has become invalid,
            //the return value will indicate it hasn't invalidated (except for the first one), resulting in false results.
            @Override
            public Pair<Boolean, IWireContainer> apply(BlockPos pos) {
                Pair<Boolean, IWireContainer> ret = getWire(pos);
                if (ret == null) {
                    return null;
                }
                if (forcePacket || forceCheck) {
                    return Pair.of(false, ret.getRight());
                }
                if (prevData.containsKey(pos)) {
                    boolean now = prevData.get(pos);
                    if (now && !ret.getLeft()) {
                        prevData.put(pos, now = false);
                    }
                    return Pair.of(now, ret.getRight());
                }
                prevData.put(pos, ret.getLeft());
                return ret;
            }

        };


        wireView.forEach(wirePart -> wirePart.checkConnections(getPos(), getWorld(), func));
        checkingWires = false;
        if (sendPacket || forcePacket) {
            sendPacket = false;
            if (!blockPackets) {
                syncWireData();
            }
        }
        resetElectricityCapability();
    }

    @Override
    public void onDataPacket(int id, CompoundNBT tag) {
        if (id == 3) {
            readWiresFromNBT(tag, true);
            WorldHelper.markBlockRangeForRenderUpdate(getWorld(), getPos().add(1, 1, 1), getPos().add(-1, -1, -1));
        }
    }

    @Override
    public void readFromNBT(CompoundNBT compound) {
        readWiresFromNBT(compound, false);
    }

    private void readWiresFromNBT(CompoundNBT compound, boolean client) {
        ListNBT l = compound.getList("wirestuff", NBTTypes.COMPOUND.getID());
        EnumBitSet<Direction> faces = EnumBitSet.noneOf(Direction.class);
        for (int i = 0; i < l.size(); i++) {
            CompoundNBT tag = l.getCompound(i);
            GroundWire wirePart = new GroundWire(Direction.values()[tag.getByte("sbfww")], tag.getByte("sbfws"));
            if (client) {
                GroundWire w = getWire(wirePart.getPlacement());
                if (w != null) {
                    wirePart = w;
                }
            }
            wirePart.readFromNBT(tag);
            if (client) {
                wirePart.readClientData(tag);
            }
            faces.add(wirePart.getPlacement());
            addWireInternal(wirePart, false);
        }
        if (client) {
            List<GroundWire> wp = Lists.newArrayList();
            for (GroundWire p : getWireView()) {
                if (!faces.contains(p.getPlacement())) {
                    wp.add(p);
                }
            }
            wires.removeAll(wp);
        }
    }

    @Override
    @Nonnull
    public CompoundNBT writeToNBT(@Nonnull CompoundNBT compound) {
        return writeWiresToNBT(compound, false);
    }

    private CompoundNBT writeWiresToNBT(CompoundNBT ret, boolean client) {
        ListNBT l = new ListNBT();
        wireView.forEach(wire -> {
            CompoundNBT tag = new CompoundNBT();
            tag.putByte("sbfww", (byte) wire.getPlacement().ordinal());
            tag.putByte("sbfws", (byte) wire.getWireSize());
            tag = wire.writeToNBT(tag);
            if (client) {
                wire.writeClientData(tag);
            }
            l.add(tag);
        });
        ret.put("wirestuff", l);
        return ret;
    }

    @Override
    public void invalidate() {
        if (hasWorld() && WorldHelper.chunkLoaded(getWorld(), getPos())) {
            BlockState state = WorldHelper.getBlockState(getWorld(), getPos());
            if (state.getBlock() != EFlux2BlockRegister.wire && myWire != null) {
                myWire.invalidate();
                notifyNeighborsOfChangeExtensively();
            }
        }
    }

    @Override
    public void onLoad() {
        if (!getWorld().isRemote) {
            ElecCore.tickHandler.registerCall(() -> {
                hadFirstLoad = true;
                if (blockPackets) {
                    blockPackets = false;
                    return;
                }
                checkConnections();
            }, getWorld());
        }
    }

    @Override
    public void onRemoved() {
        myWire.invalidate();
        wireRes.invalidate();
        wires.clear();
        myWire = null;
        wireRes = null;
        invalidate();
    }

    @Override
    public void sendInitialLoadPackets() {
        syncWireData();
    }

    public void resetWireCapability() {
        myWire.invalidate();
        myWire = LazyOptional.of(() -> this);
    }

    public void resetElectricityCapability() {
        wireRes.invalidate();
        internalWires = wireView.stream().map(GroundWire::getInternalWires).flatMap(Collection::stream).collect(Collectors.toSet());
        wireRes = LazyOptional.of(() -> this);
        BlockState state = WorldHelper.getBlockState(getWorld(), getPos());
        getWorld().notifyBlockUpdate(getPos(), state, state, 1);
    }

    private Pair<Boolean, IWireContainer> getWire(BlockPos pos) {
        LazyOptional<IWireContainer> w = otherWires.get(pos);
        if (w == null || !w.isPresent()) {
            otherWires.remove(pos);
            TileEntity tile = WorldHelper.getTileAt(getWorld(), pos);
            if (tile != null) {
                w = tile.getCapability(EFlux2.WIRE_CAPABILITY);
                if (w.isPresent()) {
                    otherWires.put(pos, w);
                    return Pair.of(false, w.orElseThrow(NullPointerException::new));
                } else {
                    return Pair.of(false, null);
                }
            }
            return null;

        }
        return Pair.of(true, w.orElseThrow(NullPointerException::new));
    }

    private LazyOptional<IWireContainer> myWire = LazyOptional.of(() -> this);
    private LazyOptional<IElectricityDevice> wireRes = LazyOptional.of(() -> this);
    private Set<IEnergyObject> internalWires = Sets.newHashSet();

    @Nonnull
    @Override
    @SuppressWarnings("all")
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == EFlux2API.ELECTRICITY_CAP) {
            return wireRes.cast();
        }
        return EFlux2.WIRE_CAPABILITY.orEmpty(cap, myWire);
    }

    @Override
    public Set<IEnergyObject> getInternalComponents() {
        return internalWires;
    }

}
