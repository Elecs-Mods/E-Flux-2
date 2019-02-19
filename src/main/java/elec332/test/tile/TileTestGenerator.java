package elec332.test.tile;

import elec332.core.api.info.IInfoDataAccessorBlock;
import elec332.core.api.info.IInfoProvider;
import elec332.core.api.info.IInformation;
import elec332.core.api.registration.RegisteredTileEntity;
import elec332.core.inventory.BasicItemHandler;
import elec332.core.inventory.widget.WidgetEnumChange;
import elec332.core.inventory.widget.slot.WidgetSlot;
import elec332.core.inventory.window.ISimpleWindowFactory;
import elec332.core.inventory.window.Window;
import elec332.core.tile.AbstractTileEntity;
import elec332.core.tile.ITileWithDrops;
import elec332.core.util.*;
import elec332.core.world.WorldHelper;
import elec332.test.api.TestModAPI;
import elec332.test.api.electricity.DefaultElectricityDevice;
import elec332.test.api.electricity.IEnergySource;
import elec332.test.api.electricity.component.EnumElectricityType;
import elec332.test.api.util.BreakReason;
import elec332.test.api.util.ConnectionPoint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Elec332 on 23-11-2017.
 */
@RegisteredTileEntity("hatseflats")
public class TileTestGenerator extends AbstractTileEntity implements IEnergySource, ISimpleWindowFactory, ITileWithDrops, ITickable, IInfoProvider, IActivatableTile {

    public TileTestGenerator() {
        inventory = new BasicItemHandler(1) {

            @Override
            public boolean isStackValidForSlot(int slot, @Nonnull ItemStack stack) {
                return TileEntityFurnace.isItemFuel(stack);
            }

        };
    }

    private final int onVoltage = 25;
    private int tick;
    private int voltage, burnTime;
    private BasicItemHandler inventory;
    private boolean active;
    private ConnectionPoint cp1, cp2;
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
        voltage = 10;//onVoltage;
        if (active) {
            voltage *= 2;
        }
        active = true;
        ef = rp = 0;
    }

    @Override
    public void read(NBTTagCompound tagCompound) {
        super.read(tagCompound);
        inventory.deserializeNBT(tagCompound);
        voltage = tagCompound.getInt("ibt");
        burnTime = tagCompound.getInt("bt");
        active = tagCompound.getBoolean("aC");
    }

    @Override
    @Nonnull
    public NBTTagCompound write(NBTTagCompound tagCompound) {
        super.write(tagCompound);
        inventory.writeToNBT(tagCompound);
        tagCompound.putInt("ibt", voltage);
        tagCompound.putInt("bt", burnTime);
        tagCompound.putBoolean("aC", active);
        return tagCompound;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> items, int fortune) {
        items.addAll(InventoryHelper.storeContents(inventory));
    }

    @Override
    public boolean onBlockActivated(EntityPlayer player, EnumHand hand, RayTraceResult hit) {
        if (!player.world.isRemote() && hand == EnumHand.MAIN_HAND && player.getHeldItem(hand).isEmpty()) {
            PlayerHelper.sendMessageToPlayer(player, "A: " + rp);
            PlayerHelper.sendMessageToPlayer(player, "V: " + ef);

            PlayerHelper.sendMessageToPlayer(player, " P: " + Math.abs(rp * ef));
        }
        return !player.isSneaking() || openTileGui(player);
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
                return TileEntityFurnace.isItemFuel(stack);
            }

        });
        window.addPlayerInventoryToContainer();
        window.addWidget(new WidgetEnumChange<>(2, 2, 30, 30, EnumElectricityType.class).addButtonEvent((WidgetEnumChange.IEnumChangedEvent<WidgetEnumChange<EnumElectricityType>>) w -> {
            System.out.println(FMLHelper.getLogicalSide() + "  " + w.getEnum());
            type.set(w.getEnum());
        }));
    }

    public boolean isActive() {
        return active;
    }

    @Override
    @SuppressWarnings("all")
    public <T> LazyOptional<T> getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == TestModAPI.ELECTRICITY_CAP ? LazyOptional.of(() -> new DefaultElectricityDevice(this)).cast() : super.getCapability(capability, facing);
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
    }

    @Nonnull
    @Override
    public NBTTagCompound getInfoNBTData(@Nonnull NBTTagCompound nbtTagCompound, TileEntity tileEntity, @Nonnull EntityPlayerMP entityPlayerMP, @Nonnull IInfoDataAccessorBlock iInfoDataAccessorBlock) {
        nbtTagCompound.putInt("volts", voltage);
        return nbtTagCompound;
    }

    @Nonnull
    @Override
    public EnumElectricityType getEnergyType() {
        return type.get();
    }

    @Nonnull
    @Override
    public ConnectionPoint getConnectionPoint(int post) {
        return post == 0 ? cp1 : cp2;
    }

    @Nullable
    @Override
    public ConnectionPoint getConnectionPoint(EnumFacing side, Vec3d hitVec) {
        return side != getTileFacing().getOpposite() ? null : (hitVec.y > 0.5 ? cp2 : cp1);
    }

    @Override
    public void updateContainingBlockInfo() {
        super.updateContainingBlockInfo();
        createConnectionPoints();
    }

    @Override
    public void onLoad() {
        createConnectionPoints();
    }

    private void createConnectionPoints() {
        cp1 = new ConnectionPoint(pos, world, getTileFacing().getOpposite(), 1, EnumFacing.DOWN);
        cp2 = new ConnectionPoint(pos, world, getTileFacing().getOpposite(), 2, EnumFacing.DOWN);
    }

    @Override
    public void breakMachine(@Nonnull BreakReason reason) {
        System.out.println("Biem: " + reason);
        WorldHelper.spawnExplosion(getWorld(), pos.getX(), pos.getY(), pos.getZ(), reason.ordinal() * 0.3f + 0.1f);
    }

    private static int getItemBurnTime(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        } else {
            Item item = stack.getItem();
            int ret = stack.getBurnTime();
            return net.minecraftforge.event.ForgeEventFactory.getItemBurnTime(stack, ret == -1 ? TileEntityFurnace.getBurnTimes().getOrDefault(item, 0) : ret);
        }
    }

}
