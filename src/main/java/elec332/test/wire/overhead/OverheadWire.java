package elec332.test.wire.overhead;

import elec332.core.util.NBTBuilder;
import elec332.test.api.util.ConnectionPoint;
import elec332.test.wire.AbstractWire;
import elec332.test.wire.WireData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;

import javax.annotation.concurrent.Immutable;
import java.util.UUID;

/**
 * Created by Elec332 on 7-11-2017.
 */
@Immutable
public class OverheadWire extends AbstractWire {

    public static OverheadWire read(NBTTagCompound tag) {
        NBTBuilder data = NBTBuilder.from(tag);
        ConnectionPoint start = ConnectionPoint.readFrom(data.getCompound("startCP"));
        ConnectionPoint end = ConnectionPoint.readFrom(data.getCompound("endCP"));
        UUID ident = data.getUUID("ident");
        Vec3d startV = data.getVec("startV");
        Vec3d endV = data.getVec("endV");
        return new OverheadWire(start, startV, end, endV, WireData.read(data.getCompound("wireD")), ident);
    }

    public OverheadWire(ConnectionPoint start, Vec3d startV, ConnectionPoint end, Vec3d endV, WireData wireData) {
        this(start, startV, end, endV, wireData, UUID.randomUUID());
    }

    private OverheadWire(ConnectionPoint start, Vec3d startV, ConnectionPoint end, Vec3d endV, WireData wireData, UUID identifier) {
        super(start, end, wireData);
        this.startPos = n ? startV : endV;
        this.endPos = n ? endV : startV;
        this.length = startPos.distanceTo(endPos);
        this.identifier = identifier;
    }

    private final Vec3d startPos, endPos;
    private final double length;
    private final UUID identifier;

    public Vec3d getStart() {
        return startPos;
    }

    public Vec3d getEnd() {
        return endPos;
    }

    public UUID getIdentifier() {
        return identifier;
    }

    public double getLength() {
        return length;
    }

    public void drop() {
        //todo
    }

    public boolean containsPost(ConnectionPoint cp) {
        return startPoint.equals(cp) || endPoint.equals(cp);
    }

    public double getResistance() {
        return wireData.getResistivity(getLength());
    }

    public NBTTagCompound serialize() {
        NBTBuilder ret = new NBTBuilder();
        ret.setTag("startCP", startPoint.serialize());
        ret.setTag("endCP", endPoint.serialize());
        ret.setUUID("ident", identifier);
        ret.setVec("startV", startPos);
        ret.setVec("endV", endPos);
        ret.setTag("wireD", wireData.serialize());
        return ret.get();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof OverheadWire && super.equals(obj) && ((OverheadWire) obj).startPos.equals(startPos) && ((OverheadWire) obj).endPos.equals(endPos) && ((OverheadWire) obj).identifier.equals(identifier);
    }

}
