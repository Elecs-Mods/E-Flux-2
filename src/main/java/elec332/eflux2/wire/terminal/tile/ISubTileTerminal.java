package elec332.eflux2.wire.terminal.tile;

import elec332.core.tile.sub.ISubTileLogic;
import elec332.eflux2.wire.terminal.GroundTerminal;

import java.util.Collection;

/**
 * Created by Elec332 on 20-2-2018
 */
public interface ISubTileTerminal extends ISubTileLogic {

    public boolean addTerminal(GroundTerminal terminal);

    public Collection<GroundTerminal> getTerminalView();

}
