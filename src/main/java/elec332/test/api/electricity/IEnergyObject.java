package elec332.test.api.electricity;

import elec332.test.api.electricity.component.EnumElectricityType;
import elec332.test.api.util.ConnectionPoint;
import elec332.test.api.wire.IWireData;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Elec332 on 12-11-2017.
 * <p>
 * * NOTE: All functions defined in this interface, and all functions in interfaces that are
 * subclassed by this interface can be called asynchronously!
 */
public interface IEnergyObject {

    default public boolean canConnectTo(IWireData wireData) {
        return true;
    }

    @Nullable
    public EnumElectricityType getEnergyType();

    @Nonnull
    public ConnectionPoint getConnectionPoint(int post);

    @Nullable
    default public ConnectionPoint getConnectionPoint(EnumFacing side, Vec3d hitVec) {
        return null;
    }

    default public int getPosts() {
        return 2;
    }

    default public String getDescription(int post) {
        return toString() + " post " + post;
    }

    default public boolean isPassiveConnector() {
        return false;
    }

}
