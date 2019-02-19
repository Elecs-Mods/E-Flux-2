package elec332.test.item;

import elec332.core.api.client.IIconRegistrar;
import elec332.core.api.client.model.IElecModelBakery;
import elec332.core.api.client.model.IElecQuadBakery;
import elec332.core.api.client.model.IElecTemplateBakery;
import elec332.core.client.model.loading.INoJsonItem;
import elec332.core.util.PlayerHelper;
import elec332.core.util.VectorHelper;
import elec332.test.TestMod;
import elec332.test.wire.terminal.GroundTerminal;
import elec332.test.wire.terminal.tile.ISubTileTerminal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
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
        super(TestMod.block, builder);
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        if (!isInGroup(group)) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            items.add(withColor(null, i));
            for (EnumDyeColor color : EnumDyeColor.values()) {
                items.add(withColor(color, i));
            }
        }
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn) {
        Pair<Integer, EnumDyeColor> data = getDataFromStack(stack);
        tooltip.add(new TextComponentString("Size: " + (data.getLeft() + 1)));
        if (data.getRight() != null) {
            tooltip.add(new TextComponentString(data.getRight().toString())); //todo localize
        }
    }

    @Override
    public boolean canBePlaced(@Nonnull BlockItemUseContext context, @Nonnull IBlockState state) {
        EnumFacing side = context.getFace();
        BlockPos pos = context.getPos();
        World world = context.getWorld();
        Vec3d hitVec = new Vec3d(context.getHitX(), context.getHitY(), context.getHitZ());
        Vec3d posz = GroundTerminal.getPlacementLocationFromHitVec(GroundTerminal.lockToPixels(hitVec), side.getOpposite().getAxis());
        return GroundTerminal.isPositionValid(posz, side.getOpposite()) && GroundTerminal.isWithinBounds(posz, getDataFromStack(context.getItem()).getLeft(), side.getOpposite()) && GroundTerminal.canTerminalStay(world, pos, side.getOpposite());
    }

    @Override
    public void afterBlockPlaced(@Nonnull BlockItemUseContext context, @Nonnull IBlockState state, TileEntity tile) {
        if (context.getWorld().isRemote) {
            return;
        }
        Pair<Integer, EnumDyeColor> data = getDataFromStack(context.getItem());
        GroundTerminal wp = GroundTerminal.makeTerminal(context.getFace().getOpposite(), data.getLeft(), new Vec3d(context.getHitX(), context.getHitY(), context.getHitZ()), data.getRight());
        ISubTileTerminal wireHandler = tile.getCapability(TestMod.TERMINAL_CAPABILITY).orElse(null);
        if (wp != null && wireHandler != null) {
            wireHandler.addTerminal(wp);
        }
    }

    @Override
    public void onExistingObjectClicked(TileEntity tile, @Nonnull RayTraceResult hit, EntityPlayer player, ItemStack stack, IBlockState state) {
    }

    @Override
    public void onEmptySolidSideClicked(TileEntity tile, @Nonnull EnumFacing hit, EntityPlayer player, ItemStack stack, IBlockState state, Vec3d hitVec) {
        ISubTileTerminal wireHandler = tile.getCapability(TestMod.TERMINAL_CAPABILITY).orElse(null);
        if (wireHandler != null) {
            Pair<Integer, EnumDyeColor> data = getDataFromStack(stack);
            GroundTerminal wp = GroundTerminal.makeTerminal(hit, data.getLeft(), GroundTerminal.getPlacementLocationFromHitVec(VectorHelper.subtractFrom(hitVec, tile.getPos()), hit.getAxis()), data.getRight());
            if (wp != null && wireHandler.addTerminal(wp) && !PlayerHelper.isPlayerInCreative(player)) {
                stack.shrink(1);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IBakedModel getItemModel(ItemStack itemStack, World world, EntityLivingBase entityLivingBase) {
        return TestMod.terminalModel;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerTextures(IIconRegistrar iIconRegistrar) {
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerModels(IElecQuadBakery iElecQuadBakery, IElecModelBakery iElecModelBakery, IElecTemplateBakery iElecTemplateBakery) {
    }

    public static ItemStack withColor(@Nullable EnumDyeColor color, int size) {
        int clr;
        if (color == null) {
            clr = -1;
        } else {
            clr = color.getId();
        }

        NBTTagCompound tag = new NBTTagCompound();
        tag.putInt("clrtr", clr);
        tag.putInt("clrtz", size);
        ItemStack ret = new ItemStack(TestMod.terminal, 1);
        ret.setTag(tag);
        return ret;
    }

    public static Pair<Integer, EnumDyeColor> getDataFromStack(@Nonnull ItemStack stack) {
        if (stack.getItem() != TestMod.terminal) {
            throw new IllegalArgumentException();
        }
        if (stack.getTag() == null) {
            return Pair.of(0, null);
        }
        int i = stack.getTag().getInt("clrtr");
        int s = Math.max(stack.getTag().getInt("clrtz"), 0);
        EnumDyeColor color;
        if (i == -1) {
            color = null;
        } else {
            color = EnumDyeColor.values()[i];
        }
        return Pair.of(s, color);
    }

}
