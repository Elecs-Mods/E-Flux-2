package elec332.eflux2.modules.wires.wire.ground;

import net.minecraft.util.Direction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by Elec332 on 23-2-2019
 */
public interface IWireContainer {

    boolean addWire(GroundWire wire);

    @Nullable
    GroundWire getWire(Direction facing);

    @Nonnull
    List<GroundWire> getWireView();

    default boolean isRealWireContainer() {
        return true;
    }

}
