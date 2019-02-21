package elec332.test.tile;

import elec332.core.world.WorldHelper;
import elec332.test.api.TestModAPI;
import elec332.test.api.electricity.DefaultElectricityDevice;
import elec332.test.api.electricity.IElectricityDevice;
import elec332.test.api.electricity.IEnergyObject;
import elec332.test.api.util.ConnectionPoint;
import net.minecraft.util.EnumFacing;
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

    public Object getConnectionPointRef(EnumFacing side, Vec3d hitVec) {
        return null;
    }

    @Nonnull
    @Override
    public ConnectionPoint getConnectionPoint(int post) {
        return connectionPointHandler.getStrict(getConnectionPointRef(post));
    }

    @Nullable
    @Override
    public ConnectionPoint getConnectionPoint(EnumFacing side, Vec3d hitVec) {
        Object ref = getConnectionPointRef(side, hitVec);
        if (ref == null) {
            return null;
        }
        return connectionPointHandler.getStrict(ref);
    }

    @Override
    public void onLoad() {
        if (!initialized) {
            for (int i = 0; i < connectionPoints.length; i++) {
                connectionPoints[i] = createConnectionPoint(i);
            }
        }
        super.onLoad();
        if (!initialized) {
            final IElectricityDevice d = new DefaultElectricityDevice(this);
            cap = LazyOptional.of(() -> d);
            markDirty();
            initialized = true;
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
        return cap == TestModAPI.ELECTRICITY_CAP ? this.cap.cast() : super.getCapability(cap, side);
    }

}
