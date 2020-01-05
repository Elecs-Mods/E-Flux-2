package elec332.eflux2.tile;

import elec332.core.api.info.IInfoDataAccessorBlock;
import elec332.core.api.info.IInfoProvider;
import elec332.core.api.info.IInformation;
import elec332.core.api.registration.RegisteredTileEntity;
import elec332.core.util.PlayerHelper;
import elec332.eflux2.api.electricity.IEnergyReceiver;
import elec332.eflux2.api.electricity.component.EnumElectricityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;

/**
 * Created by Elec332 on 23-11-2017.
 */
@RegisteredTileEntity("testenergyreeiver")
public class TileTestReceiver extends AbstractEnergyObjectTile implements IEnergyReceiver, IInfoProvider, IActivatableTile {

    private double voltage, amps;

    @Override
    public void addInformation(@Nonnull IInformation iInformation, @Nonnull IInfoDataAccessorBlock iInfoDataAccessorBlock) {
        iInformation.addInformation("Voltage: " + iInfoDataAccessorBlock.getData().getDouble("volts"));
        iInformation.addInformation("Amps: " + iInfoDataAccessorBlock.getData().getDouble("amps"));
    }

    @Nonnull
    @Override
    public CompoundNBT getInfoNBTData(@Nonnull CompoundNBT CompoundNBT, TileEntity tileEntity, @Nonnull ServerPlayerEntity ServerPlayerEntity, @Nonnull IInfoDataAccessorBlock iInfoDataAccessorBlock) {
        CompoundNBT.putDouble("volts", voltage);
        CompoundNBT.putDouble("amps", amps);
        return CompoundNBT;
    }

    @Override
    public boolean onBlockActivated(PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!player.world.isRemote() && hand == Hand.MAIN_HAND && player.getHeldItem(hand).isEmpty()) {
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

    @Override
    protected Object createConnectionPoint(int post) {
        return connectionPointHandler.makeConnectionPoint(getTileFacing().getOpposite(), post, Direction.DOWN);
    }

    @Override
    public Object getConnectionPointRef(Direction side, Vec3d hitVec) {
        return side != getTileFacing().getOpposite() ? null : getConnectionPointRef((hitVec.y > 0.5 ? 1 : 0));
    }

    @Override
    public double getResistance() {
        return 10;
    }

    @Override
    public void receivePower(double voltage, double amps) {
        this.voltage = voltage;
        this.amps = amps;
    }

}
