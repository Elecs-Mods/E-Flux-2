package elec332.test.wire.terminal.tile;

import elec332.test.tile.ISubTileLogic;
import elec332.test.wire.terminal.GroundTerminal;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Created by Elec332 on 20-2-2018
 */
public interface ISubTileTerminal extends ISubTileLogic {

    public boolean addTerminal(GroundTerminal terminal);

    public Collection<GroundTerminal> getTerminalView();

}
