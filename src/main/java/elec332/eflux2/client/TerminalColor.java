package elec332.eflux2.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Elec332 on 13-2-2019
 */
public class TerminalColor implements IBlockColor, IItemColor {

    @Override
    public int getColor(@Nonnull BlockState state, @Nullable IEnviromentBlockReader world, @Nullable BlockPos pos, int index) {
        return index;
    }

    @Override
    public int getColor(@Nonnull ItemStack stack, int index) {
        return index;
    }

}
