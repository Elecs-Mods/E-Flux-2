package elec332.test.block;

import elec332.test.wire.ground.tile.SubTileWire;
import elec332.test.wire.terminal.tile.SubTileTerminal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;

/**
 * Created by Elec332 on 1-2-2019
 */
public class BlockTest extends BlockSubTile {

    public BlockTest() {
        super(Properties.create(Material.CACTUS), SubTileWire.class, SubTileTerminal.class);
    }


    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

}
