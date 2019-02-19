package elec332.test.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;

/**
 * Created by Elec332 on 9-2-2019
 */
public interface IActivatableTile {

    public default boolean onBlockActivated(EntityPlayer player, EnumHand hand, RayTraceResult hit) {
        return false;
    }

}
