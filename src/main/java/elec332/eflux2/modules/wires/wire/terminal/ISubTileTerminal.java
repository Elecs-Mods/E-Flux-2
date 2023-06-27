package elec332.eflux2.modules.wires.wire.terminal;

import elec332.core.tile.sub.ISubTileLogic;
import elec332.eflux2.modules.wires.wire.terminal.GroundTerminal;

import java.util.Collection;

/**
 * Created by Elec332 on 20-2-2018
 */
public interface ISubTileTerminal extends ISubTileLogic {

    boolean addTerminal(GroundTerminal terminal);

    Collection<GroundTerminal> getTerminalView();

}
