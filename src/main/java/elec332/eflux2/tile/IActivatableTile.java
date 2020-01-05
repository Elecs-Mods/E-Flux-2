package elec332.eflux2.tile;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;

/**
 * Created by Elec332 on 9-2-2019
 */
public interface IActivatableTile {

    public default boolean onBlockActivated(PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        return false;
    }

}
