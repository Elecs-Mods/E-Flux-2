package elec332.eflux2.client.wire;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import elec332.core.client.model.ModelCache;
import elec332.core.world.WorldHelper;
import elec332.eflux2.EFlux2;
import elec332.eflux2.client.ModelProperties;
import elec332.eflux2.item.ItemGroundTerminal;
import elec332.eflux2.item.ItemGroundWire;
import elec332.eflux2.register.EFlux2ItemRegister;
import elec332.eflux2.wire.ground.GroundWire;
import elec332.eflux2.wire.terminal.GroundTerminal;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * Created by Elec332 on 3-2-2019
 */
@OnlyIn(Dist.CLIENT)
public class WireModelCache extends ModelCache<WireModelCache.WireRenderData> {

    public static final WireModelCache instance = new WireModelCache();

    private WireModelCache() {
    }

    @Override
    protected boolean needsModelData() {
        return true;
    }

    @Override
    protected WireRenderData get(BlockState state, IModelData modelState) {
        return new WireModelCache.WireRenderData(modelState.getData(ModelProperties.WIRE), modelState.getData(ModelProperties.TERMINAL));
    }

    @Override
    public void addModelData(@Nonnull IEnviromentBlockReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData modelData) {
        TileEntity tile = WorldHelper.getTileAt(world, pos);
        modelData.setData(ModelProperties.WIRE, tile.getCapability(EFlux2.WIRE_CAPABILITY).orElseThrow(NullPointerException::new).getWireView());
        modelData.setData(ModelProperties.TERMINAL, tile.getCapability(EFlux2.TERMINAL_CAPABILITY).orElseThrow(NullPointerException::new).getTerminalView());
    }

    @Override
    protected WireRenderData get(ItemStack stack) {
        Item item = stack.getItem();
        if (item == EFlux2ItemRegister.wire) {
            return new WireRenderData(ItemGroundWire.getColorsFromStack(stack));
        } else {
            Pair<Integer, DyeColor> data = ItemGroundTerminal.getDataFromStack(stack);
            return new WireRenderData(data.getLeft(), data.getRight());
        }
    }

    @Override
    protected void bakeQuads(List<BakedQuad> quads, Direction side, WireRenderData key) {
        if (!key.wires.isEmpty()) {
            WireRenderer.makeQuads(quads, key.wires, key.item);
        }
        if (!key.terminals.isEmpty()) {
            TerminalRenderer.makeQuads(quads, side, key.terminals, key.item);
        }
    }

    @Nonnull
    @Override
    protected ResourceLocation getTextureLocation() {
        return new ResourceLocation("eleccore", "blocks/black");
    }

    public static class WireRenderData {

        @SuppressWarnings("all")
        public WireRenderData(Collection<GroundWire> wires, Collection<GroundTerminal> terminals) {
            this.wires = Preconditions.checkNotNull(wires);
            this.item = false;
            this.terminals = Preconditions.checkNotNull(terminals);
        }

        private WireRenderData(Pair<Integer, List<DyeColor>> data) {
            wires = Lists.newArrayList();
            Preconditions.checkNotNull(data);
            GroundWire wire = new GroundWire(Direction.DOWN, data.getLeft()) { //Fix equality to make cache size not explode and omnom 1GB ram/min

                @Override
                public boolean equals(Object obj) {
                    return obj == this || (obj instanceof GroundWire && ((GroundWire) obj).getClientConnections().equals(getClientConnections()) && ((GroundWire) obj).getColorBits() == getColorBits());
                }

                @Override
                public int hashCode() {
                    return getColorBits() * 31 + getClientConnections().hashCode();
                }

            };
            wires.add(wire);
            wire.getClientConnections().add(Direction.NORTH);
            wire.getClientConnections().add(Direction.SOUTH);
            wire.setColors(data.getRight());
            this.item = true;
            this.terminals = ImmutableList.of();
        }

        private WireRenderData(int i, DyeColor color) {
            int q = color == null ? -1 : color.hashCode();
            this.terminals = Lists.newArrayList(new GroundTerminal(Direction.DOWN, i, new Vec3d(0.5, 0, 0.5), color) { //More RAM omnomming prevention

                @Override
                public boolean equals(Object obj) {
                    return obj == this || (obj instanceof GroundTerminal && ((GroundTerminal) obj).getLocation().equals(getLocation()) && getColor() == ((GroundTerminal) obj).getColor() && getSize() == ((GroundTerminal) obj).getSize() && getSide() == ((GroundTerminal) obj).getSide());
                }

                @Override
                public int hashCode() {
                    return getLocation().hashCode() + 31 * q + 31 * (31 * i + getSide().ordinal());
                }

            });
            this.item = true;
            this.wires = ImmutableList.of();
        }

        private final Collection<GroundTerminal> terminals;
        private final Collection<GroundWire> wires;
        private final boolean item;

        @Override
        public boolean equals(Object obj) {
            return obj == this || (obj instanceof WireRenderData && ((WireRenderData) obj).item == item && ((WireRenderData) obj).wires.equals(wires) && ((WireRenderData) obj).terminals.equals(terminals));
        }

        @Override
        public int hashCode() {
            return (item ? 0 : 31) + wires.hashCode() + terminals.hashCode() * 3;
        }

    }

}
