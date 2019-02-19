package elec332.test.client.wire;

import elec332.core.api.client.IIconRegistrar;
import elec332.core.api.client.model.IElecModelBakery;
import elec332.core.api.client.model.IElecQuadBakery;
import elec332.core.api.client.model.IElecTemplateBakery;
import elec332.core.api.client.model.IModelAndTextureLoader;
import elec332.core.client.RenderHelper;
import elec332.test.api.electricity.component.EnumElectricityType;
import elec332.test.util.TestModResourceLocation;
import elec332.test.util.WireFacingHelper;
import elec332.test.wire.WireColorHelper;
import elec332.test.wire.ground.GroundWire;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.model.ITransformation;
import net.minecraftforge.common.model.TRSRTransformation;

import javax.vecmath.Matrix4f;
import java.util.List;
import java.util.Set;

/**
 * Created by Elec332 on 3-2-2019
 */
public class WireRenderer implements IModelAndTextureLoader {

    private static TextureAtlasSprite black;
    private static IElecQuadBakery quadBakery;
    private static TextureAtlasSprite[] wireTypes;

    public static void makeQuads(List<BakedQuad> quads, Iterable<GroundWire> wires, boolean item) {
        for (GroundWire data : wires) {
            int size = data.getWireSize();
            EnumFacing ef = data.getPlacement();
            int x = ef.getAxis() == EnumFacing.Axis.Z ? 180 - (90 * ef.getAxisDirection().getOffset()) : ef == EnumFacing.UP ? 180 : 0;
            int z = ef.getAxis() == EnumFacing.Axis.X ? 180 - (90 * ef.getAxisDirection().getOffset()) : 0;
            ITransformation placementTransformation = RenderHelper.getTransformation(x, 0, z);
            List<EnumDyeColor> colors = WireColorHelper.getColors(data.getColorBits());
            Set<EnumFacing> conn = data.getHorizontalConnections();
            float posStart;
            int total = colors.size();
            float ft = (16 - (total * size + 2)) / 2f;
            for (EnumFacing facing : conn) {
                boolean isCheckSide = data.isCheckSide(facing);
                boolean neg = facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
                boolean change = data.getClientColors(facing) != data.getColorBits();
                boolean extend = data.isExtended(facing) && !change;
                boolean exnp = extend && isCheckSide;
                boolean shortened = data.isShortened(facing);
                boolean shrt = shortened && isCheckSide;
                int zero8 = neg ? (shrt ? size : 0) : (exnp ? 8 - size : 8);
                int eight16 = neg ? (exnp ? 8 + size : 8) : (shrt ? 16 - size : 16);
                int extStart = exnp ? -size : shrt ? size : 0;
                posStart = ft + 1;
                ITransformation baseTransformation = RenderHelper.getDefaultRotationFromFacing(facing);
                ITransformation placedBaseTransformation = merge(baseTransformation, placementTransformation);
                for (int i = 0; i < colors.size(); i++) {
                    boolean extraNeg = ef == EnumFacing.UP && facing.getAxis() == EnumFacing.Axis.X;
                    extraNeg |= ef == EnumFacing.EAST && facing.getAxis() == EnumFacing.Axis.Z;
                    extraNeg |= ef == EnumFacing.NORTH && facing.getAxis() == EnumFacing.Axis.X;
                    EnumDyeColor color = colors.get((extraNeg != neg) ? i : colors.size() - 1 - i);
                    TextureAtlasSprite wire = wireTypes[0];
                    if (i == 0) { //side
                        ITransformation i0T = merge(RenderHelper.getTransformation(0, 0, 90), baseTransformation);
                        quads.add(quadBakery.bakeQuad(new Vector3f(size, 16 - posStart, 8), new Vector3f(0, 16 - posStart, extStart), wire, EnumFacing.UP, merge(i0T, placementTransformation), color.ordinal() + 1, eight16, color.ordinal(), zero8));
                    }
                    if (conn.size() == 1 || item) { //end cap
                        quads.add(quadBakery.bakeQuad(new Vector3f(posStart, 0, 0), new Vector3f(posStart + size, size, item ? 16 : 8), wire, EnumFacing.SOUTH, placedBaseTransformation, color.ordinal(), 0.0F, color.ordinal() + 1, 2.0F));
                    }
                    if (data.isOtherMachine(facing)) { //Extra end-cap when connected to a machine
                        quads.add(quadBakery.bakeQuad(new Vector3f(posStart + size, 0, -size), new Vector3f(posStart, size, -size), wire, EnumFacing.SOUTH, placedBaseTransformation, color.ordinal(), 0.0F, color.ordinal() + 1, 2.0F));
                    }
                    for (int j = 1; j > (item ? -1 : 0); j--) { //top
                        quads.add(quadBakery.bakeQuad(new Vector3f(posStart, size * j, extend ? -size : (shortened ? size : 0)), new Vector3f(posStart + size, size * j, 8), wire, j == 1 ? EnumFacing.UP : EnumFacing.DOWN, placedBaseTransformation, color.ordinal(), neg ? (shortened ? size : 0) : (extend ? 8 - size : 8), color.ordinal() + 1, neg ? (extend ? 8 + size : 8) : (shortened ? 16 - size : 16)));
                    }
                    posStart += size;
                    if (i == colors.size() - 1) { //other side
                        ITransformation iCt = merge(RenderHelper.getTransformation(0, 180, 90), baseTransformation);
                        quads.add(quadBakery.bakeQuad(new Vector3f(size, posStart, 16 - extStart), new Vector3f(0, posStart, 8), wire, EnumFacing.UP, merge(iCt, placementTransformation), color.ordinal() + 1, eight16, color.ordinal(), zero8));
                    }
                }
                if (change && !item) {
                    extend = data.isExtended(facing);
                    int min11 = extend ? -1 : 1;
                    min11 *= (extend || shortened) ? size : 1;
                    quads.add(quadBakery.bakeQuad(new Vector3f(ft, 1.1f * size, 0), new Vector3f(16 - ft, 1.1f * size, 1.1f * min11), black, extend ? EnumFacing.DOWN : EnumFacing.UP, placedBaseTransformation));
                    if (extend) {
                        quads.add(quadBakery.bakeQuad(new Vector3f(ft, 0, 0), new Vector3f(16 - ft, 0, -1.1f), black, EnumFacing.UP, placedBaseTransformation));
                    }
                    for (EnumFacing f : EnumFacing.values()) {
                        if (f.getAxis() != EnumFacing.Axis.Y) {
                            quads.add(quadBakery.bakeQuad(new Vector3f(f == EnumFacing.EAST ? 16 - ft : ft, 0, f == EnumFacing.SOUTH ? (1.1f * min11) : 0), new Vector3f(f == EnumFacing.WEST ? ft : 16 - ft, 1.1f * size, f == EnumFacing.NORTH ? 0 : (1.1f * min11)), black, extend ? f.getOpposite() : f, placedBaseTransformation));
                        }
                    }
                }
            }

            if (conn.size() != 1 && !WireFacingHelper.isStraightLine(conn)) { //Middle blob
                quads.add(quadBakery.bakeQuad(new Vector3f(ft, 1.1f * size, ft), new Vector3f(16 - ft, 1.1f * size, 16 - ft), black, EnumFacing.UP, placementTransformation));
                for (EnumFacing facing : EnumFacing.values()) {
                    if (facing.getAxis() != EnumFacing.Axis.Y) {
                        quads.add(quadBakery.bakeQuad(new Vector3f(ft, 0, ft), new Vector3f(16 - ft, 1.1f * size, ft), black, EnumFacing.NORTH, merge(RenderHelper.getDefaultRotationFromFacing(facing), placementTransformation)));
                    }
                }
            }
        }
    }

    private static ITransformation merge(ITransformation first, ITransformation second) {
        Matrix4f m = new Matrix4f(second.getMatrixVec());
        m.mul(first.getMatrixVec());
        return new TRSRTransformation(m);
    }

    @Override
    public void registerTextures(IIconRegistrar iiconRegistrar) {
        wireTypes = new TextureAtlasSprite[EnumElectricityType.values().length];
        for (int i = 0; i < wireTypes.length; i++) {
            wireTypes[i] = iiconRegistrar.registerSprite(new TestModResourceLocation("blocks/flatwire_" + EnumElectricityType.values()[i].toString().toLowerCase()));
        }

        black = iiconRegistrar.registerSprite(new TestModResourceLocation("blocks/black"));
    }

    @Override
    public void registerModels(IElecQuadBakery quadBakery, IElecModelBakery modelBakery, IElecTemplateBakery templateBakery) {
        WireRenderer.quadBakery = quadBakery;
    }

}
