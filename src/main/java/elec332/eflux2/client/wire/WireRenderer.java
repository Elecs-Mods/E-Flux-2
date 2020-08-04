package elec332.eflux2.client.wire;

import elec332.core.api.annotations.StaticLoad;
import elec332.core.api.client.IIconRegistrar;
import elec332.core.api.client.model.IElecModelBakery;
import elec332.core.api.client.model.IElecQuadBakery;
import elec332.core.api.client.model.IElecTemplateBakery;
import elec332.core.api.client.model.IModelAndTextureLoader;
import elec332.core.client.RenderHelper;
import elec332.core.loader.client.RenderingRegistry;
import elec332.eflux2.api.electricity.component.EnumElectricityType;
import elec332.eflux2.util.EFlux2ResourceLocation;
import elec332.eflux2.util.WireFacingHelper;
import elec332.eflux2.wire.WireColorHelper;
import elec332.eflux2.wire.ground.GroundWire;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.DyeColor;
import net.minecraft.util.Direction;
import net.minecraftforge.client.extensions.IForgeTransformationMatrix;

import java.util.List;
import java.util.Set;

/**
 * Created by Elec332 on 3-2-2019
 */
@StaticLoad
public class WireRenderer implements IModelAndTextureLoader {

    static {
        RenderingRegistry.instance().registerLoader(new WireRenderer());
    }

    private static TextureAtlasSprite black;
    private static IElecQuadBakery quadBakery;
    private static TextureAtlasSprite[] wireTypes;

