package elec332.test.tile;

import elec332.core.api.info.IInfoDataAccessorBlock;
import elec332.core.api.info.IInfoProvider;
import elec332.core.api.info.IInformation;
import elec332.core.api.registration.RegisteredTileEntity;
import elec332.core.tile.AbstractTileEntity;
import elec332.core.util.PlayerHelper;
import elec332.test.api.TestModAPI;
import elec332.test.api.electricity.DefaultElectricityDevice;
import elec332.test.api.electricity.IEnergyReceiver;
import elec332.test.api.electricity.component.EnumElectricityType;
import elec332.test.api.util.ConnectionPoint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Elec332 on 23-11-2017.
 */
@RegisteredTileEntity("testenergyreeiver")
public class TileTestReceiver extends AbstractTileEntity implements IEnergyReceiver, IInfoProvider, IActivatableTile {

    private ConnectionPoint cp1, cp2;
    private double voltage, amps;

    @Override
    public void addInformation(@Nonnull IInformation iInformation, @Nonnull IInfoDataAccessorBlock iInfoDataAccessorBlock) {
        iInformation.addInformation("Voltage: " + iInfoDataAccessorBlock.getData().getDouble("volts"));
        iInformation.addInformation("Amps: " + iInfoDataAccessorBlock.getData().getDouble("amps"));
    }

    @Nonnull
    @Override
    public NBTTagCompound getInfoNBTData(@Nonnull NBTTagCompound nbtTagCompound, TileEntity tileEntity, @Nonnull EntityPlayerMP entityPlayerMP, @Nonnull IInfoDataAccessorBlock iInfoDataAccessorBlock) {
        nbtTagCompound.putDouble("volts", voltage);
        nbtTagCompound.putDouble("amps", amps);
        return nbtTagCompound;
    }

    @Override
    public boolean onBlockActivated(EntityPlayer player, EnumHand hand, RayTraceResult hit) {
        if (!player.world.isRemote() && hand == EnumHand.MAIN_HAND && player.getHeldItem(hand).isEmpty()) {
            PlayerHelper.sendMessageToPlayer(player, "A: " + amps);
            PlayerHelper.sendMessageToPlayer(player, "V: " + voltage);
            PlayerHelper.sendMessageToPlayer(player, " P: " + Math.abs(amps * voltage));
        }
        return false;
    }

    @Nonnull
    @Override
    public EnumElectricityType getEnergyType() {
        return EnumElectricityType.AC;
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
    public double getResistance() {
        return 100;
    }

    @Override
    public void receivePower(double voltage, double amps) {
        this.voltage = voltage;
        this.amps = amps;
    }

    @Override
    @SuppressWarnings("all")
    public <T> LazyOptional<T> getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == TestModAPI.ELECTRICITY_CAP ? LazyOptional.of(() -> new DefaultElectricityDevice(this)).cast() : super.getCapability(capability, facing);
    }

}
