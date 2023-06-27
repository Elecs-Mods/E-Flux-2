package elec332.eflux2.modules.wires.client;

import elec332.eflux2.modules.wires.wire.ground.GroundWire;
import elec332.eflux2.modules.wires.wire.terminal.GroundTerminal;
import net.minecraftforge.client.model.data.ModelProperty;

import java.util.Collection;

/**
 * Created by Elec332 on 22-6-2019
 */
public class ModelProperties {

    public static final ModelProperty<Collection<GroundWire>> WIRE = new ModelProperty<>();
    public static final ModelProperty<Collection<GroundTerminal>> TERMINAL = new ModelProperty<>();

}
