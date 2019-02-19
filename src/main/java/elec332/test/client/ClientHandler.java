package elec332.test.client;

import elec332.core.ElecCore;
import elec332.core.api.annotations.StaticLoad;
import elec332.core.client.RenderHelper;
import elec332.core.util.VectorHelper;
import elec332.core.world.WorldHelper;
import elec332.test.TestMod;
import elec332.test.item.ItemGroundTerminal;
import elec332.test.wire.overhead.OverHeadWireHandlerClient;
import elec332.test.wire.overhead.OverheadWire;
import elec332.test.wire.terminal.GroundTerminal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.awt.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by Elec332 on 7-11-2017.
 */
@StaticLoad
@OnlyIn(Dist.CLIENT)
public class ClientHandler {

    @SubscribeEvent
    public void renderStuff(RenderWorldLastEvent event) {
        DimensionType world = WorldHelper.getDimID(ElecCore.proxy.getClientWorld());
        Function<ChunkPos, Stream<OverheadWire>> wires = OverHeadWireHandlerClient.INSTANCE.getWires(world);
        int renderDistanceChunks = Minecraft.getInstance().gameSettings.renderDistanceChunks;
        for (int i = -renderDistanceChunks; i < renderDistanceChunks; i++) {
            for (int j = -renderDistanceChunks; j < renderDistanceChunks; j++) {
                BlockPos rPos = ElecCore.proxy.getClientPlayer().getPosition().add(i * 16, 0, j * 16);
                wires.apply(WorldHelper.chunkPosFromBlockPos(rPos)).forEach(ClientHandler::renderWire);
            }
        }
    }

    private static void renderWire(OverheadWire wire) {
        GlStateManager.pushMatrix();
        Vec3d tr = RenderFunctions.getTranslationForRendering(wire.getStart());
        GlStateManager.translated(tr.x, tr.y, tr.z);
        GlStateManager.lineWidth(3);
        RenderFunctions.renderWire(wire.getStart(), wire.getEnd(), Color.BLACK, 1, false);
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void onBlockHighlight(DrawBlockHighlightEvent event) {
        ItemStack stack = Minecraft.getInstance().player.getHeldItem(EnumHand.MAIN_HAND);
        if (stack.getItem() != TestMod.terminal) {
            return;
        }
        RayTraceResult hit = event.getTarget();
        World world = Minecraft.getInstance().world;
        if (event.getSubID() == 0 && hit.type == RayTraceResult.Type.BLOCK) {
            EnumFacing side = hit.sideHit;
            BlockPos pos = hit.getBlockPos();
            IBlockState state = world.getBlockState(pos);
            if (!state.isAir(world, pos) && GroundTerminal.canTerminalStay(world, pos.offset(side), side.getOpposite())) {
                int size = ItemGroundTerminal.getDataFromStack(stack).getLeft();
                EnumFacing.Axis axis = side.getAxis();

                Vec3d hitVec = GroundTerminal.lockToPixels(VectorHelper.subtractFrom(hit.hitVec, pos));
                hitVec = GroundTerminal.getPlacementLocationFromHitVec(hitVec, axis); //location in target block
                if (!GroundTerminal.isPositionValid(hitVec, side.getOpposite())) {
                    return;
                }
                VoxelShape shape = GroundTerminal.getShape(hitVec, size, side.getOpposite());
                if (GroundTerminal.isWithinBounds(shape)) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translated(side.getXOffset(), side.getYOffset(), side.getZOffset());
                    RenderHelper.drawSelectionBox(event.getPlayer(), world, pos, shape, event.getPartialTicks());
                    GlStateManager.popMatrix();
                }
            }
        }
    }

    static {
        MinecraftForge.EVENT_BUS.register(new ClientHandler());
    }

}
