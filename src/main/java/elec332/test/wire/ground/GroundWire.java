package elec332.test.wire.ground;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import elec332.core.util.EnumBitSet;
import elec332.core.util.HitboxHelper;
import elec332.core.world.WorldHelper;
import elec332.test.TestMod;
import elec332.test.api.TestModAPI;
import elec332.test.api.electricity.IElectricityDevice;
import elec332.test.api.electricity.IEnergyObject;
import elec332.test.api.util.ConnectionPoint;
import elec332.test.api.wire.EnumWireThickness;
import elec332.test.api.wire.WireConnectionMethod;
import elec332.test.item.ItemGroundWire;
import elec332.test.util.EnumConnectionPlace;
import elec332.test.util.WireFacingHelper;
import elec332.test.wire.AbstractWire;
import elec332.test.wire.EnumWireType;
import elec332.test.wire.WireColorHelper;
import elec332.test.wire.WireData;
import elec332.test.wire.ground.tile.IWireContainer;
import elec332.test.wire.ground.tile.SubTileWire;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 1-2-2019
 */
public class GroundWire {

    public GroundWire(EnumFacing placement, int size) {
        this.placement = placement;
        this.size = size;

        this.connections = EnumBitSet.noneOf(EnumFacing.class);
        this.connections_ = Collections.unmodifiableSet(connections);

        setShape();

        wireDataBase = new WireData(EnumWireType.TEST, getWireThickness(size), WireConnectionMethod.GROUND);
    }

    //Internal use only!
    public SubTileWire wire;

    private EnumFacing placement;
    private int size;
    private int colors = 0;
    private VoxelShape shape, smallShape, extendedShape;
    private WireData wireDataBase;
    private boolean cChange;
    private boolean isTerminal;

    private EnumBitSet<EnumFacing> connections;
    private Set<EnumFacing> connections_;
    private EnumConnectionPlace[] cps = new EnumConnectionPlace[4];
    private int[] wireConnections = new int[4];
    private BitSet extended = new BitSet(4);
    private BitSet shortened = new BitSet(4);
    private BitSet otherMachines = new BitSet(4);
    private Set<IEnergyObject> internalWires = Sets.newHashSet();

    @OnlyIn(Dist.CLIENT)
    public int getClientColors(EnumFacing side) {
        return wireConnections[WireFacingHelper.getIndexFromHorizontalFacing(side)];
    }

    public boolean isExtended(EnumFacing side) {
        return extended.get(WireFacingHelper.getIndexFromHorizontalFacing(side));
    }

    public boolean isShortened(EnumFacing side) {
        return shortened.get(WireFacingHelper.getIndexFromHorizontalFacing(side));
    }

    public boolean isOtherMachine(EnumFacing side) {
        return otherMachines.get(WireFacingHelper.getIndexFromHorizontalFacing(side));
    }

    @OnlyIn(Dist.CLIENT)
    public Set<EnumFacing> getClientConnections() {
        return connections;
    }

    public Set<EnumFacing> getHorizontalConnections() {
        return connections_;
    }

    public EnumFacing getPlacement() {
        return placement;
    }

    public int getWireSize() {
        return size;
    }

    public int getWireAmount() {
        return Integer.bitCount(getColorBits());
    }

    public int getColorBits() {
        return colors;
    }

    public boolean hasWire(EnumDyeColor color) {
        return WireColorHelper.hasWire(color, getColorBits());
    }

    public int getMaxWires() {
        return getMaxWires(size);
    }

    public void setIsTerminal(){
        this.isTerminal = true;
    }

    public boolean isTerminalPart() {
        return isTerminal;
    }

    public boolean isCheckSide(EnumFacing facing) {
        return WireFacingHelper.isCheckSide(getPlacement(), WireFacingHelper.getRealSide(getPlacement(), facing)) || isOtherMachine(facing);
    }

    public ItemStack getDropStack() {
        return ItemGroundWire.withCables(WireColorHelper.getColors(getColorBits()), size);
    }

    public boolean canStay(World world, BlockPos pos) {
        return canWireStay(world, pos, getPlacement());
    }

    public static boolean canWireStay(World world, BlockPos pos, EnumFacing placement) {
        return WorldHelper.getBlockState(world, pos.offset(placement)).getBlockFaceShape(world, pos.offset(placement), placement.getOpposite()) == BlockFaceShape.SOLID;
    }

