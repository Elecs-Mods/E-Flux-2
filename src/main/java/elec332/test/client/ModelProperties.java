package elec332.test.client;

import elec332.test.wire.ground.tile.IWireContainer;
import elec332.test.wire.terminal.tile.ISubTileTerminal;
import net.minecraftforge.client.model.data.ModelProperty;

/**
 * Created by Elec332 on 22-6-2019
 */
public class ModelProperties {

    public static final ModelProperty<IWireContainer> WIRE = new ModelProperty<>();
    public static final ModelProperty<ISubTileTerminal> TERMINAL = new ModelProperty<>();

}
