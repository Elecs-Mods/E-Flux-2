package elec332.test.api.electricity.component.optimization;

import com.google.common.collect.Lists;
import elec332.test.api.electricity.IEnergyObject;
import elec332.test.api.electricity.component.CircuitElement;
import elec332.test.api.electricity.component.EnumElectricityType;
import elec332.test.api.util.ConnectionPoint;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by Elec332 on 15-11-2017.
 */
public abstract class CompressedCircuitElement<T extends CircuitElement> extends CircuitElement<IEnergyObject> {

    protected CompressedCircuitElement(ConnectionPoint start, ConnectionPoint end, List<T> elements) {
        this(new IEnergyObject() {

            @Nullable
            @Override
            public EnumElectricityType getEnergyType() {
                return null;
            }

            @Nonnull
            @Override
            public ConnectionPoint getConnectionPoint(int post) {
                return post == 0 ? start : end;
            }

            @Nullable
            @Override
            public ConnectionPoint getConnectionPoint(EnumFacing side, Vec3d hitVec) {
                throw new RuntimeException();
            }

        }, elements);
    }

    protected CompressedCircuitElement(IEnergyObject energyTile, List<T> elements) {
        super(energyTile);
        if (elements == null) {
            elements = Lists.newArrayList();
        }
        this.elements = elements;
    }

    protected final List<T> elements;

    @Override
    public final void apply() {
        for (T t : elements) {
            t.apply();
        }
    }

    @Override
    public String toString() {
        return "CompressedCircuitElement(" + elements.size() + ")" + elements;
    }

}
