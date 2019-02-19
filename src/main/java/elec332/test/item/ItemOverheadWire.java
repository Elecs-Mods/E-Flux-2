package elec332.test.item;

import com.google.common.base.Preconditions;
import elec332.core.item.AbstractItem;
import elec332.core.util.PlayerHelper;
import elec332.core.util.VectorHelper;
import elec332.core.world.WorldHelper;
import elec332.test.api.TestModAPI;
import elec332.test.api.electricity.IElectricityDevice;
import elec332.test.api.electricity.IEnergyObject;
import elec332.test.api.util.ConnectionPoint;
import elec332.test.api.wire.EnumWireThickness;
import elec332.test.api.wire.WireConnectionMethod;
import elec332.test.wire.EnumWireType;
import elec332.test.wire.WireData;
import elec332.test.wire.overhead.OverheadWire;
import elec332.test.wire.overhead.OverheadWireHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * Created by Elec332 on 8-2-2019
 */
public class ItemOverheadWire extends AbstractItem {

    public ItemOverheadWire(Properties itemBuilder) {
        super(itemBuilder);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }
        EntityPlayer player = Preconditions.checkNotNull(context.getPlayer());
        EnumHand hand = player.getActiveHand();
        ItemStack stack = player.getHeldItem(hand);
        NBTTagCompound tag = stack.getTag();
        BlockPos pos = context.getPos();
        TileEntity tile = WorldHelper.getTileAt(world, pos);
        IElectricityDevice energyObject = tile == null ? null : tile.getCapability(TestModAPI.ELECTRICITY_CAP).orElse(null);
        if (energyObject == null) {
            return EnumActionResult.SUCCESS;
        }
        Vec3d hitVec = new Vec3d(context.getHitX(), context.getHitY(), context.getHitZ());
        EnumFacing facing = context.getFace();
        ConnectionPoint cp = null;
        for (IEnergyObject obj : energyObject.getInternalComponents()) {
            cp = obj.getConnectionPoint(facing, hitVec);
            if (cp != null) {
                break;
            }
        }
        if (cp == null) {
            return EnumActionResult.SUCCESS;
        }
        hitVec = VectorHelper.addTo(hitVec, pos);
        if (tag == null || tag.isEmpty()) {
            tag = cp.serialize();
            tag.putDouble("xH", hitVec.x);
            tag.putDouble("yH", hitVec.y);
            tag.putDouble("zH", hitVec.z);
            stack.setTag(tag);
            PlayerHelper.sendMessageToPlayer(player, "StartWire");
        } else {
            Vec3d otherHVec = new Vec3d(tag.getDouble("xH"), tag.getDouble("yH"), tag.getDouble("zH"));
            ConnectionPoint newcp = ConnectionPoint.readFrom(tag);

            if (newcp.equals(cp)) {
                PlayerHelper.sendMessageToPlayer(player, "clearWire");
                stack.setTag(null);
                return EnumActionResult.FAIL;
            }
            stack.setTag(null);
            OverheadWire wire = new OverheadWire(newcp, otherHVec, cp, hitVec, new WireData(EnumWireType.TEST, EnumWireThickness.AWG_00, WireConnectionMethod.OVERHEAD));
            OverheadWireHandler.INSTANCE.add(wire, context.getWorld());
            PlayerHelper.sendMessageToPlayer(player, "addedWire");
        }
        return EnumActionResult.SUCCESS;
    }

}
