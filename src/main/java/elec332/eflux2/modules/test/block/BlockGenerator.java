package elec332.eflux2.modules.test.block;

import elec332.core.world.WorldHelper;
import elec332.eflux2.block.BlockTileEntity;
import elec332.eflux2.modules.test.tile.TileTestGenerator;
import net.minecraft.block.BlockState;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by Elec332 on 7-2-2019
 */
public class BlockGenerator extends BlockTileEntity {

    public BlockGenerator(Properties builder) {
        super(builder, TileTestGenerator::new);
    }

    @Override
    public void animateTick(BlockState stateIn, World world, BlockPos pos, Random random) {
        TileEntity tile = WorldHelper.getTileAt(world, pos);
        if (tile instanceof TileTestGenerator && ((TileTestGenerator) tile).isActive()) {
            Direction Direction = ((TileTestGenerator) tile).getTileFacing();
            double d0 = (double) pos.getX() + 0.5D;
            double d1 = (double) pos.getY() + random.nextDouble() * 6.0D / 16.0D;
            double d2 = (double) pos.getZ() + 0.5D;
            double d3 = 0.52D;
            double d4 = random.nextDouble() * 0.6D - 0.3D;

            switch (Direction) {
                case WEST:
                    world.addParticle(ParticleTypes.SMOKE, d0 - d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
                    world.addParticle(ParticleTypes.FLAME, d0 - d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
                    break;
                case EAST:
                    world.addParticle(ParticleTypes.SMOKE, d0 + d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
                    world.addParticle(ParticleTypes.FLAME, d0 + d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
                    break;
                case NORTH:
                    world.addParticle(ParticleTypes.SMOKE, d0 + d4, d1, d2 - d3, 0.0D, 0.0D, 0.0D);
                    world.addParticle(ParticleTypes.FLAME, d0 + d4, d1, d2 - d3, 0.0D, 0.0D, 0.0D);
                    break;
                case SOUTH:
                    world.addParticle(ParticleTypes.SMOKE, d0 + d4, d1, d2 + d3, 0.0D, 0.0D, 0.0D);
                    world.addParticle(ParticleTypes.FLAME, d0 + d4, d1, d2 + d3, 0.0D, 0.0D, 0.0D);
            }
        }
    }

}
