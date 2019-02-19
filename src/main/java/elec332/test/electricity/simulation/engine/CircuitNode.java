package elec332.test.electricity.simulation.engine;

import elec332.test.api.util.ConnectionPoint;

import java.util.Vector;

final class CircuitNode {

    CircuitNode() {
        links = new Vector<>();
    }

    ConnectionPoint cp;
    Vector<CircuitNodeLink> links;
    boolean internal;

    @Override
    public String toString() {
        return cp + "  " + links;
    }

}
