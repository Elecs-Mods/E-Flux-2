package elec332.test.tile;

import elec332.core.api.registration.RegisteredTileEntity;
import elec332.test.api.electricity.component.EnumElectricityType;
import elec332.test.simulation.IElectricityTransformer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

/**
 * Created by Elec332 on 17-2-2019
 */
@RegisteredTileEntity("tiletesttransformer")
public class TileTestTransformer extends AbstractEnergyObjectTile implements IElectricityTransformer {

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

    @Override
    public Object getConnectionPointRef(EnumFacing side, Vec3d hitVec) {
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
        return getConnectionPointRef(post);
    }

    @Override
    protected Object createConnectionPoint(int post) {
        if (post % 2 == 0) { //0 and 2
            return connectionPointHandler.makeConnectionPoint(getTileFacing(), post / 2, EnumFacing.DOWN);
        } else { // 1 and 3
            return connectionPointHandler.makeConnectionPoint(getTileFacing().getOpposite(), (post - 1) / 2, EnumFacing.DOWN);
        }
    }

}
