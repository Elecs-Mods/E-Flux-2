package elec332.eflux2.modules.wires.client;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import elec332.core.ElecCore;
import elec332.core.api.annotations.StaticLoad;
import elec332.core.client.RenderHelper;
import elec332.core.util.math.HitboxHelper;
import elec332.core.util.math.VectorHelper;
import elec332.core.world.WorldHelper;
import elec332.eflux2.modules.wires.WiresModule;
import elec332.eflux2.modules.wires.item.ItemGroundTerminal;
import elec332.eflux2.modules.wires.wire.overhead.OverHeadWireHandlerClient;
import elec332.eflux2.modules.wires.wire.overhead.OverheadWire;
import elec332.eflux2.modules.wires.wire.terminal.GroundTerminal;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawHighlightEvent;
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
    public void onBlockHighlight(DrawHighlightEvent event) {
        ItemStack stack = Minecraft.getInstance().player.getHeldItem(Hand.MAIN_HAND);
        if (stack.getItem() != WiresModule.TERMINAL_ITEM.get()) {
            return;
        }
        RayTraceResult hit_ = event.getTarget();
        World world = Preconditions.checkNotNull(Minecraft.getInstance().world);
        if (hit_ instanceof BlockRayTraceResult) {
            BlockRayTraceResult hit = (BlockRayTraceResult) hit_;
            Direction side = hit.getFace();
            BlockPos pos = hit.getPos();
            BlockState state = WorldHelper.getBlockState(world, pos);
            if (!state.isAir(world, pos) && GroundTerminal.canTerminalStay(world, pos.offset(side), side.getOpposite())) {
                int size = ItemGroundTerminal.getDataFromStack(stack).getLeft();
                Direction.Axis axis = side.getAxis();

                Vec3d hitVec = GroundTerminal.lockToPixels(VectorHelper.subtractFrom(hit.getHitVec(), pos));
                hitVec = GroundTerminal.getPlacementLocationFromHitVec(hitVec, axis); //location in target block
                if (!GroundTerminal.isPositionValid(hitVec, side.getOpposite())) {
                    return;
                }
                VoxelShape shape = GroundTerminal.getShape(hitVec, size, side.getOpposite());
                if (GroundTerminal.isWithinBounds(shape) && !HitboxHelper.collides(shape, WorldHelper.getShape(world, pos.offset(side)))) {
                    MatrixStack matrixStack = event.getMatrix();
                    matrixStack.push();
                    matrixStack.translate(side.getXOffset(), side.getYOffset(), side.getZOffset());
                    RenderHelper.drawSelectionBox(world, pos, shape, event.getInfo().getProjectedView(), matrixStack, event.getBuffers());
                    matrixStack.pop();
                }
            }
        }
    }

    static {
        MinecraftForge.EVENT_BUS.register(new ClientHandler());
    }

}
