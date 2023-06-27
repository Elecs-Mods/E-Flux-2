package elec332.eflux2.modules.test.tile;

import elec332.core.api.registration.RegisteredTileEntity;
import elec332.core.tile.IActivatableTile;
import elec332.core.util.PlayerHelper;
import elec332.eflux2.api.electricity.component.EnumElectricityType;
import elec332.eflux2.simulation.IElectricityTransformer;
import elec332.eflux2.tile.AbstractEnergyObjectTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

/**
 * Created by Elec332 on 17-2-2019
 */
@RegisteredTileEntity("tiletesttransformer")
public class TileTestTransformer extends AbstractEnergyObjectTile implements IElectricityTransformer, IActivatableTile {

    @Override
    public double getInductance() {
        return 5;
    }

    @Override
    public double getRatio() {
        return 5;
    }

    @Nullable
    @Override
    public EnumElectricityType getEnergyType() {
        return EnumElectricityType.AC;
    }

    @Override
    public Object getConnectionPointRef(Direction side, Vec3d hitVec) {
        if (side.getAxis() != getTileFacing().getAxis()) {
            return null;
        }
        int post = 0;
        if (side == getTileFacing().getOpposite()) {
            post++;
        }
        if (hitVec.y > 0.5) {
            post += 2;
        }
        return getConnectionPointRef(post);
    }

    @Override
    protected Object createConnectionPoint(int post) {
        if (post % 2 == 0) { //0 and 2
            return connectionPointHandler.makeConnectionPoint(getTileFacing(), post / 2, Direction.DOWN);
        } else { // 1 and 3
            return connectionPointHandler.makeConnectionPoint(getTileFacing().getOpposite(), (post - 1) / 2, Direction.DOWN);
        }
    }

    @Override
    public ActionResultType onBlockActivated(PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        PlayerHelper.sendMessageToPlayer(player, connectionPointHandler.toString());
        return ActionResultType.SUCCESS;
    }

}
