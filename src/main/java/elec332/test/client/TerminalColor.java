package elec332.test.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReaderBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Elec332 on 13-2-2019
 */
public class TerminalColor implements IBlockColor, IItemColor {

    @Override
    public int getColor(@Nonnull IBlockState state, @Nullable IWorldReaderBase world, @Nullable BlockPos pos, int index) {
        return index;
    }

    @Override
    public int getColor(@Nonnull ItemStack stack, int index) {
        return index;
    }

}
