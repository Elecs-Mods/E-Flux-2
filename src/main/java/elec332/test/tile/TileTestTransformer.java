package elec332.test.tile;

import elec332.core.api.registration.RegisteredTileEntity;
import elec332.core.tile.AbstractTileEntity;
import elec332.test.api.TestModAPI;
import elec332.test.api.electricity.DefaultElectricityDevice;
import elec332.test.api.electricity.component.EnumElectricityType;
import elec332.test.api.util.ConnectionPoint;
import elec332.test.simulation.IElectricityTransformer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Elec332 on 17-2-2019
 */
@RegisteredTileEntity("tiletesttransformer")
public class TileTestTransformer extends AbstractTileEntity implements IElectricityTransformer {

    @Override
    public double getInductance() {
        return 5;
    }

    @Override
    public double getRatio() {
        return 5;
    }

    @Nullable
    @Override
    public EnumElectricityType getEnergyType() {
        return EnumElectricityType.AC;
    }

    @Nonnull
    @Override
    public ConnectionPoint getConnectionPoint(int post) {
        return p[post];
    }

    @Nullable
    @Override
    public ConnectionPoint getConnectionPoint(EnumFacing side, Vec3d hitVec) {
        if (side.getAxis() != getTileFacing().getAxis()) {
            return null;
        }
        int post = 0;
        if (side == getTileFacing().getOpposite()) {
            post++;
        }
        if (hitVec.y > 0.5) {
            post += 2;
        }
        return getConnectionPoint(post);
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

    @Override
    @SuppressWarnings("all")
    public <T> LazyOptional<T> getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == TestModAPI.ELECTRICITY_CAP ? LazyOptional.of(() -> new DefaultElectricityDevice(this)).cast() : super.getCapability(capability, facing);
    }

    private ConnectionPoint[] p = new ConnectionPoint[getPosts()];

    private void createConnectionPoints() {
        p[1] = new ConnectionPoint(pos, world, getTileFacing().getOpposite(), 1, EnumFacing.DOWN);
        p[3] = new ConnectionPoint(pos, world, getTileFacing().getOpposite(), 2, EnumFacing.DOWN);

        p[0] = new ConnectionPoint(pos, world, getTileFacing(), 1, EnumFacing.DOWN);
        p[2] = new ConnectionPoint(pos, world, getTileFacing(), 2, EnumFacing.DOWN);
    }

}
