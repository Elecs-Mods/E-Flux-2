package elec332.eflux2.tile;

import elec332.core.ElecCore;
import elec332.core.world.WorldHelper;
import elec332.eflux2.api.EFlux2API;
import elec332.eflux2.api.electricity.DefaultElectricityDevice;
import elec332.eflux2.api.electricity.IElectricityDevice;
import elec332.eflux2.api.electricity.IEnergyObject;
import elec332.eflux2.api.util.ConnectionPoint;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Elec332 on 20-2-2019
 */
public abstract class AbstractEnergyObjectTile extends AbstractEnergyTile implements IEnergyObject {

    public AbstractEnergyObjectTile() {
        connectionPointHandler.addListener(this::refreshCapability);
        connectionPointHandler.addListener(() -> {
            if (!world.isRemote) {
                WorldHelper.notifyNeighborsOfStateChange(world, getPos(), getBlockState().getBlock());
            }
        });
        connectionPoints = new Object[getPosts()];
    }

    protected final Object[] connectionPoints;
    private boolean initialized;
    protected LazyOptional<IElectricityDevice> cap;

    protected void refreshCapability() {
        cap.invalidate();
        final IElectricityDevice d = new DefaultElectricityDevice(this);
        cap = LazyOptional.of(() -> d);
    }

    protected abstract Object createConnectionPoint(int post);

    public Object getConnectionPointRef(int post) {
        return connectionPoints[post];
    }

    public Object getConnectionPointRef(Direction side, Vec3d hitVec) {
        return null;
    }

    @Nonnull
    @Override
    public ConnectionPoint getConnectionPoint(int post) {
        return connectionPointHandler.getStrict(getConnectionPointRef(post));
    }

    @Nullable
    @Override
    public ConnectionPoint getConnectionPoint(Direction side, Vec3d hitVec) {
        Object ref = getConnectionPointRef(side, hitVec);
        if (ref == null) {
            return null;
        }
        return connectionPointHandler.getStrict(ref);
    }

    @Override
    public void onLoad() {
        if (!initialized) {
            ElecCore.tickHandler.registerCall(() -> {
                for (int i = 0; i < connectionPoints.length; i++) {
                    connectionPoints[i] = createConnectionPoint(i);
                }
            }, getWorld());
        }
        super.onLoad();
        if (!initialized) {
            final IElectricityDevice d = new DefaultElectricityDevice(this);
            cap = LazyOptional.of(() -> d);
            ElecCore.tickHandler.registerCall(this::markDirty, getWorld());
            initialized = true;
            System.out.println("SETCAP");
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == EFlux2API.ELECTRICITY_CAP ? this.cap.cast() : super.getCapability(cap, side);
    }

}
