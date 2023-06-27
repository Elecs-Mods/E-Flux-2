package elec332.eflux2.modules.wires.wire;

import elec332.eflux2.api.electricity.IEnergyObject;
import elec332.eflux2.api.electricity.component.EnumElectricityType;
import elec332.eflux2.api.util.ConnectionPoint;
import elec332.eflux2.api.wire.WireConnectionMethod;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * Created by Elec332 on 10-2-2019
 */
@Immutable
public abstract class AbstractWire implements IEnergyObject {

    public AbstractWire(@Nonnull ConnectionPoint start, @Nonnull ConnectionPoint end, @Nonnull WireData wireData) {
        this.n = start.hashCode() > end.hashCode();
        this.startPoint = n ? start : end;
        this.endPoint = n ? end : start;
        this.wireData = wireData;
    }

    protected final ConnectionPoint startPoint, endPoint;
    protected final WireData wireData;
    protected final boolean n;

    public abstract double getResistance();

    public boolean isOverhead() {
        return wireData.getConnectionMethod() == WireConnectionMethod.OVERHEAD;
    }

    @Override
    public EnumElectricityType getEnergyType() {
        return null;
    }

    public void setPowerTest(double v, double a) {

    }

    @Nonnull
    @Override
    public ConnectionPoint getConnectionPoint(int post) {
        return post == 0 ? startPoint : endPoint;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AbstractWire && ((AbstractWire) obj).startPoint.equals(startPoint) && ((AbstractWire) obj).endPoint.equals(endPoint) && ((AbstractWire) obj).wireData.equals(wireData);
    }

}