    public Set<IEnergyObject> getInternalWires() {
        return internalWires;
    }

    public boolean addColors(Pair<Integer, Integer> data) {
        return addColors(data.getLeft(), data.getRight());
    }

    private boolean addColors(int otherSize, int wireData) {
        if (otherSize != size || isTerminal) {
            return false;
        }
        Set<EnumDyeColor> colors = Sets.newHashSet(WireColorHelper.getColors(wireData));
        if (colors.size() != Integer.bitCount(wireData) || colors.size() + getWireAmount() > getMaxWires()) {
            return false;
        }
        for (EnumDyeColor color : colors) {
            if (hasWire(color)) {
                return false;
            }
        }

        BlockPos pos = wire.getPos();
        World world = Preconditions.checkNotNull(wire.getWorld());

        VoxelShape block = VoxelShapes.combineAndSimplify(WorldHelper.getBlockState(world, pos).getShape(world, pos), getShape(), IBooleanFunction.ONLY_FIRST);
        VoxelShape newWire = getBaseShape(ImmutableList.of(), true, getColorBits() | wireData, false);
        if (VoxelShapes.compare(block, newWire, IBooleanFunction.AND)) {
            return false;
        }

        colors.forEach(c -> addColorInternal(c, false));
        setShape();
        fullSync();
        cChange = true;
        if (wire != null) {
            wire.getWorld().neighborChanged(wire.getPos(), TestMod.WIRE_MARKER, wire.getPos());
        }
        return true;
    }

    public boolean setColors(List<EnumDyeColor> colors) {
        boolean ret = false;
        this.colors = 0;
        for (EnumDyeColor color : colors) {
            ret |= !addColorInternal(color, false);
        }
        return !ret;
    }

    public VoxelShape getShape() {
        return getShape(false);
    }

    public VoxelShape getShape(boolean onlySmallMiddle) {
        return !onlySmallMiddle ? shape : smallShape;
        //return getBaseShape(getHorizontalConnections(), false, onlySmallMiddle);
    }

    public VoxelShape getExtendedShape() {
        return extendedShape;
        //return getBaseShape(getHorizontalConnections(), true, false);
    }

