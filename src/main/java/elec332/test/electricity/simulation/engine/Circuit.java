package elec332.test.electricity.simulation.engine;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import elec332.core.util.IdentityList;
import elec332.test.api.electricity.component.CircuitElement;
import elec332.test.api.electricity.component.ICircuit;
import elec332.test.api.electricity.component.ICircuitPart;
import elec332.test.api.electricity.component.optimization.CompressedCircuitElement;
import elec332.test.api.electricity.component.optimization.ICircuitCompressor;
import elec332.test.api.util.ConnectionPoint;
import elec332.test.electricity.simulation.CircuitElementFactory;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Created by Elec332 on 11-11-2017.
 */
public final class Circuit implements ICircuit {

    public final UUID uuid = UUID.randomUUID();

    boolean needsRebuild;
    boolean rebuilding = false;
    double circuitMatrix[][], circuitRightSide[], origRightSide[], origMatrix[][];
    RowInfo circuitRowInfo[];
    int circuitPermute[];
    boolean circuitNonLinear;
    int voltageSourceCount;
    int circuitMatrixSize, circuitMatrixFullSize;
    boolean circuitNeedsMap;
    CircuitElement stopElm;
    boolean dumpMatrix;
    int steps = 0;
    Vector<CircuitNode> nodeList;
    CircuitElement voltageSources[];
    boolean converged;
    int subIterations;
    String stopMessage;
    Multimap<Class, CircuitElement> sortedElements = HashMultimap.create();
    private Set<CircuitElement<?>> elmList = Sets.newHashSet();
    private List<CircuitElement<?>> compressedElm = null;
    private List<ICircuitPart> circuitParts = Lists.newArrayList();

    List<CircuitElement<?>> getCompressedElementList() {
        if (compressedElm == null) {
            compressList();
        }
        return compressedElm;
    }

    public List<ICircuitPart> getCircuitParts() {
        return circuitParts;
    }

    public boolean isRebuilding() {
        //synchronized (uuid){
        return rebuilding;
        //}
    }

    private void compressList() {
        long time = System.currentTimeMillis();
        List<CircuitElement<?>> copy = Lists.newArrayList(elmList), immC = Collections.unmodifiableList(copy);
        Multimap<ConnectionPoint, CircuitElement<?>> data = getData(copy);
        if (false)
            for (ICircuitCompressor c : CircuitElementFactory.INSTANCE.getCircuitOptimizers()) {
                Multimap<CompressedCircuitElement, CircuitElement> p = c.compress(Lists.newArrayList(immC), data);
                if (p.keySet().size() > 0) {
                    boolean doneWork = false;
                    for (CompressedCircuitElement ce1 : p.keySet()) {
                        Collection<CircuitElement> r = p.get(ce1);
                        if (r == null || r.size() == 0) {
                            continue;
                        }
                        doneWork = true;
                        copy.removeAll(p.get(ce1));
                        copy.add(ce1);
                        ce1.setCircuit(this);
                    }
                    if (doneWork) {
                        data = getData(copy);
                    }
                }
            }
        this.compressedElm = copy;
        System.out.println("Compression took: " + (System.currentTimeMillis() - time));
    }

