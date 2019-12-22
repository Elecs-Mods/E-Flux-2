package elec332.test.wire.terminal.tile;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import elec332.core.ElecCore;
import elec332.core.util.HitboxHelper;
import elec332.core.util.IndexedBlockPos;
import elec332.core.util.NBTTypes;
import elec332.core.util.RayTraceHelper;
import elec332.core.world.WorldHelper;
import elec332.test.TestMod;
import elec332.test.api.TestModAPI;
import elec332.test.api.electricity.IElectricityDevice;
import elec332.test.api.electricity.IEnergyObject;
import elec332.test.api.electricity.component.EnumElectricityType;
import elec332.test.api.util.ConnectionPoint;
import elec332.test.tile.SubTileLogicBase;
import elec332.test.wire.ground.GroundWire;
import elec332.test.wire.ground.tile.IWireContainer;
import elec332.test.wire.overhead.OverheadWireHandler;
import elec332.test.wire.terminal.GroundTerminal;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static net.minecraft.block.Block.spawnAsEntity;

/**
 * Created by Elec332 on 20-2-2018
 */
public class SubTileTerminal extends SubTileLogicBase implements ISubTileTerminal, IElectricityDevice, IEnergyObject, IWireContainer {

    public SubTileTerminal(SubTileLogicBase.Data data) {
        super(data);
        this.terminals = Lists.newArrayList();
    }

    private final List<GroundTerminal> terminals;
    private BitSet bcfs = new BitSet(EnumFacing.values().length);

    @Override
    public boolean addTerminal(GroundTerminal terminal) {
        BlockPos pos = new IndexedBlockPos(getPos(), 1);
        VoxelShape block = WorldHelper.getBlockState(getWorld(), pos).getShape(getWorld(), pos);
        if (!block.isEmpty() && VoxelShapes.compare(block, terminal.getShape(), IBooleanFunction.AND)) {
            return false;
        }
        terminals.add(terminal);
        if (terminal.getSize() > 2) {
            bcfs.set(terminal.getSide().ordinal());
        }
        sendPacket(1, writeToNBT(new NBTTagCompound()));
        notifyNeighborsOfChangeExtensively(true);
        return true;
    }

    @Override
    public void neighborChanged(BlockPos neighborPos, Block changedBlock, boolean observer) {
        if (getWorld().isRemote()) {
            return;
        }
        List<GroundTerminal> gt = Lists.newArrayList();
        BlockPos diff = neighborPos.subtract(getPos());
        EnumFacing f = EnumFacing.getFacingFromVector(diff.getX(), diff.getY(), diff.getZ());
        boolean me = neighborPos.equals(getPos());
        for (GroundTerminal p : getTerminalView()) {
            if (!p.canStay(getWorld(), getPos())) {
                gt.add(p);
                spawnAsEntity(getWorld(), getPos(), p.getStack());
            } else {
                if (me || p.getSide() == f) {
                    p.checkConnectionPoint(getWorld(), getPos());
                }
            }
        }

        removeTerminals(gt);
    }

    @Nullable
    @Override
    public ItemStack getStack(@Nonnull RayTraceResult hit, EntityPlayer player) {
        GroundTerminal terminal = getFromHit(hit);
        if (terminal != null) {
            return terminal.getStack();
        }
        return null;
    }

    @Override
    public Collection<GroundTerminal> getTerminalView() {
        return terminals;
    }

