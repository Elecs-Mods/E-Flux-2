package elec332.test.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by Elec332 on 20-2-2018
 */
public abstract class SubTileLogicBase implements ISubTileLogic {

    public SubTileLogicBase(Data data) {
        this.tile = data.tile;
        this.id = data.id;
    }

    private final TileMultiObject tile;
    protected final int id;

    @Override
    public final World getWorld() {
        return tile.getWorld();
    }

    @Override
    public final boolean hasWorld() {
        return tile.hasWorld();
    }

    @Override
    public final BlockPos getPos() {
        return tile.getPos();
    }

    @Override
    public final void markDirty() {
        tile.markDirty();
    }

    @Override
    public final void sendPacket(int ID, NBTTagCompound data) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.putInt("kid", ID);
        tag.put("data", data);
        tile.sendPacket(id, tag);
    }

    public static class Data {

        Data(TileMultiObject tile, int id) {
            this.tile = tile;
            this.id = id;
        }

        private final TileMultiObject tile;
        private final int id;

    }

}