    public void postAnalyze() {
        circuitParts.clear();
        long test = System.nanoTime();
        List<Set<Integer>> cubCir = new IdentityList<>();
        Multimap<Integer, Pair<CircuitElement, Integer>> data = HashMultimap.create();
        for (CircuitNode node : nodeList) {
            Set<Integer> myCir = Sets.newHashSet();
            for (CircuitNodeLink link : node.links) {
                CircuitElement elm = link.elm;
                int n = elm.getNode(link.num);
                data.get(n).add(Pair.of(elm, link.num));
                myCir.add(n);
                for (int i = 0; i < elm.getPostCount(); i++) {
                    if (i != link.num && elm.hasGalvanicConnection(link.num, i)) {
                        n = elm.getNode(i);
                        data.get(n).add(Pair.of(elm, i));
                        myCir.add(n);
                    }
                }
            }
            boolean b = true;
            whC:
            while (true) {
                for (Set<Integer> sl : cubCir) {
                    if (sl == myCir) {
                        continue;
                    }
                    for (int i : myCir) {
                        if (sl.contains(i)) {
                            cubCir.remove(myCir);
                            sl.addAll(myCir);
                            myCir = sl;
                            b = false;
                            continue whC;
                        }
                    }
                }
                break;
            }
            if (b) {
                cubCir.add(myCir);
            }
        }
        long diff = System.nanoTime() - test;
        System.out.println(" Took: " + (diff / 10e6));
        System.out.println(cubCir);
        System.out.println(data);
        for (Collection<Integer> c : cubCir) {
            System.out.println(" --");
            final Multimap<CircuitElement, Integer> eic = HashMultimap.create();
            c.stream()
                    .map(data::get)
                    .flatMap(Collection::stream)
                    .forEach(p -> eic.put(p.getLeft(), p.getRight()));
            ICircuitPart cp = () -> eic;
            eic.keys().forEach(ce -> ce.addCircuitPart(cp));
            circuitParts.add(cp);
            eic.forEach((ce, ic) -> System.out.println(ic + "  " + ce));
            System.out.println("------");
        }
        System.out.println(" CIRCINTERNAL = " + cubCir.size());
    }

    private Multimap<ConnectionPoint, CircuitElement<?>> getData(List<CircuitElement<?>> elements) {
        Multimap<ConnectionPoint, CircuitElement<?>> ret2 = HashMultimap.create();
        elements.forEach(e -> e.getConnectionPoints().forEach(c -> ret2.put(c, e)));
        return ret2;
    }

    public void addElement(CircuitElement<?> element) {
        if (element.getCircuit() != null && element.getCircuit() != this) {
            throw new RuntimeException();
        }
        if (elmList.add(element)) {
            sortedElements.put(element.getClass(), element);
            element.setCircuit(this);
            compressedElm = null;
            circuitParts.clear();
            needsRebuild = true;
        }
    }

    public void consumeCircuit(Circuit circuit) {
        circuit.elmList.forEach(element -> {
            element.setCircuit(null);
            Circuit.this.addElement(element);
        });
    }

    public boolean removeElement(Collection<CircuitElement<?>> elementsToRemove, Set<CircuitElement<?>> reAdd) {
        elmList.removeAll(elementsToRemove);
        elementsToRemove.forEach(element -> sortedElements.remove(element.getClass(), element));
        compressedElm = null;
        circuitParts.clear();
        needsRebuild = true;
        elmList.forEach(element1 -> element1.setCircuit(null));
        reAdd.addAll(elmList);
        return true;
    }

    public void clear() {
        elmList.clear();
        sortedElements.clear();
        sortedElements.clear();
        compressedElm = null;
        circuitParts.clear();
        if (nodeList != null) {
            nodeList.clear();
        }
    }

    CircuitNode getCircuitNode(int n) {
        if (n >= nodeList.size()) {
            return null;
        }
        return nodeList.elementAt(n);
    }

    CircuitElement getElm(int n) {
        if (n >= compressedElm.size()) {
            return null;
        }
        return compressedElm.get(n);
    }

    @Override
    public UUID getId() {
        return uuid;
    }

    // control voltage source vs with voltage from n1 to n2 (must
    // also call stampVoltageSource())
    @Override
    public void stampVCVS(int n1, int n2, double coef, int vs) {
        int vn = nodeList.size() + vs;
        stampMatrix(vn, n1, coef);
        stampMatrix(vn, n2, -coef);
    }

    // stamp independent voltage source #vs, from n1 to n2, amount v
    @Override
    public void stampVoltageSource(int n1, int n2, int vs, double v) {
        int vn = nodeList.size() + vs;
        stampMatrix(vn, n1, -1);
        stampMatrix(vn, n2, 1);
        stampRightSide(vn, v);
        stampMatrix(n1, vn, 1);
        stampMatrix(n2, vn, -1);
    }

