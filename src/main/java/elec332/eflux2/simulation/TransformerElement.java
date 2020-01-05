package elec332.eflux2.simulation;

import elec332.eflux2.api.electricity.component.CircuitElement;
import elec332.eflux2.api.electricity.component.EnumElectricityType;

/**
 * Created by Elec332 on 5-1-2018.
 */
public class TransformerElement extends CircuitElement<IElectricityTransformer> {

    public TransformerElement(IElectricityTransformer energyTile) {
        super(energyTile);
        if (energyTile.getEnergyType() != EnumElectricityType.AC) {
            throw new IllegalArgumentException();
        }
    }

    @SuppressWarnings("all")
    private final double couplingCoef = 0.9999;

    @Override
    public int getPostCount() {
        return 4;
    }

    @Override
    public void stamp() {
        // equations for transformer:
        //   v1 = L1 di1/dt + M  di2/dt
        //   v2 = M  di1/dt + L2 di2/dt
        // we invert that to get:
        //   di1/dt = a1 v1 + a2 v2
        //   di2/dt = a3 v1 + a4 v2
        // integrate di1/dt using trapezoidal approx and we get:
        //   i1(t2) = i1(t1) + dt/2 (i1(t1) + i1(t2))
        //          = i1(t1) + a1 dt/2 v1(t1) + a2 dt/2 v2(t1) +
        //                     a1 dt/2 v1(t2) + a2 dt/2 v2(t2)
        // the norton equivalent of this for i1 is:
        //  a. current source, I = i1(t1) + a1 dt/2 v1(t1) + a2 dt/2 v2(t1)
        //  b. resistor, G = a1 dt/2
        //  c. current source controlled by voltage v2, G = a2 dt/2
        // and for i2:
        //  a. current source, I = i2(t1) + a3 dt/2 v1(t1) + a4 dt/2 v2(t1)
        //  b. resistor, G = a3 dt/2
        //  c. current source controlled by voltage v2, G = a4 dt/2
        //
        // For backward euler,
        //
        //   i1(t2) = i1(t1) + a1 dt v1(t2) + a2 dt v2(t2)
        //
        // So the current source value is just i1(t1) and we use
        // dt instead of dt/2 for the resistor and VCCS.
        //
        // first winding goes from node 0 to 2, second is from 1 to 3
        double l1 = energyTile.getInductance();
        double l2 = energyTile.getInductance() * energyTile.getRatio() * energyTile.getRatio();
        double m = 0.999 * Math.sqrt(l1 * l2);
        // build inverted matrix
        double deti = 1 / (l1 * l2 - m * m);
        double ts = (1.0 / 20.0);
        double a1 = l2 * deti * ts;
        double a2 = -m * deti * ts;
        double a3 = -m * deti * ts;
        double a4 = l1 * deti * ts;
        getCircuit().stampConductance(nodes[0], nodes[2], a1);
        getCircuit().stampVCCurrentSource(nodes[0], nodes[2], nodes[1], nodes[3], a2);
        getCircuit().stampVCCurrentSource(nodes[1], nodes[3], nodes[0], nodes[2], a3);
        getCircuit().stampConductance(nodes[1], nodes[3], a4);
        getCircuit().stampRightSide(nodes[0]);
        getCircuit().stampRightSide(nodes[1]);
        getCircuit().stampRightSide(nodes[2]);
        getCircuit().stampRightSide(nodes[3]);
    }

    @Override
    public EnumElectricityType getProvidedPowerType(int post) {
        return EnumElectricityType.AC;
    }

    @Override
    public double getMaxProvidedPower(int post) {
        if (post == 0 || post == 2) {
            return (volts[0] - volts[2]) * getCurrent();
        } else {
            return (volts[1] - volts[3]) * getCurrent();
        }
    }

    @Override
    public boolean getConnection(int n1, int n2) {
        return comparePair(n1, n2, 0, 2) || comparePair(n1, n2, 1, 3);
    }

}
