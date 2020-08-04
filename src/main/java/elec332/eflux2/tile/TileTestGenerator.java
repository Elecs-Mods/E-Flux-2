package elec332.eflux2.tile;

import elec332.core.api.info.IInfoDataAccessorBlock;
import elec332.core.api.info.IInfoProvider;
import elec332.core.api.info.IInformation;
import elec332.core.api.registration.RegisteredTileEntity;
import elec332.core.inventory.BasicItemHandler;
import elec332.core.inventory.widget.WidgetEnumChange;
import elec332.core.inventory.widget.slot.WidgetSlot;
import elec332.core.inventory.window.ISimpleWindowFactory;
import elec332.core.inventory.window.Window;
import elec332.core.tile.ITileWithDrops;
import elec332.core.util.InventoryHelper;
import elec332.core.util.ItemStackHelper;
import elec332.core.util.ObjectReference;
import elec332.core.util.PlayerHelper;
import elec332.core.world.WorldHelper;
import elec332.eflux2.api.electricity.IEnergySource;
import elec332.eflux2.api.electricity.component.EnumElectricityType;
import elec332.eflux2.api.util.BreakReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by Elec332 on 23-11-2017.
 */
@RegisteredTileEntity("hatseflats")
public class TileTestGenerator extends AbstractEnergyObjectTile implements IEnergySource, ISimpleWindowFactory, ITileWithDrops, ITickableTileEntity, IInfoProvider, IActivatableTile {

    public TileTestGenerator() {
        inventory = new BasicItemHandler(1) {

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return isItemFuel(stack);
            }

        };
    }

    private final int onVoltage = 25;
    private int tick;
    private int voltage, burnTime;
    private BasicItemHandler inventory;
    private boolean active;
    private ObjectReference<EnumElectricityType> type = ObjectReference.of(EnumElectricityType.AC);

    @Override
    public void tick() {
        if (tick == 20) {
            tick = 0;
        } else {
            tick++;
        }
        if (burnTime > 0) {
            burnTime--;
        } else {
            voltage = 0;
            ItemStack stack = inventory.extractItem(0, 1, true);
            if (ItemStackHelper.isStackValid(stack)) {
                int burnTime = getItemBurnTime(stack.copy());
                if (burnTime > 0) {
                    inventory.extractItem(0, 1, false);
                    this.burnTime = burnTime;
                    voltage = onVoltage;
                    if (!active) {
                        active = true;
                        reRenderBlock();
                    }
                } else {
                    inventory.setStackInSlot(0, ItemStackHelper.NULL_STACK);
                    WorldHelper.dropStack(getWorld(), pos.offset(getTileFacing()), stack.copy());
                }
            }
            if (active && !(burnTime > 0)) {
                active = false;
                reRenderBlock();
            }
        }
        ef = rp = 0;
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        inventory.deserializeNBT(tagCompound);
        voltage = tagCompound.getInt("ibt");
        burnTime = tagCompound.getInt("bt");
        active = tagCompound.getBoolean("aC");
    }

    @Override
    @Nonnull
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        inventory.writeToNBT(tagCompound);
        tagCompound.putInt("ibt", voltage);
        tagCompound.putInt("bt", burnTime);
        tagCompound.putBoolean("aC", active);
        return tagCompound;
    }

    @Override
    public void getDrops(List<ItemStack> items, int fortune) {
        items.addAll(InventoryHelper.storeContents(inventory));
    }

    @Override
    public ActionResultType onBlockActivated(PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!player.world.isRemote() && hand == Hand.MAIN_HAND && player.getHeldItem(hand).isEmpty()) {
            PlayerHelper.sendMessageToPlayer(player, "A: " + rp);
            PlayerHelper.sendMessageToPlayer(player, "V: " + ef);

            PlayerHelper.sendMessageToPlayer(player, " P: " + Math.abs(rp * ef));
        }
        return (!player.isSneaking() || openTileGui(player)) ? ActionResultType.SUCCESS : ActionResultType.PASS;
    }

    @Override
    public void setData(double ef, double rp) {
        this.ef = ef;
        this.rp = rp;
    }

    private double ef, rp;

    @Override
    public void modifyWindow(Window window, Object... args) {
        window.addWidget(new WidgetSlot(inventory, 0, 66, 53) {

            @Override
            public boolean isItemValid(ItemStack stack) {
                return isItemFuel(stack);
            }

        });
        window.addPlayerInventoryToContainer();
        window.addWidget(new WidgetEnumChange<>(2, 2, 30, 30, EnumElectricityType.class).onValueChanged(type::set)).setEnum(type.get());
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public float getVariance() {
        return 0; //TODO
    }

    @Override
    public int getCurrentAverageEF() {
        return voltage;
    }

    @Override
    public float getMaxRP() {
        return 10;
    }

    @Override
    public void addInformation(@Nonnull IInformation iInformation, @Nonnull IInfoDataAccessorBlock iInfoDataAccessorBlock) {
        iInformation.addInformation("Provided voltage: " + iInfoDataAccessorBlock.getData().getInt("volts"));
        iInformation.addInformation("Active: " + active);
    }

    @Override
    public void gatherInformation(@Nonnull CompoundNBT tag, @Nonnull ServerPlayerEntity player, @Nonnull IInfoDataAccessorBlock hitData) {
        tag.putInt("volts", voltage);
    }

    @Nonnull
    @Override
    public EnumElectricityType getEnergyType() {
        return type.get();
    }

    @Override
    protected Object createConnectionPoint(int post) {
        return connectionPointHandler.makeConnectionPoint(getTileFacing().getOpposite(), post, Direction.DOWN);
    }

    @Override
    public Object getConnectionPointRef(Direction side, Vec3d hitVec) {
        return side != getTileFacing().getOpposite() ? null : getConnectionPointRef(hitVec.y > 0.5 ? 0 : 1);
    }

    @Override
    public void breakMachine(@Nonnull BreakReason reason) {
        System.out.println("Biem: " + reason);
        WorldHelper.spawnExplosion(getWorld(), pos.getX(), pos.getY(), pos.getZ(), reason.ordinal() * 0.3f + 0.1f);
    }

    private static boolean isItemFuel(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return getItemBurnTime(stack) > 0;
    }

    //copied from vanilla TODO: AT
    private static int getItemBurnTime(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        } else {
            Item item = stack.getItem();
            int ret = stack.getBurnTime();
            return net.minecraftforge.event.ForgeEventFactory.getItemBurnTime(stack, ret == -1 ? FurnaceTileEntity.getBurnTimes().getOrDefault(item, 0) : ret);
        }
    }

}