    public void checkConnections(BlockPos pos_, World world, Function<BlockPos, Pair<Boolean, IWireContainer>> wireGetter) {
        if (world.isRemote) {
            return;
        }

        boolean intCh = cChange;
        cChange = false;
        EnumBitSet<EnumFacing> newConnections = EnumBitSet.noneOf(EnumFacing.class);
        int[] newWireConnections = new int[4];
        BitSet newExtended = new BitSet(4);
        BitSet newShortened = new BitSet(4);
        BitSet newOtherMachines = new BitSet(4);
        EnumConnectionPlace[] newCps = new EnumConnectionPlace[4];

        //System.out.println(pos_ + "  "+ getPlacement());

        for (int i = 0; i < 4; i++) {
            EnumFacing facing = WireFacingHelper.getRealSide(getPlacement(), i);
            EnumFacing iF = WireFacingHelper.getSideFromHorizontalIndex(i);
            for (EnumConnectionPlace cp : EnumConnectionPlace.values()) {
                Pair<BlockPos, EnumFacing> pbf = cp.modify(world, pos_, this, facing);
                if (pbf == null) {
                    continue;
                }
                BlockPos pos = pbf.getLeft();
                EnumFacing otherPlacement = pbf.getRight();
                if (WorldHelper.chunkLoaded(world, pos)) {
                    Pair<Boolean, IWireContainer> data = wireGetter.apply(pos);
                    if (data == null) {
                        continue;
                    }
                    if (data.getRight() == null) {
                        if (cp != EnumConnectionPlace.CORNER_UP) {
                            TileEntity tile = WorldHelper.getTileAt(world, pos);
                            if (tile != null) {
                                LazyOptional<IElectricityDevice> ed = tile.getCapability(TestModAPI.ELECTRICITY_CAP);
                                if (ed.isPresent()) {
                                    IElectricityDevice ied = ed.orElseThrow(NullPointerException::new);
                                    boolean b = ied.getInternalComponents().stream().anyMatch(eio -> WireColorHelper.getColors(getColorBits()).stream().anyMatch(clr -> eio.canConnectTo(wireDataBase.copy(clr))));
                                    if (b) {
                                        newConnections.add(iF);
                                        newCps[i] = cp;
                                        newWireConnections[i] = getColorBits();
                                        if (cp == EnumConnectionPlace.CORNER_DOWN) {
                                            newExtended.set(i);
                                        }
                                        newOtherMachines.set(i);
                                        break;
                                    }
                                }
                            }
                        }
                        continue;
                    }
                    if (data.getLeft()) {
                        if (getHorizontalConnections().contains(iF) && cps[i] == cp) {
                            //System.out.println("Skipping existing: " + iF + " " + cp);
                            newConnections.add(iF);
                            newWireConnections[i] = wireConnections[i];
                            newExtended.set(i, extended.get(i));
                            newShortened.set(i, shortened.get(i));
                            newCps[i] = cps[i];
                            break;
                        }
                        if (!intCh) {
                            continue;
                        }
                    }

                    //System.out.println("Checking: " + iF + " " + cp + "  " + pos);
                    IWireContainer wireO = data.getRight();
                    if (wireO != null) {
                        GroundWire oWp = wireO.getWire(otherPlacement);
                        //not i > 0, as we use all 32 bits, so the number can be negative, as the last bit is the negativity bit
                        if (oWp != null && oWp.getWireSize() == getWireSize() && (getColorBits() & oWp.getColorBits()) != 0) {
                            EnumFacing to = WireFacingHelper.getRealSide(oWp.getPlacement(), i).getOpposite();
                            if (cp == EnumConnectionPlace.CORNER_UP) {
                                to = getPlacement();
                            }
                            if (cp == EnumConnectionPlace.CORNER_DOWN) {
                                to = getPlacement().getOpposite();
                            }
                            Pair<BlockPos, EnumFacing> pbfo = cp.modify(world, pos, oWp, to);
                            if (pbfo == null) {
                                continue;
                            }
                            newConnections.add(iF);
                            newCps[i] = cp;
                            newWireConnections[i] = oWp.getColorBits();//(getColorBits() | oWp.getColorBits());
                            if (cp == EnumConnectionPlace.CORNER_DOWN) {
                                newExtended.set(i);
                            } else if (cp == EnumConnectionPlace.CORNER_UP) {
                                newShortened.set(i);
                            }
                            break;
                        }
                    }

                }
            }
        }

        boolean sync = false;
        if (!newConnections.equals(connections) || !Arrays.equals(wireConnections, newWireConnections) || !extended.equals(newExtended) || !shortened.equals(newShortened) || !Arrays.equals(cps, newCps) || !otherMachines.equals(newOtherMachines)) {
            sync = true;
        }

        connections = newConnections;
        this.connections_ = Collections.unmodifiableSet(connections);
        wireConnections = newWireConnections;
        extended = newExtended;
        shortened = newShortened;
        cps = newCps;
        otherMachines = newOtherMachines;
        if (sync) {
            wire.syncWireData();
        }
        setInternalWires();
        setShape();
        wire.resetElectricityCapability();
    }

