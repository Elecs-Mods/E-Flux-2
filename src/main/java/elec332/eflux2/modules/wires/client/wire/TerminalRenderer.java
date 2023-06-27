package elec332.eflux2.modules.wires.client.wire;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import elec332.core.ElecCore;
import elec332.core.api.annotations.StaticLoad;
import elec332.core.api.client.model.ModelLoadEvent;
import elec332.core.client.RenderHelper;
import elec332.core.loader.client.RenderingRegistry;
import elec332.core.util.FMLHelper;
import elec332.eflux2.modules.wires.wire.terminal.GroundTerminal;
import elec332.eflux2.util.EFlux2ResourceLocation;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.*;
import net.minecraft.item.DyeColor;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.QuadTransformer;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Random;

/**
 * Created by Elec332 on 12-2-2019
 */
@StaticLoad
public class TerminalRenderer {

    private TerminalRenderer() {
        RenderingRegistry.instance().registerModelLocation(mrl);
        FMLHelper.getFMLModContainer(FMLHelper.getModContainer(ElecCore.instance)).getEventBus().register(this);
    }

    private static  final ResourceLocation mrl = new EFlux2ResourceLocation("wire_terminal");

    private static final List<BakedQuad>[][] models;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void afterAllModelsBaked(ModelLoadEvent event) {
        final Material testMaterial = new Material(RenderHelper.getBlocksResourceLocation(), new ResourceLocation("eleccore:blocks/normal"));
        IUnbakedModel model = event.getModelLoader().getModelOrMissing(mrl);
        IBakedModel baseModel = model.bakeModel(event.getModelLoader(), material -> {
            if (material.getTextureLocation().getPath().equals("missing")) {
                material = testMaterial;
            }
            return ModelLoader.defaultTextureGetter().apply(material);
        }, ModelRotation.X0_Y0, mrl);
        Preconditions.checkNotNull(baseModel);
        for (Direction dir : Direction.values()) {
            List<BakedQuad>[] models = TerminalRenderer.models[dir.ordinal()];
            TransformationMatrix m = RenderHelper.merge(new TransformationMatrix(new Vector3f(-0.5f, 0, -0.5f), null, null, null), RenderHelper.rotateFromDown(dir)).getTransformaion();
            for (int size = 0; size < models.length; size++) {
                List<BakedQuad> quads = Lists.newArrayList();
                float scale = (4 / 16f) * (size + 2.6f);
                QuadTransformer t = new QuadTransformer(RenderHelper.merge(m, new TransformationMatrix(null, null, new Vector3f(scale, scale, scale), null)).getTransformaion());
                quads.addAll(t.processMany(baseModel.getQuads(null, null, new Random(), EmptyModelData.INSTANCE)));
                for (Direction modelSide : Direction.values()) {
                    quads.addAll(t.processMany(baseModel.getQuads(null, modelSide, new Random(), EmptyModelData.INSTANCE)));
                }
                models[size] = ImmutableList.copyOf(quads);
            }
        }
    }

    public static void makeQuads(List<BakedQuad> ret, Direction side, Iterable<GroundTerminal> terminals) {
        if (side != null) {
            return;
        }
        for (GroundTerminal gt : terminals) {
            TransformationMatrix matrix = new TransformationMatrix(new Vector3f(gt.getLocation()), null, null, null);
            QuadTransformer t = new QuadTransformer(matrix);
            models[gt.getSide().ordinal()][gt.getSize()]
                    .stream()
                    .map(quad -> {
                        int clr = quad.getTintIndex();
                        if (clr != -1) {
                            DyeColor cl = gt.getColor();
                            if (cl != null) {
                                clr = cl.getTextColor();
                            } else {
                                clr = -1;
                            }
                        }
                        return new BakedQuad(quad.getVertexData().clone(), clr, quad.getFace(), quad.func_187508_a(), quad.shouldApplyDiffuseLighting());
                    })
                    .peek(t::processOneInPlace)
                    .forEach(ret::add);
        }
    }

    static {
        new TerminalRenderer();
        models = new List[Direction.values().length][4];
    }

}
