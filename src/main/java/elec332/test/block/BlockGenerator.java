package elec332.test.block;

import elec332.core.world.WorldHelper;
import elec332.test.tile.TileTestGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Particles;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by Elec332 on 7-2-2019
 */
public class BlockGenerator extends BlockTile {

    public BlockGenerator(Properties builder) {
        super(builder, TileTestGenerator::new);
    }

    @Override
    public void animateTick(IBlockState stateIn, World world, BlockPos pos, Random random) {
        TileEntity tile = WorldHelper.getTileAt(world, pos);
        if (tile instanceof TileTestGenerator && ((TileTestGenerator) tile).isActive()) {
            EnumFacing enumfacing = ((TileTestGenerator) tile).getTileFacing();
            double d0 = (double) pos.getX() + 0.5D;
            double d1 = (double) pos.getY() + random.nextDouble() * 6.0D / 16.0D;
            double d2 = (double) pos.getZ() + 0.5D;
            double d3 = 0.52D;
            double d4 = random.nextDouble() * 0.6D - 0.3D;

            switch (enumfacing) {
                case WEST:
                    world.addParticle(Particles.SMOKE, d0 - d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
                    world.addParticle(Particles.FLAME, d0 - d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
                    break;
                case EAST:
                    world.addParticle(Particles.SMOKE, d0 + d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
                    world.addParticle(Particles.FLAME, d0 + d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
                    break;
                case NORTH:
                    world.addParticle(Particles.SMOKE, d0 + d4, d1, d2 - d3, 0.0D, 0.0D, 0.0D);
                    world.addParticle(Particles.FLAME, d0 + d4, d1, d2 - d3, 0.0D, 0.0D, 0.0D);
                    break;
                case SOUTH:
                    world.addParticle(Particles.SMOKE, d0 + d4, d1, d2 + d3, 0.0D, 0.0D, 0.0D);
                    world.addParticle(Particles.FLAME, d0 + d4, d1, d2 + d3, 0.0D, 0.0D, 0.0D);
            }
        }
    }

}