    @Override
    public void onLoad() {
        if (!getWorld().isRemote()) {
            ElecCore.tickHandler.registerCall(() ->
                    terminals.forEach(t -> t.checkConnectionPoint(getWorld(), getPos())), getWorld()
            );
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        NBTTagList list = compound.getList("terminals", NBTTypes.COMPOUND.getID());
        terminals.clear();
        bcfs.clear();
        for (int i = 0; i < list.size(); i++) {
            NBTTagCompound tag = list.getCompound(i);
            GroundTerminal terminal = GroundTerminal.read(tag);
            terminals.add(terminal);
            if (terminal.getSize() > 2) {
                bcfs.set(terminal.getSide().ordinal());
            }
            terminal.checkConnectionPoint(getWorld(), getPos());
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        terminals.forEach(terminal -> list.add(terminal.serialize()));
        compound.put("terminals", list);
        return compound;
    }

    @Override
    public void onRemoved() {
        terminals.clear();
        bcfs.clear();
        invalidate();
    }

    @Override
    public RayTraceResult getRayTraceResult(Vec3d start, Vec3d end, int data) {
        return terminals.stream().reduce(null, (pre, wire) -> {
            Vec3d e = end;
            if (pre != null) {
                e = RayTraceHelper.slightExpand(start, pre.hitVec);
            }
            RayTraceResult hit = wire.getShape().func_212433_a(start, e, getPos());
            if (hit != null) {
                hit.subHit = 20 * wire.getSide().ordinal();
                hit.hitInfo = wire;
                return hit;
            }
            return pre;
        }, (a, b) -> b);
    }

    @Override
    public boolean removedByPlayer(@Nonnull EntityPlayer player, boolean willHarvest, @Nonnull RayTraceResult hit) {
        GroundTerminal terminal = getFromHit(hit);
        if (terminal != null) {
            removeTerminal(terminal);
            ItemStack stack = terminal.getStack();
            spawnAsEntity(getWorld(), getPos(), stack);
            return true;
        }
        return false;
    }

    private void removeTerminals(Collection<GroundTerminal> terminals) {
        if (terminals != null && !terminals.isEmpty()) {
            terminals.forEach(this.terminals::remove);
            terminals.forEach(terminal -> {
                if (terminal.getSize() > 2) {
                    bcfs.set(terminal.getSide().ordinal(), false);
                }
            });
            terminals.forEach(t -> OverheadWireHandler.INSTANCE.remove(t.getConnectionPoint(), getWorld()));
            afterRemoval();
        }
    }

    private void removeTerminal(GroundTerminal terminal) {
        if (terminal != null) {
            terminals.remove(terminal);
            if (terminal.getSize() > 2) {
                bcfs.set(terminal.getSide().ordinal(), false);
            }
            OverheadWireHandler.INSTANCE.remove(terminal.getConnectionPoint(), getWorld());
            afterRemoval();
        }
    }

    private void afterRemoval() {
        sendPacket(1, writeToNBT(new NBTTagCompound()));
        notifyNeighborsOfChangeExtensively(true);
    }

    @Override
    public boolean canBeRemoved() {
        return getTerminalView().isEmpty();
    }

    private GroundTerminal getFromHit(RayTraceResult hit) {
        int data = hit.subHit;//-hit.subHit - 20;
        EnumFacing facing = EnumFacing.values()[data / 20];
        GroundTerminal ret = (GroundTerminal) hit.hitInfo;
        if (ret.getSide() == facing) {
            return ret;
        }
        return null;
    }

    @Override //todo: cache
    public VoxelShape getShape(int data) {
        return HitboxHelper.combineShapes(terminals.stream().map(GroundTerminal::getShape));
    }

    @Override
    @SuppressWarnings("all")
    public VoxelShape getSelectionBox(@Nonnull RayTraceResult hit, EntityPlayer player) {
        return getFromHit(hit).getShape();
    }

    public void notifyNeighborsOfChangeExtensively(boolean me) {
        Block b = WorldHelper.getBlockAt(getWorld(), getPos());
        for (EnumFacing facing : EnumFacing.values()) {
            getWorld().neighborChanged(getPos().offset(facing), b, getPos());
        }
        if (me) {
            getWorld().neighborChanged(getPos(), b, getPos());
        }
    }

    @Override
    public void onDataPacket(int id, NBTTagCompound tag) {
        if (id == 1) {
            readFromNBT(tag);
            WorldHelper.markBlockForRenderUpdate(getWorld(), getPos());
        }
    }

    private LazyOptional<IElectricityDevice> connections = LazyOptional.of(() -> this);
    private Set<IEnergyObject> internals = ImmutableSet.of(this);

    @Nonnull
    @Override //todo: Cache and invalidate
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing facing) {
        if (cap == TestMod.WIRE_CAPABILITY) {
            return LazyOptional.of(() -> this).cast();
        }
        return cap == TestModAPI.ELECTRICITY_CAP ? connections.cast() : TestMod.TERMINAL_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> this));
    }

    @Override
    public Set<IEnergyObject> getInternalComponents() {
        return internals;
    }

    @Nullable
    @Override
    public EnumElectricityType getEnergyType() {
        return null;
    }

    @Nonnull
    @Override
    public ConnectionPoint getConnectionPoint(int post) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPosts() {
        return 0;
    }

    @Nullable
    @Override
    public ConnectionPoint getConnectionPoint(EnumFacing side, Vec3d hitVec) {
        Optional<GroundTerminal> g = terminals.stream().filter(gt -> HitboxHelper.doesShapeContain(gt.getShape(), hitVec)).findFirst();
        if (!g.isPresent()) {
            g = terminals.stream().filter(gt -> gt.getShape().toBoundingBoxList().stream().anyMatch(aabb -> aabb.expand(0.002, 0.002, 0.002).contains(hitVec))).findFirst();
        }
        if (g.isPresent()) {
            return g.get().getConnectionPoint();
        }
        return null;
    }

    @Override
    public boolean isPassiveConnector() {
        return true;
    }

    @Override
    public boolean addWire(GroundWire wire) {
        if (!wire.isTerminalPart() && bcfs.get(wire.getPlacement().ordinal())) {
            wire.setIsTerminal();
            return WorldHelper.getTileAt(getWorld(), getPos()).getCapability(TestMod.WIRE_CAPABILITY)
                    .map(wc -> wc.addWire(wire))
                    .orElse(false);
        }
        return false;
    }

    @Nullable
    @Override
    public GroundWire getWire(EnumFacing facing) {
        return null;
    }

    @Nonnull
    @Override
    public List<GroundWire> getWireView() {
        return ImmutableList.of();
    }

    @Override
    public boolean isRealWireContainer() {
        return false;
    }

}