    //Todo: Make this not hurt my eyes
    private void setInternalWires() {
        if (wire == null) {
            internalWires = ImmutableSet.of();
        }
        if (extended.isEmpty() && shortened.isEmpty()) {
            if (WireFacingHelper.isStraightLine(getHorizontalConnections())) {
                EnumFacing[] ef = getHorizontalConnections().toArray(new EnumFacing[0]);
                EnumFacing start = WireFacingHelper.getRealSide(getPlacement(), ef[0]);
                EnumFacing end = WireFacingHelper.getRealSide(getPlacement(), ef[1]);
                internalWires = WireColorHelper.getColors(colors).stream().map(c -> new InternalWire(this, c, c.getId(), start, end)).collect(Collectors.toSet());
            } else {
                internalWires = getHorizontalConnections().stream().flatMap(f -> {
                    EnumFacing start = WireFacingHelper.getRealSide(getPlacement(), f);
                    return WireColorHelper.getColors(colors).stream().map(c -> new InternalWire(this, c, c.getId(), start, null));
                }).collect(Collectors.toSet());
            }
        } else {
            if (WireFacingHelper.isStraightLine(getHorizontalConnections())) {
                EnumFacing[] ef = getHorizontalConnections().toArray(new EnumFacing[0]);
                EnumFacing start = ef[0];
                EnumFacing end = ef[1];
                ConnectionPoint s;
                ConnectionPoint e;

                BlockPos pos = wire.getPos();
                EnumFacing real = WireFacingHelper.getRealSide(getPlacement(), start);
                EnumFacing edge = getPlacement();
                if (isCheckSide(start)) {
                    if (isExtended(start)) {
                        pos = pos.offset(real);
                        edge = real.getOpposite();
                        real = getPlacement();
                    } else if (isShortened(start)) {
                        edge = real;
                        real = getPlacement();
                    }
                }
                s = new ConnectionPoint(pos, wire.getWorld(), real, -1, edge);

                pos = wire.getPos();
                real = WireFacingHelper.getRealSide(getPlacement(), end);
                edge = getPlacement();
                if (isCheckSide(end)) {
                    if (isExtended(end)) {
                        pos = pos.offset(real);
                        edge = real.getOpposite();
                        real = getPlacement();
                    } else if (isShortened(end)) {
                        edge = real;
                        real = getPlacement();
                    }
                }
                e = new ConnectionPoint(pos, wire.getWorld(), real, -1, edge);

                internalWires = WireColorHelper.getColors(colors).stream().map(c -> new InternalWire(s.copy(c.getId()), e.copy(c.getId()), this, c)).collect(Collectors.toSet());
            } else {
                ConnectionPoint middle = new ConnectionPoint(wire.getPos(), wire.getWorld(), null, -1, getPlacement());
                internalWires = getHorizontalConnections().stream().flatMap(start -> {
                    BlockPos pos = wire.getPos();
                    EnumFacing real = WireFacingHelper.getRealSide(getPlacement(), start);
                    EnumFacing edge = getPlacement();
                    if (isCheckSide(start)) {
                        if (isExtended(start)) {
                            pos = pos.offset(real);
                            edge = real.getOpposite();
                            real = getPlacement();
                        } else if (isShortened(start)) {
                            edge = real;
                            real = getPlacement();
                        }
                    }
                    ConnectionPoint s = new ConnectionPoint(pos, wire.getWorld(), real, -1, edge);
                    return WireColorHelper.getColors(colors).stream().map(c -> new InternalWire(s.copy(c.getId()), middle.copy(c.getId()), this, c));
                }).collect(Collectors.toSet());
            }
        }
    }

    private void setShape() {
        if (getColorBits() == 0) {
            shape = VoxelShapes.empty();
        }
        shape = getBaseShape(getHorizontalConnections(), false, false);
        smallShape = getBaseShape(getHorizontalConnections(), false, true);
        extendedShape = getBaseShape(getHorizontalConnections(), true, false);
    }

    private VoxelShape getBaseShape(Collection<EnumFacing> connections, boolean forceMiddle, boolean onlySmallMiddle) {
        return getBaseShape(connections, forceMiddle, getColorBits(), onlySmallMiddle);
    }

    private VoxelShape getBaseShape(Collection<EnumFacing> connections, boolean forceMiddle, int colors, boolean onlySmallMiddle) {
        return getBaseShape(connections, forceMiddle, colors, this::isExtended, this::isShortened, onlySmallMiddle);
    }

    public VoxelShape getBaseShape(Collection<EnumFacing> connections, boolean forceMiddle, int colors, Predicate<EnumFacing> extended_, Predicate<EnumFacing> shortened_, boolean onlySmallMiddle) {
        List<VoxelShape> parts = Lists.newArrayList();
        float width = Integer.bitCount(colors) * size;
        float stuff = ((16 - width) / 2) / 16;
        if (!isTerminalPart() && (connections.size() != 1 && !WireFacingHelper.isStraightLine(connections) || forceMiddle || onlySmallMiddle)) {
            float stuff_ = stuff;
            if (!onlySmallMiddle) {
                stuff_ -= 1 / 16f;
            }
            parts.add(VoxelShapes.create(stuff_, 0, stuff_, 1 - stuff_, (1.1f * size) / 16, 1 - stuff_));
        }
        if (!onlySmallMiddle) {
            for (EnumFacing facing : connections) {
                boolean z = facing.getAxis() == EnumFacing.Axis.Z;
                boolean n = facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
                float one = 1, zero = 0;
                boolean extended = extended_.test(facing);
                if (isCheckSide(facing) && (extended || shortened_.test(facing))) {
                    float os = size / 16f;
                    if (!extended) {
                        os = -os;
                    }
                    one += os;
                    zero -= os;
                }
                if (z) {//North South
                    parts.add(VoxelShapes.create(1 - stuff, 0, 0.5f, stuff, size / 16f, n ? zero : one));
                } else {
                    parts.add(VoxelShapes.create(0.5f, 0, 1 - stuff, n ? zero : one, size / 16f, stuff));
                }
            }
        }
        return HitboxHelper.rotateFromDown(HitboxHelper.combineShapes(parts), getPlacement());
    }

