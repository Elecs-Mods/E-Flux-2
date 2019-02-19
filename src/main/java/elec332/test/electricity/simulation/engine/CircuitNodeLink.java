package elec332.test.electricity.simulation.engine;

import elec332.test.api.electricity.component.CircuitElement;

final class CircuitNodeLink {

    int num; //post
    CircuitElement elm;

    @Override
    public String toString() {
        return "Post: " + num + "   " + elm;
    }

}
