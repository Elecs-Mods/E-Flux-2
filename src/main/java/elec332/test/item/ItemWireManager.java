package elec332.test.item;

import elec332.core.item.AbstractItem;
import elec332.core.world.WorldHelper;
import elec332.test.inventory.window.EFluxWindowHandler;
import elec332.test.tile.AbstractEnergyObjectTile;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;

/**
 * Created by Elec332 on 20-2-2019
 */
public class ItemWireManager extends AbstractItem {

    public ItemWireManager(Properties itemBuilder) {
        super(itemBuilder);
    }

    @Override
    public EnumActionResult onItemUseFirst(ItemStack stack, ItemUseContext context) {
        TileEntity tile = WorldHelper.getTileAt(context.getWorld(), context.getPos());
        if (tile instanceof AbstractEnergyObjectTile) {
            if (!context.getWorld().isRemote()) {
                int p = ((AbstractEnergyObjectTile) tile).getPosts();
                final int[] data = new int[p];
                for (int i = 0; i < p; i++) {
                    data[i] = i;
                }
                EFluxWindowHandler.openGui(context.getPlayer(), context.getPos(), 0, b -> b.writeVarIntArray(data));
            }
            return EnumActionResult.SUCCESS;
        }
        return super.onItemUse(context);
    }

}
