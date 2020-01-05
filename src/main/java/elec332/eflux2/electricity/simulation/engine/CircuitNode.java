package elec332.eflux2.electricity.simulation.engine;

import elec332.eflux2.api.util.ConnectionPoint;

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
