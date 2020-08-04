package elec332.eflux2.item;

import com.google.common.collect.Sets;
import elec332.core.api.client.IIconRegistrar;
import elec332.core.api.client.model.IElecModelBakery;
import elec332.core.api.client.model.IElecQuadBakery;
import elec332.core.api.client.model.IElecTemplateBakery;
import elec332.core.client.model.loading.INoJsonItem;
import elec332.core.item.ItemSubTile;
import elec332.core.util.PlayerHelper;
import elec332.eflux2.EFlux2;
import elec332.eflux2.client.wire.WireModelCache;
import elec332.eflux2.register.EFlux2BlockRegister;
import elec332.eflux2.register.EFlux2ItemRegister;
import elec332.eflux2.wire.WireColorHelper;
import elec332.eflux2.wire.ground.GroundWire;
import elec332.eflux2.wire.ground.tile.IWireContainer;
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
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
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
        super(EFlux2BlockRegister.wire, builder);
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        if (!isInGroup(group)) {
            return;
        }
        for (int i = 1; i < 5; i++) { //Add all 4 wire sizes, RIP creative tab
            for (DyeColor color : DyeColor.values()) {
                items.add(withCables(i, color));
            }
        }
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn) {
        Pair<Integer, List<DyeColor>> data = getColorsFromStack(stack);
        tooltip.add(new StringTextComponent("Size: " + data.getLeft()));
        for (DyeColor color : data.getRight()) {
            tooltip.add(new StringTextComponent(color.toString())); //todo localize
        }
    }

    @Override
    public boolean canBePlaced(@Nonnull BlockItemUseContext context, @Nonnull BlockState state) {
        return GroundWire.canWireStay(context.getWorld(), context.getPos(), context.getFace().getOpposite());
    }

    @Override
    public void afterBlockPlaced(@Nonnull BlockItemUseContext context, @Nonnull BlockState state, TileEntity tile) {
        if (!context.getWorld().isRemote) {
            GroundWire wp = createWirePart(context.getPlayer(), context.getItem(), context.getFace().getOpposite());
            IWireContainer wireHandler = tile.getCapability(EFlux2.WIRE_CAPABILITY).orElse(null);
            if (wireHandler != null && wp != null) {
                wireHandler.addWire(wp);
            }
        }
    }

    @Nullable
    private GroundWire createWirePart(PlayerEntity player, ItemStack stack, Direction facing) {
        Pair<Integer, List<DyeColor>> data = getColorsFromStack(stack);
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
    public void onExistingObjectClicked(TileEntity tile, @Nonnull BlockRayTraceResult hit, PlayerEntity player, ItemStack stack, BlockState state) {
        System.out.println("Ext");
        if (hit.hitInfo instanceof GroundWire) {
            System.out.println(hit.hitInfo);
            GroundWire wire = (GroundWire) hit.hitInfo;
            if (wire.addColors(getColorsDataFromStack(stack)) && !PlayerHelper.isPlayerInCreative(player)) {
                stack.shrink(1);
            }
        }
    }

    @Override
    public void onEmptySolidSideClicked(TileEntity tile, @Nonnull Direction hit, PlayerEntity player, ItemStack stack, BlockState state) {
        IWireContainer wireHandler = tile.getCapability(EFlux2.WIRE_CAPABILITY, null).orElse(null);
        if (wireHandler != null && wireHandler.getWire(hit) == null) {
            GroundWire wire = createWirePart(player, stack, hit);
            if (wireHandler.addWire(wire) && !PlayerHelper.isPlayerInCreative(player)) {
                stack.shrink(1);
            }
        }
    }

    public static ItemStack withCables(int size, @Nonnull DyeColor color1, DyeColor... colors) {
        Set<DyeColor> r = Sets.newHashSet(colors);
        r.add(color1);
        return withCables(r, size);
    }

    public static ItemStack withCables(@Nonnull Collection<DyeColor> colors, int size) {
        int clr = 0;
        Set<DyeColor> clrs = Sets.newHashSet(colors);
        for (DyeColor dye : clrs) {
            clr = WireColorHelper.addWire(dye, clr);
        }
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("clrwr", clr);
        tag.putInt("clrsz", size);
        ItemStack ret = new ItemStack(EFlux2ItemRegister.wire, 1);
        ret.setTag(tag);
        return ret;
    }

    public static Pair<Integer, Integer> getColorsDataFromStack(@Nonnull ItemStack stack) {
        if (stack.getItem() != EFlux2ItemRegister.wire) {
            throw new IllegalArgumentException();
        }
        if (stack.getTag() == null) {
            return Pair.of(1, DyeColor.WHITE.getId()); // -_- Thx JEI
        }
        int i = stack.getTag().getInt("clrwr");
        int s = stack.getTag().getInt("clrsz");
        if (i == 0) {
            throw new IllegalArgumentException();
        }
        return Pair.of(s, i);
    }

    public static Pair<Integer, List<DyeColor>> getColorsFromStack(@Nonnull ItemStack stack) {
        Pair<Integer, Integer> data = getColorsDataFromStack(stack);
        return Pair.of(data.getLeft(), WireColorHelper.getColors(data.getRight()));
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

}
