package elec332.eflux2.inventory.window;

import elec332.core.api.network.ElecByteBuf;
import elec332.core.inventory.window.IWindowHandler;
import elec332.core.inventory.window.Window;
import elec332.core.inventory.window.WindowManager;
import elec332.core.world.WorldHelper;
import elec332.eflux2.tile.AbstractEnergyObjectTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Created by Elec332 on 20-2-2019
 */
public enum EFluxWindowHandler implements IWindowHandler {

    INSTANCE;

    public static void openGui(PlayerEntity player, BlockPos pos, int id, Consumer<ElecByteBuf> data) {
        WindowManager.openWindow(player, INSTANCE, b -> {
            b.writeBoolean(true);
            b.writeBlockPos(pos);
            b.writeVarInt(id);
            data.accept(b);
        });
    }

    public static void openGui(PlayerEntity player, int id) {
        WindowManager.openWindow(player, INSTANCE, b -> {
            b.writeBoolean(false);
            b.writeVarInt(id);
        });
    }

    @Override
    public Window createWindow(PlayerEntity player, World world, ElecByteBuf data) {
        if (data.readBoolean()) {
            return create(player, world, data.readBlockPos(), data);
        }
        return create(player, world, data);
    }

    private Window create(PlayerEntity player, World world, BlockPos pos, ElecByteBuf data) {
        switch (data.readVarInt()) {
            case 0:
                int[] b = data.readVarIntArray();
                AbstractEnergyObjectTile tile = (AbstractEnergyObjectTile) WorldHelper.getTileAt(world, pos);
                return new WindowOutputColorSelector(tile.getConnectionPointHandler(), Arrays.stream(b).mapToObj(tile::getConnectionPointRef).toArray());
            default:
                return null;
        }
    }

    private Window create(PlayerEntity player, World world, ElecByteBuf data) {
        switch (data.readVarInt()) {
            default:
                return null;
        }
    }

}
