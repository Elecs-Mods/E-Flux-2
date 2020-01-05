package elec332.eflux2.electricity.simulation.engine;

import elec332.eflux2.api.electricity.component.CircuitElement;

final class CircuitNodeLink {

    int num; //post
    CircuitElement elm;

    @Override
    public String toString() {
        return "Post: " + num + "   " + elm;
    }

}
