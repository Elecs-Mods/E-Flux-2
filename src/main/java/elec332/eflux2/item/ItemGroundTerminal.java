package elec332.eflux2.item;

import com.google.common.base.Preconditions;
import elec332.core.api.client.IIconRegistrar;
import elec332.core.api.client.model.IElecModelBakery;
import elec332.core.api.client.model.IElecQuadBakery;
import elec332.core.api.client.model.IElecTemplateBakery;
import elec332.core.client.model.loading.INoJsonItem;
import elec332.core.util.PlayerHelper;
import elec332.core.util.math.RayTraceHelper;
import elec332.core.util.math.VectorHelper;
import elec332.eflux2.EFlux2;
import elec332.eflux2.client.wire.WireModelCache;
import elec332.eflux2.register.EFlux2BlockRegister;
import elec332.eflux2.register.EFlux2ItemRegister;
import elec332.eflux2.wire.terminal.GroundTerminal;
import elec332.eflux2.wire.terminal.tile.ISubTileTerminal;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by Elec332 on 20-2-2018
 */
public class ItemGroundTerminal extends ItemSubTile implements INoJsonItem {

    public ItemGroundTerminal(Properties builder) {
        super(EFlux2BlockRegister.wire, builder);
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        if (!isInGroup(group)) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            items.add(withColor(null, i));
            for (DyeColor color : DyeColor.values()) {
                items.add(withColor(color, i));
            }
        }
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn) {
        Pair<Integer, DyeColor> data = getDataFromStack(stack);
        tooltip.add(new StringTextComponent("Size: " + (data.getLeft() + 1)));
        if (data.getRight() != null) {
            tooltip.add(new StringTextComponent(data.getRight().toString())); //todo localize
        }
    }

    @Override
    public boolean canBePlaced(@Nonnull BlockItemUseContext context, @Nonnull BlockState state) {
        Direction side = context.getFace();
        BlockPos pos = context.getPos();
        World world = context.getWorld();
        Vec3d posz = GroundTerminal.getPlacementLocationFromHitVec(GroundTerminal.lockToPixels(context.getHitVec()), side.getOpposite().getAxis());
        return GroundTerminal.isPositionValid(posz, side.getOpposite()) && GroundTerminal.isWithinBounds(posz, getDataFromStack(context.getItem()).getLeft(), side.getOpposite()) && GroundTerminal.canTerminalStay(world, pos, side.getOpposite());
    }

    @Override
    public void afterBlockPlaced(@Nonnull BlockItemUseContext context, @Nonnull BlockState state, TileEntity tile) {
        if (context.getWorld().isRemote) {
            return;
        }
        Pair<Integer, DyeColor> data = getDataFromStack(context.getItem());
        GroundTerminal wp = GroundTerminal.makeTerminal(context.getFace().getOpposite(), data.getLeft(), context.getHitVec(), data.getRight());
        ISubTileTerminal wireHandler = tile.getCapability(EFlux2.TERMINAL_CAPABILITY).orElse(null);
        if (wp != null && wireHandler != null) {
            wireHandler.addTerminal(wp);
        }
    }

    @Override
    public void onExistingObjectClicked(TileEntity tile, @Nonnull BlockRayTraceResult hit, PlayerEntity player, ItemStack stack, BlockState state) {
    }

    @Override
    public void onEmptySolidSideClicked(TileEntity tile, @Nonnull Direction hit, PlayerEntity player, ItemStack stack, BlockState state) {
        ISubTileTerminal wireHandler = tile.getCapability(EFlux2.TERMINAL_CAPABILITY).orElse(null);
        if (wireHandler != null) {
            BlockRayTraceResult rtr = Preconditions.checkNotNull(RayTraceHelper.retraceBlock(tile.getWorld(), tile.getPos(), player));
            Pair<Integer, DyeColor> data = getDataFromStack(stack);
            GroundTerminal wp = GroundTerminal.makeTerminal(hit, data.getLeft(), GroundTerminal.getPlacementLocationFromHitVec(VectorHelper.subtractFrom(rtr.getHitVec(), tile.getPos()), hit.getAxis()), data.getRight());
            if (wp != null && wireHandler.addTerminal(wp) && !PlayerHelper.isPlayerInCreative(player)) {
                stack.shrink(1);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IBakedModel getItemModel(ItemStack itemStack, World world, LivingEntity entityLivingBase) {
        return WireModelCache.instance;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerTextures(IIconRegistrar iIconRegistrar) {
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerModels(IElecQuadBakery iElecQuadBakery, IElecModelBakery iElecModelBakery, IElecTemplateBakery iElecTemplateBakery) {
    }

    public static ItemStack withColor(@Nullable DyeColor color, int size) {
        int clr;
        if (color == null) {
            clr = -1;
        } else {
            clr = color.getId();
        }

        CompoundNBT tag = new CompoundNBT();
        tag.putInt("clrtr", clr);
        tag.putInt("clrtz", size);
        ItemStack ret = new ItemStack(EFlux2ItemRegister.terminal, 1);
        ret.setTag(tag);
        return ret;
    }

    public static Pair<Integer, DyeColor> getDataFromStack(@Nonnull ItemStack stack) {
        if (stack.getItem() != EFlux2ItemRegister.terminal) {
            throw new IllegalArgumentException();
        }
        if (stack.getTag() == null) {
            return Pair.of(0, null);
        }
        int i = stack.getTag().getInt("clrtr");
        int s = Math.max(stack.getTag().getInt("clrtz"), 0);
        DyeColor color;
        if (i == -1) {
            color = null;
        } else {
            color = DyeColor.values()[i];
        }
        return Pair.of(s, color);
    }

}