    public static void makeQuads(List<BakedQuad> quads, Iterable<GroundWire> wires, boolean item) {
        for (GroundWire data : wires) {
            int size = data.getWireSize();
            Direction ef = data.getPlacement();
            int x = ef.getAxis() == Direction.Axis.Z ? 180 - (90 * ef.getAxisDirection().getOffset()) : ef == Direction.UP ? 180 : 0;
            int z = ef.getAxis() == Direction.Axis.X ? 180 - (90 * ef.getAxisDirection().getOffset()) : 0;
            IForgeTransformationMatrix placementTransformation = RenderHelper.getTransformation(x, 0, z);
            List<DyeColor> colors = WireColorHelper.getColors(data.getColorBits());
            Set<Direction> conn = data.getHorizontalConnections();
            float posStart;
            int total = colors.size();
            float ft = (16 - (total * size + 2)) / 2f;
            for (Direction facing : conn) {
                boolean isCheckSide = data.isCheckSide(facing);
                boolean neg = facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
                boolean change = data.getClientColors(facing) != data.getColorBits();
                boolean extend = data.isExtended(facing) && !change;
                boolean exnp = extend && isCheckSide;
                boolean shortened = data.isShortened(facing);
                boolean shrt = shortened && isCheckSide;
                int zero8 = neg ? (shrt ? size : 0) : (exnp ? 8 - size : 8);
                int eight16 = neg ? (exnp ? 8 + size : 8) : (shrt ? 16 - size : 16);
                int extStart = exnp ? -size : shrt ? size : 0;
                posStart = ft + 1;
                IForgeTransformationMatrix baseTransformation = RenderHelper.getDefaultRotationFromFacing(facing).getRotation();
                IForgeTransformationMatrix placedBaseTransformation = RenderHelper.merge(baseTransformation, placementTransformation);
                for (int i = 0; i < colors.size(); i++) {
                    boolean extraNeg = ef == Direction.UP && facing.getAxis() == Direction.Axis.X;
                    extraNeg |= ef == Direction.EAST && facing.getAxis() == Direction.Axis.Z;
                    extraNeg |= ef == Direction.NORTH && facing.getAxis() == Direction.Axis.X;
                    DyeColor color = colors.get((extraNeg != neg) ? i : colors.size() - 1 - i);
                    TextureAtlasSprite wire = wireTypes[0];
                    if (i == 0) { //side
                        IForgeTransformationMatrix i0T = RenderHelper.merge(RenderHelper.getTransformation(0, 0, 90), baseTransformation);
                        quads.add(quadBakery.bakeQuad(new Vector3f(size, 16 - posStart, 8), new Vector3f(0, 16 - posStart, extStart), wire, Direction.UP, RenderHelper.merge(i0T, placementTransformation), color.ordinal() + 1, eight16, color.ordinal(), zero8));
                    }
                    if (conn.size() == 1 || item) { //end cap
                        quads.add(quadBakery.bakeQuad(new Vector3f(posStart, 0, 0), new Vector3f(posStart + size, size, item ? 16 : 8), wire, Direction.SOUTH, placedBaseTransformation, color.ordinal(), 0.0F, color.ordinal() + 1, 2.0F));
                    }
                    if (data.isOtherMachine(facing)) { //Extra end-cap when connected to a machine
                        quads.add(quadBakery.bakeQuad(new Vector3f(posStart + size, 0, -size), new Vector3f(posStart, size, -size), wire, Direction.SOUTH, placedBaseTransformation, color.ordinal(), 0.0F, color.ordinal() + 1, 2.0F));
                    }
                    for (int j = 1; j > (item ? -1 : 0); j--) { //top
                        quads.add(quadBakery.bakeQuad(new Vector3f(posStart, size * j, extend ? -size : (shortened ? size : 0)), new Vector3f(posStart + size, size * j, 8), wire, j == 1 ? Direction.UP : Direction.DOWN, placedBaseTransformation, color.ordinal(), neg ? (shortened ? size : 0) : (extend ? 8 - size : 8), color.ordinal() + 1, neg ? (extend ? 8 + size : 8) : (shortened ? 16 - size : 16)));
                    }
                    posStart += size;
                    if (i == colors.size() - 1) { //other side
                        IForgeTransformationMatrix iCt = RenderHelper.merge(RenderHelper.getTransformation(0, 180, 90), baseTransformation);
                        quads.add(quadBakery.bakeQuad(new Vector3f(size, posStart, 16 - extStart), new Vector3f(0, posStart, 8), wire, Direction.UP, RenderHelper.merge(iCt, placementTransformation), color.ordinal() + 1, eight16, color.ordinal(), zero8));
                    }
                }
                if (change && !item) {
                    extend = data.isExtended(facing);
                    int min11 = extend ? -1 : 1;
                    min11 *= (extend || shortened) ? size : 1;
                    quads.add(quadBakery.bakeQuad(new Vector3f(ft, 1.1f * size, 0), new Vector3f(16 - ft, 1.1f * size, 1.1f * min11), black, extend ? Direction.DOWN : Direction.UP, placedBaseTransformation));
                    if (extend) {
                        quads.add(quadBakery.bakeQuad(new Vector3f(ft, 0, 0), new Vector3f(16 - ft, 0, -1.1f), black, Direction.UP, placedBaseTransformation));
                    }
                    for (Direction f : Direction.values()) {
                        if (f.getAxis() != Direction.Axis.Y) {
                            quads.add(quadBakery.bakeQuad(new Vector3f(f == Direction.EAST ? 16 - ft : ft, 0, f == Direction.SOUTH ? (1.1f * min11) : 0), new Vector3f(f == Direction.WEST ? ft : 16 - ft, 1.1f * size, f == Direction.NORTH ? 0 : (1.1f * min11)), black, extend ? f.getOpposite() : f, placedBaseTransformation));
                        }
                    }
                }
            }

            if (conn.size() != 1 && !WireFacingHelper.isStraightLine(conn)) { //Middle blob
                quads.add(quadBakery.bakeQuad(new Vector3f(ft, 1.1f * size, ft), new Vector3f(16 - ft, 1.1f * size, 16 - ft), black, Direction.UP, placementTransformation));
                for (Direction facing : Direction.values()) {
                    if (facing.getAxis() != Direction.Axis.Y) {
                        quads.add(quadBakery.bakeQuad(new Vector3f(ft, 0, ft), new Vector3f(16 - ft, 1.1f * size, ft), black, Direction.NORTH, RenderHelper.merge(RenderHelper.getDefaultRotationFromFacing(facing), placementTransformation)));
                    }
                }
            }
        }
    }

    @Override
    public void registerTextures(IIconRegistrar iiconRegistrar) {
        wireTypes = new TextureAtlasSprite[EnumElectricityType.values().length];
        for (int i = 0; i < wireTypes.length; i++) {
            wireTypes[i] = iiconRegistrar.registerSprite(new EFlux2ResourceLocation("blocks/flatwire_" + EnumElectricityType.values()[i].toString().toLowerCase()));
        }

        black = iiconRegistrar.registerSprite(new EFlux2ResourceLocation("blocks/black"));
    }

    @Override
    public void registerModels(IElecQuadBakery quadBakery, IElecModelBakery modelBakery, IElecTemplateBakery templateBakery) {
        WireRenderer.quadBakery = quadBakery;
    }

}
