package elec332.eflux2.tile;

import elec332.core.tile.AbstractTileEntity;
import elec332.eflux2.util.ConnectionPointHandler;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;

/**
 * Created by Elec332 on 20-2-2019
 */
public class AbstractEnergyTile extends AbstractTileEntity {

    public AbstractEnergyTile() {
        this.connectionPointHandler = new ConnectionPointHandler(() -> this);
    }

    protected final ConnectionPointHandler connectionPointHandler;

    @Nonnull
    public ConnectionPointHandler getConnectionPointHandler() {
        return connectionPointHandler;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        connectionPointHandler.read(compound.getCompound("cph"));
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.put("cph", connectionPointHandler.save());
        return super.write(compound);
    }

    @Override
    public void updateContainingBlockInfo() {
        super.updateContainingBlockInfo();
        connectionPointHandler.onBlockInfoUpdate();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        connectionPointHandler.onLoad();
    }

}
