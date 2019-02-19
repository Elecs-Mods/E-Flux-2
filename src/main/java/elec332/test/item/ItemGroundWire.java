package elec332.test.item;

import com.google.common.collect.Sets;
import elec332.core.api.client.IIconRegistrar;
import elec332.core.api.client.model.IElecModelBakery;
import elec332.core.api.client.model.IElecQuadBakery;
import elec332.core.api.client.model.IElecTemplateBakery;
import elec332.core.client.model.loading.INoJsonItem;
import elec332.core.util.PlayerHelper;
import elec332.test.TestMod;
import elec332.test.wire.WireColorHelper;
import elec332.test.wire.ground.GroundWire;
import elec332.test.wire.ground.tile.ISubTileWire;
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
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by Elec332 on 1-2-2019
 */
public class ItemGroundWire extends ItemSubTile implements INoJsonItem {

    public ItemGroundWire(Properties builder) {
        super(TestMod.block, builder);
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        if (!isInGroup(group)) {
            return;
        }
        for (int i = 1; i < 5; i++) { //Add all 4 wire sizes, RIP creative tab
            for (EnumDyeColor color : EnumDyeColor.values()) {
                items.add(withCables(i, color));
            }
        }
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn) {
        Pair<Integer, List<EnumDyeColor>> data = getColorsFromStack(stack);
        tooltip.add(new TextComponentString("Size: " + data.getLeft()));
        for (EnumDyeColor color : data.getRight()) {
            tooltip.add(new TextComponentString(color.toString())); //todo localize
        }
    }

    @Override
    public boolean canBePlaced(@Nonnull BlockItemUseContext context, @Nonnull IBlockState state) {
        return GroundWire.canWireStay(context.getWorld(), context.getPos(), context.getFace().getOpposite());
    }

    @Override
    public void afterBlockPlaced(@Nonnull BlockItemUseContext context, @Nonnull IBlockState state, TileEntity tile) {
        if (!context.getWorld().isRemote) {
            GroundWire wp = createWirePart(context.getPlayer(), context.getItem(), context.getFace().getOpposite());
            ISubTileWire wireHandler = tile.getCapability(TestMod.WIRE_CAPABILITY).orElse(null);
            if (wireHandler != null && wp != null) {
                wireHandler.addWire(wp);
            }
        }
    }

    @Nullable
    private GroundWire createWirePart(EntityPlayer player, ItemStack stack, EnumFacing facing) {
        Pair<Integer, List<EnumDyeColor>> data = getColorsFromStack(stack);
        GroundWire wire = new GroundWire(facing, data.getLeft());
        if (!wire.setColors(data.getRight())) {
            if (!player.world.isRemote) {
                PlayerHelper.sendMessageToPlayer(player, "Too many wires, please reduce the wire count in this item");
            }
            return null;
        }
        return wire;
    }

    @Override
    public void onExistingObjectClicked(TileEntity tile, @Nonnull RayTraceResult hit, EntityPlayer player, ItemStack stack, IBlockState state) {
        if (hit.hitInfo instanceof GroundWire) {
            GroundWire wire = (GroundWire) hit.hitInfo;
            if (wire.addColors(getColorsDataFromStack(stack)) && !PlayerHelper.isPlayerInCreative(player)) {
                stack.shrink(1);
            }
        }
    }

    @Override
    public void onEmptySolidSideClicked(TileEntity tile, @Nonnull EnumFacing hit, EntityPlayer player, ItemStack stack, IBlockState state, Vec3d hitVec) {
        ISubTileWire wireHandler = tile.getCapability(TestMod.WIRE_CAPABILITY, null).orElse(null);
        if (wireHandler != null && wireHandler.getWire(hit) == null) {
            GroundWire wire = createWirePart(player, stack, hit);
            if (wireHandler.addWire(wire) && !PlayerHelper.isPlayerInCreative(player)) {
                stack.shrink(1);
            }
        }
    }

    public static ItemStack withCables(int size, @Nonnull EnumDyeColor color1, EnumDyeColor... colors) {
        Set<EnumDyeColor> r = Sets.newHashSet(colors);
        r.add(color1);
        return withCables(r, size);
    }

    public static ItemStack withCables(@Nonnull Collection<EnumDyeColor> colors, int size) {
        int clr = 0;
        Set<EnumDyeColor> clrs = Sets.newHashSet(colors);
        for (EnumDyeColor dye : clrs) {
            clr = WireColorHelper.addWire(dye, clr);
        }
        NBTTagCompound tag = new NBTTagCompound();
        tag.putInt("clrwr", clr);
        tag.putInt("clrsz", size);
        ItemStack ret = new ItemStack(TestMod.wire, 1);
        ret.setTag(tag);
        return ret;
    }

    public static Pair<Integer, Integer> getColorsDataFromStack(@Nonnull ItemStack stack) {
        if (stack.getItem() != TestMod.wire) {
            throw new IllegalArgumentException();
        }
        if (stack.getTag() == null) {
            return Pair.of(1, EnumDyeColor.WHITE.getId()); // -_- Thx JEI
        }
        int i = stack.getTag().getInt("clrwr");
        int s = stack.getTag().getInt("clrsz");
        if (i == 0) {
            throw new IllegalArgumentException();
        }
        return Pair.of(s, i);
    }

    public static Pair<Integer, List<EnumDyeColor>> getColorsFromStack(@Nonnull ItemStack stack) {
        Pair<Integer, Integer> data = getColorsDataFromStack(stack);
        return Pair.of(data.getLeft(), WireColorHelper.getColors(data.getRight()));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IBakedModel getItemModel(ItemStack itemStack, World world, EntityLivingBase entityLivingBase) {
        return TestMod.model;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerTextures(IIconRegistrar iIconRegistrar) {
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerModels(IElecQuadBakery iElecQuadBakery, IElecModelBakery iElecModelBakery, IElecTemplateBakery iElecTemplateBakery) {
    }

}
