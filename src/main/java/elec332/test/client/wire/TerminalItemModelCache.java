package elec332.test.client.wire;

import com.google.common.collect.Lists;
import elec332.core.client.model.ModelCache;
import elec332.test.item.ItemGroundTerminal;
import elec332.test.wire.terminal.GroundTerminal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by Elec332 on 13-2-2019
 */
public class TerminalItemModelCache extends ModelCache<Pair<Integer, EnumDyeColor>> {

    @Override
    protected Pair<Integer, EnumDyeColor> get(IBlockState state) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Pair<Integer, EnumDyeColor> get(ItemStack stack) {
        return ItemGroundTerminal.getDataFromStack(stack);
    }

    @Override
    protected void bakeQuads(List<BakedQuad> quads, EnumFacing side, Pair<Integer, EnumDyeColor> key) {
        TerminalRenderer.getTerminalQuads(quads, side, Lists.newArrayList(new GroundTerminal(EnumFacing.DOWN, key.getLeft(), new Vec3d(0.5, 0, 0.5), key.getRight())), true);
    }

    @Nonnull
    @Override
    protected ResourceLocation getTextureLocation() {
        return new ResourceLocation("eleccore", "blocks/black");
    }

}