    // use this if the amount of voltage is going to be updated in doStep()
    @Override
    public void stampVoltageSource(int n1, int n2, int vs) {
        int vn = nodeList.size() + vs;
        stampMatrix(vn, n1, -1);
        stampMatrix(vn, n2, 1);
        stampRightSide(vn);
        stampMatrix(n1, vn, 1);
        stampMatrix(n2, vn, -1);
    }

    @Override
    public void updateVoltageSource(int n1, int n2, int vs, double v) {
        int vn = nodeList.size() + vs;
        stampRightSide(vn, v);
    }

    @Override
    public void stampResistor(int n1, int n2, double r) {
        double r0 = 1 / r;
        if (Double.isNaN(r0) || Double.isInfinite(r0)) {
            System.out.print("bad resistance " + r + " " + r0 + "\n");
        }
        stampConductance(n1, n2, r0);
    }

    @Override
    public void stampConductance(int n1, int n2, double r0) {
        stampMatrix(n1, n1, r0);
        stampMatrix(n2, n2, r0);
        stampMatrix(n1, n2, -r0);
        stampMatrix(n2, n1, -r0);
    }

    // current from cn1 to cn2 is equal to voltage from vn1 to 2, divided by g
    @Override
    public void stampVCCurrentSource(int cn1, int cn2, int vn1, int vn2, double g) {
        stampMatrix(cn1, vn1, g);
        stampMatrix(cn2, vn2, g);
        stampMatrix(cn1, vn2, -g);
        stampMatrix(cn2, vn1, -g);
    }

    @Override
    public void stampCurrentSource(int n1, int n2, double i) {
        stampRightSide(n1, -i);
        stampRightSide(n2, i);
    }

    // stamp a current source from n1 to n2 depending on current through vs
    @Override
    public void stampCCCS(int n1, int n2, int vs, double gain) {
        int vn = nodeList.size() + vs;
        stampMatrix(n1, vn, gain);
        stampMatrix(n2, vn, -gain);
    }

    // stamp value x in row i, column j, meaning that a voltage change
    // of dv in node j will increase the current into node i by x dv.
    // (Unless i or j is a voltage source node.)
    @Override
    public void stampMatrix(int i, int j, double x) {
        if (i > 0 && j > 0) {
            if (circuitNeedsMap) {
                i = circuitRowInfo[i - 1].mapRow;
                RowInfo ri = circuitRowInfo[j - 1];
                if (ri.type == RowInfo.ROW_CONST) {
                    circuitRightSide[i] -= x * ri.value;
                    return;
                }
                j = ri.mapCol;
            } else {
                i--;
                j--;
            }
            circuitMatrix[i][j] += x;
        }
    }

    // stamp value x on the right side of row i, representing an
    // independent current source flowing into node i
    @Override
    public void stampRightSide(int i, double x) {
        if (i > 0) {
            if (circuitNeedsMap) {
                i = circuitRowInfo[i - 1].mapRow;
            } else {
                i--;
            }
            circuitRightSide[i] += x;
        }
    }

    // indicate that the value on the right side of row i changes in doStep()
    @Override
    public void stampRightSide(int i) {
        if (i > 0) {
            circuitRowInfo[i - 1].rsChanges = true;
        }
    }

    // indicate that the values on the left side of row i change in doStep()
    @Override
    public void stampNonLinear(int i) {
        if (i > 0) {
            circuitRowInfo[i - 1].lsChanges = true;
        }
    }

    void stop(String s, CircuitElement ce) {
        System.out.println("ERROR: " + s);
        stopMessage = s;
        circuitMatrix = null;
        stopElm = ce;
        for (int i = 0; i < 10; i++) {
            System.out.println("ERROR: " + s);
        }
        System.out.println("ERROR: " + s);
        throw new RuntimeException();
    }

}
