package elec332.test.wire.ground.tile;

import elec332.test.tile.ISubTileLogic;
import elec332.test.wire.ground.GroundWire;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by Elec332 on 20-2-2018
 */
public interface ISubTileWire extends ISubTileLogic {

    public boolean addWire(GroundWire wire);

    @Nullable
    public GroundWire getWire(EnumFacing facing);

    @Nonnull
    public List<GroundWire> getWireView();

}
