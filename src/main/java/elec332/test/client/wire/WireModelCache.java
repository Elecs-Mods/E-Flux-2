package elec332.test.client.wire;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import elec332.core.client.model.ModelCache;
import elec332.test.item.ItemGroundWire;
import elec332.test.wire.ground.GroundWire;
import elec332.test.wire.terminal.GroundTerminal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * Created by Elec332 on 3-2-2019
 */
@OnlyIn(Dist.CLIENT)
public class WireModelCache extends ModelCache<WireModelCache.WireRenderData> {

    @Override
    protected WireRenderData get(IBlockState state) {
        return null;
    }

    @Override
    protected WireRenderData get(ItemStack stack) {
        return new WireRenderData(ItemGroundWire.getColorsFromStack(stack));
    }

    @Override
    protected void bakeQuads(List<BakedQuad> quads, EnumFacing side, WireRenderData key) {
        WireRenderer.makeQuads(quads, key.wires, key.item);
        if (!key.terminals.isEmpty()) {
            TerminalRenderer.getTerminalQuads(quads, side, key.terminals, key.item);
        }
    }

    @Nonnull
    @Override
    protected ResourceLocation getTextureLocation() {
        return new ResourceLocation("eleccore", "blocks/black");
    }

    public static class WireRenderData {

        @SuppressWarnings("all")
        public WireRenderData(List<GroundWire> wires, Collection<GroundTerminal> terminals) {
            this.wires = wires;
            this.item = false;
            this.terminals = terminals;
        }

        private WireRenderData(Pair<Integer, List<EnumDyeColor>> data) {
            wires = Lists.newArrayList();
            GroundWire wire = new GroundWire(EnumFacing.DOWN, data.getLeft()) { //Fix equality to make cache size not explode and omnom 1GB ram/min

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
            wire.getClientConnections().add(EnumFacing.NORTH);
            wire.getClientConnections().add(EnumFacing.SOUTH);
            wire.setColors(data.getRight());
            this.item = true;
            this.terminals = ImmutableList.of();
        }

        private final Collection<GroundTerminal> terminals;
        private final List<GroundWire> wires;
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