    private boolean addColorInternal(EnumDyeColor color, boolean notify) {
        if (hasWire(color)) {
            return false;
        }
        if (Integer.bitCount(getColorBits()) >= getMaxWires(size)) {
            return false;
        }
        colors = WireColorHelper.addWire(color, getColorBits());
        if (notify) {
            fullSync();
        }
        setShape();
        return true;
    }

    private void fullSync() {
        if (wire != null) {
            wire.resetWireCapability();
            wire.notifyNeighborsOfChangeExtensively();
            wire.syncWireData();
            wire.resetElectricityCapability();
        }
    }

    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.putInt("wrclr", getColorBits());
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        int clr = compound.getInt("wrclr");
        if (clr != this.colors && wire != null) {
            wire.resetWireCapability();
            wire.resetElectricityCapability();
        }
        this.colors = clr;
        setShape();
    }

    public void writeClientData(NBTTagCompound tag) {
        tag.putLong("conn", connections.getSerialized());
        tag.putIntArray("wireColr", wireConnections);
        tag.putByteArray("extCon", extended.toByteArray());
        tag.putByteArray("shoCon", shortened.toByteArray());
        tag.putByteArray("othMac", otherMachines.toByteArray());

        //System.out.println("sendCient " + wire.getPos() + "  " + getPlacement());
    }

    @OnlyIn(Dist.CLIENT)
    public void readClientData(NBTTagCompound tag) {
        connections.deserialize(tag.getLong("conn"));
        wireConnections = tag.getIntArray("wireColr");
        extended = BitSet.valueOf(tag.getByteArray("extCon"));
        shortened = BitSet.valueOf(tag.getByteArray("shoCon"));
        otherMachines = BitSet.valueOf(tag.getByteArray("othMac"));
        setShape();
        //System.out.println("receiuveClient "+(wire != null ? wire.getPos() : "nup" + "  " + getPlacement()));
    }

    public static int getMaxWires(int size) {
        switch (size) {
            case 4:
                return 2;
            case 3:
                return 3;
            default:
                return 12 / size;
        }
    }

    private static EnumWireThickness getWireThickness(int size) {
        switch (size) {
            case 4:
                return EnumWireThickness.AWG_0;
            case 3:
                return EnumWireThickness.AWG_7;
            case 2:
                return EnumWireThickness.AWG_15;
            default:
                return EnumWireThickness.AWG_24;
        }
    }

    //test
    public double a[] = new double[EnumDyeColor.values().length], v[] = new double[EnumDyeColor.values().length];

    private class InternalWire extends AbstractWire {

        @SuppressWarnings("all")
        private InternalWire(GroundWire wire, EnumDyeColor color, int sideNumber, EnumFacing sideStart, EnumFacing sideEnd) {
            this(new ConnectionPoint(wire.wire.getPos(), wire.wire.getWorld(), sideStart, sideNumber, wire.placement), new ConnectionPoint(wire.wire.getPos(), wire.wire.getWorld(), sideEnd, sideNumber, wire.placement), wire, color);
        }

        private InternalWire(ConnectionPoint start, ConnectionPoint end, GroundWire wire, EnumDyeColor color) {
            super(start, end, wire.wireDataBase.copy(color));
            length = (start.getSide() == null || end.getSide() == null) ? 0.5 : 1;
        }

        private final double length;

        @Override
        public double getResistance() {
            return wireData.getResistivity(length);
        }

        @Override
        public void setPowerTest(double v, double a) {
            GroundWire.this.a[wireData.getColor().getId()] = a;
            GroundWire.this.v[wireData.getColor().getId()] = v;
        }

    }

}
