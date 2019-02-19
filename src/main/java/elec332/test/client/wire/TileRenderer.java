package elec332.test.client.wire;

import elec332.core.api.annotations.StaticLoad;
import elec332.core.client.RenderHelper;
import elec332.core.loader.client.RenderingRegistry;
import elec332.core.world.WorldHelper;
import elec332.test.TestMod;
import elec332.test.tile.TileMultiObject;
import elec332.test.wire.ground.GroundWire;
import elec332.test.wire.terminal.GroundTerminal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by Elec332 on 1-2-2019
 */
@StaticLoad
@OnlyIn(Dist.CLIENT)
public class TileRenderer extends TileEntityRenderer<TileMultiObject> {
    static {
        RenderingRegistry.instance().registerLoader(new WireRenderer());
    }

    @Override
    public void render(TileMultiObject tileEntityIn, double x_, double y_, double z_, float partialTicks, int destroyStage) {
        //ForgeMod.forgeLightPipelineEnabled = false;
        GlStateManager.pushMatrix();
        if (tileEntityIn != null) {
            GlStateManager.translated(x_, y_, z_);
            BlockPos pos = tileEntityIn.getPos();
            GlStateManager.translated(-pos.getX(), -pos.getY(), -pos.getZ());
            List<GroundWire> wirez = tileEntityIn.getCapability(TestMod.WIRE_CAPABILITY).orElseThrow(NullPointerException::new).getWireView();
            Collection<GroundTerminal> terminalz = tileEntityIn.getCapability(TestMod.TERMINAL_CAPABILITY).orElseThrow(NullPointerException::new).getTerminalView();
            Tessellator.getInstance().getBuffer().begin(7, DefaultVertexFormats.BLOCK);
            RenderHelper.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            WireModelCache.WireRenderData key = new WireModelCache.WireRenderData(wirez, terminalz);
            Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(tileEntityIn.getWorld(), TestMod.model.getModel(key), WorldHelper.getBlockState(tileEntityIn.getWorld(), pos), pos, Tessellator.getInstance().getBuffer(), false, new Random(), 0L);
            Tessellator.getInstance().draw();
        }
        GlStateManager.popMatrix();
        super.render(tileEntityIn, x_, y_, z_, partialTicks, destroyStage);
    }

}
