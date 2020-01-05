package elec332.eflux2.client.wire;

import com.google.common.collect.ImmutableMap;
import elec332.core.ElecCore;
import elec332.core.api.annotations.StaticLoad;
import elec332.core.api.client.model.ModelLoadEvent;
import elec332.core.client.RenderHelper;
import elec332.core.loader.client.RenderingRegistry;
import elec332.core.util.FMLHelper;
import elec332.eflux2.wire.terminal.GroundTerminal;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.DyeColor;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.BasicState;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.ITransformation;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Elec332 on 12-2-2019
 */
@StaticLoad
public class TerminalRenderer {

    private TerminalRenderer() {
        int count = 5;

        mrl = new ModelResourceLocation[count];
        for (int i = 0; i < count; i++) {
            mrl[i] = new ModelResourceLocation("eflux2" + ":wireterm" + (i + 1), "normal");
            RenderingRegistry.instance().registerLoadableModel(mrl[i]);
        }
        models = new IBakedModel[count];

        FMLHelper.getFMLModContainer(FMLHelper.getModContainer(ElecCore.instance)).getEventBus().register(this);
    }

    private ModelResourceLocation[] mrl;
    public static IBakedModel[] models;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void afterAllModelsBaked(ModelLoadEvent event) throws Exception {
        for (int i = 0; i < mrl.length; i++) {
            //models[i] = event.getModel(mrl[i]);
            IModel model = ModelLoaderRegistry.getModel(new ResourceLocation(mrl[i].getNamespace(), mrl[i].getPath()));
            model = model.retexture(ImmutableMap.<String, String>builder().put("missing", "eleccore:blocks/normal").build());
            models[i] = model.bake(event.getModelLoader(), ModelLoader.defaultTextureGetter(), new BasicState(model.getDefaultState(), false), DefaultVertexFormats.BLOCK);
        }
    }

    public static void makeQuads(List<BakedQuad> ret, Direction side, Iterable<GroundTerminal> terminals, boolean item) {
        //float[] f = {4, 16 / 3f, 8, 16};

        for (GroundTerminal gt : terminals) {
            int size = gt.getSize();
            float scale = (4 / 16f) * (size + 2.6f);
            //float oi6 = f[size] / 16f;//4 / 16f;
            Direction ef = gt.getSide();
            int x = ef.getAxis() == Direction.Axis.Z ? 180 - (90 * ef.getAxisDirection().getOffset()) : ef == Direction.UP ? 180 : 0;
            int z = ef.getAxis() == Direction.Axis.X ? 180 - (90 * ef.getAxisDirection().getOffset()) : 0;
            ITransformation placementTransformation = RenderHelper.getTransformation(x, 0, z);

            //Vector3f offset = new Vector3f(i % (4 - size) * oi6, 0, i / (4 - size) * oi6);
            Vector3f offset = new Vector3f(-0.5f * scale, 0, -0.5f * scale);
            Vec3d loc = gt.getLocation();
            switch (ef.getOpposite()) { //todo: Less eye-hurting here plz
                case SOUTH:
                    loc = loc.add(0, -1, 0);
                    break;
                case NORTH:
                    loc = loc.add(0, 0, -1);
                    break;
                case WEST:
                    loc = loc.add(-1, 0, 0);
                    break;
                case EAST:
                    loc = loc.add(0, -1, 0);
                    break;
                case DOWN:
                    loc = loc.add(0, -1, -1);
            }
            //offset.add((float) loc.x, (float) loc.y, (float) loc.z);
            IBakedModel model = models[4];//gt.getSize()];
            for (BakedQuad quad : model.getQuads(null, side, new Random())) {
                int[] vtxCopy = Arrays.copyOf(quad.getVertexData(), quad.getVertexData().length);
                for (int j = 0; j < 4; j++) {
                    int stIdx = j * 7;
                    Vector3f vec = new Vector3f(Float.intBitsToFloat(quad.getVertexData()[stIdx]), Float.intBitsToFloat(quad.getVertexData()[stIdx + 1]), Float.intBitsToFloat(quad.getVertexData()[stIdx + 2]));
                    vec.mul(scale);
                    vec.add(offset.getX(), offset.getY(), offset.getZ()); //todo: start here with fixing...
                    net.minecraftforge.client.ForgeHooksClient.transform(vec, placementTransformation.getMatrixVec());
                    if (item) {
                        vec.mul(1.4f);
                    }
                    vec.add((float) loc.x, (float) loc.y, (float) loc.z);
                    vtxCopy[stIdx] = Float.floatToRawIntBits(vec.getX());
                    vtxCopy[stIdx + 1] = Float.floatToRawIntBits(vec.getY());
                    vtxCopy[stIdx + 2] = Float.floatToRawIntBits(vec.getZ());
                }
                int clr = quad.getTintIndex();
                if (clr != -1) {
                    DyeColor cl = gt.getColor();
                    if (cl != null) {
                        clr = cl.getTextColor();
                    } else {
                        clr = -1;
                    }
                }
                ret.add(new BakedQuad(vtxCopy, clr, quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting(), quad.getFormat()));
            }
        }
    }

    static {
        new TerminalRenderer();
    }

}
