package elec332.test.electricity.simulation.optimization;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import elec332.test.api.electricity.component.AbstractResistorElement;
import elec332.test.api.electricity.component.CircuitElement;
import elec332.test.api.electricity.component.optimization.CompressedCircuitElement;
import elec332.test.api.electricity.component.optimization.ICircuitCompressor;
import elec332.test.api.util.ConnectionPoint;

import java.util.Collection;
import java.util.List;

/**
 * Created by Elec332 on 14-11-2017.
 */
public final class ResistorOptimizer implements ICircuitCompressor {

    @Override
    public Multimap<CompressedCircuitElement, CircuitElement> compress(List<CircuitElement> elements, Multimap<ConnectionPoint, CircuitElement<?>> map2) {
        Multimap<CompressedCircuitElement, CircuitElement> ret = HashMultimap.create();
        whileloop:
        while (true) { //Circumvent CME's
            /////////////////////////// TEST
            for (CircuitElement<?> c1 : elements) {
                if (c1 instanceof AbstractResistorElement && !((AbstractResistorElement) c1).isPolarityAgnostic()) {
                    AbstractResistorElement re = (AbstractResistorElement) c1;
                    List<ConnectionPoint> cp = c1.getConnectionPoints();
                    if (cp.size() != 2) {
                        throw new RuntimeException();
                    }
                    List<AbstractResistorElement> list = Lists.newArrayList();
                    ConnectionPoint cp1 = trace(list, re, cp.get(0), map2);
                    list.add(re);
                    ConnectionPoint cp2 = trace(list, re, cp.get(1), map2);
                    if (list.size() == 1) {
                        continue;
                    }
                    CompressedCircuitElement ce = new CombinedResistorElement(cp1, cp2, list);
                    ret.putAll(ce, list);
                    elements.removeAll(list);
                    elements.add(ce);
                    continue whileloop;
                }
            }
            ///////////////////////

            for (CircuitElement<?> c1 : elements) {
                if (c1 instanceof AbstractResistorElement && ((AbstractResistorElement) c1).isPolarityAgnostic()) {
                    AbstractResistorElement re = (AbstractResistorElement) c1;
                    List<ConnectionPoint> cp = c1.getConnectionPoints();
                    if (cp.size() != 2) {
                        throw new RuntimeException();
                    }
                    List<AbstractResistorElement> list = Lists.newArrayList();
                    ConnectionPoint cp1 = trace(list, re, cp.get(0), map2);
                    list.add(re);
                    ConnectionPoint cp2 = trace(list, re, cp.get(1), map2);
                    if (list.size() == 1) {
                        continue;
                    }
                    CompressedCircuitElement ce = new CombinedResistorElement(cp1, cp2, list);
                    ret.putAll(ce, list);
                    elements.removeAll(list);
                    elements.add(ce);
                    continue whileloop;
                }
            }

            return ret;
        }
    }

    private ConnectionPoint trace(List<AbstractResistorElement> list, AbstractResistorElement<?> re, ConnectionPoint cp, Multimap<ConnectionPoint, CircuitElement<?>> map) {
        loop:
        while (true) {
            Collection<CircuitElement<?>> ceL = map.get(cp);
            if (ceL.size() != 2) {
                return cp;
            }
            for (CircuitElement<?> ce : ceL) {
                if (ce != re) {
                    if (ce instanceof AbstractResistorElement && (((AbstractResistorElement) ce).isPolarityAgnostic())) {
                        re = (AbstractResistorElement<?>) ce;
                        if (re.getConnectionPoints().size() != 2) {
                            throw new RuntimeException();
                        }
                        list.add(re);
                        boolean first = true;
                        for (ConnectionPoint co : re.getConnectionPoints()) {
                            if (!co.equals(cp)) {
                                if (first) {
                                    re.combineData = true;
                                }
                                cp = co;
                                continue loop;
                            }
                            first = false;
                        }
                        throw new RuntimeException();
                    } else {
                        return cp;
                    }
                }
            }
            throw new RuntimeException();
        }

    }

    private class CombinedResistorElement extends CompressedCircuitElement<AbstractResistorElement> {

        CombinedResistorElement(ConnectionPoint start, ConnectionPoint end, List<AbstractResistorElement> res) {
            super(start, end, res);
            this.resistance = 0.0;
            for (AbstractResistorElement re : elements) {
                this.resistance += re.getResistance();
            }
        }

        private double resistance;

        @Override
        public void setNodeVoltage(int n, double c) {
            super.setNodeVoltage(n, c);
        }

        @Override
        protected void calculateCurrent() {
            current = getVoltageDiff() / resistance;
        }

        @Override
        public void stamp() {
            getCircuit().stampResistor(nodes[0], nodes[1], resistance);
        }

        @Override
        public void preApply() {
            double powerLeft = volts[0], powerRight = volts[1], diff = Math.abs(getVoltageDiff()), currentLeft = powerLeft;
            boolean invert = powerLeft > powerRight;
            for (AbstractResistorElement are : elements) {
                double part = are.getResistance() / resistance;
                boolean swap = (are.combineData && !are.isPolarityAgnostic()) || (are.isPolarityAgnostic() && !invert);
                int zero = swap ? 1 : 0;
                int one = swap ? 0 : 1;
                are.setNodeVoltage(zero, currentLeft);
                if (invert) {
                    currentLeft = Math.max(currentLeft - diff * part, powerRight);
                } else {
                    currentLeft = Math.min(currentLeft + diff * part, powerRight);
                }
                are.setNodeVoltage(one, currentLeft);
            }
        }

        @Override
        public String toString() {
            return super.toString() + " -> ResistorElementOptimized: R=" + resistance;
        }

    }

}
